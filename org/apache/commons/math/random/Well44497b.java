package org.apache.commons.math.random;

public class Well44497b extends AbstractWell {
   private static final long serialVersionUID = 4032007538246675492L;
   private static final int K = 44497;
   private static final int M1 = 23;
   private static final int M2 = 481;
   private static final int M3 = 229;

   public Well44497b() {
      super(44497, 23, 481, 229);
   }

   public Well44497b(int seed) {
      super(44497, 23, 481, 229, seed);
   }

   public Well44497b(int[] seed) {
      super(44497, 23, 481, 229, seed);
   }

   public Well44497b(long seed) {
      super(44497, 23, 481, 229, seed);
   }

   @Override
   protected int next(int bits) {
      int indexRm1 = this.iRm1[this.index];
      int indexRm2 = this.iRm2[this.index];
      int v0 = this.v[this.index];
      int vM1 = this.v[this.i1[this.index]];
      int vM2 = this.v[this.i2[this.index]];
      int vM3 = this.v[this.i3[this.index]];
      int z0 = -32768 & this.v[indexRm1] ^ 32767 & this.v[indexRm2];
      int z1 = v0 ^ v0 << 24 ^ vM1 ^ vM1 >>> 30;
      int z2 = vM2 ^ vM2 << 10 ^ vM3 << 26;
      int z3 = z1 ^ z2;
      int z2Prime = (z2 << 9 ^ z2 >>> 23) & -67108865;
      int z2Second = (z2 & 131072) != 0 ? z2Prime ^ -1221985044 : z2Prime;
      int z4 = z0 ^ z1 ^ z1 >>> 20 ^ z2Second ^ z3;
      this.v[this.index] = z3;
      this.v[indexRm1] = z4;
      this.v[indexRm2] &= -32768;
      this.index = indexRm1;
      z4 ^= z4 << 7 & -1814227968;
      z4 ^= z4 << 15 & -99516416;
      return z4 >>> 32 - bits;
   }
}
