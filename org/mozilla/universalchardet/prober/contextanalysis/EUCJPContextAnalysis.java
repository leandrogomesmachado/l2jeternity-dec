package org.mozilla.universalchardet.prober.contextanalysis;

public class EUCJPContextAnalysis extends JapaneseContextAnalysis {
   public static final int HIRAGANA_HIGHBYTE = 164;
   public static final int HIRAGANA_LOWBYTE_BEGIN = 161;
   public static final int HIRAGANA_LOWBYTE_END = 243;
   public static final int SINGLE_SHIFT_2 = 142;
   public static final int SINGLE_SHIFT_3 = 143;
   public static final int FIRSTPLANE_HIGHBYTE_BEGIN = 161;
   public static final int FIRSTPLANE_HIGHBYTE_END = 254;

   @Override
   protected void getOrder(JapaneseContextAnalysis.Order var1, byte[] var2, int var3) {
      var1.order = -1;
      var1.charLength = 1;
      int var4 = var2[var3] & 255;
      if (var4 != 142 && (var4 < 161 || var4 > 254)) {
         if (var4 == 143) {
            var1.charLength = 3;
         }
      } else {
         var1.charLength = 2;
      }

      if (var4 == 164) {
         int var5 = var2[var3 + 1] & 255;
         if (var5 >= 161 && var5 <= 243) {
            var1.order = var5 - 161;
         }
      }
   }

   @Override
   protected int getOrder(byte[] var1, int var2) {
      int var3 = var1[var2] & 255;
      if (var3 == 164) {
         int var4 = var1[var2 + 1] & 255;
         if (var4 >= 161 && var4 <= 243) {
            return var4 - 161;
         }
      }

      return -1;
   }
}
