package jonelo.jacksum.adapt.org.bouncycastle.crypto.digests;

import jonelo.jacksum.adapt.org.bouncycastle.crypto.BlockCipher;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.Digest;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.engines.GOST28147Engine;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.params.KeyParameter;
import jonelo.jacksum.adapt.org.bouncycastle.crypto.params.ParametersWithSBox;

public class GOST3411Digest implements Digest {
   private static final int DIGEST_LENGTH = 32;
   private byte[] H = new byte[32];
   private byte[] L = new byte[32];
   private byte[] M = new byte[32];
   private byte[] Sum = new byte[32];
   private byte[][] C = new byte[4][32];
   private byte[] xBuf = new byte[32];
   private int xBufOff;
   private long byteCount;
   private BlockCipher cipher = new GOST28147Engine();
   private byte[] K = new byte[32];
   byte[] a = new byte[8];
   short[] wS = new short[16];
   short[] w_S = new short[16];
   byte[] S = new byte[32];
   byte[] U = new byte[32];
   byte[] V = new byte[32];
   byte[] W = new byte[32];
   private static byte[] C2 = new byte[]{0, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, -1, -1, 0, -1};

   public GOST3411Digest() {
      this.cipher.init(true, new ParametersWithSBox(null, GOST28147Engine.getSBox("D-TEST")));
      this.reset();
   }

   public GOST3411Digest(GOST3411Digest var1) {
      this.cipher.init(true, new ParametersWithSBox(null, GOST28147Engine.getSBox("D-TEST")));
      this.reset();
      System.arraycopy(var1.H, 0, this.H, 0, var1.H.length);
      System.arraycopy(var1.L, 0, this.L, 0, var1.L.length);
      System.arraycopy(var1.M, 0, this.M, 0, var1.M.length);
      System.arraycopy(var1.Sum, 0, this.Sum, 0, var1.Sum.length);
      System.arraycopy(var1.C[1], 0, this.C[1], 0, var1.C[1].length);
      System.arraycopy(var1.C[2], 0, this.C[2], 0, var1.C[2].length);
      System.arraycopy(var1.C[3], 0, this.C[3], 0, var1.C[3].length);
      System.arraycopy(var1.xBuf, 0, this.xBuf, 0, var1.xBuf.length);
      this.xBufOff = var1.xBufOff;
      this.byteCount = var1.byteCount;
   }

   public String getAlgorithmName() {
      return "GOST3411";
   }

   public int getDigestSize() {
      return 32;
   }

   public void update(byte var1) {
      this.xBuf[this.xBufOff++] = var1;
      if (this.xBufOff == this.xBuf.length) {
         this.sumByteArray(this.xBuf);
         this.processBlock(this.xBuf, 0);
         this.xBufOff = 0;
      }

      ++this.byteCount;
   }

   public void update(byte[] var1, int var2, int var3) {
      while(this.xBufOff != 0 && var3 > 0) {
         this.update(var1[var2]);
         ++var2;
         --var3;
      }

      while(var3 > this.xBuf.length) {
         System.arraycopy(var1, var2, this.xBuf, 0, this.xBuf.length);
         this.sumByteArray(this.xBuf);
         this.processBlock(this.xBuf, 0);
         var2 += this.xBuf.length;
         var3 -= this.xBuf.length;
         this.byteCount += (long)this.xBuf.length;
      }

      while(var3 > 0) {
         this.update(var1[var2]);
         ++var2;
         --var3;
      }
   }

   private byte[] P(byte[] var1) {
      for(int var2 = 0; var2 < 8; ++var2) {
         this.K[0 + 4 * var2] = var1[0 + var2];
         this.K[1 + 4 * var2] = var1[8 + var2];
         this.K[2 + 4 * var2] = var1[16 + var2];
         this.K[3 + 4 * var2] = var1[24 + var2];
      }

      return this.K;
   }

   private byte[] A(byte[] var1) {
      for(int var2 = 0; var2 < 8; ++var2) {
         this.a[var2] = (byte)(var1[var2] ^ var1[var2 + 8]);
      }

      System.arraycopy(var1, 8, var1, 0, 24);
      System.arraycopy(this.a, 0, var1, 24, 8);
      return var1;
   }

   private void E(byte[] var1, byte[] var2, int var3, byte[] var4, int var5) {
      this.cipher.init(true, new KeyParameter(var1));
      this.cipher.processBlock(var4, var5, var2, var3);
   }

   private void fw(byte[] var1) {
      this.cpyBytesToShort(var1, this.wS);
      this.w_S[15] = (short)(this.wS[0] ^ this.wS[1] ^ this.wS[2] ^ this.wS[3] ^ this.wS[12] ^ this.wS[15]);

      for(int var2 = 14; var2 >= 0; --var2) {
         this.w_S[var2] = this.wS[var2 + 1];
      }

      this.cpyShortToBytes(this.w_S, var1);
   }

   protected void processBlock(byte[] var1, int var2) {
      System.arraycopy(var1, var2, this.M, 0, 32);
      boolean var3 = false;
      System.arraycopy(this.H, 0, this.U, 0, 32);
      System.arraycopy(this.M, 0, this.V, 0, 32);

      for(int var4 = 0; var4 < 32; ++var4) {
         this.W[var4] = (byte)(this.U[var4] ^ this.V[var4]);
      }

      this.E(this.P(this.W), this.S, 0, this.H, 0);

      for(int var6 = 1; var6 < 4; ++var6) {
         byte[] var7 = this.A(this.U);

         for(int var5 = 0; var5 < 32; ++var5) {
            this.U[var5] = (byte)(var7[var5] ^ this.C[var6][var5]);
         }

         this.V = this.A(this.A(this.V));

         for(int var13 = 0; var13 < 32; ++var13) {
            this.W[var13] = (byte)(this.U[var13] ^ this.V[var13]);
         }

         this.E(this.P(this.W), this.S, var6 * 8, this.H, var6 * 8);
      }

      for(int var8 = 0; var8 < 12; ++var8) {
         this.fw(this.S);
      }

      for(int var9 = 0; var9 < 32; ++var9) {
         this.S[var9] ^= this.M[var9];
      }

      this.fw(this.S);

      for(int var10 = 0; var10 < 32; ++var10) {
         this.S[var10] ^= this.H[var10];
      }

      for(int var11 = 0; var11 < 61; ++var11) {
         this.fw(this.S);
      }

      for(int var12 = 0; var12 < this.H.length; ++var12) {
         this.H[var12] = this.S[var12];
      }
   }

   private void finish() {
      this.LongToBytes(this.byteCount * 8L, this.L, 0);

      while(this.xBufOff != 0) {
         this.update((byte)0);
      }

      this.processBlock(this.L, 0);
      this.processBlock(this.Sum, 0);
   }

   public int doFinal(byte[] var1, int var2) {
      this.finish();

      for(int var3 = 0; var3 < this.H.length; ++var3) {
         var1[var3 + var2] = this.H[var3];
      }

      this.reset();
      return 32;
   }

   public void reset() {
      this.byteCount = 0L;
      this.xBufOff = 0;

      for(int var1 = 0; var1 < this.H.length; ++var1) {
         this.H[var1] = 0;
      }

      for(int var2 = 0; var2 < this.L.length; ++var2) {
         this.L[var2] = 0;
      }

      for(int var3 = 0; var3 < this.M.length; ++var3) {
         this.M[var3] = 0;
      }

      for(int var4 = 0; var4 < this.C[1].length; ++var4) {
         this.C[1][var4] = 0;
      }

      for(int var5 = 0; var5 < this.C[3].length; ++var5) {
         this.C[3][var5] = 0;
      }

      for(int var6 = 0; var6 < this.Sum.length; ++var6) {
         this.Sum[var6] = 0;
      }

      for(int var7 = 0; var7 < this.xBuf.length; ++var7) {
         this.xBuf[var7] = 0;
      }

      System.arraycopy(C2, 0, this.C[2], 0, C2.length);
   }

   private void sumByteArray(byte[] var1) {
      int var2 = 0;

      for(int var3 = 0; var3 != this.Sum.length; ++var3) {
         int var4 = (this.Sum[var3] & 255) + (var1[var3] & 255) + var2;
         this.Sum[var3] = (byte)var4;
         var2 = var4 >>> 8;
      }
   }

   private void LongToBytes(long var1, byte[] var3, int var4) {
      var3[var4 + 7] = (byte)((int)(var1 >> 56));
      var3[var4 + 6] = (byte)((int)(var1 >> 48));
      var3[var4 + 5] = (byte)((int)(var1 >> 40));
      var3[var4 + 4] = (byte)((int)(var1 >> 32));
      var3[var4 + 3] = (byte)((int)(var1 >> 24));
      var3[var4 + 2] = (byte)((int)(var1 >> 16));
      var3[var4 + 1] = (byte)((int)(var1 >> 8));
      var3[var4] = (byte)((int)var1);
   }

   private void cpyBytesToShort(byte[] var1, short[] var2) {
      for(int var3 = 0; var3 < var1.length / 2; ++var3) {
         var2[var3] = (short)(var1[var3 * 2 + 1] << 8 & 0xFF00 | var1[var3 * 2] & 255);
      }
   }

   private void cpyShortToBytes(short[] var1, byte[] var2) {
      for(int var3 = 0; var3 < var2.length / 2; ++var3) {
         var2[var3 * 2 + 1] = (byte)(var1[var3] >> 8);
         var2[var3 * 2] = (byte)var1[var3];
      }
   }
}
