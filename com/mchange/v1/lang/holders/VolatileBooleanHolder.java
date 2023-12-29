package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileBooleanHolder implements ThreadSafeBooleanHolder {
   volatile boolean value;

   @Override
   public boolean getValue() {
      return this.value;
   }

   @Override
   public void setValue(boolean var1) {
      this.value = var1;
   }
}
