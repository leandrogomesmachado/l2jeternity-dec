package com.mchange.lang;

public final class DoubleUtils {
   public static byte[] byteArrayFromDouble(double var0) {
      long var2 = Double.doubleToLongBits(var0);
      return LongUtils.byteArrayFromLong(var2);
   }

   public static double doubleFromByteArray(byte[] var0, int var1) {
      long var2 = LongUtils.longFromByteArray(var0, var1);
      return Double.longBitsToDouble(var2);
   }
}
