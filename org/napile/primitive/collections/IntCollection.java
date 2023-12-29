package org.napile.primitive.collections;

import org.napile.primitive.Container;
import org.napile.primitive.iterators.IntIterator;

public interface IntCollection extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean contains(int var1);

   IntIterator iterator();

   int[] toArray();

   int[] toArray(int[] var1);

   boolean add(int var1);

   boolean remove(int var1);

   boolean containsAll(IntCollection var1);

   boolean addAll(IntCollection var1);

   boolean removeAll(IntCollection var1);

   boolean retainAll(IntCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
