package com.mchange.v1.lang;

public final class BooleanUtils {
   public static boolean parseBoolean(String var0) throws IllegalArgumentException {
      if (var0.equals("true")) {
         return true;
      } else if (var0.equals("false")) {
         return false;
      } else {
         throw new IllegalArgumentException("\"str\" is neither \"true\" nor \"false\".");
      }
   }

   private BooleanUtils() {
   }
}
