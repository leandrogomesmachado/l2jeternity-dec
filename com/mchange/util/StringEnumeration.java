package com.mchange.util;

import com.mchange.io.IOStringEnumeration;

public interface StringEnumeration extends MEnumeration, IOStringEnumeration {
   @Override
   boolean hasMoreStrings();

   @Override
   String nextString();
}
