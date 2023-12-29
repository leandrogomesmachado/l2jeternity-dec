package org.napile.primitive.lists;

import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.iterators.LongIterator;
import org.napile.primitive.iterators.LongListIterator;

public interface LongList extends LongCollection {
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

   boolean addAll(int var1, LongCollection var2);

   @Override
   boolean removeAll(LongCollection var1);

   @Override
   boolean retainAll(LongCollection var1);

   @Override
   void clear();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();

   long get(int var1);

   long set(int var1, long var2);

   void add(int var1, long var2);

   long removeByIndex(int var1);

   int indexOf(long var1);

   int lastIndexOf(long var1);

   LongListIterator listIterator();

   LongListIterator listIterator(int var1);

   LongList subList(int var1, int var2);
}
