package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InsideSubRoutineFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class BreakStatement extends BranchStatement {
   public BreakStatement(char[] label, int sourceStart, int e) {
      super(label, sourceStart, e);
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      FlowContext targetContext = this.label == null ? flowContext.getTargetContextForDefaultBreak() : flowContext.getTargetContextForBreakLabel(this.label);
      if (targetContext == null) {
         if (this.label == null) {
            currentScope.problemReporter().invalidBreak(this);
         } else {
            currentScope.problemReporter().undefinedLabel(this);
         }

         return flowInfo;
      } else {
         targetContext.recordAbruptExit();
         targetContext.expireNullCheckedFieldInfo();
         this.initStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
         this.targetLabel = targetContext.breakLabel();
         FlowContext traversedContext = flowContext;
         int subCount = 0;
         this.subroutines = new SubRoutineStatement[5];

         do {
            SubRoutineStatement sub;
            if ((sub = traversedContext.subroutine()) != null) {
               if (subCount == this.subroutines.length) {
                  System.arraycopy(this.subroutines, 0, this.subroutines = new SubRoutineStatement[subCount * 2], 0, subCount);
               }

               this.subroutines[subCount++] = sub;
               if (sub.isSubRoutineEscaping()) {
                  break;
               }
            }

            traversedContext.recordReturnFrom(flowInfo.unconditionalInits());
            traversedContext.recordBreakTo(targetContext);
            if (traversedContext instanceof InsideSubRoutineFlowContext) {
               ASTNode node = traversedContext.associatedNode;
               if (node instanceof TryStatement) {
                  TryStatement tryStatement = (TryStatement)node;
                  flowInfo.addInitializationsFrom(tryStatement.subRoutineInits);
               }
            } else if (traversedContext == targetContext) {
               targetContext.recordBreakFrom(flowInfo);
               break;
            }
         } while((traversedContext = traversedContext.getLocalParent()) != null);

         if (subCount != this.subroutines.length) {
            System.arraycopy(this.subroutines, 0, this.subroutines = new SubRoutineStatement[subCount], 0, subCount);
         }

         return FlowInfo.DEAD_END;
      }
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      printIndent(tab, output).append("break");
      if (this.label != null) {
         output.append(' ').append(this.label);
      }

      return output.append(';');
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockscope) {
      visitor.visit(this, blockscope);
      visitor.endVisit(this, blockscope);
   }

   @Override
   public boolean doesNotCompleteNormally() {
      return true;
   }
}
