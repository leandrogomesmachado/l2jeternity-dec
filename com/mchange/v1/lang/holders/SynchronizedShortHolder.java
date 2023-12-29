package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedShortHolder implements ThreadSafeShortHolder {
   short value;

   @Override
   public synchronized short getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(short var1) {
      this.value = var1;
   }
}
