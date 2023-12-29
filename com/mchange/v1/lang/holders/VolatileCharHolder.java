package com.mchange.v1.lang.holders;

/** @deprecated */
public class VolatileCharHolder implements ThreadSafeCharHolder {
   volatile char value;

   @Override
   public char getValue() {
      return this.value;
   }

   @Override
   public void setValue(char var1) {
      this.value = var1;
   }
}
