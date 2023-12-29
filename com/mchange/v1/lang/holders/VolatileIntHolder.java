package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileIntHolder implements ThreadSafeIntHolder {
   volatile int value;

   @Override
   public int getValue() {
      return this.value;
   }

   @Override
   public void setValue(int var1) {
      this.value = var1;
   }
}
