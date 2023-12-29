package org.apache.commons.lang3;

public class CharSetUtils {
   public static String squeeze(String str, String... set) {
      if (!StringUtils.isEmpty(str) && !deepEmpty(set)) {
         CharSet chars = CharSet.getInstance(set);
         StringBuilder buffer = new StringBuilder(str.length());
         char[] chrs = str.toCharArray();
         int sz = chrs.length;
         char lastChar = chrs[0];
         char ch = ' ';
         Character inChars = null;
         Character notInChars = null;
         buffer.append(lastChar);

         for(int i = 1; i < sz; ++i) {
            ch = chrs[i];
            if (ch == lastChar) {
               if (inChars != null && ch == inChars) {
                  continue;
               }

               if (notInChars == null || ch != notInChars) {
                  if (chars.contains(ch)) {
                     inChars = ch;
                     continue;
                  }

                  notInChars = ch;
               }
            }

            buffer.append(ch);
            lastChar = ch;
         }

         return buffer.toString();
      } else {
         return str;
      }
   }

   public static boolean containsAny(String str, String... set) {
      if (!StringUtils.isEmpty(str) && !deepEmpty(set)) {
         CharSet chars = CharSet.getInstance(set);

         for(char c : str.toCharArray()) {
            if (chars.contains(c)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static int count(String str, String... set) {
      if (!StringUtils.isEmpty(str) && !deepEmpty(set)) {
         CharSet chars = CharSet.getInstance(set);
         int count = 0;

         for(char c : str.toCharArray()) {
            if (chars.contains(c)) {
               ++count;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public static String keep(String str, String... set) {
      if (str == null) {
         return null;
      } else {
         return !str.isEmpty() && !deepEmpty(set) ? modify(str, set, true) : "";
      }
   }

   public static String delete(String str, String... set) {
      return !StringUtils.isEmpty(str) && !deepEmpty(set) ? modify(str, set, false) : str;
   }

   private static String modify(String str, String[] set, boolean expect) {
      CharSet chars = CharSet.getInstance(set);
      StringBuilder buffer = new StringBuilder(str.length());
      char[] chrs = str.toCharArray();

      for(char chr : chrs) {
         if (chars.contains(chr) == expect) {
            buffer.append(chr);
         }
      }

      return buffer.toString();
   }

   private static boolean deepEmpty(String[] strings) {
      if (strings != null) {
         for(String s : strings) {
            if (StringUtils.isNotEmpty(s)) {
               return false;
            }
         }
      }

      return true;
   }
}
