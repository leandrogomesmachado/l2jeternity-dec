package com.mchange.util;

import com.mchange.io.IOEnumeration;
import com.mchange.util.impl.EmptyMEnumeration;
import java.util.Enumeration;

public interface MEnumeration extends IOEnumeration, Enumeration {
   MEnumeration EMPTY = EmptyMEnumeration.SINGLETON;

   @Override
   Object nextElement();

   @Override
   boolean hasMoreElements();
}
