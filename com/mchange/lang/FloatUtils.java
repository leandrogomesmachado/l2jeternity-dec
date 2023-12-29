package com.mchange.lang;

public final class FloatUtils {
   static final boolean DEBUG = true;
   private static FloatUtils.FParser fParser;

   public static byte[] byteArrayFromFloat(float var0) {
      int var1 = Float.floatToIntBits(var0);
      return IntegerUtils.byteArrayFromInt(var1);
   }

   public static float floatFromByteArray(byte[] var0, int var1) {
      int var2 = IntegerUtils.intFromByteArray(var0, var1);
      return Float.intBitsToFloat(var2);
   }

   public static float parseFloat(String var0, float var1) {
      if (var0 == null) {
         return var1;
      } else {
         try {
            return fParser.parseFloat(var0);
         } catch (NumberFormatException var3) {
            return var1;
         }
      }
   }

   public static float parseFloat(String var0) throws NumberFormatException {
      return fParser.parseFloat(var0);
   }

   public static String floatToString(float var0, int var1) {
      boolean var2 = var0 < 0.0F;
      var0 = var2 ? -var0 : var0;
      long var3 = Math.round((double)var0 * Math.pow(10.0, (double)(-var1)));
      String var5 = String.valueOf(var3);
      if (var3 == 0L) {
         return var5;
      } else {
         int var6 = var5.length();
         int var7 = var6 + var1;
         StringBuffer var8 = new StringBuffer(32);
         if (var2) {
            var8.append('-');
         }

         if (var7 <= 0) {
            var8.append("0.");

            for(int var9 = 0; var9 < -var7; ++var9) {
               var8.append('0');
            }

            var8.append(var5);
         } else {
            var8.append(var5.substring(0, Math.min(var7, var6)));
            if (var7 < var6) {
               var8.append('.');
               var8.append(var5.substring(var7));
            } else if (var7 > var6) {
               int var12 = 0;

               for(int var10 = var7 - var6; var12 < var10; ++var12) {
                  var8.append('0');
               }
            }
         }

         return var8.toString();
      }
   }

   static {
      try {
         fParser = new FloatUtils.J12FParser();
         fParser.parseFloat("0.1");
      } catch (NoSuchMethodError var1) {
         System.err.println("com.mchange.lang.FloatUtils: reconfiguring for Java 1.1 environment");
         fParser = new FloatUtils.J11FParser();
      }
   }

   interface FParser {
      float parseFloat(String var1) throws NumberFormatException;
   }

   static class J11FParser implements FloatUtils.FParser {
      @Override
      public float parseFloat(String var1) throws NumberFormatException {
         return new Float(var1);
      }
   }

   static class J12FParser implements FloatUtils.FParser {
      @Override
      public float parseFloat(String var1) throws NumberFormatException {
         return Float.parseFloat(var1);
      }
   }
}
