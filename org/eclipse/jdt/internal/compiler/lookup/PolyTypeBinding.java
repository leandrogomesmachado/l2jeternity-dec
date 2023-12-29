package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;

public class PolyTypeBinding extends TypeBinding {
   Expression expression;
   boolean vanillaCompatibilty = true;

   public PolyTypeBinding(Expression expression) {
      this.expression = expression;
   }

   @Override
   public char[] constantPoolName() {
      throw new UnsupportedOperationException();
   }

   @Override
   public PackageBinding getPackage() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isCompatibleWith(TypeBinding left, Scope scope) {
      return this.vanillaCompatibilty ? this.expression.isCompatibleWith(left, scope) : this.expression.isBoxingCompatibleWith(left, scope);
   }

   @Override
   public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope) {
      return this.expression.isPotentiallyCompatibleWith(targetType, scope);
   }

   @Override
   public boolean isPolyType() {
      return true;
   }

   @Override
   public boolean isFunctionalType() {
      return this.expression.isFunctionalType();
   }

   @Override
   public char[] qualifiedSourceName() {
      return this.readableName();
   }

   @Override
   public char[] sourceName() {
      return this.readableName();
   }

   @Override
   public char[] readableName() {
      return this.expression.printExpression(0, new StringBuffer()).toString().toCharArray();
   }

   @Override
   public char[] shortReadableName() {
      return this.expression instanceof LambdaExpression
         ? ((LambdaExpression)this.expression).printExpression(0, new StringBuffer(), true).toString().toCharArray()
         : this.readableName();
   }

   @Override
   public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope scope) {
      return this.expression.sIsMoreSpecific(s, t, scope);
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer("PolyTypeBinding for: ");
      return this.expression.printExpression(0, buffer).toString();
   }

   @Override
   public int kind() {
      return 65540;
   }

   public TypeBinding computeBoxingType() {
      PolyTypeBinding type = new PolyTypeBinding(this.expression);
      type.vanillaCompatibilty = !this.vanillaCompatibilty;
      return type;
   }
}
