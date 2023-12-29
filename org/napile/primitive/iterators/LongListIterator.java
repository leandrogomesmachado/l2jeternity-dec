package org.napile.primitive.iterators;

public interface LongListIterator extends LongIterator {
   @Override
   boolean hasNext();

   @Override
   long next();

   boolean hasPrevious();

   long previous();

   int nextIndex();

   int previousIndex();

   @Override
   void remove();

   void set(long var1);

   void add(long var1);
}
