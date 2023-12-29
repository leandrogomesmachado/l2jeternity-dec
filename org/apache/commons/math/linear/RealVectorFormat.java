package org.apache.commons.math.linear;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.CompositeFormat;

public class RealVectorFormat extends CompositeFormat {
   private static final long serialVersionUID = -708767813036157690L;
   private static final String DEFAULT_PREFIX = "{";
   private static final String DEFAULT_SUFFIX = "}";
   private static final String DEFAULT_SEPARATOR = "; ";
   private final String prefix;
   private final String suffix;
   private final String separator;
   private final String trimmedPrefix;
   private final String trimmedSuffix;
   private final String trimmedSeparator;
   private final NumberFormat format;

   public RealVectorFormat() {
      this("{", "}", "; ", getDefaultNumberFormat());
   }

   public RealVectorFormat(NumberFormat format) {
      this("{", "}", "; ", format);
   }

   public RealVectorFormat(String prefix, String suffix, String separator) {
      this(prefix, suffix, separator, getDefaultNumberFormat());
   }

   public RealVectorFormat(String prefix, String suffix, String separator, NumberFormat format) {
      this.prefix = prefix;
      this.suffix = suffix;
      this.separator = separator;
      this.trimmedPrefix = prefix.trim();
      this.trimmedSuffix = suffix.trim();
      this.trimmedSeparator = separator.trim();
      this.format = format;
   }

   public static Locale[] getAvailableLocales() {
      return NumberFormat.getAvailableLocales();
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public String getSeparator() {
      return this.separator;
   }

   public NumberFormat getFormat() {
      return this.format;
   }

   public static RealVectorFormat getInstance() {
      return getInstance(Locale.getDefault());
   }

   public static RealVectorFormat getInstance(Locale locale) {
      return new RealVectorFormat(getDefaultNumberFormat(locale));
   }

   public static String formatRealVector(RealVector v) {
      return getInstance().format(v);
   }

   public StringBuffer format(RealVector vector, StringBuffer toAppendTo, FieldPosition pos) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
      toAppendTo.append(this.prefix);

      for(int i = 0; i < vector.getDimension(); ++i) {
         if (i > 0) {
            toAppendTo.append(this.separator);
         }

         this.formatDouble(vector.getEntry(i), this.format, toAppendTo, pos);
      }

      toAppendTo.append(this.suffix);
      return toAppendTo;
   }

   @Override
   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      if (obj instanceof RealVector) {
         return this.format((RealVector)obj, toAppendTo, pos);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_INSTANCE_AS_REAL_VECTOR, obj.getClass().getName());
      }
   }

   public ArrayRealVector parse(String source) throws ParseException {
      ParsePosition parsePosition = new ParsePosition(0);
      ArrayRealVector result = this.parse(source, parsePosition);
      if (parsePosition.getIndex() == 0) {
         throw MathRuntimeException.createParseException(parsePosition.getErrorIndex(), LocalizedFormats.UNPARSEABLE_REAL_VECTOR, source);
      } else {
         return result;
      }
   }

   public ArrayRealVector parse(String source, ParsePosition pos) {
      int initialIndex = pos.getIndex();
      this.parseAndIgnoreWhitespace(source, pos);
      if (!this.parseFixedstring(source, this.trimmedPrefix, pos)) {
         return null;
      } else {
         List<Number> components = new ArrayList<>();
         boolean loop = true;

         while(loop) {
            if (!components.isEmpty()) {
               this.parseAndIgnoreWhitespace(source, pos);
               if (!this.parseFixedstring(source, this.trimmedSeparator, pos)) {
                  loop = false;
               }
            }

            if (loop) {
               this.parseAndIgnoreWhitespace(source, pos);
               Number component = this.parseNumber(source, this.format, pos);
               if (component == null) {
                  pos.setIndex(initialIndex);
                  return null;
               }

               components.add(component);
            }
         }

         this.parseAndIgnoreWhitespace(source, pos);
         if (!this.parseFixedstring(source, this.trimmedSuffix, pos)) {
            return null;
         } else {
            double[] data = new double[components.size()];

            for(int i = 0; i < data.length; ++i) {
               data[i] = components.get(i).doubleValue();
            }

            return new ArrayRealVector(data, false);
         }
      }
   }

   @Override
   public Object parseObject(String source, ParsePosition pos) {
      return this.parse(source, pos);
   }
}
