package org.apache.commons.math.fraction;

import java.io.Serializable;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class AbstractFormat extends NumberFormat implements Serializable {
   private static final long serialVersionUID = -6981118387974191891L;
   protected NumberFormat denominatorFormat;
   protected NumberFormat numeratorFormat;

   protected AbstractFormat() {
      this(getDefaultNumberFormat());
   }

   protected AbstractFormat(NumberFormat format) {
      this(format, (NumberFormat)format.clone());
   }

   protected AbstractFormat(NumberFormat numeratorFormat, NumberFormat denominatorFormat) {
      this.numeratorFormat = numeratorFormat;
      this.denominatorFormat = denominatorFormat;
   }

   protected static NumberFormat getDefaultNumberFormat() {
      return getDefaultNumberFormat(Locale.getDefault());
   }

   protected static NumberFormat getDefaultNumberFormat(Locale locale) {
      NumberFormat nf = NumberFormat.getNumberInstance(locale);
      nf.setMaximumFractionDigits(0);
      nf.setParseIntegerOnly(true);
      return nf;
   }

   public NumberFormat getDenominatorFormat() {
      return this.denominatorFormat;
   }

   public NumberFormat getNumeratorFormat() {
      return this.numeratorFormat;
   }

   public void setDenominatorFormat(NumberFormat format) {
      if (format == null) {
         throw new NullArgumentException(LocalizedFormats.DENOMINATOR_FORMAT);
      } else {
         this.denominatorFormat = format;
      }
   }

   public void setNumeratorFormat(NumberFormat format) {
      if (format == null) {
         throw new NullArgumentException(LocalizedFormats.NUMERATOR_FORMAT);
      } else {
         this.numeratorFormat = format;
      }
   }

   protected static void parseAndIgnoreWhitespace(String source, ParsePosition pos) {
      parseNextCharacter(source, pos);
      pos.setIndex(pos.getIndex() - 1);
   }

   protected static char parseNextCharacter(String source, ParsePosition pos) {
      int index = pos.getIndex();
      int n = source.length();
      char ret = 0;
      if (index < n) {
         char c;
         do {
            c = source.charAt(index++);
         } while(Character.isWhitespace(c) && index < n);

         pos.setIndex(index);
         if (index < n) {
            ret = c;
         }
      }

      return ret;
   }

   @Override
   public StringBuffer format(double value, StringBuffer buffer, FieldPosition position) {
      return this.format(Double.valueOf(value), buffer, position);
   }

   @Override
   public StringBuffer format(long value, StringBuffer buffer, FieldPosition position) {
      return this.format(Long.valueOf(value), buffer, position);
   }
}
