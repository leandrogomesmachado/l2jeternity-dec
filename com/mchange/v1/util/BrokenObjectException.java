package com.mchange.v1.util;

public class BrokenObjectException extends Exception {
   Object broken;

   public BrokenObjectException(Object var1, String var2) {
      super(var2);
      this.broken = var1;
   }

   public BrokenObjectException(Object var1) {
      this.broken = var1;
   }

   public Object getBrokenObject() {
      return this.broken;
   }
}
