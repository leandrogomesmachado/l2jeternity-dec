package com.mchange.v2.coalesce;

import java.util.Iterator;

class CoalescerIterator implements Iterator {
   Iterator inner;

   CoalescerIterator(Iterator var1) {
      this.inner = var1;
   }

   @Override
   public boolean hasNext() {
      return this.inner.hasNext();
   }

   @Override
   public Object next() {
      return this.inner.next();
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Objects cannot be removed from a coalescer!");
   }
}
