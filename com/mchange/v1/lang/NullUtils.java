package com.mchange.v1.lang;

/** @deprecated */
public final class NullUtils {
   public static boolean equalsOrBothNull(Object var0, Object var1) {
      if (var0 == var1) {
         return true;
      } else {
         return var0 == null ? false : var0.equals(var1);
      }
   }

   private NullUtils() {
   }
}
