package com.mchange.lang;

/** @deprecated */
public final class ArrayUtils {
   public static int indexOf(Object[] var0, Object var1) {
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         if (var1.equals(var0[var2])) {
            return var2;
         }
      }

      return -1;
   }

   public static int identityIndexOf(Object[] var0, Object var1) {
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         if (var1 == var0[var2]) {
            return var2;
         }
      }

      return -1;
   }

   public static int hashAll(Object[] var0) {
      int var1 = 0;
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         Object var4 = var0[var2];
         if (var4 != null) {
            var1 ^= var4.hashCode();
         }
      }

      return var1;
   }

   public static int hashAll(int[] var0) {
      int var1 = 0;
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         var1 ^= var0[var2];
      }

      return var1;
   }

   public static boolean startsWith(byte[] var0, byte[] var1) {
      int var2 = var0.length;
      int var3 = var1.length;
      if (var2 < var3) {
         return false;
      } else {
         for(int var4 = 0; var4 < var3; ++var4) {
            if (var0[var4] != var1[var4]) {
               return false;
            }
         }

         return true;
      }
   }

   private ArrayUtils() {
   }
}
