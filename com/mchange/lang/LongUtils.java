package com.mchange.lang;

public class LongUtils {
   private LongUtils() {
   }

   public static long longFromByteArray(byte[] var0, int var1) {
      long var2 = 0L;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 0]) << 56;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 1]) << 48;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 2]) << 40;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 3]) << 32;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 4]) << 24;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 5]) << 16;
      var2 |= (long)ByteUtils.toUnsigned(var0[var1 + 6]) << 8;
      return var2 | (long)ByteUtils.toUnsigned(var0[var1 + 7]) << 0;
   }

   public static byte[] byteArrayFromLong(long var0) {
      byte[] var2 = new byte[8];
      longIntoByteArray(var0, 0, var2);
      return var2;
   }

   public static void longIntoByteArray(long var0, int var2, byte[] var3) {
      var3[var2 + 0] = (byte)((int)(var0 >>> 56 & 255L));
      var3[var2 + 1] = (byte)((int)(var0 >>> 48 & 255L));
      var3[var2 + 2] = (byte)((int)(var0 >>> 40 & 255L));
      var3[var2 + 3] = (byte)((int)(var0 >>> 32 & 255L));
      var3[var2 + 4] = (byte)((int)(var0 >>> 24 & 255L));
      var3[var2 + 5] = (byte)((int)(var0 >>> 16 & 255L));
      var3[var2 + 6] = (byte)((int)(var0 >>> 8 & 255L));
      var3[var2 + 7] = (byte)((int)(var0 >>> 0 & 255L));
   }

   public static int fullHashLong(long var0) {
      return hashLong(var0);
   }

   public static int hashLong(long var0) {
      return (int)var0 ^ (int)(var0 >>> 32);
   }
}
