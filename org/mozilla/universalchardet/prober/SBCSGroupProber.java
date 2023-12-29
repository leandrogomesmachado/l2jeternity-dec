package org.mozilla.universalchardet.prober;

import java.nio.ByteBuffer;
import org.mozilla.universalchardet.prober.sequence.HebrewModel;
import org.mozilla.universalchardet.prober.sequence.Ibm855Model;
import org.mozilla.universalchardet.prober.sequence.Ibm866Model;
import org.mozilla.universalchardet.prober.sequence.Koi8rModel;
import org.mozilla.universalchardet.prober.sequence.Latin5BulgarianModel;
import org.mozilla.universalchardet.prober.sequence.Latin5Model;
import org.mozilla.universalchardet.prober.sequence.Latin7Model;
import org.mozilla.universalchardet.prober.sequence.MacCyrillicModel;
import org.mozilla.universalchardet.prober.sequence.SequenceModel;
import org.mozilla.universalchardet.prober.sequence.Win1251BulgarianModel;
import org.mozilla.universalchardet.prober.sequence.Win1251Model;
import org.mozilla.universalchardet.prober.sequence.Win1253Model;

public class SBCSGroupProber extends CharsetProber {
   private CharsetProber.ProbingState state;
   private CharsetProber[] probers = new CharsetProber[13];
   private boolean[] isActive = new boolean[13];
   private int bestGuess;
   private int activeNum;
   private static final SequenceModel win1251Model = new Win1251Model();
   private static final SequenceModel koi8rModel = new Koi8rModel();
   private static final SequenceModel latin5Model = new Latin5Model();
   private static final SequenceModel macCyrillicModel = new MacCyrillicModel();
   private static final SequenceModel ibm866Model = new Ibm866Model();
   private static final SequenceModel ibm855Model = new Ibm855Model();
   private static final SequenceModel latin7Model = new Latin7Model();
   private static final SequenceModel win1253Model = new Win1253Model();
   private static final SequenceModel latin5BulgarianModel = new Latin5BulgarianModel();
   private static final SequenceModel win1251BulgarianModel = new Win1251BulgarianModel();
   private static final SequenceModel hebrewModel = new HebrewModel();

   public SBCSGroupProber() {
      this.probers[0] = new SingleByteCharsetProber(win1251Model);
      this.probers[1] = new SingleByteCharsetProber(koi8rModel);
      this.probers[2] = new SingleByteCharsetProber(latin5Model);
      this.probers[3] = new SingleByteCharsetProber(macCyrillicModel);
      this.probers[4] = new SingleByteCharsetProber(ibm866Model);
      this.probers[5] = new SingleByteCharsetProber(ibm855Model);
      this.probers[6] = new SingleByteCharsetProber(latin7Model);
      this.probers[7] = new SingleByteCharsetProber(win1253Model);
      this.probers[8] = new SingleByteCharsetProber(latin5BulgarianModel);
      this.probers[9] = new SingleByteCharsetProber(win1251BulgarianModel);
      HebrewProber var1 = new HebrewProber();
      this.probers[10] = var1;
      this.probers[11] = new SingleByteCharsetProber(hebrewModel, false, var1);
      this.probers[12] = new SingleByteCharsetProber(hebrewModel, true, var1);
      var1.setModalProbers(this.probers[11], this.probers[12]);
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
      ByteBuffer var5 = this.filterWithoutEnglishLetters(var1, var2, var3);
      if (var5.position() != 0) {
         for(int var6 = 0; var6 < this.probers.length; ++var6) {
            if (this.isActive[var6]) {
               CharsetProber.ProbingState var4 = this.probers[var6].handleData(var5.array(), 0, var5.position());
               if (var4 == CharsetProber.ProbingState.FOUND_IT) {
                  this.bestGuess = var6;
                  this.state = CharsetProber.ProbingState.FOUND_IT;
                  break;
               }

               if (var4 == CharsetProber.ProbingState.NOT_ME) {
                  this.isActive[var6] = false;
                  --this.activeNum;
                  if (this.activeNum <= 0) {
                     this.state = CharsetProber.ProbingState.NOT_ME;
                     break;
                  }
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
