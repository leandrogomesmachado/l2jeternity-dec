package org.mozilla.universalchardet.prober.statemachine;

public class PkgInt {
   public static final int INDEX_SHIFT_4BITS = 3;
   public static final int INDEX_SHIFT_8BITS = 2;
   public static final int INDEX_SHIFT_16BITS = 1;
   public static final int SHIFT_MASK_4BITS = 7;
   public static final int SHIFT_MASK_8BITS = 3;
   public static final int SHIFT_MASK_16BITS = 1;
   public static final int BIT_SHIFT_4BITS = 2;
   public static final int BIT_SHIFT_8BITS = 3;
   public static final int BIT_SHIFT_16BITS = 4;
   public static final int UNIT_MASK_4BITS = 15;
   public static final int UNIT_MASK_8BITS = 255;
   public static final int UNIT_MASK_16BITS = 65535;
   private int indexShift;
   private int shiftMask;
   private int bitShift;
   private int unitMask;
   private int[] data;

   public PkgInt(int var1, int var2, int var3, int var4, int[] var5) {
      this.indexShift = var1;
      this.shiftMask = var2;
      this.bitShift = var3;
      this.unitMask = var4;
      this.data = var5;
   }

   public static int pack16bits(int var0, int var1) {
      return var1 << 16 | var0;
   }

   public static int pack8bits(int var0, int var1, int var2, int var3) {
      return pack16bits(var1 << 8 | var0, var3 << 8 | var2);
   }

   public static int pack4bits(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return pack8bits(var1 << 4 | var0, var3 << 4 | var2, var5 << 4 | var4, var7 << 4 | var6);
   }

   public int unpack(int var1) {
      return this.data[var1 >> this.indexShift] >> ((var1 & this.shiftMask) << this.bitShift) & this.unitMask;
   }
}
