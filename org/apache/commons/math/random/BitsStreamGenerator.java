package org.apache.commons.math.random;

import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.util.FastMath;

public abstract class BitsStreamGenerator implements RandomGenerator {
   private double nextGaussian = Double.NaN;

   @Override
   public abstract void setSeed(int var1);

   @Override
   public abstract void setSeed(int[] var1);

   @Override
   public abstract void setSeed(long var1);

   protected abstract int next(int var1);

   @Override
   public boolean nextBoolean() {
      return this.next(1) != 0;
   }

   @Override
   public void nextBytes(byte[] bytes) {
      int i = 0;

      for(int iEnd = bytes.length - 3; i < iEnd; i += 4) {
         int random = this.next(32);
         bytes[i] = (byte)(random & 0xFF);
         bytes[i + 1] = (byte)(random >> 8 & 0xFF);
         bytes[i + 2] = (byte)(random >> 16 & 0xFF);
         bytes[i + 3] = (byte)(random >> 24 & 0xFF);
      }

      for(int random = this.next(32); i < bytes.length; random >>= 8) {
         bytes[i++] = (byte)(random & 0xFF);
      }
   }

   @Override
   public double nextDouble() {
      long high = (long)this.next(26) << 26;
      int low = this.next(26);
      return (double)(high | (long)low) * 2.220446E-16F;
   }

   @Override
   public float nextFloat() {
      return (float)this.next(23) * 1.1920929E-7F;
   }

   @Override
   public double nextGaussian() {
      double random;
      if (Double.isNaN(this.nextGaussian)) {
         double x = this.nextDouble();
         double y = this.nextDouble();
         double alpha = (Math.PI * 2) * x;
         double r = FastMath.sqrt(-2.0 * FastMath.log(y));
         random = r * FastMath.cos(alpha);
         this.nextGaussian = r * FastMath.sin(alpha);
      } else {
         random = this.nextGaussian;
         this.nextGaussian = Double.NaN;
      }

      return random;
   }

   @Override
   public int nextInt() {
      return this.next(32);
   }

   @Override
   public int nextInt(int n) throws IllegalArgumentException {
      if (n < 1) {
         throw new NotStrictlyPositiveException(n);
      } else {
         int mask = n | n >> 1;
         mask |= mask >> 2;
         mask |= mask >> 4;
         mask |= mask >> 8;
         mask |= mask >> 16;

         int random;
         do {
            random = this.next(32) & mask;
         } while(random >= n);

         return random;
      }
   }

   @Override
   public long nextLong() {
      long high = (long)this.next(32) << 32;
      long low = (long)this.next(32) & 4294967295L;
      return high | low;
   }
}
