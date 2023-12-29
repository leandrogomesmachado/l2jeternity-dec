package org.apache.commons.math.geometry;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.CompositeFormat;

public class Vector3DFormat extends CompositeFormat {
   private static final long serialVersionUID = -5447606608652576301L;
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

   public Vector3DFormat() {
      this("{", "}", "; ", getDefaultNumberFormat());
   }

   public Vector3DFormat(NumberFormat format) {
      this("{", "}", "; ", format);
   }

   public Vector3DFormat(String prefix, String suffix, String separator) {
      this(prefix, suffix, separator, getDefaultNumberFormat());
   }

   public Vector3DFormat(String prefix, String suffix, String separator, NumberFormat format) {
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

   public static Vector3DFormat getInstance() {
      return getInstance(Locale.getDefault());
   }

   public static Vector3DFormat getInstance(Locale locale) {
      return new Vector3DFormat(getDefaultNumberFormat(locale));
   }

   public static String formatVector3D(Vector3D v) {
      return getInstance().format(v);
   }

   public StringBuffer format(Vector3D vector, StringBuffer toAppendTo, FieldPosition pos) {
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
      toAppendTo.append(this.prefix);
      this.formatDouble(vector.getX(), this.format, toAppendTo, pos);
      toAppendTo.append(this.separator);
      this.formatDouble(vector.getY(), this.format, toAppendTo, pos);
      toAppendTo.append(this.separator);
      this.formatDouble(vector.getZ(), this.format, toAppendTo, pos);
      toAppendTo.append(this.suffix);
      return toAppendTo;
   }

   @Override
   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      if (obj instanceof Vector3D) {
         return this.format((Vector3D)obj, toAppendTo, pos);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_INSTANCE_AS_3D_VECTOR, obj.getClass().getName());
      }
   }

   public Vector3D parse(String source) throws ParseException {
      ParsePosition parsePosition = new ParsePosition(0);
      Vector3D result = this.parse(source, parsePosition);
      if (parsePosition.getIndex() == 0) {
         throw MathRuntimeException.createParseException(parsePosition.getErrorIndex(), LocalizedFormats.UNPARSEABLE_3D_VECTOR, source);
      } else {
         return result;
      }
   }

   public Vector3D parse(String source, ParsePosition pos) {
      int initialIndex = pos.getIndex();
      this.parseAndIgnoreWhitespace(source, pos);
      if (!this.parseFixedstring(source, this.trimmedPrefix, pos)) {
         return null;
      } else {
         this.parseAndIgnoreWhitespace(source, pos);
         Number x = this.parseNumber(source, this.format, pos);
         if (x == null) {
            pos.setIndex(initialIndex);
            return null;
         } else {
            this.parseAndIgnoreWhitespace(source, pos);
            if (!this.parseFixedstring(source, this.trimmedSeparator, pos)) {
               return null;
            } else {
               this.parseAndIgnoreWhitespace(source, pos);
               Number y = this.parseNumber(source, this.format, pos);
               if (y == null) {
                  pos.setIndex(initialIndex);
                  return null;
               } else {
                  this.parseAndIgnoreWhitespace(source, pos);
                  if (!this.parseFixedstring(source, this.trimmedSeparator, pos)) {
                     return null;
                  } else {
                     this.parseAndIgnoreWhitespace(source, pos);
                     Number z = this.parseNumber(source, this.format, pos);
                     if (z == null) {
                        pos.setIndex(initialIndex);
                        return null;
                     } else {
                        this.parseAndIgnoreWhitespace(source, pos);
                        return !this.parseFixedstring(source, this.trimmedSuffix, pos)
                           ? null
                           : new Vector3D(x.doubleValue(), y.doubleValue(), z.doubleValue());
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public Object parseObject(String source, ParsePosition pos) {
      return this.parse(source, pos);
   }
}
