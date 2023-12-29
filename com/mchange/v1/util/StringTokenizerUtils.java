package com.mchange.v1.util;

import java.util.StringTokenizer;

public final class StringTokenizerUtils {
   public static String[] tokenizeToArray(String var0, String var1, boolean var2) {
      StringTokenizer var3 = new StringTokenizer(var0, var1, var2);
      String[] var4 = new String[var3.countTokens()];

      for(int var5 = 0; var3.hasMoreTokens(); ++var5) {
         var4[var5] = var3.nextToken();
      }

      return var4;
   }

   public static String[] tokenizeToArray(String var0, String var1) {
      return tokenizeToArray(var0, var1, false);
   }

   public static String[] tokenizeToArray(String var0) {
      return tokenizeToArray(var0, " \t\r\n");
   }
}
