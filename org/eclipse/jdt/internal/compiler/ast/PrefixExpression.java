package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class PrefixExpression extends CompoundAssignment {
   public PrefixExpression(Expression lhs, Expression expression, int operator, int pos) {
      super(lhs, expression, operator, lhs.sourceEnd);
      this.sourceStart = pos;
      this.sourceEnd = lhs.sourceEnd;
   }

   @Override
   public boolean checkCastCompatibility() {
      return false;
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
      output.append(this.operatorToString()).append(' ');
      return this.lhs.printExpression(0, output);
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
