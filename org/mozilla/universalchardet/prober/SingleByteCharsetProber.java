package org.mozilla.universalchardet.prober;

import org.mozilla.universalchardet.prober.sequence.SequenceModel;

public class SingleByteCharsetProber extends CharsetProber {
   public static final int SAMPLE_SIZE = 64;
   public static final int SB_ENOUGH_REL_THRESHOLD = 1024;
   public static final float POSITIVE_SHORTCUT_THRESHOLD = 0.95F;
   public static final float NEGATIVE_SHORTCUT_THRESHOLD = 0.05F;
   public static final int SYMBOL_CAT_ORDER = 250;
   public static final int NUMBER_OF_SEQ_CAT = 4;
   public static final int POSITIVE_CAT = 3;
   public static final int NEGATIVE_CAT = 0;
   private CharsetProber.ProbingState state;
   private SequenceModel model;
   private boolean reversed;
   private short lastOrder;
   private int totalSeqs;
   private int[] seqCounters;
   private int totalChar;
   private int freqChar;
   private CharsetProber nameProber;

   public SingleByteCharsetProber(SequenceModel var1) {
      this.model = var1;
      this.reversed = false;
      this.nameProber = null;
      this.seqCounters = new int[4];
      this.reset();
   }

   public SingleByteCharsetProber(SequenceModel var1, boolean var2, CharsetProber var3) {
      this.model = var1;
      this.reversed = var2;
      this.nameProber = var3;
      this.seqCounters = new int[4];
      this.reset();
   }

   boolean keepEnglishLetters() {
      return this.model.getKeepEnglishLetter();
   }

   @Override
   public String getCharSetName() {
      return this.nameProber == null ? this.model.getCharsetName() : this.nameProber.getCharSetName();
   }

   @Override
   public float getConfidence() {
      if (this.totalSeqs > 0) {
         float var1 = 1.0F * (float)this.seqCounters[3] / (float)this.totalSeqs / this.model.getTypicalPositiveRatio();
         var1 = var1 * (float)this.freqChar / (float)this.totalChar;
         if (var1 >= 1.0F) {
            var1 = 0.99F;
         }

         return var1;
      } else {
         return 0.01F;
      }
   }

   @Override
   public CharsetProber.ProbingState getState() {
      return this.state;
   }

   @Override
   public CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3) {
      int var5 = var2 + var3;

      for(int var6 = var2; var6 < var5; ++var6) {
         short var4 = this.model.getOrder(var1[var6]);
         if (var4 < 250) {
            ++this.totalChar;
         }

         if (var4 < 64) {
            ++this.freqChar;
            if (this.lastOrder < 64) {
               ++this.totalSeqs;
               if (!this.reversed) {
                  this.seqCounters[this.model.getPrecedence(this.lastOrder * 64 + var4)]++;
               } else {
                  this.seqCounters[this.model.getPrecedence(var4 * 64 + this.lastOrder)]++;
               }
            }
         }

         this.lastOrder = var4;
      }

      if (this.state == CharsetProber.ProbingState.DETECTING && this.totalSeqs > 1024) {
         float var7 = this.getConfidence();
         if (var7 > 0.95F) {
            this.state = CharsetProber.ProbingState.FOUND_IT;
         } else if (var7 < 0.05F) {
            this.state = CharsetProber.ProbingState.NOT_ME;
         }
      }

      return this.state;
   }

   @Override
   public void reset() {
      this.state = CharsetProber.ProbingState.DETECTING;
      this.lastOrder = 255;

      for(int var1 = 0; var1 < 4; ++var1) {
         this.seqCounters[var1] = 0;
      }

      this.totalSeqs = 0;
      this.totalChar = 0;
      this.freqChar = 0;
   }

   @Override
   public void setOption() {
   }
}
