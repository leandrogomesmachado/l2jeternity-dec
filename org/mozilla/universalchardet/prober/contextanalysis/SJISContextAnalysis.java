package org.mozilla.universalchardet.prober.contextanalysis;

public class SJISContextAnalysis extends JapaneseContextAnalysis {
   public static final int HIRAGANA_HIGHBYTE = 130;
   public static final int HIRAGANA_LOWBYTE_BEGIN = 159;
   public static final int HIRAGANA_LOWBYTE_END = 241;
   public static final int HIGHBYTE_BEGIN_1 = 129;
   public static final int HIGHBYTE_END_1 = 159;
   public static final int HIGHBYTE_BEGIN_2 = 224;
   public static final int HIGHBYTE_END_2 = 239;

   @Override
   protected void getOrder(JapaneseContextAnalysis.Order var1, byte[] var2, int var3) {
      var1.order = -1;
      var1.charLength = 1;
      int var4 = var2[var3] & 255;
      if (var4 >= 129 && var4 <= 159 || var4 >= 224 && var4 <= 239) {
         var1.charLength = 2;
      }

      if (var4 == 130) {
         int var5 = var2[var3 + 1] & 255;
         if (var5 >= 159 && var5 <= 241) {
            var1.order = var5 - 159;
         }
      }
   }

   @Override
   protected int getOrder(byte[] var1, int var2) {
      int var3 = var1[var2] & 255;
      if (var3 == 130) {
         int var4 = var1[var2 + 1] & 255;
         if (var4 >= 159 && var4 <= 241) {
            return var4 - 159;
         }
      }

      return -1;
   }
}
