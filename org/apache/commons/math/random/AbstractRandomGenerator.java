package org.apache.commons.math.random;

import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractRandomGenerator implements RandomGenerator {
   private double cachedNormalDeviate = Double.NaN;

   public void clear() {
      this.cachedNormalDeviate = Double.NaN;
   }

   @Override
   public void setSeed(int seed) {
      this.setSeed((long)seed);
   }

   @Override
   public void setSeed(int[] seed) {
      long prime = 4294967291L;
      long combined = 0L;

      for(int s : seed) {
         combined = combined * 4294967291L + (long)s;
      }

      this.setSeed(combined);
   }

   @Override
   public abstract void setSeed(long var1);

   @Override
   public void nextBytes(byte[] bytes) {
      int bytesOut = 0;

      while(bytesOut < bytes.length) {
         int randInt = this.nextInt();

         for(int i = 0; i < 3; ++i) {
            if (i > 0) {
               randInt >>= 8;
            }

            bytes[bytesOut++] = (byte)randInt;
            if (bytesOut == bytes.length) {
               return;
            }
         }
      }
   }

   @Override
   public int nextInt() {
      return (int)(this.nextDouble() * 2.147483647E9);
   }

   @Override
   public int nextInt(int n) {
      if (n <= 0) {
         throw new NotStrictlyPositiveException(n);
      } else {
         int result = (int)(this.nextDouble() * (double)n);
         return result < n ? result : n - 1;
      }
   }

   @Override
   public long nextLong() {
      return (long)(this.nextDouble() * 9.223372E18F);
   }

   @Override
   public boolean nextBoolean() {
      return this.nextDouble() <= 0.5;
   }

   @Override
   public float nextFloat() {
      return (float)this.nextDouble();
   }

   @Override
   public abstract double nextDouble();

   @Override
   public double nextGaussian() {
      if (!Double.isNaN(this.cachedNormalDeviate)) {
         double dev = this.cachedNormalDeviate;
         this.cachedNormalDeviate = Double.NaN;
         return dev;
      } else {
         double v1 = 0.0;
         double v2 = 0.0;

         double s;
         for(s = 1.0; s >= 1.0; s = v1 * v1 + v2 * v2) {
            v1 = 2.0 * this.nextDouble() - 1.0;
            v2 = 2.0 * this.nextDouble() - 1.0;
         }

         if (s != 0.0) {
            s = FastMath.sqrt(-2.0 * FastMath.log(s) / s);
         }

         this.cachedNormalDeviate = v2 * s;
         return v1 * s;
      }
   }
}
