package com.mchange.lang;

import com.mchange.v2.lang.VersionUtils;
import java.io.PrintStream;
import java.io.PrintWriter;

/** @deprecated */
public class PotentiallySecondaryException extends Exception implements PotentiallySecondary {
   static final String NESTED_MSG = ">>>>>>>>>> NESTED EXCEPTION >>>>>>>>";
   Throwable nested;

   public PotentiallySecondaryException(String var1, Throwable var2) {
      super(var1);
      this.nested = var2;
   }

   public PotentiallySecondaryException(Throwable var1) {
      this("", var1);
   }

   public PotentiallySecondaryException(String var1) {
      this(var1, null);
   }

   public PotentiallySecondaryException() {
      this("", null);
   }

   @Override
   public Throwable getNestedThrowable() {
      return this.nested;
   }

   private void setNested(Throwable var1) {
      this.nested = var1;
      if (VersionUtils.isAtLeastJavaVersion14()) {
         this.initCause(var1);
      }
   }

   @Override
   public void printStackTrace(PrintWriter var1) {
      super.printStackTrace(var1);
      if (!VersionUtils.isAtLeastJavaVersion14() && this.nested != null) {
         var1.println(">>>>>>>>>> NESTED EXCEPTION >>>>>>>>");
         this.nested.printStackTrace(var1);
      }
   }

   @Override
   public void printStackTrace(PrintStream var1) {
      super.printStackTrace(var1);
      if (!VersionUtils.isAtLeastJavaVersion14() && this.nested != null) {
         var1.println("NESTED_MSG");
         this.nested.printStackTrace(var1);
      }
   }

   @Override
   public void printStackTrace() {
      if (VersionUtils.isAtLeastJavaVersion14()) {
         super.printStackTrace();
      } else {
         this.printStackTrace(System.err);
      }
   }
}
