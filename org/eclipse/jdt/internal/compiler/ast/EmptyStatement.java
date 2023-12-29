package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class EmptyStatement extends Statement {
   public EmptyStatement(int startPosition, int endPosition) {
      this.sourceStart = startPosition;
      this.sourceEnd = endPosition;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      return flowInfo;
   }

   @Override
   public int complainIfUnreachable(FlowInfo flowInfo, BlockScope scope, int complaintLevel, boolean endOfBlock) {
      return scope.compilerOptions().complianceLevel < 3145728L ? complaintLevel : super.complainIfUnreachable(flowInfo, scope, complaintLevel, endOfBlock);
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      return printIndent(tab, output).append(';');
   }

   @Override
   public void resolve(BlockScope scope) {
      if ((this.bits & 1) == 0) {
         scope.problemReporter().superfluousSemicolon(this.sourceStart, this.sourceEnd);
      } else {
         scope.problemReporter().emptyControlFlowStatement(this.sourceStart, this.sourceEnd);
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
