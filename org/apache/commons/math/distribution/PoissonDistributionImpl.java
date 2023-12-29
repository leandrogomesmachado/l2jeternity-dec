package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.FastMath;

public class PoissonDistributionImpl extends AbstractIntegerDistribution implements PoissonDistribution, Serializable {
   public static final int DEFAULT_MAX_ITERATIONS = 10000000;
   public static final double DEFAULT_EPSILON = 1.0E-12;
   private static final long serialVersionUID = -3349935121172596109L;
   private NormalDistribution normal;
   private double mean;
   private int maxIterations = 10000000;
   private double epsilon = 1.0E-12;

   public PoissonDistributionImpl(double p) {
      this(p, new NormalDistributionImpl());
   }

   public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
      this.setMean(p);
      this.epsilon = epsilon;
      this.maxIterations = maxIterations;
   }

   public PoissonDistributionImpl(double p, double epsilon) {
      this.setMean(p);
      this.epsilon = epsilon;
   }

   public PoissonDistributionImpl(double p, int maxIterations) {
      this.setMean(p);
      this.maxIterations = maxIterations;
   }

   @Deprecated
   public PoissonDistributionImpl(double p, NormalDistribution z) {
      this.setNormalAndMeanInternal(z, p);
   }

   @Override
   public double getMean() {
      return this.mean;
   }

   @Deprecated
   @Override
   public void setMean(double p) {
      this.setNormalAndMeanInternal(this.normal, p);
   }

   private void setNormalAndMeanInternal(NormalDistribution z, double p) {
      if (p <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
      } else {
         this.mean = p;
         this.normal = z;
         this.normal.setMean(p);
         this.normal.setStandardDeviation(FastMath.sqrt(p));
      }
   }

   @Override
   public double probability(int x) {
      double ret;
      if (x < 0 || x == Integer.MAX_VALUE) {
         ret = 0.0;
      } else if (x == 0) {
         ret = FastMath.exp(-this.mean);
      } else {
         ret = FastMath.exp(-SaddlePointExpansion.getStirlingError((double)x) - SaddlePointExpansion.getDeviancePart((double)x, this.mean))
            / FastMath.sqrt((Math.PI * 2) * (double)x);
      }

      return ret;
   }

   @Override
   public double cumulativeProbability(int x) throws MathException {
      if (x < 0) {
         return 0.0;
      } else {
         return x == Integer.MAX_VALUE ? 1.0 : Gamma.regularizedGammaQ((double)x + 1.0, this.mean, this.epsilon, this.maxIterations);
      }
   }

   @Override
   public double normalApproximateProbability(int x) throws MathException {
      return this.normal.cumulativeProbability((double)x + 0.5);
   }

   @Override
   public int sample() throws MathException {
      return (int)FastMath.min(this.randomData.nextPoisson(this.mean), 2147483647L);
   }

   @Override
   protected int getDomainLowerBound(double p) {
      return 0;
   }

   @Override
   protected int getDomainUpperBound(double p) {
      return Integer.MAX_VALUE;
   }

   @Deprecated
   public void setNormal(NormalDistribution value) {
      this.setNormalAndMeanInternal(value, this.mean);
   }

   public int getSupportLowerBound() {
      return 0;
   }

   public int getSupportUpperBound() {
      return Integer.MAX_VALUE;
   }

   public double getNumericalVariance() {
      return this.getMean();
   }
}
