package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;

public class SingleMemberAnnotation extends Annotation {
   public Expression memberValue;
   private MemberValuePair[] singlePairs;

   public SingleMemberAnnotation(TypeReference type, int sourceStart) {
      this.type = type;
      this.sourceStart = sourceStart;
      this.sourceEnd = type.sourceEnd;
   }

   public SingleMemberAnnotation() {
   }

   @Override
   public ElementValuePair[] computeElementValuePairs() {
      return new ElementValuePair[]{this.memberValuePairs()[0].compilerElementPair};
   }

   @Override
   public MemberValuePair[] memberValuePairs() {
      if (this.singlePairs == null) {
         this.singlePairs = new MemberValuePair[]{new MemberValuePair(VALUE, this.memberValue.sourceStart, this.memberValue.sourceEnd, this.memberValue)};
      }

      return this.singlePairs;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      super.printExpression(indent, output);
      output.append('(');
      this.memberValue.printExpression(indent, output);
      return output.append(')');
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.memberValue != null) {
            this.memberValue.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.memberValue != null) {
            this.memberValue.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }
}
