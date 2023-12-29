package com.mchange.util.impl;

import java.util.Enumeration;
import java.util.NoSuchElementException;

class CircularListEnumeration implements Enumeration {
   boolean forward;
   boolean terminated;
   boolean done;
   CircularListRecord startRecord;
   CircularListRecord lastRecord;

   CircularListEnumeration(CircularList var1, boolean var2, boolean var3) {
      if (var1.firstRecord == null) {
         this.done = true;
      } else {
         this.done = false;
         this.forward = var2;
         this.terminated = var3;
         this.startRecord = var2 ? var1.firstRecord : var1.firstRecord.prev;
         this.lastRecord = var2 ? this.startRecord.prev : this.startRecord;
      }
   }

   @Override
   public boolean hasMoreElements() {
      return !this.done;
   }

   @Override
   public Object nextElement() {
      if (this.done) {
         throw new NoSuchElementException();
      } else {
         this.lastRecord = this.forward ? this.lastRecord.next : this.lastRecord.prev;
         if (this.terminated && this.lastRecord == (this.forward ? this.startRecord.prev : this.startRecord)) {
            this.done = true;
         }

         return this.lastRecord.object;
      }
   }
}
