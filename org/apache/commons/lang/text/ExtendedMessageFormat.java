package org.apache.commons.lang.text;

import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;

public class ExtendedMessageFormat extends MessageFormat {
   private static final long serialVersionUID = -2362048321261811743L;
   private static final String DUMMY_PATTERN = "";
   private static final String ESCAPED_QUOTE = "''";
   private static final char START_FMT = ',';
   private static final char END_FE = '}';
   private static final char START_FE = '{';
   private static final char QUOTE = '\'';
   private String toPattern;
   private Map registry;

   public ExtendedMessageFormat(String pattern) {
      this(pattern, Locale.getDefault());
   }

   public ExtendedMessageFormat(String pattern, Locale locale) {
      this(pattern, locale, null);
   }

   public ExtendedMessageFormat(String pattern, Map registry) {
      this(pattern, Locale.getDefault(), registry);
   }

   public ExtendedMessageFormat(String pattern, Locale locale, Map registry) {
      super("");
      this.setLocale(locale);
      this.registry = registry;
      this.applyPattern(pattern);
   }

   public String toPattern() {
      return this.toPattern;
   }

   public final void applyPattern(String pattern) {
      if (this.registry == null) {
         super.applyPattern(pattern);
         this.toPattern = super.toPattern();
      } else {
         ArrayList foundFormats = new ArrayList();
         ArrayList foundDescriptions = new ArrayList();
         StringBuffer stripCustom = new StringBuffer(pattern.length());
         ParsePosition pos = new ParsePosition(0);
         char[] c = pattern.toCharArray();
         int fmtCount = 0;

         while(pos.getIndex() < pattern.length()) {
            switch(c[pos.getIndex()]) {
               case '\'':
                  this.appendQuotedString(pattern, pos, stripCustom, true);
                  break;
               case '{':
                  ++fmtCount;
                  this.seekNonWs(pattern, pos);
                  int start = pos.getIndex();
                  int index = this.readArgumentIndex(pattern, this.next(pos));
                  stripCustom.append('{').append(index);
                  this.seekNonWs(pattern, pos);
                  Format format = null;
                  String formatDescription = null;
                  if (c[pos.getIndex()] == ',') {
                     formatDescription = this.parseFormatDescription(pattern, this.next(pos));
                     format = this.getFormat(formatDescription);
                     if (format == null) {
                        stripCustom.append(',').append(formatDescription);
                     }
                  }

                  foundFormats.add(format);
                  foundDescriptions.add(format == null ? null : formatDescription);
                  Validate.isTrue(foundFormats.size() == fmtCount);
                  Validate.isTrue(foundDescriptions.size() == fmtCount);
                  if (c[pos.getIndex()] != '}') {
                     throw new IllegalArgumentException("Unreadable format element at position " + start);
                  }
               default:
                  stripCustom.append(c[pos.getIndex()]);
                  this.next(pos);
            }
         }

         super.applyPattern(stripCustom.toString());
         this.toPattern = this.insertFormats(super.toPattern(), foundDescriptions);
         if (this.containsElements(foundFormats)) {
            Format[] origFormats = this.getFormats();
            int i = 0;

            for(Format f : foundFormats) {
               if (f != null) {
                  origFormats[i] = f;
               }

               ++i;
            }

            super.setFormats(origFormats);
         }
      }
   }

   public void setFormat(int formatElementIndex, Format newFormat) {
      throw new UnsupportedOperationException();
   }

   public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
      throw new UnsupportedOperationException();
   }

   public void setFormats(Format[] newFormats) {
      throw new UnsupportedOperationException();
   }

   public void setFormatsByArgumentIndex(Format[] newFormats) {
      throw new UnsupportedOperationException();
   }

   private Format getFormat(String desc) {
      if (this.registry != null) {
         String name = desc;
         String args = null;
         int i = desc.indexOf(44);
         if (i > 0) {
            name = desc.substring(0, i).trim();
            args = desc.substring(i + 1).trim();
         }

         FormatFactory factory = (FormatFactory)this.registry.get(name);
         if (factory != null) {
            return factory.getFormat(name, args, this.getLocale());
         }
      }

      return null;
   }

   private int readArgumentIndex(String pattern, ParsePosition pos) {
      int start = pos.getIndex();
      this.seekNonWs(pattern, pos);
      StringBuffer result = new StringBuffer();

      boolean error;
      for(error = false; !error && pos.getIndex() < pattern.length(); this.next(pos)) {
         char c = pattern.charAt(pos.getIndex());
         if (Character.isWhitespace(c)) {
            this.seekNonWs(pattern, pos);
            c = pattern.charAt(pos.getIndex());
            if (c != ',' && c != '}') {
               error = true;
               continue;
            }
         }

         if ((c == ',' || c == '}') && result.length() > 0) {
            try {
               return Integer.parseInt(result.toString());
            } catch (NumberFormatException var8) {
            }
         }

         error = !Character.isDigit(c);
         result.append(c);
      }

      if (error) {
         throw new IllegalArgumentException("Invalid format argument index at position " + start + ": " + pattern.substring(start, pos.getIndex()));
      } else {
         throw new IllegalArgumentException("Unterminated format element at position " + start);
      }
   }

   private String parseFormatDescription(String pattern, ParsePosition pos) {
      int start = pos.getIndex();
      this.seekNonWs(pattern, pos);
      int text = pos.getIndex();

      for(int depth = 1; pos.getIndex() < pattern.length(); this.next(pos)) {
         switch(pattern.charAt(pos.getIndex())) {
            case '\'':
               this.getQuotedString(pattern, pos, false);
               break;
            case '{':
               ++depth;
               break;
            case '}':
               if (--depth == 0) {
                  return pattern.substring(text, pos.getIndex());
               }
         }
      }

      throw new IllegalArgumentException("Unterminated format element at position " + start);
   }

   private String insertFormats(String pattern, ArrayList customPatterns) {
      if (!this.containsElements(customPatterns)) {
         return pattern;
      } else {
         StringBuffer sb = new StringBuffer(pattern.length() * 2);
         ParsePosition pos = new ParsePosition(0);
         int fe = -1;
         int depth = 0;

         while(pos.getIndex() < pattern.length()) {
            char c = pattern.charAt(pos.getIndex());
            switch(c) {
               case '\'':
                  this.appendQuotedString(pattern, pos, sb, false);
                  break;
               case '{':
                  if (++depth == 1) {
                     ++fe;
                     sb.append('{').append(this.readArgumentIndex(pattern, this.next(pos)));
                     String customPattern = (String)customPatterns.get(fe);
                     if (customPattern != null) {
                        sb.append(',').append(customPattern);
                     }
                  }
                  break;
               case '}':
                  --depth;
               default:
                  sb.append(c);
                  this.next(pos);
            }
         }

         return sb.toString();
      }
   }

   private void seekNonWs(String pattern, ParsePosition pos) {
      int len = 0;
      char[] buffer = pattern.toCharArray();

      do {
         len = StrMatcher.splitMatcher().isMatch(buffer, pos.getIndex());
         pos.setIndex(pos.getIndex() + len);
      } while(len > 0 && pos.getIndex() < pattern.length());
   }

   private ParsePosition next(ParsePosition pos) {
      pos.setIndex(pos.getIndex() + 1);
      return pos;
   }

   private StringBuffer appendQuotedString(String pattern, ParsePosition pos, StringBuffer appendTo, boolean escapingOn) {
      int start = pos.getIndex();
      char[] c = pattern.toCharArray();
      if (escapingOn && c[start] == '\'') {
         return appendTo == null ? null : appendTo.append('\'');
      } else {
         int lastHold = start;

         for(int i = pos.getIndex(); i < pattern.length(); ++i) {
            if (escapingOn && pattern.substring(i).startsWith("''")) {
               appendTo.append(c, lastHold, pos.getIndex() - lastHold).append('\'');
               pos.setIndex(i + "''".length());
               lastHold = pos.getIndex();
            } else {
               switch(c[pos.getIndex()]) {
                  case '\'':
                     this.next(pos);
                     return appendTo == null ? null : appendTo.append(c, lastHold, pos.getIndex() - lastHold);
                  default:
                     this.next(pos);
               }
            }
         }

         throw new IllegalArgumentException("Unterminated quoted string at position " + start);
      }
   }

   private void getQuotedString(String pattern, ParsePosition pos, boolean escapingOn) {
      this.appendQuotedString(pattern, pos, null, escapingOn);
   }

   private boolean containsElements(Collection coll) {
      if (coll != null && coll.size() != 0) {
         Iterator iter = coll.iterator();

         while(iter.hasNext()) {
            if (iter.next() != null) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
