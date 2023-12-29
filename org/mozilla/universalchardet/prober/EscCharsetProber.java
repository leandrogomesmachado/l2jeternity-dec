package org.mozilla.universalchardet.prober;

import org.mozilla.universalchardet.prober.statemachine.CodingStateMachine;
import org.mozilla.universalchardet.prober.statemachine.HZSMModel;
import org.mozilla.universalchardet.prober.statemachine.ISO2022CNSMModel;
import org.mozilla.universalchardet.prober.statemachine.ISO2022JPSMModel;
import org.mozilla.universalchardet.prober.statemachine.ISO2022KRSMModel;

public class EscCharsetProber extends CharsetProber {
   private CodingStateMachine[] codingSM = new CodingStateMachine[4];
   private int activeSM;
   private CharsetProber.ProbingState state;
   private String detectedCharset;
   private static final HZSMModel hzsModel = new HZSMModel();
   private static final ISO2022CNSMModel iso2022cnModel = new ISO2022CNSMModel();
   private static final ISO2022JPSMModel iso2022jpModel = new ISO2022JPSMModel();
   private static final ISO2022KRSMModel iso2022krModel = new ISO2022KRSMModel();

   public EscCharsetProber() {
      this.codingSM[0] = new CodingStateMachine(hzsModel);
      this.codingSM[1] = new CodingStateMachine(iso2022cnModel);
      this.codingSM[2] = new CodingStateMachine(iso2022jpModel);
      this.codingSM[3] = new CodingStateMachine(iso2022krModel);
      this.reset();
   }

   @Override
   public String getCharSetName() {
      return this.detectedCharset;
   }

   @Override
   public float getConfidence() {
      return 0.99F;
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      int var5 = var2 + var3;

      for(int var6 = var2; var6 < var5 && this.state == CharsetProber.ProbingState.DETECTING; ++var6) {
         for(int var7 = this.activeSM - 1; var7 >= 0; --var7) {
            int var4 = this.codingSM[var7].nextState(var1[var6]);
            if (var4 == 1) {
               --this.activeSM;
               if (this.activeSM <= 0) {
                  this.state = CharsetProber.ProbingState.NOT_ME;
                  return this.state;
               }

               if (var7 != this.activeSM) {
                  CodingStateMachine var8 = this.codingSM[this.activeSM];
                  this.codingSM[this.activeSM] = this.codingSM[var7];
                  this.codingSM[var7] = var8;
               }
            } else if (var4 == 2) {
               this.state = CharsetProber.ProbingState.FOUND_IT;
               this.detectedCharset = this.codingSM[var7].getCodingStateMachine();
               return this.state;
            }
         }
      }

      return this.state;
   }

   @Override
   public void reset() {
      this.state = CharsetProber.ProbingState.DETECTING;

      for(int var1 = 0; var1 < this.codingSM.length; ++var1) {
         this.codingSM[var1].reset();
      }

      this.activeSM = this.codingSM.length;
      this.detectedCharset = null;
   }

   @Override
   public void setOption() {
   }
}
