package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class AND_AND_Expression extends BinaryExpression {
   int rightInitStateIndex = -1;
   int mergedInitStateIndex = -1;

   public AND_AND_Expression(Expression left, Expression right, int operator) {
      super(left, right, operator);
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      Constant cst = this.left.optimizedBooleanConstant();
      boolean isLeftOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue();
      boolean isLeftOptimizedFalse = cst != Constant.NotAConstant && !cst.booleanValue();
      if (isLeftOptimizedTrue) {
         FlowInfo mergedInfo = this.left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
         mergedInfo = this.right.analyseCode(currentScope, flowContext, mergedInfo);
         this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
         return mergedInfo;
      } else {
         FlowInfo leftInfo = this.left.analyseCode(currentScope, flowContext, flowInfo);
         if ((flowContext.tagBits & 4) != 0) {
            flowContext.expireNullCheckedFieldInfo();
         }

         FlowInfo rightInfo = leftInfo.initsWhenTrue().unconditionalCopy();
         this.rightInitStateIndex = currentScope.methodScope().recordInitializationStates(rightInfo);
         int previousMode = rightInfo.reachMode();
         if (isLeftOptimizedFalse && (rightInfo.reachMode() & 3) == 0) {
            currentScope.problemReporter().fakeReachable(this.right);
            rightInfo.setReachMode(1);
         }

         rightInfo = this.right.analyseCode(currentScope, flowContext, rightInfo);
         if ((flowContext.tagBits & 4) != 0) {
            flowContext.expireNullCheckedFieldInfo();
         }

         this.left.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
         this.right.checkNPEbyUnboxing(currentScope, flowContext, leftInfo.initsWhenTrue());
         FlowInfo mergedInfo = FlowInfo.conditional(
            rightInfo.safeInitsWhenTrue(),
            leftInfo.initsWhenFalse().unconditionalInits().mergedWith(rightInfo.initsWhenFalse().setReachMode(previousMode).unconditionalInits())
         );
         this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
         return mergedInfo;
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
         Constant cst = this.right.constant;
         if (cst != Constant.NotAConstant) {
            if (cst.booleanValue()) {
               this.left.generateCode(currentScope, codeStream, valueRequired);
            } else {
               this.left.generateCode(currentScope, codeStream, false);
               if (valueRequired) {
                  codeStream.iconst_0();
               }
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            codeStream.generateImplicitConversion(this.implicitConversion);
            codeStream.recordPositionsFrom(pc, this.sourceStart);
         } else {
            BranchLabel falseLabel;
            boolean leftIsConst;
            boolean leftIsTrue;
            boolean rightIsConst;
            boolean rightIsTrue;
            label108: {
               falseLabel = new BranchLabel(codeStream);
               cst = this.left.optimizedBooleanConstant();
               leftIsConst = cst != Constant.NotAConstant;
               leftIsTrue = leftIsConst && cst.booleanValue();
               cst = this.right.optimizedBooleanConstant();
               rightIsConst = cst != Constant.NotAConstant;
               rightIsTrue = rightIsConst && cst.booleanValue();
               if (leftIsConst) {
                  this.left.generateCode(currentScope, codeStream, false);
                  if (!leftIsTrue) {
                     break label108;
                  }
               } else {
                  this.left.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, true);
               }

               if (this.rightInitStateIndex != -1) {
                  codeStream.addDefinitelyAssignedVariables(currentScope, this.rightInitStateIndex);
               }

               if (rightIsConst) {
                  this.right.generateCode(currentScope, codeStream, false);
               } else {
                  this.right.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, valueRequired);
               }
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            if (valueRequired) {
               if (leftIsConst && !leftIsTrue) {
                  codeStream.iconst_0();
               } else {
                  if (rightIsConst && !rightIsTrue) {
                     codeStream.iconst_0();
                  } else {
                     codeStream.iconst_1();
                  }

                  if (falseLabel.forwardReferenceCount() > 0) {
                     if ((this.bits & 16) != 0) {
                        codeStream.generateImplicitConversion(this.implicitConversion);
                        codeStream.generateReturnBytecode(this);
                        falseLabel.place();
                        codeStream.iconst_0();
                     } else {
                        BranchLabel endLabel;
                        codeStream.goto_(endLabel = new BranchLabel(codeStream));
                        codeStream.decrStackSize(1);
                        falseLabel.place();
                        codeStream.iconst_0();
                        endLabel.place();
                     }
                  } else {
                     falseLabel.place();
                  }
               }

               codeStream.generateImplicitConversion(this.implicitConversion);
               codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
            } else {
               falseLabel.place();
            }
         }
      }
   }

   @Override
   public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
      if (this.constant != Constant.NotAConstant) {
         super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
      } else {
         Constant cst = this.right.constant;
         if (cst != Constant.NotAConstant && cst.booleanValue()) {
            int pc = codeStream.position;
            this.left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            codeStream.recordPositionsFrom(pc, this.sourceStart);
         } else {
            cst = this.left.optimizedBooleanConstant();
            boolean leftIsConst = cst != Constant.NotAConstant;
            boolean leftIsTrue = leftIsConst && cst.booleanValue();
            cst = this.right.optimizedBooleanConstant();
            boolean rightIsConst = cst != Constant.NotAConstant;
            boolean rightIsTrue = rightIsConst && cst.booleanValue();
            if (falseLabel == null) {
               if (trueLabel != null) {
                  BranchLabel internalFalseLabel = new BranchLabel(codeStream);
                  this.left.generateOptimizedBoolean(currentScope, codeStream, null, internalFalseLabel, !leftIsConst);
                  if (leftIsConst && !leftIsTrue) {
                     internalFalseLabel.place();
                  } else {
                     if (this.rightInitStateIndex != -1) {
                        codeStream.addDefinitelyAssignedVariables(currentScope, this.rightInitStateIndex);
                     }

                     this.right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, null, valueRequired && !rightIsConst);
                     if (valueRequired && rightIsConst && rightIsTrue) {
                        codeStream.goto_(trueLabel);
                        codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
                     }

                     internalFalseLabel.place();
                  }
               }
            } else if (trueLabel == null) {
               this.left.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, !leftIsConst);
               int pc = codeStream.position;
               if (leftIsConst && !leftIsTrue) {
                  if (valueRequired) {
                     codeStream.goto_(falseLabel);
                  }

                  codeStream.recordPositionsFrom(pc, this.sourceEnd);
               } else {
                  if (this.rightInitStateIndex != -1) {
                     codeStream.addDefinitelyAssignedVariables(currentScope, this.rightInitStateIndex);
                  }

                  this.right.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, valueRequired && !rightIsConst);
                  if (valueRequired && rightIsConst && !rightIsTrue) {
                     codeStream.goto_(falseLabel);
                     codeStream.recordPositionsFrom(pc, this.sourceEnd);
                  }
               }
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }
         }
      }
   }

   @Override
   public boolean isCompactableOperation() {
      return false;
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      TypeBinding result = super.resolveType(scope);
      Binding leftDirect = Expression.getDirectBinding(this.left);
      if (leftDirect != null && leftDirect == Expression.getDirectBinding(this.right) && !(this.right instanceof Assignment)) {
         scope.problemReporter().comparingIdenticalExpressions(this);
      }

      return result;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         this.left.traverse(visitor, scope);
         this.right.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }
}
