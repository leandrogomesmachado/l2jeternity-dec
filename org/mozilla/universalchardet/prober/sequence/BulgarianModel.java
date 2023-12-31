package org.mozilla.universalchardet.prober.sequence;

public class BulgarianModel extends SequenceModel {
   public static final float TYPICAL_POSITIVE_RATIO = 0.969392F;
   private static final byte[] bulgarianLangModel = new byte[]{
      0,
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
      2,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      2,
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
      0,
      3,
      3,
      3,
      2,
      2,
      3,
      2,
      2,
      1,
      2,
      2,
      3,
      1,
      3,
      3,
      2,
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
      0,
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
      0,
      3,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      2,
      3,
      2,
      3,
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
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      2,
      2,
      2,
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
      3,
      2,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      2,
      3,
      3,
      2,
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
      3,
      2,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      3,
      3,
      3,
      3,
      2,
      3,
      2,
      2,
      1,
      3,
      3,
      3,
      3,
      2,
      2,
      2,
      1,
      1,
      2,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      2,
      2,
      3,
      3,
      1,
      1,
      2,
      3,
      3,
      2,
      3,
      3,
      3,
      3,
      2,
      1,
      2,
      0,
      2,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      1,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      2,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      3,
      1,
      3,
      0,
      3,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      1,
      3,
      3,
      2,
      3,
      3,
      3,
      1,
      3,
      3,
      2,
      3,
      2,
      2,
      2,
      0,
      0,
      2,
      0,
      2,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      0,
      3,
      3,
      3,
      2,
      2,
      3,
      3,
      3,
      1,
      2,
      2,
      3,
      2,
      1,
      1,
      2,
      0,
      2,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      3,
      1,
      2,
      3,
      2,
      2,
      2,
      3,
      3,
      3,
      3,
      3,
      2,
      2,
      3,
      1,
      2,
      0,
      2,
      1,
      2,
      0,
      0,
      0,
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
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      1,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      3,
      3,
      2,
      3,
      3,
      2,
      3,
      2,
      2,
      2,
      3,
      1,
      2,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      1,
      1,
      1,
      2,
      2,
      1,
      3,
      1,
      3,
      2,
      2,
      3,
      0,
      0,
      1,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      2,
      2,
      3,
      2,
      2,
      3,
      1,
      2,
      1,
      1,
      1,
      2,
      3,
      1,
      3,
      1,
      2,
      2,
      0,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      1,
      3,
      2,
      2,
      3,
      3,
      1,
      2,
      3,
      1,
      1,
      3,
      3,
      3,
      3,
      1,
      2,
      2,
      1,
      1,
      1,
      0,
      2,
      0,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      1,
      2,
      2,
      3,
      3,
      3,
      2,
      2,
      1,
      1,
      2,
      0,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      0,
      1,
      2,
      1,
      3,
      3,
      2,
      3,
      3,
      3,
      3,
      3,
      2,
      3,
      2,
      1,
      0,
      3,
      1,
      2,
      1,
      2,
      1,
      2,
      3,
      2,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      1,
      2,
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
      0,
      0,
      3,
      1,
      3,
      3,
      2,
      3,
      3,
      2,
      2,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
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
      2,
      1,
      1,
      2,
      1,
      3,
      3,
      0,
      3,
      1,
      1,
      1,
      1,
      3,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      2,
      2,
      2,
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
      3,
      1,
      3,
      3,
      2,
      3,
      2,
      2,
      2,
      3,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      2,
      3,
      3,
      2,
      2,
      3,
      2,
      1,
      1,
      1,
      1,
      1,
      3,
      1,
      3,
      1,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
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
      2,
      3,
      2,
      0,
      3,
      2,
      0,
      3,
      0,
      2,
      0,
      0,
      2,
      1,
      3,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      2,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      2,
      1,
      1,
      1,
      2,
      2,
      1,
      2,
      1,
      1,
      1,
      0,
      1,
      1,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      2,
      1,
      3,
      1,
      1,
      2,
      1,
      3,
      2,
      1,
      1,
      0,
      1,
      2,
      3,
      2,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      3,
      3,
      3,
      3,
      2,
      2,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      2,
      1,
      0,
      3,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      2,
      3,
      2,
      3,
      3,
      1,
      3,
      2,
      1,
      1,
      1,
      2,
      1,
      1,
      2,
      1,
      3,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      1,
      1,
      2,
      2,
      3,
      3,
      2,
      3,
      2,
      2,
      2,
      3,
      1,
      2,
      2,
      1,
      1,
      2,
      1,
      1,
      2,
      2,
      0,
      1,
      1,
      0,
      1,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      2,
      1,
      3,
      1,
      0,
      2,
      2,
      1,
      3,
      2,
      1,
      0,
      0,
      2,
      0,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      3,
      1,
      2,
      0,
      2,
      3,
      1,
      2,
      3,
      2,
      0,
      1,
      3,
      1,
      2,
      1,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      2,
      2,
      2,
      3,
      2,
      2,
      2,
      2,
      1,
      2,
      1,
      1,
      2,
      2,
      1,
      1,
      2,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      1,
      3,
      3,
      3,
      3,
      3,
      2,
      1,
      2,
      2,
      1,
      2,
      0,
      2,
      0,
      1,
      0,
      1,
      2,
      1,
      2,
      1,
      1,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      3,
      3,
      2,
      3,
      3,
      1,
      1,
      3,
      1,
      0,
      3,
      2,
      1,
      0,
      0,
      0,
      1,
      2,
      0,
      2,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      1,
      2,
      1,
      2,
      2,
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
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      2,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      3,
      1,
      0,
      1,
      0,
      2,
      3,
      2,
      2,
      2,
      3,
      2,
      2,
      2,
      2,
      2,
      1,
      0,
      2,
      1,
      2,
      1,
      1,
      1,
      0,
      1,
      2,
      1,
      2,
      2,
      2,
      1,
      1,
      1,
      2,
      2,
      2,
      2,
      1,
      2,
      1,
      1,
      0,
      1,
      2,
      1,
      2,
      2,
      2,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      2,
      3,
      2,
      3,
      3,
      0,
      0,
      2,
      1,
      0,
      2,
      1,
      0,
      0,
      0,
      0,
      2,
      3,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      0,
      1,
      2,
      2,
      1,
      2,
      1,
      2,
      2,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      0,
      1,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      2,
      0,
      0,
      3,
      3,
      2,
      2,
      3,
      0,
      2,
      3,
      1,
      1,
      2,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      0,
      2,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      1,
      2,
      0,
      2,
      2,
      1,
      1,
      1,
      1,
      2,
      1,
      0,
      1,
      2,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      2,
      3,
      2,
      3,
      3,
      0,
      0,
      3,
      0,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      2,
      2,
      1,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      1,
      2,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      2,
      2,
      2,
      1,
      0,
      2,
      0,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      3,
      3,
      3,
      3,
      2,
      2,
      2,
      2,
      2,
      0,
      2,
      1,
      1,
      1,
      1,
      2,
      1,
      2,
      1,
      1,
      0,
      2,
      0,
      1,
      0,
      1,
      0,
      0,
      2,
      0,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      2,
      2,
      1,
      1,
      0,
      2,
      0,
      1,
      0,
      2,
      0,
      0,
      1,
      1,
      1,
      0,
      0,
      2,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      2,
      3,
      3,
      3,
      3,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      0,
      1,
      2,
      2,
      2,
      2,
      1,
      1,
      2,
      1,
      1,
      2,
      2,
      2,
      1,
      2,
      0,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      0,
      2,
      3,
      3,
      3,
      3,
      0,
      2,
      2,
      0,
      2,
      1,
      0,
      0,
      0,
      1,
      1,
      1,
      2,
      0,
      2,
      0,
      0,
      0,
      3,
      0,
      0,
      0,
      0,
      2,
      0,
      2,
      2,
      1,
      1,
      1,
      2,
      1,
      2,
      1,
      1,
      2,
      2,
      2,
      1,
      2,
      0,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      0,
      2,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      2,
      3,
      3,
      3,
      3,
      0,
      2,
      1,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      1,
      2,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      2,
      2,
      2,
      0,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      3,
      3,
      2,
      2,
      3,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      2,
      2,
      1,
      2,
      2,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      3,
      1,
      0,
      1,
      0,
      2,
      2,
      2,
      2,
      3,
      2,
      1,
      1,
      1,
      2,
      3,
      0,
      0,
      1,
      0,
      2,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      2,
      2,
      1,
      2,
      1,
      2,
      2,
      1,
      1,
      0,
      1,
      2,
      1,
      2,
      2,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      2,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      2,
      1,
      0,
      1,
      0,
      3,
      1,
      2,
      2,
      2,
      2,
      1,
      2,
      2,
      1,
      1,
      1,
      0,
      2,
      1,
      2,
      2,
      1,
      1,
      2,
      1,
      1,
      0,
      2,
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
      1,
      2,
      0,
      1,
      1,
      0,
      2,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      2,
      1,
      1,
      1,
      1,
      2,
      2,
      2,
      2,
      1,
      2,
      2,
      2,
      1,
      2,
      2,
      1,
      1,
      2,
      1,
      2,
      3,
      2,
      2,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      2,
      2,
      3,
      2,
      0,
      1,
      2,
      0,
      1,
      2,
      1,
      1,
      0,
      1,
      0,
      1,
      2,
      1,
      2,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      1,
      1,
      0,
      0,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      0,
      2,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      1,
      2,
      2,
      2,
      2,
      2,
      2,
      2,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      2,
      2,
      2,
      2,
      1,
      1,
      2,
      1,
      2,
      1,
      1,
      1,
      0,
      2,
      1,
      2,
      1,
      1,
      1,
      0,
      2,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      1,
      1,
      0,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      2,
      2,
      3,
      2,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      2,
      2,
      2,
      2,
      2,
      0,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      1,
      2,
      3,
      1,
      2,
      1,
      0,
      1,
      1,
      0,
      2,
      2,
      2,
      0,
      0,
      1,
      0,
      0,
      1,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      2,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      0,
      1,
      0,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      2,
      2,
      2,
      2,
      0,
      0,
      2,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      2,
      2,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      2,
      1,
      1,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      2,
      2,
      2,
      0,
      0,
      2,
      0,
      1,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      2,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
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
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      2,
      3,
      2,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      2,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      0,
      1,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      1,
      2,
      2,
      2,
      1,
      2,
      1,
      2,
      2,
      1,
      1,
      2,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      2,
      0,
      1,
      0,
      1,
      1,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      1,
      3,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      2,
      2,
      2,
      1,
      0,
      0,
      1,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      1,
      0,
      2,
      0,
      1,
      0,
      0,
      1,
      1,
      2,
      0,
      1,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      2,
      2,
      2,
      0,
      1,
      1,
      0,
      2,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      0,
      2,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      2,
      2,
      2,
      2,
      0,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      1,
      0,
      0,
      1,
      2,
      1,
      1,
      1,
      1,
      1,
      1,
      2,
      2,
      1,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      1,
      1,
      1,
      0,
      0,
      0,
      1,
      1,
      2,
      1,
      1,
      1,
      1,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      2,
      1,
      2,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
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
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      1,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
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
      1,
      1,
      0,
      0,
      1,
      0,
      2,
      0,
      0,
      2,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      0,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      1,
      0,
      1,
      1,
      0,
      0,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
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
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      1,
      1,
      0,
      1,
      1,
      1,
      1,
      1,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1
   };

   public BulgarianModel(short[] var1, String var2) {
      super(var1, bulgarianLangModel, 0.969392F, false, var2);
   }
}
