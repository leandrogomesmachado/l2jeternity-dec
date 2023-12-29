package com.mchange.io.impl;

import com.mchange.io.IOStringEnumeration;
import java.io.IOException;

public abstract class IOStringEnumerationHelperBase implements IOStringEnumeration {
   @Override
   public abstract boolean hasMoreStrings() throws IOException;

   @Override
   public abstract String nextString() throws IOException;

   @Override
   public final boolean hasMoreElements() throws IOException {
      return this.hasMoreStrings();
   }

   @Override
   public final Object nextElement() throws IOException {
      return this.nextString();
   }
}
