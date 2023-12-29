package com.mchange.util;

import java.util.Iterator;

public class IteratorUtils {
   public static Iterator unmodifiableIterator(final Iterator var0) {
      return new Iterator() {
         @Override
         public boolean hasNext() {
            return var0.hasNext();
         }

         @Override
         public Object next() {
            return var0.next();
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException("This Iterator does not support the remove operation.");
         }
      };
   }
}
