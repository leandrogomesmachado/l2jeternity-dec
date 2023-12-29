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
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ForStatement extends Statement {
   public Statement[] initializations;
   public Expression condition;
   public Statement[] increments;
   public Statement action;
   public BlockScope scope;
   private BranchLabel breakLabel;
   private BranchLabel continueLabel;
   int preCondInitStateIndex = -1;
   int preIncrementsInitStateIndex = -1;
   int condIfTrueInitStateIndex = -1;
   int mergedInitStateIndex = -1;

   public ForStatement(Statement[] initializations, Expression condition, Statement[] increments, Statement action, boolean neededScope, int s, int e) {
      this.sourceStart = s;
      this.sourceEnd = e;
      this.initializations = initializations;
      this.condition = condition;
      this.increments = increments;
      this.action = action;
      if (action instanceof EmptyStatement) {
         action.bits |= 1;
      }

      if (neededScope) {
         this.bits |= 536870912;
      }
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      this.breakLabel = new BranchLabel();
      this.continueLabel = new BranchLabel();
      int initialComplaintLevel = (flowInfo.reachMode() & 3) != 0 ? 1 : 0;
      if (this.initializations != null) {
         int i = 0;

         for(int count = this.initializations.length; i < count; ++i) {
            flowInfo = this.initializations[i].analyseCode(this.scope, flowContext, flowInfo);
         }
      }

      this.preCondInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
      Constant cst = this.condition == null ? null : this.condition.constant;
      boolean isConditionTrue = cst == null || cst != Constant.NotAConstant && cst.booleanValue();
      boolean isConditionFalse = cst != null && cst != Constant.NotAConstant && !cst.booleanValue();
      cst = this.condition == null ? null : this.condition.optimizedBooleanConstant();
      boolean isConditionOptimizedTrue = cst == null || cst != Constant.NotAConstant && cst.booleanValue();
      boolean isConditionOptimizedFalse = cst != null && cst != Constant.NotAConstant && !cst.booleanValue();
      LoopingFlowContext condLoopContext = null;
      FlowInfo condInfo = flowInfo.nullInfoLessUnconditionalCopy();
      if (this.condition != null && !isConditionTrue) {
         condInfo = this.condition
            .analyseCode(this.scope, condLoopContext = new LoopingFlowContext(flowContext, flowInfo, this, null, null, this.scope, true), condInfo);
         this.condition.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
      }

      LoopingFlowContext loopingContext;
      UnconditionalFlowInfo actionInfo;
      if (this.action == null || this.action.isEmptyBlock() && currentScope.compilerOptions().complianceLevel <= 3080192L) {
         if (condLoopContext != null) {
            condLoopContext.complainOnDeferredFinalChecks(this.scope, condInfo);
         }

         if (isConditionTrue) {
            if (condLoopContext != null) {
               condLoopContext.complainOnDeferredNullChecks(currentScope, condInfo);
            }

            return FlowInfo.DEAD_END;
         }

         if (isConditionFalse) {
            this.continueLabel = null;
         }

         actionInfo = condInfo.initsWhenTrue().unconditionalCopy();
         loopingContext = new LoopingFlowContext(flowContext, flowInfo, this, this.breakLabel, this.continueLabel, this.scope, false);
      } else {
         loopingContext = new LoopingFlowContext(flowContext, flowInfo, this, this.breakLabel, this.continueLabel, this.scope, true);
         FlowInfo initsWhenTrue = condInfo.initsWhenTrue();
         this.condIfTrueInitStateIndex = currentScope.methodScope().recordInitializationStates(initsWhenTrue);
         if (isConditionFalse) {
            actionInfo = FlowInfo.DEAD_END;
         } else {
            actionInfo = initsWhenTrue.unconditionalCopy();
            if (isConditionOptimizedFalse) {
               actionInfo.setReachMode(1);
            }
         }

         if (this.action.complainIfUnreachable(actionInfo, this.scope, initialComplaintLevel, true) < 2) {
            actionInfo = this.action.analyseCode(this.scope, loopingContext, actionInfo).unconditionalInits();
         }

         if ((actionInfo.tagBits & loopingContext.initsOnContinue.tagBits & 1) != 0) {
            this.continueLabel = null;
         } else {
            if (condLoopContext != null) {
               condLoopContext.complainOnDeferredFinalChecks(this.scope, condInfo);
            }

            actionInfo = actionInfo.mergedWith(loopingContext.initsOnContinue);
            loopingContext.complainOnDeferredFinalChecks(this.scope, actionInfo);
         }
      }

      FlowInfo exitBranch = flowInfo.copy();
      LoopingFlowContext incrementContext = null;
      if (this.continueLabel != null) {
         if (this.increments != null) {
            incrementContext = new LoopingFlowContext(flowContext, flowInfo, this, null, null, this.scope, true);
            FlowInfo incrementInfo = actionInfo;
            this.preIncrementsInitStateIndex = currentScope.methodScope().recordInitializationStates(actionInfo);
            int i = 0;

            for(int count = this.increments.length; i < count; ++i) {
               incrementInfo = this.increments[i].analyseCode(this.scope, incrementContext, incrementInfo);
            }

            incrementContext.complainOnDeferredFinalChecks(this.scope, actionInfo = incrementInfo.unconditionalInits());
         }

         exitBranch.addPotentialInitializationsFrom(actionInfo).addInitializationsFrom(condInfo.initsWhenFalse());
      } else {
         exitBranch.addInitializationsFrom(condInfo.initsWhenFalse());
         if (this.increments != null && initialComplaintLevel == 0) {
            currentScope.problemReporter().fakeReachable(this.increments[0]);
         }
      }

      if (condLoopContext != null) {
         condLoopContext.complainOnDeferredNullChecks(currentScope, actionInfo);
      }

      loopingContext.complainOnDeferredNullChecks(currentScope, actionInfo);
      if (incrementContext != null) {
         incrementContext.complainOnDeferredNullChecks(currentScope, actionInfo);
      }

      if (loopingContext.hasEscapingExceptions()) {
         FlowInfo loopbackFlowInfo = flowInfo.copy();
         if (this.continueLabel != null) {
            loopbackFlowInfo = loopbackFlowInfo.mergedWith(loopbackFlowInfo.unconditionalCopy().addNullInfoFrom(actionInfo).unconditionalInits());
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
      if (this.initializations != null) {
         for(int i = 0; i < this.initializations.length; ++i) {
            Statement init = this.initializations[i];
            if (init instanceof LocalDeclaration) {
               LocalVariableBinding binding = ((LocalDeclaration)init).binding;
               mergedInfo.resetAssignmentInfo(binding);
            }
         }
      }

      this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
      return mergedInfo;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         if (this.initializations != null) {
            int i = 0;

            for(int max = this.initializations.length; i < max; ++i) {
               this.initializations[i].generateCode(this.scope, codeStream);
            }
         }

         Constant cst = this.condition == null ? null : this.condition.optimizedBooleanConstant();
         boolean isConditionOptimizedFalse = cst != null && cst != Constant.NotAConstant && !cst.booleanValue();
         if (isConditionOptimizedFalse) {
            this.condition.generateCode(this.scope, codeStream, false);
            if ((this.bits & 536870912) != 0) {
               codeStream.exitUserScope(this.scope);
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
               codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            codeStream.recordPositionsFrom(pc, this.sourceStart);
         } else {
            BranchLabel actionLabel = new BranchLabel(codeStream);
            actionLabel.tagBits |= 2;
            BranchLabel conditionLabel = new BranchLabel(codeStream);
            this.breakLabel.initialize(codeStream);
            if (this.continueLabel == null) {
               conditionLabel.place();
               if (this.condition != null && this.condition.constant == Constant.NotAConstant) {
                  this.condition.generateOptimizedBoolean(this.scope, codeStream, null, this.breakLabel, true);
               }
            } else {
               this.continueLabel.initialize(codeStream);
               if (this.condition != null
                  && this.condition.constant == Constant.NotAConstant
                  && (this.action != null && !this.action.isEmptyBlock() || this.increments != null)) {
                  conditionLabel.tagBits |= 2;
                  int jumpPC = codeStream.position;
                  codeStream.goto_(conditionLabel);
                  codeStream.recordPositionsFrom(jumpPC, this.condition.sourceStart);
               }
            }

            if (this.action != null) {
               if (this.condIfTrueInitStateIndex != -1) {
                  codeStream.addDefinitelyAssignedVariables(currentScope, this.condIfTrueInitStateIndex);
               }

               actionLabel.place();
               this.action.generateCode(this.scope, codeStream);
            } else {
               actionLabel.place();
            }

            if (this.preIncrementsInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preIncrementsInitStateIndex);
               codeStream.addDefinitelyAssignedVariables(currentScope, this.preIncrementsInitStateIndex);
            }

            if (this.continueLabel != null) {
               this.continueLabel.place();
               if (this.increments != null) {
                  int i = 0;

                  for(int max = this.increments.length; i < max; ++i) {
                     this.increments[i].generateCode(this.scope, codeStream);
                  }
               }

               if (this.preCondInitStateIndex != -1) {
                  codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preCondInitStateIndex);
               }

               conditionLabel.place();
               if (this.condition != null && this.condition.constant == Constant.NotAConstant) {
                  this.condition.generateOptimizedBoolean(this.scope, codeStream, actionLabel, null, true);
               } else {
                  codeStream.goto_(actionLabel);
               }
            } else if (this.preCondInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preCondInitStateIndex);
            }

            if ((this.bits & 536870912) != 0) {
               codeStream.exitUserScope(this.scope);
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
   public StringBuffer printStatement(int tab, StringBuffer output) {
      printIndent(tab, output).append("for (");
      if (this.initializations != null) {
         for(int i = 0; i < this.initializations.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.initializations[i].print(0, output);
         }
      }

      output.append("; ");
      if (this.condition != null) {
         this.condition.printExpression(0, output);
      }

      output.append("; ");
      if (this.increments != null) {
         for(int i = 0; i < this.increments.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.increments[i].print(0, output);
         }
      }

      output.append(") ");
      if (this.action == null) {
         output.append(';');
      } else {
         output.append('\n');
         this.action.printStatement(tab + 1, output);
      }

      return output;
   }

   @Override
   public void resolve(BlockScope upperScope) {
      this.scope = (this.bits & 536870912) != 0 ? new BlockScope(upperScope) : upperScope;
      if (this.initializations != null) {
         int i = 0;

         for(int length = this.initializations.length; i < length; ++i) {
            this.initializations[i].resolve(this.scope);
         }
      }

      if (this.condition != null) {
         TypeBinding type = this.condition.resolveTypeExpecting(this.scope, TypeBinding.BOOLEAN);
         this.condition.computeConversion(this.scope, type, type);
      }

      if (this.increments != null) {
         int i = 0;

         for(int length = this.increments.length; i < length; ++i) {
            this.increments[i].resolve(this.scope);
         }
      }

      if (this.action != null) {
         this.action.resolve(this.scope);
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         if (this.initializations != null) {
            int initializationsLength = this.initializations.length;

            for(int i = 0; i < initializationsLength; ++i) {
               this.initializations[i].traverse(visitor, this.scope);
            }
         }

         if (this.condition != null) {
            this.condition.traverse(visitor, this.scope);
         }

         if (this.increments != null) {
            int incrementsLength = this.increments.length;

            for(int i = 0; i < incrementsLength; ++i) {
               this.increments[i].traverse(visitor, this.scope);
            }
         }

         if (this.action != null) {
            this.action.traverse(visitor, this.scope);
         }
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public boolean doesNotCompleteNormally() {
      Constant cst = this.condition == null ? null : this.condition.constant;
      boolean isConditionTrue = cst == null || cst != Constant.NotAConstant && cst.booleanValue();
      cst = this.condition == null ? null : this.condition.optimizedBooleanConstant();
      boolean isConditionOptimizedTrue = cst == null ? true : cst != Constant.NotAConstant && cst.booleanValue();
      return (isConditionTrue || isConditionOptimizedTrue) && (this.action == null || !this.action.breaksOut(null));
   }

   @Override
   public boolean completesByContinue() {
      return this.action.continuesAtOuterLabel();
   }
}
