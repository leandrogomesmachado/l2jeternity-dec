package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class MD5 extends BaseHash {
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "D41D8CD98F00B204E9800998ECF8427E";
   private static Boolean valid;
   private int h0;
   private int h1;
   private int h2;
   private int h3;

   public MD5() {
      super("md5", 16, 64);
   }

   private MD5(MD5 var1) {
      this();
      this.h0 = var1.h0;
      this.h1 = var1.h1;
      this.h2 = var1.h2;
      this.h3 = var1.h3;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public Object clone() {
      return new MD5(this);
   }

   protected synchronized void transform(byte[] var1, int var2) {
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
      int var19 = this.h0;
      int var20 = this.h1;
      int var21 = this.h2;
      int var22 = this.h3;
      var19 += (var20 & var21 | ~var20 & var22) + var3 + -680876936;
      var19 = var20 + (var19 << 7 | var19 >>> -7);
      var22 += (var19 & var20 | ~var19 & var21) + var4 + -389564586;
      var22 = var19 + (var22 << 12 | var22 >>> -12);
      var21 += (var22 & var19 | ~var22 & var20) + var5 + 606105819;
      var21 = var22 + (var21 << 17 | var21 >>> -17);
      var20 += (var21 & var22 | ~var21 & var19) + var6 + -1044525330;
      var20 = var21 + (var20 << 22 | var20 >>> -22);
      var19 += (var20 & var21 | ~var20 & var22) + var7 + -176418897;
      var19 = var20 + (var19 << 7 | var19 >>> -7);
      var22 += (var19 & var20 | ~var19 & var21) + var8 + 1200080426;
      var22 = var19 + (var22 << 12 | var22 >>> -12);
      var21 += (var22 & var19 | ~var22 & var20) + var9 + -1473231341;
      var21 = var22 + (var21 << 17 | var21 >>> -17);
      var20 += (var21 & var22 | ~var21 & var19) + var10 + -45705983;
      var20 = var21 + (var20 << 22 | var20 >>> -22);
      var19 += (var20 & var21 | ~var20 & var22) + var11 + 1770035416;
      var19 = var20 + (var19 << 7 | var19 >>> -7);
      var22 += (var19 & var20 | ~var19 & var21) + var12 + -1958414417;
      var22 = var19 + (var22 << 12 | var22 >>> -12);
      var21 += (var22 & var19 | ~var22 & var20) + var13 + -42063;
      var21 = var22 + (var21 << 17 | var21 >>> -17);
      var20 += (var21 & var22 | ~var21 & var19) + var14 + -1990404162;
      var20 = var21 + (var20 << 22 | var20 >>> -22);
      var19 += (var20 & var21 | ~var20 & var22) + var15 + 1804603682;
      var19 = var20 + (var19 << 7 | var19 >>> -7);
      var22 += (var19 & var20 | ~var19 & var21) + var16 + -40341101;
      var22 = var19 + (var22 << 12 | var22 >>> -12);
      var21 += (var22 & var19 | ~var22 & var20) + var17 + -1502002290;
      var21 = var22 + (var21 << 17 | var21 >>> -17);
      var20 += (var21 & var22 | ~var21 & var19) + var18 + 1236535329;
      var20 = var21 + (var20 << 22 | var20 >>> -22);
      var19 += (var20 & var22 | var21 & ~var22) + var4 + -165796510;
      var19 = var20 + (var19 << 5 | var19 >>> -5);
      var22 += (var19 & var21 | var20 & ~var21) + var9 + -1069501632;
      var22 = var19 + (var22 << 9 | var22 >>> -9);
      var21 += (var22 & var20 | var19 & ~var20) + var14 + 643717713;
      var21 = var22 + (var21 << 14 | var21 >>> -14);
      var20 += (var21 & var19 | var22 & ~var19) + var3 + -373897302;
      var20 = var21 + (var20 << 20 | var20 >>> -20);
      var19 += (var20 & var22 | var21 & ~var22) + var8 + -701558691;
      var19 = var20 + (var19 << 5 | var19 >>> -5);
      var22 += (var19 & var21 | var20 & ~var21) + var13 + 38016083;
      var22 = var19 + (var22 << 9 | var22 >>> -9);
      var21 += (var22 & var20 | var19 & ~var20) + var18 + -660478335;
      var21 = var22 + (var21 << 14 | var21 >>> -14);
      var20 += (var21 & var19 | var22 & ~var19) + var7 + -405537848;
      var20 = var21 + (var20 << 20 | var20 >>> -20);
      var19 += (var20 & var22 | var21 & ~var22) + var12 + 568446438;
      var19 = var20 + (var19 << 5 | var19 >>> -5);
      var22 += (var19 & var21 | var20 & ~var21) + var17 + -1019803690;
      var22 = var19 + (var22 << 9 | var22 >>> -9);
      var21 += (var22 & var20 | var19 & ~var20) + var6 + -187363961;
      var21 = var22 + (var21 << 14 | var21 >>> -14);
      var20 += (var21 & var19 | var22 & ~var19) + var11 + 1163531501;
      var20 = var21 + (var20 << 20 | var20 >>> -20);
      var19 += (var20 & var22 | var21 & ~var22) + var16 + -1444681467;
      var19 = var20 + (var19 << 5 | var19 >>> -5);
      var22 += (var19 & var21 | var20 & ~var21) + var5 + -51403784;
      var22 = var19 + (var22 << 9 | var22 >>> -9);
      var21 += (var22 & var20 | var19 & ~var20) + var10 + 1735328473;
      var21 = var22 + (var21 << 14 | var21 >>> -14);
      var20 += (var21 & var19 | var22 & ~var19) + var15 + -1926607734;
      var20 = var21 + (var20 << 20 | var20 >>> -20);
      var19 += (var20 ^ var21 ^ var22) + var8 + -378558;
      var19 = var20 + (var19 << 4 | var19 >>> -4);
      var22 += (var19 ^ var20 ^ var21) + var11 + -2022574463;
      var22 = var19 + (var22 << 11 | var22 >>> -11);
      var21 += (var22 ^ var19 ^ var20) + var14 + 1839030562;
      var21 = var22 + (var21 << 16 | var21 >>> -16);
      var20 += (var21 ^ var22 ^ var19) + var17 + -35309556;
      var20 = var21 + (var20 << 23 | var20 >>> -23);
      var19 += (var20 ^ var21 ^ var22) + var4 + -1530992060;
      var19 = var20 + (var19 << 4 | var19 >>> -4);
      var22 += (var19 ^ var20 ^ var21) + var7 + 1272893353;
      var22 = var19 + (var22 << 11 | var22 >>> -11);
      var21 += (var22 ^ var19 ^ var20) + var10 + -155497632;
      var21 = var22 + (var21 << 16 | var21 >>> -16);
      var20 += (var21 ^ var22 ^ var19) + var13 + -1094730640;
      var20 = var21 + (var20 << 23 | var20 >>> -23);
      var19 += (var20 ^ var21 ^ var22) + var16 + 681279174;
      var19 = var20 + (var19 << 4 | var19 >>> -4);
      var22 += (var19 ^ var20 ^ var21) + var3 + -358537222;
      var22 = var19 + (var22 << 11 | var22 >>> -11);
      var21 += (var22 ^ var19 ^ var20) + var6 + -722521979;
      var21 = var22 + (var21 << 16 | var21 >>> -16);
      var20 += (var21 ^ var22 ^ var19) + var9 + 76029189;
      var20 = var21 + (var20 << 23 | var20 >>> -23);
      var19 += (var20 ^ var21 ^ var22) + var12 + -640364487;
      var19 = var20 + (var19 << 4 | var19 >>> -4);
      var22 += (var19 ^ var20 ^ var21) + var15 + -421815835;
      var22 = var19 + (var22 << 11 | var22 >>> -11);
      var21 += (var22 ^ var19 ^ var20) + var18 + 530742520;
      var21 = var22 + (var21 << 16 | var21 >>> -16);
      var20 += (var21 ^ var22 ^ var19) + var5 + -995338651;
      var20 = var21 + (var20 << 23 | var20 >>> -23);
      var19 += (var21 ^ (var20 | ~var22)) + var3 + -198630844;
      var19 = var20 + (var19 << 6 | var19 >>> -6);
      var22 += (var20 ^ (var19 | ~var21)) + var10 + 1126891415;
      var22 = var19 + (var22 << 10 | var22 >>> -10);
      var21 += (var19 ^ (var22 | ~var20)) + var17 + -1416354905;
      var21 = var22 + (var21 << 15 | var21 >>> -15);
      var20 += (var22 ^ (var21 | ~var19)) + var8 + -57434055;
      var20 = var21 + (var20 << 21 | var20 >>> -21);
      var19 += (var21 ^ (var20 | ~var22)) + var15 + 1700485571;
      var19 = var20 + (var19 << 6 | var19 >>> -6);
      var22 += (var20 ^ (var19 | ~var21)) + var6 + -1894986606;
      var22 = var19 + (var22 << 10 | var22 >>> -10);
      var21 += (var19 ^ (var22 | ~var20)) + var13 + -1051523;
      var21 = var22 + (var21 << 15 | var21 >>> -15);
      var20 += (var22 ^ (var21 | ~var19)) + var4 + -2054922799;
      var20 = var21 + (var20 << 21 | var20 >>> -21);
      var19 += (var21 ^ (var20 | ~var22)) + var11 + 1873313359;
      var19 = var20 + (var19 << 6 | var19 >>> -6);
      var22 += (var20 ^ (var19 | ~var21)) + var18 + -30611744;
      var22 = var19 + (var22 << 10 | var22 >>> -10);
      var21 += (var19 ^ (var22 | ~var20)) + var9 + -1560198380;
      var21 = var22 + (var21 << 15 | var21 >>> -15);
      var20 += (var22 ^ (var21 | ~var19)) + var16 + 1309151649;
      var20 = var21 + (var20 << 21 | var20 >>> -21);
      var19 += (var21 ^ (var20 | ~var22)) + var7 + -145523070;
      var19 = var20 + (var19 << 6 | var19 >>> -6);
      var22 += (var20 ^ (var19 | ~var21)) + var14 + -1120210379;
      var22 = var19 + (var22 << 10 | var22 >>> -10);
      var21 += (var19 ^ (var22 | ~var20)) + var5 + 718787259;
      var21 = var22 + (var21 << 15 | var21 >>> -15);
      var20 += (var22 ^ (var21 | ~var19)) + var12 + -343485551;
      var20 = var21 + (var20 << 21 | var20 >>> -21);
      this.h0 += var19;
      this.h1 += var20;
      this.h2 += var21;
      this.h3 += var22;
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
         (byte)(this.h3 >>> 24)
      };
   }

   protected void resetContext() {
      this.h0 = 1732584193;
      this.h1 = -271733879;
      this.h2 = -1732584194;
      this.h3 = 271733878;
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean("D41D8CD98F00B204E9800998ECF8427E".equals(Util.toString(new MD5().digest())));
      }

      return valid;
   }
}
