package com.mchange.util.impl;

import com.mchange.util.StringEnumeration;

public abstract class StringEnumerationHelperBase implements StringEnumeration {
   @Override
   public abstract boolean hasMoreStrings();

   @Override
   public abstract String nextString();

   @Override
   public final boolean hasMoreElements() {
      return this.hasMoreStrings();
   }

   @Override
   public final Object nextElement() {
      return this.nextString();
   }
}
