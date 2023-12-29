package com.mchange.lang;

import java.io.UnsupportedEncodingException;

/** @deprecated */
public final class StringUtils {
   public static final String[] EMPTY_STRING_ARRAY = new String[0];

   public static String normalString(String var0) {
      return nonEmptyTrimmedOrNull(var0);
   }

   public static boolean nonEmptyString(String var0) {
      return var0 != null && var0.length() > 0;
   }

   public static boolean nonWhitespaceString(String var0) {
      return var0 != null && var0.trim().length() > 0;
   }

   public static String nonEmptyOrNull(String var0) {
      return nonEmptyString(var0) ? var0 : null;
   }

   public static String nonNullOrBlank(String var0) {
      return var0 != null ? var0 : "";
   }

   public static String nonEmptyTrimmedOrNull(String var0) {
      String var1 = var0;
      if (var0 != null) {
         var1 = var0.trim();
         var1 = var1.length() > 0 ? var1 : null;
      }

      return var1;
   }

   public static byte[] getUTF8Bytes(String var0) {
      try {
         return var0.getBytes("UTF8");
      } catch (UnsupportedEncodingException var2) {
         var2.printStackTrace();
         throw new InternalError("UTF8 is an unsupported encoding?!?");
      }
   }
}
