package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedIntHolder implements ThreadSafeIntHolder {
   int value;

   @Override
   public synchronized int getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(int var1) {
      this.value = var1;
   }
}
