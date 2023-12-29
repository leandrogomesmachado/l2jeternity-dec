package com.mchange.v1.util;

import java.util.Collection;
import java.util.Iterator;

public class UnreliableIteratorUtils {
   public static void addToCollection(Collection var0, UnreliableIterator var1) throws UnreliableIteratorException {
      while(var1.hasNext()) {
         var0.add(var1.next());
      }
   }

   public static UnreliableIterator unreliableIteratorFromIterator(final Iterator var0) {
      return new UnreliableIterator() {
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
            var0.remove();
         }

         @Override
         public void close() {
         }
      };
   }
}
