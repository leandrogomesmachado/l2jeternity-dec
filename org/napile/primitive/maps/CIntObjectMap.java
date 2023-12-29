package org.napile.primitive.maps;

public interface CIntObjectMap<V> extends IntObjectMap<V> {
   V putIfAbsent(int var1, V var2);

   boolean remove(int var1, Object var2);

   boolean replace(int var1, V var2, V var3);

   V replace(int var1, V var2);
}
