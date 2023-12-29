package org.napile.primitive.sets;

import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;

public interface IntSet extends IntCollection {
   @Override
   int size();

   @Override
   boolean isEmpty();

   @Override
   boolean contains(int var1);

   @Override
   IntIterator iterator();

   @Override
   int[] toArray();

   @Override
   int[] toArray(int[] var1);

   @Override
   boolean add(int var1);

   @Override
   boolean remove(int var1);

   @Override
   boolean containsAll(IntCollection var1);

   @Override
   boolean addAll(IntCollection var1);

   @Override
   boolean retainAll(IntCollection var1);

   @Override
   boolean removeAll(IntCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
