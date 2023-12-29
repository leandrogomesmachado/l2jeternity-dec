package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public abstract class FunctionalExpression extends Expression {
   protected TypeBinding expectedType;
   public MethodBinding descriptor;
   public MethodBinding binding;
   protected MethodBinding actualMethodBinding;
   boolean ignoreFurtherInvestigation;
   protected ExpressionContext expressionContext = ExpressionContext.VANILLA_CONTEXT;
   public CompilationResult compilationResult;
   public BlockScope enclosingScope;
   public int bootstrapMethodNumber = -1;
   public boolean shouldCaptureInstance = false;
   protected static IErrorHandlingPolicy silentErrorHandlingPolicy = DefaultErrorHandlingPolicies.ignoreAllProblems();
   private boolean hasReportedSamProblem = false;
   public boolean isSerializable;

   public FunctionalExpression(CompilationResult compilationResult) {
      this.compilationResult = compilationResult;
   }

   public FunctionalExpression() {
   }

   @Override
   public boolean isBoxingCompatibleWith(TypeBinding targetType, Scope scope) {
      return false;
   }

   public void setCompilationResult(CompilationResult compilationResult) {
      this.compilationResult = compilationResult;
   }

   public MethodBinding getMethodBinding() {
      return null;
   }

   @Override
   public void setExpectedType(TypeBinding expectedType) {
      this.expectedType = expectedType;
   }

   @Override
   public void setExpressionContext(ExpressionContext context) {
      this.expressionContext = context;
   }

   @Override
   public ExpressionContext getExpressionContext() {
      return this.expressionContext;
   }

   @Override
   public boolean isPolyExpression(MethodBinding candidate) {
      return true;
   }

   @Override
   public boolean isPolyExpression() {
      return true;
   }

   @Override
   public boolean isFunctionalType() {
      return true;
   }

   @Override
   public boolean isPertinentToApplicability(TypeBinding targetType, MethodBinding method) {
      if (targetType instanceof TypeVariableBinding) {
         TypeVariableBinding typeVariable = (TypeVariableBinding)targetType;
         if (method != null) {
            if (typeVariable.declaringElement == method) {
               return false;
            }

            if (method.isConstructor() && typeVariable.declaringElement == method.declaringClass) {
               return false;
            }
         } else if (typeVariable.declaringElement instanceof MethodBinding) {
            return false;
         }
      }

      return true;
   }

   @Override
   public TypeBinding invocationTargetType() {
      if (this.expectedType == null) {
         return null;
      } else {
         MethodBinding sam = this.expectedType.getSingleAbstractMethod(this.enclosingScope, true);
         if (sam == null || sam.problemId() == 17) {
            return null;
         } else {
            return (TypeBinding)(sam.isConstructor() ? sam.declaringClass : sam.returnType);
         }
      }
   }

   @Override
   public TypeBinding expectedType() {
      return this.expectedType;
   }

   public boolean argumentsTypeElided() {
      return true;
   }

   public int recordFunctionalType(Scope scope) {
      while(scope != null) {
         switch(scope.kind) {
            case 2:
               ReferenceContext context = ((MethodScope)scope).referenceContext;
               if (context instanceof LambdaExpression) {
                  LambdaExpression expression = (LambdaExpression)context;
                  if (expression != expression.original) {
                     return 0;
                  }
               }
            case 3:
            default:
               scope = scope.parent;
               break;
            case 4:
               CompilationUnitDeclaration unit = ((CompilationUnitScope)scope).referenceContext;
               return unit.record(this);
         }
      }

      return 0;
   }

   @Override
   public TypeBinding resolveType(BlockScope blockScope) {
      return this.resolveType(blockScope, false);
   }

   public TypeBinding resolveType(BlockScope blockScope, boolean skipKosherCheck) {
      this.constant = Constant.NotAConstant;
      this.enclosingScope = blockScope;
      MethodBinding sam = this.expectedType == null ? null : this.expectedType.getSingleAbstractMethod(blockScope, this.argumentsTypeElided());
      if (sam == null) {
         blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
         return null;
      } else if (!sam.isValidBinding()) {
         return this.reportSamProblem(blockScope, sam);
      } else {
         this.descriptor = sam;
         if (!skipKosherCheck && !this.kosherDescriptor(blockScope, sam, true)) {
            return this.resolvedType = null;
         } else {
            if (this.expectedType instanceof IntersectionTypeBinding18) {
               ReferenceBinding[] intersectingTypes = ((IntersectionTypeBinding18)this.expectedType).intersectingTypes;
               int t = 0;

               for(int max = intersectingTypes.length; t < max; ++t) {
                  if (intersectingTypes[t].findSuperTypeOriginatingFrom(37, false) != null) {
                     this.isSerializable = true;
                     break;
                  }
               }
            } else if (this.expectedType.findSuperTypeOriginatingFrom(37, false) != null) {
               this.isSerializable = true;
            }

            LookupEnvironment environment = blockScope.environment();
            if (environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
               if ((sam.tagBits & 4096L) == 0L) {
                  new ImplicitNullAnnotationVerifier(environment, environment.globalOptions.inheritNullAnnotations)
                     .checkImplicitNullAnnotations(sam, null, false, blockScope);
               }

               NullAnnotationMatching.checkForContradictions(sam, this, blockScope);
            }

            return this.resolvedType = this.expectedType;
         }
      }
   }

   protected TypeBinding reportSamProblem(BlockScope blockScope, MethodBinding sam) {
      if (this.hasReportedSamProblem) {
         return null;
      } else {
         switch(sam.problemId()) {
            case 17:
               blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
               this.hasReportedSamProblem = true;
               break;
            case 18:
               blockScope.problemReporter().illFormedParameterizationOfFunctionalInterface(this);
               this.hasReportedSamProblem = true;
         }

         return null;
      }
   }

   public boolean kosherDescriptor(Scope scope, MethodBinding sam, boolean shouldChatter) {
      FunctionalExpression.VisibilityInspector inspector = new FunctionalExpression.VisibilityInspector(this, scope, shouldChatter);
      boolean status = true;
      if (!inspector.visible(sam.returnType)) {
         status = false;
      }

      if (!inspector.visible(sam.parameters)) {
         status = false;
      }

      if (!inspector.visible(sam.thrownExceptions)) {
         status = false;
      }

      if (!inspector.visible(this.expectedType)) {
         status = false;
      }

      return status;
   }

   public int nullStatus(FlowInfo flowInfo) {
      return 4;
   }

   public int diagnosticsSourceEnd() {
      return this.sourceEnd;
   }

   public MethodBinding[] getRequiredBridges() {
      ReferenceBinding functionalType;
      if (this.expectedType instanceof IntersectionTypeBinding18) {
         functionalType = (ReferenceBinding)((IntersectionTypeBinding18)this.expectedType).getSAMType(this.enclosingScope);
      } else {
         functionalType = (ReferenceBinding)this.expectedType;
      }

      class BridgeCollector {
         MethodBinding[] bridges;
         MethodBinding method;
         char[] selector;
         LookupEnvironment environment;
         Scope scope;

         BridgeCollector(ReferenceBinding functionalType, MethodBinding method) {
            this.method = method;
            this.selector = method.selector;
            this.environment = FunctionalExpression.this.enclosingScope.environment();
            this.scope = FunctionalExpression.this.enclosingScope;
            this.collectBridges(new ReferenceBinding[]{functionalType});
         }

         void collectBridges(ReferenceBinding[] interfaces) {
            int length = interfaces == null ? 0 : interfaces.length;

            for(int i = 0; i < length; ++i) {
               ReferenceBinding superInterface = interfaces[i];
               if (superInterface != null) {
                  MethodBinding[] methods = superInterface.getMethods(this.selector);
                  int j = 0;

                  for(int count = methods == null ? 0 : methods.length; j < count; ++j) {
                     MethodBinding inheritedMethod = methods[j];
                     if (inheritedMethod != null
                        && this.method != inheritedMethod
                        && !inheritedMethod.isStatic()
                        && !inheritedMethod.redeclaresPublicObjectMethod(this.scope)) {
                        inheritedMethod = MethodVerifier.computeSubstituteMethod(inheritedMethod, this.method, this.environment);
                        if (inheritedMethod != null
                           && MethodVerifier.isSubstituteParameterSubsignature(this.method, inheritedMethod, this.environment)
                           && MethodVerifier.areReturnTypesCompatible(this.method, inheritedMethod, this.environment)) {
                           MethodBinding originalInherited = inheritedMethod.original();
                           MethodBinding originalOverride = this.method.original();
                           if (!originalOverride.areParameterErasuresEqual(originalInherited)
                              || TypeBinding.notEquals(originalOverride.returnType.erasure(), originalInherited.returnType.erasure())) {
                              this.add(originalInherited);
                           }
                        }
                     }
                  }

                  this.collectBridges(superInterface.superInterfaces());
               }
            }
         }

         void add(MethodBinding inheritedMethod) {
            if (this.bridges == null) {
               this.bridges = new MethodBinding[]{inheritedMethod};
            } else {
               int length = this.bridges.length;

               for(int i = 0; i < length; ++i) {
                  if (this.bridges[i].areParameterErasuresEqual(inheritedMethod)
                     && TypeBinding.equalsEquals(this.bridges[i].returnType.erasure(), inheritedMethod.returnType.erasure())) {
                     return;
                  }
               }

               System.arraycopy(this.bridges, 0, this.bridges = new MethodBinding[length + 1], 0, length);
               this.bridges[length] = inheritedMethod;
            }
         }

         MethodBinding[] getBridges() {
            return this.bridges;
         }
      }

      return new BridgeCollector(functionalType, this.descriptor).getBridges();
   }

   boolean requiresBridges() {
      return this.getRequiredBridges() != null;
   }

   class VisibilityInspector extends TypeBindingVisitor {
      private Scope scope;
      private boolean shouldChatter;
      private boolean visible = true;
      private FunctionalExpression expression;

      public VisibilityInspector(FunctionalExpression expression, Scope scope, boolean shouldChatter) {
         this.scope = scope;
         this.shouldChatter = shouldChatter;
         this.expression = expression;
      }

      private void checkVisibility(ReferenceBinding referenceBinding) {
         if (!referenceBinding.canBeSeenBy(this.scope)) {
            this.visible = false;
            if (this.shouldChatter) {
               this.scope.problemReporter().descriptorHasInvisibleType(this.expression, referenceBinding);
            }
         }
      }

      @Override
      public boolean visit(ReferenceBinding referenceBinding) {
         this.checkVisibility(referenceBinding);
         return true;
      }

      @Override
      public boolean visit(ParameterizedTypeBinding parameterizedTypeBinding) {
         this.checkVisibility(parameterizedTypeBinding);
         return true;
      }

      @Override
      public boolean visit(RawTypeBinding rawTypeBinding) {
         this.checkVisibility(rawTypeBinding);
         return true;
      }

      public boolean visible(TypeBinding type) {
         TypeBindingVisitor.visit(this, type);
         return this.visible;
      }

      public boolean visible(TypeBinding[] types) {
         TypeBindingVisitor.visit(this, types);
         return this.visible;
      }
   }
}
