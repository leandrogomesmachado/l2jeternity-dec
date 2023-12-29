package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedByteHolder implements ThreadSafeByteHolder {
   byte value;

   @Override
   public synchronized byte getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(byte var1) {
      this.value = var1;
   }
}
