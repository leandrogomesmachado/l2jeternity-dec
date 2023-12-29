package com.mchange.util;

public interface IntObjectMap {
   Object get(int var1);

   void put(int var1, Object var2);

   boolean putNoReplace(int var1, Object var2);

   Object remove(int var1);

   boolean containsInt(int var1);

   int getSize();

   void clear();

   IntEnumeration ints();
}
