package com.mchange.io;

import java.io.IOException;

public interface IOByteArrayMap {
   byte[] get(byte[] var1) throws IOException;

   void put(byte[] var1, byte[] var2) throws IOException;

   boolean putNoReplace(byte[] var1, byte[] var2) throws IOException;

   boolean remove(byte[] var1) throws IOException;

   boolean containsKey(byte[] var1) throws IOException;

   IOByteArrayEnumeration keys() throws IOException;
}
