package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedBooleanHolder implements ThreadSafeBooleanHolder {
   boolean value;

   @Override
   public synchronized boolean getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(boolean var1) {
      this.value = var1;
   }
}
