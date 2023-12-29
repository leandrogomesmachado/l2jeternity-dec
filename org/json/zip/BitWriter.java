package org.json.zip;

import java.io.IOException;

public interface BitWriter {
   void one() throws IOException;

   void pad(int var1) throws IOException;

   void write(int var1, int var2) throws IOException;

   void zero() throws IOException;
}
