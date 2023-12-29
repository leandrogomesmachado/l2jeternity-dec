package org.napile.primitive.maps;

import java.util.Set;
import org.napile.primitive.Container;
import org.napile.primitive.collections.LongCollection;
import org.napile.primitive.pair.IntLongPair;
import org.napile.primitive.sets.IntSet;

public interface IntLongMap extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean containsKey(int var1);

   boolean containsValue(long var1);

   long get(int var1);

   long put(int var1, long var2);

   long remove(int var1);

   void putAll(IntLongMap var1);

   @Override
   void clear();

   int[] keys();

   int[] keys(int[] var1);

   IntSet keySet();

   long[] values();

   long[] values(long[] var1);

   LongCollection valueCollection();

   Set<IntLongPair> entrySet();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
