package com.mchange.v2.util;

import java.util.Comparator;

public final class ComparatorUtils {
   public static Comparator reverse(final Comparator var0) {
      return new Comparator() {
         @Override
         public int compare(Object var1, Object var2) {
            return -var0.compare(var1, var2);
         }
      };
   }
}
