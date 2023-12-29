package com.mchange.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

public class PotentiallySecondaryError extends Error implements PotentiallySecondary {
   static final String NESTED_MSG = ">>>>>>>>>> NESTED THROWABLE >>>>>>>>";
   Throwable nested;

   public PotentiallySecondaryError(String var1, Throwable var2) {
      super(var1);
      this.nested = var2;
   }

   public PotentiallySecondaryError(Throwable var1) {
      this("", var1);
   }

   public PotentiallySecondaryError(String var1) {
      this(var1, null);
   }

   public PotentiallySecondaryError() {
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
         var1.println(">>>>>>>>>> NESTED THROWABLE >>>>>>>>");
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
