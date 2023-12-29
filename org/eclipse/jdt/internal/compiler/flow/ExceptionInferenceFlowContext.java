package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class ExceptionInferenceFlowContext extends ExceptionHandlingFlowContext {
   public ExceptionInferenceFlowContext(
      FlowContext parent,
      ASTNode associatedNode,
      ReferenceBinding[] handledExceptions,
      FlowContext initializationParent,
      BlockScope scope,
      UnconditionalFlowInfo flowInfo
   ) {
      super(parent, associatedNode, handledExceptions, initializationParent, scope, flowInfo);
   }
}
