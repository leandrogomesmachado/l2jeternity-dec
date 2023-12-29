package org.apache.commons.math.random;

import java.io.Serializable;
import org.apache.commons.math.util.FastMath;

public class MersenneTwister extends BitsStreamGenerator implements Serializable {
   private static final long serialVersionUID = 8661194735290153518L;
   private static final int N = 624;
   private static final int M = 397;
   private static final int[] MAG01 = new int[]{0, -1727483681};
   private int[] mt = new int[624];
   private int mti;

   public MersenneTwister() {
      this.setSeed(System.currentTimeMillis());
   }

   public MersenneTwister(int seed) {
      this.setSeed(seed);
   }

   public MersenneTwister(int[] seed) {
      this.setSeed(seed);
   }

   public MersenneTwister(long seed) {
      this.setSeed(seed);
   }

   @Override
   public void setSeed(int seed) {
      long longMT = (long)seed;
      this.mt[0] = (int)longMT;

      for(this.mti = 1; this.mti < 624; ++this.mti) {
         longMT = 1812433253L * (longMT ^ longMT >> 30) + (long)this.mti & 4294967295L;
         this.mt[this.mti] = (int)longMT;
      }
   }

   @Override
   public void setSeed(int[] seed) {
      if (seed == null) {
         this.setSeed(System.currentTimeMillis());
      } else {
         this.setSeed(19650218);
         int i = 1;
         int j = 0;

         for(int k = FastMath.max(624, seed.length); k != 0; --k) {
            long l0 = (long)this.mt[i] & 2147483647L | (this.mt[i] < 0 ? 2147483648L : 0L);
            long l1 = (long)this.mt[i - 1] & 2147483647L | (this.mt[i - 1] < 0 ? 2147483648L : 0L);
            long l = (l0 ^ (l1 ^ l1 >> 30) * 1664525L) + (long)seed[j] + (long)j;
            this.mt[i] = (int)(l & 4294967295L);
            ++i;
            ++j;
            if (i >= 624) {
               this.mt[0] = this.mt[623];
               i = 1;
            }

            if (j >= seed.length) {
               j = 0;
            }
         }

         for(int k = 623; k != 0; --k) {
            long l0 = (long)this.mt[i] & 2147483647L | (this.mt[i] < 0 ? 2147483648L : 0L);
            long l1 = (long)this.mt[i - 1] & 2147483647L | (this.mt[i - 1] < 0 ? 2147483648L : 0L);
            long l = (l0 ^ (l1 ^ l1 >> 30) * 1566083941L) - (long)i;
            this.mt[i] = (int)(l & 4294967295L);
            if (++i >= 624) {
               this.mt[0] = this.mt[623];
               i = 1;
            }
         }

         this.mt[0] = Integer.MIN_VALUE;
      }
   }

   @Override
   public void setSeed(long seed) {
      this.setSeed(new int[]{(int)(seed >>> 32), (int)(seed & 4294967295L)});
   }

   @Override
   protected int next(int bits) {
      if (this.mti >= 624) {
         int mtNext = this.mt[0];

         for(int k = 0; k < 227; ++k) {
            int mtCurr = mtNext;
            mtNext = this.mt[k + 1];
            int y = mtCurr & -2147483648 | mtNext & 2147483647;
            this.mt[k] = this.mt[k + 397] ^ y >>> 1 ^ MAG01[y & 1];
         }

         for(int k = 227; k < 623; ++k) {
            int mtCurr = mtNext;
            mtNext = this.mt[k + 1];
            int y = mtCurr & -2147483648 | mtNext & 2147483647;
            this.mt[k] = this.mt[k + -227] ^ y >>> 1 ^ MAG01[y & 1];
         }

         int y = mtNext & -2147483648 | this.mt[0] & 2147483647;
         this.mt[623] = this.mt[396] ^ y >>> 1 ^ MAG01[y & 1];
         this.mti = 0;
      }

      int y = this.mt[this.mti++];
      y ^= y >>> 11;
      y ^= y << 7 & -1658038656;
      y ^= y << 15 & -272236544;
      y ^= y >>> 18;
      return y >>> 32 - bits;
   }
}
