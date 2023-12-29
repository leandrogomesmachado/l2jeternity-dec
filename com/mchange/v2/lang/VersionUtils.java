package com.mchange.v2.lang;

import com.mchange.v1.util.StringTokenizerUtils;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;

public final class VersionUtils {
   private static final MLogger logger = MLog.getLogger(VersionUtils.class);
   private static final int[] DFLT_VERSION_ARRAY = new int[]{1, 1};
   private static final int[] JDK_VERSION_ARRAY;
   private static final int JDK_VERSION;
   private static final Integer NUM_BITS;

   public static Integer jvmNumberOfBits() {
      return NUM_BITS;
   }

   public static boolean isJavaVersion10() {
      return JDK_VERSION == 10;
   }

   public static boolean isJavaVersion11() {
      return JDK_VERSION == 11;
   }

   public static boolean isJavaVersion12() {
      return JDK_VERSION == 12;
   }

   public static boolean isJavaVersion13() {
      return JDK_VERSION == 13;
   }

   public static boolean isJavaVersion14() {
      return JDK_VERSION == 14;
   }

   public static boolean isJavaVersion15() {
      return JDK_VERSION == 15;
   }

   public static boolean isAtLeastJavaVersion10() {
      return JDK_VERSION >= 10;
   }

   public static boolean isAtLeastJavaVersion11() {
      return JDK_VERSION >= 11;
   }

   public static boolean isAtLeastJavaVersion12() {
      return JDK_VERSION >= 12;
   }

   public static boolean isAtLeastJavaVersion13() {
      return JDK_VERSION >= 13;
   }

   public static boolean isAtLeastJavaVersion14() {
      return JDK_VERSION >= 14;
   }

   public static boolean isAtLeastJavaVersion15() {
      return JDK_VERSION >= 15;
   }

   public static boolean isAtLeastJavaVersion16() {
      return JDK_VERSION >= 16;
   }

   public static boolean isAtLeastJavaVersion17() {
      return JDK_VERSION >= 17;
   }

   public static int[] extractVersionNumberArray(String var0) throws NumberFormatException {
      return extractVersionNumberArray(var0, var0.split("\\D+"));
   }

   public static int[] extractVersionNumberArray(String var0, String var1) throws NumberFormatException {
      String[] var2 = StringTokenizerUtils.tokenizeToArray(var0, var1, false);
      return extractVersionNumberArray(var0, var2);
   }

   private static int[] extractVersionNumberArray(String var0, String[] var1) throws NumberFormatException {
      int var2 = var1.length;
      int[] var3 = new int[var2];

      for(int var4 = 0; var4 < var2; ++var4) {
         try {
            var3[var4] = Integer.parseInt(var1[var4]);
         } catch (NumberFormatException var7) {
            if (var4 <= 1) {
               throw var7;
            }

            if (logger.isLoggable(MLevel.INFO)) {
               logger.log(
                  MLevel.INFO,
                  "JVM version string ("
                     + var0
                     + ") contains non-integral component ("
                     + var1[var4]
                     + "). Using precending components only to resolve JVM version."
               );
            }

            int[] var6 = new int[var4];
            System.arraycopy(var3, 0, var6, 0, var4);
            var3 = var6;
            break;
         }
      }

      return var3;
   }

   public boolean prefixMatches(int[] var1, int[] var2) {
      if (var1.length > var2.length) {
         return false;
      } else {
         int var3 = 0;

         for(int var4 = var1.length; var3 < var4; ++var3) {
            if (var1[var3] != var2[var3]) {
               return false;
            }
         }

         return true;
      }
   }

   public static int lexicalCompareVersionNumberArrays(int[] var0, int[] var1) {
      int var2 = var0.length;
      int var3 = var1.length;

      for(int var4 = 0; var4 < var2; ++var4) {
         if (var4 == var3) {
            return 1;
         }

         if (var0[var4] > var1[var4]) {
            return 1;
         }

         if (var0[var4] < var1[var4]) {
            return -1;
         }
      }

      return var3 > var2 ? -1 : 0;
   }

   static {
      String var0 = System.getProperty("java.version");
      int[] var1;
      if (var0 == null) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.warning("Could not find java.version System property. Defaulting to JDK 1.1");
         }

         var1 = DFLT_VERSION_ARRAY;
      } else {
         try {
            var1 = extractVersionNumberArray(var0);
         } catch (NumberFormatException var6) {
            if (logger.isLoggable(MLevel.WARNING)) {
               logger.warning("java.version ''" + var0 + "'' could not be parsed. Defaulting to JDK 1.1.");
            }

            var1 = DFLT_VERSION_ARRAY;
         }
      }

      int var2 = 0;
      if (var1.length > 0) {
         var2 += var1[0] * 10;
      }

      if (var1.length > 1) {
         var2 += var1[1];
      }

      JDK_VERSION_ARRAY = var1;
      JDK_VERSION = var2;

      Integer var3;
      try {
         String var4 = System.getProperty("sun.arch.data.model");
         if (var4 == null) {
            var3 = null;
         } else {
            var3 = new Integer(var4);
         }
      } catch (Exception var5) {
         var3 = null;
      }

      if (var3 != null && var3 != 32 && var3 != 64) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.warning("Determined a surprising jvmNumerOfBits: " + var3 + ". Setting jvmNumberOfBits to unknown (null).");
         }

         NUM_BITS = null;
      } else {
         NUM_BITS = var3;
      }
   }
}
