package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class MarkerAnnotation extends Annotation {
   public MarkerAnnotation(TypeReference type, int sourceStart) {
      this.type = type;
      this.sourceStart = sourceStart;
      this.sourceEnd = type.sourceEnd;
   }

   @Override
   public MemberValuePair[] memberValuePairs() {
      return NoValuePairs;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope) && this.type != null) {
         this.type.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope) && this.type != null) {
         this.type.traverse(visitor, scope);
      }

      visitor.endVisit(this, scope);
   }
}
