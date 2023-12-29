package com.mchange.v1.util;

import com.mchange.util.AssertException;

public class DebugUtils {
   private DebugUtils() {
   }

   public static void myAssert(boolean var0) {
      if (!var0) {
         throw new AssertException();
      }
   }

   public static void myAssert(boolean var0, String var1) {
      if (!var0) {
         throw new AssertException(var1);
      }
   }
}
