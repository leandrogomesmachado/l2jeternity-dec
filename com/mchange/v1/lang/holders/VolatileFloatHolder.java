package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileFloatHolder implements ThreadSafeFloatHolder {
   volatile float value;

   @Override
   public float getValue() {
      return this.value;
   }

   @Override
   public void setValue(float var1) {
      this.value = var1;
   }
}
