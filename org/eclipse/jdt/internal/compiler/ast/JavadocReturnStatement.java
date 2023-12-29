package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class JavadocReturnStatement extends ReturnStatement {
   public JavadocReturnStatement(int s, int e) {
      super(null, s, e);
      this.bits |= 294912;
   }

   @Override
   public void resolve(BlockScope scope) {
      MethodScope methodScope = scope.methodScope();
      MethodBinding methodBinding = null;
      Object var10000;
      if (methodScope.referenceContext instanceof AbstractMethodDeclaration) {
         methodBinding = ((AbstractMethodDeclaration)methodScope.referenceContext).binding;
         var10000 = ((AbstractMethodDeclaration)methodScope.referenceContext).binding == null ? null : methodBinding.returnType;
      } else {
         var10000 = TypeBinding.VOID;
      }

      TypeBinding methodType = (TypeBinding)var10000;
      if (methodType == null || methodType == TypeBinding.VOID) {
         scope.problemReporter().javadocUnexpectedTag(this.sourceStart, this.sourceEnd);
      } else if ((this.bits & 262144) != 0) {
         scope.problemReporter().javadocEmptyReturnTag(this.sourceStart, this.sourceEnd, scope.getDeclarationModifiers());
      }
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      printIndent(tab, output).append("return");
      if ((this.bits & 262144) == 0) {
         output.append(' ').append(" <not empty>");
      }

      return output;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }

   public void traverse(ASTVisitor visitor, ClassScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
