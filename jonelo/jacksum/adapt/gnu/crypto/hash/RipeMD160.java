package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class RipeMD160 extends BaseHash {
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "9C1185A5C5E9FC54612808977EE8F548B2258D31";
   private static final int[] R = new int[]{
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15,
      7,
      4,
      13,
      1,
      10,
      6,
      15,
      3,
      12,
      0,
      9,
      5,
      2,
      14,
      11,
      8,
      3,
      10,
      14,
      4,
      9,
      15,
      8,
      1,
      2,
      7,
      0,
      6,
      13,
      11,
      5,
      12,
      1,
      9,
      11,
      10,
      0,
      8,
      12,
      4,
      13,
      3,
      7,
      15,
      14,
      5,
      6,
      2,
      4,
      0,
      5,
      9,
      7,
      12,
      2,
      10,
      14,
      1,
      3,
      8,
      11,
      6,
      15,
      13
   };
   private static final int[] Rp = new int[]{
      5,
      14,
      7,
      0,
      9,
      2,
      11,
      4,
      13,
      6,
      15,
      8,
      1,
      10,
      3,
      12,
      6,
      11,
      3,
      7,
      0,
      13,
      5,
      10,
      14,
      15,
      8,
      12,
      4,
      9,
      1,
      2,
      15,
      5,
      1,
      3,
      7,
      14,
      6,
      9,
      11,
      8,
      12,
      2,
      10,
      0,
      4,
      13,
      8,
      6,
      4,
      1,
      3,
      11,
      15,
      0,
      5,
      12,
      2,
      13,
      9,
      7,
      10,
      14,
      12,
      15,
      10,
      4,
      1,
      5,
      8,
      7,
      6,
      2,
      13,
      14,
      0,
      3,
      9,
      11
   };
   private static final int[] S = new int[]{
      11,
      14,
      15,
      12,
      5,
      8,
      7,
      9,
      11,
      13,
      14,
      15,
      6,
      7,
      9,
      8,
      7,
      6,
      8,
      13,
      11,
      9,
      7,
      15,
      7,
      12,
      15,
      9,
      11,
      7,
      13,
      12,
      11,
      13,
      6,
      7,
      14,
      9,
      13,
      15,
      14,
      8,
      13,
      6,
      5,
      12,
      7,
      5,
      11,
      12,
      14,
      15,
      14,
      15,
      9,
      8,
      9,
      14,
      5,
      6,
      8,
      6,
      5,
      12,
      9,
      15,
      5,
      11,
      6,
      8,
      13,
      12,
      5,
      12,
      13,
      14,
      11,
      8,
      5,
      6
   };
   private static final int[] Sp = new int[]{
      8,
      9,
      9,
      11,
      13,
      15,
      15,
      5,
      7,
      7,
      8,
      11,
      14,
      14,
      12,
      6,
      9,
      13,
      15,
      7,
      12,
      8,
      9,
      11,
      7,
      7,
      12,
      7,
      6,
      15,
      13,
      11,
      9,
      7,
      15,
      11,
      8,
      6,
      6,
      14,
      12,
      13,
      5,
      14,
      13,
      13,
      7,
      5,
      15,
      5,
      8,
      11,
      14,
      14,
      6,
      14,
      6,
      9,
      12,
      9,
      12,
      5,
      15,
      8,
      8,
      5,
      12,
      9,
      12,
      5,
      14,
      6,
      8,
      13,
      6,
      5,
      15,
      13,
      11,
      11
   };
   private static Boolean valid;
   private int h0;
   private int h1;
   private int h2;
   private int h3;
   private int h4;
   private int[] X = new int[16];

   public RipeMD160() {
      super("ripemd160", 20, 64);
   }

   private RipeMD160(RipeMD160 var1) {
      this();
      this.h0 = var1.h0;
      this.h1 = var1.h1;
      this.h2 = var1.h2;
      this.h3 = var1.h3;
      this.h4 = var1.h4;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public Object clone() {
      return new RipeMD160(this);
   }

   protected void transform(byte[] var1, int var2) {
      for(int var15 = 0; var15 < 16; ++var15) {
         this.X[var15] = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | var1[var2++] << 24;
      }

      int var8 = this.h0;
      int var3 = this.h0;
      int var9 = this.h1;
      int var4 = this.h1;
      int var10 = this.h2;
      int var5 = this.h2;
      int var11 = this.h3;
      int var6 = this.h3;
      int var12 = this.h4;
      int var7 = this.h4;

      int var38;
      for(var38 = 0; var38 < 16; ++var38) {
         int var14 = S[var38];
         int var13 = var3 + (var4 ^ var5 ^ var6) + this.X[var38];
         var3 = var7;
         var7 = var6;
         var6 = var5 << 10 | var5 >>> 22;
         var5 = var4;
         var4 = (var13 << var14 | var13 >>> 32 - var14) + var3;
         var14 = Sp[var38];
         var13 = var8 + (var9 ^ (var10 | ~var11)) + this.X[Rp[var38]] + 1352829926;
         var8 = var12;
         var12 = var11;
         var11 = var10 << 10 | var10 >>> 22;
         var10 = var9;
         var9 = (var13 << var14 | var13 >>> 32 - var14) + var8;
      }

      while(var38 < 32) {
         int var30 = S[var38];
         int var20 = var3 + (var4 & var5 | ~var4 & var6) + this.X[R[var38]] + 1518500249;
         var3 = var7;
         var7 = var6;
         var6 = var5 << 10 | var5 >>> 22;
         var5 = var4;
         var4 = (var20 << var30 | var20 >>> 32 - var30) + var3;
         var30 = Sp[var38];
         var20 = var8 + (var9 & var11 | var10 & ~var11) + this.X[Rp[var38]] + 1548603684;
         var8 = var12;
         var12 = var11;
         var11 = var10 << 10 | var10 >>> 22;
         var10 = var9;
         var9 = (var20 << var30 | var20 >>> 32 - var30) + var8;
         ++var38;
      }

      while(var38 < 48) {
         int var32 = S[var38];
         int var22 = var3 + ((var4 | ~var5) ^ var6) + this.X[R[var38]] + 1859775393;
         var3 = var7;
         var7 = var6;
         var6 = var5 << 10 | var5 >>> 22;
         var5 = var4;
         var4 = (var22 << var32 | var22 >>> 32 - var32) + var3;
         var32 = Sp[var38];
         var22 = var8 + ((var9 | ~var10) ^ var11) + this.X[Rp[var38]] + 1836072691;
         var8 = var12;
         var12 = var11;
         var11 = var10 << 10 | var10 >>> 22;
         var10 = var9;
         var9 = (var22 << var32 | var22 >>> 32 - var32) + var8;
         ++var38;
      }

      while(var38 < 64) {
         int var34 = S[var38];
         int var24 = var3 + (var4 & var6 | var5 & ~var6) + this.X[R[var38]] + -1894007588;
         var3 = var7;
         var7 = var6;
         var6 = var5 << 10 | var5 >>> 22;
         var5 = var4;
         var4 = (var24 << var34 | var24 >>> 32 - var34) + var3;
         var34 = Sp[var38];
         var24 = var8 + (var9 & var10 | ~var9 & var11) + this.X[Rp[var38]] + 2053994217;
         var8 = var12;
         var12 = var11;
         var11 = var10 << 10 | var10 >>> 22;
         var10 = var9;
         var9 = (var24 << var34 | var24 >>> 32 - var34) + var8;
         ++var38;
      }

      while(var38 < 80) {
         int var36 = S[var38];
         int var26 = var3 + (var4 ^ (var5 | ~var6)) + this.X[R[var38]] + -1454113458;
         var3 = var7;
         var7 = var6;
         var6 = var5 << 10 | var5 >>> 22;
         var5 = var4;
         var4 = (var26 << var36 | var26 >>> 32 - var36) + var3;
         var36 = Sp[var38];
         var26 = var8 + (var9 ^ var10 ^ var11) + this.X[Rp[var38]];
         var8 = var12;
         var12 = var11;
         var11 = var10 << 10 | var10 >>> 22;
         var10 = var9;
         var9 = (var26 << var36 | var26 >>> 32 - var36) + var8;
         ++var38;
      }

      int var28 = this.h1 + var5 + var11;
      this.h1 = this.h2 + var6 + var12;
      this.h2 = this.h3 + var7 + var8;
      this.h3 = this.h4 + var3 + var9;
      this.h4 = this.h0 + var4 + var10;
      this.h0 = var28;
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
         valid = new Boolean("9C1185A5C5E9FC54612808977EE8F548B2258D31".equals(Util.toString(new RipeMD160().digest())));
      }

      return valid;
   }
}
