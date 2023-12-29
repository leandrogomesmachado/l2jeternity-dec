package com.mchange.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

public class PotentiallySecondaryRuntimeException extends RuntimeException implements PotentiallySecondary {
   static final String NESTED_MSG = ">>>>>>>>>> NESTED EXCEPTION >>>>>>>>";
   Throwable nested;

   public PotentiallySecondaryRuntimeException(String var1, Throwable var2) {
      super(var1);
      this.nested = var2;
   }

   public PotentiallySecondaryRuntimeException(Throwable var1) {
      this("", var1);
   }

   public PotentiallySecondaryRuntimeException(String var1) {
      this(var1, null);
   }

   public PotentiallySecondaryRuntimeException() {
      this("", null);
   }

   @Override
   public Throwable getNestedThrowable() {
      return this.nested;
   }

   @Override
   public void printStackTrace(PrintWriter var1) {
      super.printStackTrace(var1);
      if (this.nested != null) {
         var1.println(">>>>>>>>>> NESTED EXCEPTION >>>>>>>>");
         this.nested.printStackTrace(var1);
      }
   }

   @Override
   public void printStackTrace(PrintStream var1) {
      super.printStackTrace(var1);
      if (this.nested != null) {
         var1.println("NESTED_MSG");
         this.nested.printStackTrace(var1);
      }
   }

   @Override
   public void printStackTrace() {
      this.printStackTrace(System.err);
   }
}
