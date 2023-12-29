package com.mchange.v2.util;

public final class XORShiftRandomUtils {
   public static long nextLong(long var0) {
      var0 ^= var0 << 21;
      var0 ^= var0 >>> 35;
      return var0 ^ var0 << 4;
   }

   public static void main(String[] var0) {
      long var1 = System.currentTimeMillis();
      byte var3 = 100;
      int[] var4 = new int[var3];

      for(int var5 = 0; var5 < 1000000; ++var5) {
         var1 = nextLong(var1);
         var4[(int)(Math.abs(var1) % (long)var3)]++;
         if (var5 % 10000 == 0) {
            System.out.println(var1);
         }
      }

      for(int var6 = 0; var6 < var3; ++var6) {
         if (var6 != 0) {
            System.out.print(", ");
         }

         System.out.print(var6 + " -> " + var4[var6]);
      }

      System.out.println();
   }
}
