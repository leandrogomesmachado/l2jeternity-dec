package org.napile.primitive.collections;

import org.napile.primitive.Container;
import org.napile.primitive.iterators.LongIterator;

public interface LongCollection extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean contains(long var1);

   LongIterator iterator();

   long[] toArray();

   long[] toArray(long[] var1);

   boolean add(long var1);

   boolean remove(long var1);

   boolean containsAll(LongCollection var1);

   boolean addAll(LongCollection var1);

   boolean removeAll(LongCollection var1);

   boolean retainAll(LongCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
