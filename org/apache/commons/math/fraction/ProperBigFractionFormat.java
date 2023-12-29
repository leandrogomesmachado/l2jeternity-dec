package org.apache.commons.math.fraction;

import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class ProperBigFractionFormat extends BigFractionFormat {
   private static final long serialVersionUID = -6337346779577272307L;
   private NumberFormat wholeFormat;

   public ProperBigFractionFormat() {
      this(getDefaultNumberFormat());
   }

   public ProperBigFractionFormat(NumberFormat format) {
      this(format, (NumberFormat)format.clone(), (NumberFormat)format.clone());
   }

   public ProperBigFractionFormat(NumberFormat wholeFormat, NumberFormat numeratorFormat, NumberFormat denominatorFormat) {
      super(numeratorFormat, denominatorFormat);
      this.setWholeFormat(wholeFormat);
   }

   @Override
   public StringBuffer format(BigFraction fraction, StringBuffer toAppendTo, FieldPosition pos) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
      BigInteger num = fraction.getNumerator();
      BigInteger den = fraction.getDenominator();
      BigInteger whole = num.divide(den);
      num = num.remainder(den);
      if (!BigInteger.ZERO.equals(whole)) {
         this.getWholeFormat().format(whole, toAppendTo, pos);
         toAppendTo.append(' ');
         if (num.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
         }
      }

      this.getNumeratorFormat().format(num, toAppendTo, pos);
      toAppendTo.append(" / ");
      this.getDenominatorFormat().format(den, toAppendTo, pos);
      return toAppendTo;
   }

   public NumberFormat getWholeFormat() {
      return this.wholeFormat;
   }

   @Override
   public BigFraction parse(String source, ParsePosition pos) {
      BigFraction ret = super.parse(source, pos);
      if (ret != null) {
         return ret;
      } else {
         int initialIndex = pos.getIndex();
         parseAndIgnoreWhitespace(source, pos);
         BigInteger whole = this.parseNextBigInteger(source, pos);
         if (whole == null) {
            pos.setIndex(initialIndex);
            return null;
         } else {
            parseAndIgnoreWhitespace(source, pos);
            BigInteger num = this.parseNextBigInteger(source, pos);
            if (num == null) {
               pos.setIndex(initialIndex);
               return null;
            } else if (num.compareTo(BigInteger.ZERO) < 0) {
               pos.setIndex(initialIndex);
               return null;
            } else {
               int startIndex = pos.getIndex();
               char c = parseNextCharacter(source, pos);
               switch(c) {
                  case '\u0000':
                     return new BigFraction(num);
                  case '/':
                     parseAndIgnoreWhitespace(source, pos);
                     BigInteger den = this.parseNextBigInteger(source, pos);
                     if (den == null) {
                        pos.setIndex(initialIndex);
                        return null;
                     } else {
                        if (den.compareTo(BigInteger.ZERO) < 0) {
                           pos.setIndex(initialIndex);
                           return null;
                        }

                        boolean wholeIsNeg = whole.compareTo(BigInteger.ZERO) < 0;
                        if (wholeIsNeg) {
                           whole = whole.negate();
                        }

                        num = whole.multiply(den).add(num);
                        if (wholeIsNeg) {
                           num = num.negate();
                        }

                        return new BigFraction(num, den);
                     }
                  default:
                     pos.setIndex(initialIndex);
                     pos.setErrorIndex(startIndex);
                     return null;
               }
            }
         }
      }
   }

   public void setWholeFormat(NumberFormat format) {
      if (format == null) {
         throw new NullArgumentException(LocalizedFormats.WHOLE_FORMAT);
      } else {
         this.wholeFormat = format;
      }
   }
}
