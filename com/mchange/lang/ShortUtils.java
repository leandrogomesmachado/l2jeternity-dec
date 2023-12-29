package com.mchange.lang;

public class ShortUtils {
   public static final int UNSIGNED_MAX_VALUE = 65535;

   public static int shortFromByteArray(byte[] var0, int var1) {
      int var2 = 0;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 0]) << 8;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 1]) << 0;
      return (short)var2;
   }

   public static byte[] byteArrayFromShort(short var0) {
      byte[] var1 = new byte[2];
      shortIntoByteArray(var0, 0, var1);
      return var1;
   }

   public static void shortIntoByteArray(short var0, int var1, byte[] var2) {
      var2[var1 + 0] = (byte)(var0 >>> 8 & 0xFF);
      var2[var1 + 1] = (byte)(var0 >>> 0 & 0xFF);
   }

   public static int toUnsigned(short var0) {
      return var0 < 0 ? 65536 + var0 : var0;
   }

   private ShortUtils() {
   }
}
