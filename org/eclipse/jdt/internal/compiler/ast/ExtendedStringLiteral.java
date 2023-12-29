package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ExtendedStringLiteral extends StringLiteral {
   public ExtendedStringLiteral(StringLiteral str, CharLiteral character) {
      super(str.source, str.sourceStart, str.sourceEnd, str.lineNumber);
      this.extendWith(character);
   }

   public ExtendedStringLiteral(StringLiteral str1, StringLiteral str2) {
      super(str1.source, str1.sourceStart, str1.sourceEnd, str1.lineNumber);
      this.extendWith(str2);
   }

   @Override
   public ExtendedStringLiteral extendWith(CharLiteral lit) {
      int length = this.source.length;
      System.arraycopy(this.source, 0, this.source = new char[length + 1], 0, length);
      this.source[length] = lit.value;
      this.sourceEnd = lit.sourceEnd;
      return this;
   }

   @Override
   public ExtendedStringLiteral extendWith(StringLiteral lit) {
      int length = this.source.length;
      System.arraycopy(this.source, 0, this.source = new char[length + lit.source.length], 0, length);
      System.arraycopy(lit.source, 0, this.source, length, lit.source.length);
      this.sourceEnd = lit.sourceEnd;
      return this;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return output.append("ExtendedStringLiteral{").append(this.source).append('}');
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
