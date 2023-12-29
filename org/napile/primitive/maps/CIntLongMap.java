package org.napile.primitive.maps;

public interface CIntLongMap extends IntLongMap {
   long putIfAbsent(int var1, long var2);

   boolean remove(int var1, long var2);

   boolean replace(int var1, long var2, long var4);

   long replace(int var1, long var2);
}
