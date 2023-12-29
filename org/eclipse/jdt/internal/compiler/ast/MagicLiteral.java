package org.eclipse.jdt.internal.compiler.ast;

public abstract class MagicLiteral extends Literal {
   public MagicLiteral(int start, int end) {
      super(start, end);
   }

   @Override
   public boolean isValidJavaStatement() {
      return false;
   }

   @Override
   public char[] source() {
      return null;
   }
}
