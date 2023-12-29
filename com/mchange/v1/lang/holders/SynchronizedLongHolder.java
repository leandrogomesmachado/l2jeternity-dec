package com.mchange.v1.lang.holders;

/** @deprecated */
public class SynchronizedLongHolder implements ThreadSafeLongHolder {
   long value;

   @Override
   public synchronized long getValue() {
      return this.value;
   }

   @Override
   public synchronized void setValue(long var1) {
      this.value = var1;
   }

   public SynchronizedLongHolder(long var1) {
      this.value = var1;
   }

   public SynchronizedLongHolder() {
      this(0L);
   }
}
