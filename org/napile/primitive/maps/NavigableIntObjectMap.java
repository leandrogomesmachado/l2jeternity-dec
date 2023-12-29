package org.napile.primitive.maps;

import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.NavigableIntSet;

public interface NavigableIntObjectMap<V> extends SortedIntObjectMap<V> {
   IntObjectPair<V> lowerEntry(int var1);

   int lowerKey(int var1);

   IntObjectPair<V> floorEntry(int var1);

   int floorKey(int var1);

   IntObjectPair<V> ceilingEntry(int var1);

   int ceilingKey(int var1);

   IntObjectPair<V> higherEntry(int var1);

   int higherKey(int var1);

   IntObjectPair<V> firstEntry();

   IntObjectPair<V> lastEntry();

   IntObjectPair<V> pollFirstEntry();

   IntObjectPair<V> pollLastEntry();

   NavigableIntObjectMap<V> descendingMap();

   NavigableIntSet navigableKeySet();

   NavigableIntSet descendingKeySet();

   NavigableIntObjectMap<V> subMap(int var1, boolean var2, int var3, boolean var4);

   NavigableIntObjectMap<V> headMap(int var1, boolean var2);

   NavigableIntObjectMap<V> tailMap(int var1, boolean var2);

   @Override
   SortedIntObjectMap<V> subMap(int var1, int var2);

   @Override
   SortedIntObjectMap<V> headMap(int var1);

   @Override
   SortedIntObjectMap<V> tailMap(int var1);
}
