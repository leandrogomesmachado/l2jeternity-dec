package org.apache.commons.math.random;

import java.io.Serializable;

public abstract class AbstractWell extends BitsStreamGenerator implements Serializable {
   private static final long serialVersionUID = -817701723016583596L;
   protected int index;
   protected final int[] v;
   protected final int[] iRm1;
   protected final int[] iRm2;
   protected final int[] i1;
   protected final int[] i2;
   protected final int[] i3;

   protected AbstractWell(int k, int m1, int m2, int m3) {
      this(k, m1, m2, m3, System.currentTimeMillis());
   }

   protected AbstractWell(int k, int m1, int m2, int m3, int seed) {
      this(k, m1, m2, m3, new int[]{seed});
   }

   protected AbstractWell(int k, int m1, int m2, int m3, int[] seed) {
      int w = 32;
      int r = (k + 32 - 1) / 32;
      this.v = new int[r];
      this.index = 0;
      this.iRm1 = new int[r];
      this.iRm2 = new int[r];
      this.i1 = new int[r];
      this.i2 = new int[r];
      this.i3 = new int[r];

      for(int j = 0; j < r; ++j) {
         this.iRm1[j] = (j + r - 1) % r;
         this.iRm2[j] = (j + r - 2) % r;
         this.i1[j] = (j + m1) % r;
         this.i2[j] = (j + m2) % r;
         this.i3[j] = (j + m3) % r;
      }

      this.setSeed(seed);
   }

   protected AbstractWell(int k, int m1, int m2, int m3, long seed) {
      this(k, m1, m2, m3, new int[]{(int)(seed >>> 32), (int)(seed & 4294967295L)});
   }

   @Override
   public void setSeed(int seed) {
      this.setSeed(new int[]{seed});
   }

   @Override
   public void setSeed(int[] seed) {
      if (seed == null) {
         this.setSeed(System.currentTimeMillis());
      } else {
         System.arraycopy(seed, 0, this.v, 0, Math.min(seed.length, this.v.length));
         if (seed.length < this.v.length) {
            for(int i = seed.length; i < this.v.length; ++i) {
               long l = (long)this.v[i - seed.length];
               this.v[i] = (int)(1812433253L * (l ^ l >> 30) + (long)i & 4294967295L);
            }
         }

         this.index = 0;
      }
   }

   @Override
   public void setSeed(long seed) {
      this.setSeed(new int[]{(int)(seed >>> 32), (int)(seed & 4294967295L)});
   }

   @Override
   protected abstract int next(int var1);
}
