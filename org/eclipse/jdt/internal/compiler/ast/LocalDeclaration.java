package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;

public class LocalDeclaration extends AbstractVariableDeclaration {
   public LocalVariableBinding binding;

   public LocalDeclaration(char[] name, int sourceStart, int sourceEnd) {
      this.name = name;
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
      this.declarationEnd = sourceEnd;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         this.bits |= 1073741824;
      }

      if (this.initialization == null) {
         return flowInfo;
      } else {
         this.initialization.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
         FlowInfo preInitInfo = null;
         boolean shouldAnalyseResource = this.binding != null
            && flowInfo.reachMode() == 0
            && currentScope.compilerOptions().analyseResourceLeaks
            && FakedTrackingVariable.isAnyCloseable(this.initialization.resolvedType);
         if (shouldAnalyseResource) {
            preInitInfo = flowInfo.unconditionalCopy();
            FakedTrackingVariable.preConnectTrackerAcrossAssignment(this, this.binding, this.initialization, flowInfo);
         }

         FlowInfo var7 = this.initialization.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
         if (shouldAnalyseResource) {
            FakedTrackingVariable.handleResourceAssignment(currentScope, preInitInfo, var7, flowContext, this, this.initialization, this.binding);
         } else {
            FakedTrackingVariable.cleanUpAfterAssignment(currentScope, 2, this.initialization);
         }

         int nullStatus = this.initialization.nullStatus(var7, flowContext);
         if (!var7.isDefinitelyAssigned(this.binding)) {
            this.bits |= 8;
         } else {
            this.bits &= -9;
         }

         var7.markAsDefinitelyAssigned(this.binding);
         if (currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled) {
            nullStatus = NullAnnotationMatching.checkAssignment(
               currentScope, flowContext, this.binding, var7, nullStatus, this.initialization, this.initialization.resolvedType
            );
         }

         if ((this.binding.type.tagBits & 2L) == 0L) {
            var7.markNullStatus(this.binding, nullStatus);
         }

         return var7;
      }
   }

   public void checkModifiers() {
      if ((this.modifiers & 65535 & -17) != 0) {
         this.modifiers = this.modifiers & -4194305 | 8388608;
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if (this.binding.resolvedPosition != -1) {
         codeStream.addVisibleLocalVariable(this.binding);
      }

      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         if (this.initialization != null) {
            if (this.binding.resolvedPosition < 0) {
               if (this.initialization.constant == Constant.NotAConstant) {
                  this.initialization.generateCode(currentScope, codeStream, false);
               }
            } else {
               this.initialization.generateCode(currentScope, codeStream, true);
               if (this.binding.type.isArrayType()
                  && this.initialization instanceof CastExpression
                  && ((CastExpression)this.initialization).innermostCastedExpression().resolvedType == TypeBinding.NULL) {
                  codeStream.checkcast(this.binding.type);
               }

               codeStream.store(this.binding, false);
               if ((this.bits & 8) != 0) {
                  this.binding.recordInitializationStartPC(codeStream.position);
               }
            }
         }

         codeStream.recordPositionsFrom(pc, this.sourceStart);
      }
   }

   @Override
   public int getKind() {
      return 4;
   }

   public void getAllAnnotationContexts(int targetType, LocalVariableBinding localVariable, List allAnnotationContexts) {
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(this, targetType, localVariable, allAnnotationContexts);
      this.traverseWithoutInitializer(collector, null);
   }

   public void getAllAnnotationContexts(int targetType, int parameterIndex, List allAnnotationContexts) {
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(this, targetType, parameterIndex, allAnnotationContexts);
      this.traverse(collector, null);
   }

   public boolean isArgument() {
      return false;
   }

   public boolean isReceiver() {
      return false;
   }

   @Override
   public void resolve(BlockScope scope) {
      TypeBinding variableType = this.type.resolveType(scope, true);
      this.bits |= this.type.bits & 1048576;
      this.checkModifiers();
      if (variableType != null) {
         if (variableType == TypeBinding.VOID) {
            scope.problemReporter().variableTypeCannotBeVoid(this);
            return;
         }

         if (variableType.isArrayType() && ((ArrayBinding)variableType).leafComponentType == TypeBinding.VOID) {
            scope.problemReporter().variableTypeCannotBeVoidArray(this);
            return;
         }
      }

      Binding existingVariable = scope.getBinding(this.name, 3, this, false);
      if (existingVariable != null && existingVariable.isValidBinding()) {
         boolean localExists = existingVariable instanceof LocalVariableBinding;
         if (localExists && (this.bits & 2097152) != 0 && scope.isLambdaSubscope() && this.hiddenVariableDepth == 0) {
            scope.problemReporter().lambdaRedeclaresLocal(this);
         } else if (localExists && this.hiddenVariableDepth == 0) {
            scope.problemReporter().redefineLocal(this);
         } else {
            scope.problemReporter().localVariableHiding(this, existingVariable, false);
         }
      }

      if ((this.modifiers & 16) != 0 && this.initialization == null) {
         this.modifiers |= 67108864;
      }

      this.binding = new LocalVariableBinding(this, variableType, this.modifiers, false);
      scope.addLocalVariable(this.binding);
      this.binding.setConstant(Constant.NotAConstant);
      if (variableType == null) {
         if (this.initialization != null) {
            this.initialization.resolveType(scope);
         }
      } else {
         if (this.initialization != null) {
            if (this.initialization instanceof ArrayInitializer) {
               TypeBinding initializationType = this.initialization.resolveTypeExpecting(scope, variableType);
               if (initializationType != null) {
                  ((ArrayInitializer)this.initialization).binding = (ArrayBinding)initializationType;
                  this.initialization.computeConversion(scope, variableType, initializationType);
               }
            } else {
               this.initialization.setExpressionContext(ExpressionContext.ASSIGNMENT_CONTEXT);
               this.initialization.setExpectedType(variableType);
               TypeBinding initializationType = this.initialization.resolveType(scope);
               if (initializationType != null) {
                  if (TypeBinding.notEquals(variableType, initializationType)) {
                     scope.compilationUnitScope().recordTypeConversion(variableType, initializationType);
                  }

                  if (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, variableType)
                     || initializationType.isCompatibleWith(variableType, scope)) {
                     this.initialization.computeConversion(scope, variableType, initializationType);
                     if (initializationType.needsUncheckedConversion(variableType)) {
                        scope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, variableType);
                     }

                     if (this.initialization instanceof CastExpression && (this.initialization.bits & 16384) == 0) {
                        CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression)this.initialization);
                     }
                  } else if (this.isBoxingCompatible(initializationType, variableType, this.initialization, scope)) {
                     this.initialization.computeConversion(scope, variableType, initializationType);
                     if (this.initialization instanceof CastExpression && (this.initialization.bits & 16384) == 0) {
                        CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression)this.initialization);
                     }
                  } else if ((variableType.tagBits & 128L) == 0L) {
                     scope.problemReporter().typeMismatchError(initializationType, variableType, this.initialization, null);
                  }
               }
            }

            if (this.binding == Expression.getDirectBinding(this.initialization)) {
               scope.problemReporter().assignmentHasNoEffect(this, this.name);
            }

            this.binding
               .setConstant(
                  this.binding.isFinal()
                     ? this.initialization.constant.castTo((variableType.id << 4) + this.initialization.constant.typeID())
                     : Constant.NotAConstant
               );
         }

         resolveAnnotations(scope, this.annotations, this.binding, true);
         Annotation.isTypeUseCompatible(this.type, scope, this.annotations);
         if (!scope.validateNullAnnotation(this.binding.tagBits, this.type, this.annotations)) {
            this.binding.tagBits &= -108086391056891905L;
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, scope);
            }
         }

         this.type.traverse(visitor, scope);
         if (this.initialization != null) {
            this.initialization.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }

   private void traverseWithoutInitializer(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, scope);
            }
         }

         this.type.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }

   public boolean isRecoveredFromLoneIdentifier() {
      return this.name == RecoveryScanner.FAKE_IDENTIFIER
         && (this.type instanceof SingleTypeReference || this.type instanceof QualifiedTypeReference && !(this.type instanceof ArrayQualifiedTypeReference))
         && this.initialization == null
         && !this.type.isBaseTypeReference();
   }
}
