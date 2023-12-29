package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileByteHolder implements ThreadSafeByteHolder {
   volatile byte value;

   @Override
   public byte getValue() {
      return this.value;
   }

   @Override
   public void setValue(byte var1) {
      this.value = var1;
   }
}
