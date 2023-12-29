package org.napile.primitive.maps;

import java.util.Collection;
import java.util.Set;
import org.napile.primitive.Container;
import org.napile.primitive.pair.LongObjectPair;
import org.napile.primitive.sets.LongSet;

public interface LongObjectMap<V> extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean containsKey(long var1);

   boolean containsValue(Object var1);

   V get(long var1);

   V put(long var1, V var3);

   V remove(long var1);

   void putAll(LongObjectMap<? extends V> var1);

   @Override
   void clear();

   long[] keys();

   long[] keys(long[] var1);

   LongSet keySet();

   Object[] values();

   V[] values(V[] var1);

   Collection<V> valueCollection();

   Set<LongObjectPair<V>> entrySet();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
