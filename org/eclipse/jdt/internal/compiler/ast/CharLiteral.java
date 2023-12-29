package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

public class CharLiteral extends NumberLiteral {
   char value;

   public CharLiteral(char[] token, int s, int e) {
      super(token, s, e);
      this.computeValue();
   }

   @Override
   public void computeConstant() {
      this.constant = CharConstant.fromValue(this.value);
   }

   private void computeValue() {
      if ((this.value = this.source[1]) == '\\') {
         char digit;
         switch(digit = this.source[2]) {
            case '"':
               this.value = '"';
               break;
            case '\'':
               this.value = '\'';
               break;
            case '\\':
               this.value = '\\';
               break;
            case 'b':
               this.value = '\b';
               break;
            case 'f':
               this.value = '\f';
               break;
            case 'n':
               this.value = '\n';
               break;
            case 'r':
               this.value = '\r';
               break;
            case 't':
               this.value = '\t';
               break;
            default:
               int number = ScannerHelper.getNumericValue(digit);
               if ((digit = this.source[3]) != true) {
                  number = number * 8 + ScannerHelper.getNumericValue(digit);
                  if ((digit = this.source[4]) != true) {
                     number = number * 8 + ScannerHelper.getNumericValue(digit);
                  }

                  this.value = (char)number;
               } else {
                  this.constant = CharConstant.fromValue(this.value = (char)number);
               }
         }
      }
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
   public TypeBinding literalType(BlockScope scope) {
      return TypeBinding.CHAR;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      visitor.visit(this, blockScope);
      visitor.endVisit(this, blockScope);
   }
}
