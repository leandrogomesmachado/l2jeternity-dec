package org.apache.commons.math.fraction;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class FractionFormat extends AbstractFormat {
   private static final long serialVersionUID = 3008655719530972611L;

   public FractionFormat() {
   }

   public FractionFormat(NumberFormat format) {
      super(format);
   }

   public FractionFormat(NumberFormat numeratorFormat, NumberFormat denominatorFormat) {
      super(numeratorFormat, denominatorFormat);
   }

   public static Locale[] getAvailableLocales() {
      return NumberFormat.getAvailableLocales();
   }

   public static String formatFraction(Fraction f) {
      return getImproperInstance().format(f);
   }

   public static FractionFormat getImproperInstance() {
      return getImproperInstance(Locale.getDefault());
   }

   public static FractionFormat getImproperInstance(Locale locale) {
      return new FractionFormat(getDefaultNumberFormat(locale));
   }

   public static FractionFormat getProperInstance() {
      return getProperInstance(Locale.getDefault());
   }

   public static FractionFormat getProperInstance(Locale locale) {
      return new ProperFractionFormat(getDefaultNumberFormat(locale));
   }

   protected static NumberFormat getDefaultNumberFormat() {
      return getDefaultNumberFormat(Locale.getDefault());
   }

   public StringBuffer format(Fraction fraction, StringBuffer toAppendTo, FieldPosition pos) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
      this.getNumeratorFormat().format((long)fraction.getNumerator(), toAppendTo, pos);
      toAppendTo.append(" / ");
      this.getDenominatorFormat().format((long)fraction.getDenominator(), toAppendTo, pos);
      return toAppendTo;
   }

   @Override
   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer ret = null;
      if (obj instanceof Fraction) {
         ret = this.format((Fraction)obj, toAppendTo, pos);
      } else {
         if (!(obj instanceof Number)) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_OBJECT_TO_FRACTION);
         }

         try {
            ret = this.format(new Fraction(((Number)obj).doubleValue()), toAppendTo, pos);
         } catch (ConvergenceException var6) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_CONVERT_OBJECT_TO_FRACTION, var6.getLocalizedMessage());
         }
      }

      return ret;
   }

   public Fraction parse(String source) throws ParseException {
      ParsePosition parsePosition = new ParsePosition(0);
      Fraction result = this.parse(source, parsePosition);
      if (parsePosition.getIndex() == 0) {
         throw MathRuntimeException.createParseException(parsePosition.getErrorIndex(), LocalizedFormats.UNPARSEABLE_FRACTION_NUMBER, source);
      } else {
         return result;
      }
   }

   public Fraction parse(String source, ParsePosition pos) {
      int initialIndex = pos.getIndex();
      parseAndIgnoreWhitespace(source, pos);
      Number num = this.getNumeratorFormat().parse(source, pos);
      if (num == null) {
         pos.setIndex(initialIndex);
         return null;
      } else {
         int startIndex = pos.getIndex();
         char c = parseNextCharacter(source, pos);
         switch(c) {
            case '\u0000':
               return new Fraction(num.intValue(), 1);
            case '/':
               parseAndIgnoreWhitespace(source, pos);
               Number den = this.getDenominatorFormat().parse(source, pos);
               if (den == null) {
                  pos.setIndex(initialIndex);
                  return null;
               }

               return new Fraction(num.intValue(), den.intValue());
            default:
               pos.setIndex(initialIndex);
               pos.setErrorIndex(startIndex);
               return null;
         }
      }
   }
}
