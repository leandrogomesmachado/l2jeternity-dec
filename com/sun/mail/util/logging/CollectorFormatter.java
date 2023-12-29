package com.sun.mail.util.logging;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class CollectorFormatter extends Formatter {
   private static final long INIT_TIME = System.currentTimeMillis();
   private final String fmt;
   private final Formatter formatter;
   private final Comparator<? super LogRecord> comparator;
   private LogRecord last;
   private long count;
   private long thrown;
   private long minMillis;
   private long maxMillis;

   public CollectorFormatter() {
      String p = this.getClass().getName();
      this.fmt = this.initFormat(p);
      this.formatter = this.initFormatter(p);
      this.comparator = this.initComparator(p);
      this.reset();
   }

   public CollectorFormatter(String format) {
      String p = this.getClass().getName();
      this.fmt = format == null ? this.initFormat(p) : format;
      this.formatter = this.initFormatter(p);
      this.comparator = this.initComparator(p);
      this.reset();
   }

   public CollectorFormatter(String format, Formatter f, Comparator<? super LogRecord> c) {
      String p = this.getClass().getName();
      this.fmt = format == null ? this.initFormat(p) : format;
      this.formatter = f;
      this.comparator = c;
      this.reset();
   }

   @Override
   public String format(LogRecord record) {
      if (record == null) {
         throw new NullPointerException();
      } else {
         boolean accepted;
         do {
            LogRecord peek = this.peek();
            LogRecord update = this.apply(peek != null ? peek : record, record);
            if (peek != update) {
               update.getSourceMethodName();
               accepted = this.acceptAndUpdate(peek, update);
            } else {
               accepted = true;
               this.accept(record);
            }
         } while(!accepted);

         return "";
      }
   }

   @Override
   public String getTail(Handler h) {
      return this.formatRecord(h, true);
   }

   @Override
   public String toString() {
      String result;
      try {
         result = this.formatRecord((Handler)null, false);
      } catch (RuntimeException var3) {
         result = super.toString();
      }

      return result;
   }

   protected LogRecord apply(LogRecord t, LogRecord u) {
      if (t == null || u == null) {
         throw new NullPointerException();
      } else if (this.comparator != null) {
         return this.comparator.compare(t, u) >= 0 ? t : u;
      } else {
         return u;
      }
   }

   private synchronized void accept(LogRecord record) {
      long millis = record.getMillis();
      this.minMillis = Math.min(this.minMillis, millis);
      this.maxMillis = Math.max(this.maxMillis, millis);
      ++this.count;
      if (record.getThrown() != null) {
         ++this.thrown;
      }
   }

   private synchronized void reset() {
      this.last = null;
      this.count = 0L;
      this.thrown = 0L;
      this.minMillis = Long.MAX_VALUE;
      this.maxMillis = Long.MIN_VALUE;
   }

   private String formatRecord(Handler h, boolean reset) {
      LogRecord record;
      long c;
      long t;
      long msl;
      long msh;
      synchronized(this) {
         record = this.last;
         c = this.count;
         t = this.thrown;
         msl = this.minMillis;
         msh = this.maxMillis;
         if (reset) {
            this.reset();
         }
      }

      if (c == 0L) {
         msl = INIT_TIME;
         msh = System.currentTimeMillis();
      }

      Formatter f = this.formatter;
      String head;
      String msg;
      String tail;
      if (f != null) {
         synchronized(f) {
            head = f.getHead(h);
            msg = record != null ? f.format(record) : "";
            tail = f.getTail(h);
         }
      } else {
         tail = "";
         msg = "";
         head = "";
      }

      Locale l = null;
      if (record != null) {
         ResourceBundle rb = record.getResourceBundle();
         l = rb == null ? null : rb.getLocale();
      }

      MessageFormat mf;
      if (l == null) {
         mf = new MessageFormat(this.fmt);
      } else {
         mf = new MessageFormat(this.fmt, l);
      }

      return mf.format(new Object[]{this.finish(head), this.finish(msg), this.finish(tail), c, c - 1L, t, c - t, msl, msh});
   }

   protected String finish(String s) {
      return s.trim();
   }

   private synchronized LogRecord peek() {
      return this.last;
   }

   private synchronized boolean acceptAndUpdate(LogRecord e, LogRecord u) {
      if (e == this.last) {
         this.accept(u);
         this.last = u;
         return true;
      } else {
         return false;
      }
   }

   private String initFormat(String p) {
      LogManager m = LogManagerProperties.getLogManager();
      String v = m.getProperty(p.concat(".format"));
      if (v == null || v.length() == 0) {
         v = "{0}{1}{2}{4,choice,-1#|0#|0<... {4,number,integer} more}\n";
      }

      return v;
   }

   private Formatter initFormatter(String p) {
      LogManager m = LogManagerProperties.getLogManager();
      String v = m.getProperty(p.concat(".formatter"));
      Formatter f;
      if (v == null || v.length() == 0) {
         f = Formatter.class.cast(new CompactFormatter());
      } else if (!"null".equalsIgnoreCase(v)) {
         try {
            f = LogManagerProperties.newFormatter(v);
         } catch (RuntimeException var6) {
            throw var6;
         } catch (Exception var7) {
            throw new UndeclaredThrowableException(var7);
         }
      } else {
         f = null;
      }

      return f;
   }

   private Comparator<? super LogRecord> initComparator(String p) {
      LogManager m = LogManagerProperties.getLogManager();
      String name = m.getProperty(p.concat(".comparator"));
      String reverse = m.getProperty(p.concat(".comparator.reverse"));

      try {
         Comparator<? super LogRecord> c;
         if (name != null && name.length() != 0) {
            if (!"null".equalsIgnoreCase(name)) {
               c = LogManagerProperties.newComparator(name);
               if (Boolean.parseBoolean(reverse)) {
                  assert c != null;

                  c = LogManagerProperties.reverseOrder(c);
               }
            } else {
               if (reverse != null) {
                  throw new IllegalArgumentException("No comparator to reverse.");
               }

               c = null;
            }
         } else {
            if (reverse != null) {
               throw new IllegalArgumentException("No comparator to reverse.");
            }

            c = Comparator.class.cast(SeverityComparator.getInstance());
         }

         return c;
      } catch (RuntimeException var7) {
         throw var7;
      } catch (Exception var8) {
         throw new UndeclaredThrowableException(var8);
      }
   }
}
