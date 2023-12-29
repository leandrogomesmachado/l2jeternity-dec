package com.mchange.util.impl;

class CircularListRecord {
   Object object;
   CircularListRecord next;
   CircularListRecord prev;

   CircularListRecord(Object var1, CircularListRecord var2, CircularListRecord var3) {
      this.object = var1;
      this.prev = var2;
      this.next = var3;
   }

   CircularListRecord(Object var1) {
      this.object = var1;
      this.prev = this;
      this.next = this;
   }
}
