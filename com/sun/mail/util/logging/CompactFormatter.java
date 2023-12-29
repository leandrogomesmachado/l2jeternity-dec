package com.sun.mail.util.logging;

import java.util.Collections;
import java.util.Date;
import java.util.Formattable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class CompactFormatter extends Formatter {
   private final String fmt;

   public CompactFormatter() {
      String p = this.getClass().getName();
      this.fmt = this.initFormat(p);
   }

   public CompactFormatter(String format) {
      String p = this.getClass().getName();
      this.fmt = format == null ? this.initFormat(p) : format;
   }

   @Override
   public String format(LogRecord record) {
      ResourceBundle rb = record.getResourceBundle();
      Locale l = rb == null ? null : rb.getLocale();
      String msg = this.formatMessage(record);
      String thrown = this.formatThrown(record);
      Object[] params = new Object[]{
         new Date(record.getMillis()),
         this.formatSource(record),
         this.formatLoggerName(record),
         this.formatLevel(record),
         msg,
         thrown,
         new CompactFormatter.Alternate(msg, thrown),
         new CompactFormatter.Alternate(thrown, msg)
      };
      return String.format(l, this.fmt, params);
   }

   @Override
   public String formatMessage(LogRecord record) {
      String msg = super.formatMessage(record);
      msg = replaceClassName(msg, record.getThrown());
      return replaceClassName(msg, record.getParameters());
   }

   public String formatMessage(Throwable t) {
      return t != null ? replaceClassName(this.apply(t).getMessage(), t) : "";
   }

   public String formatLevel(LogRecord record) {
      return record.getLevel().getLocalizedName();
   }

   public String formatSource(LogRecord record) {
      String source = record.getSourceClassName();
      if (source != null) {
         if (record.getSourceMethodName() != null) {
            source = simpleClassName(source) + " " + record.getSourceMethodName();
         } else {
            source = simpleClassName(source);
         }
      } else {
         source = simpleClassName(record.getLoggerName());
      }

      return source;
   }

   public String formatLoggerName(LogRecord record) {
      return simpleClassName(record.getLoggerName());
   }

   public String formatThrown(LogRecord record) {
      Throwable t = record.getThrown();
      String msg;
      if (t != null) {
         Throwable root = this.apply(t);
         if (root != null) {
            msg = this.formatMessage(t);
            String site = this.formatBackTrace(record);
            msg = root.getClass().getSimpleName() + ": " + msg + (isNullOrSpaces(site) ? "" : ' ' + site);
         } else {
            msg = "";
         }
      } else {
         msg = "";
      }

      return msg;
   }

   public String formatBackTrace(LogRecord record) {
      String site = "";
      Throwable t = record.getThrown();
      if (t != null) {
         Throwable root = this.apply(t);
         if (root != null) {
            site = this.findAndFormat(root.getStackTrace());
            if (isNullOrSpaces(site)) {
               int limit = 0;

               for(Throwable c = t; c != null; c = c.getCause()) {
                  site = this.findAndFormat(c.getStackTrace());
                  if (!isNullOrSpaces(site) || ++limit == 65536) {
                     break;
                  }
               }
            }
         }
      }

      return site;
   }

   private String findAndFormat(StackTraceElement[] trace) {
      String site = "";

      for(StackTraceElement s : trace) {
         if (!this.ignore(s)) {
            site = this.formatStackTraceElement(s);
            break;
         }
      }

      if (isNullOrSpaces(site)) {
         for(StackTraceElement s : trace) {
            if (!this.defaultIgnore(s)) {
               site = this.formatStackTraceElement(s);
               break;
            }
         }
      }

      return site;
   }

   private String formatStackTraceElement(StackTraceElement s) {
      String v = simpleClassName(s.getClassName());
      String result;
      if (v != null) {
         result = s.toString().replace(s.getClassName(), v);
      } else {
         result = s.toString();
      }

      v = simpleFileName(s.getFileName());
      if (v != null && result.startsWith(v)) {
         result = result.replace(s.getFileName(), "");
      }

      return result;
   }

   protected Throwable apply(Throwable t) {
      return SeverityComparator.getInstance().apply(t);
   }

   protected boolean ignore(StackTraceElement s) {
      return this.isUnknown(s) || this.defaultIgnore(s);
   }

   protected String toAlternate(String s) {
      return s != null ? s.replaceAll("[\\x00-\\x1F\\x7F]+", "") : null;
   }

   private boolean defaultIgnore(StackTraceElement s) {
      return this.isSynthetic(s) || this.isStaticUtility(s) || this.isReflection(s);
   }

   private boolean isStaticUtility(StackTraceElement s) {
      try {
         return LogManagerProperties.isStaticUtilityClass(s.getClassName());
      } catch (RuntimeException var3) {
      } catch (Exception var4) {
      } catch (LinkageError var5) {
      }

      return !s.getClassName().endsWith("es") && s.getClassName().endsWith("s") || s.getClassName().contains("Util");
   }

   private boolean isSynthetic(StackTraceElement s) {
      return s.getMethodName().indexOf(36) > -1;
   }

   private boolean isUnknown(StackTraceElement s) {
      return s.getLineNumber() < 0;
   }

   private boolean isReflection(StackTraceElement s) {
      try {
         return LogManagerProperties.isReflectionClass(s.getClassName());
      } catch (RuntimeException var3) {
      } catch (Exception var4) {
      } catch (LinkageError var5) {
      }

      return s.getClassName().startsWith("java.lang.reflect.") || s.getClassName().startsWith("sun.reflect.");
   }

   private String initFormat(String p) {
      LogManager m = LogManagerProperties.getLogManager();
      String v = m.getProperty(p.concat(".format"));
      if (isNullOrSpaces(v)) {
         v = "%7$#.160s%n";
      }

      return v;
   }

   private static String replaceClassName(String msg, Throwable t) {
      if (!isNullOrSpaces(msg)) {
         int limit = 0;

         for(Throwable c = t; c != null; c = c.getCause()) {
            Class<?> k = c.getClass();
            msg = msg.replace(k.getName(), k.getSimpleName());
            if (++limit == 65536) {
               break;
            }
         }
      }

      return msg;
   }

   private static String replaceClassName(String msg, Object[] p) {
      if (!isNullOrSpaces(msg) && p != null) {
         for(Object o : p) {
            if (o != null) {
               Class<?> k = o.getClass();
               msg = msg.replace(k.getName(), k.getSimpleName());
            }
         }
      }

      return msg;
   }

   private static String simpleClassName(String name) {
      if (name != null) {
         int index = name.lastIndexOf(46);
         name = index > -1 ? name.substring(index + 1) : name;
      }

      return name;
   }

   private static String simpleFileName(String name) {
      if (name != null) {
         int index = name.lastIndexOf(46);
         name = index > -1 ? name.substring(0, index) : name;
      }

      return name;
   }

   private static boolean isNullOrSpaces(String s) {
      return s == null || s.trim().length() == 0;
   }

   private class Alternate implements Formattable {
      private final String left;
      private final String right;

      Alternate(String left, String right) {
         this.left = String.valueOf(left);
         this.right = String.valueOf(right);
      }

      @Override
      public void formatTo(java.util.Formatter formatter, int flags, int width, int precision) {
         String l = this.left;
         String r = this.right;
         if ((flags & 2) == 2) {
            l = l.toUpperCase(formatter.locale());
            r = r.toUpperCase(formatter.locale());
         }

         if ((flags & 4) == 4) {
            l = CompactFormatter.this.toAlternate(l);
            r = CompactFormatter.this.toAlternate(r);
         }

         if (precision <= 0) {
            precision = Integer.MAX_VALUE;
         }

         int fence = Math.min(l.length(), precision);
         if (fence > precision >> 1) {
            fence = Math.max(fence - r.length(), fence >> 1);
         }

         if (fence > 0) {
            if (fence > l.length() && Character.isHighSurrogate(l.charAt(fence - 1))) {
               --fence;
            }

            l = l.substring(0, fence);
         }

         r = r.substring(0, Math.min(precision - fence, r.length()));
         if (width > 0) {
            int half = width >> 1;
            if (l.length() < half) {
               l = this.pad(flags, l, half);
            }

            if (r.length() < half) {
               r = this.pad(flags, r, half);
            }
         }

         Object[] empty = Collections.emptySet().toArray();
         formatter.format(l, empty);
         if (l.length() != 0 && r.length() != 0) {
            formatter.format("|", empty);
         }

         formatter.format(r, empty);
      }

      private String pad(int flags, String s, int length) {
         int padding = length - s.length();
         StringBuilder b = new StringBuilder(length);
         if ((flags & 1) == 1) {
            for(int i = 0; i < padding; ++i) {
               b.append(' ');
            }

            b.append(s);
         } else {
            b.append(s);

            for(int i = 0; i < padding; ++i) {
               b.append(' ');
            }
         }

         return b.toString();
      }
   }
}
