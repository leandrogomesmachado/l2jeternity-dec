package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TrueLiteral extends MagicLiteral {
   static final char[] source = new char[]{'t', 'r', 'u', 'e'};

   public TrueLiteral(int s, int e) {
      super(s, e);
   }

   @Override
   public void computeConstant() {
      this.constant = BooleanConstant.fromValue(true);
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      int pc = codeStream.position;
      if (valueRequired) {
         codeStream.generateConstant(this.constant, this.implicitConversion);
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
      int pc = codeStream.position;
      if (valueRequired && falseLabel == null && trueLabel != null) {
         codeStream.goto_(trueLabel);
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public TypeBinding literalType(BlockScope scope) {
      return TypeBinding.BOOLEAN;
   }

   @Override
   public char[] source() {
      return source;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
