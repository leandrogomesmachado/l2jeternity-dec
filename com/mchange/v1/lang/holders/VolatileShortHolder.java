package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileShortHolder implements ThreadSafeShortHolder {
   volatile short value;

   @Override
   public short getValue() {
      return this.value;
   }

   @Override
   public void setValue(short var1) {
      this.value = var1;
   }
}
