package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractIntegerDistribution extends AbstractDistribution implements IntegerDistribution, Serializable {
   private static final long serialVersionUID = -1146319659338487221L;
   protected final RandomDataImpl randomData = new RandomDataImpl();

   protected AbstractIntegerDistribution() {
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      return this.cumulativeProbability((int)FastMath.floor(x));
   }

   @Override
   public double cumulativeProbability(double x0, double x1) throws MathException {
      if (x0 > x1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1);
      } else {
         return FastMath.floor(x0) < x0
            ? this.cumulativeProbability((int)FastMath.floor(x0) + 1, (int)FastMath.floor(x1))
            : this.cumulativeProbability((int)FastMath.floor(x0), (int)FastMath.floor(x1));
      }
   }

   @Override
   public abstract double cumulativeProbability(int var1) throws MathException;

   @Override
   public double probability(double x) {
      double fl = FastMath.floor(x);
      return fl == x ? this.probability((int)x) : 0.0;
   }

   @Override
   public double cumulativeProbability(int x0, int x1) throws MathException {
      if (x0 > x1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1);
      } else {
         return this.cumulativeProbability(x1) - this.cumulativeProbability(x0 - 1);
      }
   }

   @Override
   public int inverseCumulativeProbability(double p) throws MathException {
      if (!(p < 0.0) && !(p > 1.0)) {
         int x0 = this.getDomainLowerBound(p);
         int x1 = this.getDomainUpperBound(p);

         while(x0 < x1) {
            int xm = x0 + (x1 - x0) / 2;
            double pm = this.checkedCumulativeProbability(xm);
            if (pm > p) {
               if (xm == x1) {
                  --x1;
               } else {
                  x1 = xm;
               }
            } else if (xm == x0) {
               ++x0;
            } else {
               x0 = xm;
            }
         }

         double pm = this.checkedCumulativeProbability(x0);

         while(pm > p) {
            pm = this.checkedCumulativeProbability(--x0);
         }

         return x0;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   public void reseedRandomGenerator(long seed) {
      this.randomData.reSeed(seed);
   }

   public int sample() throws MathException {
      return this.randomData.nextInversionDeviate(this);
   }

   public int[] sample(int sampleSize) throws MathException {
      if (sampleSize <= 0) {
         MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SAMPLE_SIZE, sampleSize);
      }

      int[] out = new int[sampleSize];

      for(int i = 0; i < sampleSize; ++i) {
         out[i] = this.sample();
      }

      return out;
   }

   private double checkedCumulativeProbability(int argument) throws MathException {
      double result = Double.NaN;
      result = this.cumulativeProbability(argument);
      if (Double.isNaN(result)) {
         throw new MathException(LocalizedFormats.DISCRETE_CUMULATIVE_PROBABILITY_RETURNED_NAN, argument);
      } else {
         return result;
      }
   }

   protected abstract int getDomainLowerBound(double var1);

   protected abstract int getDomainUpperBound(double var1);

   public boolean isSupportLowerBoundInclusive() {
      return true;
   }

   public boolean isSupportUpperBoundInclusive() {
      return true;
   }
}
