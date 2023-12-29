package org.napile.primitive.iterators;

public interface IntListIterator extends IntIterator {
   @Override
   boolean hasNext();

   @Override
   int next();

   boolean hasPrevious();

   int previous();

   int nextIndex();

   int previousIndex();

   @Override
   void remove();

   void set(int var1);

   void add(int var1);
}
