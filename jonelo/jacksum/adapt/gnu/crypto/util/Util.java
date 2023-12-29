package jonelo.jacksum.adapt.gnu.crypto.util;

import java.math.BigInteger;

public class Util {
   private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
   private static final String BASE64_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./";
   private static final char[] BASE64_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./".toCharArray();

   private Util() {
   }

   public static String toString(byte[] var0) {
      return toString(var0, 0, var0.length);
   }

   public static final String toString(byte[] var0, int var1, int var2) {
      char[] var3 = new char[var2 * 2];
      int var4 = 0;

      byte var6;
      for(int var5 = 0; var4 < var2; var3[var5++] = HEX_DIGITS[var6 & 15]) {
         var6 = var0[var1 + var4++];
         var3[var5++] = HEX_DIGITS[var6 >>> 4 & 15];
      }

      return new String(var3);
   }

   public static String toReversedString(byte[] var0) {
      return toReversedString(var0, 0, var0.length);
   }

   public static final String toReversedString(byte[] var0, int var1, int var2) {
      char[] var3 = new char[var2 * 2];
      int var4 = var1 + var2 - 1;

      byte var6;
      for(int var5 = 0; var4 >= var1; var3[var5++] = HEX_DIGITS[var6 & 15]) {
         var6 = var0[var1 + var4--];
         var3[var5++] = HEX_DIGITS[var6 >>> 4 & 15];
      }

      return new String(var3);
   }

   public static byte[] toBytesFromString(String var0) {
      int var1 = var0.length();
      byte[] var2 = new byte[(var1 + 1) / 2];
      int var3 = 0;
      int var4 = 0;
      if (var1 % 2 == 1) {
         var2[var4++] = (byte)fromDigit(var0.charAt(var3++));
      }

      while(var3 < var1) {
         var2[var4] = (byte)(fromDigit(var0.charAt(var3++)) << 4);
         int var6 = var4++;
         var2[var6] |= (byte)fromDigit(var0.charAt(var3++));
      }

      return var2;
   }

   public static byte[] toReversedBytesFromString(String var0) {
      int var1 = var0.length();
      byte[] var2 = new byte[(var1 + 1) / 2];
      int var3 = 0;
      if (var1 % 2 == 1) {
         var2[var3++] = (byte)fromDigit(var0.charAt(--var1));
      }

      while(var1 > 0) {
         var2[var3] = (byte)fromDigit(var0.charAt(--var1));
         int var5 = var3++;
         var2[var5] |= (byte)(fromDigit(var0.charAt(--var1)) << 4);
      }

      return var2;
   }

   public static int fromDigit(char var0) {
      if (var0 >= '0' && var0 <= '9') {
         return var0 - 48;
      } else if (var0 >= 'A' && var0 <= 'F') {
         return var0 - 65 + 10;
      } else if (var0 >= 'a' && var0 <= 'f') {
         return var0 - 97 + 10;
      } else {
         throw new IllegalArgumentException("Invalid hexadecimal digit: " + var0);
      }
   }

   public static String toString(int var0) {
      char[] var1 = new char[8];

      for(int var2 = 7; var2 >= 0; --var2) {
         var1[var2] = HEX_DIGITS[var0 & 15];
         var0 >>>= 4;
      }

      return new String(var1);
   }

   public static String toString(int[] var0) {
      int var1 = var0.length;
      char[] var2 = new char[var1 * 8];
      int var3 = 0;

      for(int var4 = 0; var3 < var1; ++var3) {
         int var5 = var0[var3];
         var2[var4++] = HEX_DIGITS[var5 >>> 28 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 24 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 20 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 16 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 12 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 8 & 15];
         var2[var4++] = HEX_DIGITS[var5 >>> 4 & 15];
         var2[var4++] = HEX_DIGITS[var5 & 15];
      }

      return new String(var2);
   }

   public static String toString(long var0) {
      char[] var2 = new char[16];

      for(int var3 = 15; var3 >= 0; --var3) {
         var2[var3] = HEX_DIGITS[(int)(var0 & 15L)];
         var0 >>>= 4;
      }

      return new String(var2);
   }

   public static String toUnicodeString(byte[] var0) {
      return toUnicodeString(var0, 0, var0.length);
   }

   public static final String toUnicodeString(byte[] var0, int var1, int var2) {
      StringBuffer var3 = new StringBuffer();
      int var4 = 0;
      int var5 = 0;
      var3.append('\n').append("\"");

      while(var4 < var2) {
         var3.append("\\u");
         byte var6 = var0[var1 + var4++];
         var3.append(HEX_DIGITS[var6 >>> 4 & 15]);
         var3.append(HEX_DIGITS[var6 & 15]);
         var6 = var0[var1 + var4++];
         var3.append(HEX_DIGITS[var6 >>> 4 & 15]);
         var3.append(HEX_DIGITS[var6 & 15]);
         if (++var5 % 8 == 0) {
            var3.append("\"+").append('\n').append("\"");
         }
      }

      var3.append("\"").append('\n');
      return var3.toString();
   }

   public static String toUnicodeString(int[] var0) {
      StringBuffer var1 = new StringBuffer();
      int var2 = 0;
      int var3 = 0;
      var1.append('\n').append("\"");

      while(var2 < var0.length) {
         int var4 = var0[var2++];
         var1.append("\\u");
         var1.append(HEX_DIGITS[var4 >>> 28 & 15]);
         var1.append(HEX_DIGITS[var4 >>> 24 & 15]);
         var1.append(HEX_DIGITS[var4 >>> 20 & 15]);
         var1.append(HEX_DIGITS[var4 >>> 16 & 15]);
         var1.append("\\u");
         var1.append(HEX_DIGITS[var4 >>> 12 & 15]);
         var1.append(HEX_DIGITS[var4 >>> 8 & 15]);
         var1.append(HEX_DIGITS[var4 >>> 4 & 15]);
         var1.append(HEX_DIGITS[var4 & 15]);
         if (++var3 % 4 == 0) {
            var1.append("\"+").append('\n').append("\"");
         }
      }

      var1.append("\"").append('\n');
      return var1.toString();
   }

   public static byte[] toBytesFromUnicode(String var0) {
      int var1 = var0.length() * 2;
      byte[] var2 = new byte[var1];

      for(int var4 = 0; var4 < var1; ++var4) {
         char var3 = var0.charAt(var4 >>> 1);
         var2[var4] = (byte)((var4 & 1) == 0 ? var3 >>> '\b' : var3);
      }

      return var2;
   }

   public static String dumpString(byte[] var0, int var1, int var2, String var3) {
      if (var0 == null) {
         return var3 + "null\n";
      } else {
         StringBuffer var4 = new StringBuffer(var2 * 3);
         if (var2 > 32) {
            var4.append(var3).append("Hexadecimal dump of ").append(var2).append(" bytes...\n");
         }

         int var5 = var1 + var2;
         int var7 = Integer.toString(var2).length();
         if (var7 < 4) {
            var7 = 4;
         }

         while(var1 < var5) {
            if (var2 > 32) {
               String var6 = "         " + var1;
               var4.append(var3).append(var6.substring(var6.length() - var7)).append(": ");
            }

            int var8;
            for(var8 = 0; var8 < 32 && var1 + var8 + 7 < var5; var8 += 8) {
               var4.append(toString(var0, var1 + var8, 8)).append(' ');
            }

            if (var8 < 32) {
               while(var8 < 32 && var1 + var8 < var5) {
                  var4.append(byteToString(var0[var1 + var8]));
                  ++var8;
               }
            }

            var4.append('\n');
            var1 += 32;
         }

         return var4.toString();
      }
   }

   public static String dumpString(byte[] var0) {
      return var0 == null ? "null\n" : dumpString(var0, 0, var0.length, "");
   }

   public static String dumpString(byte[] var0, String var1) {
      return var0 == null ? "null\n" : dumpString(var0, 0, var0.length, var1);
   }

   public static String dumpString(byte[] var0, int var1, int var2) {
      return dumpString(var0, var1, var2, "");
   }

   public static String byteToString(int var0) {
      char[] var1 = new char[]{HEX_DIGITS[var0 >>> 4 & 15], HEX_DIGITS[var0 & 15]};
      return new String(var1);
   }

   public static final String toBase64(byte[] var0) {
      int var1 = var0.length;
      int var2 = var1 % 3;
      byte var3 = 0;
      byte var4 = 0;
      byte var5 = 0;
      switch(var2) {
         case 1:
            var5 = var0[0];
            break;
         case 2:
            var4 = var0[0];
            var5 = var0[1];
      }

      StringBuffer var6 = new StringBuffer();
      boolean var8 = false;

      while(true) {
         int var7 = (var3 & 252) >>> 2;
         if (var8 || var7 != 0) {
            var6.append(BASE64_CHARSET[var7]);
            var8 = true;
         }

         var7 = (var3 & 3) << 4 | (var4 & 240) >>> 4;
         if (var8 || var7 != 0) {
            var6.append(BASE64_CHARSET[var7]);
            var8 = true;
         }

         var7 = (var4 & 15) << 2 | (var5 & 192) >>> 6;
         if (var8 || var7 != 0) {
            var6.append(BASE64_CHARSET[var7]);
            var8 = true;
         }

         var7 = var5 & 63;
         if (var8 || var7 != 0) {
            var6.append(BASE64_CHARSET[var7]);
            var8 = true;
         }

         if (var2 >= var1) {
            break;
         }

         try {
            var3 = var0[var2++];
            var4 = var0[var2++];
            var5 = var0[var2++];
         } catch (ArrayIndexOutOfBoundsException var10) {
            break;
         }
      }

      return var8 ? var6.toString() : "0";
   }

   public static final byte[] fromBase64(String var0) {
      int var1 = var0.length();
      if (var1 == 0) {
         throw new NumberFormatException("Empty string");
      } else {
         byte[] var2 = new byte[var1 + 1];

         for(int var3 = 0; var3 < var1; ++var3) {
            try {
               var2[var3] = (byte)"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./".indexOf(var0.charAt(var3));
            } catch (ArrayIndexOutOfBoundsException var6) {
               throw new NumberFormatException("Illegal character at #" + var3);
            }
         }

         int var9 = var1 - 1;
         int var4 = var1;

         try {
            do {
               var2[var4] = var2[var9];
               if (--var9 < 0) {
                  break;
               }

               var2[var4] = (byte)(var2[var4] | (var2[var9] & 3) << 6);
               --var4;
               var2[var4] = (byte)((var2[var9] & 60) >>> 2);
               if (--var9 < 0) {
                  break;
               }

               var2[var4] = (byte)(var2[var4] | (var2[var9] & 15) << 4);
               --var4;
               var2[var4] = (byte)((var2[var9] & 48) >>> 4);
               if (--var9 < 0) {
                  break;
               }

               var2[var4] = (byte)(var2[var4] | var2[var9] << 2);
               --var4;
               var2[var4] = 0;
            } while(--var9 >= 0);
         } catch (Exception var8) {
         }

         try {
            while(var2[var4] == 0) {
               ++var4;
            }
         } catch (Exception var7) {
            return new byte[1];
         }

         byte[] var5 = new byte[var1 - var4 + 1];
         System.arraycopy(var2, var4, var5, 0, var1 - var4 + 1);
         return var5;
      }
   }

   public static final byte[] trim(BigInteger var0) {
      byte[] var1 = var0.toByteArray();
      if (var1.length != 0 && var1[0] == 0) {
         int var2 = var1.length;
         int var3 = 1;

         while(var1[var3] == 0 && var3 < var2) {
            ++var3;
         }

         byte[] var4 = new byte[var2 - var3];
         System.arraycopy(var1, var3, var4, 0, var2 - var3);
         return var4;
      } else {
         return var1;
      }
   }

   public static final String dump(BigInteger var0) {
      return dumpString(trim(var0));
   }
}
