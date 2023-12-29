package com.mchange.io;

import java.io.IOException;

public interface IOByteArrayEnumeration extends IOEnumeration {
   byte[] nextBytes() throws IOException;

   boolean hasMoreBytes() throws IOException;

   @Override
   Object nextElement() throws IOException;

   @Override
   boolean hasMoreElements() throws IOException;
}
