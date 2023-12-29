package com.mchange.io;

import java.io.IOException;

public interface IOStringObjectMap {
   Object get(String var1) throws IOException;

   void put(String var1, Object var2) throws IOException;

   boolean putNoReplace(String var1, Object var2) throws IOException;

   boolean remove(String var1) throws IOException;

   boolean containsKey(String var1) throws IOException;

   IOStringEnumeration keys() throws IOException;
}
