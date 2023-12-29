package org.mozilla.universalchardet.prober.distributionanalysis;

public class SJISDistributionAnalysis extends JISDistributionAnalysis {
   public static final int HIGHBYTE_BEGIN_1 = 129;
   public static final int HIGHBYTE_END_1 = 159;
   public static final int HIGHBYTE_BEGIN_2 = 224;
   public static final int HIGHBYTE_END_2 = 239;
   public static final int LOWBYTE_BEGIN_1 = 64;
   public static final int LOWBYTE_BEGIN_2 = 128;

   @Override
   protected int getOrder(byte[] var1, int var2) {
      int var3 = -1;
      int var4 = var1[var2] & 255;
      if (var4 >= 129 && var4 <= 159) {
         var3 = 188 * (var4 - 129);
      } else {
         if (var4 < 224 || var4 > 239) {
            return -1;
         }

         var3 = 188 * (var4 - 224 + 31);
      }

      int var5 = var1[var2 + 1] & 255;
      var3 += var5 - 64;
      if (var5 >= 128) {
         --var3;
      }

      return var3;
   }
}
