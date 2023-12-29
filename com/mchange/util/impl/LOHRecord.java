package com.mchange.util.impl;

class LOHRecord extends LOHRecElem {
   LongObjectHash parent;
   int size = 0;

   LOHRecord(long var1) {
      super(var1, null, null);
   }

   LOHRecElem findLong(long var1) {
      for(Object var3 = this; ((LOHRecElem)var3).next != null; var3 = ((LOHRecElem)var3).next) {
         if (((LOHRecElem)var3).next.num == var1) {
            return (LOHRecElem)var3;
         }
      }

      return null;
   }

   boolean add(long var1, Object var3, boolean var4) {
      LOHRecElem var5 = this.findLong(var1);
      if (var5 != null) {
         if (var4) {
            var5.next = new LOHRecElem(var1, var3, var5.next.next);
         }

         return true;
      } else {
         this.next = new LOHRecElem(var1, var3, this.next);
         ++this.size;
         return false;
      }
   }

   Object remove(long var1) {
      LOHRecElem var3 = this.findLong(var1);
      if (var3 == null) {
         return null;
      } else {
         Object var4 = var3.next.obj;
         var3.next = var3.next.next;
         --this.size;
         if (this.size == 0) {
            this.parent.records[(int)this.num] = null;
         }

         return var4;
      }
   }

   Object get(long var1) {
      LOHRecElem var3 = this.findLong(var1);
      return var3 != null ? var3.next.obj : null;
   }

   LOHRecord split(int var1) {
      LOHRecord var2 = null;
      Object var3 = null;

      for(Object var4 = this; ((LOHRecElem)var4).next != null; var4 = ((LOHRecElem)var4).next) {
         if (((LOHRecElem)var4).next.num % (long)var1 != this.num) {
            if (var2 == null) {
               var2 = new LOHRecord(this.num * 2L);
               var3 = var2;
            }

            ((LOHRecElem)var3).next = ((LOHRecElem)var4).next;
            ((LOHRecElem)var4).next = ((LOHRecElem)var4).next.next;
            var3 = ((LOHRecElem)var3).next;
            ((LOHRecElem)var3).next = null;
         }
      }

      return var2;
   }
}
