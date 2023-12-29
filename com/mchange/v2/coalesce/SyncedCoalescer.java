package com.mchange.v2.coalesce;

import java.util.Iterator;

class SyncedCoalescer implements Coalescer {
   Coalescer inner;

   public SyncedCoalescer(Coalescer var1) {
      this.inner = var1;
   }

   @Override
   public synchronized Object coalesce(Object var1) {
      return this.inner.coalesce(var1);
   }

   @Override
   public synchronized int countCoalesced() {
      return this.inner.countCoalesced();
   }

   @Override
   public synchronized Iterator iterator() {
      return this.inner.iterator();
   }
}
