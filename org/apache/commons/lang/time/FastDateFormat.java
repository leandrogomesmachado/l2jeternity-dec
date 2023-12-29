package org.apache.commons.lang.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang.Validate;

public class FastDateFormat extends Format {
   private static final long serialVersionUID = 1L;
   public static final int FULL = 0;
   public static final int LONG = 1;
   public static final int MEDIUM = 2;
   public static final int SHORT = 3;
   private static String cDefaultPattern;
   private static final Map cInstanceCache = new HashMap(7);
   private static final Map cDateInstanceCache = new HashMap(7);
   private static final Map cTimeInstanceCache = new HashMap(7);
   private static final Map cDateTimeInstanceCache = new HashMap(7);
   private static final Map cTimeZoneDisplayCache = new HashMap(7);
   private final String mPattern;
   private final TimeZone mTimeZone;
   private final boolean mTimeZoneForced;
   private final Locale mLocale;
   private final boolean mLocaleForced;
   private transient FastDateFormat.Rule[] mRules;
   private transient int mMaxLengthEstimate;

   public static FastDateFormat getInstance() {
      return getInstance(getDefaultPattern(), null, null);
   }

   public static FastDateFormat getInstance(String pattern) {
      return getInstance(pattern, null, null);
   }

   public static FastDateFormat getInstance(String pattern, TimeZone timeZone) {
      return getInstance(pattern, timeZone, null);
   }

   public static FastDateFormat getInstance(String pattern, Locale locale) {
      return getInstance(pattern, null, locale);
   }

   public static synchronized FastDateFormat getInstance(String pattern, TimeZone timeZone, Locale locale) {
      FastDateFormat emptyFormat = new FastDateFormat(pattern, timeZone, locale);
      FastDateFormat format = (FastDateFormat)cInstanceCache.get(emptyFormat);
      if (format == null) {
         format = emptyFormat;
         emptyFormat.init();
         cInstanceCache.put(emptyFormat, emptyFormat);
      }

      return format;
   }

   public static FastDateFormat getDateInstance(int style) {
      return getDateInstance(style, null, null);
   }

   public static FastDateFormat getDateInstance(int style, Locale locale) {
      return getDateInstance(style, null, locale);
   }

   public static FastDateFormat getDateInstance(int style, TimeZone timeZone) {
      return getDateInstance(style, timeZone, null);
   }

   public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
      Object key = new Integer(style);
      if (timeZone != null) {
         key = new FastDateFormat.Pair(key, timeZone);
      }

      if (locale == null) {
         locale = Locale.getDefault();
      }

      Object var8 = new FastDateFormat.Pair(key, locale);
      FastDateFormat format = (FastDateFormat)cDateInstanceCache.get(var8);
      if (format == null) {
         try {
            SimpleDateFormat formatter = (SimpleDateFormat)DateFormat.getDateInstance(style, locale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, locale);
            cDateInstanceCache.put(var8, format);
         } catch (ClassCastException var7) {
            throw new IllegalArgumentException("No date pattern for locale: " + locale);
         }
      }

      return format;
   }

   public static FastDateFormat getTimeInstance(int style) {
      return getTimeInstance(style, null, null);
   }

   public static FastDateFormat getTimeInstance(int style, Locale locale) {
      return getTimeInstance(style, null, locale);
   }

   public static FastDateFormat getTimeInstance(int style, TimeZone timeZone) {
      return getTimeInstance(style, timeZone, null);
   }

   public static synchronized FastDateFormat getTimeInstance(int style, TimeZone timeZone, Locale locale) {
      Object key = new Integer(style);
      if (timeZone != null) {
         key = new FastDateFormat.Pair(key, timeZone);
      }

      if (locale != null) {
         key = new FastDateFormat.Pair(key, locale);
      }

      FastDateFormat format = (FastDateFormat)cTimeInstanceCache.get(key);
      if (format == null) {
         if (locale == null) {
            locale = Locale.getDefault();
         }

         try {
            SimpleDateFormat formatter = (SimpleDateFormat)DateFormat.getTimeInstance(style, locale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, locale);
            cTimeInstanceCache.put(key, format);
         } catch (ClassCastException var7) {
            throw new IllegalArgumentException("No date pattern for locale: " + locale);
         }
      }

      return format;
   }

   public static FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
      return getDateTimeInstance(dateStyle, timeStyle, null, null);
   }

   public static FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
      return getDateTimeInstance(dateStyle, timeStyle, null, locale);
   }

   public static FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone) {
      return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
   }

   public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
      Object key = new FastDateFormat.Pair(new Integer(dateStyle), new Integer(timeStyle));
      if (timeZone != null) {
         key = new FastDateFormat.Pair(key, timeZone);
      }

      if (locale == null) {
         locale = Locale.getDefault();
      }

      Object var9 = new FastDateFormat.Pair(key, locale);
      FastDateFormat format = (FastDateFormat)cDateTimeInstanceCache.get(var9);
      if (format == null) {
         try {
            SimpleDateFormat formatter = (SimpleDateFormat)DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
            String pattern = formatter.toPattern();
            format = getInstance(pattern, timeZone, locale);
            cDateTimeInstanceCache.put(var9, format);
         } catch (ClassCastException var8) {
            throw new IllegalArgumentException("No date time pattern for locale: " + locale);
         }
      }

      return format;
   }

   static synchronized String getTimeZoneDisplay(TimeZone tz, boolean daylight, int style, Locale locale) {
      Object key = new FastDateFormat.TimeZoneDisplayKey(tz, daylight, style, locale);
      String value = (String)cTimeZoneDisplayCache.get(key);
      if (value == null) {
         value = tz.getDisplayName(daylight, style, locale);
         cTimeZoneDisplayCache.put(key, value);
      }

      return value;
   }

   private static synchronized String getDefaultPattern() {
      if (cDefaultPattern == null) {
         cDefaultPattern = new SimpleDateFormat().toPattern();
      }

      return cDefaultPattern;
   }

   protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
      if (pattern == null) {
         throw new IllegalArgumentException("The pattern must not be null");
      } else {
         this.mPattern = pattern;
         this.mTimeZoneForced = timeZone != null;
         if (timeZone == null) {
            timeZone = TimeZone.getDefault();
         }

         this.mTimeZone = timeZone;
         this.mLocaleForced = locale != null;
         if (locale == null) {
            locale = Locale.getDefault();
         }

         this.mLocale = locale;
      }
   }

   protected void init() {
      List rulesList = this.parsePattern();
      this.mRules = rulesList.toArray(new FastDateFormat.Rule[rulesList.size()]);
      int len = 0;
      int i = this.mRules.length;

      while(--i >= 0) {
         len += this.mRules[i].estimateLength();
      }

      this.mMaxLengthEstimate = len;
   }

   protected List parsePattern() {
      DateFormatSymbols symbols = new DateFormatSymbols(this.mLocale);
      List rules = new ArrayList();
      String[] ERAs = symbols.getEras();
      String[] months = symbols.getMonths();
      String[] shortMonths = symbols.getShortMonths();
      String[] weekdays = symbols.getWeekdays();
      String[] shortWeekdays = symbols.getShortWeekdays();
      String[] AmPmStrings = symbols.getAmPmStrings();
      int length = this.mPattern.length();
      int[] indexRef = new int[1];

      for(int i = 0; i < length; ++i) {
         indexRef[0] = i;
         String token = this.parseToken(this.mPattern, indexRef);
         i = indexRef[0];
         int tokenLen = token.length();
         if (tokenLen == 0) {
            break;
         }

         char c = token.charAt(0);
         FastDateFormat.Rule rule;
         switch(c) {
            case '\'':
               String sub = token.substring(1);
               if (sub.length() == 1) {
                  rule = new FastDateFormat.CharacterLiteral(sub.charAt(0));
               } else {
                  rule = new FastDateFormat.StringLiteral(sub);
               }
               break;
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'A':
            case 'B':
            case 'C':
            case 'I':
            case 'J':
            case 'L':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'V':
            case 'X':
            case 'Y':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            case 'b':
            case 'c':
            case 'e':
            case 'f':
            case 'g':
            case 'i':
            case 'j':
            case 'l':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 't':
            case 'u':
            case 'v':
            case 'x':
            default:
               throw new IllegalArgumentException("Illegal pattern component: " + token);
            case 'D':
               rule = this.selectNumberRule(6, tokenLen);
               break;
            case 'E':
               rule = new FastDateFormat.TextField(7, tokenLen < 4 ? shortWeekdays : weekdays);
               break;
            case 'F':
               rule = this.selectNumberRule(8, tokenLen);
               break;
            case 'G':
               rule = new FastDateFormat.TextField(0, ERAs);
               break;
            case 'H':
               rule = this.selectNumberRule(11, tokenLen);
               break;
            case 'K':
               rule = this.selectNumberRule(10, tokenLen);
               break;
            case 'M':
               if (tokenLen >= 4) {
                  rule = new FastDateFormat.TextField(2, months);
               } else if (tokenLen == 3) {
                  rule = new FastDateFormat.TextField(2, shortMonths);
               } else if (tokenLen == 2) {
                  rule = FastDateFormat.TwoDigitMonthField.INSTANCE;
               } else {
                  rule = FastDateFormat.UnpaddedMonthField.INSTANCE;
               }
               break;
            case 'S':
               rule = this.selectNumberRule(14, tokenLen);
               break;
            case 'W':
               rule = this.selectNumberRule(4, tokenLen);
               break;
            case 'Z':
               if (tokenLen == 1) {
                  rule = FastDateFormat.TimeZoneNumberRule.INSTANCE_NO_COLON;
               } else {
                  rule = FastDateFormat.TimeZoneNumberRule.INSTANCE_COLON;
               }
               break;
            case 'a':
               rule = new FastDateFormat.TextField(9, AmPmStrings);
               break;
            case 'd':
               rule = this.selectNumberRule(5, tokenLen);
               break;
            case 'h':
               rule = new FastDateFormat.TwelveHourField(this.selectNumberRule(10, tokenLen));
               break;
            case 'k':
               rule = new FastDateFormat.TwentyFourHourField(this.selectNumberRule(11, tokenLen));
               break;
            case 'm':
               rule = this.selectNumberRule(12, tokenLen);
               break;
            case 's':
               rule = this.selectNumberRule(13, tokenLen);
               break;
            case 'w':
               rule = this.selectNumberRule(3, tokenLen);
               break;
            case 'y':
               if (tokenLen >= 4) {
                  rule = this.selectNumberRule(1, tokenLen);
               } else {
                  rule = FastDateFormat.TwoDigitYearField.INSTANCE;
               }
               break;
            case 'z':
               if (tokenLen >= 4) {
                  rule = new FastDateFormat.TimeZoneNameRule(this.mTimeZone, this.mTimeZoneForced, this.mLocale, 1);
               } else {
                  rule = new FastDateFormat.TimeZoneNameRule(this.mTimeZone, this.mTimeZoneForced, this.mLocale, 0);
               }
         }

         rules.add(rule);
      }

      return rules;
   }

   protected String parseToken(String pattern, int[] indexRef) {
      StringBuffer buf = new StringBuffer();
      int i = indexRef[0];
      int length = pattern.length();
      char c = pattern.charAt(i);
      if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
         buf.append(c);

         while(i + 1 < length) {
            char peek = pattern.charAt(i + 1);
            if (peek != c) {
               break;
            }

            buf.append(c);
            ++i;
         }
      } else {
         buf.append('\'');

         for(boolean inLiteral = false; i < length; ++i) {
            c = pattern.charAt(i);
            if (c == '\'') {
               if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                  ++i;
                  buf.append(c);
               } else {
                  inLiteral = !inLiteral;
               }
            } else {
               if (!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                  --i;
                  break;
               }

               buf.append(c);
            }
         }
      }

      indexRef[0] = i;
      return buf.toString();
   }

   protected FastDateFormat.NumberRule selectNumberRule(int field, int padding) {
      switch(padding) {
         case 1:
            return new FastDateFormat.UnpaddedNumberField(field);
         case 2:
            return new FastDateFormat.TwoDigitNumberField(field);
         default:
            return new FastDateFormat.PaddedNumberField(field, padding);
      }
   }

   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      if (obj instanceof Date) {
         return this.format((Date)obj, toAppendTo);
      } else if (obj instanceof Calendar) {
         return this.format((Calendar)obj, toAppendTo);
      } else if (obj instanceof Long) {
         return this.format((Long)obj, toAppendTo);
      } else {
         throw new IllegalArgumentException("Unknown class: " + (obj == null ? "<null>" : obj.getClass().getName()));
      }
   }

   public String format(long millis) {
      return this.format(new Date(millis));
   }

   public String format(Date date) {
      Calendar c = new GregorianCalendar(this.mTimeZone);
      c.setTime(date);
      return this.applyRules(c, new StringBuffer(this.mMaxLengthEstimate)).toString();
   }

   public String format(Calendar calendar) {
      return this.format(calendar, new StringBuffer(this.mMaxLengthEstimate)).toString();
   }

   public StringBuffer format(long millis, StringBuffer buf) {
      return this.format(new Date(millis), buf);
   }

   public StringBuffer format(Date date, StringBuffer buf) {
      Calendar c = new GregorianCalendar(this.mTimeZone);
      c.setTime(date);
      return this.applyRules(c, buf);
   }

   public StringBuffer format(Calendar calendar, StringBuffer buf) {
      if (this.mTimeZoneForced) {
         calendar = (Calendar)calendar.clone();
         calendar.setTimeZone(this.mTimeZone);
      }

      return this.applyRules(calendar, buf);
   }

   protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
      FastDateFormat.Rule[] rules = this.mRules;
      int len = this.mRules.length;

      for(int i = 0; i < len; ++i) {
         rules[i].appendTo(buf, calendar);
      }

      return buf;
   }

   public Object parseObject(String source, ParsePosition pos) {
      pos.setIndex(0);
      pos.setErrorIndex(0);
      return null;
   }

   public String getPattern() {
      return this.mPattern;
   }

   public TimeZone getTimeZone() {
      return this.mTimeZone;
   }

   public boolean getTimeZoneOverridesCalendar() {
      return this.mTimeZoneForced;
   }

   public Locale getLocale() {
      return this.mLocale;
   }

   public int getMaxLengthEstimate() {
      return this.mMaxLengthEstimate;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof FastDateFormat)) {
         return false;
      } else {
         FastDateFormat other = (FastDateFormat)obj;
         return (this.mPattern == other.mPattern || this.mPattern.equals(other.mPattern))
            && (this.mTimeZone == other.mTimeZone || this.mTimeZone.equals(other.mTimeZone))
            && (this.mLocale == other.mLocale || this.mLocale.equals(other.mLocale))
            && this.mTimeZoneForced == other.mTimeZoneForced
            && this.mLocaleForced == other.mLocaleForced;
      }
   }

   public int hashCode() {
      int total = 0;
      total += this.mPattern.hashCode();
      total += this.mTimeZone.hashCode();
      total += this.mTimeZoneForced ? 1 : 0;
      total += this.mLocale.hashCode();
      return total + (this.mLocaleForced ? 1 : 0);
   }

   public String toString() {
      return "FastDateFormat[" + this.mPattern + "]";
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this.init();
   }

   private static class CharacterLiteral implements FastDateFormat.Rule {
      private final char mValue;

      CharacterLiteral(char value) {
         this.mValue = value;
      }

      public int estimateLength() {
         return 1;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         buffer.append(this.mValue);
      }
   }

   private interface NumberRule extends FastDateFormat.Rule {
      void appendTo(StringBuffer var1, int var2);
   }

   private static class PaddedNumberField implements FastDateFormat.NumberRule {
      private final int mField;
      private final int mSize;

      PaddedNumberField(int field, int size) {
         if (size < 3) {
            throw new IllegalArgumentException();
         } else {
            this.mField = field;
            this.mSize = size;
         }
      }

      public int estimateLength() {
         return 4;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(this.mField));
      }

      public final void appendTo(StringBuffer buffer, int value) {
         if (value < 100) {
            int i = this.mSize;

            while(--i >= 2) {
               buffer.append('0');
            }

            buffer.append((char)(value / 10 + 48));
            buffer.append((char)(value % 10 + 48));
         } else {
            int digits;
            if (value < 1000) {
               digits = 3;
            } else {
               Validate.isTrue(value > -1, "Negative values should not be possible", (long)value);
               digits = Integer.toString(value).length();
            }

            int i = this.mSize;

            while(--i >= digits) {
               buffer.append('0');
            }

            buffer.append(Integer.toString(value));
         }
      }
   }

   private static class Pair {
      private final Object mObj1;
      private final Object mObj2;

      public Pair(Object obj1, Object obj2) {
         this.mObj1 = obj1;
         this.mObj2 = obj2;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof FastDateFormat.Pair)) {
            return false;
         } else {
            FastDateFormat.Pair key = (FastDateFormat.Pair)obj;
            return (this.mObj1 == null ? key.mObj1 == null : this.mObj1.equals(key.mObj1))
               && (this.mObj2 == null ? key.mObj2 == null : this.mObj2.equals(key.mObj2));
         }
      }

      public int hashCode() {
         return (this.mObj1 == null ? 0 : this.mObj1.hashCode()) + (this.mObj2 == null ? 0 : this.mObj2.hashCode());
      }

      public String toString() {
         return "[" + this.mObj1 + ':' + this.mObj2 + ']';
      }
   }

   private interface Rule {
      int estimateLength();

      void appendTo(StringBuffer var1, Calendar var2);
   }

   private static class StringLiteral implements FastDateFormat.Rule {
      private final String mValue;

      StringLiteral(String value) {
         this.mValue = value;
      }

      public int estimateLength() {
         return this.mValue.length();
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         buffer.append(this.mValue);
      }
   }

   private static class TextField implements FastDateFormat.Rule {
      private final int mField;
      private final String[] mValues;

      TextField(int field, String[] values) {
         this.mField = field;
         this.mValues = values;
      }

      public int estimateLength() {
         int max = 0;
         int i = this.mValues.length;

         while(--i >= 0) {
            int len = this.mValues[i].length();
            if (len > max) {
               max = len;
            }
         }

         return max;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         buffer.append(this.mValues[calendar.get(this.mField)]);
      }
   }

   private static class TimeZoneDisplayKey {
      private final TimeZone mTimeZone;
      private final int mStyle;
      private final Locale mLocale;

      TimeZoneDisplayKey(TimeZone timeZone, boolean daylight, int style, Locale locale) {
         this.mTimeZone = timeZone;
         if (daylight) {
            style |= Integer.MIN_VALUE;
         }

         this.mStyle = style;
         this.mLocale = locale;
      }

      public int hashCode() {
         return this.mStyle * 31 + this.mLocale.hashCode();
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof FastDateFormat.TimeZoneDisplayKey)) {
            return false;
         } else {
            FastDateFormat.TimeZoneDisplayKey other = (FastDateFormat.TimeZoneDisplayKey)obj;
            return this.mTimeZone.equals(other.mTimeZone) && this.mStyle == other.mStyle && this.mLocale.equals(other.mLocale);
         }
      }
   }

   private static class TimeZoneNameRule implements FastDateFormat.Rule {
      private final TimeZone mTimeZone;
      private final boolean mTimeZoneForced;
      private final Locale mLocale;
      private final int mStyle;
      private final String mStandard;
      private final String mDaylight;

      TimeZoneNameRule(TimeZone timeZone, boolean timeZoneForced, Locale locale, int style) {
         this.mTimeZone = timeZone;
         this.mTimeZoneForced = timeZoneForced;
         this.mLocale = locale;
         this.mStyle = style;
         if (timeZoneForced) {
            this.mStandard = FastDateFormat.getTimeZoneDisplay(timeZone, false, style, locale);
            this.mDaylight = FastDateFormat.getTimeZoneDisplay(timeZone, true, style, locale);
         } else {
            this.mStandard = null;
            this.mDaylight = null;
         }
      }

      public int estimateLength() {
         if (this.mTimeZoneForced) {
            return Math.max(this.mStandard.length(), this.mDaylight.length());
         } else {
            return this.mStyle == 0 ? 4 : 40;
         }
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         if (this.mTimeZoneForced) {
            if (this.mTimeZone.useDaylightTime() && calendar.get(16) != 0) {
               buffer.append(this.mDaylight);
            } else {
               buffer.append(this.mStandard);
            }
         } else {
            TimeZone timeZone = calendar.getTimeZone();
            if (timeZone.useDaylightTime() && calendar.get(16) != 0) {
               buffer.append(FastDateFormat.getTimeZoneDisplay(timeZone, true, this.mStyle, this.mLocale));
            } else {
               buffer.append(FastDateFormat.getTimeZoneDisplay(timeZone, false, this.mStyle, this.mLocale));
            }
         }
      }
   }

   private static class TimeZoneNumberRule implements FastDateFormat.Rule {
      static final FastDateFormat.TimeZoneNumberRule INSTANCE_COLON = new FastDateFormat.TimeZoneNumberRule(true);
      static final FastDateFormat.TimeZoneNumberRule INSTANCE_NO_COLON = new FastDateFormat.TimeZoneNumberRule(false);
      final boolean mColon;

      TimeZoneNumberRule(boolean colon) {
         this.mColon = colon;
      }

      public int estimateLength() {
         return 5;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         int offset = calendar.get(15) + calendar.get(16);
         if (offset < 0) {
            buffer.append('-');
            offset = -offset;
         } else {
            buffer.append('+');
         }

         int hours = offset / 3600000;
         buffer.append((char)(hours / 10 + 48));
         buffer.append((char)(hours % 10 + 48));
         if (this.mColon) {
            buffer.append(':');
         }

         int minutes = offset / 60000 - 60 * hours;
         buffer.append((char)(minutes / 10 + 48));
         buffer.append((char)(minutes % 10 + 48));
      }
   }

   private static class TwelveHourField implements FastDateFormat.NumberRule {
      private final FastDateFormat.NumberRule mRule;

      TwelveHourField(FastDateFormat.NumberRule rule) {
         this.mRule = rule;
      }

      public int estimateLength() {
         return this.mRule.estimateLength();
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         int value = calendar.get(10);
         if (value == 0) {
            value = calendar.getLeastMaximum(10) + 1;
         }

         this.mRule.appendTo(buffer, value);
      }

      public void appendTo(StringBuffer buffer, int value) {
         this.mRule.appendTo(buffer, value);
      }
   }

   private static class TwentyFourHourField implements FastDateFormat.NumberRule {
      private final FastDateFormat.NumberRule mRule;

      TwentyFourHourField(FastDateFormat.NumberRule rule) {
         this.mRule = rule;
      }

      public int estimateLength() {
         return this.mRule.estimateLength();
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         int value = calendar.get(11);
         if (value == 0) {
            value = calendar.getMaximum(11) + 1;
         }

         this.mRule.appendTo(buffer, value);
      }

      public void appendTo(StringBuffer buffer, int value) {
         this.mRule.appendTo(buffer, value);
      }
   }

   private static class TwoDigitMonthField implements FastDateFormat.NumberRule {
      static final FastDateFormat.TwoDigitMonthField INSTANCE = new FastDateFormat.TwoDigitMonthField();

      TwoDigitMonthField() {
      }

      public int estimateLength() {
         return 2;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(2) + 1);
      }

      public final void appendTo(StringBuffer buffer, int value) {
         buffer.append((char)(value / 10 + 48));
         buffer.append((char)(value % 10 + 48));
      }
   }

   private static class TwoDigitNumberField implements FastDateFormat.NumberRule {
      private final int mField;

      TwoDigitNumberField(int field) {
         this.mField = field;
      }

      public int estimateLength() {
         return 2;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(this.mField));
      }

      public final void appendTo(StringBuffer buffer, int value) {
         if (value < 100) {
            buffer.append((char)(value / 10 + 48));
            buffer.append((char)(value % 10 + 48));
         } else {
            buffer.append(Integer.toString(value));
         }
      }
   }

   private static class TwoDigitYearField implements FastDateFormat.NumberRule {
      static final FastDateFormat.TwoDigitYearField INSTANCE = new FastDateFormat.TwoDigitYearField();

      TwoDigitYearField() {
      }

      public int estimateLength() {
         return 2;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(1) % 100);
      }

      public final void appendTo(StringBuffer buffer, int value) {
         buffer.append((char)(value / 10 + 48));
         buffer.append((char)(value % 10 + 48));
      }
   }

   private static class UnpaddedMonthField implements FastDateFormat.NumberRule {
      static final FastDateFormat.UnpaddedMonthField INSTANCE = new FastDateFormat.UnpaddedMonthField();

      UnpaddedMonthField() {
      }

      public int estimateLength() {
         return 2;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(2) + 1);
      }

      public final void appendTo(StringBuffer buffer, int value) {
         if (value < 10) {
            buffer.append((char)(value + 48));
         } else {
            buffer.append((char)(value / 10 + 48));
            buffer.append((char)(value % 10 + 48));
         }
      }
   }

   private static class UnpaddedNumberField implements FastDateFormat.NumberRule {
      static final FastDateFormat.UnpaddedNumberField INSTANCE_YEAR = new FastDateFormat.UnpaddedNumberField(1);
      private final int mField;

      UnpaddedNumberField(int field) {
         this.mField = field;
      }

      public int estimateLength() {
         return 4;
      }

      public void appendTo(StringBuffer buffer, Calendar calendar) {
         this.appendTo(buffer, calendar.get(this.mField));
      }

      public final void appendTo(StringBuffer buffer, int value) {
         if (value < 10) {
            buffer.append((char)(value + 48));
         } else if (value < 100) {
            buffer.append((char)(value / 10 + 48));
            buffer.append((char)(value % 10 + 48));
         } else {
            buffer.append(Integer.toString(value));
         }
      }
   }
}
