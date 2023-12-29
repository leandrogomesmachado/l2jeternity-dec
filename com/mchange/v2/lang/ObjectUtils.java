package com.mchange.v2.lang;

public final class ObjectUtils {
   public static boolean eqOrBothNull(Object var0, Object var1) {
      if (var0 == var1) {
         return true;
      } else {
         return var0 == null ? false : var0.equals(var1);
      }
   }

   public static int hashOrZero(Object var0) {
      return var0 == null ? 0 : var0.hashCode();
   }

   private ObjectUtils() {
   }
}
