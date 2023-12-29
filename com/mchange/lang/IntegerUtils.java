package com.mchange.lang;

public final class IntegerUtils {
   public static final long UNSIGNED_MAX_VALUE = -1L;

   public static int parseInt(String var0, int var1) {
      if (var0 == null) {
         return var1;
      } else {
         try {
            return Integer.parseInt(var0);
         } catch (NumberFormatException var3) {
            return var1;
         }
      }
   }

   public static int parseInt(String var0, int var1, int var2) {
      if (var0 == null) {
         return var2;
      } else {
         try {
            return Integer.parseInt(var0, var1);
         } catch (NumberFormatException var4) {
            return var2;
         }
      }
   }

   public static int intFromByteArray(byte[] var0, int var1) {
      int var2 = 0;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 0]) << 24;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 1]) << 16;
      var2 |= ByteUtils.toUnsigned(var0[var1 + 2]) << 8;
      return var2 | ByteUtils.toUnsigned(var0[var1 + 3]) << 0;
   }

   public static byte[] byteArrayFromInt(int var0) {
      byte[] var1 = new byte[4];
      intIntoByteArray(var0, 0, var1);
      return var1;
   }

   public static void intIntoByteArray(int var0, int var1, byte[] var2) {
      var2[var1 + 0] = (byte)(var0 >>> 24 & 0xFF);
      var2[var1 + 1] = (byte)(var0 >>> 16 & 0xFF);
      var2[var1 + 2] = (byte)(var0 >>> 8 & 0xFF);
      var2[var1 + 3] = (byte)(var0 >>> 0 & 0xFF);
   }

   public static long toUnsigned(int var0) {
      return var0 < 0 ? 0L + (long)var0 : (long)var0;
   }

   private IntegerUtils() {
   }
}
