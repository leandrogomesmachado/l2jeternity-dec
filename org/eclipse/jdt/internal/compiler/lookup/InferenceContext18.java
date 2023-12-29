package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.util.Sorting;

public class InferenceContext18 {
   static final boolean SIMULATE_BUG_JDK_8026527 = true;
   static final boolean SHOULD_WORKAROUND_BUG_JDK_8054721 = true;
   static final boolean ARGUMENT_CONSTRAINTS_ARE_SOFT = false;
   InvocationSite currentInvocation;
   Expression[] invocationArguments;
   InferenceVariable[] inferenceVariables;
   int nextVarId;
   ConstraintFormula[] initialConstraints;
   ConstraintExpressionFormula[] finalConstraints;
   BoundSet currentBounds;
   int inferenceKind;
   public int stepCompleted = 0;
   public static final int NOT_INFERRED = 0;
   public static final int APPLICABILITY_INFERRED = 1;
   public static final int TYPE_INFERRED = 2;
   public List<ConstraintFormula> constraintsWithUncheckedConversion;
   public boolean usesUncheckedConversion;
   public InferenceContext18 outerContext;
   Scope scope;
   LookupEnvironment environment;
   ReferenceBinding object;
   public BoundSet b2;
   private InferenceVariable[] internedVariables;
   public static final int CHECK_UNKNOWN = 0;
   public static final int CHECK_STRICT = 1;
   public static final int CHECK_LOOSE = 2;
   public static final int CHECK_VARARG = 3;
   int captureId = 0;

   private InferenceVariable getInferenceVariable(TypeBinding typeParameter, int rank, InvocationSite site) {
      InferenceContext18 outermostContext = this.environment.currentInferenceContext;
      if (outermostContext == null) {
         outermostContext = this;
      }

      int i = 0;
      InferenceVariable[] interned = outermostContext.internedVariables;
      if (interned == null) {
         outermostContext.internedVariables = new InferenceVariable[10];
      } else {
         for(InferenceVariable var : interned) {
            if (var == null) {
               break;
            }

            if (var.typeParameter == typeParameter && var.rank == rank && this.isSameSite(var.site, site)) {
               return var;
            }
         }

         byte len;
         if (i >= len) {
            System.arraycopy(interned, 0, outermostContext.internedVariables = new InferenceVariable[len + 10], 0, len);
         }
      }

      return outermostContext.internedVariables[i] = new InferenceVariable(typeParameter, rank, this.nextVarId++, site, this.environment, this.object);
   }

   boolean isSameSite(InvocationSite site1, InvocationSite site2) {
      if (site1 == site2) {
         return true;
      } else if (site1 != null && site2 != null) {
         return site1.sourceStart() == site2.sourceStart() && site1.sourceEnd() == site2.sourceEnd() && site1.toString().equals(site2.toString());
      } else {
         return false;
      }
   }

   public InferenceContext18(Scope scope, Expression[] arguments, InvocationSite site, InferenceContext18 outerContext) {
      this.scope = scope;
      this.environment = scope.environment();
      this.object = scope.getJavaLangObject();
      this.invocationArguments = arguments;
      this.currentInvocation = site;
      this.outerContext = outerContext;
      if (site instanceof Invocation) {
         scope.compilationUnitScope().registerInferredInvocation((Invocation)site);
      }
   }

   public InferenceContext18(Scope scope) {
      this.scope = scope;
      this.environment = scope.environment();
      this.object = scope.getJavaLangObject();
   }

   public InferenceVariable[] createInitialBoundSet(TypeVariableBinding[] typeParameters) {
      if (this.currentBounds == null) {
         this.currentBounds = new BoundSet();
      }

      if (typeParameters != null) {
         InferenceVariable[] newInferenceVariables = this.addInitialTypeVariableSubstitutions(typeParameters);
         this.currentBounds.addBoundsFromTypeParameters(this, typeParameters, newInferenceVariables);
         return newInferenceVariables;
      } else {
         return Binding.NO_INFERENCE_VARIABLES;
      }
   }

   public TypeBinding substitute(TypeBinding type) {
      InferenceSubstitution inferenceSubstitution = new InferenceSubstitution(this);
      return inferenceSubstitution.substitute(inferenceSubstitution, type);
   }

   public void createInitialConstraintsForParameters(TypeBinding[] parameters, boolean checkVararg, TypeBinding varArgsType, MethodBinding method) {
      if (this.invocationArguments != null) {
         int len = checkVararg ? parameters.length - 1 : Math.min(parameters.length, this.invocationArguments.length);
         int maxConstraints = checkVararg ? this.invocationArguments.length : len;
         int numConstraints = 0;
         boolean ownConstraints;
         if (this.initialConstraints == null) {
            this.initialConstraints = new ConstraintFormula[maxConstraints];
            ownConstraints = true;
         } else {
            numConstraints = this.initialConstraints.length;
            maxConstraints += numConstraints;
            System.arraycopy(this.initialConstraints, 0, this.initialConstraints = new ConstraintFormula[maxConstraints], 0, numConstraints);
            ownConstraints = false;
         }

         for(int i = 0; i < len; ++i) {
            TypeBinding thetaF = this.substitute(parameters[i]);
            if (this.invocationArguments[i].isPertinentToApplicability(parameters[i], method)) {
               this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, 1, false);
            } else if (!this.isTypeVariableOfCandidate(parameters[i], method)) {
               this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, 8);
            }
         }

         if (checkVararg && varArgsType instanceof ArrayBinding) {
            varArgsType = ((ArrayBinding)varArgsType).elementsType();
            TypeBinding thetaF = this.substitute(varArgsType);

            for(int i = len; i < this.invocationArguments.length; ++i) {
               if (this.invocationArguments[i].isPertinentToApplicability(varArgsType, method)) {
                  this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, 1, false);
               } else if (!this.isTypeVariableOfCandidate(varArgsType, method)) {
                  this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, 8);
               }
            }
         }

         if (numConstraints == 0) {
            this.initialConstraints = ConstraintFormula.NO_CONSTRAINTS;
         } else if (numConstraints < maxConstraints) {
            System.arraycopy(this.initialConstraints, 0, this.initialConstraints = new ConstraintFormula[numConstraints], 0, numConstraints);
         }

         if (ownConstraints) {
            int length = this.initialConstraints.length;
            System.arraycopy(this.initialConstraints, 0, this.finalConstraints = new ConstraintExpressionFormula[length], 0, length);
         }
      }
   }

   private boolean isTypeVariableOfCandidate(TypeBinding type, MethodBinding candidate) {
      if (type instanceof TypeVariableBinding) {
         Binding declaringElement = ((TypeVariableBinding)type).declaringElement;
         if (declaringElement == candidate) {
            return true;
         }

         if (candidate.isConstructor() && declaringElement == candidate.declaringClass) {
            return true;
         }
      }

      return false;
   }

   private InferenceVariable[] addInitialTypeVariableSubstitutions(TypeBinding[] typeVariables) {
      int len = typeVariables.length;
      if (len == 0) {
         if (this.inferenceVariables == null) {
            this.inferenceVariables = Binding.NO_INFERENCE_VARIABLES;
         }

         return Binding.NO_INFERENCE_VARIABLES;
      } else {
         InferenceVariable[] newVariables = new InferenceVariable[len];

         for(int i = 0; i < len; ++i) {
            newVariables[i] = this.getInferenceVariable(typeVariables[i], i, this.currentInvocation);
         }

         if (this.inferenceVariables != null && this.inferenceVariables.length != 0) {
            int prev = this.inferenceVariables.length;
            System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len + prev], 0, prev);
            System.arraycopy(newVariables, 0, this.inferenceVariables, prev, len);
         } else {
            this.inferenceVariables = newVariables;
         }

         return newVariables;
      }
   }

   public InferenceVariable[] addTypeVariableSubstitutions(TypeBinding[] typeVariables) {
      int len2 = typeVariables.length;
      InferenceVariable[] newVariables = new InferenceVariable[len2];
      InferenceVariable[] toAdd = new InferenceVariable[len2];
      int numToAdd = 0;

      for(int i = 0; i < typeVariables.length; ++i) {
         if (typeVariables[i] instanceof InferenceVariable) {
            newVariables[i] = (InferenceVariable)typeVariables[i];
         } else {
            toAdd[numToAdd++] = newVariables[i] = this.getInferenceVariable(typeVariables[i], i, this.currentInvocation);
         }
      }

      if (numToAdd > 0) {
         int start = 0;
         if (this.inferenceVariables != null) {
            int len1 = this.inferenceVariables.length;
            System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len1 + numToAdd], 0, len1);
            start = len1;
         } else {
            this.inferenceVariables = new InferenceVariable[numToAdd];
         }

         System.arraycopy(toAdd, 0, this.inferenceVariables, start, numToAdd);
      }

      return newVariables;
   }

   public void addThrowsContraints(TypeBinding[] parameters, InferenceVariable[] variables, ReferenceBinding[] thrownExceptions) {
      for(int i = 0; i < parameters.length; ++i) {
         TypeBinding parameter = parameters[i];

         for(int j = 0; j < thrownExceptions.length; ++j) {
            if (TypeBinding.equalsEquals(parameter, thrownExceptions[j])) {
               this.currentBounds.inThrows.add(variables[i].prototype());
               break;
            }
         }
      }
   }

   public void inferInvocationApplicability(MethodBinding method, TypeBinding[] arguments, boolean isDiamond) {
      ConstraintExpressionFormula.inferInvocationApplicability(this, method, arguments, isDiamond, this.inferenceKind);
   }

   public BoundSet inferInvocationType(TypeBinding expectedType, InvocationSite invocationSite, MethodBinding method) throws InferenceFailureException {
      if (expectedType == null && method.returnType != null) {
         this.substitute(method.returnType);
      }

      this.currentBounds = this.b2.copy();

      try {
         if (expectedType != null
            && expectedType != TypeBinding.VOID
            && invocationSite instanceof Expression
            && ((Expression)invocationSite).isPolyExpression(method)
            && !ConstraintExpressionFormula.inferPolyInvocationType(this, invocationSite, expectedType, method)) {
            return null;
         } else {
            Set<ConstraintFormula> c = new HashSet<>();
            if (!this.addConstraintsToC(this.invocationArguments, c, method, this.inferenceKind, false, invocationSite)) {
               return null;
            } else {
               BoundSet connectivityBoundSet = this.currentBounds.copy();

               for(ConstraintFormula cf : c) {
                  connectivityBoundSet.reduceOneConstraint(this, cf);
               }

               List<Set<InferenceVariable>> components = connectivityBoundSet.computeConnectedComponents(this.inferenceVariables);

               while(!c.isEmpty()) {
                  Set<ConstraintFormula> bottomSet = this.findBottomSet(c, this.allOutputVariables(c), components);
                  if (bottomSet.isEmpty()) {
                     bottomSet.add(this.pickFromCycle(c));
                  }

                  c.removeAll(bottomSet);
                  Set<InferenceVariable> allInputs = new HashSet<>();
                  Iterator<ConstraintFormula> bottomIt = bottomSet.iterator();

                  while(bottomIt.hasNext()) {
                     allInputs.addAll(bottomIt.next().inputVariables(this));
                  }

                  InferenceVariable[] variablesArray = allInputs.toArray(new InferenceVariable[allInputs.size()]);
                  if (!this.currentBounds.incorporate(this)) {
                     return null;
                  }

                  BoundSet solution = this.resolve(variablesArray);
                  if (solution == null) {
                     solution = this.resolve(this.inferenceVariables);
                  }

                  for(ConstraintFormula constraint : bottomSet) {
                     if (solution != null && !constraint.applySubstitution(solution, variablesArray)) {
                        return null;
                     }

                     if (!this.currentBounds.reduceOneConstraint(this, constraint)) {
                        return null;
                     }
                  }
               }

               BoundSet solution = this.solve();
               if (solution != null && this.isResolved(solution)) {
                  this.reportUncheckedConversions(solution);
                  return this.currentBounds = solution;
               } else {
                  this.currentBounds = this.b2;
                  return null;
               }
            }
         }
      } finally {
         this.stepCompleted = 2;
      }
   }

   private boolean addConstraintsToC(
      Expression[] exprs, Set<ConstraintFormula> c, MethodBinding method, int inferenceKindForMethod, boolean interleaved, InvocationSite site
   ) throws InferenceFailureException {
      if (exprs != null) {
         int k = exprs.length;
         int p = method.parameters.length;
         if (k < (method.isVarargs() ? p - 1 : p)) {
            return false;
         }

         TypeBinding[] fs;
         switch(inferenceKindForMethod) {
            case 1:
            case 2:
               fs = method.parameters;
               break;
            case 3:
               fs = this.varArgTypes(method.parameters, k);
               break;
            default:
               throw new IllegalStateException("Unexpected checkKind " + this.inferenceKind);
         }

         for(int i = 0; i < k; ++i) {
            TypeBinding fsi = fs[Math.min(i, p - 1)];
            InferenceSubstitution inferenceSubstitution = new InferenceSubstitution(this.environment, this.inferenceVariables, site);
            TypeBinding substF = inferenceSubstitution.substitute(inferenceSubstitution, fsi);
            if (!this.addConstraintsToC_OneExpr(exprs[i], c, fsi, substF, method, interleaved)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean addConstraintsToC_OneExpr(
      Expression expri, Set<ConstraintFormula> c, TypeBinding fsi, TypeBinding substF, MethodBinding method, boolean interleaved
   ) throws InferenceFailureException {
      if (!expri.isPertinentToApplicability(fsi, method)) {
         c.add(new ConstraintExpressionFormula(expri, substF, 1, false));
      }

      if (expri instanceof FunctionalExpression) {
         c.add(new ConstraintExceptionFormula((FunctionalExpression)expri, substF));
         if (expri instanceof LambdaExpression) {
            LambdaExpression lambda = (LambdaExpression)expri;
            BlockScope skope = lambda.enclosingScope;
            if (substF.isFunctionalInterface(skope)) {
               ReferenceBinding t = (ReferenceBinding)substF;
               ParameterizedTypeBinding withWildCards = parameterizedWithWildcard(t);
               if (withWildCards != null) {
                  t = ConstraintExpressionFormula.findGroundTargetType(this, skope, lambda, withWildCards);
               }

               if (!t.isProperType(true) && t.isParameterizedType()) {
                  t = (ReferenceBinding)Scope.substitute(this.getResultSubstitution(this.currentBounds, false), t);
               }

               MethodBinding functionType;
               if (t != null
                  && (functionType = t.getSingleAbstractMethod(skope, true)) != null
                  && (lambda = lambda.resolveExpressionExpecting(t, this.scope, this)) != null) {
                  TypeBinding r = functionType.returnType;
                  Expression[] resultExpressions = lambda.resultExpressions();
                  int i = 0;

                  for(int length = resultExpressions == null ? 0 : resultExpressions.length; i < length; ++i) {
                     Expression resultExpression = resultExpressions[i];
                     if (!this.addConstraintsToC_OneExpr(resultExpression, c, r.original(), r, method, true)) {
                        return false;
                     }
                  }
               }
            }
         }
      } else {
         if (expri instanceof Invocation && expri.isPolyExpression()) {
            if (substF.isProperType(true)) {
               return true;
            }

            Invocation invocation = (Invocation)expri;
            MethodBinding innerMethod = invocation.binding();
            if (innerMethod == null) {
               return true;
            }

            Expression[] arguments = invocation.arguments();
            TypeBinding[] argumentTypes = arguments == null ? Binding.NO_PARAMETERS : new TypeBinding[arguments.length];

            for(int i = 0; i < argumentTypes.length; ++i) {
               argumentTypes[i] = arguments[i].resolvedType;
            }

            InferenceContext18 innerContext = null;
            if (innerMethod instanceof ParameterizedGenericMethodBinding) {
               innerContext = invocation.getInferenceContext((ParameterizedGenericMethodBinding)innerMethod);
            }

            if (interleaved && innerContext != null) {
               MethodBinding shallowMethod = innerMethod.shallowOriginal();
               innerContext.outerContext = this;
               if (innerContext.stepCompleted < 1) {
                  innerContext.inferInvocationApplicability(shallowMethod, argumentTypes, shallowMethod.isConstructor());
               }

               if (!ConstraintExpressionFormula.inferPolyInvocationType(innerContext, invocation, substF, shallowMethod)) {
                  return false;
               }

               return innerContext.addConstraintsToC(arguments, c, innerMethod.genericMethod(), innerContext.inferenceKind, interleaved, invocation);
            }

            int applicabilityKind = this.getInferenceKind(innerMethod, argumentTypes);
            return this.addConstraintsToC(arguments, c, innerMethod.genericMethod(), applicabilityKind, interleaved, invocation);
         }

         if (expri instanceof ConditionalExpression) {
            ConditionalExpression ce = (ConditionalExpression)expri;
            if (this.addConstraintsToC_OneExpr(ce.valueIfTrue, c, fsi, substF, method, interleaved)
               && this.addConstraintsToC_OneExpr(ce.valueIfFalse, c, fsi, substF, method, interleaved)) {
               return true;
            }

            return false;
         }
      }

      return true;
   }

   protected int getInferenceKind(MethodBinding nonGenericMethod, TypeBinding[] argumentTypes) {
      switch(this.scope.parameterCompatibilityLevel(nonGenericMethod, argumentTypes)) {
         case 1:
            return 2;
         case 2:
            return 3;
         default:
            return 1;
      }
   }

   public ReferenceBinding inferFunctionalInterfaceParameterization(
      LambdaExpression lambda, BlockScope blockScope, ParameterizedTypeBinding targetTypeWithWildCards
   ) {
      TypeBinding[] q = this.createBoundsForFunctionalInterfaceParameterizationInference(targetTypeWithWildCards);
      if (q != null && q.length == lambda.arguments().length && this.reduceWithEqualityConstraints(lambda.argumentTypes(), q)) {
         ReferenceBinding genericType = targetTypeWithWildCards.genericType();
         TypeBinding[] a = targetTypeWithWildCards.arguments;
         TypeBinding[] aprime = this.getFunctionInterfaceArgumentSolutions(a);
         return blockScope.environment().createParameterizedType(genericType, aprime, targetTypeWithWildCards.enclosingType());
      } else {
         return targetTypeWithWildCards;
      }
   }

   TypeBinding[] createBoundsForFunctionalInterfaceParameterizationInference(ParameterizedTypeBinding functionalInterface) {
      if (this.currentBounds == null) {
         this.currentBounds = new BoundSet();
      }

      TypeBinding[] a = functionalInterface.arguments;
      if (a == null) {
         return null;
      } else {
         InferenceVariable[] alpha = this.addInitialTypeVariableSubstitutions(a);

         for(int i = 0; i < a.length; ++i) {
            TypeBound bound;
            if (a[i].kind() == 516) {
               WildcardBinding wildcard = (WildcardBinding)a[i];
               switch(wildcard.boundKind) {
                  case 0:
                     bound = new TypeBound(alpha[i], this.object, 2);
                     break;
                  case 1:
                     bound = new TypeBound(alpha[i], wildcard.allBounds(), 2);
                     break;
                  case 2:
                     bound = new TypeBound(alpha[i], wildcard.bound, 3);
                     break;
                  default:
                     continue;
               }
            } else {
               bound = new TypeBound(alpha[i], a[i], 4);
            }

            this.currentBounds.addBound(bound, this.environment);
         }

         TypeBinding falpha = this.substitute(functionalInterface);
         return falpha.getSingleAbstractMethod(this.scope, true).parameters;
      }
   }

   public boolean reduceWithEqualityConstraints(TypeBinding[] p, TypeBinding[] q) {
      if (p != null) {
         for(int i = 0; i < p.length; ++i) {
            try {
               if (!this.reduceAndIncorporate(ConstraintTypeFormula.create(p[i], q[i], 4))) {
                  return false;
               }
            } catch (InferenceFailureException var4) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean isMoreSpecificThan(MethodBinding m1, MethodBinding m2, boolean isVarArgs, boolean isVarArgs2) {
      if (isVarArgs != isVarArgs2) {
         return isVarArgs2;
      } else {
         Expression[] arguments = this.invocationArguments;
         int numInvocArgs = arguments == null ? 0 : arguments.length;
         TypeVariableBinding[] p = m2.typeVariables();
         TypeBinding[] s = m1.parameters;
         TypeBinding[] t = new TypeBinding[m2.parameters.length];
         this.createInitialBoundSet(p);

         for(int i = 0; i < t.length; ++i) {
            t[i] = this.substitute(m2.parameters[i]);
         }

         try {
            for(int i = 0; i < numInvocArgs; ++i) {
               TypeBinding si = getParameter(s, i, isVarArgs);
               TypeBinding ti = getParameter(t, i, isVarArgs);
               Boolean result = this.moreSpecificMain(si, ti, this.invocationArguments[i]);
               if (result == Boolean.FALSE) {
                  return false;
               }

               if (result == null && !this.reduceAndIncorporate(ConstraintTypeFormula.create(si, ti, 2))) {
                  return false;
               }
            }

            if (t.length == numInvocArgs + 1) {
               TypeBinding skplus1 = getParameter(s, numInvocArgs, true);
               TypeBinding tkplus1 = getParameter(t, numInvocArgs, true);
               if (!this.reduceAndIncorporate(ConstraintTypeFormula.create(skplus1, tkplus1, 2))) {
                  return false;
               }
            }

            return this.solve() != null;
         } catch (InferenceFailureException var14) {
            return false;
         }
      }
   }

   private Boolean moreSpecificMain(TypeBinding si, TypeBinding ti, Expression expri) throws InferenceFailureException {
      if (si.isProperType(true) && ti.isProperType(true)) {
         return expri.sIsMoreSpecific(si, ti, this.scope) ? Boolean.TRUE : Boolean.FALSE;
      } else if (!ti.isFunctionalInterface(this.scope)) {
         return null;
      } else {
         TypeBinding funcI = ti.original();
         if (!si.isFunctionalInterface(this.scope)) {
            return null;
         } else if (!this.siSuperI(si, funcI) && !this.siSubI(si, funcI)) {
            if (si instanceof IntersectionTypeBinding18) {
               TypeBinding[] elements = ((IntersectionTypeBinding18)si).intersectingTypes;
               int i = 0;

               while(true) {
                  if (i >= elements.length) {
                     return null;
                  }

                  if (!this.siSuperI(elements[i], funcI)) {
                     for(int ix = 0; ix < elements.length; ++ix) {
                        if (this.siSubI(elements[ix], funcI)) {
                           return null;
                        }
                     }
                     break;
                  }

                  ++i;
               }
            }

            TypeBinding siCapture = si.capture(this.scope, expri.sourceStart, expri.sourceEnd);
            MethodBinding sam = siCapture.getSingleAbstractMethod(this.scope, false);
            TypeBinding[] u = sam.parameters;
            TypeBinding r1 = (TypeBinding)(sam.isConstructor() ? sam.declaringClass : sam.returnType);
            sam = ti.getSingleAbstractMethod(this.scope, true);
            TypeBinding[] v = sam.parameters;
            TypeBinding r2 = (TypeBinding)(sam.isConstructor() ? sam.declaringClass : sam.returnType);
            return this.checkExpression(expri, u, r1, v, r2);
         } else {
            return null;
         }
      }
   }

   private boolean checkExpression(Expression expri, TypeBinding[] u, TypeBinding r1, TypeBinding[] v, TypeBinding r2) throws InferenceFailureException {
      if (expri instanceof LambdaExpression && !((LambdaExpression)expri).argumentsTypeElided()) {
         for(int i = 0; i < u.length; ++i) {
            if (!this.reduceAndIncorporate(ConstraintTypeFormula.create(u[i], v[i], 4))) {
               return false;
            }
         }

         if (r2.id == 6) {
            return true;
         } else {
            LambdaExpression lambda = (LambdaExpression)expri;
            Expression[] results = lambda.resultExpressions();
            if (results != Expression.NO_EXPRESSIONS) {
               if (r1.isFunctionalInterface(this.scope) && r2.isFunctionalInterface(this.scope) && !r1.isCompatibleWith(r2) && !r2.isCompatibleWith(r1)) {
                  for(int i = 0; i < results.length; ++i) {
                     if (!this.checkExpression(results[i], u, r1, v, r2)) {
                        return false;
                     }
                  }

                  return true;
               }

               if (r1.isPrimitiveType() && !r2.isPrimitiveType()) {
                  int i = 0;

                  while(true) {
                     if (i >= results.length) {
                        return true;
                     }

                     if (results[i].isPolyExpression() || results[i].resolvedType != null && !results[i].resolvedType.isPrimitiveType()) {
                        break;
                     }

                     ++i;
                  }
               }

               if (r2.isPrimitiveType() && !r1.isPrimitiveType()) {
                  int i = 0;

                  while(true) {
                     if (i >= results.length) {
                        return true;
                     }

                     if ((results[i].isPolyExpression() || results[i].resolvedType == null || results[i].resolvedType.isPrimitiveType())
                        && !results[i].isPolyExpression()) {
                        break;
                     }

                     ++i;
                  }
               }
            }

            return this.reduceAndIncorporate(ConstraintTypeFormula.create(r1, r2, 2));
         }
      } else if (expri instanceof ReferenceExpression && ((ReferenceExpression)expri).isExactMethodReference()) {
         ReferenceExpression reference = (ReferenceExpression)expri;

         for(int i = 0; i < u.length; ++i) {
            if (!this.reduceAndIncorporate(ConstraintTypeFormula.create(u[i], v[i], 4))) {
               return false;
            }
         }

         if (r2.id == 6) {
            return true;
         } else {
            MethodBinding method = reference.getExactMethod();
            TypeBinding returnType = (TypeBinding)(method.isConstructor() ? method.declaringClass : method.returnType);
            if (r1.isPrimitiveType() && !r2.isPrimitiveType() && returnType.isPrimitiveType()) {
               return true;
            } else {
               return r2.isPrimitiveType() && !r1.isPrimitiveType() && !returnType.isPrimitiveType()
                  ? true
                  : this.reduceAndIncorporate(ConstraintTypeFormula.create(r1, r2, 2));
            }
         }
      } else if (expri instanceof ConditionalExpression) {
         ConditionalExpression cond = (ConditionalExpression)expri;
         return this.checkExpression(cond.valueIfTrue, u, r1, v, r2) && this.checkExpression(cond.valueIfFalse, u, r1, v, r2);
      } else {
         return false;
      }
   }

   private boolean siSuperI(TypeBinding si, TypeBinding funcI) {
      if (!TypeBinding.equalsEquals(si, funcI) && !TypeBinding.equalsEquals(si.original(), funcI)) {
         TypeBinding[] superIfcs = funcI.superInterfaces();
         if (superIfcs == null) {
            return false;
         } else {
            for(int i = 0; i < superIfcs.length; ++i) {
               if (this.siSuperI(si, superIfcs[i].original())) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private boolean siSubI(TypeBinding si, TypeBinding funcI) {
      if (!TypeBinding.equalsEquals(si, funcI) && !TypeBinding.equalsEquals(si.original(), funcI)) {
         TypeBinding[] superIfcs = si.superInterfaces();
         if (superIfcs == null) {
            return false;
         } else {
            for(int i = 0; i < superIfcs.length; ++i) {
               if (this.siSubI(superIfcs[i], funcI)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public BoundSet solve(boolean inferringApplicability) throws InferenceFailureException {
      if (!this.reduce()) {
         return null;
      } else if (!this.currentBounds.incorporate(this)) {
         return null;
      } else {
         if (inferringApplicability) {
            this.b2 = this.currentBounds.copy();
         }

         BoundSet solution = this.resolve(this.inferenceVariables);
         if (inferringApplicability && solution != null && this.finalConstraints != null) {
            for(ConstraintExpressionFormula constraint : this.finalConstraints) {
               if (!constraint.left.isPolyExpression()) {
                  constraint.applySubstitution(solution, this.inferenceVariables);
                  if (!this.currentBounds.reduceOneConstraint(this, constraint)) {
                     return null;
                  }
               }
            }
         }

         return solution;
      }
   }

   public BoundSet solve() throws InferenceFailureException {
      return this.solve(false);
   }

   public BoundSet solve(InferenceVariable[] toResolve) throws InferenceFailureException {
      if (!this.reduce()) {
         return null;
      } else {
         return !this.currentBounds.incorporate(this) ? null : this.resolve(toResolve);
      }
   }

   private boolean reduce() throws InferenceFailureException {
      for(int i = 0; this.initialConstraints != null && i < this.initialConstraints.length; ++i) {
         ConstraintFormula currentConstraint = this.initialConstraints[i];
         if (currentConstraint != null) {
            this.initialConstraints[i] = null;
            if (!this.currentBounds.reduceOneConstraint(this, currentConstraint)) {
               return false;
            }
         }
      }

      this.initialConstraints = null;
      return true;
   }

   public boolean isResolved(BoundSet boundSet) {
      if (this.inferenceVariables != null) {
         for(int i = 0; i < this.inferenceVariables.length; ++i) {
            if (!boundSet.isInstantiated(this.inferenceVariables[i])) {
               return false;
            }
         }
      }

      return true;
   }

   public TypeBinding[] getSolutions(TypeVariableBinding[] typeParameters, InvocationSite site, BoundSet boundSet) {
      int len = typeParameters.length;
      TypeBinding[] substitutions = new TypeBinding[len];
      InferenceVariable[] outerVariables = null;
      if (this.outerContext != null && this.outerContext.stepCompleted < 2) {
         outerVariables = this.outerContext.inferenceVariables;
      }

      for(int i = 0; i < typeParameters.length; ++i) {
         for(int j = 0; j < this.inferenceVariables.length; ++j) {
            InferenceVariable variable = this.inferenceVariables[j];
            if (this.isSameSite(variable.site, site) && TypeBinding.equalsEquals(variable.typeParameter, typeParameters[i])) {
               TypeBinding outerVar = null;
               if (outerVariables != null && (outerVar = boundSet.getEquivalentOuterVariable(variable, outerVariables)) != null) {
                  substitutions[i] = outerVar;
                  break;
               }

               substitutions[i] = boundSet.getInstantiation(variable, this.environment);
               break;
            }
         }

         if (substitutions[i] == null) {
            return null;
         }
      }

      return substitutions;
   }

   public boolean reduceAndIncorporate(ConstraintFormula constraint) throws InferenceFailureException {
      return this.currentBounds.reduceOneConstraint(this, constraint);
   }

   private BoundSet resolve(InferenceVariable[] toResolve) throws InferenceFailureException {
      this.captureId = 0;
      BoundSet tmpBoundSet = this.currentBounds;
      Set<InferenceVariable> variableSet;
      if (this.inferenceVariables != null) {
         while((variableSet = this.getSmallestVariableSet(tmpBoundSet, toResolve)) != null) {
            int oldNumUninstantiated = tmpBoundSet.numUninstantiatedVariables(this.inferenceVariables);
            final int numVars = variableSet.size();
            if (numVars > 0) {
               final InferenceVariable[] variables;
               variables = variableSet.toArray(new InferenceVariable[numVars]);
               label100:
               if (!tmpBoundSet.hasCaptureBound(variableSet)) {
                  BoundSet prevBoundSet = tmpBoundSet;
                  tmpBoundSet = tmpBoundSet.copy();

                  for(int j = 0; j < variables.length; ++j) {
                     InferenceVariable variable = variables[j];
                     TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, true);
                     if (lowerBounds != Binding.NO_TYPES) {
                        TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
                        if (lub == TypeBinding.VOID || lub == null) {
                           return null;
                        }

                        tmpBoundSet.addBound(new TypeBound(variable, lub, 4), this.environment);
                     } else {
                        TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, true);
                        if (tmpBoundSet.inThrows.contains(variable.prototype()) && tmpBoundSet.hasOnlyTrivialExceptionBounds(variable, upperBounds)) {
                           TypeBinding runtimeException = this.scope.getType(TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION, 3);
                           tmpBoundSet.addBound(new TypeBound(variable, runtimeException, 4), this.environment);
                        } else {
                           TypeBinding glb = this.object;
                           if (upperBounds != Binding.NO_TYPES) {
                              if (upperBounds.length == 1) {
                                 glb = upperBounds[0];
                              } else {
                                 ReferenceBinding[] glbs = Scope.greaterLowerBound((ReferenceBinding[])upperBounds);
                                 if (glbs == null) {
                                    throw new UnsupportedOperationException("no glb for " + Arrays.asList(upperBounds));
                                 }

                                 if (glbs.length == 1) {
                                    glb = glbs[0];
                                 } else {
                                    IntersectionTypeBinding18 intersection = (IntersectionTypeBinding18)this.environment.createIntersectionType18(glbs);
                                    if (!ReferenceBinding.isConsistentIntersection(intersection.intersectingTypes)) {
                                       tmpBoundSet = prevBoundSet;
                                       break label100;
                                    }

                                    glb = intersection;
                                 }
                              }
                           }

                           tmpBoundSet.addBound(new TypeBound(variable, glb, 4), this.environment);
                        }
                     }
                  }

                  if (tmpBoundSet.incorporate(this)) {
                     continue;
                  }

                  tmpBoundSet = prevBoundSet;
               }

               Sorting.sortInferenceVariables(variables);
               final CaptureBinding18[] zs = new CaptureBinding18[numVars];

               for(int j = 0; j < numVars; ++j) {
                  zs[j] = this.freshCapture(variables[j]);
               }

               final BoundSet kurrentBoundSet = tmpBoundSet;
               Substitution theta = new Substitution() {
                  @Override
                  public LookupEnvironment environment() {
                     return InferenceContext18.this.environment;
                  }

                  @Override
                  public boolean isRawSubstitution() {
                     return false;
                  }

                  @Override
                  public TypeBinding substitute(TypeVariableBinding typeVariable) {
                     for(int j = 0; j < numVars; ++j) {
                        if (TypeBinding.equalsEquals(variables[j], typeVariable)) {
                           return zs[j];
                        }
                     }

                     if (typeVariable instanceof InferenceVariable) {
                        InferenceVariable inferenceVariable = (InferenceVariable)typeVariable;
                        TypeBinding instantiation = kurrentBoundSet.getInstantiation(inferenceVariable, null);
                        if (instantiation != null) {
                           return instantiation;
                        }
                     }

                     return typeVariable;
                  }
               };

               for(int j = 0; j < numVars; ++j) {
                  InferenceVariable variable = variables[j];
                  CaptureBinding18 zsj = zs[j];
                  TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, true);
                  if (lowerBounds != Binding.NO_TYPES) {
                     TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
                     if (lub != TypeBinding.VOID && lub != null) {
                        zsj.lowerBound = lub;
                     }
                  }

                  TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, false);
                  if (upperBounds != Binding.NO_TYPES) {
                     for(int k = 0; k < upperBounds.length; ++k) {
                        upperBounds[k] = Scope.substitute(theta, upperBounds[k]);
                     }

                     if (!this.setUpperBounds(zsj, upperBounds)) {
                        continue;
                     }
                  }

                  if (tmpBoundSet == this.currentBounds) {
                     tmpBoundSet = tmpBoundSet.copy();
                  }

                  Iterator<ParameterizedTypeBinding> captureKeys = tmpBoundSet.captures.keySet().iterator();
                  Set<ParameterizedTypeBinding> toRemove = new HashSet<>();

                  while(captureKeys.hasNext()) {
                     ParameterizedTypeBinding key = captureKeys.next();
                     int len = key.arguments.length;

                     for(int i = 0; i < len; ++i) {
                        if (TypeBinding.equalsEquals(key.arguments[i], variable)) {
                           toRemove.add(key);
                           break;
                        }
                     }
                  }

                  captureKeys = toRemove.iterator();

                  while(captureKeys.hasNext()) {
                     tmpBoundSet.captures.remove(captureKeys.next());
                  }

                  tmpBoundSet.addBound(new TypeBound(variable, zsj, 4), this.environment);
               }

               if (!tmpBoundSet.incorporate(this)) {
                  return null;
               }

               if (tmpBoundSet.numUninstantiatedVariables(this.inferenceVariables) == oldNumUninstantiated) {
                  return null;
               }
            }
         }
      }

      return tmpBoundSet;
   }

   private CaptureBinding18 freshCapture(InferenceVariable variable) {
      int id = this.captureId++;
      char[] sourceName = CharOperation.concat("Z".toCharArray(), '#', String.valueOf(id).toCharArray(), '-', variable.sourceName);
      int start = this.currentInvocation != null ? this.currentInvocation.sourceStart() : 0;
      int end = this.currentInvocation != null ? this.currentInvocation.sourceEnd() : 0;
      return new CaptureBinding18(this.scope.enclosingSourceType(), sourceName, variable.typeParameter.shortReadableName(), start, end, id, this.environment);
   }

   private boolean setUpperBounds(CaptureBinding18 typeVariable, TypeBinding[] substitutedUpperBounds) {
      if (substitutedUpperBounds.length == 1) {
         return typeVariable.setUpperBounds(substitutedUpperBounds, this.object);
      } else {
         TypeBinding[] glbs = Scope.greaterLowerBound(substitutedUpperBounds, this.scope, this.environment);
         if (glbs == null) {
            return false;
         } else {
            if (typeVariable.lowerBound != null) {
               for(int i = 0; i < glbs.length; ++i) {
                  if (!typeVariable.lowerBound.isCompatibleWith(glbs[i])) {
                     return false;
                  }
               }
            }

            sortTypes(glbs);
            return typeVariable.setUpperBounds(glbs, this.object);
         }
      }
   }

   static void sortTypes(TypeBinding[] types) {
      Arrays.sort(types, new Comparator<TypeBinding>() {
         public int compare(TypeBinding o1, TypeBinding o2) {
            int i1 = o1.id;
            int i2 = o2.id;
            return i1 < i2 ? -1 : (i1 == i2 ? 0 : 1);
         }
      });
   }

   private Set<InferenceVariable> getSmallestVariableSet(BoundSet bounds, InferenceVariable[] subSet) {
      Set<InferenceVariable> v = new HashSet<>();
      Map<InferenceVariable, Set<InferenceVariable>> dependencies = new HashMap<>();

      for(InferenceVariable iv : subSet) {
         Set<InferenceVariable> tmp = new HashSet<>();
         this.addDependencies(bounds, tmp, iv);
         dependencies.put(iv, tmp);
         v.addAll(tmp);
      }

      int min = Integer.MAX_VALUE;
      Set<InferenceVariable> result = null;

      for(InferenceVariable currentVariable : v) {
         if (!bounds.isInstantiated(currentVariable)) {
            Set<InferenceVariable> set = dependencies.get(currentVariable);
            if (set == null) {
               this.addDependencies(bounds, set = new HashSet<>(), currentVariable);
            }

            int cur = set.size();
            if (cur == 1) {
               return set;
            }

            if (cur < min) {
               result = set;
               min = cur;
            }
         }
      }

      return result;
   }

   private void addDependencies(BoundSet boundSet, Set<InferenceVariable> variableSet, InferenceVariable currentVariable) {
      if (!boundSet.isInstantiated(currentVariable)) {
         if (variableSet.add(currentVariable)) {
            for(int j = 0; j < this.inferenceVariables.length; ++j) {
               InferenceVariable nextVariable = this.inferenceVariables[j];
               if (!TypeBinding.equalsEquals(nextVariable, currentVariable) && boundSet.dependsOnResolutionOf(currentVariable, nextVariable)) {
                  this.addDependencies(boundSet, variableSet, nextVariable);
               }
            }
         }
      }
   }

   private ConstraintFormula pickFromCycle(Set<ConstraintFormula> c) {
      HashMap<ConstraintFormula, Set<ConstraintFormula>> dependencies = new HashMap<>();
      Set<ConstraintFormula> cycles = new HashSet<>();

      for(ConstraintFormula constraint : c) {
         Collection<InferenceVariable> infVars = constraint.inputVariables(this);

         for(ConstraintFormula other : c) {
            if (other != constraint && this.dependsOn(infVars, other.outputVariables(this))) {
               Set<ConstraintFormula> targetSet = dependencies.get(constraint);
               if (targetSet == null) {
                  dependencies.put(constraint, targetSet = new HashSet<>());
               }

               targetSet.add(other);
               Set<ConstraintFormula> nodesInCycle = new HashSet<>();
               if (this.isReachable(dependencies, other, constraint, new HashSet<>(), nodesInCycle)) {
                  cycles.addAll(nodesInCycle);
               }
            }
         }
      }

      Set<ConstraintFormula> outside = new HashSet<>(c);
      outside.removeAll(cycles);
      Set<ConstraintFormula> candidatesII = new HashSet<>();

      label115:
      for(ConstraintFormula candidate : cycles) {
         Collection<InferenceVariable> infVars = candidate.inputVariables(this);

         for(ConstraintFormula out : outside) {
            if (this.dependsOn(infVars, out.outputVariables(this))) {
               continue label115;
            }
         }

         candidatesII.add(candidate);
      }

      if (candidatesII.isEmpty()) {
         candidatesII = c;
      }

      Set<ConstraintFormula> candidatesIII = new HashSet<>();

      for(ConstraintFormula candidate : candidatesII) {
         if (candidate instanceof ConstraintExpressionFormula) {
            candidatesIII.add(candidate);
         }
      }

      if (candidatesIII.isEmpty()) {
         candidatesIII = candidatesII;
      } else {
         Map<ConstraintExpressionFormula, ConstraintExpressionFormula> expressionContainedBy = new HashMap<>();

         for(ConstraintFormula one : candidatesIII) {
            ConstraintExpressionFormula oneCEF = (ConstraintExpressionFormula)one;
            Expression exprOne = oneCEF.left;

            for(ConstraintFormula two : candidatesIII) {
               if (one != two) {
                  ConstraintExpressionFormula twoCEF = (ConstraintExpressionFormula)two;
                  Expression exprTwo = twoCEF.left;
                  if (this.doesExpressionContain(exprOne, exprTwo)) {
                     ConstraintExpressionFormula previous = expressionContainedBy.get(two);
                     if (previous == null || this.doesExpressionContain(previous.left, exprOne)) {
                        expressionContainedBy.put(twoCEF, oneCEF);
                     }
                  }
               }
            }
         }

         Map<ConstraintExpressionFormula, Set<ConstraintExpressionFormula>> containmentForest = new HashMap<>();

         for(Entry<ConstraintExpressionFormula, ConstraintExpressionFormula> parentRelation : expressionContainedBy.entrySet()) {
            ConstraintExpressionFormula parent = parentRelation.getValue();
            Set<ConstraintExpressionFormula> children = containmentForest.get(parent);
            if (children == null) {
               containmentForest.put(parent, children = new HashSet<>());
            }

            children.add(parentRelation.getKey());
         }

         int bestRank = -1;
         ConstraintExpressionFormula candidate = null;

         for(ConstraintExpressionFormula parent : containmentForest.keySet()) {
            int rank = this.rankNode(parent, expressionContainedBy, containmentForest);
            if (rank > bestRank) {
               bestRank = rank;
               candidate = parent;
            }
         }

         if (candidate != null) {
            return candidate;
         }
      }

      if (candidatesIII.isEmpty()) {
         throw new IllegalStateException("cannot pick constraint from cyclic set");
      } else {
         return candidatesIII.iterator().next();
      }
   }

   private boolean dependsOn(Collection<InferenceVariable> inputsOfFirst, Collection<InferenceVariable> outputsOfOther) {
      for(InferenceVariable iv : inputsOfFirst) {
         for(InferenceVariable otherIV : outputsOfOther) {
            if (this.currentBounds.dependsOnResolutionOf(iv, otherIV)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isReachable(
      Map<ConstraintFormula, Set<ConstraintFormula>> deps,
      ConstraintFormula from,
      ConstraintFormula to,
      Set<ConstraintFormula> nodesVisited,
      Set<ConstraintFormula> nodesInCycle
   ) {
      if (from == to) {
         nodesInCycle.add(from);
         return true;
      } else if (!nodesVisited.add(from)) {
         return false;
      } else {
         Set<ConstraintFormula> targetSet = deps.get(from);
         if (targetSet != null) {
            for(ConstraintFormula tgt : targetSet) {
               if (this.isReachable(deps, tgt, to, nodesVisited, nodesInCycle)) {
                  nodesInCycle.add(from);
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean doesExpressionContain(Expression exprOne, Expression exprTwo) {
      if (exprTwo.sourceStart > exprOne.sourceStart) {
         return exprTwo.sourceEnd <= exprOne.sourceEnd;
      } else if (exprTwo.sourceStart == exprOne.sourceStart) {
         return exprTwo.sourceEnd < exprOne.sourceEnd;
      } else {
         return false;
      }
   }

   private int rankNode(
      ConstraintExpressionFormula parent,
      Map<ConstraintExpressionFormula, ConstraintExpressionFormula> expressionContainedBy,
      Map<ConstraintExpressionFormula, Set<ConstraintExpressionFormula>> containmentForest
   ) {
      if (expressionContainedBy.get(parent) != null) {
         return -1;
      } else {
         Set<ConstraintExpressionFormula> children = containmentForest.get(parent);
         if (children == null) {
            return 1;
         } else {
            int sum = 1;

            for(ConstraintExpressionFormula child : children) {
               int cRank = this.rankNode(child, expressionContainedBy, containmentForest);
               if (cRank > 0) {
                  sum += cRank;
               }
            }

            return sum;
         }
      }
   }

   private Set<ConstraintFormula> findBottomSet(
      Set<ConstraintFormula> constraints, Set<InferenceVariable> allOutputVariables, List<Set<InferenceVariable>> components
   ) {
      Set<ConstraintFormula> result = new HashSet<>();

      label22:
      for(ConstraintFormula constraint : constraints) {
         for(InferenceVariable in : constraint.inputVariables(this)) {
            if (this.canInfluenceAnyOf(in, allOutputVariables, components)) {
               continue label22;
            }
         }

         result.add(constraint);
      }

      return result;
   }

   private boolean canInfluenceAnyOf(InferenceVariable in, Set<InferenceVariable> allOuts, List<Set<InferenceVariable>> components) {
      for(Set<InferenceVariable> component : components) {
         if (component.contains(in)) {
            for(InferenceVariable out : allOuts) {
               if (component.contains(out)) {
                  return true;
               }
            }

            return false;
         }
      }

      return false;
   }

   Set<InferenceVariable> allOutputVariables(Set<ConstraintFormula> constraints) {
      Set<InferenceVariable> result = new HashSet<>();
      Iterator<ConstraintFormula> it = constraints.iterator();

      while(it.hasNext()) {
         result.addAll(it.next().outputVariables(this));
      }

      return result;
   }

   private TypeBinding[] varArgTypes(TypeBinding[] parameters, int k) {
      TypeBinding[] types = new TypeBinding[k];
      int declaredLength = parameters.length - 1;
      System.arraycopy(parameters, 0, types, 0, declaredLength);
      TypeBinding last = ((ArrayBinding)parameters[declaredLength]).elementsType();

      for(int i = declaredLength; i < k; ++i) {
         types[i] = last;
      }

      return types;
   }

   public InferenceContext18.SuspendedInferenceRecord enterPolyInvocation(InvocationSite invocation, Expression[] innerArguments) {
      InferenceContext18.SuspendedInferenceRecord record = new InferenceContext18.SuspendedInferenceRecord(
         this.currentInvocation, this.invocationArguments, this.inferenceVariables, this.inferenceKind, this.usesUncheckedConversion
      );
      this.inferenceVariables = null;
      this.invocationArguments = innerArguments;
      this.currentInvocation = invocation;
      this.usesUncheckedConversion = false;
      return record;
   }

   public InferenceContext18.SuspendedInferenceRecord enterLambda(LambdaExpression lambda) {
      InferenceContext18.SuspendedInferenceRecord record = new InferenceContext18.SuspendedInferenceRecord(
         this.currentInvocation, this.invocationArguments, this.inferenceVariables, this.inferenceKind, this.usesUncheckedConversion
      );
      this.inferenceVariables = null;
      this.invocationArguments = null;
      this.currentInvocation = null;
      this.usesUncheckedConversion = false;
      return record;
   }

   public void integrateInnerInferenceB2(InferenceContext18 innerCtx) {
      this.currentBounds.addBounds(innerCtx.b2, this.environment);
      this.inferenceVariables = innerCtx.inferenceVariables;
      this.inferenceKind = innerCtx.inferenceKind;
      innerCtx.outerContext = this;
      this.usesUncheckedConversion = innerCtx.usesUncheckedConversion;

      for(InferenceVariable variable : this.inferenceVariables) {
         variable.updateSourceName(this.nextVarId++);
      }
   }

   public void resumeSuspendedInference(InferenceContext18.SuspendedInferenceRecord record) {
      if (this.inferenceVariables == null) {
         this.inferenceVariables = record.inferenceVariables;
      } else {
         int l1 = this.inferenceVariables.length;
         int l2 = record.inferenceVariables.length;
         System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[l1 + l2], l2, l1);
         System.arraycopy(record.inferenceVariables, 0, this.inferenceVariables, 0, l2);
      }

      this.currentInvocation = record.site;
      this.invocationArguments = record.invocationArguments;
      this.inferenceKind = record.inferenceKind;
      this.usesUncheckedConversion = record.usesUncheckedConversion;
   }

   private Substitution getResultSubstitution(final BoundSet result, final boolean full) {
      return new Substitution() {
         @Override
         public LookupEnvironment environment() {
            return InferenceContext18.this.environment;
         }

         @Override
         public boolean isRawSubstitution() {
            return false;
         }

         @Override
         public TypeBinding substitute(TypeVariableBinding typeVariable) {
            if (typeVariable instanceof InferenceVariable) {
               TypeBinding instantiation = result.getInstantiation((InferenceVariable)typeVariable, InferenceContext18.this.environment);
               if (instantiation != null || full) {
                  return instantiation;
               }
            }

            return typeVariable;
         }
      };
   }

   public boolean isVarArgs() {
      return this.inferenceKind == 3;
   }

   public static TypeBinding getParameter(TypeBinding[] parameters, int rank, boolean isVarArgs) {
      if (isVarArgs) {
         if (rank >= parameters.length - 1) {
            return ((ArrayBinding)parameters[parameters.length - 1]).elementsType();
         }
      } else if (rank >= parameters.length) {
         return null;
      }

      return parameters[rank];
   }

   public MethodBinding getReturnProblemMethodIfNeeded(TypeBinding expectedType, MethodBinding method) {
      if (expectedType != null && method.returnType instanceof ReferenceBinding && method.returnType.erasure().isCompatibleWith(expectedType)) {
         return method;
      } else {
         ProblemMethodBinding problemMethod = new ProblemMethodBinding(method, method.selector, method.parameters, 23);
         problemMethod.returnType = expectedType != null ? expectedType : method.returnType;
         problemMethod.inferenceContext = this;
         return problemMethod;
      }
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer("Inference Context");
      switch(this.stepCompleted) {
         case 0:
            buf.append(" (initial)");
            break;
         case 1:
            buf.append(" (applicability inferred)");
            break;
         case 2:
            buf.append(" (type inferred)");
      }

      switch(this.inferenceKind) {
         case 1:
            buf.append(" (strict)");
            break;
         case 2:
            buf.append(" (loose)");
            break;
         case 3:
            buf.append(" (vararg)");
      }

      if (this.currentBounds != null && this.isResolved(this.currentBounds)) {
         buf.append(" (resolved)");
      }

      buf.append('\n');
      if (this.inferenceVariables != null) {
         buf.append("Inference Variables:\n");

         for(int i = 0; i < this.inferenceVariables.length; ++i) {
            buf.append('\t').append(this.inferenceVariables[i].sourceName).append("\t:\t");
            if (this.currentBounds != null && this.currentBounds.isInstantiated(this.inferenceVariables[i])) {
               buf.append(this.currentBounds.getInstantiation(this.inferenceVariables[i], this.environment).readableName());
            } else {
               buf.append("NOT INSTANTIATED");
            }

            buf.append('\n');
         }
      }

      if (this.initialConstraints != null) {
         buf.append("Initial Constraints:\n");

         for(int i = 0; i < this.initialConstraints.length; ++i) {
            if (this.initialConstraints[i] != null) {
               buf.append('\t').append(this.initialConstraints[i].toString()).append('\n');
            }
         }
      }

      if (this.currentBounds != null) {
         buf.append(this.currentBounds.toString());
      }

      return buf.toString();
   }

   public static ParameterizedTypeBinding parameterizedWithWildcard(TypeBinding type) {
      if (type != null && type.kind() == 260) {
         ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)type;
         TypeBinding[] arguments = parameterizedType.arguments;
         if (arguments != null) {
            for(int i = 0; i < arguments.length; ++i) {
               if (arguments[i].isWildcard()) {
                  return parameterizedType;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public TypeBinding[] getFunctionInterfaceArgumentSolutions(TypeBinding[] a) {
      int m = a.length;
      TypeBinding[] aprime = new TypeBinding[m];

      for(int i = 0; i < this.inferenceVariables.length; ++i) {
         InferenceVariable alphai = this.inferenceVariables[i];
         TypeBinding t = this.currentBounds.getInstantiation(alphai, this.environment);
         if (t != null) {
            aprime[i] = t;
         } else {
            aprime[i] = a[i];
         }
      }

      return aprime;
   }

   public void recordUncheckedConversion(ConstraintTypeFormula constraint) {
      if (this.constraintsWithUncheckedConversion == null) {
         this.constraintsWithUncheckedConversion = new ArrayList<>();
      }

      this.constraintsWithUncheckedConversion.add(constraint);
      this.usesUncheckedConversion = true;
   }

   void reportUncheckedConversions(BoundSet solution) {
      if (this.constraintsWithUncheckedConversion != null) {
         int len = this.constraintsWithUncheckedConversion.size();
         Substitution substitution = this.getResultSubstitution(solution, true);

         for(int i = 0; i < len; ++i) {
            ConstraintTypeFormula constraint = (ConstraintTypeFormula)this.constraintsWithUncheckedConversion.get(i);
            TypeBinding expectedType = constraint.right;
            TypeBinding providedType = constraint.left;
            if (!expectedType.isProperType(true)) {
               expectedType = Scope.substitute(substitution, expectedType);
            }

            if (!providedType.isProperType(true)) {
               providedType = Scope.substitute(substitution, providedType);
            }
         }
      }
   }

   public boolean usesUncheckedConversion() {
      return this.constraintsWithUncheckedConversion != null;
   }

   public static void missingImplementation(String msg) {
      throw new UnsupportedOperationException(msg);
   }

   public void forwardResults(BoundSet result, Invocation invocation, ParameterizedMethodBinding pmb, TypeBinding targetType) {
      if (targetType != null) {
         invocation.registerResult(targetType, pmb);
      }

      Expression[] arguments = invocation.arguments();
      int i = 0;

      for(int length = arguments == null ? 0 : arguments.length; i < length; ++i) {
         Expression[] expressions = arguments[i].getPolyExpressions();
         int j = 0;

         for(int jLength = expressions.length; j < jLength; ++j) {
            Expression expression = expressions[j];
            if (expression instanceof Invocation) {
               Invocation polyInvocation = (Invocation)expression;
               MethodBinding binding = polyInvocation.binding();
               if (binding != null && binding.isValidBinding()) {
                  ParameterizedMethodBinding methodSubstitute = null;
                  if (binding instanceof ParameterizedGenericMethodBinding) {
                     MethodBinding shallowOriginal = binding.shallowOriginal();
                     TypeBinding[] solutions = this.getSolutions(shallowOriginal.typeVariables(), polyInvocation, result);
                     if (solutions == null) {
                        continue;
                     }

                     methodSubstitute = this.environment.createParameterizedGenericMethod(shallowOriginal, solutions);
                  } else {
                     if (!binding.isConstructor() || !(binding instanceof ParameterizedMethodBinding)) {
                        continue;
                     }

                     MethodBinding shallowOriginal = binding.shallowOriginal();
                     ReferenceBinding genericType = shallowOriginal.declaringClass;
                     TypeBinding[] solutions = this.getSolutions(genericType.typeVariables(), polyInvocation, result);
                     if (solutions == null) {
                        continue;
                     }

                     ParameterizedTypeBinding parameterizedType = this.environment
                        .createParameterizedType(genericType, solutions, binding.declaringClass.enclosingType());

                     MethodBinding[] var22;
                     for(MethodBinding parameterizedMethod : var22 = parameterizedType.methods()) {
                        if (parameterizedMethod.original() == shallowOriginal) {
                           methodSubstitute = (ParameterizedMethodBinding)parameterizedMethod;
                           break;
                        }
                     }
                  }

                  if (methodSubstitute != null && methodSubstitute.isValidBinding()) {
                     boolean variableArity = pmb.isVarargs();
                     TypeBinding[] parameters = pmb.parameters;
                     if (variableArity && parameters.length == arguments.length && i == length - 1) {
                        TypeBinding returnType = methodSubstitute.returnType.capture(this.scope, expression.sourceStart, expression.sourceEnd);
                        if (returnType.isCompatibleWith(parameters[parameters.length - 1], this.scope)) {
                           variableArity = false;
                        }
                     }

                     TypeBinding parameterType = getParameter(parameters, i, variableArity);
                     this.forwardResults(result, polyInvocation, methodSubstitute, parameterType);
                  }
               }
            }
         }
      }
   }

   public void cleanUp() {
      this.b2 = null;
      this.currentBounds = null;
   }

   static class SuspendedInferenceRecord {
      InvocationSite site;
      Expression[] invocationArguments;
      InferenceVariable[] inferenceVariables;
      int inferenceKind;
      boolean usesUncheckedConversion;

      SuspendedInferenceRecord(
         InvocationSite site, Expression[] invocationArguments, InferenceVariable[] inferenceVariables, int inferenceKind, boolean usesUncheckedConversion
      ) {
         this.site = site;
         this.invocationArguments = invocationArguments;
         this.inferenceVariables = inferenceVariables;
         this.inferenceKind = inferenceKind;
         this.usesUncheckedConversion = usesUncheckedConversion;
      }
   }
}
