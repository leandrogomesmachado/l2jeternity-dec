package org.mozilla.universalchardet.prober;

import java.nio.ByteBuffer;
import org.mozilla.universalchardet.Constants;

public class Latin1Prober extends CharsetProber {
   public static final byte UDF = 0;
   public static final byte OTH = 1;
   public static final byte ASC = 2;
   public static final byte ASS = 3;
   public static final byte ACV = 4;
   public static final byte ACO = 5;
   public static final byte ASV = 6;
   public static final byte ASO = 7;
   public static final int CLASS_NUM = 8;
   public static final int FREQ_CAT_NUM = 4;
   private CharsetProber.ProbingState state;
   private byte lastCharClass;
   private int[] freqCounter = new int[4];
   private static final byte[] latin1CharToClass = new byte[]{
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      7,
      1,
      1,
      1,
      1,
      1,
      1,
      5,
      1,
      5,
      0,
      5,
      0,
      0,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      7,
      1,
      7,
      0,
      7,
      5,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      4,
      4,
      4,
      4,
      4,
      4,
      5,
      5,
      4,
      4,
      4,
      4,
      4,
      4,
      4,
      4,
      5,
      5,
      4,
      4,
      4,
      4,
      4,
      1,
      4,
      4,
      4,
      4,
      4,
      5,
      5,
      5,
      6,
      6,
      6,
      6,
      6,
      6,
      7,
      7,
      6,
      6,
      6,
      6,
      6,
      6,
      6,
      6,
      7,
      7,
      6,
      6,
      6,
      6,
      6,
      1,
      6,
      6,
      6,
      6,
      6,
      7,
      7,
      7
   };
   private static final byte[] latin1ClassModel = new byte[]{
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      0,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      0,
      3,
      3,
      3,
      1,
      1,
      3,
      3,
      0,
      3,
      3,
      3,
      1,
      2,
      1,
      2,
      0,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      0,
      3,
      1,
      3,
      1,
      1,
      1,
      3,
      0,
      3,
      1,
      3,
      1,
      1,
      3,
      3
   };

   public Latin1Prober() {
      this.reset();
   }

   @Override
   public String getCharSetName() {
      return Constants.CHARSET_WINDOWS_1252;
   }

   @Override
   public float getConfidence() {
      if (this.state == CharsetProber.ProbingState.NOT_ME) {
         return 0.01F;
      } else {
         int var2 = 0;

         for(int var3 = 0; var3 < this.freqCounter.length; ++var3) {
            var2 += this.freqCounter[var3];
         }

         float var1;
         if (var2 <= 0) {
            var1 = 0.0F;
         } else {
            var1 = (float)this.freqCounter[3] * 1.0F / (float)var2;
            var1 -= (float)this.freqCounter[1] * 20.0F / (float)var2;
         }

         if (var1 < 0.0F) {
            var1 = 0.0F;
         }

         return var1 * 0.5F;
      }
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      ByteBuffer var4 = this.filterWithEnglishLetters(var1, var2, var3);
      byte[] var7 = var4.array();
      int var8 = var4.position();

      for(int var9 = 0; var9 < var8; ++var9) {
         int var10 = var7[var9] & 255;
         byte var5 = latin1CharToClass[var10];
         byte var6 = latin1ClassModel[this.lastCharClass * 8 + var5];
         if (var6 == 0) {
            this.state = CharsetProber.ProbingState.NOT_ME;
            break;
         }

         this.freqCounter[var6]++;
         this.lastCharClass = var5;
      }

      return this.state;
   }

   @Override
   public void reset() {
      this.state = CharsetProber.ProbingState.DETECTING;
      this.lastCharClass = 1;

      for(int var1 = 0; var1 < this.freqCounter.length; ++var1) {
         this.freqCounter[var1] = 0;
      }
   }

   @Override
   public void setOption() {
   }
}
