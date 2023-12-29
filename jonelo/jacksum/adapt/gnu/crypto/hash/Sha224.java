package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Sha224 extends BaseHash {
   private static final int[] k = new int[]{
      1116352408,
      1899447441,
      -1245643825,
      -373957723,
      961987163,
      1508970993,
      -1841331548,
      -1424204075,
      -670586216,
      310598401,
      607225278,
      1426881987,
      1925078388,
      -2132889090,
      -1680079193,
      -1046744716,
      -459576895,
      -272742522,
      264347078,
      604807628,
      770255983,
      1249150122,
      1555081692,
      1996064986,
      -1740746414,
      -1473132947,
      -1341970488,
      -1084653625,
      -958395405,
      -710438585,
      113926993,
      338241895,
      666307205,
      773529912,
      1294757372,
      1396182291,
      1695183700,
      1986661051,
      -2117940946,
      -1838011259,
      -1564481375,
      -1474664885,
      -1035236496,
      -949202525,
      -778901479,
      -694614492,
      -200395387,
      275423344,
      430227734,
      506948616,
      659060556,
      883997877,
      958139571,
      1322822218,
      1537002063,
      1747873779,
      1955562222,
      2024104815,
      -2067236844,
      -1933114872,
      -1866530822,
      -1538233109,
      -1090935817,
      -965641998
   };
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "23097D223405D8228642A477BDA255B32AADBCE4BDA0B3F7E36C9DA7";
   private static final int[] w = new int[64];
   private static Boolean valid;
   private int h0;
   private int h1;
   private int h2;
   private int h3;
   private int h4;
   private int h5;
   private int h6;
   private int h7;

   public Sha224() {
      super("sha-224", 32, 64);
   }

   private Sha224(Sha224 var1) {
      this();
      this.h0 = var1.h0;
      this.h1 = var1.h1;
      this.h2 = var1.h2;
      this.h3 = var1.h3;
      this.h4 = var1.h4;
      this.h5 = var1.h5;
      this.h6 = var1.h6;
      this.h7 = var1.h7;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public static final int[] G(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, byte[] var8, int var9) {
      return sha(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9);
   }

   public Object clone() {
      return new Sha224(this);
   }

   protected void transform(byte[] var1, int var2) {
      int[] var3 = sha(this.h0, this.h1, this.h2, this.h3, this.h4, this.h5, this.h6, this.h7, var1, var2);
      this.h0 = var3[0];
      this.h1 = var3[1];
      this.h2 = var3[2];
      this.h3 = var3[3];
      this.h4 = var3[4];
      this.h5 = var3[5];
      this.h6 = var3[6];
      this.h7 = var3[7];
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
         (byte)this.h4,
         (byte)(this.h5 >>> 24),
         (byte)(this.h5 >>> 16),
         (byte)(this.h5 >>> 8),
         (byte)this.h5,
         (byte)(this.h6 >>> 24),
         (byte)(this.h6 >>> 16),
         (byte)(this.h6 >>> 8),
         (byte)this.h6
      };
   }

   protected void resetContext() {
      this.h0 = -1056596264;
      this.h1 = 914150663;
      this.h2 = 812702999;
      this.h3 = -150054599;
      this.h4 = -4191439;
      this.h5 = 1750603025;
      this.h6 = 1694076839;
      this.h7 = -1090891868;
   }

   public boolean selfTest() {
      if (valid == null) {
         Sha224 var1 = new Sha224();
         var1.update((byte)97);
         var1.update((byte)98);
         var1.update((byte)99);
         String var2 = Util.toString(var1.digest());
         valid = new Boolean("23097D223405D8228642A477BDA255B32AADBCE4BDA0B3F7E36C9DA7".equals(var2));
      }

      return valid;
   }

   private static final synchronized int[] sha(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, byte[] var8, int var9) {
      int var10 = var0;
      int var11 = var1;
      int var12 = var2;
      int var13 = var3;
      int var14 = var4;
      int var15 = var5;
      int var16 = var6;
      int var17 = var7;

      for(int var18 = 0; var18 < 16; ++var18) {
         w[var18] = var8[var9++] << 24 | (var8[var9++] & 255) << 16 | (var8[var9++] & 255) << 8 | var8[var9++] & 255;
      }

      for(int var24 = 16; var24 < 64; ++var24) {
         int var19 = w[var24 - 2];
         int var20 = w[var24 - 15];
         w[var24] = ((var19 >>> 17 | var19 << 15) ^ (var19 >>> 19 | var19 << 13) ^ var19 >>> 10)
            + w[var24 - 7]
            + ((var20 >>> 7 | var20 << 25) ^ (var20 >>> 18 | var20 << 14) ^ var20 >>> 3)
            + w[var24 - 16];
      }

      for(int var25 = 0; var25 < 64; ++var25) {
         int var26 = var17
            + ((var14 >>> 6 | var14 << 26) ^ (var14 >>> 11 | var14 << 21) ^ (var14 >>> 25 | var14 << 7))
            + (var14 & var15 ^ ~var14 & var16)
            + k[var25]
            + w[var25];
         int var27 = ((var10 >>> 2 | var10 << 30) ^ (var10 >>> 13 | var10 << 19) ^ (var10 >>> 22 | var10 << 10))
            + (var10 & var11 ^ var10 & var12 ^ var11 & var12);
         var17 = var16;
         var16 = var15;
         var15 = var14;
         var14 = var13 + var26;
         var13 = var12;
         var12 = var11;
         var11 = var10;
         var10 = var26 + var27;
      }

      return new int[]{var0 + var10, var1 + var11, var2 + var12, var3 + var13, var4 + var14, var5 + var15, var6 + var16, var7 + var17};
   }
}
