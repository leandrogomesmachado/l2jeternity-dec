package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Haval extends BaseHash {
   public static final int HAVAL_VERSION = 1;
   public static final int HAVAL_128_BIT = 16;
   public static final int HAVAL_160_BIT = 20;
   public static final int HAVAL_192_BIT = 24;
   public static final int HAVAL_224_BIT = 28;
   public static final int HAVAL_256_BIT = 32;
   public static final int HAVAL_3_ROUND = 3;
   public static final int HAVAL_4_ROUND = 4;
   public static final int HAVAL_5_ROUND = 5;
   private static final int BLOCK_SIZE = 128;
   private static final String DIGEST0 = "C68F39913F901F3DDF44C707357A7D70";
   private static Boolean valid;
   private int rounds = 3;
   private int h0;
   private int h1;
   private int h2;
   private int h3;
   private int h4;
   private int h5;
   private int h6;
   private int h7;

   public Haval() {
      this(16, 3);
   }

   public Haval(int var1) {
      this(var1, 3);
   }

   public Haval(int var1, int var2) {
      super("haval", var1, 128);
      if (var1 != 16 && var1 != 20 && var1 != 24 && var1 != 28 && var1 != 32) {
         throw new IllegalArgumentException("Invalid HAVAL output size");
      } else if (var2 != 3 && var2 != 4 && var2 != 5) {
         throw new IllegalArgumentException("Invalid HAVAL number of rounds");
      } else {
         this.rounds = var2;
      }
   }

   private Haval(Haval var1) {
      this(var1.hashSize, var1.rounds);
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

   public Object clone() {
      return new Haval(this);
   }

   protected synchronized void transform(byte[] var1, int var2) {
      int var3 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var4 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var5 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var6 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var7 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var8 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var9 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var10 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var11 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var12 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var13 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var14 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var15 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var16 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var17 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var18 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var19 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var20 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var21 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var22 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var23 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var24 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var25 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var26 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var27 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var28 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var29 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var30 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var31 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var32 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var33 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var34 = var1[var2++] & 255 | (var1[var2++] & 255) << 8 | (var1[var2++] & 255) << 16 | (var1[var2++] & 255) << 24;
      int var35 = this.h0;
      int var36 = this.h1;
      int var37 = this.h2;
      int var38 = this.h3;
      int var39 = this.h4;
      int var40 = this.h5;
      int var41 = this.h6;
      int var42 = this.h7;
      var42 = this.FF1(var42, var41, var40, var39, var38, var37, var36, var35, var3);
      var41 = this.FF1(var41, var40, var39, var38, var37, var36, var35, var42, var4);
      var40 = this.FF1(var40, var39, var38, var37, var36, var35, var42, var41, var5);
      var39 = this.FF1(var39, var38, var37, var36, var35, var42, var41, var40, var6);
      var38 = this.FF1(var38, var37, var36, var35, var42, var41, var40, var39, var7);
      var37 = this.FF1(var37, var36, var35, var42, var41, var40, var39, var38, var8);
      var36 = this.FF1(var36, var35, var42, var41, var40, var39, var38, var37, var9);
      var35 = this.FF1(var35, var42, var41, var40, var39, var38, var37, var36, var10);
      var42 = this.FF1(var42, var41, var40, var39, var38, var37, var36, var35, var11);
      var41 = this.FF1(var41, var40, var39, var38, var37, var36, var35, var42, var12);
      var40 = this.FF1(var40, var39, var38, var37, var36, var35, var42, var41, var13);
      var39 = this.FF1(var39, var38, var37, var36, var35, var42, var41, var40, var14);
      var38 = this.FF1(var38, var37, var36, var35, var42, var41, var40, var39, var15);
      var37 = this.FF1(var37, var36, var35, var42, var41, var40, var39, var38, var16);
      var36 = this.FF1(var36, var35, var42, var41, var40, var39, var38, var37, var17);
      var35 = this.FF1(var35, var42, var41, var40, var39, var38, var37, var36, var18);
      var42 = this.FF1(var42, var41, var40, var39, var38, var37, var36, var35, var19);
      var41 = this.FF1(var41, var40, var39, var38, var37, var36, var35, var42, var20);
      var40 = this.FF1(var40, var39, var38, var37, var36, var35, var42, var41, var21);
      var39 = this.FF1(var39, var38, var37, var36, var35, var42, var41, var40, var22);
      var38 = this.FF1(var38, var37, var36, var35, var42, var41, var40, var39, var23);
      var37 = this.FF1(var37, var36, var35, var42, var41, var40, var39, var38, var24);
      var36 = this.FF1(var36, var35, var42, var41, var40, var39, var38, var37, var25);
      var35 = this.FF1(var35, var42, var41, var40, var39, var38, var37, var36, var26);
      var42 = this.FF1(var42, var41, var40, var39, var38, var37, var36, var35, var27);
      var41 = this.FF1(var41, var40, var39, var38, var37, var36, var35, var42, var28);
      var40 = this.FF1(var40, var39, var38, var37, var36, var35, var42, var41, var29);
      var39 = this.FF1(var39, var38, var37, var36, var35, var42, var41, var40, var30);
      var38 = this.FF1(var38, var37, var36, var35, var42, var41, var40, var39, var31);
      var37 = this.FF1(var37, var36, var35, var42, var41, var40, var39, var38, var32);
      var36 = this.FF1(var36, var35, var42, var41, var40, var39, var38, var37, var33);
      var35 = this.FF1(var35, var42, var41, var40, var39, var38, var37, var36, var34);
      var42 = this.FF2(var42, var41, var40, var39, var38, var37, var36, var35, var8, 1160258022);
      var41 = this.FF2(var41, var40, var39, var38, var37, var36, var35, var42, var17, 953160567);
      var40 = this.FF2(var40, var39, var38, var37, var36, var35, var42, var41, var29, -1101764913);
      var39 = this.FF2(var39, var38, var37, var36, var35, var42, var41, var40, var21, 887688300);
      var38 = this.FF2(var38, var37, var36, var35, var42, var41, var40, var39, var14, -1062458953);
      var37 = this.FF2(var37, var36, var35, var42, var41, var40, var39, var38, var31, -914599715);
      var36 = this.FF2(var36, var35, var42, var41, var40, var39, var38, var37, var10, 1065670069);
      var35 = this.FF2(var35, var42, var41, var40, var39, var38, var37, var36, var19, -1253635817);
      var42 = this.FF2(var42, var41, var40, var39, var38, var37, var36, var35, var3, -1843997223);
      var41 = this.FF2(var41, var40, var39, var38, var37, var36, var35, var42, var26, -1988494565);
      var40 = this.FF2(var40, var39, var38, var37, var36, var35, var42, var41, var23, -785314906);
      var39 = this.FF2(var39, var38, var37, var36, var35, var42, var41, var40, var25, -1730169428);
      var38 = this.FF2(var38, var37, var36, var35, var42, var41, var40, var39, var4, 805139163);
      var37 = this.FF2(var37, var36, var35, var42, var41, var40, var39, var38, var13, -803545161);
      var36 = this.FF2(var36, var35, var42, var41, var40, var39, var38, var37, var7, -1193168915);
      var35 = this.FF2(var35, var42, var41, var40, var39, var38, var37, var36, var11, 1780907670);
      var42 = this.FF2(var42, var41, var40, var39, var38, var37, var36, var35, var33, -1166241723);
      var41 = this.FF2(var41, var40, var39, var38, var37, var36, var35, var42, var6, -248741991);
      var40 = this.FF2(var40, var39, var38, var37, var36, var35, var42, var41, var24, 614570311);
      var39 = this.FF2(var39, var38, var37, var36, var35, var42, var41, var40, var12, -1282315017);
      var38 = this.FF2(var38, var37, var36, var35, var42, var41, var40, var39, var20, 134345442);
      var37 = this.FF2(var37, var36, var35, var42, var41, var40, var39, var38, var27, -2054226922);
      var36 = this.FF2(var36, var35, var42, var41, var40, var39, var38, var37, var32, 1667834072);
      var35 = this.FF2(var35, var42, var41, var40, var39, var38, var37, var36, var9, 1901547113);
      var42 = this.FF2(var42, var41, var40, var39, var38, var37, var36, var35, var22, -1537671517);
      var41 = this.FF2(var41, var40, var39, var38, var37, var36, var35, var42, var15, -191677058);
      var40 = this.FF2(var40, var39, var38, var37, var36, var35, var42, var41, var18, 227898511);
      var39 = this.FF2(var39, var38, var37, var36, var35, var42, var41, var40, var16, 1921955416);
      var38 = this.FF2(var38, var37, var36, var35, var42, var41, var40, var39, var5, 1904987480);
      var37 = this.FF2(var37, var36, var35, var42, var41, var40, var39, var38, var28, -2112533778);
      var36 = this.FF2(var36, var35, var42, var41, var40, var39, var38, var37, var34, 2069144605);
      var35 = this.FF2(var35, var42, var41, var40, var39, var38, var37, var36, var30, -1034266187);
      var42 = this.FF3(var42, var41, var40, var39, var38, var37, var36, var35, var22, -1674521287);
      var41 = this.FF3(var41, var40, var39, var38, var37, var36, var35, var42, var12, 720527379);
      var40 = this.FF3(var40, var39, var38, var37, var36, var35, var42, var41, var7, -976113629);
      var39 = this.FF3(var39, var38, var37, var36, var35, var42, var41, var40, var23, 677414384);
      var38 = this.FF3(var38, var37, var36, var35, var42, var41, var40, var39, var31, -901678824);
      var37 = this.FF3(var37, var36, var35, var42, var41, var40, var39, var38, var20, -1193592593);
      var36 = this.FF3(var36, var35, var42, var41, var40, var39, var38, var37, var11, -1904616272);
      var35 = this.FF3(var35, var42, var41, var40, var39, var38, var37, var36, var25, 1614419982);
      var42 = this.FF3(var42, var41, var40, var39, var38, var37, var36, var35, var32, 1822297739);
      var41 = this.FF3(var41, var40, var39, var38, var37, var36, var35, var42, var17, -1340175810);
      var40 = this.FF3(var40, var39, var38, var37, var36, var35, var42, var41, var28, -686458943);
      var39 = this.FF3(var39, var38, var37, var36, var35, var42, var41, var40, var15, -1120842969);
      var38 = this.FF3(var38, var37, var36, var35, var42, var41, var40, var39, var27, 2024746970);
      var37 = this.FF3(var37, var36, var35, var42, var41, var40, var39, var38, var33, 1432378464);
      var36 = this.FF3(var36, var35, var42, var41, var40, var39, var38, var37, var19, -430627341);
      var35 = this.FF3(var35, var42, var41, var40, var39, var38, var37, var36, var29, -1437226092);
      var42 = this.FF3(var42, var41, var40, var39, var38, var37, var36, var35, var34, 1464375394);
      var41 = this.FF3(var41, var40, var39, var38, var37, var36, var35, var42, var18, 1676153920);
      var40 = this.FF3(var40, var39, var38, var37, var36, var35, var42, var41, var10, 1439316330);
      var39 = this.FF3(var39, var38, var37, var36, var35, var42, var41, var40, var6, 715854006);
      var38 = this.FF3(var38, var37, var36, var35, var42, var41, var40, var39, var4, -1261675468);
      var37 = this.FF3(var37, var36, var35, var42, var41, var40, var39, var38, var3, 289532110);
      var36 = this.FF3(var36, var35, var42, var41, var40, var39, var38, var37, var21, -1588296017);
      var35 = this.FF3(var35, var42, var41, var40, var39, var38, var37, var36, var30, 2087905683);
      var42 = this.FF3(var42, var41, var40, var39, var38, var37, var36, var35, var16, -1276242927);
      var41 = this.FF3(var41, var40, var39, var38, var37, var36, var35, var42, var9, 1668267050);
      var40 = this.FF3(var40, var39, var38, var37, var36, var35, var42, var41, var24, 732546397);
      var39 = this.FF3(var39, var38, var37, var36, var35, var42, var41, var40, var13, 1947742710);
      var38 = this.FF3(var38, var37, var36, var35, var42, var41, var40, var39, var26, -832815594);
      var37 = this.FF3(var37, var36, var35, var42, var41, var40, var39, var38, var14, -1685613794);
      var36 = this.FF3(var36, var35, var42, var41, var40, var39, var38, var37, var8, -1344882125);
      var35 = this.FF3(var35, var42, var41, var40, var39, var38, var37, var36, var5, 1814351708);
      if (this.rounds >= 4) {
         int var309 = this.FF4(var42, var41, var40, var39, var38, var37, var36, var35, var27, 2050118529);
         int var291 = this.FF4(var41, var40, var39, var38, var37, var36, var35, var309, var7, 680887927);
         int var273 = this.FF4(var40, var39, var38, var37, var36, var35, var309, var291, var3, 999245976);
         int var255 = this.FF4(var39, var38, var37, var36, var35, var309, var291, var273, var17, 1800124847);
         int var237 = this.FF4(var38, var37, var36, var35, var309, var291, var273, var255, var5, -994056165);
         int var219 = this.FF4(var37, var36, var35, var309, var291, var273, var255, var237, var10, 1713906067);
         int var201 = this.FF4(var36, var35, var309, var291, var273, var255, var237, var219, var31, 1641548236);
         var35 = this.FF4(var35, var309, var291, var273, var255, var237, var219, var201, var26, -81679983);
         int var310 = this.FF4(var309, var291, var273, var255, var237, var219, var201, var35, var29, 1216130144);
         int var292 = this.FF4(var291, var273, var255, var237, var219, var201, var35, var310, var9, 1575780402);
         int var274 = this.FF4(var273, var255, var237, var219, var201, var35, var310, var292, var33, -276538019);
         int var256 = this.FF4(var255, var237, var219, var201, var35, var310, var292, var274, var23, -377129551);
         int var238 = this.FF4(var237, var219, var201, var35, var310, var292, var274, var256, var21, -601480446);
         int var220 = this.FF4(var219, var201, var35, var310, var292, var274, var256, var238, var28, -345695352);
         int var202 = this.FF4(var201, var35, var310, var292, var274, var256, var238, var220, var22, 596196993);
         var35 = this.FF4(var35, var310, var292, var274, var256, var238, var220, var202, var6, -745100091);
         int var311 = this.FF4(var310, var292, var274, var256, var238, var220, var202, var35, var25, 258830323);
         int var293 = this.FF4(var292, var274, var256, var238, var220, var202, var35, var311, var14, -2081144263);
         int var275 = this.FF4(var274, var256, var238, var220, var202, var35, var311, var293, var34, 772490370);
         int var257 = this.FF4(var256, var238, var220, var202, var35, var311, var293, var275, var24, -1534844924);
         int var239 = this.FF4(var238, var220, var202, var35, var311, var293, var275, var257, var11, 1774776394);
         int var221 = this.FF4(var220, var202, var35, var311, var293, var275, var257, var239, var30, -1642095778);
         int var203 = this.FF4(var202, var35, var311, var293, var275, var257, var239, var221, var15, 566650946);
         var35 = this.FF4(var35, var311, var293, var275, var257, var239, var221, var203, var12, -152474470);
         var42 = this.FF4(var311, var293, var275, var257, var239, var221, var203, var35, var4, 1728879713);
         var41 = this.FF4(var293, var275, var257, var239, var221, var203, var35, var42, var32, -1412200208);
         var40 = this.FF4(var275, var257, var239, var221, var203, var35, var42, var41, var8, 1783734482);
         var39 = this.FF4(var257, var239, var221, var203, var35, var42, var41, var40, var18, -665571480);
         var38 = this.FF4(var239, var221, var203, var35, var42, var41, var40, var39, var20, -1777359064);
         var37 = this.FF4(var221, var203, var35, var42, var41, var40, var39, var38, var13, -1420741725);
         var36 = this.FF4(var203, var35, var42, var41, var40, var39, var38, var37, var19, 1861159788);
         var35 = this.FF4(var35, var42, var41, var40, var39, var38, var37, var36, var16, 326777828);
         if (this.rounds == 5) {
            int var312 = this.FF5(var42, var41, var40, var39, var38, var37, var36, var35, var30, -1170476976);
            int var294 = this.FF5(var41, var40, var39, var38, var37, var36, var35, var312, var6, 2130389656);
            int var276 = this.FF5(var40, var39, var38, var37, var36, var35, var312, var294, var24, -1578015459);
            int var258 = this.FF5(var39, var38, var37, var36, var35, var312, var294, var276, var29, 967770486);
            int var240 = this.FF5(var38, var37, var36, var35, var312, var294, var276, var258, var20, 1724537150);
            int var222 = this.FF5(var37, var36, var35, var312, var294, var276, var258, var240, var14, -2109534584);
            int var204 = this.FF5(var36, var35, var312, var294, var276, var258, var240, var222, var23, -1930525159);
            var35 = this.FF5(var35, var312, var294, var276, var258, var240, var222, var204, var32, 1164943284);
            int var313 = this.FF5(var312, var294, var276, var258, var240, var222, var204, var35, var22, 2105845187);
            int var295 = this.FF5(var294, var276, var258, var240, var222, var204, var35, var313, var3, 998989502);
            int var277 = this.FF5(var276, var258, var240, var222, var204, var35, var313, var295, var15, -529566248);
            int var259 = this.FF5(var258, var240, var222, var204, var35, var313, var295, var277, var10, -2050940813);
            int var241 = this.FF5(var240, var222, var204, var35, var313, var295, var277, var259, var16, 1075463327);
            int var223 = this.FF5(var222, var204, var35, var313, var295, var277, var259, var241, var11, 1455516326);
            int var205 = this.FF5(var204, var35, var313, var295, var277, var259, var241, var223, var34, 1322494562);
            var35 = this.FF5(var35, var313, var295, var277, var259, var241, var223, var205, var13, 910128902);
            int var314 = this.FF5(var313, var295, var277, var259, var241, var223, var205, var35, var8, 469688178);
            int var296 = this.FF5(var295, var277, var259, var241, var223, var205, var35, var314, var12, 1117454909);
            int var278 = this.FF5(var277, var259, var241, var223, var205, var35, var314, var296, var17, 936433444);
            int var260 = this.FF5(var259, var241, var223, var205, var35, var314, var296, var278, var33, -804646328);
            int var242 = this.FF5(var241, var223, var205, var35, var314, var296, var278, var260, var21, -619713837);
            int var224 = this.FF5(var223, var205, var35, var314, var296, var278, var260, var242, var9, 1240580251);
            int var206 = this.FF5(var205, var35, var314, var296, var278, var260, var242, var224, var31, 122909385);
            var35 = this.FF5(var35, var314, var296, var278, var260, var242, var224, var206, var27, -2137449605);
            var42 = this.FF5(var314, var296, var278, var260, var242, var224, var206, var35, var5, 634681816);
            var41 = this.FF5(var296, var278, var260, var242, var224, var206, var35, var42, var26, -152510729);
            var40 = this.FF5(var278, var260, var242, var224, var206, var35, var42, var41, var19, -469872614);
            var39 = this.FF5(var260, var242, var224, var206, var35, var42, var41, var40, var25, -1233564613);
            var38 = this.FF5(var242, var224, var206, var35, var42, var41, var40, var39, var7, -1754472259);
            var37 = this.FF5(var224, var206, var35, var42, var41, var40, var39, var38, var4, 79693498);
            var36 = this.FF5(var206, var35, var42, var41, var40, var39, var38, var37, var28, -1045868618);
            var35 = this.FF5(var35, var42, var41, var40, var39, var38, var37, var36, var18, 1084186820);
         }
      }

      this.h7 += var42;
      this.h6 += var41;
      this.h5 += var40;
      this.h4 += var39;
      this.h3 += var38;
      this.h2 += var37;
      this.h1 += var36;
      this.h0 += var35;
   }

   protected byte[] padBuffer() {
      int var1 = (int)(this.count % 128L);
      int var2 = var1 < 118 ? 118 - var1 : 246 - var1;
      byte[] var3 = new byte[var2 + 10];
      var3[0] = 1;
      int var4 = this.hashSize * 8;
      var3[var2++] = (byte)((var4 & 3) << 6 | (this.rounds & 7) << 3 | 1);
      var3[var2++] = (byte)(var4 >>> 2);
      long var5 = this.count << 3;
      var3[var2++] = (byte)((int)var5);
      var3[var2++] = (byte)((int)(var5 >>> 8));
      var3[var2++] = (byte)((int)(var5 >>> 16));
      var3[var2++] = (byte)((int)(var5 >>> 24));
      var3[var2++] = (byte)((int)(var5 >>> 32));
      var3[var2++] = (byte)((int)(var5 >>> 40));
      var3[var2++] = (byte)((int)(var5 >>> 48));
      var3[var2] = (byte)((int)(var5 >>> 56));
      return var3;
   }

   protected byte[] getResult() {
      this.tailorDigestBits();
      byte[] var1 = new byte[this.hashSize];
      if (this.hashSize >= 32) {
         var1[31] = (byte)(this.h7 >>> 24);
         var1[30] = (byte)(this.h7 >>> 16);
         var1[29] = (byte)(this.h7 >>> 8);
         var1[28] = (byte)this.h7;
      }

      if (this.hashSize >= 28) {
         var1[27] = (byte)(this.h6 >>> 24);
         var1[26] = (byte)(this.h6 >>> 16);
         var1[25] = (byte)(this.h6 >>> 8);
         var1[24] = (byte)this.h6;
      }

      if (this.hashSize >= 24) {
         var1[23] = (byte)(this.h5 >>> 24);
         var1[22] = (byte)(this.h5 >>> 16);
         var1[21] = (byte)(this.h5 >>> 8);
         var1[20] = (byte)this.h5;
      }

      if (this.hashSize >= 20) {
         var1[19] = (byte)(this.h4 >>> 24);
         var1[18] = (byte)(this.h4 >>> 16);
         var1[17] = (byte)(this.h4 >>> 8);
         var1[16] = (byte)this.h4;
      }

      var1[15] = (byte)(this.h3 >>> 24);
      var1[14] = (byte)(this.h3 >>> 16);
      var1[13] = (byte)(this.h3 >>> 8);
      var1[12] = (byte)this.h3;
      var1[11] = (byte)(this.h2 >>> 24);
      var1[10] = (byte)(this.h2 >>> 16);
      var1[9] = (byte)(this.h2 >>> 8);
      var1[8] = (byte)this.h2;
      var1[7] = (byte)(this.h1 >>> 24);
      var1[6] = (byte)(this.h1 >>> 16);
      var1[5] = (byte)(this.h1 >>> 8);
      var1[4] = (byte)this.h1;
      var1[3] = (byte)(this.h0 >>> 24);
      var1[2] = (byte)(this.h0 >>> 16);
      var1[1] = (byte)(this.h0 >>> 8);
      var1[0] = (byte)this.h0;
      return var1;
   }

   protected void resetContext() {
      this.h0 = 608135816;
      this.h1 = -2052912941;
      this.h2 = 320440878;
      this.h3 = 57701188;
      this.h4 = -1542899678;
      this.h5 = 698298832;
      this.h6 = 137296536;
      this.h7 = -330404727;
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean("C68F39913F901F3DDF44C707357A7D70".equals(Util.toString(new Haval().digest())));
      }

      return valid;
   }

   private void tailorDigestBits() {
      switch(this.hashSize) {
         case 16:
            int var12 = this.h7 & 0xFF | this.h6 & 0xFF000000 | this.h5 & 0xFF0000 | this.h4 & 0xFF00;
            this.h0 += var12 >>> 8 | var12 << 24;
            var12 = this.h7 & 0xFF00 | this.h6 & 0xFF | this.h5 & 0xFF000000 | this.h4 & 0xFF0000;
            this.h1 += var12 >>> 16 | var12 << 16;
            var12 = this.h7 & 0xFF0000 | this.h6 & 0xFF00 | this.h5 & 0xFF | this.h4 & 0xFF000000;
            this.h2 += var12 >>> 24 | var12 << 8;
            var12 = this.h7 & 0xFF000000 | this.h6 & 0xFF0000 | this.h5 & 0xFF00 | this.h4 & 0xFF;
            this.h3 += var12;
            break;
         case 20:
            int var7 = this.h7 & 63 | this.h6 & -33554432 | this.h5 & 33030144;
            this.h0 += var7 >>> 19 | var7 << 13;
            var7 = this.h7 & 4032 | this.h6 & 63 | this.h5 & -33554432;
            this.h1 += var7 >>> 25 | var7 << 7;
            var7 = this.h7 & 520192 | this.h6 & 4032 | this.h5 & 63;
            this.h2 += var7;
            var7 = this.h7 & 33030144 | this.h6 & 520192 | this.h5 & 4032;
            this.h3 += var7 >>> 6;
            var7 = this.h7 & -33554432 | this.h6 & 33030144 | this.h5 & 520192;
            this.h4 += var7 >>> 12;
            break;
         case 24:
            int var1 = this.h7 & 31 | this.h6 & -67108864;
            this.h0 += var1 >>> 26 | var1 << 6;
            var1 = this.h7 & 992 | this.h6 & 31;
            this.h1 += var1;
            var1 = this.h7 & 64512 | this.h6 & 992;
            this.h2 += var1 >>> 5;
            var1 = this.h7 & 2031616 | this.h6 & 64512;
            this.h3 += var1 >>> 10;
            var1 = this.h7 & 65011712 | this.h6 & 2031616;
            this.h4 += var1 >>> 16;
            var1 = this.h7 & -67108864 | this.h6 & 65011712;
            this.h5 += var1 >>> 21;
            break;
         case 28:
            this.h0 += this.h7 >>> 27 & 31;
            this.h1 += this.h7 >>> 22 & 31;
            this.h2 += this.h7 >>> 18 & 15;
            this.h3 += this.h7 >>> 13 & 31;
            this.h4 += this.h7 >>> 9 & 15;
            this.h5 += this.h7 >>> 4 & 31;
            this.h6 += this.h7 & 15;
      }
   }

   private int FF1(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
      int var10;
      switch(this.rounds) {
         case 3:
            var10 = this.f1(var7, var8, var5, var3, var2, var6, var4);
            break;
         case 4:
            var10 = this.f1(var6, var2, var7, var4, var3, var5, var8);
            break;
         default:
            var10 = this.f1(var5, var4, var7, var8, var3, var6, var2);
      }

      return (var10 >>> 7 | var10 << 25) + (var1 >>> 11 | var1 << 21) + var9;
   }

   private int FF2(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      int var11;
      switch(this.rounds) {
         case 3:
            var11 = this.f2(var4, var6, var7, var8, var3, var5, var2);
            break;
         case 4:
            var11 = this.f2(var5, var3, var6, var8, var7, var2, var4);
            break;
         default:
            var11 = this.f2(var2, var6, var7, var8, var5, var4, var3);
      }

      return (var11 >>> 7 | var11 << 25) + (var1 >>> 11 | var1 << 21) + var9 + var10;
   }

   private int FF3(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      int var11;
      switch(this.rounds) {
         case 3:
            var11 = this.f3(var2, var7, var6, var5, var4, var3, var8);
            break;
         case 4:
            var11 = this.f3(var7, var4, var5, var2, var8, var6, var3);
            break;
         default:
            var11 = this.f3(var6, var2, var8, var4, var5, var7, var3);
      }

      return (var11 >>> 7 | var11 << 25) + (var1 >>> 11 | var1 << 21) + var9 + var10;
   }

   private int FF4(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      int var11;
      switch(this.rounds) {
         case 4:
            var11 = this.f4(var2, var4, var8, var3, var6, var7, var5);
            break;
         default:
            var11 = this.f4(var7, var3, var5, var6, var8, var4, var2);
      }

      return (var11 >>> 7 | var11 << 25) + (var1 >>> 11 | var1 << 21) + var9 + var10;
   }

   private int FF5(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      int var11 = this.f5(var6, var3, var8, var2, var4, var5, var7);
      return (var11 >>> 7 | var11 << 25) + (var1 >>> 11 | var1 << 21) + var9 + var10;
   }

   private int f1(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return var6 & (var7 ^ var3) ^ var5 & var2 ^ var4 & var1 ^ var7;
   }

   private int f2(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return var5 & (var6 & ~var4 ^ var3 & var2 ^ var1 ^ var7) ^ var3 & (var6 ^ var2) ^ var4 & var2 ^ var7;
   }

   private int f3(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return var4 & (var6 & var5 ^ var1 ^ var7) ^ var6 & var3 ^ var5 & var2 ^ var7;
   }

   private int f4(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return var3 & (var2 & ~var5 ^ var4 & ~var1 ^ var6 ^ var1 ^ var7) ^ var4 & (var6 & var5 ^ var2 ^ var1) ^ var5 & var1 ^ var7;
   }

   private int f5(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      return var7 & (var6 & var5 & var4 ^ ~var2) ^ var6 & var3 ^ var5 & var2 ^ var4 & var1;
   }
}
