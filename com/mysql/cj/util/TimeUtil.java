package com.mysql.cj.util;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.InvalidConnectionAttributeException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;

public class TimeUtil {
   static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
   private static final String TIME_ZONE_MAPPINGS_RESOURCE = "/com/mysql/cj/util/TimeZoneMapping.properties";
   private static Properties timeZoneMappings = null;
   protected static final Method systemNanoTimeMethod;

   public static boolean nanoTimeAvailable() {
      return systemNanoTimeMethod != null;
   }

   public static long getCurrentTimeNanosOrMillis() {
      if (systemNanoTimeMethod != null) {
         try {
            return systemNanoTimeMethod.invoke(null, (Object[])null);
         } catch (IllegalArgumentException var1) {
         } catch (IllegalAccessException var2) {
         } catch (InvocationTargetException var3) {
         }
      }

      return System.currentTimeMillis();
   }

   public static String getCanonicalTimezone(String timezoneStr, ExceptionInterceptor exceptionInterceptor) {
      if (timezoneStr == null) {
         return null;
      } else {
         timezoneStr = timezoneStr.trim();
         if (timezoneStr.length() > 2 && (timezoneStr.charAt(0) == '+' || timezoneStr.charAt(0) == '-') && Character.isDigit(timezoneStr.charAt(1))) {
            return "GMT" + timezoneStr;
         } else {
            synchronized(TimeUtil.class) {
               if (timeZoneMappings == null) {
                  loadTimeZoneMappings(exceptionInterceptor);
               }
            }

            String canonicalTz;
            if ((canonicalTz = timeZoneMappings.getProperty(timezoneStr)) != null) {
               return canonicalTz;
            } else {
               throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
                  InvalidConnectionAttributeException.class,
                  Messages.getString("TimeUtil.UnrecognizedTimezoneId", new Object[]{timezoneStr}),
                  exceptionInterceptor
               );
            }
         }
      }
   }

   public static String formatNanos(int nanos, boolean usingMicros) {
      if (nanos > 999999999) {
         nanos %= 100000000;
      }

      if (usingMicros) {
         nanos /= 1000;
      }

      if (nanos == 0) {
         return "0";
      } else {
         int digitCount = usingMicros ? 6 : 9;
         String nanosString = Integer.toString(nanos);
         String zeroPadding = usingMicros ? "000000" : "000000000";
         nanosString = zeroPadding.substring(0, digitCount - nanosString.length()) + nanosString;
         int pos = digitCount - 1;

         while(nanosString.charAt(pos) == '0') {
            --pos;
         }

         return nanosString.substring(0, pos + 1);
      }
   }

   private static void loadTimeZoneMappings(ExceptionInterceptor exceptionInterceptor) {
      timeZoneMappings = new Properties();

      try {
         timeZoneMappings.load(TimeUtil.class.getResourceAsStream("/com/mysql/cj/util/TimeZoneMapping.properties"));
      } catch (IOException var5) {
         throw ExceptionFactory.createException(Messages.getString("TimeUtil.LoadTimeZoneMappingError"), exceptionInterceptor);
      }

      for(String tz : TimeZone.getAvailableIDs()) {
         if (!timeZoneMappings.containsKey(tz)) {
            timeZoneMappings.put(tz, tz);
         }
      }
   }

   public static Timestamp truncateFractionalSeconds(Timestamp timestamp) {
      Timestamp truncatedTimestamp = new Timestamp(timestamp.getTime());
      truncatedTimestamp.setNanos(0);
      return truncatedTimestamp;
   }

   public static final String getDateTimePattern(String dt, boolean toTime) throws IOException {
      int dtLength = dt != null ? dt.length() : 0;
      if (dtLength >= 8 && dtLength <= 10) {
         int dashCount = 0;
         boolean isDateOnly = true;

         for(int i = 0; i < dtLength; ++i) {
            char c = dt.charAt(i);
            if (!Character.isDigit(c) && c != '-') {
               isDateOnly = false;
               break;
            }

            if (c == '-') {
               ++dashCount;
            }
         }

         if (isDateOnly && dashCount == 2) {
            return "yyyy-MM-dd";
         }
      }

      boolean colonsOnly = true;

      for(int i = 0; i < dtLength; ++i) {
         char c = dt.charAt(i);
         if (!Character.isDigit(c) && c != ':') {
            colonsOnly = false;
            break;
         }
      }

      if (colonsOnly) {
         return "HH:mm:ss";
      } else {
         StringReader reader = new StringReader(dt + " ");
         ArrayList<Object[]> vec = new ArrayList<>();
         ArrayList<Object[]> vecRemovelist = new ArrayList<>();
         Object[] nv = new Object[]{'y', new StringBuilder(), 0};
         vec.add(nv);
         if (toTime) {
            nv = new Object[]{'h', new StringBuilder(), 0};
            vec.add(nv);
         }

         int z;
         while((z = reader.read()) != -1) {
            char separator = (char)z;
            int maxvecs = vec.size();

            for(int count = 0; count < maxvecs; ++count) {
               Object[] v = (Object[])vec.get(count);
               int n = v[2];
               char c = getSuccessor(v[0], n);
               if (!Character.isLetterOrDigit(separator)) {
                  if (c == v[0] && c != 'S') {
                     vecRemovelist.add(v);
                  } else {
                     ((StringBuilder)v[1]).append(separator);
                     if (c == 'X' || c == 'Y') {
                        v[2] = 4;
                     }
                  }
               } else {
                  if (c == 'X') {
                     c = 'y';
                     nv = new Object[]{null, new StringBuilder(((StringBuilder)v[1]).toString()).append('M'), null};
                     nv[0] = 'M';
                     nv[2] = 1;
                     vec.add(nv);
                  } else if (c == 'Y') {
                     c = 'M';
                     nv = new Object[]{null, new StringBuilder(((StringBuilder)v[1]).toString()).append('d'), null};
                     nv[0] = 'd';
                     nv[2] = 1;
                     vec.add(nv);
                  }

                  ((StringBuilder)v[1]).append(c);
                  if (c == v[0]) {
                     v[2] = n + 1;
                  } else {
                     v[0] = c;
                     v[2] = 1;
                  }
               }
            }

            int size = vecRemovelist.size();

            for(int i = 0; i < size; ++i) {
               Object[] v = (Object[])vecRemovelist.get(i);
               vec.remove(v);
            }

            vecRemovelist.clear();
         }

         int size = vec.size();

         for(int i = 0; i < size; ++i) {
            Object[] v = (Object[])vec.get(i);
            char c = v[0];
            int n = v[2];
            boolean bk = getSuccessor(c, n) != c;
            boolean atEnd = (c == 's' || c == 'm' || c == 'h' && toTime) && bk;
            boolean finishesAtDate = bk && c == 'd' && !toTime;
            boolean containsEnd = ((StringBuilder)v[1]).toString().indexOf(87) != -1;
            if (!atEnd && !finishesAtDate || containsEnd) {
               vecRemovelist.add(v);
            }
         }

         size = vecRemovelist.size();

         for(int i = 0; i < size; ++i) {
            vec.remove(vecRemovelist.get(i));
         }

         vecRemovelist.clear();
         Object[] v = (Object[])vec.get(0);
         StringBuilder format = (StringBuilder)v[1];
         format.setLength(format.length() - 1);
         return format.toString();
      }
   }

   private static final char getSuccessor(char c, int n) {
      return (char)(c == 'y' && n == 2
         ? 'X'
         : (
            c == 'y' && n < 4
               ? 'y'
               : (
                  c == 'y'
                     ? 'M'
                     : (
                        c == 'M' && n == 2
                           ? 'Y'
                           : (
                              c == 'M' && n < 3
                                 ? 'M'
                                 : (
                                    c == 'M'
                                       ? 'd'
                                       : (
                                          c == 'd' && n < 2
                                             ? 'd'
                                             : (
                                                c == 'd'
                                                   ? 'H'
                                                   : (
                                                      c == 'H' && n < 2
                                                         ? 'H'
                                                         : (c == 'H' ? 'm' : (c == 'm' && n < 2 ? 'm' : (c == 'm' ? 's' : (c == 's' && n < 2 ? 's' : 'W'))))
                                                   )
                                             )
                                       )
                                 )
                           )
                     )
               )
         ));
   }

   static {
      Method aMethod;
      try {
         aMethod = System.class.getMethod("nanoTime", (Class<?>[])null);
      } catch (SecurityException var2) {
         aMethod = null;
      } catch (NoSuchMethodException var3) {
         aMethod = null;
      }

      systemNanoTimeMethod = aMethod;
   }
}
