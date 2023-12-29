package com.mchange.util;

public interface LongObjectMap {
   Object get(long var1);

   void put(long var1, Object var3);

   boolean putNoReplace(long var1, Object var3);

   Object remove(long var1);

   boolean containsLong(long var1);

   long getSize();
}
