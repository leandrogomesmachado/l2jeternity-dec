package com.mchange.v2.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class WrapperIterator implements Iterator {
   protected static final Object SKIP_TOKEN = new Object();
   static final boolean DEBUG = true;
   Iterator inner;
   boolean supports_remove;
   Object lastOut = null;
   Object nextOut = SKIP_TOKEN;

   public WrapperIterator(Iterator var1, boolean var2) {
      this.inner = var1;
      this.supports_remove = var2;
   }

   public WrapperIterator(Iterator var1) {
      this(var1, false);
   }

   @Override
   public boolean hasNext() {
      this.findNext();
      return this.nextOut != SKIP_TOKEN;
   }

   private void findNext() {
      if (this.nextOut == SKIP_TOKEN) {
         while(this.inner.hasNext() && this.nextOut == SKIP_TOKEN) {
            this.nextOut = this.transformObject(this.inner.next());
         }
      }
   }

   @Override
   public Object next() {
      this.findNext();
      if (this.nextOut != SKIP_TOKEN) {
         this.lastOut = this.nextOut;
         this.nextOut = SKIP_TOKEN;
         if (this.nextOut == SKIP_TOKEN && this.lastOut != SKIP_TOKEN) {
            return this.lastOut;
         } else {
            throw new AssertionError("Better check out this weird WrapperIterator logic!");
         }
      } else {
         throw new NoSuchElementException();
      }
   }

   @Override
   public void remove() {
      if (this.supports_remove) {
         if (this.nextOut != SKIP_TOKEN) {
            throw new UnsupportedOperationException(this.getClass().getName() + " cannot support remove after" + " hasNext() has been called!");
         } else if (this.lastOut != SKIP_TOKEN) {
            this.inner.remove();
         } else {
            throw new NoSuchElementException();
         }
      } else {
         throw new UnsupportedOperationException();
      }
   }

   protected abstract Object transformObject(Object var1);
}
