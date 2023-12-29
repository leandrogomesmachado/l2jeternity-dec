package com.mchange.v2.util;

import com.mchange.v2.lang.VersionUtils;

public class ResourceClosedException extends RuntimeException {
   Throwable rootCause;

   public ResourceClosedException(String var1, Throwable var2) {
      super(var1);
      this.setRootCause(var2);
   }

   public ResourceClosedException(Throwable var1) {
      this.setRootCause(var1);
   }

   public ResourceClosedException(String var1) {
      super(var1);
   }

   public ResourceClosedException() {
   }

   @Override
   public Throwable getCause() {
      return this.rootCause;
   }

   private void setRootCause(Throwable var1) {
      this.rootCause = var1;
      if (VersionUtils.isAtLeastJavaVersion14()) {
         this.initCause(var1);
      }
   }
}
