package com.mchange.util;

import com.mchange.io.IOByteArrayEnumeration;

public interface ByteArrayEnumeration extends MEnumeration, IOByteArrayEnumeration {
   @Override
   byte[] nextBytes();

   @Override
   boolean hasMoreBytes();
}
