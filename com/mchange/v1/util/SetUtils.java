package com.mchange.v1.util;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class SetUtils {
   public static Set oneElementUnmodifiableSet(final Object var0) {
      return new AbstractSet() {
         @Override
         public Iterator iterator() {
            return IteratorUtils.oneElementUnmodifiableIterator(var0);
         }

         @Override
         public int size() {
            return 1;
         }

         @Override
         public boolean isEmpty() {
            return false;
         }

         @Override
         public boolean contains(Object var1) {
            return var1 == var0;
         }
      };
   }

   public static Set setFromArray(Object[] var0) {
      HashSet var1 = new HashSet();
      int var2 = 0;

      for(int var3 = var0.length; var2 < var3; ++var2) {
         var1.add(var0[var2]);
      }

      return var1;
   }

   public static boolean equivalentDisregardingSort(Set var0, Set var1) {
      return var0.containsAll(var1) && var1.containsAll(var0);
   }

   public static int hashContentsDisregardingSort(Set var0) {
      int var1 = 0;

      for(Object var3 : var0) {
         if (var3 != null) {
            var1 ^= var3.hashCode();
         }
      }

      return var1;
   }
}
