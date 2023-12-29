package org.mozilla.universalchardet.prober.distributionanalysis;

public class EUCJPDistributionAnalysis extends JISDistributionAnalysis {
   public static final int HIGHBYTE_BEGIN = 161;
   public static final int HIGHBYTE_END = 254;
   public static final int LOWBYTE_BEGIN = 161;
   public static final int LOWBYTE_END = 254;

   @Override
   protected int getOrder(byte[] var1, int var2) {
      int var3 = var1[var2] & 255;
      if (var3 >= 161) {
         int var4 = var1[var2 + 1] & 255;
         return 94 * (var3 - 161) + var4 - 161;
      } else {
         return -1;
      }
   }
}
