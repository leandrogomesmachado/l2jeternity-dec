package com.mchange.v1.util;

import com.mchange.lang.PotentiallySecondaryRuntimeException;

public class UnexpectedException extends PotentiallySecondaryRuntimeException {
   public UnexpectedException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public UnexpectedException(Throwable var1) {
      super(var1);
   }

   public UnexpectedException(String var1) {
      super(var1);
   }

   public UnexpectedException() {
   }

   /** @deprecated */
   public UnexpectedException(Throwable var1, String var2) {
      this(var2, var1);
   }
}
