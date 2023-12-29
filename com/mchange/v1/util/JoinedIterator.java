package com.mchange.v1.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class JoinedIterator implements Iterator {
   Iterator[] its;
   Iterator removeIterator = null;
   boolean permit_removes;
   int cur = 0;

   public JoinedIterator(Iterator[] var1, boolean var2) {
      this.its = var1;
      this.permit_removes = var2;
   }

   @Override
   public boolean hasNext() {
      if (this.cur == this.its.length) {
         return false;
      } else if (this.its[this.cur].hasNext()) {
         return true;
      } else {
         ++this.cur;
         return this.hasNext();
      }
   }

   @Override
   public Object next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.removeIterator = this.its[this.cur];
         return this.removeIterator.next();
      }
   }

   @Override
   public void remove() {
      if (this.permit_removes) {
         if (this.removeIterator != null) {
            this.removeIterator.remove();
            this.removeIterator = null;
         } else {
            throw new IllegalStateException("next() not called, or element already removed.");
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }
}
