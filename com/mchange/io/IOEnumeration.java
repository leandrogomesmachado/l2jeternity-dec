package com.mchange.io;

import java.io.IOException;

public interface IOEnumeration {
   boolean hasMoreElements() throws IOException;

   Object nextElement() throws IOException;
}
