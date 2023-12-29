package com.mchange.util.impl;

class IOHRecord extends IOHRecElem {
   IntObjectHash parent;
   int size = 0;

   IOHRecord(int var1) {
      super(var1, null, null);
   }

   IOHRecElem findInt(int var1) {
      for(Object var2 = this; ((IOHRecElem)var2).next != null; var2 = ((IOHRecElem)var2).next) {
         if (((IOHRecElem)var2).next.num == var1) {
            return (IOHRecElem)var2;
         }
      }

      return null;
   }

   boolean add(int var1, Object var2, boolean var3) {
      IOHRecElem var4 = this.findInt(var1);
      if (var4 != null) {
         if (var3) {
            var4.next = new IOHRecElem(var1, var2, var4.next.next);
         }

         return true;
      } else {
         this.next = new IOHRecElem(var1, var2, this.next);
         ++this.size;
         return false;
      }
   }

   Object remove(int var1) {
      IOHRecElem var2 = this.findInt(var1);
      if (var2 == null) {
         return null;
      } else {
         Object var3 = var2.next.obj;
         var2.next = var2.next.next;
         --this.size;
         if (this.size == 0) {
            this.parent.records[this.num] = null;
         }

         return var3;
      }
   }

   Object get(int var1) {
      IOHRecElem var2 = this.findInt(var1);
      return var2 != null ? var2.next.obj : null;
   }

   IOHRecord split(int var1) {
      IOHRecord var2 = null;
      Object var3 = null;

      for(Object var4 = this; ((IOHRecElem)var4).next != null; var4 = ((IOHRecElem)var4).next) {
         if (Math.abs(((IOHRecElem)var4).next.num % var1) != this.num) {
            if (var2 == null) {
               var2 = new IOHRecord(this.num * 2);
               var3 = var2;
            }

            ((IOHRecElem)var3).next = ((IOHRecElem)var4).next;
            ((IOHRecElem)var4).next = ((IOHRecElem)var4).next.next;
            var3 = ((IOHRecElem)var3).next;
            ((IOHRecElem)var3).next = null;
         }
      }

      return var2;
   }
}
