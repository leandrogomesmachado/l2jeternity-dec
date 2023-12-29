package org.mozilla.universalchardet.prober;

import org.mozilla.universalchardet.Constants;

public class HebrewProber extends CharsetProber {
   public static final int FINAL_KAF = 234;
   public static final int NORMAL_KAF = 235;
   public static final int FINAL_MEM = 237;
   public static final int NORMAL_MEM = 238;
   public static final int FINAL_NUN = 239;
   public static final int NORMAL_NUN = 240;
   public static final int FINAL_PE = 243;
   public static final int NORMAL_PE = 244;
   public static final int FINAL_TSADI = 245;
   public static final int NORMAL_TSADI = 246;
   public static final byte SPACE = 32;
   public static final int MIN_FINAL_CHAR_DISTANCE = 5;
   public static final float MIN_MODEL_DISTANCE = 0.01F;
   private int finalCharLogicalScore;
   private int finalCharVisualScore;
   private byte prev;
   private byte beforePrev;
   private CharsetProber logicalProber = null;
   private CharsetProber visualProber = null;

   public HebrewProber() {
      this.reset();
   }

   public void setModalProbers(CharsetProber var1, CharsetProber var2) {
      this.logicalProber = var1;
      this.visualProber = var2;
   }

   @Override
   public String getCharSetName() {
      int var1 = this.finalCharLogicalScore - this.finalCharVisualScore;
      if (var1 >= 5) {
         return Constants.CHARSET_WINDOWS_1255;
      } else if (var1 <= -5) {
         return Constants.CHARSET_ISO_8859_8;
      } else {
         float var2 = this.logicalProber.getConfidence() - this.visualProber.getConfidence();
         if (var2 > 0.01F) {
            return Constants.CHARSET_WINDOWS_1255;
         } else if (var2 < -0.01F) {
            return Constants.CHARSET_ISO_8859_8;
         } else {
            return var1 < 0 ? Constants.CHARSET_ISO_8859_8 : Constants.CHARSET_WINDOWS_1255;
         }
      }
   }

   @Override
   public float getConfidence() {
      return 0.0F;
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.logicalProber.getState() == CharsetProber.ProbingState.NOT_ME && this.visualProber.getState() == CharsetProber.ProbingState.NOT_ME
         ? CharsetProber.ProbingState.NOT_ME
         : CharsetProber.ProbingState.DETECTING;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      if (this.getState() == CharsetProber.ProbingState.NOT_ME) {
         return CharsetProber.ProbingState.NOT_ME;
      } else {
         int var5 = var2 + var3;

         for(int var6 = var2; var6 < var5; ++var6) {
            byte var4 = var1[var6];
            if (var4 == 32) {
               if (this.beforePrev != 32) {
                  if (isFinal(this.prev)) {
                     ++this.finalCharLogicalScore;
                  } else if (isNonFinal(this.prev)) {
                     ++this.finalCharVisualScore;
                  }
               }
            } else if (this.beforePrev == 32 && isFinal(this.prev) && var4 != 32) {
               ++this.finalCharVisualScore;
            }

            this.beforePrev = this.prev;
            this.prev = var4;
         }

         return CharsetProber.ProbingState.DETECTING;
      }
   }

   @Override
   public void reset() {
      this.finalCharLogicalScore = 0;
      this.finalCharVisualScore = 0;
      this.prev = 32;
      this.beforePrev = 32;
   }

   @Override
   public void setOption() {
   }

   protected static boolean isFinal(byte var0) {
      int var1 = var0 & 255;
      return var1 == 234 || var1 == 237 || var1 == 239 || var1 == 243 || var1 == 245;
   }

   protected static boolean isNonFinal(byte var0) {
      int var1 = var0 & 255;
      return var1 == 235 || var1 == 238 || var1 == 240 || var1 == 244;
   }
}
