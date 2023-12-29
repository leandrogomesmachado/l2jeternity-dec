package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SuperReference extends ThisReference {
   public SuperReference(int sourceStart, int sourceEnd) {
      super(sourceStart, sourceEnd);
   }

   public static ExplicitConstructorCall implicitSuperConstructorCall() {
      return new ExplicitConstructorCall(1);
   }

   @Override
   public boolean isImplicitThis() {
      return false;
   }

   @Override
   public boolean isSuper() {
      return true;
   }

   @Override
   public boolean isUnqualifiedSuper() {
      return true;
   }

   @Override
   public boolean isThis() {
      return false;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return output.append("super");
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      this.constant = Constant.NotAConstant;
      ReferenceBinding enclosingReceiverType = scope.enclosingReceiverType();
      if (!this.checkAccess(scope, enclosingReceiverType)) {
         return null;
      } else if (enclosingReceiverType.id == 1) {
         scope.problemReporter().cannotUseSuperInJavaLangObject(this);
         return null;
      } else {
         return this.resolvedType = enclosingReceiverType.superclass();
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      visitor.visit(this, blockScope);
      visitor.endVisit(this, blockScope);
   }
}
