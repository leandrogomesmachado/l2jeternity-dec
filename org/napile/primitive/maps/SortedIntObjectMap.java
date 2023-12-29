package org.napile.primitive.maps;

import java.util.Collection;
import java.util.Set;
import org.napile.primitive.comparators.IntComparator;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.IntSet;

public interface SortedIntObjectMap<V> extends IntObjectMap<V> {
   IntComparator comparator();

   SortedIntObjectMap<V> subMap(int var1, int var2);

   SortedIntObjectMap<V> headMap(int var1);

   SortedIntObjectMap<V> tailMap(int var1);

   int firstKey();

   int lastKey();

   @Override
   int[] keys();

   @Override
   int[] keys(int[] var1);

   @Override
   IntSet keySet();

   @Override
   Object[] values();

   @Override
   V[] values(V[] var1);

   @Override
   Collection<V> valueCollection();

   @Override
   Set<IntObjectPair<V>> entrySet();
}
