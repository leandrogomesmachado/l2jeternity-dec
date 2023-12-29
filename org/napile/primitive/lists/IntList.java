package org.napile.primitive.lists;

import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.iterators.IntIterator;
import org.napile.primitive.iterators.IntListIterator;

public interface IntList extends IntCollection {
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

   boolean addAll(int var1, IntCollection var2);

   @Override
   boolean removeAll(IntCollection var1);

   @Override
   boolean retainAll(IntCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();

   int get(int var1);

   int set(int var1, int var2);

   void add(int var1, int var2);

   int removeByIndex(int var1);

   int indexOf(int var1);

   int lastIndexOf(int var1);

   IntListIterator listIterator();

   IntListIterator listIterator(int var1);

   IntList subList(int var1, int var2);
}
