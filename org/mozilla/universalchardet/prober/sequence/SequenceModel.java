package org.mozilla.universalchardet.prober.sequence;

public abstract class SequenceModel {
   protected short[] charToOrderMap;
   protected byte[] precedenceMatrix;
   protected float typicalPositiveRatio;
   protected boolean keepEnglishLetter;
   protected String charsetName;

   public SequenceModel(short[] var1, byte[] var2, float var3, boolean var4, String var5) {
      this.charToOrderMap = var1;
      this.precedenceMatrix = var2;
      this.typicalPositiveRatio = var3;
      this.keepEnglishLetter = var4;
      this.charsetName = var5;
   }

   public short getOrder(byte var1) {
      int var2 = var1 & 255;
      return this.charToOrderMap[var2];
   }

   public byte getPrecedence(int var1) {
      return this.precedenceMatrix[var1];
   }

   public float getTypicalPositiveRatio() {
      return this.typicalPositiveRatio;
   }

   public boolean getKeepEnglishLetter() {
      return this.keepEnglishLetter;
   }

   public String getCharsetName() {
      return this.charsetName;
   }
}
