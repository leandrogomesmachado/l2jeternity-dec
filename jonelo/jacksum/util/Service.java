package jonelo.jacksum.util;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import jonelo.sugar.util.GeneralString;

public class Service {
   private static final char[] HEX = "0123456789abcdef".toCharArray();

   public static String right(long var0, int var2) {
      StringBuffer var3 = new StringBuffer(var0 + "");

      while(var3.length() < var2) {
         var3.insert(0, ' ');
      }

      return var3.toString();
   }

   public static String decformat(long var0, String var2) {
      DecimalFormat var3 = new DecimalFormat(var2);
      return var3.format(var0);
   }

   public static String hexformat(long var0, int var2) {
      StringBuffer var3 = new StringBuffer(Long.toHexString(var0));

      while(var3.length() < var2) {
         var3.insert(0, '0');
      }

      return var3.toString();
   }

   public static String hexformat(long var0, int var2, int var3, char var4) {
      StringBuffer var5 = new StringBuffer(Long.toHexString(var0));

      while(var5.length() < var2) {
         var5.insert(0, '0');
      }

      if (var3 > 0) {
         var5 = insertBlanks(var5, var3, var4);
      }

      return var5.toString();
   }

   public static String format(byte[] var0) {
      return format(var0, false);
   }

   private static StringBuffer insertBlanks(StringBuffer var0, int var1, char var2) {
      int var3 = var0.length() / 2;
      if (var3 <= var1) {
         return var0;
      } else {
         StringBuffer var4 = new StringBuffer(var0.length() + (var3 / var1 - 1));
         int var5 = var1 * 2;

         for(int var6 = 0; var6 < var0.length(); ++var6) {
            if (var6 > 0 && var6 % var5 == 0) {
               var4.append(var2);
            }

            var4.append(var0.charAt(var6));
         }

         return var4;
      }
   }

   public static String format(byte[] var0, boolean var1, int var2, char var3) {
      if (var0 == null) {
         return "";
      } else {
         StringBuffer var4 = new StringBuffer(var0.length * 2);

         for(int var6 = 0; var6 < var0.length; ++var6) {
            int var5 = var0[var6] & 255;
            var4.append(HEX[var5 >>> 4]);
            var4.append(HEX[var5 & 15]);
         }

         if (var2 > 0) {
            var4 = insertBlanks(var4, var2, var3);
         }

         return var1 ? var4.toString().toUpperCase() : var4.toString();
      }
   }

   public static String formatAsBits(byte[] var0) {
      if (var0 == null) {
         return "";
      } else {
         StringBuffer var1 = new StringBuffer(var0.length);
         BigInteger var2 = new BigInteger(1, var0);
         var1.append(var2.toString(2));

         while(var1.length() < var0.length * 8) {
            var1.insert(0, '0');
         }

         return var1.toString();
      }
   }

   public static String format(byte[] var0, boolean var1) {
      return format(var0, false, 0, ' ');
   }

   public static boolean isSymbolicLink(File var0) {
      if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
         return false;
      } else {
         try {
            String var1 = var0.getCanonicalPath();
            String var2 = var0.getAbsolutePath();
            var2 = GeneralString.replaceString(var2, "/./", "/");
            if (var2.endsWith("/.")) {
               return false;
            } else {
               return !var2.equals(var1);
            }
         } catch (IOException var3) {
            System.err.println(var3);
            return true;
         }
      }
   }

   public static void setLongInByteArray(long var0, byte[] var2) throws IndexOutOfBoundsException {
      setLongInByteArray(var0, var2, 0);
   }

   public static void setLongInByteArray(long var0, byte[] var2, int var3) throws IndexOutOfBoundsException {
      byte[] var4 = new byte[8];

      for(int var7 = 0; var7 < 8; ++var7) {
         long var5 = var0 & 255L;
         var4[var7] = (byte)((int)var5);
         var0 >>= 8;
      }

      for(int var8 = 0; var8 < 8; ++var8) {
         var2[var8 + var3] = var4[7 - var8];
      }
   }

   public static void setIntInByteArray(int var0, byte[] var1) throws IndexOutOfBoundsException {
      setIntInByteArray(var0, var1, 0);
   }

   public static void setIntInByteArray(int var0, byte[] var1, int var2) throws IndexOutOfBoundsException {
      byte[] var3 = new byte[4];

      for(int var5 = 0; var5 < 4; ++var5) {
         int var4 = var0 & 0xFF;
         var3[var5] = (byte)var4;
         var0 >>= 8;
      }

      for(int var6 = 0; var6 < 4; ++var6) {
         var1[var6 + var2] = var3[3 - var6];
      }
   }

   public static final String duration(long var0) {
      long var2 = 0L;
      long var4 = 0L;
      long var6 = 0L;
      long var8 = 0L;
      long var10 = 0L;
      var2 = var0 % 1000L;
      var0 /= 1000L;
      if (var0 > 0L) {
         var4 = var0 % 60L;
         var0 /= 60L;
      }

      if (var0 > 0L) {
         var6 = var0 % 60L;
         var0 /= 60L;
      }

      if (var0 > 0L) {
         var8 = var0 % 24L;
         var0 /= 24L;
      }

      return var0 + " d, " + var8 + " h, " + var6 + " m, " + var4 + " s, " + var2 + " ms";
   }
}
