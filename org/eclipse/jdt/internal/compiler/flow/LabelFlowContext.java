package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class LabelFlowContext extends SwitchFlowContext {
   public char[] labelName;

   public LabelFlowContext(FlowContext parent, ASTNode associatedNode, char[] labelName, BranchLabel breakLabel, BlockScope scope) {
      super(parent, associatedNode, breakLabel, false);
      this.labelName = labelName;
      this.checkLabelValidity(scope);
   }

   void checkLabelValidity(BlockScope scope) {
      for(FlowContext current = this.getLocalParent(); current != null; current = current.getLocalParent()) {
         char[] currentLabelName;
         if ((currentLabelName = current.labelName()) != null && CharOperation.equals(currentLabelName, this.labelName)) {
            scope.problemReporter().alreadyDefinedLabel(this.labelName, this.associatedNode);
         }
      }
   }

   @Override
   public String individualToString() {
      return "Label flow context [label:" + String.valueOf(this.labelName) + "]";
   }

   @Override
   public char[] labelName() {
      return this.labelName;
   }
}
