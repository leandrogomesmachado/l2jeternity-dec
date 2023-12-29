package com.mchange.v1.util;

import java.util.Map;

/** @deprecated */
public final class MapUtils {
   public static boolean equivalentDisregardingSort(Map var0, Map var1) {
      if (var0.size() != var1.size()) {
         return false;
      } else {
         for(Object var3 : var0.keySet()) {
            if (!var0.get(var3).equals(var1.get(var3))) {
               return false;
            }
         }

         return true;
      }
   }

   public static int hashContentsDisregardingSort(Map var0) {
      int var1 = 0;

      for(Object var3 : var0.keySet()) {
         Object var4 = var0.get(var3);
         var1 ^= var3.hashCode() ^ var4.hashCode();
      }

      return var1;
   }
}
