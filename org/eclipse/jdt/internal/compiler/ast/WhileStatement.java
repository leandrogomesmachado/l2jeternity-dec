package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.LoopingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class WhileStatement extends Statement {
   public Expression condition;
   public Statement action;
   private BranchLabel breakLabel;
   private BranchLabel continueLabel;
   int preCondInitStateIndex = -1;
   int condIfTrueInitStateIndex = -1;
   int mergedInitStateIndex = -1;

   public WhileStatement(Expression condition, Statement action, int s, int e) {
      this.condition = condition;
      this.action = action;
      if (action instanceof EmptyStatement) {
         action.bits |= 1;
      }

      this.sourceStart = s;
      this.sourceEnd = e;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      this.breakLabel = new BranchLabel();
      this.continueLabel = new BranchLabel();
      int initialComplaintLevel = (flowInfo.reachMode() & 3) != 0 ? 1 : 0;
      Constant cst = this.condition.constant;
      boolean isConditionTrue = cst != Constant.NotAConstant && cst.booleanValue();
      boolean isConditionFalse = cst != Constant.NotAConstant && !cst.booleanValue();
      cst = this.condition.optimizedBooleanConstant();
      boolean isConditionOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue();
      boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && !cst.booleanValue();
      this.preCondInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
      FlowInfo condInfo = flowInfo.nullInfoLessUnconditionalCopy();
      LoopingFlowContext condLoopContext;
      FlowInfo var18 = this.condition
         .analyseCode(currentScope, condLoopContext = new LoopingFlowContext(flowContext, flowInfo, this, null, null, currentScope, true), condInfo);
      this.condition.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
      if (this.action == null || this.action.isEmptyBlock() && currentScope.compilerOptions().complianceLevel <= 3080192L) {
         condLoopContext.complainOnDeferredFinalChecks(currentScope, var18);
         condLoopContext.complainOnDeferredNullChecks(currentScope, var18.unconditionalInits());
         if (isConditionTrue) {
            return FlowInfo.DEAD_END;
         } else {
            FlowInfo mergedInfo = flowInfo.copy().addInitializationsFrom(var18.initsWhenFalse());
            if (isConditionOptimizedTrue) {
               mergedInfo.setReachMode(1);
            }

            this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
            return mergedInfo;
         }
      } else {
         LoopingFlowContext loopingContext = new LoopingFlowContext(flowContext, flowInfo, this, this.breakLabel, this.continueLabel, currentScope, true);
         FlowInfo actionInfo;
         if (isConditionFalse) {
            actionInfo = FlowInfo.DEAD_END;
         } else {
            actionInfo = var18.initsWhenTrue().copy();
            if (isConditionOptimizedFalse) {
               actionInfo.setReachMode(1);
            }
         }

         this.condIfTrueInitStateIndex = currentScope.methodScope().recordInitializationStates(var18.initsWhenTrue());
         if (this.action.complainIfUnreachable(actionInfo, currentScope, initialComplaintLevel, true) < 2) {
            actionInfo = this.action.analyseCode(currentScope, loopingContext, actionInfo);
         }

         FlowInfo exitBranch = flowInfo.copy();
         int combinedTagBits = actionInfo.tagBits & loopingContext.initsOnContinue.tagBits;
         UnconditionalFlowInfo var19;
         if ((combinedTagBits & 3) != 0) {
            if ((combinedTagBits & 1) != 0) {
               this.continueLabel = null;
            }

            exitBranch.addInitializationsFrom(var18.initsWhenFalse());
            var19 = actionInfo.mergedWith(loopingContext.initsOnContinue.unconditionalInits());
            condLoopContext.complainOnDeferredNullChecks(currentScope, var19, false);
            loopingContext.complainOnDeferredNullChecks(currentScope, var19, false);
         } else {
            condLoopContext.complainOnDeferredFinalChecks(currentScope, var18);
            var19 = actionInfo.mergedWith(loopingContext.initsOnContinue.unconditionalInits());
            condLoopContext.complainOnDeferredNullChecks(currentScope, var19);
            loopingContext.complainOnDeferredFinalChecks(currentScope, var19);
            loopingContext.complainOnDeferredNullChecks(currentScope, var19);
            exitBranch.addPotentialInitializationsFrom(var19.unconditionalInits()).addInitializationsFrom(var18.initsWhenFalse());
         }

         if (loopingContext.hasEscapingExceptions()) {
            FlowInfo loopbackFlowInfo = flowInfo.copy();
            if (this.continueLabel != null) {
               loopbackFlowInfo = loopbackFlowInfo.mergedWith(loopbackFlowInfo.unconditionalCopy().addNullInfoFrom(var19).unconditionalInits());
            }

            loopingContext.simulateThrowAfterLoopBack(loopbackFlowInfo);
         }

         FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
            (FlowInfo)((loopingContext.initsOnBreak.tagBits & 3) != 0
               ? loopingContext.initsOnBreak
               : flowInfo.addInitializationsFrom(loopingContext.initsOnBreak)),
            isConditionOptimizedTrue,
            exitBranch,
            isConditionOptimizedFalse,
            !isConditionTrue
         );
         this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
         return mergedInfo;
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         Constant cst = this.condition.optimizedBooleanConstant();
         boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && !cst.booleanValue();
         if (isConditionOptimizedFalse) {
            this.condition.generateCode(currentScope, codeStream, false);
            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
               codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            codeStream.recordPositionsFrom(pc, this.sourceStart);
         } else {
            this.breakLabel.initialize(codeStream);
            if (this.continueLabel == null) {
               if (this.condition.constant == Constant.NotAConstant) {
                  this.condition.generateOptimizedBoolean(currentScope, codeStream, null, this.breakLabel, true);
               }
            } else {
               this.continueLabel.initialize(codeStream);
               if ((this.condition.constant == Constant.NotAConstant || !this.condition.constant.booleanValue())
                  && this.action != null
                  && !this.action.isEmptyBlock()) {
                  int jumpPC = codeStream.position;
                  codeStream.goto_(this.continueLabel);
                  codeStream.recordPositionsFrom(jumpPC, this.condition.sourceStart);
               }
            }

            BranchLabel actionLabel = new BranchLabel(codeStream);
            if (this.action != null) {
               actionLabel.tagBits |= 2;
               if (this.condIfTrueInitStateIndex != -1) {
                  codeStream.addDefinitelyAssignedVariables(currentScope, this.condIfTrueInitStateIndex);
               }

               actionLabel.place();
               this.action.generateCode(currentScope, codeStream);
               if (this.preCondInitStateIndex != -1) {
                  codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preCondInitStateIndex);
               }
            } else {
               actionLabel.place();
            }

            if (this.continueLabel != null) {
               this.continueLabel.place();
               this.condition.generateOptimizedBoolean(currentScope, codeStream, actionLabel, null, true);
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
               codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            this.breakLabel.place();
            codeStream.recordPositionsFrom(pc, this.sourceStart);
         }
      }
   }

   @Override
   public void resolve(BlockScope scope) {
      TypeBinding type = this.condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
      this.condition.computeConversion(scope, type, type);
      if (this.action != null) {
         this.action.resolve(scope);
      }
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      printIndent(tab, output).append("while (");
      this.condition.printExpression(0, output).append(')');
      if (this.action == null) {
         output.append(';');
      } else {
         this.action.printStatement(tab + 1, output);
      }

      return output;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.condition.traverse(visitor, blockScope);
         if (this.action != null) {
            this.action.traverse(visitor, blockScope);
         }
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public boolean doesNotCompleteNormally() {
      Constant cst = this.condition.constant;
      boolean isConditionTrue = cst == null || cst != Constant.NotAConstant && cst.booleanValue();
      cst = this.condition.optimizedBooleanConstant();
      boolean isConditionOptimizedTrue = cst == null ? true : cst != Constant.NotAConstant && cst.booleanValue();
      return (isConditionTrue || isConditionOptimizedTrue) && (this.action == null || !this.action.breaksOut(null));
   }

   @Override
   public boolean completesByContinue() {
      return this.action.continuesAtOuterLabel();
   }
}
