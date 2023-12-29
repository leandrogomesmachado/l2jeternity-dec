package l2e.commons.util;

import l2e.gameserver.Config;

public final class StringUtil {
   private StringUtil() {
   }

   public static String concat(String... strings) {
      StringBuilder sbString = new StringBuilder();

      for(String string : strings) {
         sbString.append(string);
      }

      return sbString.toString();
   }

   public static StringBuilder startAppend(int sizeHint, String... strings) {
      int length = getLength(strings);
      StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);

      for(String string : strings) {
         sbString.append(string);
      }

      return sbString;
   }

   public static void append(StringBuilder sb, Object... content) {
      for(Object obj : content) {
         sb.append(obj == null ? null : obj.toString());
      }
   }

   public static void append(StringBuilder sbString, String... strings) {
      sbString.ensureCapacity(sbString.length() + getLength(strings));

      for(String string : strings) {
         sbString.append(string);
      }
   }

   private static int getLength(String[] strings) {
      int length = 0;

      for(String string : strings) {
         if (string == null) {
            length += 4;
         } else {
            length += string.length();
         }
      }

      return length;
   }

   public static String getTraceString(StackTraceElement[] trace) {
      StringBuilder sbString = new StringBuilder();

      for(StackTraceElement element : trace) {
         sbString.append(element.toString()).append(Config.EOL);
      }

      return sbString.toString();
   }

   public static String substringBetween(String str, String open, String close) {
      int INDEX_NOT_FOUND = -1;
      if (str != null && open != null && close != null) {
         int start = str.indexOf(open);
         if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
               return str.substring(start + open.length(), end);
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
