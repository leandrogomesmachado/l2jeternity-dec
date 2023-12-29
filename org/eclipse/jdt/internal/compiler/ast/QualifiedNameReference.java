package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class QualifiedNameReference extends NameReference {
   public char[][] tokens;
   public long[] sourcePositions;
   public FieldBinding[] otherBindings;
   int[] otherDepths;
   public int indexOfFirstFieldBinding;
   public SyntheticMethodBinding syntheticWriteAccessor;
   public SyntheticMethodBinding[] syntheticReadAccessors;
   public TypeBinding genericCast;
   public TypeBinding[] otherGenericCasts;

   public QualifiedNameReference(char[][] tokens, long[] positions, int sourceStart, int sourceEnd) {
      this.tokens = tokens;
      this.sourcePositions = positions;
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
   }

   @Override
   public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
      int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;
      boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
      boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= 3145728L;
      FieldBinding lastFieldBinding = null;
      switch(this.bits & 7) {
         case 1:
            lastFieldBinding = (FieldBinding)this.binding;
            if (needValue || complyTo14) {
               this.manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, 0, flowInfo);
            }

            if (lastFieldBinding.isBlankFinal() && this.otherBindings != null && currentScope.needBlankFinalFieldInitializationCheck(lastFieldBinding)) {
               FlowInfo fieldInits = flowContext.getInitsForFinalBlankInitializationCheck(lastFieldBinding.declaringClass.original(), flowInfo);
               if (!fieldInits.isDefinitelyAssigned(lastFieldBinding)) {
                  currentScope.problemReporter().uninitializedBlankFinalField(lastFieldBinding, this);
               }
            }
            break;
         case 2:
            LocalVariableBinding localBinding;
            if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding)this.binding)) {
               currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
            }

            if ((flowInfo.tagBits & 3) == 0) {
               localBinding.useFlag = 1;
            } else if (localBinding.useFlag == 0) {
               localBinding.useFlag = 2;
            }

            if (needValue) {
               this.checkInternalNPE(currentScope, flowContext, flowInfo, true);
            }
      }

      if (needValue) {
         this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
      }

      if (this.otherBindings != null) {
         for(int i = 0; i < otherBindingsCount - 1; ++i) {
            lastFieldBinding = this.otherBindings[i];
            needValue = !this.otherBindings[i + 1].isStatic();
            if (needValue || complyTo14) {
               this.manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, i + 1, flowInfo);
            }
         }

         lastFieldBinding = this.otherBindings[otherBindingsCount - 1];
      }

      if (isCompound) {
         if (otherBindingsCount == 0 && lastFieldBinding.isBlankFinal() && currentScope.needBlankFinalFieldInitializationCheck(lastFieldBinding)) {
            FlowInfo fieldInits = flowContext.getInitsForFinalBlankInitializationCheck(lastFieldBinding.declaringClass, flowInfo);
            if (!fieldInits.isDefinitelyAssigned(lastFieldBinding)) {
               currentScope.problemReporter().uninitializedBlankFinalField(lastFieldBinding, this);
            }
         }

         this.manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, otherBindingsCount, flowInfo);
      }

      if (assignment.expression != null) {
         flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
      }

      if (lastFieldBinding.isFinal()) {
         if (otherBindingsCount == 0
            && this.indexOfFirstFieldBinding == 1
            && lastFieldBinding.isBlankFinal()
            && !isCompound
            && currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) {
            if (flowInfo.isPotentiallyAssigned(lastFieldBinding)) {
               currentScope.problemReporter().duplicateInitializationOfBlankFinalField(lastFieldBinding, this);
            } else {
               flowContext.recordSettingFinal(lastFieldBinding, this, flowInfo);
            }

            flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
         } else {
            currentScope.problemReporter().cannotAssignToFinalField(lastFieldBinding, this);
            if (otherBindingsCount == 0 && currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) {
               flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
            }
         }
      }

      this.manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, -1, flowInfo);
      return flowInfo;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      return this.analyseCode(currentScope, flowContext, flowInfo, true);
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
      int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;
      boolean needValue = otherBindingsCount == 0 ? valueRequired : !this.otherBindings[0].isStatic();
      boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= 3145728L;
      switch(this.bits & 7) {
         case 1:
            if (needValue || complyTo14) {
               this.manageSyntheticAccessIfNecessary(currentScope, (FieldBinding)this.binding, 0, flowInfo);
            }

            FieldBinding fieldBinding = (FieldBinding)this.binding;
            if (this.indexOfFirstFieldBinding == 1 && fieldBinding.isBlankFinal() && currentScope.needBlankFinalFieldInitializationCheck(fieldBinding)) {
               FlowInfo fieldInits = flowContext.getInitsForFinalBlankInitializationCheck(fieldBinding.declaringClass.original(), flowInfo);
               if (!fieldInits.isDefinitelyAssigned(fieldBinding)) {
                  currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
               }
            }
            break;
         case 2:
            LocalVariableBinding localBinding;
            if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding)this.binding)) {
               currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
            }

            if ((flowInfo.tagBits & 3) == 0) {
               localBinding.useFlag = 1;
            } else if (localBinding.useFlag == 0) {
               localBinding.useFlag = 2;
            }
      }

      if (needValue) {
         this.checkInternalNPE(currentScope, flowContext, flowInfo, true);
      }

      if (needValue) {
         this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
      }

      if (this.otherBindings != null) {
         for(int i = 0; i < otherBindingsCount; ++i) {
            needValue = i < otherBindingsCount - 1 ? !this.otherBindings[i + 1].isStatic() : valueRequired;
            if (needValue || complyTo14) {
               this.manageSyntheticAccessIfNecessary(currentScope, this.otherBindings[i], i + 1, flowInfo);
            }
         }
      }

      return flowInfo;
   }

   private void checkInternalNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, boolean checkString) {
      if ((this.bits & 7) == 2) {
         LocalVariableBinding local = (LocalVariableBinding)this.binding;
         if (local != null && (local.type.tagBits & 2L) == 0L && (checkString || local.type.id != 11)) {
            if ((this.bits & 131072) == 0) {
               flowContext.recordUsingNullReference(scope, local, this, 3, flowInfo);
            }

            flowInfo.markAsComparedEqualToNonNull(local);
            flowContext.markFinallyNullStatus(local, 4);
         }
      }

      if (this.otherBindings != null) {
         if ((this.bits & 7) == 1) {
            this.checkNullableFieldDereference(scope, (FieldBinding)this.binding, this.sourcePositions[this.indexOfFirstFieldBinding - 1], flowContext, 0);
         }

         int length = this.otherBindings.length - 1;

         for(int i = 0; i < length; ++i) {
            this.checkNullableFieldDereference(scope, this.otherBindings[i], this.sourcePositions[this.indexOfFirstFieldBinding + i], flowContext, 0);
         }
      }
   }

   @Override
   public boolean checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, int ttlForFieldCheck) {
      if (super.checkNPE(scope, flowContext, flowInfo, ttlForFieldCheck)) {
         return true;
      } else {
         FieldBinding fieldBinding = null;
         long position = 0L;
         if (this.otherBindings == null) {
            if ((this.bits & 7) == 1) {
               fieldBinding = (FieldBinding)this.binding;
               position = this.sourcePositions[0];
            }
         } else {
            fieldBinding = this.otherBindings[this.otherBindings.length - 1];
            position = this.sourcePositions[this.sourcePositions.length - 1];
         }

         return fieldBinding != null ? this.checkNullableFieldDereference(scope, fieldBinding, position, flowContext, ttlForFieldCheck) : false;
      }
   }

   @Override
   public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
      if (runtimeTimeType != null && compileTimeType != null) {
         FieldBinding field = null;
         int length = this.otherBindings == null ? 0 : this.otherBindings.length;
         if (length == 0) {
            if ((this.bits & 1) != 0 && this.binding != null && this.binding.isValidBinding()) {
               field = (FieldBinding)this.binding;
            }
         } else {
            field = this.otherBindings[length - 1];
         }

         if (field != null) {
            FieldBinding originalBinding = field.original();
            TypeBinding originalType = originalBinding.type;
            if (originalType.leafComponentType().isTypeVariable()) {
               TypeBinding targetType = !compileTimeType.isBaseType() && runtimeTimeType.isBaseType() ? compileTimeType : runtimeTimeType;
               TypeBinding typeCast = originalType.genericCast(targetType);
               this.setGenericCast(length, typeCast);
               if (typeCast instanceof ReferenceBinding) {
                  ReferenceBinding referenceCast = (ReferenceBinding)typeCast;
                  if (!referenceCast.canBeSeenBy(scope)) {
                     scope.problemReporter()
                        .invalidType(this, new ProblemReferenceBinding(CharOperation.splitOn('.', referenceCast.shortReadableName()), referenceCast, 2));
                  }
               }
            }
         }

         super.computeConversion(scope, runtimeTimeType, compileTimeType);
      }
   }

   @Override
   public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
      int pc = codeStream.position;
      FieldBinding lastFieldBinding = this.generateReadSequence(currentScope, codeStream);
      codeStream.recordPositionsFrom(pc, this.sourceStart);
      assignment.expression.generateCode(currentScope, codeStream, true);
      this.fieldStore(currentScope, codeStream, lastFieldBinding, this.syntheticWriteAccessor, this.getFinalReceiverType(), false, valueRequired);
      if (valueRequired) {
         codeStream.generateImplicitConversion(assignment.implicitConversion);
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      int pc = codeStream.position;
      if (this.constant != Constant.NotAConstant) {
         if (valueRequired) {
            codeStream.generateConstant(this.constant, this.implicitConversion);
         }
      } else {
         FieldBinding lastFieldBinding = this.generateReadSequence(currentScope, codeStream);
         if (lastFieldBinding != null) {
            boolean isStatic = lastFieldBinding.isStatic();
            Constant fieldConstant = lastFieldBinding.constant();
            if (fieldConstant != Constant.NotAConstant) {
               if (!isStatic) {
                  codeStream.invokeObjectGetClass();
                  codeStream.pop();
               }

               if (valueRequired) {
                  codeStream.generateConstant(fieldConstant, this.implicitConversion);
               }
            } else {
               boolean isFirst = lastFieldBinding == this.binding
                  && (this.indexOfFirstFieldBinding == 1 || TypeBinding.equalsEquals(lastFieldBinding.declaringClass, currentScope.enclosingReceiverType()))
                  && this.otherBindings == null;
               TypeBinding requiredGenericCast = this.getGenericCast(this.otherBindings == null ? 0 : this.otherBindings.length);
               if (valueRequired
                  || !isFirst && currentScope.compilerOptions().complianceLevel >= 3145728L
                  || (this.implicitConversion & 1024) != 0
                  || requiredGenericCast != null) {
                  int lastFieldPc = codeStream.position;
                  if (lastFieldBinding.declaringClass == null) {
                     codeStream.arraylength();
                     if (valueRequired) {
                        codeStream.generateImplicitConversion(this.implicitConversion);
                     } else {
                        codeStream.pop();
                     }
                  } else {
                     SyntheticMethodBinding accessor = this.syntheticReadAccessors == null
                        ? null
                        : this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
                     if (accessor == null) {
                        TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
                           currentScope, lastFieldBinding, this.getFinalReceiverType(), isFirst
                        );
                        if (isStatic) {
                           codeStream.fieldAccess((byte)-78, lastFieldBinding, constantPoolDeclaringClass);
                        } else {
                           codeStream.fieldAccess((byte)-76, lastFieldBinding, constantPoolDeclaringClass);
                        }
                     } else {
                        codeStream.invoke((byte)-72, accessor, null);
                     }

                     if (requiredGenericCast != null) {
                        codeStream.checkcast(requiredGenericCast);
                     }

                     if (valueRequired) {
                        codeStream.generateImplicitConversion(this.implicitConversion);
                     } else {
                        boolean isUnboxing = (this.implicitConversion & 1024) != 0;
                        if (isUnboxing) {
                           codeStream.generateImplicitConversion(this.implicitConversion);
                        }

                        switch(isUnboxing ? this.postConversionType(currentScope).id : lastFieldBinding.type.id) {
                           case 7:
                           case 8:
                              codeStream.pop2();
                              break;
                           default:
                              codeStream.pop();
                        }
                     }
                  }

                  int fieldPosition = (int)(this.sourcePositions[this.sourcePositions.length - 1] >>> 32);
                  codeStream.recordPositionsFrom(lastFieldPc, fieldPosition);
               } else if (!isStatic) {
                  codeStream.invokeObjectGetClass();
                  codeStream.pop();
               }
            }
         }
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public void generateCompoundAssignment(
      BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired
   ) {
      FieldBinding lastFieldBinding = this.generateReadSequence(currentScope, codeStream);
      this.reportOnlyUselesslyReadPrivateField(currentScope, lastFieldBinding, valueRequired);
      boolean isFirst = lastFieldBinding == this.binding
         && (this.indexOfFirstFieldBinding == 1 || TypeBinding.equalsEquals(lastFieldBinding.declaringClass, currentScope.enclosingReceiverType()))
         && this.otherBindings == null;
      TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, lastFieldBinding, this.getFinalReceiverType(), isFirst);
      SyntheticMethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
      if (lastFieldBinding.isStatic()) {
         if (accessor == null) {
            codeStream.fieldAccess((byte)-78, lastFieldBinding, constantPoolDeclaringClass);
         } else {
            codeStream.invoke((byte)-72, accessor, null);
         }
      } else {
         codeStream.dup();
         if (accessor == null) {
            codeStream.fieldAccess((byte)-76, lastFieldBinding, constantPoolDeclaringClass);
         } else {
            codeStream.invoke((byte)-72, accessor, null);
         }
      }

      int operationTypeID;
      switch(operationTypeID = (this.implicitConversion & 0xFF) >> 4) {
         case 0:
         case 1:
         case 11:
            codeStream.generateStringConcatenationAppend(currentScope, null, expression);
            break;
         default:
            TypeBinding requiredGenericCast = this.getGenericCast(this.otherBindings == null ? 0 : this.otherBindings.length);
            if (requiredGenericCast != null) {
               codeStream.checkcast(requiredGenericCast);
            }

            codeStream.generateImplicitConversion(this.implicitConversion);
            if (expression == IntLiteral.One) {
               codeStream.generateConstant(expression.constant, this.implicitConversion);
            } else {
               expression.generateCode(currentScope, codeStream, true);
            }

            codeStream.sendOperator(operator, operationTypeID);
            codeStream.generateImplicitConversion(assignmentImplicitConversion);
      }

      this.fieldStore(currentScope, codeStream, lastFieldBinding, this.syntheticWriteAccessor, this.getFinalReceiverType(), false, valueRequired);
   }

   @Override
   public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
      FieldBinding lastFieldBinding = this.generateReadSequence(currentScope, codeStream);
      this.reportOnlyUselesslyReadPrivateField(currentScope, lastFieldBinding, valueRequired);
      boolean isFirst = lastFieldBinding == this.binding
         && (this.indexOfFirstFieldBinding == 1 || TypeBinding.equalsEquals(lastFieldBinding.declaringClass, currentScope.enclosingReceiverType()))
         && this.otherBindings == null;
      TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, lastFieldBinding, this.getFinalReceiverType(), isFirst);
      SyntheticMethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
      if (lastFieldBinding.isStatic()) {
         if (accessor == null) {
            codeStream.fieldAccess((byte)-78, lastFieldBinding, constantPoolDeclaringClass);
         } else {
            codeStream.invoke((byte)-72, accessor, constantPoolDeclaringClass);
         }
      } else {
         codeStream.dup();
         if (accessor == null) {
            codeStream.fieldAccess((byte)-76, lastFieldBinding, null);
         } else {
            codeStream.invoke((byte)-72, accessor, null);
         }
      }

      TypeBinding requiredGenericCast = this.getGenericCast(this.otherBindings == null ? 0 : this.otherBindings.length);
      TypeBinding operandType;
      if (requiredGenericCast != null) {
         codeStream.checkcast(requiredGenericCast);
         operandType = requiredGenericCast;
      } else {
         operandType = lastFieldBinding.type;
      }

      if (valueRequired) {
         if (lastFieldBinding.isStatic()) {
            switch(operandType.id) {
               case 7:
               case 8:
                  codeStream.dup2();
                  break;
               default:
                  codeStream.dup();
            }
         } else {
            switch(operandType.id) {
               case 7:
               case 8:
                  codeStream.dup2_x1();
                  break;
               default:
                  codeStream.dup_x1();
            }
         }
      }

      codeStream.generateImplicitConversion(this.implicitConversion);
      codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
      codeStream.sendOperator(postIncrement.operator, this.implicitConversion & 15);
      codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);
      this.fieldStore(currentScope, codeStream, lastFieldBinding, this.syntheticWriteAccessor, this.getFinalReceiverType(), false, false);
   }

   public FieldBinding generateReadSequence(BlockScope currentScope, CodeStream codeStream) {
      int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;
      boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
      boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= 3145728L;
      FieldBinding lastFieldBinding;
      TypeBinding lastGenericCast;
      TypeBinding lastReceiverType;
      switch(this.bits & 7) {
         case 1:
            lastFieldBinding = ((FieldBinding)this.binding).original();
            lastGenericCast = this.genericCast;
            lastReceiverType = this.actualReceiverType;
            if (lastFieldBinding.constant() == Constant.NotAConstant && (needValue && !lastFieldBinding.isStatic() || lastGenericCast != null)) {
               int pc = codeStream.position;
               if ((this.bits & 8160) != 0) {
                  ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & 8160) >> 5);
                  Object[] emulationPath = currentScope.getEmulationPath(targetType, true, false);
                  codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
               } else {
                  this.generateReceiver(codeStream);
               }

               codeStream.recordPositionsFrom(pc, this.sourceStart);
            }
            break;
         case 2:
            lastFieldBinding = null;
            lastGenericCast = null;
            LocalVariableBinding localBinding = (LocalVariableBinding)this.binding;
            lastReceiverType = localBinding.type;
            if (needValue) {
               Constant localConstant = localBinding.constant();
               if (localConstant != Constant.NotAConstant) {
                  codeStream.generateConstant(localConstant, 0);
               } else if ((this.bits & 524288) != 0) {
                  this.checkEffectiveFinality(localBinding, currentScope);
                  VariableBinding[] path = currentScope.getEmulationPath(localBinding);
                  codeStream.generateOuterAccess(path, this, localBinding, currentScope);
               } else {
                  codeStream.load(localBinding);
               }
            }
            break;
         default:
            return null;
      }

      int positionsLength = this.sourcePositions.length;
      FieldBinding initialFieldBinding = lastFieldBinding;
      if (this.otherBindings != null) {
         for(int i = 0; i < otherBindingsCount; ++i) {
            int pc = codeStream.position;
            FieldBinding nextField = this.otherBindings[i].original();
            TypeBinding nextGenericCast = this.otherGenericCasts == null ? null : this.otherGenericCasts[i];
            if (lastFieldBinding != null) {
               needValue = !nextField.isStatic();
               Constant fieldConstant = lastFieldBinding.constant();
               if (fieldConstant != Constant.NotAConstant) {
                  if (i > 0 && !lastFieldBinding.isStatic()) {
                     codeStream.invokeObjectGetClass();
                     codeStream.pop();
                  }

                  if (needValue) {
                     codeStream.generateConstant(fieldConstant, 0);
                  }
               } else {
                  if (!needValue && (i <= 0 || !complyTo14) && lastGenericCast == null) {
                     if (lastFieldBinding == initialFieldBinding) {
                        if (lastFieldBinding.isStatic() && TypeBinding.notEquals(initialFieldBinding.declaringClass, this.actualReceiverType.erasure())) {
                           MethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[i];
                           if (accessor != null) {
                              codeStream.invoke((byte)-72, accessor, null);
                           } else {
                              TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
                                 currentScope, lastFieldBinding, lastReceiverType, i == 0 && this.indexOfFirstFieldBinding == 1
                              );
                              codeStream.fieldAccess((byte)-78, lastFieldBinding, constantPoolDeclaringClass);
                           }

                           codeStream.pop();
                        }
                     } else if (!lastFieldBinding.isStatic()) {
                        codeStream.invokeObjectGetClass();
                        codeStream.pop();
                     }

                     lastReceiverType = lastFieldBinding.type;
                  } else {
                     MethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[i];
                     if (accessor == null) {
                        TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
                           currentScope, lastFieldBinding, lastReceiverType, i == 0 && this.indexOfFirstFieldBinding == 1
                        );
                        if (lastFieldBinding.isStatic()) {
                           codeStream.fieldAccess((byte)-78, lastFieldBinding, constantPoolDeclaringClass);
                        } else {
                           codeStream.fieldAccess((byte)-76, lastFieldBinding, constantPoolDeclaringClass);
                        }
                     } else {
                        codeStream.invoke((byte)-72, accessor, null);
                     }

                     if (lastGenericCast != null) {
                        codeStream.checkcast(lastGenericCast);
                        lastReceiverType = lastGenericCast;
                     } else {
                        lastReceiverType = lastFieldBinding.type;
                     }

                     if (!needValue) {
                        codeStream.pop();
                     }
                  }

                  if (positionsLength - otherBindingsCount + i - 1 >= 0) {
                     int fieldPosition = (int)(this.sourcePositions[positionsLength - otherBindingsCount + i - 1] >>> 32);
                     codeStream.recordPositionsFrom(pc, fieldPosition);
                  }
               }
            }

            lastFieldBinding = nextField;
            lastGenericCast = nextGenericCast;
         }
      }

      return lastFieldBinding;
   }

   public void generateReceiver(CodeStream codeStream) {
      codeStream.aload_0();
   }

   @Override
   public TypeBinding[] genericTypeArguments() {
      return null;
   }

   protected FieldBinding getCodegenBinding(int index) {
      return index == 0 ? ((FieldBinding)this.binding).original() : this.otherBindings[index - 1].original();
   }

   protected TypeBinding getFinalReceiverType() {
      int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;
      switch(otherBindingsCount) {
         case 0:
            return this.actualReceiverType;
         case 1:
            return this.genericCast != null ? this.genericCast : ((VariableBinding)this.binding).type;
         default:
            TypeBinding previousGenericCast = this.otherGenericCasts == null ? null : this.otherGenericCasts[otherBindingsCount - 2];
            return previousGenericCast != null ? previousGenericCast : this.otherBindings[otherBindingsCount - 2].type;
      }
   }

   protected TypeBinding getGenericCast(int index) {
      if (index == 0) {
         return this.genericCast;
      } else {
         return this.otherGenericCasts == null ? null : this.otherGenericCasts[index - 1];
      }
   }

   public TypeBinding getOtherFieldBindings(BlockScope scope) {
      int length = this.tokens.length;
      FieldBinding field = (this.bits & 1) != 0 ? (FieldBinding)this.binding : null;
      TypeBinding type = ((VariableBinding)this.binding).type;
      int index = this.indexOfFirstFieldBinding;
      if (index == length) {
         this.constant = ((FieldBinding)this.binding).constant(scope);
         return type != null && (this.bits & 8192) == 0 ? type.capture(scope, this.sourceStart, this.sourceEnd) : type;
      } else {
         int otherBindingsLength = length - index;
         this.otherBindings = new FieldBinding[otherBindingsLength];
         this.otherDepths = new int[otherBindingsLength];
         this.constant = ((VariableBinding)this.binding).constant(scope);

         int firstDepth;
         for(firstDepth = (this.bits & 8160) >> 5; index < length; ++index) {
            char[] token = this.tokens[index];
            if (type == null) {
               return null;
            }

            this.bits &= -8161;
            FieldBinding previousField = field;
            field = scope.getField(type.capture(scope, (int)(this.sourcePositions[index] >>> 32), (int)this.sourcePositions[index]), token, this);
            int place = index - this.indexOfFirstFieldBinding;
            this.otherBindings[place] = field;
            this.otherDepths[place] = (this.bits & 8160) >> 5;
            if (!field.isValidBinding()) {
               this.constant = Constant.NotAConstant;
               scope.problemReporter().invalidField(this, field, index, type);
               this.setDepth(firstDepth);
               return null;
            }

            if (previousField != null) {
               TypeBinding declaringClass = type.getErasureCompatibleType(field.declaringClass);
               FieldBinding originalBinding = previousField.original();
               if (TypeBinding.notEquals(declaringClass, type) || originalBinding.type.leafComponentType().isTypeVariable()) {
                  this.setGenericCast(index - 1, originalBinding.type.genericCast(declaringClass));
               }
            }

            if (this.isFieldUseDeprecated(field, scope, index + 1 == length ? this.bits : 0)) {
               scope.problemReporter().deprecatedField(field, this);
            }

            if (this.constant != Constant.NotAConstant) {
               this.constant = field.constant(scope);
            }

            if (field.isStatic()) {
               if ((field.modifiers & 16384) != 0) {
                  ReferenceBinding declaringClass = field.original().declaringClass;
                  MethodScope methodScope = scope.methodScope();
                  SourceTypeBinding sourceType = methodScope.enclosingSourceType();
                  if ((this.bits & 8192) == 0
                     && TypeBinding.equalsEquals(sourceType, declaringClass)
                     && methodScope.lastVisibleFieldID >= 0
                     && field.id >= methodScope.lastVisibleFieldID
                     && (!field.isStatic() || methodScope.isStatic)) {
                     scope.problemReporter().forwardReference(this, index, field);
                  }

                  if ((TypeBinding.equalsEquals(sourceType, declaringClass) || TypeBinding.equalsEquals(sourceType.superclass, declaringClass))
                     && field.constant(scope) == Constant.NotAConstant
                     && !methodScope.isStatic
                     && methodScope.isInsideInitializerOrConstructor()) {
                     scope.problemReporter().enumStaticFieldUsedDuringInitialization(field, this);
                  }
               }

               scope.problemReporter().nonStaticAccessToStaticField(this, field, index);
               if (TypeBinding.notEquals(field.declaringClass, type)) {
                  scope.problemReporter().indirectAccessToStaticField(this, field);
               }
            }

            type = field.type;
         }

         this.setDepth(firstDepth);
         type = this.otherBindings[otherBindingsLength - 1].type;
         return type != null && (this.bits & 8192) == 0 ? type.capture(scope, this.sourceStart, this.sourceEnd) : type;
      }
   }

   @Override
   public boolean isEquivalent(Reference reference) {
      if (reference instanceof FieldReference) {
         return reference.isEquivalent(this);
      } else if (!(reference instanceof QualifiedNameReference)) {
         return false;
      } else {
         QualifiedNameReference qualifiedReference = (QualifiedNameReference)reference;
         if (this.tokens.length != qualifiedReference.tokens.length) {
            return false;
         } else if (this.binding != qualifiedReference.binding) {
            return false;
         } else {
            if (this.otherBindings != null) {
               if (qualifiedReference.otherBindings == null) {
                  return false;
               }

               int len = this.otherBindings.length;
               if (len != qualifiedReference.otherBindings.length) {
                  return false;
               }

               for(int i = 0; i < len; ++i) {
                  if (this.otherBindings[i] != qualifiedReference.otherBindings[i]) {
                     return false;
                  }
               }
            } else if (qualifiedReference.otherBindings != null) {
               return false;
            }

            return true;
         }
      }
   }

   public boolean isFieldAccess() {
      if (this.otherBindings != null) {
         return true;
      } else {
         return (this.bits & 7) == 1;
      }
   }

   @Override
   public FieldBinding lastFieldBinding() {
      if (this.otherBindings != null) {
         return this.otherBindings[this.otherBindings.length - 1];
      } else {
         return this.binding != null && (this.bits & 7) == 1 ? (FieldBinding)this.binding : null;
      }
   }

   public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
      if (((this.bits & 8160) != 0 || (this.bits & 524288) != 0) && this.constant == Constant.NotAConstant) {
         if ((this.bits & 7) == 2) {
            LocalVariableBinding localVariableBinding = (LocalVariableBinding)this.binding;
            if (localVariableBinding != null) {
               if ((localVariableBinding.tagBits & 256L) != 0L) {
                  return;
               }

               switch(localVariableBinding.useFlag) {
                  case 1:
                  case 2:
                     currentScope.emulateOuterAccess(localVariableBinding);
               }
            }
         }
      }
   }

   public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FieldBinding fieldBinding, int index, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         if (fieldBinding.constant(currentScope) == Constant.NotAConstant) {
            if (fieldBinding.isPrivate()) {
               FieldBinding codegenField = this.getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
               ReferenceBinding declaringClass = codegenField.declaringClass;
               if (TypeBinding.notEquals(declaringClass, currentScope.enclosingSourceType())) {
                  this.setSyntheticAccessor(fieldBinding, index, ((SourceTypeBinding)declaringClass).addSyntheticMethod(codegenField, index >= 0, false));
                  currentScope.problemReporter().needToEmulateFieldAccess(codegenField, this, index >= 0);
                  return;
               }
            } else if (fieldBinding.isProtected()) {
               int depth = index != 0 && (index >= 0 || this.otherDepths != null)
                  ? this.otherDepths[index < 0 ? this.otherDepths.length - 1 : index - 1]
                  : (this.bits & 8160) >> 5;
               if (depth > 0 && fieldBinding.declaringClass.getPackage() != currentScope.enclosingSourceType().getPackage()) {
                  FieldBinding codegenField = this.getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
                  this.setSyntheticAccessor(
                     fieldBinding,
                     index,
                     ((SourceTypeBinding)currentScope.enclosingSourceType().enclosingTypeAt(depth)).addSyntheticMethod(codegenField, index >= 0, false)
                  );
                  currentScope.problemReporter().needToEmulateFieldAccess(codegenField, this, index >= 0);
                  return;
               }
            }
         }
      }
   }

   @Override
   public Constant optimizedBooleanConstant() {
      switch(this.resolvedType.id) {
         case 5:
         case 33:
            if (this.constant != Constant.NotAConstant) {
               return this.constant;
            } else {
               switch(this.bits & 7) {
                  case 1:
                     if (this.otherBindings == null) {
                        return ((FieldBinding)this.binding).constant();
                     }
                  case 2:
                     return this.otherBindings[this.otherBindings.length - 1].constant();
               }
            }
         default:
            return Constant.NotAConstant;
      }
   }

   @Override
   public TypeBinding postConversionType(Scope scope) {
      TypeBinding convertedType = this.resolvedType;
      TypeBinding requiredGenericCast = this.getGenericCast(this.otherBindings == null ? 0 : this.otherBindings.length);
      if (requiredGenericCast != null) {
         convertedType = requiredGenericCast;
      }

      int runtimeType = (this.implicitConversion & 0xFF) >> 4;
      switch(runtimeType) {
         case 2:
            convertedType = TypeBinding.CHAR;
            break;
         case 3:
            convertedType = TypeBinding.BYTE;
            break;
         case 4:
            convertedType = TypeBinding.SHORT;
            break;
         case 5:
            convertedType = TypeBinding.BOOLEAN;
         case 6:
         default:
            break;
         case 7:
            convertedType = TypeBinding.LONG;
            break;
         case 8:
            convertedType = TypeBinding.DOUBLE;
            break;
         case 9:
            convertedType = TypeBinding.FLOAT;
            break;
         case 10:
            convertedType = TypeBinding.INT;
      }

      if ((this.implicitConversion & 512) != 0) {
         convertedType = scope.environment().computeBoxingType(convertedType);
      }

      return convertedType;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      for(int i = 0; i < this.tokens.length; ++i) {
         if (i > 0) {
            output.append('.');
         }

         output.append(this.tokens[i]);
      }

      return output;
   }

   public TypeBinding reportError(BlockScope scope) {
      if (this.binding instanceof ProblemFieldBinding) {
         scope.problemReporter().invalidField(this, (FieldBinding)this.binding);
      } else if (!(this.binding instanceof ProblemReferenceBinding) && !(this.binding instanceof MissingTypeBinding)) {
         scope.problemReporter().unresolvableReference(this, this.binding);
      } else {
         scope.problemReporter().invalidType(this, (TypeBinding)this.binding);
      }

      return null;
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      this.actualReceiverType = scope.enclosingReceiverType();
      this.constant = Constant.NotAConstant;
      if ((this.binding = scope.getBinding(this.tokens, this.bits & 7, this, true)).isValidBinding()) {
         switch(this.bits & 7) {
            case 3:
            case 7:
               if (this.binding instanceof LocalVariableBinding) {
                  this.bits &= -8;
                  this.bits |= 2;
                  LocalVariableBinding local = (LocalVariableBinding)this.binding;
                  if (!local.isFinal() && (this.bits & 524288) != 0 && scope.compilerOptions().sourceLevel < 3407872L) {
                     scope.problemReporter().cannotReferToNonFinalOuterLocal((LocalVariableBinding)this.binding, this);
                  }

                  if (local.type != null && (local.type.tagBits & 128L) != 0L) {
                     return null;
                  }

                  this.resolvedType = this.getOtherFieldBindings(scope);
                  if (this.resolvedType != null && (this.resolvedType.tagBits & 128L) != 0L) {
                     FieldBinding lastField = this.otherBindings[this.otherBindings.length - 1];
                     scope.problemReporter()
                        .invalidField(
                           this,
                           new ProblemFieldBinding(lastField.declaringClass, lastField.name, 1),
                           this.tokens.length,
                           this.resolvedType.leafComponentType()
                        );
                     return null;
                  }

                  return this.resolvedType;
               }

               if (this.binding instanceof FieldBinding) {
                  this.bits &= -8;
                  this.bits |= 1;
                  FieldBinding fieldBinding = (FieldBinding)this.binding;
                  MethodScope methodScope = scope.methodScope();
                  ReferenceBinding declaringClass = fieldBinding.original().declaringClass;
                  SourceTypeBinding sourceType = methodScope.enclosingSourceType();
                  if ((this.indexOfFirstFieldBinding == 1 || (fieldBinding.modifiers & 16384) != 0 || !fieldBinding.isFinal() && declaringClass.isEnum())
                     && TypeBinding.equalsEquals(sourceType, declaringClass)
                     && methodScope.lastVisibleFieldID >= 0
                     && fieldBinding.id >= methodScope.lastVisibleFieldID
                     && (!fieldBinding.isStatic() || methodScope.isStatic)
                     && (!methodScope.insideTypeAnnotation || fieldBinding.id != methodScope.lastVisibleFieldID)) {
                     scope.problemReporter().forwardReference(this, this.indexOfFirstFieldBinding - 1, fieldBinding);
                  }

                  if (this.isFieldUseDeprecated(fieldBinding, scope, this.indexOfFirstFieldBinding == this.tokens.length ? this.bits : 0)) {
                     scope.problemReporter().deprecatedField(fieldBinding, this);
                  }

                  if (fieldBinding.isStatic()) {
                     if (declaringClass.isEnum()
                        && (TypeBinding.equalsEquals(sourceType, declaringClass) || TypeBinding.equalsEquals(sourceType.superclass, declaringClass))
                        && fieldBinding.constant(scope) == Constant.NotAConstant
                        && !methodScope.isStatic
                        && methodScope.isInsideInitializerOrConstructor()) {
                        scope.problemReporter().enumStaticFieldUsedDuringInitialization(fieldBinding, this);
                     }

                     if (this.indexOfFirstFieldBinding > 1
                        && TypeBinding.notEquals(fieldBinding.declaringClass, this.actualReceiverType)
                        && fieldBinding.declaringClass.canBeSeenBy(scope)) {
                        scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
                     }
                  } else {
                     boolean inStaticContext = scope.methodScope().isStatic;
                     if (this.indexOfFirstFieldBinding == 1) {
                        if (scope.compilerOptions().getSeverity(4194304) != 256) {
                           scope.problemReporter().unqualifiedFieldAccess(this, fieldBinding);
                        }

                        if (!inStaticContext) {
                           scope.tagAsAccessingEnclosingInstanceStateOf(fieldBinding.declaringClass, false);
                        }
                     }

                     if (this.indexOfFirstFieldBinding > 1 || inStaticContext) {
                        scope.problemReporter().staticFieldAccessToNonStaticVariable(this, fieldBinding);
                        return null;
                     }
                  }

                  this.resolvedType = this.getOtherFieldBindings(scope);
                  if (this.resolvedType != null && (this.resolvedType.tagBits & 128L) != 0L) {
                     FieldBinding lastField = this.indexOfFirstFieldBinding == this.tokens.length
                        ? (FieldBinding)this.binding
                        : this.otherBindings[this.otherBindings.length - 1];
                     scope.problemReporter()
                        .invalidField(
                           this,
                           new ProblemFieldBinding(lastField.declaringClass, lastField.name, 1),
                           this.tokens.length,
                           this.resolvedType.leafComponentType()
                        );
                     return null;
                  }

                  return this.resolvedType;
               }

               this.bits &= -8;
               this.bits |= 4;
            case 4:
               TypeBinding type = (TypeBinding)this.binding;
               type = scope.environment().convertToRawType(type, false);
               return this.resolvedType = type;
            case 5:
            case 6:
         }
      }

      return this.resolvedType = this.reportError(scope);
   }

   @Override
   public void setFieldIndex(int index) {
      this.indexOfFirstFieldBinding = index;
   }

   protected void setGenericCast(int index, TypeBinding someGenericCast) {
      if (someGenericCast != null) {
         if (index == 0) {
            this.genericCast = someGenericCast;
         } else {
            if (this.otherGenericCasts == null) {
               this.otherGenericCasts = new TypeBinding[this.otherBindings.length];
            }

            this.otherGenericCasts[index - 1] = someGenericCast;
         }
      }
   }

   protected void setSyntheticAccessor(FieldBinding fieldBinding, int index, SyntheticMethodBinding syntheticAccessor) {
      if (index < 0) {
         this.syntheticWriteAccessor = syntheticAccessor;
      } else {
         if (this.syntheticReadAccessors == null) {
            this.syntheticReadAccessors = new SyntheticMethodBinding[this.otherBindings == null ? 1 : this.otherBindings.length + 1];
         }

         this.syntheticReadAccessors[index] = syntheticAccessor;
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }

   @Override
   public String unboundReferenceErrorName() {
      return new String(this.tokens[0]);
   }

   @Override
   public char[][] getName() {
      return this.tokens;
   }

   @Override
   public VariableBinding nullAnnotatedVariableBinding(boolean supportTypeAnnotations) {
      if (this.binding != null && this.isFieldAccess()) {
         FieldBinding fieldBinding;
         if (this.otherBindings == null) {
            fieldBinding = (FieldBinding)this.binding;
         } else {
            fieldBinding = this.otherBindings[this.otherBindings.length - 1];
         }

         if (supportTypeAnnotations || fieldBinding.isNullable() || fieldBinding.isNonNull()) {
            return fieldBinding;
         }
      }

      return null;
   }
}
