package org.mozilla.universalchardet.prober;

import java.util.Arrays;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.prober.contextanalysis.SJISContextAnalysis;
import org.mozilla.universalchardet.prober.distributionanalysis.SJISDistributionAnalysis;
import org.mozilla.universalchardet.prober.statemachine.CodingStateMachine;
import org.mozilla.universalchardet.prober.statemachine.SJISSMModel;
import org.mozilla.universalchardet.prober.statemachine.SMModel;

public class SJISProber extends CharsetProber {
   private CodingStateMachine codingSM;
   private CharsetProber.ProbingState state;
   private SJISContextAnalysis contextAnalyzer;
   private SJISDistributionAnalysis distributionAnalyzer;
   private byte[] lastChar;
   private static final SMModel smModel = new SJISSMModel();

   public SJISProber() {
      this.codingSM = new CodingStateMachine(smModel);
      this.contextAnalyzer = new SJISContextAnalysis();
      this.distributionAnalyzer = new SJISDistributionAnalysis();
      this.lastChar = new byte[2];
      this.reset();
   }

   @Override
   public String getCharSetName() {
      return Constants.CHARSET_SHIFT_JIS;
   }

   @Override
   public float getConfidence() {
      float var1 = this.contextAnalyzer.getConfidence();
      float var2 = this.distributionAnalyzer.getConfidence();
      return Math.max(var1, var2);
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
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

         if (var4 == 0) {
            int var7 = this.codingSM.getCurrentCharLen();
            if (var6 == var2) {
               this.lastChar[1] = var1[var2];
               this.contextAnalyzer.handleOneChar(this.lastChar, 2 - var7, var7);
               this.distributionAnalyzer.handleOneChar(this.lastChar, 0, var7);
            } else {
               this.contextAnalyzer.handleOneChar(var1, var6 + 1 - var7, var7);
               this.distributionAnalyzer.handleOneChar(var1, var6 - 1, var7);
            }
         }
      }

      this.lastChar[0] = var1[var5 - 1];
      if (this.state == CharsetProber.ProbingState.DETECTING && this.contextAnalyzer.gotEnoughData() && this.getConfidence() > 0.95F) {
         this.state = CharsetProber.ProbingState.FOUND_IT;
      }

      return this.state;
   }

   @Override
   public void reset() {
      this.codingSM.reset();
      this.state = CharsetProber.ProbingState.DETECTING;
      this.contextAnalyzer.reset();
      this.distributionAnalyzer.reset();
      Arrays.fill(this.lastChar, (byte)0);
   }

   @Override
   public void setOption() {
   }
}
