package org.napile.primitive.sets;

import org.napile.primitive.iterators.IntIterator;

public interface NavigableIntSet extends SortedIntSet {
   int lower(int var1);

   int floor(int var1);

   int ceiling(int var1);

   int higher(int var1);

   int pollFirst();

   int pollLast();

   @Override
   IntIterator iterator();

   NavigableIntSet descendingSet();

   IntIterator descendingIterator();

   NavigableIntSet subSet(int var1, boolean var2, int var3, boolean var4);

   NavigableIntSet headSet(int var1, boolean var2);

   NavigableIntSet tailSet(int var1, boolean var2);

   @Override
   SortedIntSet subSet(int var1, int var2);

   @Override
   SortedIntSet headSet(int var1);

   @Override
   SortedIntSet tailSet(int var1);
}
