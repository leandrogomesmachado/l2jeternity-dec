package org.napile.primitive.sets;

import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.LongIterator;

public interface LongSet extends LongCollection {
   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(long var1);

   @Override
   LongIterator iterator();

   @Override
   long[] toArray();

   @Override
   long[] toArray(long[] var1);

   @Override
   boolean add(long var1);

   @Override
   boolean remove(long var1);

   @Override
   boolean containsAll(LongCollection var1);

   @Override
   boolean addAll(LongCollection var1);

   @Override
   boolean retainAll(LongCollection var1);

   @Override
   boolean removeAll(LongCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
