package com.mchange.lang;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ThrowableUtils {
   public static String extractStackTrace(Throwable var0) {
      StringWriter var1 = new StringWriter();
      PrintWriter var2 = new PrintWriter(var1);
      var0.printStackTrace(var2);
      var2.flush();
      return var1.toString();
   }

   public static boolean isChecked(Throwable var0) {
      return var0 instanceof Exception && !(var0 instanceof RuntimeException);
   }

   public static boolean isUnchecked(Throwable var0) {
      return !isChecked(var0);
   }

   private ThrowableUtils() {
   }
}
