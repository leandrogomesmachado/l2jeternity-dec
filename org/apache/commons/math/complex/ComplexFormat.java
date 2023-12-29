package org.apache.commons.math.complex;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.CompositeFormat;

public class ComplexFormat extends CompositeFormat {
   private static final long serialVersionUID = -3343698360149467646L;
   private static final String DEFAULT_IMAGINARY_CHARACTER = "i";
   private String imaginaryCharacter;
   private NumberFormat imaginaryFormat;
   private NumberFormat realFormat;

   public ComplexFormat() {
      this("i", getDefaultNumberFormat());
   }

   public ComplexFormat(NumberFormat format) {
      this("i", format);
   }

   public ComplexFormat(NumberFormat realFormat, NumberFormat imaginaryFormat) {
      this("i", realFormat, imaginaryFormat);
   }

   public ComplexFormat(String imaginaryCharacter) {
      this(imaginaryCharacter, getDefaultNumberFormat());
   }

   public ComplexFormat(String imaginaryCharacter, NumberFormat format) {
      this(imaginaryCharacter, format, (NumberFormat)format.clone());
   }

   public ComplexFormat(String imaginaryCharacter, NumberFormat realFormat, NumberFormat imaginaryFormat) {
      this.setImaginaryCharacter(imaginaryCharacter);
      this.setImaginaryFormat(imaginaryFormat);
      this.setRealFormat(realFormat);
   }

   public static Locale[] getAvailableLocales() {
      return NumberFormat.getAvailableLocales();
   }

   public static String formatComplex(Complex c) {
      return getInstance().format(c);
   }

   public StringBuffer format(Complex complex, StringBuffer toAppendTo, FieldPosition pos) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
      double re = complex.getReal();
      this.formatDouble(re, this.getRealFormat(), toAppendTo, pos);
      double im = complex.getImaginary();
      if (im < 0.0) {
         toAppendTo.append(" - ");
         this.formatDouble(-im, this.getImaginaryFormat(), toAppendTo, pos);
         toAppendTo.append(this.getImaginaryCharacter());
      } else if (im > 0.0 || Double.isNaN(im)) {
         toAppendTo.append(" + ");
         this.formatDouble(im, this.getImaginaryFormat(), toAppendTo, pos);
         toAppendTo.append(this.getImaginaryCharacter());
      }

      return toAppendTo;
   }

   @Override
   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      StringBuffer ret = null;
      if (obj instanceof Complex) {
         ret = this.format((Complex)obj, toAppendTo, pos);
      } else {
         if (!(obj instanceof Number)) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_INSTANCE_AS_COMPLEX, obj.getClass().getName());
         }

         ret = this.format(new Complex(((Number)obj).doubleValue(), 0.0), toAppendTo, pos);
      }

      return ret;
   }

   public String getImaginaryCharacter() {
      return this.imaginaryCharacter;
   }

   public NumberFormat getImaginaryFormat() {
      return this.imaginaryFormat;
   }

   public static ComplexFormat getInstance() {
      return getInstance(Locale.getDefault());
   }

   public static ComplexFormat getInstance(Locale locale) {
      NumberFormat f = getDefaultNumberFormat(locale);
      return new ComplexFormat(f);
   }

   public NumberFormat getRealFormat() {
      return this.realFormat;
   }

   public Complex parse(String source) throws ParseException {
      ParsePosition parsePosition = new ParsePosition(0);
      Complex result = this.parse(source, parsePosition);
      if (parsePosition.getIndex() == 0) {
         throw MathRuntimeException.createParseException(parsePosition.getErrorIndex(), LocalizedFormats.UNPARSEABLE_COMPLEX_NUMBER, source);
      } else {
         return result;
      }
   }

   public Complex parse(String source, ParsePosition pos) {
      int initialIndex = pos.getIndex();
      this.parseAndIgnoreWhitespace(source, pos);
      Number re = this.parseNumber(source, this.getRealFormat(), pos);
      if (re == null) {
         pos.setIndex(initialIndex);
         return null;
      } else {
         int startIndex = pos.getIndex();
         char c = this.parseNextCharacter(source, pos);
         int sign = 0;
         byte var9;
         switch(c) {
            case '\u0000':
               return new Complex(re.doubleValue(), 0.0);
            case '+':
               var9 = 1;
               break;
            case '-':
               var9 = -1;
               break;
            default:
               pos.setIndex(initialIndex);
               pos.setErrorIndex(startIndex);
               return null;
         }

         this.parseAndIgnoreWhitespace(source, pos);
         Number im = this.parseNumber(source, this.getRealFormat(), pos);
         if (im == null) {
            pos.setIndex(initialIndex);
            return null;
         } else {
            return !this.parseFixedstring(source, this.getImaginaryCharacter(), pos) ? null : new Complex(re.doubleValue(), im.doubleValue() * (double)var9);
         }
      }
   }

   @Override
   public Object parseObject(String source, ParsePosition pos) {
      return this.parse(source, pos);
   }

   public void setImaginaryCharacter(String imaginaryCharacter) {
      if (imaginaryCharacter != null && imaginaryCharacter.length() != 0) {
         this.imaginaryCharacter = imaginaryCharacter;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.EMPTY_STRING_FOR_IMAGINARY_CHARACTER);
      }
   }

   public void setImaginaryFormat(NumberFormat imaginaryFormat) {
      if (imaginaryFormat == null) {
         throw new NullArgumentException(LocalizedFormats.IMAGINARY_FORMAT);
      } else {
         this.imaginaryFormat = imaginaryFormat;
      }
   }

   public void setRealFormat(NumberFormat realFormat) {
      if (realFormat == null) {
         throw new NullArgumentException(LocalizedFormats.REAL_FORMAT);
      } else {
         this.realFormat = realFormat;
      }
   }
}
