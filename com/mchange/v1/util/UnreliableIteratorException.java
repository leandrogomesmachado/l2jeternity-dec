package com.mchange.v1.util;

import com.mchange.lang.PotentiallySecondaryException;

public class UnreliableIteratorException extends PotentiallySecondaryException {
   public UnreliableIteratorException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public UnreliableIteratorException(Throwable var1) {
      super(var1);
   }

   public UnreliableIteratorException(String var1) {
      super(var1);
   }

   public UnreliableIteratorException() {
   }
}
