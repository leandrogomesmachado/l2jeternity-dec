package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public final class Whirlpool2003 extends BaseHash {
   private static final int BLOCK_SIZE = 64;
   private static final String DIGEST0 = "19FA61D75522A4669B44E39C1D2E1726C530232130D407F89AFEE0964997F7A73E83BE698B288FEBCF88E3E03C4F0757EA8964E59B63D93708B138CC42A66EB3";
   private static final int R = 10;
   private static final String Sd = "ᠣ웨螸ŏ㚦틵祯酒悼鮎ꌌ笵ᷠퟂ\u2e4b﹗ᕷ㟥\u9ff0䫚壉⤊놠殅뵝ჴ쬾է\ue427䆋Ᵹ闘ﯮ籦\udd17䞞쨭뼇굚茳挂ꩱ젙䧙\uf2e3守騦㊰\ue90f햀뻍㑈ｺ遟\u2068\u1aae둔錢擱猒䀈쏬\udba1贽需켫皂혛떯橐䗳ワ㽕ꋪ斺⿀\ude1c\ufd4d鉵ڊ닦ฟ拔ꢖ暈╙葲㥌幸㢌톥\ue261댡鰞䏇ﰄ写洍\ufadf縤㮫츑轎럫㲁铷뤓ⳓ\ue76e쐃噄義⪻셓\udc0b鵬ㅴ\uf646겉ᓡᘺ椉炶탭챂颤⡜\uf886";
   private static final long[] T0 = new long[256];
   private static final long[] T1 = new long[256];
   private static final long[] T2 = new long[256];
   private static final long[] T3 = new long[256];
   private static final long[] T4 = new long[256];
   private static final long[] T5 = new long[256];
   private static final long[] T6 = new long[256];
   private static final long[] T7 = new long[256];
   private static final long[] rc = new long[10];
   private static Boolean valid;
   private long H0;
   private long H1;
   private long H2;
   private long H3;
   private long H4;
   private long H5;
   private long H6;
   private long H7;
   private long k00;
   private long k01;
   private long k02;
   private long k03;
   private long k04;
   private long k05;
   private long k06;
   private long k07;
   private long Kr0;
   private long Kr1;
   private long Kr2;
   private long Kr3;
   private long Kr4;
   private long Kr5;
   private long Kr6;
   private long Kr7;
   private long n0;
   private long n1;
   private long n2;
   private long n3;
   private long n4;
   private long n5;
   private long n6;
   private long n7;
   private long nn0;
   private long nn1;
   private long nn2;
   private long nn3;
   private long nn4;
   private long nn5;
   private long nn6;
   private long nn7;
   private long w0;
   private long w1;
   private long w2;
   private long w3;
   private long w4;
   private long w5;
   private long w6;
   private long w7;

   public Whirlpool2003() {
      super("whirlpool", 20, 64);
   }

   private Whirlpool2003(Whirlpool2003 var1) {
      this();
      this.H0 = var1.H0;
      this.H1 = var1.H1;
      this.H2 = var1.H2;
      this.H3 = var1.H3;
      this.H4 = var1.H4;
      this.H5 = var1.H5;
      this.H6 = var1.H6;
      this.H7 = var1.H7;
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
   }

   public Object clone() {
      return new Whirlpool2003(this);
   }

   protected void transform(byte[] var1, int var2) {
      this.n0 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n1 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n2 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n3 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n4 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n5 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n6 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.n7 = ((long)var1[var2++] & 255L) << 56
         | ((long)var1[var2++] & 255L) << 48
         | ((long)var1[var2++] & 255L) << 40
         | ((long)var1[var2++] & 255L) << 32
         | ((long)var1[var2++] & 255L) << 24
         | ((long)var1[var2++] & 255L) << 16
         | ((long)var1[var2++] & 255L) << 8
         | (long)var1[var2++] & 255L;
      this.k00 = this.H0;
      this.k01 = this.H1;
      this.k02 = this.H2;
      this.k03 = this.H3;
      this.k04 = this.H4;
      this.k05 = this.H5;
      this.k06 = this.H6;
      this.k07 = this.H7;
      this.nn0 = this.n0 ^ this.k00;
      this.nn1 = this.n1 ^ this.k01;
      this.nn2 = this.n2 ^ this.k02;
      this.nn3 = this.n3 ^ this.k03;
      this.nn4 = this.n4 ^ this.k04;
      this.nn5 = this.n5 ^ this.k05;
      this.nn6 = this.n6 ^ this.k06;
      this.nn7 = this.n7 ^ this.k07;
      this.w0 = this.w1 = this.w2 = this.w3 = this.w4 = this.w5 = this.w6 = this.w7 = 0L;

      for(int var3 = 0; var3 < 10; ++var3) {
         this.Kr0 = T0[(int)(this.k00 >> 56 & 255L)]
            ^ T1[(int)(this.k07 >> 48 & 255L)]
            ^ T2[(int)(this.k06 >> 40 & 255L)]
            ^ T3[(int)(this.k05 >> 32 & 255L)]
            ^ T4[(int)(this.k04 >> 24 & 255L)]
            ^ T5[(int)(this.k03 >> 16 & 255L)]
            ^ T6[(int)(this.k02 >> 8 & 255L)]
            ^ T7[(int)(this.k01 & 255L)]
            ^ rc[var3];
         this.Kr1 = T0[(int)(this.k01 >> 56 & 255L)]
            ^ T1[(int)(this.k00 >> 48 & 255L)]
            ^ T2[(int)(this.k07 >> 40 & 255L)]
            ^ T3[(int)(this.k06 >> 32 & 255L)]
            ^ T4[(int)(this.k05 >> 24 & 255L)]
            ^ T5[(int)(this.k04 >> 16 & 255L)]
            ^ T6[(int)(this.k03 >> 8 & 255L)]
            ^ T7[(int)(this.k02 & 255L)];
         this.Kr2 = T0[(int)(this.k02 >> 56 & 255L)]
            ^ T1[(int)(this.k01 >> 48 & 255L)]
            ^ T2[(int)(this.k00 >> 40 & 255L)]
            ^ T3[(int)(this.k07 >> 32 & 255L)]
            ^ T4[(int)(this.k06 >> 24 & 255L)]
            ^ T5[(int)(this.k05 >> 16 & 255L)]
            ^ T6[(int)(this.k04 >> 8 & 255L)]
            ^ T7[(int)(this.k03 & 255L)];
         this.Kr3 = T0[(int)(this.k03 >> 56 & 255L)]
            ^ T1[(int)(this.k02 >> 48 & 255L)]
            ^ T2[(int)(this.k01 >> 40 & 255L)]
            ^ T3[(int)(this.k00 >> 32 & 255L)]
            ^ T4[(int)(this.k07 >> 24 & 255L)]
            ^ T5[(int)(this.k06 >> 16 & 255L)]
            ^ T6[(int)(this.k05 >> 8 & 255L)]
            ^ T7[(int)(this.k04 & 255L)];
         this.Kr4 = T0[(int)(this.k04 >> 56 & 255L)]
            ^ T1[(int)(this.k03 >> 48 & 255L)]
            ^ T2[(int)(this.k02 >> 40 & 255L)]
            ^ T3[(int)(this.k01 >> 32 & 255L)]
            ^ T4[(int)(this.k00 >> 24 & 255L)]
            ^ T5[(int)(this.k07 >> 16 & 255L)]
            ^ T6[(int)(this.k06 >> 8 & 255L)]
            ^ T7[(int)(this.k05 & 255L)];
         this.Kr5 = T0[(int)(this.k05 >> 56 & 255L)]
            ^ T1[(int)(this.k04 >> 48 & 255L)]
            ^ T2[(int)(this.k03 >> 40 & 255L)]
            ^ T3[(int)(this.k02 >> 32 & 255L)]
            ^ T4[(int)(this.k01 >> 24 & 255L)]
            ^ T5[(int)(this.k00 >> 16 & 255L)]
            ^ T6[(int)(this.k07 >> 8 & 255L)]
            ^ T7[(int)(this.k06 & 255L)];
         this.Kr6 = T0[(int)(this.k06 >> 56 & 255L)]
            ^ T1[(int)(this.k05 >> 48 & 255L)]
            ^ T2[(int)(this.k04 >> 40 & 255L)]
            ^ T3[(int)(this.k03 >> 32 & 255L)]
            ^ T4[(int)(this.k02 >> 24 & 255L)]
            ^ T5[(int)(this.k01 >> 16 & 255L)]
            ^ T6[(int)(this.k00 >> 8 & 255L)]
            ^ T7[(int)(this.k07 & 255L)];
         this.Kr7 = T0[(int)(this.k07 >> 56 & 255L)]
            ^ T1[(int)(this.k06 >> 48 & 255L)]
            ^ T2[(int)(this.k05 >> 40 & 255L)]
            ^ T3[(int)(this.k04 >> 32 & 255L)]
            ^ T4[(int)(this.k03 >> 24 & 255L)]
            ^ T5[(int)(this.k02 >> 16 & 255L)]
            ^ T6[(int)(this.k01 >> 8 & 255L)]
            ^ T7[(int)(this.k00 & 255L)];
         this.k00 = this.Kr0;
         this.k01 = this.Kr1;
         this.k02 = this.Kr2;
         this.k03 = this.Kr3;
         this.k04 = this.Kr4;
         this.k05 = this.Kr5;
         this.k06 = this.Kr6;
         this.k07 = this.Kr7;
         this.w0 = T0[(int)(this.nn0 >> 56 & 255L)]
            ^ T1[(int)(this.nn7 >> 48 & 255L)]
            ^ T2[(int)(this.nn6 >> 40 & 255L)]
            ^ T3[(int)(this.nn5 >> 32 & 255L)]
            ^ T4[(int)(this.nn4 >> 24 & 255L)]
            ^ T5[(int)(this.nn3 >> 16 & 255L)]
            ^ T6[(int)(this.nn2 >> 8 & 255L)]
            ^ T7[(int)(this.nn1 & 255L)]
            ^ this.Kr0;
         this.w1 = T0[(int)(this.nn1 >> 56 & 255L)]
            ^ T1[(int)(this.nn0 >> 48 & 255L)]
            ^ T2[(int)(this.nn7 >> 40 & 255L)]
            ^ T3[(int)(this.nn6 >> 32 & 255L)]
            ^ T4[(int)(this.nn5 >> 24 & 255L)]
            ^ T5[(int)(this.nn4 >> 16 & 255L)]
            ^ T6[(int)(this.nn3 >> 8 & 255L)]
            ^ T7[(int)(this.nn2 & 255L)]
            ^ this.Kr1;
         this.w2 = T0[(int)(this.nn2 >> 56 & 255L)]
            ^ T1[(int)(this.nn1 >> 48 & 255L)]
            ^ T2[(int)(this.nn0 >> 40 & 255L)]
            ^ T3[(int)(this.nn7 >> 32 & 255L)]
            ^ T4[(int)(this.nn6 >> 24 & 255L)]
            ^ T5[(int)(this.nn5 >> 16 & 255L)]
            ^ T6[(int)(this.nn4 >> 8 & 255L)]
            ^ T7[(int)(this.nn3 & 255L)]
            ^ this.Kr2;
         this.w3 = T0[(int)(this.nn3 >> 56 & 255L)]
            ^ T1[(int)(this.nn2 >> 48 & 255L)]
            ^ T2[(int)(this.nn1 >> 40 & 255L)]
            ^ T3[(int)(this.nn0 >> 32 & 255L)]
            ^ T4[(int)(this.nn7 >> 24 & 255L)]
            ^ T5[(int)(this.nn6 >> 16 & 255L)]
            ^ T6[(int)(this.nn5 >> 8 & 255L)]
            ^ T7[(int)(this.nn4 & 255L)]
            ^ this.Kr3;
         this.w4 = T0[(int)(this.nn4 >> 56 & 255L)]
            ^ T1[(int)(this.nn3 >> 48 & 255L)]
            ^ T2[(int)(this.nn2 >> 40 & 255L)]
            ^ T3[(int)(this.nn1 >> 32 & 255L)]
            ^ T4[(int)(this.nn0 >> 24 & 255L)]
            ^ T5[(int)(this.nn7 >> 16 & 255L)]
            ^ T6[(int)(this.nn6 >> 8 & 255L)]
            ^ T7[(int)(this.nn5 & 255L)]
            ^ this.Kr4;
         this.w5 = T0[(int)(this.nn5 >> 56 & 255L)]
            ^ T1[(int)(this.nn4 >> 48 & 255L)]
            ^ T2[(int)(this.nn3 >> 40 & 255L)]
            ^ T3[(int)(this.nn2 >> 32 & 255L)]
            ^ T4[(int)(this.nn1 >> 24 & 255L)]
            ^ T5[(int)(this.nn0 >> 16 & 255L)]
            ^ T6[(int)(this.nn7 >> 8 & 255L)]
            ^ T7[(int)(this.nn6 & 255L)]
            ^ this.Kr5;
         this.w6 = T0[(int)(this.nn6 >> 56 & 255L)]
            ^ T1[(int)(this.nn5 >> 48 & 255L)]
            ^ T2[(int)(this.nn4 >> 40 & 255L)]
            ^ T3[(int)(this.nn3 >> 32 & 255L)]
            ^ T4[(int)(this.nn2 >> 24 & 255L)]
            ^ T5[(int)(this.nn1 >> 16 & 255L)]
            ^ T6[(int)(this.nn0 >> 8 & 255L)]
            ^ T7[(int)(this.nn7 & 255L)]
            ^ this.Kr6;
         this.w7 = T0[(int)(this.nn7 >> 56 & 255L)]
            ^ T1[(int)(this.nn6 >> 48 & 255L)]
            ^ T2[(int)(this.nn5 >> 40 & 255L)]
            ^ T3[(int)(this.nn4 >> 32 & 255L)]
            ^ T4[(int)(this.nn3 >> 24 & 255L)]
            ^ T5[(int)(this.nn2 >> 16 & 255L)]
            ^ T6[(int)(this.nn1 >> 8 & 255L)]
            ^ T7[(int)(this.nn0 & 255L)]
            ^ this.Kr7;
         this.nn0 = this.w0;
         this.nn1 = this.w1;
         this.nn2 = this.w2;
         this.nn3 = this.w3;
         this.nn4 = this.w4;
         this.nn5 = this.w5;
         this.nn6 = this.w6;
         this.nn7 = this.w7;
      }

      this.H0 ^= this.w0 ^ this.n0;
      this.H1 ^= this.w1 ^ this.n1;
      this.H2 ^= this.w2 ^ this.n2;
      this.H3 ^= this.w3 ^ this.n3;
      this.H4 ^= this.w4 ^ this.n4;
      this.H5 ^= this.w5 ^ this.n5;
      this.H6 ^= this.w6 ^ this.n6;
      this.H7 ^= this.w7 ^ this.n7;
   }

   protected byte[] padBuffer() {
      int var1 = (int)((this.count + 33L) % 64L);
      int var2 = var1 == 0 ? 33 : 64 - var1 + 33;
      byte[] var3 = new byte[var2];
      var3[0] = -128;
      long var4 = this.count * 8L;
      int var6 = var2 - 8;
      var3[var6++] = (byte)((int)(var4 >>> 56));
      var3[var6++] = (byte)((int)(var4 >>> 48));
      var3[var6++] = (byte)((int)(var4 >>> 40));
      var3[var6++] = (byte)((int)(var4 >>> 32));
      var3[var6++] = (byte)((int)(var4 >>> 24));
      var3[var6++] = (byte)((int)(var4 >>> 16));
      var3[var6++] = (byte)((int)(var4 >>> 8));
      var3[var6] = (byte)((int)var4);
      return var3;
   }

   protected byte[] getResult() {
      return new byte[]{
         (byte)((int)(this.H0 >>> 56)),
         (byte)((int)(this.H0 >>> 48)),
         (byte)((int)(this.H0 >>> 40)),
         (byte)((int)(this.H0 >>> 32)),
         (byte)((int)(this.H0 >>> 24)),
         (byte)((int)(this.H0 >>> 16)),
         (byte)((int)(this.H0 >>> 8)),
         (byte)((int)this.H0),
         (byte)((int)(this.H1 >>> 56)),
         (byte)((int)(this.H1 >>> 48)),
         (byte)((int)(this.H1 >>> 40)),
         (byte)((int)(this.H1 >>> 32)),
         (byte)((int)(this.H1 >>> 24)),
         (byte)((int)(this.H1 >>> 16)),
         (byte)((int)(this.H1 >>> 8)),
         (byte)((int)this.H1),
         (byte)((int)(this.H2 >>> 56)),
         (byte)((int)(this.H2 >>> 48)),
         (byte)((int)(this.H2 >>> 40)),
         (byte)((int)(this.H2 >>> 32)),
         (byte)((int)(this.H2 >>> 24)),
         (byte)((int)(this.H2 >>> 16)),
         (byte)((int)(this.H2 >>> 8)),
         (byte)((int)this.H2),
         (byte)((int)(this.H3 >>> 56)),
         (byte)((int)(this.H3 >>> 48)),
         (byte)((int)(this.H3 >>> 40)),
         (byte)((int)(this.H3 >>> 32)),
         (byte)((int)(this.H3 >>> 24)),
         (byte)((int)(this.H3 >>> 16)),
         (byte)((int)(this.H3 >>> 8)),
         (byte)((int)this.H3),
         (byte)((int)(this.H4 >>> 56)),
         (byte)((int)(this.H4 >>> 48)),
         (byte)((int)(this.H4 >>> 40)),
         (byte)((int)(this.H4 >>> 32)),
         (byte)((int)(this.H4 >>> 24)),
         (byte)((int)(this.H4 >>> 16)),
         (byte)((int)(this.H4 >>> 8)),
         (byte)((int)this.H4),
         (byte)((int)(this.H5 >>> 56)),
         (byte)((int)(this.H5 >>> 48)),
         (byte)((int)(this.H5 >>> 40)),
         (byte)((int)(this.H5 >>> 32)),
         (byte)((int)(this.H5 >>> 24)),
         (byte)((int)(this.H5 >>> 16)),
         (byte)((int)(this.H5 >>> 8)),
         (byte)((int)this.H5),
         (byte)((int)(this.H6 >>> 56)),
         (byte)((int)(this.H6 >>> 48)),
         (byte)((int)(this.H6 >>> 40)),
         (byte)((int)(this.H6 >>> 32)),
         (byte)((int)(this.H6 >>> 24)),
         (byte)((int)(this.H6 >>> 16)),
         (byte)((int)(this.H6 >>> 8)),
         (byte)((int)this.H6),
         (byte)((int)(this.H7 >>> 56)),
         (byte)((int)(this.H7 >>> 48)),
         (byte)((int)(this.H7 >>> 40)),
         (byte)((int)(this.H7 >>> 32)),
         (byte)((int)(this.H7 >>> 24)),
         (byte)((int)(this.H7 >>> 16)),
         (byte)((int)(this.H7 >>> 8)),
         (byte)((int)this.H7)
      };
   }

   protected void resetContext() {
      this.H0 = this.H1 = this.H2 = this.H3 = this.H4 = this.H5 = this.H6 = this.H7 = 0L;
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean(
            "19FA61D75522A4669B44E39C1D2E1726C530232130D407F89AFEE0964997F7A73E83BE698B288FEBCF88E3E03C4F0757EA8964E59B63D93708B138CC42A66EB3"
               .equals(Util.toString(new Whirlpool2003().digest()))
         );
      }

      return valid;
   }

   static {
      short var0 = 285;
      byte[] var21 = new byte[256];

      for(int var1 = 0; var1 < 256; ++var1) {
         char var20 = "ᠣ웨螸ŏ㚦틵祯酒悼鮎ꌌ笵ᷠퟂ\u2e4b﹗ᕷ㟥\u9ff0䫚壉⤊놠殅뵝ჴ쬾է\ue427䆋Ᵹ闘ﯮ籦\udd17䞞쨭뼇굚茳挂ꩱ젙䧙\uf2e3守騦㊰\ue90f햀뻍㑈ｺ遟\u2068\u1aae둔錢擱猒䀈쏬\udba1贽需켫皂혛떯橐䗳ワ㽕ꋪ斺⿀\ude1c\ufd4d鉵ڊ닦ฟ拔ꢖ暈╙葲㥌幸㢌톥\ue261댡鰞䏇ﰄ写洍\ufadf縤㮫츑轎럫㲁铷뤓ⳓ\ue76e쐃噄義⪻셓\udc0b鵬ㅴ\uf646겉ᓡᘺ椉炶탭챂颤⡜\uf886"
            .charAt(var1 >>> 1);
         long var4 = (long)((var1 & 1) == 0 ? var20 >>> '\b' : var20) & 255L;
         long var6 = var4 << 1;
         if (var6 > 255L) {
            var6 ^= (long)var0;
         }

         long var8 = var6 ^ var4;
         long var10 = var6 << 1;
         if (var10 > 255L) {
            var10 ^= (long)var0;
         }

         long var12 = var10 ^ var4;
         long var14 = var10 << 1;
         if (var14 > 255L) {
            var14 ^= (long)var0;
         }

         long var16 = var14 ^ var4;
         var21[var1] = (byte)((int)var4);
         long var18;
         T0[var1] = var18 = var4 << 56 | var4 << 48 | var10 << 40 | var4 << 32 | var14 << 24 | var12 << 16 | var6 << 8 | var16;
         T1[var1] = var18 >>> 8 | var18 << 56;
         T2[var1] = var18 >>> 16 | var18 << 48;
         T3[var1] = var18 >>> 24 | var18 << 40;
         T4[var1] = var18 >>> 32 | var18 << 32;
         T5[var1] = var18 >>> 40 | var18 << 24;
         T6[var1] = var18 >>> 48 | var18 << 16;
         T7[var1] = var18 >>> 56 | var18 << 8;
      }

      int var2 = 1;
      int var22 = 0;

      for(int var3 = 0; var2 < 11; ++var2) {
         rc[var22++] = ((long)var21[var3++] & 255L) << 56
            | ((long)var21[var3++] & 255L) << 48
            | ((long)var21[var3++] & 255L) << 40
            | ((long)var21[var3++] & 255L) << 32
            | ((long)var21[var3++] & 255L) << 24
            | ((long)var21[var3++] & 255L) << 16
            | ((long)var21[var3++] & 255L) << 8
            | (long)var21[var3++] & 255L;
      }
   }
}
