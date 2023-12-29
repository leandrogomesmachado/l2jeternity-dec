package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Has160 extends BaseHash {
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "975E810488CF2A3D49838478124AFCE4B1C78804";
   private static final int[] w = new int[20];
   private static Boolean valid;
   private int h0;
   private int h1;
   private int h2;
   private int h3;
   private int h4;
   private static final int[] rot = new int[]{5, 11, 7, 15, 6, 13, 8, 14, 7, 12, 9, 11, 8, 15, 6, 12, 9, 14, 5, 13};
   private static final int[] tor = new int[]{27, 21, 25, 17, 26, 19, 24, 18, 25, 20, 23, 21, 24, 17, 26, 20, 23, 18, 27, 19};
   private static int[] ndx = new int[]{
      18,
      0,
      1,
      2,
      3,
      19,
      4,
      5,
      6,
      7,
      16,
      8,
      9,
      10,
      11,
      17,
      12,
      13,
      14,
      15,
      18,
      3,
      6,
      9,
      12,
      19,
      15,
      2,
      5,
      8,
      16,
      11,
      14,
      1,
      4,
      17,
      7,
      10,
      13,
      0,
      18,
      12,
      5,
      14,
      7,
      19,
      0,
      9,
      2,
      11,
      16,
      4,
      13,
      6,
      15,
      17,
      8,
      1,
      10,
      3,
      18,
      7,
      2,
      13,
      8,
      19,
      3,
      14,
      9,
      4,
      16,
      15,
      10,
      5,
      0,
      17,
      11,
      6,
      1,
      12
   };

   public Has160() {
      super("has-160", 20, 64);
   }

   private Has160(Has160 var1) {
      this();
      this.h0 = var1.h0;
      this.h1 = var1.h1;
      this.h2 = var1.h2;
      this.h3 = var1.h3;
      this.h4 = var1.h4;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public static final int[] G(int var0, int var1, int var2, int var3, int var4, byte[] var5, int var6) {
      return has(var0, var1, var2, var3, var4, var5, var6);
   }

   public Object clone() {
      return new Has160(this);
   }

   protected void transform(byte[] var1, int var2) {
      int[] var3 = has(this.h0, this.h1, this.h2, this.h3, this.h4, var1, var2);
      this.h0 = var3[0];
      this.h1 = var3[1];
      this.h2 = var3[2];
      this.h3 = var3[3];
      this.h4 = var3[4];
   }

   protected byte[] padBuffer() {
      int var1 = (int)(this.count % 64L);
      int var2 = var1 < 56 ? 56 - var1 : 120 - var1;
      byte[] var3 = new byte[var2 + 8];
      var3[0] = -128;
      long var4 = this.count << 3;
      var3[var2++] = (byte)((int)var4);
      var3[var2++] = (byte)((int)(var4 >>> 8));
      var3[var2++] = (byte)((int)(var4 >>> 16));
      var3[var2++] = (byte)((int)(var4 >>> 24));
      var3[var2++] = (byte)((int)(var4 >>> 32));
      var3[var2++] = (byte)((int)(var4 >>> 40));
      var3[var2++] = (byte)((int)(var4 >>> 48));
      var3[var2] = (byte)((int)(var4 >>> 56));
      return var3;
   }

   protected byte[] getResult() {
      return new byte[]{
         (byte)this.h0,
         (byte)(this.h0 >>> 8),
         (byte)(this.h0 >>> 16),
         (byte)(this.h0 >>> 24),
         (byte)this.h1,
         (byte)(this.h1 >>> 8),
         (byte)(this.h1 >>> 16),
         (byte)(this.h1 >>> 24),
         (byte)this.h2,
         (byte)(this.h2 >>> 8),
         (byte)(this.h2 >>> 16),
         (byte)(this.h2 >>> 24),
         (byte)this.h3,
         (byte)(this.h3 >>> 8),
         (byte)(this.h3 >>> 16),
         (byte)(this.h3 >>> 24),
         (byte)this.h4,
         (byte)(this.h4 >>> 8),
         (byte)(this.h4 >>> 16),
         (byte)(this.h4 >>> 24)
      };
   }

   protected void resetContext() {
      this.h0 = 1732584193;
      this.h1 = -271733879;
      this.h2 = -1732584194;
      this.h3 = 271733878;
      this.h4 = -1009589776;
   }

   public boolean selfTest() {
      if (valid == null) {
         Has160 var1 = new Has160();
         var1.update((byte)97);
         var1.update((byte)98);
         var1.update((byte)99);
         String var2 = Util.toString(var1.digest());
         valid = new Boolean("975E810488CF2A3D49838478124AFCE4B1C78804".equals(var2));
      }

      return valid;
   }

   private static final synchronized int[] has(int var0, int var1, int var2, int var3, int var4, byte[] var5, int var6) {
      int var7 = var0;
      int var8 = var1;
      int var9 = var2;
      int var10 = var3;
      int var11 = var4;

      for(int var12 = 0; var12 < 16; ++var12) {
         w[var12] = var5[var6 + 3] << 24 | (var5[var6 + 2] & 255) << 16 | (var5[var6 + 1] & 255) << 8 | var5[var6] & 255;
         var6 += 4;
      }

      w[16] = w[0] ^ w[1] ^ w[2] ^ w[3];
      w[17] = w[4] ^ w[5] ^ w[6] ^ w[7];
      w[18] = w[8] ^ w[9] ^ w[10] ^ w[11];
      w[19] = w[12] ^ w[13] ^ w[14] ^ w[15];

      for(int var14 = 0; var14 < 20; ++var14) {
         int var13 = (var7 << rot[var14] | var7 >>> tor[var14]) + (var8 & var9 | ~var8 & var10) + var11 + w[ndx[var14]];
         var11 = var10;
         var10 = var9;
         var9 = var8 << 10 | var8 >>> 22;
         var8 = var7;
         var7 = var13;
      }

      w[16] = w[3] ^ w[6] ^ w[9] ^ w[12];
      w[17] = w[2] ^ w[5] ^ w[8] ^ w[15];
      w[18] = w[1] ^ w[4] ^ w[11] ^ w[14];
      w[19] = w[0] ^ w[7] ^ w[10] ^ w[13];

      for(int var15 = 20; var15 < 40; ++var15) {
         int var18 = (var7 << rot[var15 - 20] | var7 >>> tor[var15 - 20]) + (var8 ^ var9 ^ var10) + var11 + w[ndx[var15]] + 1518500249;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 17 | var8 >>> 15;
         var8 = var7;
         var7 = var18;
      }

      w[16] = w[5] ^ w[7] ^ w[12] ^ w[14];
      w[17] = w[0] ^ w[2] ^ w[9] ^ w[11];
      w[18] = w[4] ^ w[6] ^ w[13] ^ w[15];
      w[19] = w[1] ^ w[3] ^ w[8] ^ w[10];

      for(int var16 = 40; var16 < 60; ++var16) {
         int var19 = (var7 << rot[var16 - 40] | var7 >>> tor[var16 - 40]) + (var9 ^ (var8 | ~var10)) + var11 + w[ndx[var16]] + 1859775393;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 25 | var8 >>> 7;
         var8 = var7;
         var7 = var19;
      }

      w[16] = w[2] ^ w[7] ^ w[8] ^ w[13];
      w[17] = w[3] ^ w[4] ^ w[9] ^ w[14];
      w[18] = w[0] ^ w[5] ^ w[10] ^ w[15];
      w[19] = w[1] ^ w[6] ^ w[11] ^ w[12];

      for(int var17 = 60; var17 < 80; ++var17) {
         int var20 = (var7 << rot[var17 - 60] | var7 >>> tor[var17 - 60]) + (var8 ^ var9 ^ var10) + var11 + w[ndx[var17]] + -1894007588;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 30 | var8 >>> 2;
         var8 = var7;
         var7 = var20;
      }

      return new int[]{var0 + var7, var1 + var8, var2 + var9, var3 + var10, var4 + var11};
   }
}
