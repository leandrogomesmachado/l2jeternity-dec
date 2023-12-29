package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.FloatUtil;

public class FloatLiteral extends NumberLiteral {
   float value;

   public FloatLiteral(char[] token, int s, int e) {
      super(token, s, e);
   }

   @Override
   public void computeConstant() {
      boolean containsUnderscores = CharOperation.indexOf('_', this.source) > 0;
      if (containsUnderscores) {
         this.source = CharOperation.remove(this.source, '_');
      }

      Float computedValue;
      try {
         computedValue = Float.valueOf(String.valueOf(this.source));
      } catch (NumberFormatException var7) {
         try {
            float v = FloatUtil.valueOfHexFloatLiteral(this.source);
            if (v == Float.POSITIVE_INFINITY) {
               return;
            }

            if (Float.isNaN(v)) {
               return;
            }

            this.value = v;
            this.constant = FloatConstant.fromValue(v);
         } catch (NumberFormatException var6) {
         }

         return;
      }

      float floatValue = computedValue;
      if (!(floatValue > Float.MAX_VALUE)) {
         if (floatValue < Float.MIN_VALUE) {
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

         this.value = floatValue;
         this.constant = FloatConstant.fromValue(this.value);
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
      return TypeBinding.FLOAT;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }
}
