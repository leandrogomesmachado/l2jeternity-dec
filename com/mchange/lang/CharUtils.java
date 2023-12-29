package com.mchange.lang;

import java.io.StringWriter;

public final class CharUtils {
   public static int charFromByteArray(byte[] var0, int var1) {
      int var2 = 0;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 0]) << 8;
      return var2 | ByteUtils.toUnsigned(var0[var1 + 1]) << 0;
   }

   public static byte[] byteArrayFromChar(char var0) {
      byte[] var1 = new byte[2];
      charIntoByteArray(var0, 0, var1);
      return var1;
   }

   public static void charIntoByteArray(int var0, int var1, byte[] var2) {
      var2[var1 + 0] = (byte)(var0 >>> 8 & 0xFF);
      var2[var1 + 1] = (byte)(var0 >>> 0 & 0xFF);
   }

   public static String toHexAscii(char var0) {
      StringWriter var1 = new StringWriter(4);
      ByteUtils.addHexAscii((byte)(var0 >>> '\b' & 0xFF), var1);
      ByteUtils.addHexAscii((byte)(var0 & 255), var1);
      return var1.toString();
   }

   public static char[] fromHexAscii(String var0) {
      int var1 = var0.length();
      if (var1 % 4 != 0) {
         throw new NumberFormatException("Hex ascii must be exactly four digits per char.");
      } else {
         byte[] var2 = ByteUtils.fromHexAscii(var0);
         int var3 = var1 / 4;
         char[] var4 = new char[var3];

         for(int var5 = 0; var1 < var3; ++var5) {
            var4[var5] = (char)charFromByteArray(var2, var5 * 2);
         }

         return var4;
      }
   }

   private CharUtils() {
   }
}
