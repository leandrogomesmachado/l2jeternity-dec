package org.mozilla.universalchardet.prober;

import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.prober.statemachine.CodingStateMachine;
import org.mozilla.universalchardet.prober.statemachine.SMModel;
import org.mozilla.universalchardet.prober.statemachine.UTF8SMModel;

public class UTF8Prober extends CharsetProber {
   public static final float ONE_CHAR_PROB = 0.5F;
   private CodingStateMachine codingSM;
   private CharsetProber.ProbingState state;
   private int numOfMBChar = 0;
   private static final SMModel smModel = new UTF8SMModel();

   public UTF8Prober() {
      this.codingSM = new CodingStateMachine(smModel);
      this.reset();
   }

   @Override
   public String getCharSetName() {
      return Constants.CHARSET_UTF_8;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      int var5 = var2 + var3;

      for(int var6 = var2; var6 < var5; ++var6) {
         int var4 = this.codingSM.nextState(var1[var6]);
         if (var4 == 1) {
            this.state = CharsetProber.ProbingState.NOT_ME;
            break;
         }

         if (var4 == 2) {
            this.state = CharsetProber.ProbingState.FOUND_IT;
            break;
         }

         if (var4 == 0 && this.codingSM.getCurrentCharLen() >= 2) {
            ++this.numOfMBChar;
         }
      }

      if (this.state == CharsetProber.ProbingState.DETECTING && this.getConfidence() > 0.95F) {
         this.state = CharsetProber.ProbingState.FOUND_IT;
      }

      return this.state;
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
   }

   @Override
   public void reset() {
      this.codingSM.reset();
      this.numOfMBChar = 0;
      this.state = CharsetProber.ProbingState.DETECTING;
   }

   @Override
   public float getConfidence() {
      float var1 = 0.99F;
      if (this.numOfMBChar >= 6) {
         return 0.99F;
      } else {
         for(int var2 = 0; var2 < this.numOfMBChar; ++var2) {
            var1 *= 0.5F;
         }

         return 1.0F - var1;
      }
   }

   @Override
   public void setOption() {
   }
}
