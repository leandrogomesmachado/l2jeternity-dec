package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticFactoryMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class AllocationExpression extends Expression implements IPolyExpression, Invocation {
   public TypeReference type;
   public Expression[] arguments;
   public MethodBinding binding;
   MethodBinding syntheticAccessor;
   public TypeReference[] typeArguments;
   public TypeBinding[] genericTypeArguments;
   public FieldDeclaration enumConstant;
   protected TypeBinding typeExpected;
   public boolean inferredReturnType;
   public FakedTrackingVariable closeTracker;
   public ExpressionContext expressionContext = ExpressionContext.VANILLA_CONTEXT;
   private SimpleLookupTable inferenceContexts;
   public HashMap<TypeBinding, MethodBinding> solutionsPerTargetType;
   private InferenceContext18 outerInferenceContext;
   public boolean argsContainCast;
   public TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
   public boolean argumentsHaveErrors = false;

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      this.checkCapturedLocalInitializationIfNecessary((ReferenceBinding)this.binding.declaringClass.erasure(), currentScope, flowInfo);
      if (this.arguments != null) {
         boolean analyseResources = currentScope.compilerOptions().analyseResourceLeaks;
         boolean hasResourceWrapperType = analyseResources
            && this.resolvedType instanceof ReferenceBinding
            && ((ReferenceBinding)this.resolvedType).hasTypeBit(4);
         int i = 0;

         for(int count = this.arguments.length; i < count; ++i) {
            flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
            if (analyseResources && !hasResourceWrapperType) {
               flowInfo = FakedTrackingVariable.markPassedToOutside(currentScope, this.arguments[i], flowInfo, flowContext, false);
            }

            this.arguments[i].checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
         }

         this.analyseArguments(currentScope, flowContext, flowInfo, this.binding, this.arguments);
      }

      ReferenceBinding[] thrownExceptions = this.binding.thrownExceptions;
      if (this.binding.thrownExceptions.length != 0) {
         if ((this.bits & 65536) != 0 && this.genericTypeArguments == null) {
            thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
         }

         flowContext.checkExceptionHandlers(thrownExceptions, this, flowInfo.unconditionalCopy(), currentScope);
      }

      if (currentScope.compilerOptions().analyseResourceLeaks && FakedTrackingVariable.isAnyCloseable(this.resolvedType)) {
         FakedTrackingVariable.analyseCloseableAllocation(currentScope, flowInfo, this);
      }

      ReferenceBinding declaringClass = this.binding.declaringClass;
      MethodScope methodScope = currentScope.methodScope();
      if (declaringClass.isMemberType() && !declaringClass.isStatic() || declaringClass.isLocalType() && !methodScope.isStatic && methodScope.isLambdaScope()) {
         currentScope.tagAsAccessingEnclosingInstanceStateOf(this.binding.declaringClass.enclosingType(), false);
      }

      this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
      this.manageSyntheticAccessIfNecessary(currentScope, flowInfo);
      flowContext.recordAbruptExit();
      return flowInfo;
   }

   public void checkCapturedLocalInitializationIfNecessary(ReferenceBinding checkedType, BlockScope currentScope, FlowInfo flowInfo) {
      if ((checkedType.tagBits & 2100L) == 2068L && !currentScope.isDefinedInType(checkedType)) {
         NestedTypeBinding nestedType = (NestedTypeBinding)checkedType;
         SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
         if (syntheticArguments != null) {
            int i = 0;

            for(int count = syntheticArguments.length; i < count; ++i) {
               SyntheticArgumentBinding syntheticArgument = syntheticArguments[i];
               LocalVariableBinding targetLocal = syntheticArgument.actualOuterLocalVariable;
               if (syntheticArgument.actualOuterLocalVariable != null && targetLocal.declaration != null && !flowInfo.isDefinitelyAssigned(targetLocal)) {
                  currentScope.problemReporter().uninitializedLocalVariable(targetLocal, this);
               }
            }
         }
      }
   }

   public Expression enclosingInstance() {
      return null;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      this.cleanUpInferenceContexts();
      if (!valueRequired) {
         currentScope.problemReporter().unusedObjectAllocation(this);
      }

      int pc = codeStream.position;
      MethodBinding codegenBinding = this.binding.original();
      ReferenceBinding allocatedType = codegenBinding.declaringClass;
      codeStream.new_(this.type, allocatedType);
      boolean isUnboxing = (this.implicitConversion & 1024) != 0;
      if (valueRequired || isUnboxing) {
         codeStream.dup();
      }

      if (this.type != null) {
         codeStream.recordPositionsFrom(pc, this.type.sourceStart);
      } else {
         codeStream.ldc(String.valueOf(this.enumConstant.name));
         codeStream.generateInlinedValue(this.enumConstant.binding.id);
      }

      if (allocatedType.isNestedType()) {
         codeStream.generateSyntheticEnclosingInstanceValues(currentScope, allocatedType, this.enclosingInstance(), this);
      }

      this.generateArguments(this.binding, this.arguments, currentScope, codeStream);
      if (allocatedType.isNestedType()) {
         codeStream.generateSyntheticOuterArgumentValues(currentScope, allocatedType, this);
      }

      if (this.syntheticAccessor == null) {
         codeStream.invoke((byte)-73, codegenBinding, null, this.typeArguments);
      } else {
         int i = 0;

         for(int max = this.syntheticAccessor.parameters.length - codegenBinding.parameters.length; i < max; ++i) {
            codeStream.aconst_null();
         }

         codeStream.invoke((byte)-73, this.syntheticAccessor, null, this.typeArguments);
      }

      if (valueRequired) {
         codeStream.generateImplicitConversion(this.implicitConversion);
      } else if (isUnboxing) {
         codeStream.generateImplicitConversion(this.implicitConversion);
         switch(this.postConversionType(currentScope).id) {
            case 7:
            case 8:
               codeStream.pop2();
               break;
            default:
               codeStream.pop();
         }
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public TypeBinding[] genericTypeArguments() {
      return this.genericTypeArguments;
   }

   @Override
   public boolean isSuperAccess() {
      return false;
   }

   @Override
   public boolean isTypeAccess() {
      return true;
   }

   public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         ReferenceBinding allocatedTypeErasure = (ReferenceBinding)this.binding.declaringClass.erasure();
         if (allocatedTypeErasure.isNestedType() && (currentScope.enclosingSourceType().isLocalType() || currentScope.isLambdaSubscope())) {
            if (allocatedTypeErasure.isLocalType()) {
               ((LocalTypeBinding)allocatedTypeErasure).addInnerEmulationDependent(currentScope, false);
            } else {
               currentScope.propagateInnerEmulation(allocatedTypeErasure, false);
            }
         }
      }
   }

   public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         MethodBinding codegenBinding = this.binding.original();
         if (codegenBinding.isPrivate()) {
            SourceTypeBinding var10000 = currentScope.enclosingSourceType();
            ReferenceBinding declaringClass = codegenBinding.declaringClass;
            if (TypeBinding.notEquals(var10000, codegenBinding.declaringClass)) {
               if ((declaringClass.tagBits & 16L) != 0L && currentScope.compilerOptions().complianceLevel >= 3145728L) {
                  codegenBinding.tagBits |= 512L;
               } else {
                  this.syntheticAccessor = ((SourceTypeBinding)declaringClass).addSyntheticMethod(codegenBinding, this.isSuperAccess());
                  currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
               }
            }
         }
      }
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      if (this.type != null) {
         output.append("new ");
      }

      if (this.typeArguments != null) {
         output.append('<');
         int max = this.typeArguments.length - 1;

         for(int j = 0; j < max; ++j) {
            this.typeArguments[j].print(0, output);
            output.append(", ");
         }

         this.typeArguments[max].print(0, output);
         output.append('>');
      }

      if (this.type != null) {
         this.type.printExpression(0, output);
      }

      output.append('(');
      if (this.arguments != null) {
         for(int i = 0; i < this.arguments.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.arguments[i].printExpression(0, output);
         }
      }

      return output.append(')');
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      boolean isDiamond = this.type != null && (this.type.bits & 524288) != 0;
      CompilerOptions compilerOptions = scope.compilerOptions();
      long sourceLevel = compilerOptions.sourceLevel;
      if (this.constant != Constant.NotAConstant) {
         this.constant = Constant.NotAConstant;
         if (this.type == null) {
            this.resolvedType = scope.enclosingReceiverType();
         } else {
            this.resolvedType = this.type.resolveType(scope, true);
         }

         if (this.type != null) {
            this.checkIllegalNullAnnotation(scope, this.resolvedType);
            if (this.type instanceof ParameterizedQualifiedTypeReference) {
               ReferenceBinding currentType = (ReferenceBinding)this.resolvedType;
               if (currentType == null) {
                  return currentType;
               }

               label252:
               while((currentType.modifiers & 8) == 0 && !currentType.isRawType()) {
                  if ((currentType = currentType.enclosingType()) == null) {
                     ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference)this.type;

                     for(int i = qRef.typeArguments.length - 2; i >= 0; --i) {
                        if (qRef.typeArguments[i] != null) {
                           scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, this.resolvedType);
                           break label252;
                        }
                     }
                     break;
                  }
               }
            }
         }

         if (this.typeArguments != null) {
            int length = this.typeArguments.length;
            this.argumentsHaveErrors = sourceLevel < 3211264L;
            this.genericTypeArguments = new TypeBinding[length];

            for(int i = 0; i < length; ++i) {
               TypeReference typeReference = this.typeArguments[i];
               if ((this.genericTypeArguments[i] = typeReference.resolveType(scope, true)) == null) {
                  this.argumentsHaveErrors = true;
               }

               if (this.argumentsHaveErrors && typeReference instanceof Wildcard) {
                  scope.problemReporter().illegalUsageOfWildcard(typeReference);
               }
            }

            if (isDiamond) {
               scope.problemReporter().diamondNotWithExplicitTypeArguments(this.typeArguments);
               return null;
            }

            if (this.argumentsHaveErrors) {
               if (this.arguments != null) {
                  int i = 0;

                  for(int max = this.arguments.length; i < max; ++i) {
                     this.arguments[i].resolveType(scope);
                  }
               }

               return null;
            }
         }

         if (this.arguments != null) {
            this.argumentsHaveErrors = false;
            int length = this.arguments.length;
            this.argumentTypes = new TypeBinding[length];

            for(int i = 0; i < length; ++i) {
               Expression argument = this.arguments[i];
               if (argument instanceof CastExpression) {
                  argument.bits |= 32;
                  this.argsContainCast = true;
               }

               argument.setExpressionContext(ExpressionContext.INVOCATION_CONTEXT);
               if (this.arguments[i].resolvedType != null) {
                  scope.problemReporter().genericInferenceError("Argument was unexpectedly found resolved", this);
               }

               if ((this.argumentTypes[i] = argument.resolveType(scope)) == null) {
                  this.argumentsHaveErrors = true;
               }
            }

            if (this.argumentsHaveErrors) {
               if (isDiamond) {
                  return null;
               }

               if (this.resolvedType instanceof ReferenceBinding) {
                  TypeBinding[] pseudoArgs = new TypeBinding[length];
                  int i = length;

                  while(--i >= 0) {
                     pseudoArgs[i] = (TypeBinding)(this.argumentTypes[i] == null ? TypeBinding.NULL : this.argumentTypes[i]);
                  }

                  this.binding = scope.findMethod((ReferenceBinding)this.resolvedType, TypeConstants.INIT, pseudoArgs, this, false);
                  if (this.binding != null && !this.binding.isValidBinding()) {
                     MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
                     if (closestMatch != null) {
                        if (closestMatch.original().typeVariables != Binding.NO_TYPE_VARIABLES) {
                           closestMatch = scope.environment().createParameterizedGenericMethod(closestMatch.original(), null);
                        }

                        this.binding = closestMatch;
                        MethodBinding closestMatchOriginal = closestMatch.original();
                        if (closestMatchOriginal.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(closestMatchOriginal)) {
                           closestMatchOriginal.modifiers |= 134217728;
                        }
                     }
                  }
               }

               return this.resolvedType;
            }
         }

         if (this.resolvedType == null || !this.resolvedType.isValidBinding()) {
            return null;
         }

         if (this.type != null && !this.resolvedType.canBeInstantiated()) {
            scope.problemReporter().cannotInstantiate(this.type, this.resolvedType);
            return this.resolvedType;
         }
      }

      if (isDiamond) {
         this.binding = this.inferConstructorOfElidedParameterizedType(scope);
         if (this.binding == null || !this.binding.isValidBinding()) {
            scope.problemReporter().cannotInferElidedTypes(this);
            return this.resolvedType = null;
         }

         if (this.typeExpected == null && compilerOptions.sourceLevel >= 3407872L && this.expressionContext.definesTargetType()) {
            return new PolyTypeBinding(this);
         }

         this.resolvedType = this.type.resolvedType = this.binding.declaringClass;
         resolvePolyExpressionArguments(this, this.binding, this.argumentTypes, scope);
      } else {
         this.binding = this.findConstructorBinding(scope, this, (ReferenceBinding)this.resolvedType, this.argumentTypes);
      }

      if (!this.binding.isValidBinding()) {
         if (this.binding.declaringClass == null) {
            this.binding.declaringClass = (ReferenceBinding)this.resolvedType;
         }

         if (this.type != null && !this.type.resolvedType.isValidBinding()) {
            return null;
         } else {
            scope.problemReporter().invalidConstructor(this, this.binding);
            return this.resolvedType;
         }
      } else {
         if ((this.binding.tagBits & 128L) != 0L) {
            scope.problemReporter().missingTypeInConstructor(this, this.binding);
         }

         if (this.isMethodUseDeprecated(this.binding, scope, true)) {
            scope.problemReporter().deprecatedMethod(this.binding, this);
         }

         if (checkInvocationArguments(scope, null, this.resolvedType, this.binding, this.arguments, this.argumentTypes, this.argsContainCast, this)) {
            this.bits |= 65536;
         }

         if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
            scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
         }

         if (!isDiamond && this.resolvedType.isParameterizedTypeWithActualArguments()) {
            this.checkTypeArgumentRedundancy((ParameterizedTypeBinding)this.resolvedType, scope);
         }

         if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
            if ((this.binding.tagBits & 4096L) == 0L) {
               new ImplicitNullAnnotationVerifier(scope.environment(), compilerOptions.inheritNullAnnotations)
                  .checkImplicitNullAnnotations(this.binding, null, false, scope);
            }

            if (compilerOptions.sourceLevel >= 3407872L && this.binding instanceof ParameterizedGenericMethodBinding && this.typeArguments != null) {
               TypeVariableBinding[] typeVariables = this.binding.original().typeVariables();

               for(int i = 0; i < this.typeArguments.length; ++i) {
                  this.typeArguments[i].checkNullConstraints(scope, (ParameterizedGenericMethodBinding)this.binding, typeVariables, i);
               }
            }
         }

         if (compilerOptions.sourceLevel >= 3407872L && this.binding.getTypeAnnotations() != Binding.NO_ANNOTATIONS) {
            this.resolvedType = scope.environment().createAnnotatedType(this.resolvedType, this.binding.getTypeAnnotations());
         }

         return this.resolvedType;
      }
   }

   void checkIllegalNullAnnotation(BlockScope scope, TypeBinding allocationType) {
      if (allocationType != null) {
         long nullTagBits = allocationType.tagBits & 108086391056891904L;
         if (nullTagBits != 0L) {
            Annotation annotation = this.type.findAnnotation(nullTagBits);
            if (annotation != null) {
               scope.problemReporter().nullAnnotationUnsupportedLocation(annotation);
            }
         }
      }
   }

   @Override
   public boolean isBoxingCompatibleWith(TypeBinding targetType, Scope scope) {
      return this.isPolyExpression() ? false : this.isCompatibleWith(scope.boxing(targetType), scope);
   }

   @Override
   public boolean isCompatibleWith(TypeBinding targetType, Scope scope) {
      if (!this.argumentsHaveErrors && this.binding != null && this.binding.isValidBinding() && targetType != null && scope != null) {
         TypeBinding allocationType = this.resolvedType;
         if (this.isPolyExpression()) {
            TypeBinding originalExpectedType = this.typeExpected;

            try {
               MethodBinding method = this.solutionsPerTargetType != null ? this.solutionsPerTargetType.get(targetType) : null;
               if (method == null) {
                  this.typeExpected = targetType;
                  method = this.inferConstructorOfElidedParameterizedType(scope);
                  if (method == null || !method.isValidBinding()) {
                     return false;
                  }
               }

               allocationType = method.declaringClass;
            } finally {
               this.typeExpected = originalExpectedType;
            }
         }

         return allocationType != null && allocationType.isCompatibleWith(targetType, scope);
      } else {
         return false;
      }
   }

   public MethodBinding inferConstructorOfElidedParameterizedType(Scope scope) {
      if (this.typeExpected != null && this.binding != null) {
         MethodBinding cached = this.solutionsPerTargetType != null ? this.solutionsPerTargetType.get(this.typeExpected) : null;
         if (cached != null) {
            return cached;
         }
      }

      ReferenceBinding genericType = ((ParameterizedTypeBinding)this.resolvedType).genericType();
      ReferenceBinding enclosingType = this.resolvedType.enclosingType();
      ParameterizedTypeBinding allocationType = scope.environment().createParameterizedType(genericType, genericType.typeVariables(), enclosingType);
      MethodBinding factory = scope.getStaticFactory(allocationType, enclosingType, this.argumentTypes, this);
      if (factory instanceof ParameterizedGenericMethodBinding && factory.isValidBinding()) {
         ParameterizedGenericMethodBinding genericFactory = (ParameterizedGenericMethodBinding)factory;
         this.inferredReturnType = genericFactory.inferredReturnType;
         SyntheticFactoryMethodBinding sfmb = (SyntheticFactoryMethodBinding)factory.original();
         TypeVariableBinding[] constructorTypeVariables = sfmb.getConstructor().typeVariables();
         TypeBinding[] constructorTypeArguments = constructorTypeVariables != null ? new TypeBinding[constructorTypeVariables.length] : Binding.NO_TYPES;
         if (constructorTypeArguments.length > 0) {
            System.arraycopy(
               ((ParameterizedGenericMethodBinding)factory).typeArguments,
               sfmb.typeVariables().length - constructorTypeArguments.length,
               constructorTypeArguments,
               0,
               constructorTypeArguments.length
            );
         }

         MethodBinding constructor = sfmb.applyTypeArgumentsOnConstructor(
            ((ParameterizedTypeBinding)factory.returnType).arguments, constructorTypeArguments, genericFactory.inferredWithUncheckedConversion
         );
         if (constructor instanceof ParameterizedGenericMethodBinding
            && scope.compilerOptions().sourceLevel >= 3407872L
            && this.expressionContext == ExpressionContext.INVOCATION_CONTEXT
            && this.typeExpected == null) {
            constructor = ParameterizedGenericMethodBinding.computeCompatibleMethod18(constructor.shallowOriginal(), this.argumentTypes, scope, this);
         }

         if (this.typeExpected != null) {
            this.registerResult(this.typeExpected, constructor);
         }

         return constructor;
      } else {
         return null;
      }
   }

   public TypeBinding[] inferElidedTypes(Scope scope) {
      ReferenceBinding genericType = ((ParameterizedTypeBinding)this.resolvedType).genericType();
      ReferenceBinding enclosingType = this.resolvedType.enclosingType();
      ParameterizedTypeBinding allocationType = scope.environment().createParameterizedType(genericType, genericType.typeVariables(), enclosingType);
      MethodBinding factory = scope.getStaticFactory(allocationType, enclosingType, this.argumentTypes, this);
      if (factory instanceof ParameterizedGenericMethodBinding && factory.isValidBinding()) {
         ParameterizedGenericMethodBinding genericFactory = (ParameterizedGenericMethodBinding)factory;
         this.inferredReturnType = genericFactory.inferredReturnType;
         return ((ParameterizedTypeBinding)factory.returnType).arguments;
      } else {
         return null;
      }
   }

   public void checkTypeArgumentRedundancy(ParameterizedTypeBinding allocationType, BlockScope scope) {
      if (scope.problemReporter().computeSeverity(16778100) != 256 && scope.compilerOptions().sourceLevel >= 3342336L) {
         if (allocationType.arguments != null) {
            if (this.genericTypeArguments == null) {
               if (this.type != null) {
                  if (this.argumentTypes == Binding.NO_PARAMETERS && this.typeExpected instanceof ParameterizedTypeBinding) {
                     ParameterizedTypeBinding expected = (ParameterizedTypeBinding)this.typeExpected;
                     if (expected.arguments != null && allocationType.arguments.length == expected.arguments.length) {
                        int i = 0;

                        while(i < allocationType.arguments.length && !TypeBinding.notEquals(allocationType.arguments[i], expected.arguments[i])) {
                           ++i;
                        }

                        if (i == allocationType.arguments.length) {
                           scope.problemReporter().redundantSpecificationOfTypeArguments(this.type, allocationType.arguments);
                           return;
                        }
                     }
                  }

                  int previousBits = this.type.bits;

                  TypeBinding[] inferredTypes;
                  try {
                     this.type.bits |= 524288;
                     inferredTypes = this.inferElidedTypes(scope);
                  } finally {
                     this.type.bits = previousBits;
                  }

                  if (inferredTypes != null) {
                     for(int i = 0; i < inferredTypes.length; ++i) {
                        if (TypeBinding.notEquals(inferredTypes[i], allocationType.arguments[i])) {
                           return;
                        }
                     }

                     scope.problemReporter().redundantSpecificationOfTypeArguments(this.type, allocationType.arguments);
                  }
               }
            }
         }
      }
   }

   @Override
   public void setActualReceiverType(ReferenceBinding receiverType) {
   }

   @Override
   public void setDepth(int i) {
   }

   @Override
   public void setFieldIndex(int i) {
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.typeArguments != null) {
            int i = 0;

            for(int typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; ++i) {
               this.typeArguments[i].traverse(visitor, scope);
            }
         }

         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.arguments != null) {
            int i = 0;

            for(int argumentsLength = this.arguments.length; i < argumentsLength; ++i) {
               this.arguments[i].traverse(visitor, scope);
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public void setExpectedType(TypeBinding expectedType) {
      this.typeExpected = expectedType;
   }

   @Override
   public void setExpressionContext(ExpressionContext context) {
      this.expressionContext = context;
   }

   @Override
   public boolean isPolyExpression() {
      return this.isPolyExpression(this.binding);
   }

   @Override
   public boolean isPolyExpression(MethodBinding method) {
      return (this.expressionContext == ExpressionContext.ASSIGNMENT_CONTEXT || this.expressionContext == ExpressionContext.INVOCATION_CONTEXT)
         && this.type != null
         && (this.type.bits & 524288) != 0;
   }

   @Override
   public TypeBinding invocationTargetType() {
      return this.typeExpected;
   }

   @Override
   public boolean statementExpression() {
      return (this.bits & 534773760) == 0;
   }

   @Override
   public MethodBinding binding() {
      return this.binding;
   }

   @Override
   public Expression[] arguments() {
      return this.arguments;
   }

   @Override
   public void registerInferenceContext(ParameterizedGenericMethodBinding method, InferenceContext18 infCtx18) {
      if (this.inferenceContexts == null) {
         this.inferenceContexts = new SimpleLookupTable();
      }

      this.inferenceContexts.put(method, infCtx18);
   }

   @Override
   public void registerResult(TypeBinding targetType, MethodBinding method) {
      if (method != null && method.isConstructor()) {
         if (this.solutionsPerTargetType == null) {
            this.solutionsPerTargetType = new HashMap<>();
         }

         this.solutionsPerTargetType.put(targetType, method);
      }
   }

   @Override
   public InferenceContext18 getInferenceContext(ParameterizedMethodBinding method) {
      return this.inferenceContexts == null ? null : (InferenceContext18)this.inferenceContexts.get(method);
   }

   @Override
   public void cleanUpInferenceContexts() {
      if (this.inferenceContexts != null) {
         for(Object value : this.inferenceContexts.valueTable) {
            if (value != null) {
               ((InferenceContext18)value).cleanUp();
            }
         }

         this.inferenceContexts = null;
         this.outerInferenceContext = null;
         this.solutionsPerTargetType = null;
      }
   }

   @Override
   public ExpressionContext getExpressionContext() {
      return this.expressionContext;
   }

   @Override
   public InferenceContext18 freshInferenceContext(Scope scope) {
      return new InferenceContext18(scope, this.arguments, this, this.outerInferenceContext);
   }
}
