package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.SubRoutineStatement;

public class InsideSubRoutineFlowContext extends TryFlowContext {
   public UnconditionalFlowInfo initsOnReturn = FlowInfo.DEAD_END;

   public InsideSubRoutineFlowContext(FlowContext parent, ASTNode associatedNode) {
      super(parent, associatedNode);
   }

   @Override
   public String individualToString() {
      StringBuffer buffer = new StringBuffer("Inside SubRoutine flow context");
      buffer.append("[initsOnReturn -").append(this.initsOnReturn.toString()).append(']');
      return buffer.toString();
   }

   @Override
   public UnconditionalFlowInfo initsOnReturn() {
      return this.initsOnReturn;
   }

   @Override
   public boolean isNonReturningContext() {
      return ((SubRoutineStatement)this.associatedNode).isSubRoutineEscaping();
   }

   @Override
   public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         if (this.initsOnReturn == FlowInfo.DEAD_END) {
            this.initsOnReturn = (UnconditionalFlowInfo)flowInfo.copy();
         } else {
            this.initsOnReturn = this.initsOnReturn.mergedWith(flowInfo);
         }
      }
   }

   @Override
   public SubRoutineStatement subroutine() {
      return (SubRoutineStatement)this.associatedNode;
   }
}
