package com.mchange.lang;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public final class ByteUtils {
   public static final short UNSIGNED_MAX_VALUE = 255;

   public static short toUnsigned(byte var0) {
      return (short)(var0 < 0 ? 256 + var0 : var0);
   }

   public static String toHexAscii(byte var0) {
      StringWriter var1 = new StringWriter(2);
      addHexAscii(var0, var1);
      return var1.toString();
   }

   public static String toHexAscii(byte[] var0) {
      int var1 = var0.length;
      StringWriter var2 = new StringWriter(var1 * 2);

      for(int var3 = 0; var3 < var1; ++var3) {
         addHexAscii(var0[var3], var2);
      }

      return var2.toString();
   }

   public static byte[] fromHexAscii(String var0) throws NumberFormatException {
      try {
         int var1 = var0.length();
         if (var1 % 2 != 0) {
            throw new NumberFormatException("Hex ascii must be exactly two digits per byte.");
         } else {
            int var2 = var1 / 2;
            byte[] var3 = new byte[var2];
            int var4 = 0;

            int var6;
            for(StringReader var5 = new StringReader(var0); var4 < var2; var3[var4++] = (byte)var6) {
               var6 = 16 * fromHexDigit(var5.read()) + fromHexDigit(var5.read());
            }

            return var3;
         }
      } catch (IOException var7) {
         throw new InternalError("IOException reading from StringReader?!?!");
      }
   }

   static void addHexAscii(byte var0, StringWriter var1) {
      short var2 = toUnsigned(var0);
      int var3 = var2 / 16;
      int var4 = var2 % 16;
      var1.write(toHexDigit(var3));
      var1.write(toHexDigit(var4));
   }

   private static int fromHexDigit(int var0) throws NumberFormatException {
      if (var0 >= 48 && var0 < 58) {
         return var0 - 48;
      } else if (var0 >= 65 && var0 < 71) {
         return var0 - 55;
      } else if (var0 >= 97 && var0 < 103) {
         return var0 - 87;
      } else {
         throw new NumberFormatException(39 + var0 + "' is not a valid hexadecimal digit.");
      }
   }

   private static char toHexDigit(int var0) {
      char var1;
      if (var0 <= 9) {
         var1 = (char)(var0 + 48);
      } else {
         var1 = (char)(var0 + 55);
      }

      return var1;
   }

   private ByteUtils() {
   }
}
