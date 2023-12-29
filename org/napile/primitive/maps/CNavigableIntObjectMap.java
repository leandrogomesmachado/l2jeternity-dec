package org.napile.primitive.maps;

import org.napile.primitive.sets.NavigableIntSet;

public interface CNavigableIntObjectMap<V> extends CIntObjectMap<V>, NavigableIntObjectMap<V> {
   CNavigableIntObjectMap<V> subMap(int var1, boolean var2, int var3, boolean var4);

   CNavigableIntObjectMap<V> headMap(int var1, boolean var2);

   CNavigableIntObjectMap<V> tailMap(int var1, boolean var2);

   CNavigableIntObjectMap<V> subMap(int var1, int var2);

   CNavigableIntObjectMap<V> headMap(int var1);

   CNavigableIntObjectMap<V> tailMap(int var1);

   CNavigableIntObjectMap<V> descendingMap();

   @Override
   NavigableIntSet navigableKeySet();

   NavigableIntSet keySet();

   @Override
   NavigableIntSet descendingKeySet();
}
