package com.mchange.util;

import com.mchange.io.IOStringEnumeration;
import com.mchange.io.IOStringObjectMap;

public interface StringObjectMap extends IOStringObjectMap {
   @Override
   Object get(String var1);

   @Override
   void put(String var1, Object var2);

   @Override
   boolean putNoReplace(String var1, Object var2);

   @Override
   boolean remove(String var1);

   @Override
   boolean containsKey(String var1);

   @Override
   IOStringEnumeration keys();

   StringEnumeration mkeys();
}
