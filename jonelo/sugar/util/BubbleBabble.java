package jonelo.sugar.util;

public class BubbleBabble {
   public static String encode(byte[] var0) {
      char[] var1 = new char[]{'a', 'e', 'i', 'o', 'u', 'y'};
      char[] var2 = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'z', 'x'};
      int var3 = 1;
      int var4 = var0.length / 2 + 1;
      StringBuffer var5 = new StringBuffer(var4 * 6);
      var5.append('x');

      for(int var6 = 0; var6 < var4; ++var6) {
         if (var6 + 1 >= var4 && var0.length % 2 == 0) {
            int var12 = var3 % 6;
            byte var13 = 16;
            int var14 = var3 / 6;
            var5.append(var1[var12]);
            var5.append(var2[var13]);
            var5.append(var1[var14]);
         } else {
            int var7 = (((var0[2 * var6] & 255) >>> 6 & 3) + var3) % 6;
            int var8 = (var0[2 * var6] & 255) >>> 2 & 15;
            int var9 = ((var0[2 * var6] & 255 & 3) + var3 / 6) % 6;
            var5.append(var1[var7]);
            var5.append(var2[var8]);
            var5.append(var1[var9]);
            if (var6 + 1 < var4) {
               int var10 = (var0[2 * var6 + 1] & 255) >>> 4 & 15;
               int var11 = var0[2 * var6 + 1] & 255 & 15;
               var5.append(var2[var10]);
               var5.append('-');
               var5.append(var2[var11]);
               var3 = (var3 * 5 + (var0[2 * var6] & 255) * 7 + (var0[2 * var6 + 1] & 255)) % 36;
            }
         }
      }

      var5.append('x');
      return var5.toString();
   }
}
