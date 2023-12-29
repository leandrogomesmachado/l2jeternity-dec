package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public abstract class TryFlowContext extends FlowContext {
   public FlowContext outerTryContext;

   public TryFlowContext(FlowContext parent, ASTNode associatedNode) {
      super(parent, associatedNode);
   }

   @Override
   public void markFinallyNullStatus(LocalVariableBinding local, int nullStatus) {
      if (this.outerTryContext != null) {
         this.outerTryContext.markFinallyNullStatus(local, nullStatus);
      }

      super.markFinallyNullStatus(local, nullStatus);
   }

   @Override
   public void mergeFinallyNullInfo(FlowInfo flowInfo) {
      if (this.outerTryContext != null) {
         this.outerTryContext.mergeFinallyNullInfo(flowInfo);
      }

      super.mergeFinallyNullInfo(flowInfo);
   }
}
