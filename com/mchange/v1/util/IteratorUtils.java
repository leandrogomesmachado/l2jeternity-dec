package com.mchange.v1.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class IteratorUtils {
   public static final Iterator EMPTY_ITERATOR = new Iterator() {
      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public Object next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         throw new IllegalStateException();
      }
   };

   public static Iterator oneElementUnmodifiableIterator(final Object var0) {
      return new Iterator() {
         boolean shot = false;

         @Override
         public boolean hasNext() {
            return !this.shot;
         }

         @Override
         public Object next() {
            if (this.shot) {
               throw new NoSuchElementException();
            } else {
               this.shot = true;
               return var0;
            }
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException("remove() not supported.");
         }
      };
   }

   public static boolean equivalent(Iterator var0, Iterator var1) {
      while(true) {
         boolean var2 = var0.hasNext();
         boolean var3 = var1.hasNext();
         if (var2 ^ var3) {
            return false;
         }

         if (var2) {
            Object var4 = var0.next();
            Object var5 = var1.next();
            if (var4 == var5) {
               continue;
            }

            if (var4 == null) {
               return false;
            }

            if (var4.equals(var5)) {
               continue;
            }

            return false;
         }

         return true;
      }
   }

   public static ArrayList toArrayList(Iterator var0, int var1) {
      ArrayList var2 = new ArrayList(var1);

      while(var0.hasNext()) {
         var2.add(var0.next());
      }

      return var2;
   }

   public static void fillArray(Iterator var0, Object[] var1, boolean var2) {
      int var3 = 0;
      int var4 = var1.length;

      while(var3 < var4 && var0.hasNext()) {
         var1[var3++] = var0.next();
      }

      if (var2 && var3 < var4) {
         var1[var3] = null;
      }
   }

   public static void fillArray(Iterator var0, Object[] var1) {
      fillArray(var0, var1, false);
   }

   public static Object[] toArray(Iterator var0, int var1, Class var2, boolean var3) {
      Object[] var4 = (Object[])Array.newInstance(var2, var1);
      fillArray(var0, var4, var3);
      return var4;
   }

   public static Object[] toArray(Iterator var0, int var1, Class var2) {
      return toArray(var0, var1, var2, false);
   }

   public static Object[] toArray(Iterator var0, int var1, Object[] var2) {
      if (var2.length >= var1) {
         fillArray(var0, var2, true);
         return var2;
      } else {
         Class var3 = var2.getClass().getComponentType();
         return toArray(var0, var1, var3);
      }
   }

   private IteratorUtils() {
   }
}
