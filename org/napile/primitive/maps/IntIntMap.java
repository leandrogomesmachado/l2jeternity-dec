package org.napile.primitive.maps;

import java.util.Set;
import org.napile.primitive.Container;
import org.napile.primitive.collections.IntCollection;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.sets.IntSet;

public interface IntIntMap extends Container {
   @Override
   int size();

   @Override
   boolean isEmpty();

   boolean containsKey(int var1);

   boolean containsValue(int var1);

   int get(int var1);

   int put(int var1, int var2);

   int remove(int var1);

   void putAll(IntIntMap var1);

   @Override
   void clear();

   int[] keys();

   int[] keys(int[] var1);

   IntSet keySet();

   int[] values();

   int[] values(int[] var1);

   IntCollection valueCollection();

   Set<IntIntPair> entrySet();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
