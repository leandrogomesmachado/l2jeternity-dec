package com.mchange.v2.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FastCsvUtils {
   private static final int ESCAPE_BIT = 16777216;
   private static final int SHIFT_BIT = 33554432;
   private static final int SHIFT_OFFSET = 8;

   public static String csvReadLine(BufferedReader var0) throws IOException {
      String var1 = var0.readLine();
      String var2;
      if (var1 != null) {
         int var3 = countQuotes(var1);
         if (var3 % 2 != 0) {
            StringBuilder var4 = new StringBuilder(var1);

            do {
               var1 = var0.readLine();
               var4.append(var1);
               var3 += countQuotes(var1);
            } while(var3 % 2 != 0);

            var2 = var4.toString();
         } else {
            var2 = var1;
         }
      } else {
         var2 = null;
      }

      return var2;
   }

   private static int countQuotes(String var0) {
      char[] var1 = var0.toCharArray();
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var1.length; var3 < var4; ++var3) {
         if (var1[var3] == '"') {
            ++var2;
         }
      }

      return var2;
   }

   public static String[] splitRecord(String var0) throws MalformedCsvException {
      int[] var1 = upshiftQuoteString(var0);
      List var2 = splitShifted(var1);
      int var3 = var2.size();
      String[] var4 = new String[var3];

      for(int var5 = 0; var5 < var3; ++var5) {
         var4[var5] = downshift((int[])var2.get(var5));
      }

      return var4;
   }

   private static void debugPrint(int[] var0) {
      int var1 = var0.length;
      char[] var2 = new char[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         var2[var3] = isShifted(var0[var3]) ? 95 : (char)var0[var3];
      }

      System.err.println(new String(var2));
   }

   private static List splitShifted(int[] var0) {
      ArrayList var1 = new ArrayList();
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 <= var4; ++var3) {
         if (var3 == var4 || var0[var3] == 44) {
            int var5 = var3 - var2;
            int var7 = -1;

            int var6;
            for(var6 = var2; var6 <= var3; ++var6) {
               if (var6 == var3) {
                  var7 = 0;
                  break;
               }

               if (var0[var6] != 32 && var0[var6] != 9) {
                  break;
               }
            }

            if (var7 < 0) {
               if (var6 == var3 - 1) {
                  var7 = 1;
               } else {
                  for(var7 = var3 - var6; var7 > 0; --var7) {
                     int var8 = var6 + var7 - 1;
                     if (var0[var8] != 32 && var0[var8] != 9) {
                        break;
                     }
                  }
               }
            }

            int[] var9 = new int[var7];
            if (var7 > 0) {
               System.arraycopy(var0, var6, var9, 0, var7);
            }

            var1.add(var9);
            var2 = var3 + 1;
         }
      }

      return var1;
   }

   private static String downshift(int[] var0) {
      int var1 = var0.length;
      char[] var2 = new char[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         int var4 = var0[var3];
         var2[var3] = (char)(isShifted(var4) ? var4 >>> 8 : var4);
      }

      return new String(var2);
   }

   private static boolean isShifted(int var0) {
      return (var0 & 33554432) != 0;
   }

   private static int[] upshiftQuoteString(String var0) throws MalformedCsvException {
      char[] var1 = var0.toCharArray();
      int[] var2 = new int[var1.length];
      FastCsvUtils.EscapedCharReader var3 = new FastCsvUtils.EscapedCharReader(var1);
      int var4 = 0;
      boolean var5 = false;

      for(int var6 = var3.read(var5); var6 >= 0; var6 = var3.read(var5)) {
         if (var6 == 34) {
            var5 = !var5;
         } else {
            var2[var4++] = findShiftyChar(var6, var5);
         }
      }

      int[] var7 = new int[var4];
      System.arraycopy(var2, 0, var7, 0, var4);
      return var7;
   }

   private static int findShiftyChar(int var0, boolean var1) {
      return var1 ? var0 << 8 | 33554432 : var0;
   }

   private static int escape(int var0) {
      return var0 | 16777216;
   }

   private static boolean isEscaped(int var0) {
      return (var0 & 16777216) != 0;
   }

   private FastCsvUtils() {
   }

   private static class EscapedCharReader {
      char[] chars;
      int finger;

      EscapedCharReader(char[] var1) {
         this.chars = var1;
         this.finger = 0;
      }

      int read(boolean var1) throws MalformedCsvException {
         if (this.finger < this.chars.length) {
            char var2 = this.chars[this.finger++];
            if (var2 != '"' || !var1) {
               return var2;
            } else if (this.finger < this.chars.length) {
               char var3 = this.chars[this.finger];
               if (var3 == '"') {
                  ++this.finger;
                  return FastCsvUtils.escape(var3);
               } else {
                  return var2;
               }
            } else {
               return var2;
            }
         } else {
            return -1;
         }
      }
   }
}
