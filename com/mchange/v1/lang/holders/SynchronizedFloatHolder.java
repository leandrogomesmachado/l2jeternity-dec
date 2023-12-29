package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedFloatHolder implements ThreadSafeFloatHolder {
   float value;

   @Override
   public synchronized float getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(float var1) {
      this.value = var1;
   }
}
