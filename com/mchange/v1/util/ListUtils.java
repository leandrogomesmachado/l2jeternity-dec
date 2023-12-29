package com.mchange.v1.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public final class ListUtils {
   public static List oneElementUnmodifiableList(final Object var0) {
      return new AbstractList() {
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

         @Override
         public Object get(int var1) {
            if (var1 != 0) {
               throw new IndexOutOfBoundsException("One element list has no element index " + var1);
            } else {
               return var0;
            }
         }
      };
   }

   public static boolean equivalent(List var0, List var1) {
      if (var0.size() != var1.size()) {
         return false;
      } else {
         Iterator var2 = var0.iterator();
         Iterator var3 = var1.iterator();
         return IteratorUtils.equivalent(var2, var3);
      }
   }

   public static int hashContents(List var0) {
      int var1 = 0;
      int var2 = 0;

      for(Object var4 : var0) {
         if (var4 != null) {
            var1 ^= var4.hashCode() ^ var2;
         }

         ++var2;
      }

      return var1;
   }
}
