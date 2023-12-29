package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.FloatUtil;

public class DoubleLiteral extends NumberLiteral {
   double value;

   public DoubleLiteral(char[] token, int s, int e) {
      super(token, s, e);
   }

   @Override
   public void computeConstant() {
      boolean containsUnderscores = CharOperation.indexOf('_', this.source) > 0;
      if (containsUnderscores) {
         this.source = CharOperation.remove(this.source, '_');
      }

      Double computedValue;
      try {
         computedValue = Double.valueOf(String.valueOf(this.source));
      } catch (NumberFormatException var8) {
         try {
            double v = FloatUtil.valueOfHexDoubleLiteral(this.source);
            if (v == Double.POSITIVE_INFINITY) {
               return;
            }

            if (Double.isNaN(v)) {
               return;
            }

            this.value = v;
            this.constant = DoubleConstant.fromValue(v);
         } catch (NumberFormatException var7) {
         }

         return;
      }

      double doubleValue = computedValue;
      if (!(doubleValue > Double.MAX_VALUE)) {
         if (doubleValue < Double.MIN_VALUE) {
            boolean isHexaDecimal = false;
            int i = 0;

            label59:
            while(i < this.source.length) {
               switch(this.source[i]) {
                  case 'D':
                  case 'E':
                  case 'F':
                  case 'd':
                  case 'e':
                  case 'f':
                     if (isHexaDecimal) {
                        return;
                     }
                  case 'P':
                  case 'p':
                     break label59;
                  case 'X':
                  case 'x':
                     isHexaDecimal = true;
                  case '.':
                  case '0':
                     ++i;
                     break;
                  default:
                     return;
               }
            }
         }

         this.value = doubleValue;
         this.constant = DoubleConstant.fromValue(this.value);
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
      return TypeBinding.DOUBLE;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
