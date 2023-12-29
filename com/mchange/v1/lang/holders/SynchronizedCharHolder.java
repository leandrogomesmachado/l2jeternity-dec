package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedCharHolder implements ThreadSafeCharHolder {
   char value;

   @Override
   public synchronized char getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(char var1) {
      this.value = var1;
   }
}
