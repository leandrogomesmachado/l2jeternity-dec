package org.napile.primitive.maps;

import java.util.Collection;
import java.util.Set;
import org.napile.primitive.Container;
import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.sets.IntSet;

public interface IntObjectMap<V> extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean containsKey(int var1);

   boolean containsValue(Object var1);

   V get(int var1);

   V put(int var1, V var2);

   V remove(int var1);

   void putAll(IntObjectMap<? extends V> var1);

   @Override
   void clear();

   int[] keys();

   int[] keys(int[] var1);

   IntSet keySet();

   Object[] values();

   V[] values(V[] var1);

   Collection<V> valueCollection();

   Set<IntObjectPair<V>> entrySet();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
