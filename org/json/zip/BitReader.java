package org.json.zip;

import java.io.IOException;

public interface BitReader {
   boolean bit() throws IOException;

   long nrBits();

   boolean pad(int var1) throws IOException;

   int read(int var1) throws IOException;
}
