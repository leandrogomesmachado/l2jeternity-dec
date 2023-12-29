package org.mozilla.universalchardet.prober;

public class MBCSGroupProber extends CharsetProber {
   private CharsetProber.ProbingState state;
   private CharsetProber[] probers = new CharsetProber[7];
   private boolean[] isActive = new boolean[7];
   private int bestGuess;
   private int activeNum;

   public MBCSGroupProber() {
      this.probers[0] = new UTF8Prober();
      this.probers[1] = new SJISProber();
      this.probers[2] = new EUCJPProber();
      this.probers[3] = new GB18030Prober();
      this.probers[4] = new EUCKRProber();
      this.probers[5] = new Big5Prober();
      this.probers[6] = new EUCTWProber();
      this.reset();
   }

   @Override
   public String getCharSetName() {
      if (this.bestGuess == -1) {
         this.getConfidence();
         if (this.bestGuess == -1) {
            this.bestGuess = 0;
         }
      }

      return this.probers[this.bestGuess].getCharSetName();
   }

   @Override
   public float getConfidence() {
      float var1 = 0.0F;
      if (this.state == CharsetProber.ProbingState.FOUND_IT) {
         return 0.99F;
      } else if (this.state == CharsetProber.ProbingState.NOT_ME) {
         return 0.01F;
      } else {
         for(int var3 = 0; var3 < this.probers.length; ++var3) {
            if (this.isActive[var3]) {
               float var2 = this.probers[var3].getConfidence();
               if (var1 < var2) {
                  var1 = var2;
                  this.bestGuess = var3;
               }
            }
         }

         return var1;
      }
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      boolean var5 = true;
      byte[] var6 = new byte[var3];
      int var7 = 0;
      int var8 = var2 + var3;

      for(int var9 = var2; var9 < var8; ++var9) {
         if ((var1[var9] & 128) != 0) {
            var6[var7++] = var1[var9];
            var5 = true;
         } else if (var5) {
            var6[var7++] = var1[var9];
            var5 = false;
         }
      }

      for(int var10 = 0; var10 < this.probers.length; ++var10) {
         if (this.isActive[var10]) {
            CharsetProber.ProbingState var4 = this.probers[var10].handleData(var6, 0, var7);
            if (var4 == CharsetProber.ProbingState.FOUND_IT) {
               this.bestGuess = var10;
               this.state = CharsetProber.ProbingState.FOUND_IT;
               break;
            }

            if (var4 == CharsetProber.ProbingState.NOT_ME) {
               this.isActive[var10] = false;
               --this.activeNum;
               if (this.activeNum <= 0) {
                  this.state = CharsetProber.ProbingState.NOT_ME;
                  break;
               }
            }
         }
      }

      return this.state;
   }

   @Override
   public void reset() {
      this.activeNum = 0;

      for(int var1 = 0; var1 < this.probers.length; ++var1) {
         this.probers[var1].reset();
         this.isActive[var1] = true;
         ++this.activeNum;
      }

      this.bestGuess = -1;
      this.state = CharsetProber.ProbingState.DETECTING;
   }

   @Override
   public void setOption() {
   }
}
