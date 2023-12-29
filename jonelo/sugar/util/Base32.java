package jonelo.sugar.util;

public class Base32 {
   private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
   private static final int[] base32Lookup = new int[]{
      255,
      255,
      26,
      27,
      28,
      29,
      30,
      31,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      255,
      255,
      255,
      255,
      255,
      255,
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      255,
      255,
      255,
      255,
      255
   };

   public static String encode(byte[] var0) {
      int var1 = 0;
      int var2 = 0;
      int var3 = 0;
      byte var6 = 0;
      switch(var0.length) {
         case 1:
            var6 = 6;
            break;
         case 2:
            var6 = 4;
            break;
         case 3:
            var6 = 3;
            break;
         case 4:
            var6 = 1;
      }

      StringBuffer var7;
      for(var7 = new StringBuffer((var0.length + 7) * 8 / 5 + var6); var1 < var0.length; var7.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(var3))) {
         int var4 = var0[var1] >= 0 ? var0[var1] : var0[var1] + 256;
         if (var2 > 3) {
            int var5;
            if (var1 + 1 < var0.length) {
               var5 = var0[var1 + 1] >= 0 ? var0[var1 + 1] : var0[var1 + 1] + 256;
            } else {
               var5 = 0;
            }

            var3 = var4 & 255 >> var2;
            var2 = (var2 + 5) % 8;
            var3 <<= var2;
            var3 |= var5 >> 8 - var2;
            ++var1;
         } else {
            var3 = var4 >> 8 - (var2 + 5) & 31;
            var2 = (var2 + 5) % 8;
            if (var2 == 0) {
               ++var1;
            }
         }
      }

      switch(var0.length) {
         case 1:
            var7.append("======");
            break;
         case 2:
            var7.append("====");
            break;
         case 3:
            var7.append("===");
            break;
         case 4:
            var7.append("=");
      }

      return var7.toString();
   }

   public static byte[] decode(String var0) {
      byte[] var6 = new byte[var0.length() * 5 / 8];
      int var1 = 0;
      int var2 = 0;

      for(int var4 = 0; var1 < var0.length(); ++var1) {
         int var3 = var0.charAt(var1) - '0';
         if (var3 >= 0 && var3 < base32Lookup.length) {
            int var5 = base32Lookup[var3];
            if (var5 != 255) {
               if (var2 <= 3) {
                  var2 = (var2 + 5) % 8;
                  if (var2 == 0) {
                     var6[var4] = (byte)(var6[var4] | var5);
                     if (++var4 >= var6.length) {
                        break;
                     }
                  } else {
                     var6[var4] = (byte)(var6[var4] | var5 << 8 - var2);
                  }
               } else {
                  var2 = (var2 + 5) % 8;
                  var6[var4] = (byte)(var6[var4] | var5 >>> var2);
                  if (++var4 >= var6.length) {
                     break;
                  }

                  var6[var4] = (byte)(var6[var4] | var5 << 8 - var2);
               }
            }
         }
      }

      return var6;
   }

   public static void main(String[] var0) {
      if (var0.length == 0) {
         System.out.println("Supply a Base32-encoded argument.");
      } else {
         System.out.println(" Original: " + var0[0]);
         byte[] var1 = decode(var0[0]);
         System.out.print("      Hex: ");

         for(int var2 = 0; var2 < var1.length; ++var2) {
            int var3 = var1[var2];
            if (var3 < 0) {
               var3 += 256;
            }

            System.out.print(Integer.toHexString(var3 + 256).substring(1));
         }

         System.out.println();
         System.out.println("Reencoded: " + encode(var1));
      }
   }
}
