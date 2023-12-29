package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class MD2 extends BaseHash {
   private static final int DIGEST_LENGTH = 16;
   private static final int BLOCK_LENGTH = 16;
   private static final byte[] PI = new byte[]{
      41,
      46,
      67,
      -55,
      -94,
      -40,
      124,
      1,
      61,
      54,
      84,
      -95,
      -20,
      -16,
      6,
      19,
      98,
      -89,
      5,
      -13,
      -64,
      -57,
      115,
      -116,
      -104,
      -109,
      43,
      -39,
      -68,
      76,
      -126,
      -54,
      30,
      -101,
      87,
      60,
      -3,
      -44,
      -32,
      22,
      103,
      66,
      111,
      24,
      -118,
      23,
      -27,
      18,
      -66,
      78,
      -60,
      -42,
      -38,
      -98,
      -34,
      73,
      -96,
      -5,
      -11,
      -114,
      -69,
      47,
      -18,
      122,
      -87,
      104,
      121,
      -111,
      21,
      -78,
      7,
      63,
      -108,
      -62,
      16,
      -119,
      11,
      34,
      95,
      33,
      -128,
      127,
      93,
      -102,
      90,
      -112,
      50,
      39,
      53,
      62,
      -52,
      -25,
      -65,
      -9,
      -105,
      3,
      -1,
      25,
      48,
      -77,
      72,
      -91,
      -75,
      -47,
      -41,
      94,
      -110,
      42,
      -84,
      86,
      -86,
      -58,
      79,
      -72,
      56,
      -46,
      -106,
      -92,
      125,
      -74,
      118,
      -4,
      107,
      -30,
      -100,
      116,
      4,
      -15,
      69,
      -99,
      112,
      89,
      100,
      113,
      -121,
      32,
      -122,
      91,
      -49,
      101,
      -26,
      45,
      -88,
      2,
      27,
      96,
      37,
      -83,
      -82,
      -80,
      -71,
      -10,
      28,
      70,
      97,
      105,
      52,
      64,
      126,
      15,
      85,
      71,
      -93,
      35,
      -35,
      81,
      -81,
      58,
      -61,
      92,
      -7,
      -50,
      -70,
      -59,
      -22,
      38,
      44,
      83,
      13,
      110,
      -123,
      40,
      -124,
      9,
      -45,
      -33,
      -51,
      -12,
      65,
      -127,
      77,
      82,
      106,
      -36,
      55,
      -56,
      108,
      -63,
      -85,
      -6,
      36,
      -31,
      123,
      8,
      12,
      -67,
      -79,
      74,
      120,
      -120,
      -107,
      -117,
      -29,
      99,
      -24,
      109,
      -23,
      -53,
      -43,
      -2,
      59,
      0,
      29,
      57,
      -14,
      -17,
      -73,
      14,
      102,
      88,
      -48,
      -28,
      -90,
      119,
      114,
      -8,
      -21,
      117,
      75,
      10,
      49,
      68,
      80,
      -76,
      -113,
      -19,
      31,
      26,
      -37,
      -103,
      -115,
      51,
      -97,
      17,
      -125,
      20
   };
   private static final String DIGEST0 = "8350E5A3E24C153DF2275C9F80692773";
   private static Boolean valid;
   private byte[] checksum;
   private byte[] work;

   public MD2() {
      super("md2", 16, 16);
   }

   private MD2(MD2 var1) {
      this();
      this.count = var1.count;
      this.buffer = (byte[])var1.buffer.clone();
      this.checksum = (byte[])var1.checksum.clone();
      this.work = (byte[])var1.work.clone();
   }

   public Object clone() {
      return new MD2(this);
   }

   protected byte[] getResult() {
      byte[] var1 = new byte[16];
      this.encryptBlock(this.checksum, 0);

      for(int var2 = 0; var2 < 16; ++var2) {
         var1[var2] = this.work[var2];
      }

      return var1;
   }

   protected void resetContext() {
      this.checksum = new byte[16];
      this.work = new byte[48];
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean("8350E5A3E24C153DF2275C9F80692773".equals(Util.toString(new MD2().digest())));
      }

      return valid;
   }

   protected byte[] padBuffer() {
      int var1 = 16 - (int)(this.count % 16L);
      if (var1 == 0) {
         var1 = 16;
      }

      byte[] var2 = new byte[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         var2[var3] = (byte)var1;
      }

      return var2;
   }

   protected void transform(byte[] var1, int var2) {
      this.updateCheckSumAndEncryptBlock(var1, var2);
   }

   private void encryptBlock(byte[] var1, int var2) {
      for(int var3 = 0; var3 < 16; ++var3) {
         byte var4 = var1[var2 + var3];
         this.work[16 + var3] = var4;
         this.work[32 + var3] = (byte)(this.work[var3] ^ var4);
      }

      byte var6 = 0;

      for(int var7 = 0; var7 < 18; ++var7) {
         for(int var5 = 0; var5 < 48; ++var5) {
            var6 = (byte)(this.work[var5] ^ PI[var6 & 255]);
            this.work[var5] = var6;
         }

         var6 = (byte)(var6 + var7);
      }
   }

   private void updateCheckSumAndEncryptBlock(byte[] var1, int var2) {
      byte var3 = this.checksum[15];

      for(int var4 = 0; var4 < 16; ++var4) {
         byte var5 = var1[var2 + var4];
         this.work[16 + var4] = var5;
         this.work[32 + var4] = (byte)(this.work[var4] ^ var5);
         var3 = (byte)(this.checksum[var4] ^ PI[(var5 ^ var3) & 0xFF]);
         this.checksum[var4] = var3;
      }

      byte var7 = 0;

      for(int var8 = 0; var8 < 18; ++var8) {
         for(int var6 = 0; var6 < 48; ++var6) {
            var7 = (byte)(this.work[var6] ^ PI[var7 & 255]);
            this.work[var6] = var7;
         }

         var7 = (byte)(var7 + var8);
      }
   }
}
