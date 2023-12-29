package jonelo.sugar.util;

import java.text.MessageFormat;
import java.util.ArrayList;

public class GeneralString {
   private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private static final String specialChars = "=: \t\r\n\f#!";

   public static char nibbleToHexChar(int var0) {
      return hexDigits[var0 & 15];
   }

   public static String replaceString(String var0, String var1, String var2) {
      int var3 = var0.indexOf(var1);
      if (var3 > -1) {
         StringBuffer var4 = new StringBuffer();
         var4.append(var0.substring(0, var3));
         var4.append(var2);
         var4.append(var0.substring(var3 + var1.length()));
         return var4.toString();
      } else {
         return var0;
      }
   }

   public static String replaceAllStrings(String var0, String var1, String var2) {
      StringBuffer var3 = new StringBuffer(var0);
      int var4 = var0.length();
      int var5 = var1.length();

      while((var4 = var3.toString().lastIndexOf(var1, var4 - 1)) > -1) {
         var3.replace(var4, var4 + var5, var2);
      }

      return var3.toString();
   }

   public static void replaceAllStrings(StringBuffer var0, String var1, String var2) {
      int var3 = var0.length();
      int var4 = var1.length();

      while((var3 = var0.toString().lastIndexOf(var1, var3 - 1)) > -1) {
         var0.replace(var3, var3 + var4, var2);
      }
   }

   public static String removeAllStrings(String var0, String var1) {
      return replaceAllStrings(var0, var1, "");
   }

   public static String replaceString(String var0, int var1, String var2) {
      StringBuffer var3 = new StringBuffer(var0);

      for(int var4 = 0; var4 < var2.length(); ++var4) {
         var3.setCharAt(var1 + var4, var2.charAt(var4));
      }

      return var3.toString();
   }

   public static String translateEscapeSequences(String var0) {
      String var1 = replaceAllStrings(var0, "\\t", "\t");
      var1 = replaceAllStrings(var1, "\\n", "\n");
      var1 = replaceAllStrings(var1, "\\r", "\r");
      var1 = replaceAllStrings(var1, "\\\"", "\"");
      var1 = replaceAllStrings(var1, "\\'", "'");
      return replaceAllStrings(var1, "\\\\", "\\");
   }

   public static String removeChar(String var0, char var1) {
      StringBuffer var2 = new StringBuffer();

      for(int var3 = 0; var3 < var0.length(); ++var3) {
         if (var0.charAt(var3) != var1) {
            var2.append(var0.charAt(var3));
         }
      }

      return var2.toString();
   }

   public static String removeChar(String var0, int var1) {
      StringBuffer var2 = new StringBuffer(var0.length() - 1);
      var2.append(var0.substring(0, var1)).append(var0.substring(var1 + 1));
      return var2.toString();
   }

   public static String replaceChar(String var0, char var1, char var2) {
      StringBuffer var3 = new StringBuffer(var0);

      for(int var4 = 0; var4 < var0.length(); ++var4) {
         if (var0.charAt(var4) == var1) {
            var3.setCharAt(var4, var2);
         }
      }

      return var3.toString();
   }

   public static String replaceChar(String var0, int var1, char var2) {
      StringBuffer var3 = new StringBuffer(var0);
      var3.setCharAt(var1, var2);
      return var3.toString();
   }

   public static int countChar(String var0, char var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < var0.length(); ++var3) {
         if (var0.charAt(var3) == var1) {
            ++var2;
         }
      }

      return var2;
   }

   public static String message(String var0, char var1) {
      Character var2 = new Character(var1);
      Object[] var3 = new Object[]{var2.toString()};
      return MessageFormat.format(var0, var3);
   }

   public static String message(String var0, int var1) {
      Integer var2 = new Integer(var1);
      Object[] var3 = new Object[]{var2.toString()};
      return MessageFormat.format(var0, var3);
   }

   public static String message(String var0, int var1, int var2) {
      Integer var3 = new Integer(var1);
      Integer var4 = new Integer(var2);
      Object[] var5 = new Object[]{var3.toString(), var4.toString()};
      return MessageFormat.format(var0, var5);
   }

   public static String message(String var0, String var1) {
      Object[] var2 = new Object[]{var1};
      return MessageFormat.format(var0, var2);
   }

   public static String decodeEncodedUnicode(String var0) {
      int var2 = var0.length();
      StringBuffer var3 = new StringBuffer(var2);
      int var4 = 0;

      while(var4 < var2) {
         char var1 = var0.charAt(var4++);
         if (var1 == '\\') {
            var1 = var0.charAt(var4++);
            switch(var1) {
               case 'f':
                  var3.append('\f');
                  break;
               case 'n':
                  var3.append('\n');
                  break;
               case 'r':
                  var3.append('\r');
                  break;
               case 't':
                  var3.append('\t');
                  break;
               case 'u':
                  int var5 = 0;

                  for(int var6 = 0; var6 < 4; ++var6) {
                     var1 = var0.charAt(var4++);
                     if (var1 >= '0' && var1 <= '9') {
                        var5 = (var5 << 4) + var1 - 48;
                     } else if (var1 >= 'a' && var1 <= 'f') {
                        var5 = (var5 << 4) + 10 + var1 - 97;
                     } else {
                        if (var1 < 'A' || var1 > 'F') {
                           throw new IllegalArgumentException("Wrong \\uxxxx encoding");
                        }

                        var5 = (var5 << 4) + 10 + var1 - 65;
                     }
                  }

                  var3.append((char)var5);
                  break;
               default:
                  var3.append(var1);
            }
         } else {
            var3.append(var1);
         }
      }

      return var3.toString();
   }

   public static String encodeUnicode(String var0) {
      int var1 = var0.length();
      StringBuffer var2 = new StringBuffer(var1 * 2);

      for(int var3 = 0; var3 < var1; ++var3) {
         char var4 = var0.charAt(var3);
         switch(var4) {
            case '\t':
               var2.append('\\');
               var2.append('t');
               break;
            case '\n':
               var2.append('\\');
               var2.append('n');
               break;
            case '\f':
               var2.append('\\');
               var2.append('f');
               break;
            case '\r':
               var2.append('\\');
               var2.append('r');
               break;
            case ' ':
               var2.append(' ');
               break;
            case '\\':
               var2.append('\\');
               var2.append('\\');
               break;
            default:
               if (var4 >= ' ' && var4 <= '~') {
                  if ("=: \t\r\n\f#!".indexOf(var4) != -1) {
                     var2.append('\\');
                  }

                  var2.append(var4);
               } else {
                  var2.append('\\');
                  var2.append('u');
                  var2.append(nibbleToHexChar(var4 >> '\f' & 15));
                  var2.append(nibbleToHexChar(var4 >> '\b' & 15));
                  var2.append(nibbleToHexChar(var4 >> 4 & 15));
                  var2.append(nibbleToHexChar(var4 & 15));
               }
         }
      }

      return var2.toString();
   }

   public static String[] split(String var0, String var1) {
      ArrayList var2 = new ArrayList();
      int var3 = 0;
      int var4 = -1;

      do {
         var4 = var0.substring(var3).indexOf(var1);
         if (var4 > -1) {
            var2.add(var0.substring(var3, var3 + var4));
            var3 = var3 + var4 + var1.length();
         }
      } while(var4 > -1);

      if (var3 < var0.length()) {
         var2.add(var0.substring(var3));
      }

      String[] var5 = new String[var2.size()];

      for(int var6 = 0; var6 < var5.length; ++var6) {
         var5[var6] = (String)var2.get(var6);
      }

      return var5;
   }
}
