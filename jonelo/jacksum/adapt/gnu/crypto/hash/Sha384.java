package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Sha384 extends BaseHash {
   private static final long[] k = new long[]{
      4794697086780616226L,
      8158064640168781261L,
      -5349999486874862801L,
      -1606136188198331460L,
      4131703408338449720L,
      6480981068601479193L,
      -7908458776815382629L,
      -6116909921290321640L,
      -2880145864133508542L,
      1334009975649890238L,
      2608012711638119052L,
      6128411473006802146L,
      8268148722764581231L,
      -9160688886553864527L,
      -7215885187991268811L,
      -4495734319001033068L,
      -1973867731355612462L,
      -1171420211273849373L,
      1135362057144423861L,
      2597628984639134821L,
      3308224258029322869L,
      5365058923640841347L,
      6679025012923562964L,
      8573033837759648693L,
      -7476448914759557205L,
      -6327057829258317296L,
      -5763719355590565569L,
      -4658551843659510044L,
      -4116276920077217854L,
      -3051310485924567259L,
      489312712824947311L,
      1452737877330783856L,
      2861767655752347644L,
      3322285676063803686L,
      5560940570517711597L,
      5996557281743188959L,
      7280758554555802590L,
      8532644243296465576L,
      -9096487096722542874L,
      -7894198246740708037L,
      -6719396339535248540L,
      -6333637450476146687L,
      -4446306890439682159L,
      -4076793802049405392L,
      -3345356375505022440L,
      -2983346525034927856L,
      -860691631967231958L,
      1182934255886127544L,
      1847814050463011016L,
      2177327727835720531L,
      2830643537854262169L,
      3796741975233480872L,
      4115178125766777443L,
      5681478168544905931L,
      6601373596472566643L,
      7507060721942968483L,
      8399075790359081724L,
      8693463985226723168L,
      -8878714635349349518L,
      -8302665154208450068L,
      -8016688836872298968L,
      -6606660893046293015L,
      -4685533653050689259L,
      -4147400797238176981L,
      -3880063495543823972L,
      -3348786107499101689L,
      -1523767162380948706L,
      -757361751448694408L,
      500013540394364858L,
      748580250866718886L,
      1242879168328830382L,
      1977374033974150939L,
      2944078676154940804L,
      3659926193048069267L,
      4368137639120453308L,
      4836135668995329356L,
      5532061633213252278L,
      6448918945643986474L,
      6902733635092675308L,
      7801388544844847127L
   };
   private static final int BLOCK_SIZE = 128;
   private static final String DIGEST0 = "CB00753F45A35E8BB5A03D699AC65007272C32AB0EDED1631A8B605A43FF5BED8086072BA1E7CC2358BAECA134C825A7";
   private static final long[] w = new long[80];
   private static Boolean valid;
   private long h0;
   private long h1;
   private long h2;
   private long h3;
   private long h4;
   private long h5;
   private long h6;
   private long h7;

   public Sha384() {
      super("sha-384", 48, 128);
   }

   private Sha384(Sha384 var1) {
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

   public static final long[] G(long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, byte[] var16, int var17) {
      return sha(var0, var2, var4, var6, var8, var10, var12, var14, var16, var17);
   }

   public Object clone() {
      return new Sha384(this);
   }

   protected void transform(byte[] var1, int var2) {
      long[] var3 = sha(this.h0, this.h1, this.h2, this.h3, this.h4, this.h5, this.h6, this.h7, var1, var2);
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
      int var1 = (int)(this.count % 128L);
      int var2 = var1 < 112 ? 112 - var1 : 240 - var1;
      byte[] var3 = new byte[var2 + 16];
      var3[0] = -128;
      long var4 = this.count << 3;
      var2 += 8;
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
         (byte)((int)(this.h0 >>> 56)),
         (byte)((int)(this.h0 >>> 48)),
         (byte)((int)(this.h0 >>> 40)),
         (byte)((int)(this.h0 >>> 32)),
         (byte)((int)(this.h0 >>> 24)),
         (byte)((int)(this.h0 >>> 16)),
         (byte)((int)(this.h0 >>> 8)),
         (byte)((int)this.h0),
         (byte)((int)(this.h1 >>> 56)),
         (byte)((int)(this.h1 >>> 48)),
         (byte)((int)(this.h1 >>> 40)),
         (byte)((int)(this.h1 >>> 32)),
         (byte)((int)(this.h1 >>> 24)),
         (byte)((int)(this.h1 >>> 16)),
         (byte)((int)(this.h1 >>> 8)),
         (byte)((int)this.h1),
         (byte)((int)(this.h2 >>> 56)),
         (byte)((int)(this.h2 >>> 48)),
         (byte)((int)(this.h2 >>> 40)),
         (byte)((int)(this.h2 >>> 32)),
         (byte)((int)(this.h2 >>> 24)),
         (byte)((int)(this.h2 >>> 16)),
         (byte)((int)(this.h2 >>> 8)),
         (byte)((int)this.h2),
         (byte)((int)(this.h3 >>> 56)),
         (byte)((int)(this.h3 >>> 48)),
         (byte)((int)(this.h3 >>> 40)),
         (byte)((int)(this.h3 >>> 32)),
         (byte)((int)(this.h3 >>> 24)),
         (byte)((int)(this.h3 >>> 16)),
         (byte)((int)(this.h3 >>> 8)),
         (byte)((int)this.h3),
         (byte)((int)(this.h4 >>> 56)),
         (byte)((int)(this.h4 >>> 48)),
         (byte)((int)(this.h4 >>> 40)),
         (byte)((int)(this.h4 >>> 32)),
         (byte)((int)(this.h4 >>> 24)),
         (byte)((int)(this.h4 >>> 16)),
         (byte)((int)(this.h4 >>> 8)),
         (byte)((int)this.h4),
         (byte)((int)(this.h5 >>> 56)),
         (byte)((int)(this.h5 >>> 48)),
         (byte)((int)(this.h5 >>> 40)),
         (byte)((int)(this.h5 >>> 32)),
         (byte)((int)(this.h5 >>> 24)),
         (byte)((int)(this.h5 >>> 16)),
         (byte)((int)(this.h5 >>> 8)),
         (byte)((int)this.h5)
      };
   }

   protected void resetContext() {
      this.h0 = -3766243637369397544L;
      this.h1 = 7105036623409894663L;
      this.h2 = -7973340178411365097L;
      this.h3 = 1526699215303891257L;
      this.h4 = 7436329637833083697L;
      this.h5 = -8163818279084223215L;
      this.h6 = -2662702644619276377L;
      this.h7 = 5167115440072839076L;
   }

   public boolean selfTest() {
      if (valid == null) {
         Sha384 var1 = new Sha384();
         var1.update((byte)97);
         var1.update((byte)98);
         var1.update((byte)99);
         String var2 = Util.toString(var1.digest());
         valid = new Boolean("CB00753F45A35E8BB5A03D699AC65007272C32AB0EDED1631A8B605A43FF5BED8086072BA1E7CC2358BAECA134C825A7".equals(var2));
      }

      return valid;
   }

   private static final synchronized long[] sha(
      long var0, long var2, long var4, long var6, long var8, long var10, long var12, long var14, byte[] var16, int var17
   ) {
      long var18 = var0;
      long var20 = var2;
      long var22 = var4;
      long var24 = var6;
      long var26 = var8;
      long var28 = var10;
      long var30 = var12;
      long var32 = var14;

      for(int var38 = 0; var38 < 16; ++var38) {
         w[var38] = (long)var16[var17++] << 56
            | ((long)var16[var17++] & 255L) << 48
            | ((long)var16[var17++] & 255L) << 40
            | ((long)var16[var17++] & 255L) << 32
            | ((long)var16[var17++] & 255L) << 24
            | ((long)var16[var17++] & 255L) << 16
            | ((long)var16[var17++] & 255L) << 8
            | (long)var16[var17++] & 255L;
      }

      for(int var48 = 16; var48 < 80; ++var48) {
         long var34 = w[var48 - 2];
         long var36 = w[var48 - 15];
         w[var48] = ((var34 >>> 19 | var34 << 45) ^ (var34 >>> 61 | var34 << 3) ^ var34 >>> 6)
            + w[var48 - 7]
            + ((var36 >>> 1 | var36 << 63) ^ (var36 >>> 8 | var36 << 56) ^ var36 >>> 7)
            + w[var48 - 16];
      }

      for(int var49 = 0; var49 < 80; ++var49) {
         long var46 = var32
            + ((var26 >>> 14 | var26 << 50) ^ (var26 >>> 18 | var26 << 46) ^ (var26 >>> 41 | var26 << 23))
            + (var26 & var28 ^ ~var26 & var30)
            + k[var49]
            + w[var49];
         long var47 = ((var18 >>> 28 | var18 << 36) ^ (var18 >>> 34 | var18 << 30) ^ (var18 >>> 39 | var18 << 25))
            + (var18 & var20 ^ var18 & var22 ^ var20 & var22);
         var32 = var30;
         var30 = var28;
         var28 = var26;
         var26 = var24 + var46;
         var24 = var22;
         var22 = var20;
         var20 = var18;
         var18 = var46 + var47;
      }

      return new long[]{var0 + var18, var2 + var20, var4 + var22, var6 + var24, var8 + var26, var10 + var28, var12 + var30, var14 + var32};
   }
}
