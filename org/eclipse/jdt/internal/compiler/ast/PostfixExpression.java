package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class PostfixExpression extends CompoundAssignment {
   public PostfixExpression(Expression lhs, Expression expression, int operator, int pos) {
      super(lhs, expression, operator, pos);
      this.sourceStart = lhs.sourceStart;
      this.sourceEnd = pos;
   }

   @Override
   public boolean checkCastCompatibility() {
      return false;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      int pc = codeStream.position;
      ((Reference)this.lhs).generatePostIncrement(currentScope, codeStream, this, valueRequired);
      if (valueRequired) {
         codeStream.generateImplicitConversion(this.implicitConversion);
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public String operatorToString() {
      switch(this.operator) {
         case 13:
            return "--";
         case 14:
            return "++";
         default:
            return "unknown operator";
      }
   }

   @Override
   public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {
      return this.lhs.printExpression(indent, output).append(' ').append(this.operatorToString());
   }

   @Override
   public boolean restrainUsageToNumericTypes() {
      return true;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         this.lhs.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }
}
