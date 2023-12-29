package com.mchange.util.impl;

import com.mchange.util.IntEnumeration;

public abstract class IntEnumerationHelperBase implements IntEnumeration {
   @Override
   public abstract boolean hasMoreInts();

   @Override
   public abstract int nextInt();

   @Override
   public final boolean hasMoreElements() {
      return this.hasMoreInts();
   }

   @Override
   public final Object nextElement() {
      return new Integer(this.nextInt());
   }
}
