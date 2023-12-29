package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExpressionContext;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;

class ConstraintExpressionFormula extends ConstraintFormula {
   Expression left;
   boolean isSoft;

   ConstraintExpressionFormula(Expression expression, TypeBinding type, int relation) {
      this.left = expression;
      this.right = type;
      this.relation = relation;
   }

   ConstraintExpressionFormula(Expression expression, TypeBinding type, int relation, boolean isSoft) {
      this(expression, type, relation);
      this.isSoft = isSoft;
   }

   @Override
   public Object reduce(InferenceContext18 inferenceContext) throws InferenceFailureException {
      if (this.relation == 8) {
         return this.left.isPotentiallyCompatibleWith(this.right, inferenceContext.scope) ? TRUE : FALSE;
      } else if (this.right.isProperType(true)) {
         return !this.left.isCompatibleWith(this.right, inferenceContext.scope) && !this.left.isBoxingCompatibleWith(this.right, inferenceContext.scope)
            ? FALSE
            : TRUE;
      } else if (!this.canBePolyExpression(this.left)) {
         TypeBinding exprType = this.left.resolvedType;
         if (exprType != null && exprType.isValidBinding()) {
            return ConstraintTypeFormula.create(exprType, this.right, 1, this.isSoft);
         } else {
            return this.left instanceof MessageSend && ((MessageSend)this.left).actualReceiverType instanceof InferenceVariable ? null : FALSE;
         }
      } else if (this.left instanceof Invocation) {
         Invocation invocation = (Invocation)this.left;
         MethodBinding previousMethod = invocation.binding();
         if (previousMethod == null) {
            return null;
         } else {
            MethodBinding var22 = previousMethod.shallowOriginal();
            InferenceContext18.SuspendedInferenceRecord prevInvocation = inferenceContext.enterPolyInvocation(invocation, invocation.arguments());

            try {
               Expression[] arguments = invocation.arguments();
               TypeBinding[] argumentTypes = arguments == null ? Binding.NO_PARAMETERS : new TypeBinding[arguments.length];

               for(int i = 0; i < argumentTypes.length; ++i) {
                  argumentTypes[i] = arguments[i].resolvedType;
               }

               if (previousMethod instanceof ParameterizedGenericMethodBinding) {
                  InferenceContext18 innerCtx = invocation.getInferenceContext((ParameterizedGenericMethodBinding)previousMethod);
                  if (innerCtx == null) {
                     TypeBinding exprType = this.left.resolvedType;
                     if (exprType == null || !exprType.isValidBinding()) {
                        return FALSE;
                     }

                     return ConstraintTypeFormula.create(exprType, this.right, 1, this.isSoft);
                  }

                  if (innerCtx.stepCompleted < 1) {
                     return FALSE;
                  }

                  inferenceContext.integrateInnerInferenceB2(innerCtx);
               } else {
                  inferenceContext.inferenceKind = inferenceContext.getInferenceKind(previousMethod, argumentTypes);
                  boolean isDiamond = var22.isConstructor() && this.left.isPolyExpression(var22);
                  inferInvocationApplicability(inferenceContext, var22, argumentTypes, isDiamond, inferenceContext.inferenceKind);
               }

               return inferPolyInvocationType(inferenceContext, invocation, this.right, var22) ? null : FALSE;
            } finally {
               inferenceContext.resumeSuspendedInference(prevInvocation);
            }
         }
      } else if (this.left instanceof ConditionalExpression) {
         ConditionalExpression conditional = (ConditionalExpression)this.left;
         return new ConstraintFormula[]{
            new ConstraintExpressionFormula(conditional.valueIfTrue, this.right, this.relation, this.isSoft),
            new ConstraintExpressionFormula(conditional.valueIfFalse, this.right, this.relation, this.isSoft)
         };
      } else if (this.left instanceof LambdaExpression) {
         LambdaExpression lambda = (LambdaExpression)this.left;
         BlockScope scope = lambda.enclosingScope;
         if (!this.right.isFunctionalInterface(scope)) {
            return FALSE;
         } else {
            ReferenceBinding t = (ReferenceBinding)this.right;
            ParameterizedTypeBinding withWildCards = InferenceContext18.parameterizedWithWildcard(t);
            if (withWildCards != null) {
               t = findGroundTargetType(inferenceContext, scope, lambda, withWildCards);
            }

            if (t == null) {
               return FALSE;
            } else {
               MethodBinding functionType = t.getSingleAbstractMethod(scope, true);
               if (functionType == null) {
                  return FALSE;
               } else {
                  TypeBinding[] parameters = functionType.parameters;
                  if (parameters.length != lambda.arguments().length) {
                     return FALSE;
                  } else {
                     if (lambda.argumentsTypeElided()) {
                        for(int i = 0; i < parameters.length; ++i) {
                           if (!parameters[i].isProperType(true)) {
                              return FALSE;
                           }
                        }
                     }

                     lambda = lambda.resolveExpressionExpecting(t, inferenceContext.scope, inferenceContext);
                     if (lambda == null) {
                        return FALSE;
                     } else {
                        if (functionType.returnType == TypeBinding.VOID) {
                           if (!lambda.isVoidCompatible()) {
                              return FALSE;
                           }
                        } else if (!lambda.isValueCompatible()) {
                           return FALSE;
                        }

                        List<ConstraintFormula> result = new ArrayList<>();
                        if (!lambda.argumentsTypeElided()) {
                           Argument[] arguments = lambda.arguments();

                           for(int i = 0; i < parameters.length; ++i) {
                              result.add(ConstraintTypeFormula.create(parameters[i], arguments[i].type.resolvedType, 4));
                           }

                           if (lambda.resolvedType != null) {
                              result.add(ConstraintTypeFormula.create(lambda.resolvedType, this.right, 2));
                           }
                        }

                        if (functionType.returnType != TypeBinding.VOID) {
                           TypeBinding r = functionType.returnType;
                           Expression[] exprs = lambda.resultExpressions();
                           int i = 0;

                           for(int length = exprs == null ? 0 : exprs.length; i < length; ++i) {
                              Expression expr = exprs[i];
                              if (r.isProperType(true) && expr.resolvedType != null) {
                                 TypeBinding exprType = expr.resolvedType;
                                 if (!expr.isConstantValueOfTypeAssignableToType(exprType, r)
                                    && !exprType.isCompatibleWith(r)
                                    && !expr.isBoxingCompatible(exprType, r, expr, scope)) {
                                    return FALSE;
                                 }
                              } else {
                                 result.add(new ConstraintExpressionFormula(expr, r, 1, this.isSoft));
                              }
                           }
                        }

                        return result.size() == 0 ? TRUE : result.toArray(new ConstraintFormula[result.size()]);
                     }
                  }
               }
            }
         }
      } else {
         return this.left instanceof ReferenceExpression
            ? this.reduceReferenceExpressionCompatibility((ReferenceExpression)this.left, inferenceContext)
            : FALSE;
      }
   }

   public static ReferenceBinding findGroundTargetType(
      InferenceContext18 inferenceContext, BlockScope scope, LambdaExpression lambda, ParameterizedTypeBinding targetTypeWithWildCards
   ) {
      if (lambda.argumentsTypeElided()) {
         return lambda.findGroundTargetTypeForElidedLambda(scope, targetTypeWithWildCards);
      } else {
         InferenceContext18.SuspendedInferenceRecord previous = inferenceContext.enterLambda(lambda);

         ReferenceBinding var6;
         try {
            var6 = inferenceContext.inferFunctionalInterfaceParameterization(lambda, scope, targetTypeWithWildCards);
         } finally {
            inferenceContext.resumeSuspendedInference(previous);
         }

         return var6;
      }
   }

   private boolean canBePolyExpression(Expression expr) {
      ExpressionContext previousExpressionContext = expr.getExpressionContext();
      if (previousExpressionContext == ExpressionContext.VANILLA_CONTEXT) {
         this.left.setExpressionContext(ExpressionContext.ASSIGNMENT_CONTEXT);
      }

      boolean var4;
      try {
         var4 = expr.isPolyExpression();
      } finally {
         expr.setExpressionContext(previousExpressionContext);
      }

      return var4;
   }

   private Object reduceReferenceExpressionCompatibility(ReferenceExpression reference, InferenceContext18 inferenceContext) {
      TypeBinding t = this.right;
      if (t.isProperType(true)) {
         throw new IllegalStateException("Should not reach here with T being a proper type");
      } else if (!t.isFunctionalInterface(inferenceContext.scope)) {
         return FALSE;
      } else {
         MethodBinding functionType = t.getSingleAbstractMethod(inferenceContext.scope, true);
         if (functionType == null) {
            return FALSE;
         } else {
            reference = reference.resolveExpressionExpecting(t, inferenceContext.scope, inferenceContext);
            MethodBinding potentiallyApplicable = reference != null ? reference.binding : null;
            if (potentiallyApplicable == null) {
               return FALSE;
            } else if (reference.isExactMethodReference()) {
               List<ConstraintFormula> newConstraints = new ArrayList<>();
               TypeBinding[] p = functionType.parameters;
               int n = p.length;
               TypeBinding[] pPrime = potentiallyApplicable.parameters;
               int k = pPrime.length;
               int offset = 0;
               if (n == k + 1) {
                  newConstraints.add(ConstraintTypeFormula.create(p[0], reference.lhs.resolvedType, 1));
                  offset = 1;
               }

               for(int i = offset; i < n; ++i) {
                  newConstraints.add(ConstraintTypeFormula.create(p[i], pPrime[i - offset], 1));
               }

               TypeBinding r = functionType.returnType;
               if (r != TypeBinding.VOID) {
                  TypeBinding rAppl = (TypeBinding)(potentiallyApplicable.isConstructor() && !reference.isArrayConstructorReference()
                     ? potentiallyApplicable.declaringClass
                     : potentiallyApplicable.returnType);
                  if (rAppl == TypeBinding.VOID) {
                     return FALSE;
                  }

                  TypeBinding rPrime = rAppl.capture(inferenceContext.scope, reference.sourceStart, reference.sourceEnd);
                  newConstraints.add(ConstraintTypeFormula.create(rPrime, r, 1));
               }

               return newConstraints.toArray(new ConstraintFormula[newConstraints.size()]);
            } else {
               int n = functionType.parameters.length;

               for(int i = 0; i < n; ++i) {
                  if (!functionType.parameters[i].isProperType(true)) {
                     return FALSE;
                  }
               }

               MethodBinding compileTimeDecl = potentiallyApplicable;
               if (!potentiallyApplicable.isValidBinding()) {
                  return FALSE;
               } else {
                  TypeBinding r = (TypeBinding)(functionType.isConstructor() ? functionType.declaringClass : functionType.returnType);
                  if (r.id == 6) {
                     return TRUE;
                  } else {
                     MethodBinding original = potentiallyApplicable.shallowOriginal();
                     TypeBinding compileTypeReturn = (TypeBinding)(original.isConstructor() ? original.declaringClass : original.returnType);
                     if (reference.typeArguments == null
                        && (
                           original.typeVariables() != Binding.NO_TYPE_VARIABLES && compileTypeReturn.mentionsAny(original.typeVariables(), -1)
                              || original.isConstructor() && potentiallyApplicable.declaringClass.isRawType()
                        )) {
                        TypeBinding[] argumentTypes;
                        if (t.isParameterizedType()) {
                           MethodBinding capturedFunctionType = ((ParameterizedTypeBinding)t)
                              .getSingleAbstractMethod(inferenceContext.scope, true, reference.sourceStart, reference.sourceEnd);
                           argumentTypes = capturedFunctionType.parameters;
                        } else {
                           argumentTypes = functionType.parameters;
                        }

                        InferenceContext18.SuspendedInferenceRecord prevInvocation = inferenceContext.enterPolyInvocation(
                           reference, reference.createPseudoExpressions(argumentTypes)
                        );

                        try {
                           InferenceContext18 innerContex = reference.getInferenceContext((ParameterizedMethodBinding)compileTimeDecl);
                           int innerInferenceKind = innerContex != null ? innerContex.inferenceKind : 1;
                           inferInvocationApplicability(inferenceContext, original, argumentTypes, original.isConstructor(), innerInferenceKind);
                           if (!inferPolyInvocationType(inferenceContext, reference, r, original)) {
                              return FALSE;
                           }

                           if (!original.isConstructor() || reference.receiverType.isRawType() || reference.receiverType.typeArguments() == null) {
                              return null;
                           }
                        } catch (InferenceFailureException var19) {
                           return FALSE;
                        } finally {
                           inferenceContext.resumeSuspendedInference(prevInvocation);
                        }
                     }

                     TypeBinding rPrime = (TypeBinding)(potentiallyApplicable.isConstructor()
                        ? potentiallyApplicable.declaringClass
                        : potentiallyApplicable.returnType.capture(inferenceContext.scope, reference.sourceStart(), reference.sourceEnd()));
                     return rPrime.id == 6 ? FALSE : ConstraintTypeFormula.create(rPrime, r, 1, this.isSoft);
                  }
               }
            }
         }
      }
   }

   static void inferInvocationApplicability(
      InferenceContext18 inferenceContext, MethodBinding method, TypeBinding[] arguments, boolean isDiamond, int checkType
   ) {
      TypeVariableBinding[] typeVariables = method.getAllTypeVariables(isDiamond);
      InferenceVariable[] inferenceVariables = inferenceContext.createInitialBoundSet(typeVariables);
      int paramLength = method.parameters.length;
      TypeBinding varArgsType = null;
      if (method.isVarargs()) {
         int varArgPos = paramLength - 1;
         varArgsType = method.parameters[varArgPos];
      }

      inferenceContext.createInitialConstraintsForParameters(method.parameters, checkType == 3, varArgsType, method);
      inferenceContext.addThrowsContraints(typeVariables, inferenceVariables, method.thrownExceptions);
   }

   static boolean inferPolyInvocationType(InferenceContext18 inferenceContext, InvocationSite invocationSite, TypeBinding targetType, MethodBinding method) throws InferenceFailureException {
      TypeBinding[] typeArguments = invocationSite.genericTypeArguments();
      if (typeArguments == null) {
         TypeBinding returnType = (TypeBinding)(method.isConstructor() ? method.declaringClass : method.returnType);
         if (returnType == TypeBinding.VOID) {
            throw new InferenceFailureException("expression has no value");
         }

         if (inferenceContext.usesUncheckedConversion) {
            TypeBinding erasure = inferenceContext.environment.convertToRawType(returnType, false);
            ConstraintTypeFormula newConstraint = ConstraintTypeFormula.create(erasure, targetType, 1);
            return inferenceContext.reduceAndIncorporate(newConstraint);
         }

         TypeBinding rTheta = inferenceContext.substitute(returnType);
         ParameterizedTypeBinding parameterizedType = InferenceContext18.parameterizedWithWildcard(rTheta);
         if (parameterizedType != null && parameterizedType.arguments != null) {
            TypeBinding[] arguments = parameterizedType.arguments;
            InferenceVariable[] betas = inferenceContext.addTypeVariableSubstitutions(arguments);
            ParameterizedTypeBinding gbeta = inferenceContext.environment
               .createParameterizedType(parameterizedType.genericType(), betas, parameterizedType.enclosingType(), parameterizedType.getTypeAnnotations());
            inferenceContext.currentBounds.captures.put(gbeta, parameterizedType);
            parameterizedType = parameterizedType.capture(inferenceContext.scope, invocationSite.sourceStart(), invocationSite.sourceEnd());
            arguments = parameterizedType.arguments;
            int i = 0;

            for(int length = arguments.length; i < length; ++i) {
               if (arguments[i].isCapture() && arguments[i].isProperType(true)) {
                  CaptureBinding capture = (CaptureBinding)arguments[i];
                  inferenceContext.currentBounds.addBound(new TypeBound(betas[i], capture, 4), inferenceContext.environment);
               }
            }

            ConstraintTypeFormula newConstraint = ConstraintTypeFormula.create(gbeta, targetType, 1);
            return inferenceContext.reduceAndIncorporate(newConstraint);
         }

         if (rTheta.leafComponentType() instanceof InferenceVariable) {
            InferenceVariable alpha = (InferenceVariable)rTheta.leafComponentType();
            TypeBinding targetLeafType = targetType.leafComponentType();
            boolean toResolve = false;
            if (inferenceContext.currentBounds.condition18_5_2_bullet_3_3_1(alpha, targetLeafType)) {
               toResolve = true;
            } else if (inferenceContext.currentBounds.condition18_5_2_bullet_3_3_2(alpha, targetLeafType, inferenceContext)) {
               toResolve = true;
            } else if (targetLeafType.isPrimitiveType()) {
               TypeBinding wrapper = inferenceContext.currentBounds.findWrapperTypeBound(alpha);
               if (wrapper != null) {
                  toResolve = true;
               }
            }

            if (toResolve) {
               BoundSet solution = inferenceContext.solve(new InferenceVariable[]{alpha});
               if (solution == null) {
                  return false;
               }

               TypeBinding u = solution.getInstantiation(alpha, null)
                  .capture(inferenceContext.scope, invocationSite.sourceStart(), invocationSite.sourceEnd());
               if (rTheta.dimensions() != 0) {
                  u = inferenceContext.environment.createArrayType(u, rTheta.dimensions());
               }

               ConstraintTypeFormula newConstraint = ConstraintTypeFormula.create(u, targetType, 1);
               return inferenceContext.reduceAndIncorporate(newConstraint);
            }
         }

         ConstraintTypeFormula newConstraint = ConstraintTypeFormula.create(rTheta, targetType, 1);
         if (!inferenceContext.reduceAndIncorporate(newConstraint)) {
            return false;
         }
      }

      return true;
   }

   @Override
   Collection<InferenceVariable> inputVariables(InferenceContext18 context) {
      if (this.left instanceof LambdaExpression) {
         if (this.right instanceof InferenceVariable) {
            return Collections.singletonList((InferenceVariable)this.right);
         }

         if (this.right.isFunctionalInterface(context.scope)) {
            LambdaExpression lambda = (LambdaExpression)this.left;
            MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true);
            Set<InferenceVariable> variables = new HashSet<>();
            if (lambda.argumentsTypeElided()) {
               int len = sam.parameters.length;

               for(int i = 0; i < len; ++i) {
                  sam.parameters[i].collectInferenceVariables(variables);
               }
            }

            if (sam.returnType != TypeBinding.VOID) {
               TypeBinding r = sam.returnType;
               LambdaExpression resolved = lambda.resolveExpressionExpecting(this.right, context.scope, context);
               Expression[] resultExpressions = resolved != null ? resolved.resultExpressions() : null;
               int i = 0;

               for(int length = resultExpressions == null ? 0 : resultExpressions.length; i < length; ++i) {
                  variables.addAll(new ConstraintExpressionFormula(resultExpressions[i], r, 1).inputVariables(context));
               }
            }

            return variables;
         }
      } else if (this.left instanceof ReferenceExpression) {
         if (this.right instanceof InferenceVariable) {
            return Collections.singletonList((InferenceVariable)this.right);
         }

         if (this.right.isFunctionalInterface(context.scope) && !this.left.isExactMethodReference()) {
            MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true);
            Set<InferenceVariable> variables = new HashSet<>();
            int len = sam.parameters.length;

            for(int i = 0; i < len; ++i) {
               sam.parameters[i].collectInferenceVariables(variables);
            }

            return variables;
         }
      } else if (this.left instanceof ConditionalExpression && this.left.isPolyExpression()) {
         ConditionalExpression expr = (ConditionalExpression)this.left;
         Set<InferenceVariable> variables = new HashSet<>();
         variables.addAll(new ConstraintExpressionFormula(expr.valueIfTrue, this.right, 1).inputVariables(context));
         variables.addAll(new ConstraintExpressionFormula(expr.valueIfFalse, this.right, 1).inputVariables(context));
         return variables;
      }

      return EMPTY_VARIABLE_LIST;
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer().append('⟨');
      this.left.printExpression(4, buf);
      buf.append(relationToString(this.relation));
      this.appendTypeName(buf, this.right);
      buf.append('⟩');
      return buf.toString();
   }
}
