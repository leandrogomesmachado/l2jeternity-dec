package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Sha160 extends BaseHash {
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "A9993E364706816ABA3E25717850C26C9CD0D89D";
   private static final int[] w = new int[80];
   private static Boolean valid;
   private int h0;
   private int h1;
   private int h2;
   private int h3;
   private int h4;

   public Sha160() {
      super("sha-160", 20, 64);
   }

   private Sha160(Sha160 var1) {
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
      return sha(var0, var1, var2, var3, var4, var5, var6);
   }

   public Object clone() {
      return new Sha160(this);
   }

   protected void transform(byte[] var1, int var2) {
      int[] var3 = sha(this.h0, this.h1, this.h2, this.h3, this.h4, var1, var2);
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
      var3[var2++] = (byte)((int)(var4 >>> 56));
      var3[var2++] = (byte)((int)(var4 >>> 48));
      var3[var2++] = (byte)((int)(var4 >>> 40));
      var3[var2++] = (byte)((int)(var4 >>> 32));
      var3[var2++] = (byte)((int)(var4 >>> 24));
      var3[var2++] = (byte)((int)(var4 >>> 16));
      var3[var2++] = (byte)((int)(var4 >>> 8));
      var3[var2] = (byte)((int)var4);
      return var3;
   }

   protected byte[] getResult() {
      return new byte[]{
         (byte)(this.h0 >>> 24),
         (byte)(this.h0 >>> 16),
         (byte)(this.h0 >>> 8),
         (byte)this.h0,
         (byte)(this.h1 >>> 24),
         (byte)(this.h1 >>> 16),
         (byte)(this.h1 >>> 8),
         (byte)this.h1,
         (byte)(this.h2 >>> 24),
         (byte)(this.h2 >>> 16),
         (byte)(this.h2 >>> 8),
         (byte)this.h2,
         (byte)(this.h3 >>> 24),
         (byte)(this.h3 >>> 16),
         (byte)(this.h3 >>> 8),
         (byte)this.h3,
         (byte)(this.h4 >>> 24),
         (byte)(this.h4 >>> 16),
         (byte)(this.h4 >>> 8),
         (byte)this.h4
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
         Sha160 var1 = new Sha160();
         var1.update((byte)97);
         var1.update((byte)98);
         var1.update((byte)99);
         String var2 = Util.toString(var1.digest());
         valid = new Boolean("A9993E364706816ABA3E25717850C26C9CD0D89D".equals(var2));
      }

      return valid;
   }

   private static final synchronized int[] sha(int var0, int var1, int var2, int var3, int var4, byte[] var5, int var6) {
      int var7 = var0;
      int var8 = var1;
      int var9 = var2;
      int var10 = var3;
      int var11 = var4;

      for(int var12 = 0; var12 < 16; ++var12) {
         w[var12] = var5[var6++] << 24 | (var5[var6++] & 255) << 16 | (var5[var6++] & 255) << 8 | var5[var6++] & 255;
      }

      for(int var17 = 16; var17 < 80; ++var17) {
         int var13 = w[var17 - 3] ^ w[var17 - 8] ^ w[var17 - 14] ^ w[var17 - 16];
         w[var17] = var13 << 1 | var13 >>> 31;
      }

      for(int var18 = 0; var18 < 20; ++var18) {
         int var22 = (var7 << 5 | var7 >>> 27) + (var8 & var9 | ~var8 & var10) + var11 + w[var18] + 1518500249;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 30 | var8 >>> 2;
         var8 = var7;
         var7 = var22;
      }

      for(int var19 = 20; var19 < 40; ++var19) {
         int var23 = (var7 << 5 | var7 >>> 27) + (var8 ^ var9 ^ var10) + var11 + w[var19] + 1859775393;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 30 | var8 >>> 2;
         var8 = var7;
         var7 = var23;
      }

      for(int var20 = 40; var20 < 60; ++var20) {
         int var24 = (var7 << 5 | var7 >>> 27) + (var8 & var9 | var8 & var10 | var9 & var10) + var11 + w[var20] + -1894007588;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 30 | var8 >>> 2;
         var8 = var7;
         var7 = var24;
      }

      for(int var21 = 60; var21 < 80; ++var21) {
         int var25 = (var7 << 5 | var7 >>> 27) + (var8 ^ var9 ^ var10) + var11 + w[var21] + -899497514;
         var11 = var10;
         var10 = var9;
         var9 = var8 << 30 | var8 >>> 2;
         var8 = var7;
         var7 = var25;
      }

      return new int[]{var0 + var7, var1 + var8, var2 + var9, var3 + var10, var4 + var11};
   }
}
