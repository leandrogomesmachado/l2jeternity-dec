package com.mchange.util.impl;

class IOHRecElem {
   int num;
   Object obj;
   IOHRecElem next;

   IOHRecElem(int var1, Object var2, IOHRecElem var3) {
      this.num = var1;
      this.obj = var2;
      this.next = var3;
   }
}
