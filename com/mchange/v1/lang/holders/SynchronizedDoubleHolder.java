package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedDoubleHolder implements ThreadSafeDoubleHolder {
   double value;

   @Override
   public synchronized double getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(double var1) {
      this.value = var1;
   }
}
