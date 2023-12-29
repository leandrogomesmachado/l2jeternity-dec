package com.mchange.v3.decode;

public class CannotDecodeException extends Exception {
   public CannotDecodeException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public CannotDecodeException(String var1) {
      super(var1);
   }

   public CannotDecodeException(Throwable var1) {
      super(var1);
   }

   public CannotDecodeException() {
   }
}
