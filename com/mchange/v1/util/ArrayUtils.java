package com.mchange.v1.util;

import com.mchange.v2.lang.ObjectUtils;

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

   public static int hashArray(Object[] var0) {
      int var1 = var0.length;
      int var2 = var1;

      for(int var3 = 0; var3 < var1; ++var3) {
         int var4 = ObjectUtils.hashOrZero(var0[var3]);
         int var5 = var3 % 32;
         int var6 = var4 >>> var5;
         var6 |= var4 << 32 - var5;
         var2 ^= var6;
      }

      return var2;
   }

   public static int hashArray(int[] var0) {
      int var1 = var0.length;
      int var2 = var1;

      for(int var3 = 0; var3 < var1; ++var3) {
         int var4 = var0[var3];
         int var5 = var3 % 32;
         int var6 = var4 >>> var5;
         var6 |= var4 << 32 - var5;
         var2 ^= var6;
      }

      return var2;
   }

   public static int hashOrZeroArray(Object[] var0) {
      return var0 == null ? 0 : hashArray(var0);
   }

   public static int hashOrZeroArray(int[] var0) {
      return var0 == null ? 0 : hashArray(var0);
   }

   /** @deprecated */
   public static String stringifyContents(Object[] var0) {
      StringBuffer var1 = new StringBuffer();
      var1.append("[ ");
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         if (var2 != 0) {
            var1.append(", ");
         }

         var1.append(var0[var2].toString());
      }

      var1.append(" ]");
      return var1.toString();
   }

   private static String toString(String[] var0, int var1) {
      StringBuffer var2 = new StringBuffer(var1);
      boolean var3 = true;
      var2.append('[');
      int var4 = 0;

      for(int var5 = var0.length; var4 < var5; ++var4) {
         if (var3) {
            var3 = false;
         } else {
            var2.append(',');
         }

         var2.append(var0[var4]);
      }

      var2.append(']');
      return var2.toString();
   }

   public static String toString(boolean[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(byte[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(char[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(short[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(int[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(long[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(float[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(double[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         String var5 = String.valueOf(var0[var3]);
         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   public static String toString(Object[] var0) {
      String[] var1 = new String[var0.length];
      int var2 = 0;
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         Object var6 = var0[var3];
         String var5;
         if (var6 instanceof Object[]) {
            var5 = toString(var6);
         } else if (var6 instanceof double[]) {
            var5 = toString((double[])var6);
         } else if (var6 instanceof float[]) {
            var5 = toString((float[])var6);
         } else if (var6 instanceof long[]) {
            var5 = toString((long[])var6);
         } else if (var6 instanceof int[]) {
            var5 = toString((int[])var6);
         } else if (var6 instanceof short[]) {
            var5 = toString((short[])var6);
         } else if (var6 instanceof char[]) {
            var5 = toString((char[])var6);
         } else if (var6 instanceof byte[]) {
            var5 = toString((byte[])var6);
         } else if (var6 instanceof boolean[]) {
            var5 = toString((boolean[])var6);
         } else {
            var5 = String.valueOf(var0[var3]);
         }

         var2 += var5.length();
         var1[var3] = var5;
      }

      return toString(var1, var2 + var0.length + 1);
   }

   private ArrayUtils() {
   }
}
