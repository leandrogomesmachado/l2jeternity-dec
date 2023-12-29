package com.mchange.util;

import com.mchange.io.IOByteArrayEnumeration;
import com.mchange.io.IOByteArrayMap;

public interface ByteArrayMap extends IOByteArrayMap {
   @Override
   byte[] get(byte[] var1);

   @Override
   void put(byte[] var1, byte[] var2);

   @Override
   boolean putNoReplace(byte[] var1, byte[] var2);

   @Override
   boolean remove(byte[] var1);

   @Override
   boolean containsKey(byte[] var1);

   @Override
   IOByteArrayEnumeration keys();

   ByteArrayEnumeration mkeys();
}
