package com.mchange.v1.util;

import java.util.NoSuchElementException;

public abstract class WrapperUIterator implements UIterator {
   protected static final Object SKIP_TOKEN = new Object();
   static final boolean DEBUG = true;
   UIterator inner;
   boolean supports_remove;
   Object lastOut = null;
   Object nextOut = SKIP_TOKEN;

   public WrapperUIterator(UIterator var1, boolean var2) {
      this.inner = var1;
      this.supports_remove = var2;
   }

   public WrapperUIterator(UIterator var1) {
      this(var1, false);
   }

   @Override
   public boolean hasNext() throws Exception {
      this.findNext();
      return this.nextOut != SKIP_TOKEN;
   }

   private void findNext() throws Exception {
      if (this.nextOut == SKIP_TOKEN) {
         while(this.inner.hasNext() && this.nextOut == SKIP_TOKEN) {
            this.nextOut = this.transformObject(this.inner.next());
         }
      }
   }

   @Override
   public Object next() throws NoSuchElementException, Exception {
      this.findNext();
      if (this.nextOut == SKIP_TOKEN) {
         throw new NoSuchElementException();
      } else {
         this.lastOut = this.nextOut;
         this.nextOut = SKIP_TOKEN;
         DebugUtils.myAssert(this.nextOut == SKIP_TOKEN && this.lastOut != SKIP_TOKEN);

         assert this.nextOut == SKIP_TOKEN && this.lastOut != SKIP_TOKEN;

         return this.lastOut;
      }
   }

   @Override
   public void remove() throws Exception {
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

   @Override
   public void close() throws Exception {
      this.inner.close();
   }

   protected abstract Object transformObject(Object var1) throws Exception;
}
