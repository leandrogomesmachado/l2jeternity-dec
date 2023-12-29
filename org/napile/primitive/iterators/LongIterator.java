package org.napile.primitive.iterators;

public interface LongIterator {
   boolean hasNext();

   long next();

   void remove();
}
