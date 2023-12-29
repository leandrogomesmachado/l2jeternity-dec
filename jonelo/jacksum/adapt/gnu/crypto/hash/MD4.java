package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class MD4 extends BaseHash {
   private static final int DIGEST_LENGTH = 16;
   private static final int BLOCK_LENGTH = 64;
   private static final int A = 1732584193;
   private static final int B = -271733879;
   private static final int C = -1732584194;
   private static final int D = 271733878;
   private static final String DIGEST0 = "31D6CFE0D16AE931B73C59D7E0C089C0";
   private static Boolean valid;
   private int a;
   private int b;
   private int c;
   private int d;

   public MD4() {
      super("md4", 16, 64);
   }

   private MD4(MD4 var1) {
      this();
      this.a = var1.a;
      this.b = var1.b;
      this.c = var1.c;
      this.d = var1.d;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public Object clone() {
      return new MD4(this);
   }

   protected byte[] getResult() {
      return new byte[]{
         (byte)this.a,
         (byte)(this.a >>> 8),
         (byte)(this.a >>> 16),
         (byte)(this.a >>> 24),
         (byte)this.b,
         (byte)(this.b >>> 8),
         (byte)(this.b >>> 16),
         (byte)(this.b >>> 24),
         (byte)this.c,
         (byte)(this.c >>> 8),
         (byte)(this.c >>> 16),
         (byte)(this.c >>> 24),
         (byte)this.d,
         (byte)(this.d >>> 8),
         (byte)(this.d >>> 16),
         (byte)(this.d >>> 24)
      };
   }

   protected void resetContext() {
      this.a = 1732584193;
      this.b = -271733879;
      this.c = -1732584194;
      this.d = 271733878;
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean("31D6CFE0D16AE931B73C59D7E0C089C0".equals(Util.toString(new MD4().digest())));
      }

      return valid;
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

   protected void transform(byte[] var1, int var2) {
      int var3 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var4 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var5 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var6 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var7 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var8 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var9 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var10 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var11 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var12 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var13 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var14 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var15 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var16 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var17 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      int var18 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2] << 24;
      int var19 = this.a;
      int var20 = this.b;
      int var21 = this.c;
      int var22 = this.d;
      var19 += (var20 & var21 | ~var20 & var22) + var3;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & var20 | ~var19 & var21) + var4;
      var22 = var22 << 7 | var22 >>> -7;
      var21 += (var22 & var19 | ~var22 & var20) + var5;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 & var22 | ~var21 & var19) + var6;
      var20 = var20 << 19 | var20 >>> -19;
      var19 += (var20 & var21 | ~var20 & var22) + var7;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & var20 | ~var19 & var21) + var8;
      var22 = var22 << 7 | var22 >>> -7;
      var21 += (var22 & var19 | ~var22 & var20) + var9;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 & var22 | ~var21 & var19) + var10;
      var20 = var20 << 19 | var20 >>> -19;
      var19 += (var20 & var21 | ~var20 & var22) + var11;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & var20 | ~var19 & var21) + var12;
      var22 = var22 << 7 | var22 >>> -7;
      var21 += (var22 & var19 | ~var22 & var20) + var13;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 & var22 | ~var21 & var19) + var14;
      var20 = var20 << 19 | var20 >>> -19;
      var19 += (var20 & var21 | ~var20 & var22) + var15;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & var20 | ~var19 & var21) + var16;
      var22 = var22 << 7 | var22 >>> -7;
      var21 += (var22 & var19 | ~var22 & var20) + var17;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 & var22 | ~var21 & var19) + var18;
      var20 = var20 << 19 | var20 >>> -19;
      var19 += (var20 & (var21 | var22) | var21 & var22) + var3 + 1518500249;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & (var20 | var21) | var20 & var21) + var7 + 1518500249;
      var22 = var22 << 5 | var22 >>> -5;
      var21 += (var22 & (var19 | var20) | var19 & var20) + var11 + 1518500249;
      var21 = var21 << 9 | var21 >>> -9;
      var20 += (var21 & (var22 | var19) | var22 & var19) + var15 + 1518500249;
      var20 = var20 << 13 | var20 >>> -13;
      var19 += (var20 & (var21 | var22) | var21 & var22) + var4 + 1518500249;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & (var20 | var21) | var20 & var21) + var8 + 1518500249;
      var22 = var22 << 5 | var22 >>> -5;
      var21 += (var22 & (var19 | var20) | var19 & var20) + var12 + 1518500249;
      var21 = var21 << 9 | var21 >>> -9;
      var20 += (var21 & (var22 | var19) | var22 & var19) + var16 + 1518500249;
      var20 = var20 << 13 | var20 >>> -13;
      var19 += (var20 & (var21 | var22) | var21 & var22) + var5 + 1518500249;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & (var20 | var21) | var20 & var21) + var9 + 1518500249;
      var22 = var22 << 5 | var22 >>> -5;
      var21 += (var22 & (var19 | var20) | var19 & var20) + var13 + 1518500249;
      var21 = var21 << 9 | var21 >>> -9;
      var20 += (var21 & (var22 | var19) | var22 & var19) + var17 + 1518500249;
      var20 = var20 << 13 | var20 >>> -13;
      var19 += (var20 & (var21 | var22) | var21 & var22) + var6 + 1518500249;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 & (var20 | var21) | var20 & var21) + var10 + 1518500249;
      var22 = var22 << 5 | var22 >>> -5;
      var21 += (var22 & (var19 | var20) | var19 & var20) + var14 + 1518500249;
      var21 = var21 << 9 | var21 >>> -9;
      var20 += (var21 & (var22 | var19) | var22 & var19) + var18 + 1518500249;
      var20 = var20 << 13 | var20 >>> -13;
      var19 += (var20 ^ var21 ^ var22) + var3 + 1859775393;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 ^ var20 ^ var21) + var11 + 1859775393;
      var22 = var22 << 9 | var22 >>> -9;
      var21 += (var22 ^ var19 ^ var20) + var7 + 1859775393;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 ^ var22 ^ var19) + var15 + 1859775393;
      var20 = var20 << 15 | var20 >>> -15;
      var19 += (var20 ^ var21 ^ var22) + var5 + 1859775393;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 ^ var20 ^ var21) + var13 + 1859775393;
      var22 = var22 << 9 | var22 >>> -9;
      var21 += (var22 ^ var19 ^ var20) + var9 + 1859775393;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 ^ var22 ^ var19) + var17 + 1859775393;
      var20 = var20 << 15 | var20 >>> -15;
      var19 += (var20 ^ var21 ^ var22) + var4 + 1859775393;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 ^ var20 ^ var21) + var12 + 1859775393;
      var22 = var22 << 9 | var22 >>> -9;
      var21 += (var22 ^ var19 ^ var20) + var8 + 1859775393;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 ^ var22 ^ var19) + var16 + 1859775393;
      var20 = var20 << 15 | var20 >>> -15;
      var19 += (var20 ^ var21 ^ var22) + var6 + 1859775393;
      var19 = var19 << 3 | var19 >>> -3;
      var22 += (var19 ^ var20 ^ var21) + var14 + 1859775393;
      var22 = var22 << 9 | var22 >>> -9;
      var21 += (var22 ^ var19 ^ var20) + var10 + 1859775393;
      var21 = var21 << 11 | var21 >>> -11;
      var20 += (var21 ^ var22 ^ var19) + var18 + 1859775393;
      var20 = var20 << 15 | var20 >>> -15;
      this.a += var19;
      this.b += var20;
      this.c += var21;
      this.d += var22;
   }
}
