package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class FieldReference extends Reference implements InvocationSite {
   public static final int READ = 0;
   public static final int WRITE = 1;
   public Expression receiver;
   public char[] token;
   public FieldBinding binding;
   public MethodBinding[] syntheticAccessors;
   public long nameSourcePosition;
   public TypeBinding actualReceiverType;
   public TypeBinding genericCast;

   public FieldReference(char[] source, long pos) {
      this.token = source;
      this.nameSourcePosition = pos;
      this.sourceStart = (int)(pos >>> 32);
      this.sourceEnd = (int)(pos & 4294967295L);
      this.bits |= 1;
   }

   @Override
   public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
      if (isCompound) {
         if (this.binding.isBlankFinal() && this.receiver.isThis() && currentScope.needBlankFinalFieldInitializationCheck(this.binding)) {
            FlowInfo fieldInits = flowContext.getInitsForFinalBlankInitializationCheck(this.binding.declaringClass.original(), flowInfo);
            if (!fieldInits.isDefinitelyAssigned(this.binding)) {
               currentScope.problemReporter().uninitializedBlankFinalField(this.binding, this);
            }
         }

         this.manageSyntheticAccessIfNecessary(currentScope, flowInfo, true);
      }

      FlowInfo var7 = this.receiver.analyseCode(currentScope, flowContext, flowInfo, !this.binding.isStatic()).unconditionalInits();
      if (assignment.expression != null) {
         var7 = assignment.expression.analyseCode(currentScope, flowContext, var7).unconditionalInits();
      }

      this.manageSyntheticAccessIfNecessary(currentScope, var7, false);
      if (this.binding.isFinal()) {
         if (this.binding.isBlankFinal()
            && !isCompound
            && this.receiver.isThis()
            && !(this.receiver instanceof QualifiedThisReference)
            && (this.receiver.bits & 534773760) == 0
            && currentScope.allowBlankFinalFieldAssignment(this.binding)) {
            if (var7.isPotentiallyAssigned(this.binding)) {
               currentScope.problemReporter().duplicateInitializationOfBlankFinalField(this.binding, this);
            } else {
               flowContext.recordSettingFinal(this.binding, this, var7);
            }

            var7.markAsDefinitelyAssigned(this.binding);
         } else {
            currentScope.problemReporter().cannotAssignToFinalField(this.binding, this);
         }
      } else if ((this.binding.isNonNull() || this.binding.type.isTypeVariable())
         && !isCompound
         && this.receiver.isThis()
         && !(this.receiver instanceof QualifiedThisReference)
         && TypeBinding.equalsEquals(this.receiver.resolvedType, this.binding.declaringClass)
         && (this.receiver.bits & 534773760) == 0) {
         var7.markAsDefinitelyAssigned(this.binding);
      }

      return var7;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      return this.analyseCode(currentScope, flowContext, flowInfo, true);
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
      boolean nonStatic = !this.binding.isStatic();
      this.receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic);
      if (nonStatic) {
         this.receiver.checkNPE(currentScope, flowContext, flowInfo, 1);
      }

      if (valueRequired || currentScope.compilerOptions().complianceLevel >= 3145728L) {
         this.manageSyntheticAccessIfNecessary(currentScope, flowInfo, true);
      }

      if (currentScope.compilerOptions().complianceLevel >= 3342336L) {
         FieldBinding fieldBinding = this.binding;
         if (this.receiver.isThis() && fieldBinding.isBlankFinal() && currentScope.needBlankFinalFieldInitializationCheck(fieldBinding)) {
            FlowInfo fieldInits = flowContext.getInitsForFinalBlankInitializationCheck(fieldBinding.declaringClass.original(), flowInfo);
            if (!fieldInits.isDefinitelyAssigned(fieldBinding)) {
               currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
            }
         }
      }

      return flowInfo;
   }

   @Override
   public boolean checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, int ttlForFieldCheck) {
      return flowContext.isNullcheckedFieldAccess(this)
         ? true
         : this.checkNullableFieldDereference(scope, this.binding, this.nameSourcePosition, flowContext, ttlForFieldCheck);
   }

   @Override
   public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
      if (runtimeTimeType != null && compileTimeType != null) {
         if (this.binding != null && this.binding.isValidBinding()) {
            FieldBinding originalBinding = this.binding.original();
            TypeBinding originalType = originalBinding.type;
            if (originalType.leafComponentType().isTypeVariable()) {
               TypeBinding targetType = !compileTimeType.isBaseType() && runtimeTimeType.isBaseType() ? compileTimeType : runtimeTimeType;
               this.genericCast = originalBinding.type.genericCast(targetType);
               if (this.genericCast instanceof ReferenceBinding) {
                  ReferenceBinding referenceCast = (ReferenceBinding)this.genericCast;
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
   public FieldBinding fieldBinding() {
      return this.binding;
   }

   @Override
   public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
      int pc = codeStream.position;
      FieldBinding codegenBinding = this.binding.original();
      this.receiver.generateCode(currentScope, codeStream, !codegenBinding.isStatic());
      codeStream.recordPositionsFrom(pc, this.sourceStart);
      assignment.expression.generateCode(currentScope, codeStream, true);
      this.fieldStore(
         currentScope,
         codeStream,
         codegenBinding,
         this.syntheticAccessors == null ? null : this.syntheticAccessors[1],
         this.actualReceiverType,
         this.receiver.isImplicitThis(),
         valueRequired
      );
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

         codeStream.recordPositionsFrom(pc, this.sourceStart);
      } else {
         FieldBinding codegenBinding = this.binding.original();
         boolean isStatic = codegenBinding.isStatic();
         boolean isThisReceiver = this.receiver instanceof ThisReference;
         Constant fieldConstant = codegenBinding.constant();
         if (fieldConstant != Constant.NotAConstant) {
            if (!isThisReceiver) {
               this.receiver.generateCode(currentScope, codeStream, !isStatic);
               if (!isStatic) {
                  codeStream.invokeObjectGetClass();
                  codeStream.pop();
               }
            }

            if (valueRequired) {
               codeStream.generateConstant(fieldConstant, this.implicitConversion);
            }

            codeStream.recordPositionsFrom(pc, this.sourceStart);
         } else {
            if (valueRequired
               || !isThisReceiver && currentScope.compilerOptions().complianceLevel >= 3145728L
               || (this.implicitConversion & 1024) != 0
               || this.genericCast != null) {
               this.receiver.generateCode(currentScope, codeStream, !isStatic);
               if ((this.bits & 262144) != 0) {
                  codeStream.checkcast(this.actualReceiverType);
               }

               pc = codeStream.position;
               if (codegenBinding.declaringClass == null) {
                  codeStream.arraylength();
                  if (valueRequired) {
                     codeStream.generateImplicitConversion(this.implicitConversion);
                  } else {
                     codeStream.pop();
                  }
               } else {
                  if (this.syntheticAccessors != null && this.syntheticAccessors[0] != null) {
                     codeStream.invoke((byte)-72, this.syntheticAccessors[0], null);
                  } else {
                     TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
                        currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
                     );
                     if (isStatic) {
                        codeStream.fieldAccess((byte)-78, codegenBinding, constantPoolDeclaringClass);
                     } else {
                        codeStream.fieldAccess((byte)-76, codegenBinding, constantPoolDeclaringClass);
                     }
                  }

                  if (this.genericCast != null) {
                     codeStream.checkcast(this.genericCast);
                  }

                  if (valueRequired) {
                     codeStream.generateImplicitConversion(this.implicitConversion);
                  } else {
                     boolean isUnboxing = (this.implicitConversion & 1024) != 0;
                     if (isUnboxing) {
                        codeStream.generateImplicitConversion(this.implicitConversion);
                     }

                     switch(isUnboxing ? this.postConversionType(currentScope).id : codegenBinding.type.id) {
                        case 7:
                        case 8:
                           codeStream.pop2();
                           break;
                        default:
                           codeStream.pop();
                     }
                  }
               }
            } else if (isThisReceiver) {
               if (isStatic && TypeBinding.notEquals(this.binding.original().declaringClass, this.actualReceiverType.erasure())) {
                  MethodBinding accessor = this.syntheticAccessors == null ? null : this.syntheticAccessors[0];
                  if (accessor == null) {
                     TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
                        currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
                     );
                     codeStream.fieldAccess((byte)-78, codegenBinding, constantPoolDeclaringClass);
                  } else {
                     codeStream.invoke((byte)-72, accessor, null);
                  }

                  switch(codegenBinding.type.id) {
                     case 7:
                     case 8:
                        codeStream.pop2();
                        break;
                     default:
                        codeStream.pop();
                  }
               }
            } else {
               this.receiver.generateCode(currentScope, codeStream, !isStatic);
               if (!isStatic) {
                  codeStream.invokeObjectGetClass();
                  codeStream.pop();
               }
            }

            codeStream.recordPositionsFrom(pc, this.sourceEnd);
         }
      }
   }

   @Override
   public void generateCompoundAssignment(
      BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired
   ) {
      this.reportOnlyUselesslyReadPrivateField(currentScope, this.binding, valueRequired);
      FieldBinding codegenBinding = this.binding.original();
      boolean isStatic;
      this.receiver.generateCode(currentScope, codeStream, !(isStatic = codegenBinding.isStatic()));
      if (isStatic) {
         if (this.syntheticAccessors != null && this.syntheticAccessors[0] != null) {
            codeStream.invoke((byte)-72, this.syntheticAccessors[0], null);
         } else {
            TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
               currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
            );
            codeStream.fieldAccess((byte)-78, codegenBinding, constantPoolDeclaringClass);
         }
      } else {
         codeStream.dup();
         if (this.syntheticAccessors != null && this.syntheticAccessors[0] != null) {
            codeStream.invoke((byte)-72, this.syntheticAccessors[0], null);
         } else {
            TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
               currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
            );
            codeStream.fieldAccess((byte)-76, codegenBinding, constantPoolDeclaringClass);
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
            if (this.genericCast != null) {
               codeStream.checkcast(this.genericCast);
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

      this.fieldStore(
         currentScope,
         codeStream,
         codegenBinding,
         this.syntheticAccessors == null ? null : this.syntheticAccessors[1],
         this.actualReceiverType,
         this.receiver.isImplicitThis(),
         valueRequired
      );
   }

   @Override
   public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
      this.reportOnlyUselesslyReadPrivateField(currentScope, this.binding, valueRequired);
      FieldBinding codegenBinding = this.binding.original();
      boolean isStatic;
      this.receiver.generateCode(currentScope, codeStream, !(isStatic = codegenBinding.isStatic()));
      if (isStatic) {
         if (this.syntheticAccessors != null && this.syntheticAccessors[0] != null) {
            codeStream.invoke((byte)-72, this.syntheticAccessors[0], null);
         } else {
            TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
               currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
            );
            codeStream.fieldAccess((byte)-78, codegenBinding, constantPoolDeclaringClass);
         }
      } else {
         codeStream.dup();
         if (this.syntheticAccessors != null && this.syntheticAccessors[0] != null) {
            codeStream.invoke((byte)-72, this.syntheticAccessors[0], null);
         } else {
            TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(
               currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis()
            );
            codeStream.fieldAccess((byte)-76, codegenBinding, constantPoolDeclaringClass);
         }
      }

      TypeBinding operandType;
      if (this.genericCast != null) {
         codeStream.checkcast(this.genericCast);
         operandType = this.genericCast;
      } else {
         operandType = codegenBinding.type;
      }

      if (valueRequired) {
         if (isStatic) {
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
      this.fieldStore(
         currentScope,
         codeStream,
         codegenBinding,
         this.syntheticAccessors == null ? null : this.syntheticAccessors[1],
         this.actualReceiverType,
         this.receiver.isImplicitThis(),
         false
      );
   }

   @Override
   public TypeBinding[] genericTypeArguments() {
      return null;
   }

   @Override
   public InferenceContext18 freshInferenceContext(Scope scope) {
      return null;
   }

   @Override
   public boolean isEquivalent(Reference reference) {
      if (this.receiver.isThis() && !(this.receiver instanceof QualifiedThisReference)) {
         char[] otherToken = null;
         if (reference instanceof SingleNameReference) {
            otherToken = ((SingleNameReference)reference).token;
         } else if (reference instanceof FieldReference) {
            FieldReference fr = (FieldReference)reference;
            if (fr.receiver.isThis() && !(fr.receiver instanceof QualifiedThisReference)) {
               otherToken = fr.token;
            }
         }

         return otherToken != null && CharOperation.equals(this.token, otherToken);
      } else {
         char[][] thisTokens = this.getThisFieldTokens(1);
         if (thisTokens == null) {
            return false;
         } else {
            char[][] otherTokens = null;
            if (reference instanceof FieldReference) {
               otherTokens = ((FieldReference)reference).getThisFieldTokens(1);
            } else if (reference instanceof QualifiedNameReference) {
               if (((QualifiedNameReference)reference).binding instanceof LocalVariableBinding) {
                  return false;
               }

               otherTokens = ((QualifiedNameReference)reference).tokens;
            }

            return CharOperation.equals(thisTokens, otherTokens);
         }
      }
   }

   private char[][] getThisFieldTokens(int nestingCount) {
      char[][] result = null;
      if (this.receiver.isThis() && !(this.receiver instanceof QualifiedThisReference)) {
         result = new char[nestingCount][];
         result[0] = this.token;
      } else if (this.receiver instanceof FieldReference) {
         result = ((FieldReference)this.receiver).getThisFieldTokens(nestingCount + 1);
         if (result != null) {
            result[result.length - nestingCount] = this.token;
         }
      }

      return result;
   }

   @Override
   public boolean isSuperAccess() {
      return this.receiver.isSuper();
   }

   @Override
   public boolean isQualifiedSuper() {
      return this.receiver.isQualifiedSuper();
   }

   @Override
   public boolean isTypeAccess() {
      return this.receiver != null && this.receiver.isTypeReference();
   }

   @Override
   public FieldBinding lastFieldBinding() {
      return this.binding;
   }

   public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {
      if ((flowInfo.tagBits & 1) == 0) {
         FieldBinding codegenBinding = this.binding.original();
         if (this.binding.isPrivate()) {
            if (TypeBinding.notEquals(currentScope.enclosingSourceType(), codegenBinding.declaringClass)
               && this.binding.constant(currentScope) == Constant.NotAConstant) {
               if (this.syntheticAccessors == null) {
                  this.syntheticAccessors = new MethodBinding[2];
               }

               this.syntheticAccessors[isReadAccess ? 0 : 1] = ((SourceTypeBinding)codegenBinding.declaringClass)
                  .addSyntheticMethod(codegenBinding, isReadAccess, false);
               currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
               return;
            }
         } else {
            if (this.receiver instanceof QualifiedSuperReference) {
               SourceTypeBinding destinationType = (SourceTypeBinding)((QualifiedSuperReference)this.receiver).currentCompatibleType;
               if (this.syntheticAccessors == null) {
                  this.syntheticAccessors = new MethodBinding[2];
               }

               this.syntheticAccessors[isReadAccess ? 0 : 1] = destinationType.addSyntheticMethod(codegenBinding, isReadAccess, this.isSuperAccess());
               currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
               return;
            }

            SourceTypeBinding enclosingSourceType;
            if (this.binding.isProtected()
               && (this.bits & 8160) != 0
               && this.binding.declaringClass.getPackage() != (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()) {
               SourceTypeBinding currentCompatibleType = (SourceTypeBinding)enclosingSourceType.enclosingTypeAt((this.bits & 8160) >> 5);
               if (this.syntheticAccessors == null) {
                  this.syntheticAccessors = new MethodBinding[2];
               }

               this.syntheticAccessors[isReadAccess ? 0 : 1] = currentCompatibleType.addSyntheticMethod(codegenBinding, isReadAccess, this.isSuperAccess());
               currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
               return;
            }
         }
      }
   }

   @Override
   public Constant optimizedBooleanConstant() {
      if (this.resolvedType == null) {
         return Constant.NotAConstant;
      } else {
         switch(this.resolvedType.id) {
            case 5:
            case 33:
               return this.constant != Constant.NotAConstant ? this.constant : this.binding.constant();
            default:
               return Constant.NotAConstant;
         }
      }
   }

   @Override
   public TypeBinding postConversionType(Scope scope) {
      TypeBinding convertedType = this.resolvedType;
      if (this.genericCast != null) {
         convertedType = this.genericCast;
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
      return this.receiver.printExpression(0, output).append('.').append(this.token);
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      boolean receiverCast = false;
      if (this.receiver instanceof CastExpression) {
         this.receiver.bits |= 32;
         receiverCast = true;
      }

      this.actualReceiverType = this.receiver.resolveType(scope);
      if (this.actualReceiverType == null) {
         this.constant = Constant.NotAConstant;
         return null;
      } else {
         if (receiverCast && TypeBinding.equalsEquals(((CastExpression)this.receiver).expression.resolvedType, this.actualReceiverType)) {
            scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);
         }

         FieldBinding fieldBinding = this.binding = scope.getField(this.actualReceiverType, this.token, this);
         if (!fieldBinding.isValidBinding()) {
            this.constant = Constant.NotAConstant;
            if (this.receiver.resolvedType instanceof ProblemReferenceBinding) {
               return null;
            }

            ReferenceBinding declaringClass = fieldBinding.declaringClass;
            boolean avoidSecondary = declaringClass != null && declaringClass.isAnonymousType() && declaringClass.superclass() instanceof MissingTypeBinding;
            if (!avoidSecondary) {
               scope.problemReporter().invalidField(this, this.actualReceiverType);
            }

            if (fieldBinding instanceof ProblemFieldBinding) {
               ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding)fieldBinding;
               FieldBinding closestMatch = problemFieldBinding.closestMatch;
               switch(problemFieldBinding.problemId()) {
                  case 2:
                  case 5:
                  case 6:
                  case 7:
                     if (closestMatch != null) {
                        fieldBinding = closestMatch;
                     }
                  case 3:
                  case 4:
               }
            }

            if (!fieldBinding.isValidBinding()) {
               return null;
            }
         }

         TypeBinding oldReceiverType = this.actualReceiverType;
         this.actualReceiverType = this.actualReceiverType.getErasureCompatibleType(fieldBinding.declaringClass);
         this.receiver.computeConversion(scope, this.actualReceiverType, this.actualReceiverType);
         if (TypeBinding.notEquals(this.actualReceiverType, oldReceiverType)
            && TypeBinding.notEquals(this.receiver.postConversionType(scope), this.actualReceiverType)) {
            this.bits |= 262144;
         }

         if (this.isFieldUseDeprecated(fieldBinding, scope, this.bits)) {
            scope.problemReporter().deprecatedField(fieldBinding, this);
         }

         boolean isImplicitThisRcv = this.receiver.isImplicitThis();
         this.constant = isImplicitThisRcv ? fieldBinding.constant(scope) : Constant.NotAConstant;
         if (fieldBinding.isStatic()) {
            if (!isImplicitThisRcv && (!(this.receiver instanceof NameReference) || (((NameReference)this.receiver).bits & 4) == 0)) {
               scope.problemReporter().nonStaticAccessToStaticField(this, fieldBinding);
            }

            ReferenceBinding declaringClass = this.binding.declaringClass;
            if (!isImplicitThisRcv && TypeBinding.notEquals(declaringClass, this.actualReceiverType) && declaringClass.canBeSeenBy(scope)) {
               scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
            }

            if (declaringClass.isEnum()) {
               MethodScope methodScope = scope.methodScope();
               SourceTypeBinding sourceType = scope.enclosingSourceType();
               if (this.constant == Constant.NotAConstant
                  && !methodScope.isStatic
                  && (TypeBinding.equalsEquals(sourceType, declaringClass) || TypeBinding.equalsEquals(sourceType.superclass, declaringClass))
                  && methodScope.isInsideInitializerOrConstructor()) {
                  scope.problemReporter().enumStaticFieldUsedDuringInitialization(this.binding, this);
               }
            }
         }

         TypeBinding fieldType = fieldBinding.type;
         if (fieldType != null) {
            if ((this.bits & 8192) == 0) {
               fieldType = fieldType.capture(scope, this.sourceStart, this.sourceEnd);
            }

            this.resolvedType = fieldType;
            if ((fieldType.tagBits & 128L) != 0L) {
               scope.problemReporter().invalidType(this, fieldType);
               return null;
            }
         }

         return fieldType;
      }
   }

   @Override
   public void setActualReceiverType(ReferenceBinding receiverType) {
      this.actualReceiverType = receiverType;
   }

   @Override
   public void setDepth(int depth) {
      this.bits &= -8161;
      if (depth > 0) {
         this.bits |= (depth & 0xFF) << 5;
      }
   }

   @Override
   public void setFieldIndex(int index) {
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         this.receiver.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public VariableBinding nullAnnotatedVariableBinding(boolean supportTypeAnnotations) {
      return this.binding == null || !supportTypeAnnotations && (this.binding.tagBits & 108086391056891904L) == 0L ? null : this.binding;
   }
}
