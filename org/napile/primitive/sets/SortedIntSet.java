package org.napile.primitive.sets;

import org.napile.primitive.comparators.IntComparator;

public interface SortedIntSet extends IntSet {
   IntComparator comparator();

   SortedIntSet subSet(int var1, int var2);

   SortedIntSet headSet(int var1);

   SortedIntSet tailSet(int var1);

   int first();

   int last();
}
