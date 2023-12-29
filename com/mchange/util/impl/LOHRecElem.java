package com.mchange.util.impl;

class LOHRecElem {
   long num;
   Object obj;
   LOHRecElem next;

   LOHRecElem(long var1, Object var3, LOHRecElem var4) {
      this.num = var1;
      this.obj = var3;
      this.next = var4;
   }
}
