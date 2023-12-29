package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class QualifiedAllocationExpression extends AllocationExpression {
   public Expression enclosingInstance;
   public TypeDeclaration anonymousType;

   public QualifiedAllocationExpression() {
   }

   public QualifiedAllocationExpression(TypeDeclaration anonymousType) {
      this.anonymousType = anonymousType;
      anonymousType.allocation = this;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.enclosingInstance != null) {
         flowInfo = this.enclosingInstance.analyseCode(currentScope, flowContext, flowInfo);
      } else if (this.binding != null && this.binding.declaringClass != null) {
         ReferenceBinding superclass = this.binding.declaringClass.superclass();
         if (superclass != null && superclass.isMemberType() && !superclass.isStatic()) {
            currentScope.tagAsAccessingEnclosingInstanceStateOf(superclass.enclosingType(), false);
         }
      }

      this.checkCapturedLocalInitializationIfNecessary(
         (ReferenceBinding)(this.anonymousType == null ? this.binding.declaringClass.erasure() : this.binding.declaringClass.superclass().erasure()),
         currentScope,
         flowInfo
      );
      if (this.arguments != null) {
         boolean analyseResources = currentScope.compilerOptions().analyseResourceLeaks;
         boolean hasResourceWrapperType = analyseResources
            && this.resolvedType instanceof ReferenceBinding
            && ((ReferenceBinding)this.resolvedType).hasTypeBit(4);
         int i = 0;

         for(int count = this.arguments.length; i < count; ++i) {
            flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo);
            if (analyseResources && !hasResourceWrapperType) {
               flowInfo = FakedTrackingVariable.markPassedToOutside(currentScope, this.arguments[i], flowInfo, flowContext, false);
            }

            this.arguments[i].checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
         }

         this.analyseArguments(currentScope, flowContext, flowInfo, this.binding, this.arguments);
      }

      if (this.anonymousType != null) {
         flowInfo = this.anonymousType.analyseCode(currentScope, flowContext, flowInfo);
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

      this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
      this.manageSyntheticAccessIfNecessary(currentScope, flowInfo);
      flowContext.recordAbruptExit();
      return flowInfo;
   }

   @Override
   public Expression enclosingInstance() {
      return this.enclosingInstance;
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
      if (this.anonymousType != null) {
         this.anonymousType.generateCode(currentScope, codeStream);
      }
   }

   @Override
   public boolean isSuperAccess() {
      return this.anonymousType != null;
   }

   @Override
   public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         ReferenceBinding allocatedTypeErasure = (ReferenceBinding)this.binding.declaringClass.erasure();
         if (allocatedTypeErasure.isNestedType() && (currentScope.enclosingSourceType().isLocalType() || currentScope.isLambdaSubscope())) {
            if (allocatedTypeErasure.isLocalType()) {
               ((LocalTypeBinding)allocatedTypeErasure).addInnerEmulationDependent(currentScope, this.enclosingInstance != null);
            } else {
               currentScope.propagateInnerEmulation(allocatedTypeErasure, this.enclosingInstance != null);
            }
         }
      }
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      if (this.enclosingInstance != null) {
         this.enclosingInstance.printExpression(0, output).append('.');
      }

      super.printExpression(0, output);
      if (this.anonymousType != null) {
         this.anonymousType.print(indent, output);
      }

      return output;
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      if (this.anonymousType == null && this.enclosingInstance == null) {
         return super.resolveType(scope);
      } else {
         TypeBinding result = this.resolveTypeForQualifiedAllocationExpression(scope);
         if (result != null && !result.isPolyType() && this.binding != null) {
            CompilerOptions compilerOptions = scope.compilerOptions();
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
         }

         return result;
      }
   }

   private TypeBinding resolveTypeForQualifiedAllocationExpression(BlockScope scope) {
      boolean isDiamond = this.type != null && (this.type.bits & 524288) != 0;
      TypeBinding enclosingInstanceType = null;
      TypeBinding receiverType = null;
      long sourceLevel = scope.compilerOptions().sourceLevel;
      if (this.constant != Constant.NotAConstant) {
         this.constant = Constant.NotAConstant;
         ReferenceBinding enclosingInstanceReference = null;
         boolean hasError = false;
         boolean enclosingInstanceContainsCast = false;
         if (this.enclosingInstance != null) {
            if (this.enclosingInstance instanceof CastExpression) {
               this.enclosingInstance.bits |= 32;
               enclosingInstanceContainsCast = true;
            }

            if ((enclosingInstanceType = this.enclosingInstance.resolveType(scope)) == null) {
               hasError = true;
            } else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
               scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(enclosingInstanceType, this.enclosingInstance);
               hasError = true;
            } else if (this.type instanceof QualifiedTypeReference) {
               scope.problemReporter().illegalUsageOfQualifiedTypeReference((QualifiedTypeReference)this.type);
               hasError = true;
            } else if (!(enclosingInstanceReference = (ReferenceBinding)enclosingInstanceType).canBeSeenBy(scope)) {
               enclosingInstanceType = new ProblemReferenceBinding(enclosingInstanceReference.compoundName, enclosingInstanceReference, 2);
               scope.problemReporter().invalidType(this.enclosingInstance, enclosingInstanceType);
               hasError = true;
            } else {
               this.resolvedType = receiverType = ((SingleTypeReference)this.type).resolveTypeEnclosing(scope, (ReferenceBinding)enclosingInstanceType);
               this.checkIllegalNullAnnotation(scope, receiverType);
               if (receiverType != null && enclosingInstanceContainsCast) {
                  CastExpression.checkNeedForEnclosingInstanceCast(scope, this.enclosingInstance, enclosingInstanceType, receiverType);
               }
            }
         } else if (this.type == null) {
            receiverType = scope.enclosingSourceType();
         } else {
            receiverType = this.type.resolveType(scope, true);
            this.checkIllegalNullAnnotation(scope, receiverType);
            if (receiverType != null && receiverType.isValidBinding() && this.type instanceof ParameterizedQualifiedTypeReference) {
               ReferenceBinding currentType = (ReferenceBinding)receiverType;

               label350:
               while((currentType.modifiers & 8) == 0 && !currentType.isRawType()) {
                  if ((currentType = currentType.enclosingType()) == null) {
                     ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference)this.type;

                     for(int i = qRef.typeArguments.length - 2; i >= 0; --i) {
                        if (qRef.typeArguments[i] != null) {
                           scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, receiverType);
                           break label350;
                        }
                     }
                     break;
                  }
               }
            }
         }

         if (receiverType == null || !receiverType.isValidBinding()) {
            hasError = true;
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

         this.argumentTypes = Binding.NO_PARAMETERS;
         if (this.arguments != null) {
            int length = this.arguments.length;
            this.argumentTypes = new TypeBinding[length];

            for(int i = 0; i < length; ++i) {
               Expression argument = this.arguments[i];
               if (argument instanceof CastExpression) {
                  argument.bits |= 32;
                  this.argsContainCast = true;
               }

               argument.setExpressionContext(ExpressionContext.INVOCATION_CONTEXT);
               if ((this.argumentTypes[i] = argument.resolveType(scope)) == null) {
                  hasError = true;
                  this.argumentsHaveErrors = true;
               }
            }
         }

         if (hasError) {
            if (isDiamond) {
               return null;
            }

            if (receiverType instanceof ReferenceBinding) {
               ReferenceBinding referenceReceiver = (ReferenceBinding)receiverType;
               if (receiverType.isValidBinding()) {
                  int length = this.arguments == null ? 0 : this.arguments.length;
                  TypeBinding[] pseudoArgs = new TypeBinding[length];
                  int i = length;

                  while(--i >= 0) {
                     pseudoArgs[i] = (TypeBinding)(this.argumentTypes[i] == null ? TypeBinding.NULL : this.argumentTypes[i]);
                  }

                  this.binding = scope.findMethod(referenceReceiver, TypeConstants.INIT, pseudoArgs, this, false);
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

               if (this.anonymousType != null) {
                  scope.addAnonymousType(this.anonymousType, referenceReceiver);
                  this.anonymousType.resolve(scope);
                  return this.resolvedType = this.anonymousType.binding;
               }
            }

            return this.resolvedType = receiverType;
         }

         if (this.anonymousType != null) {
            if (isDiamond) {
               scope.problemReporter().diamondNotWithAnoymousClasses(this.type);
               return null;
            }

            ReferenceBinding superType = (ReferenceBinding)receiverType;
            if (superType.isTypeVariable()) {
               ReferenceBinding var20 = new ProblemReferenceBinding(new char[][]{superType.sourceName()}, superType, 9);
               scope.problemReporter().invalidType(this, var20);
               return null;
            }

            if (this.type != null && superType.isEnum()) {
               scope.problemReporter().cannotInstantiate(this.type, superType);
               return this.resolvedType = superType;
            }

            ReferenceBinding anonymousSuperclass = superType.isInterface() ? scope.getJavaLangObject() : superType;
            scope.addAnonymousType(this.anonymousType, superType);
            this.anonymousType.resolve(scope);
            this.resolvedType = this.anonymousType.binding;
            if ((this.resolvedType.tagBits & 131072L) != 0L) {
               return null;
            }

            MethodBinding inheritedBinding = this.findConstructorBinding(scope, this, anonymousSuperclass, this.argumentTypes);
            if (!inheritedBinding.isValidBinding()) {
               if (inheritedBinding.declaringClass == null) {
                  inheritedBinding.declaringClass = anonymousSuperclass;
               }

               if (this.type != null && !this.type.resolvedType.isValidBinding()) {
                  return null;
               }

               scope.problemReporter().invalidConstructor(this, inheritedBinding);
               return this.resolvedType;
            }

            if ((inheritedBinding.tagBits & 128L) != 0L) {
               scope.problemReporter().missingTypeInConstructor(this, inheritedBinding);
            }

            if (this.enclosingInstance != null) {
               ReferenceBinding targetEnclosing = inheritedBinding.declaringClass.enclosingType();
               if (targetEnclosing == null) {
                  scope.problemReporter().unnecessaryEnclosingInstanceSpecification(this.enclosingInstance, superType);
                  return this.resolvedType;
               }

               if (!enclosingInstanceType.isCompatibleWith(targetEnclosing) && !scope.isBoxingCompatibleWith(enclosingInstanceType, targetEnclosing)) {
                  scope.problemReporter().typeMismatchError(enclosingInstanceType, targetEnclosing, this.enclosingInstance, null);
                  return this.resolvedType;
               }

               this.enclosingInstance.computeConversion(scope, targetEnclosing, enclosingInstanceType);
            }

            if (this.arguments != null
               && checkInvocationArguments(scope, null, anonymousSuperclass, inheritedBinding, this.arguments, this.argumentTypes, this.argsContainCast, this)
               )
             {
               this.bits |= 65536;
            }

            if (this.typeArguments != null && inheritedBinding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
               scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(inheritedBinding, this.genericTypeArguments, this.typeArguments);
            }

            this.binding = this.anonymousType
               .createDefaultConstructorWithBinding(inheritedBinding, (this.bits & 65536) != 0 && this.genericTypeArguments == null);
            return this.resolvedType;
         }

         if (!receiverType.canBeInstantiated()) {
            scope.problemReporter().cannotInstantiate(this.type, receiverType);
            return this.resolvedType = receiverType;
         }
      } else if (this.enclosingInstance != null) {
         enclosingInstanceType = this.enclosingInstance.resolvedType;
         receiverType = this.type.resolvedType;
         this.resolvedType = this.type.resolvedType;
      }

      if (isDiamond) {
         this.binding = this.inferConstructorOfElidedParameterizedType(scope);
         if (this.binding == null || !this.binding.isValidBinding()) {
            scope.problemReporter().cannotInferElidedTypes(this);
            return this.resolvedType = null;
         }

         if (this.typeExpected == null && sourceLevel >= 3407872L && this.expressionContext.definesTargetType()) {
            return new PolyTypeBinding(this);
         }

         receiverType = this.binding.declaringClass;
         this.resolvedType = this.type.resolvedType = this.binding.declaringClass;
         resolvePolyExpressionArguments(this, this.binding, this.argumentTypes, scope);
      } else {
         this.binding = this.findConstructorBinding(scope, this, (ReferenceBinding)receiverType, this.argumentTypes);
      }

      if (this.binding.isValidBinding()) {
         if (this.isMethodUseDeprecated(this.binding, scope, true)) {
            scope.problemReporter().deprecatedMethod(this.binding, this);
         }

         if (checkInvocationArguments(scope, null, receiverType, this.binding, this.arguments, this.argumentTypes, this.argsContainCast, this)) {
            this.bits |= 65536;
         }

         if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
            scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
         }

         if ((this.binding.tagBits & 128L) != 0L) {
            scope.problemReporter().missingTypeInConstructor(this, this.binding);
         }

         if (!isDiamond && receiverType.isParameterizedTypeWithActualArguments()) {
            this.checkTypeArgumentRedundancy((ParameterizedTypeBinding)receiverType, scope);
         }

         ReferenceBinding expectedType = this.binding.declaringClass.enclosingType();
         if (TypeBinding.notEquals(expectedType, enclosingInstanceType)) {
            scope.compilationUnitScope().recordTypeConversion(expectedType, enclosingInstanceType);
         }

         if (!enclosingInstanceType.isCompatibleWith(expectedType) && !scope.isBoxingCompatibleWith(enclosingInstanceType, expectedType)) {
            scope.problemReporter().typeMismatchError(enclosingInstanceType, expectedType, this.enclosingInstance, null);
            return this.resolvedType = receiverType;
         } else {
            this.enclosingInstance.computeConversion(scope, expectedType, enclosingInstanceType);
            return this.resolvedType = receiverType;
         }
      } else {
         if (this.binding.declaringClass == null) {
            this.binding.declaringClass = (ReferenceBinding)receiverType;
         }

         if (this.type != null && !this.type.resolvedType.isValidBinding()) {
            return null;
         } else {
            scope.problemReporter().invalidConstructor(this, this.binding);
            return this.resolvedType = receiverType;
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.enclosingInstance != null) {
            this.enclosingInstance.traverse(visitor, scope);
         }

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
            int argumentsLength = this.arguments.length;

            for(int i = 0; i < argumentsLength; ++i) {
               this.arguments[i].traverse(visitor, scope);
            }
         }

         if (this.anonymousType != null) {
            this.anonymousType.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }
}
