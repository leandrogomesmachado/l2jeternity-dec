package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class ExponentialDistributionImpl extends AbstractContinuousDistribution implements ExponentialDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = 2401296428283614780L;
   private double mean;
   private final double solverAbsoluteAccuracy;

   public ExponentialDistributionImpl(double mean) {
      this(mean, 1.0E-9);
   }

   public ExponentialDistributionImpl(double mean, double inverseCumAccuracy) {
      this.setMeanInternal(mean);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Deprecated
   @Override
   public void setMean(double mean) {
      this.setMeanInternal(mean);
   }

   private void setMeanInternal(double newMean) {
      if (newMean <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_MEAN, newMean);
      } else {
         this.mean = newMean;
      }
   }

   @Override
   public double getMean() {
      return this.mean;
   }

   @Deprecated
   @Override
   public double density(Double x) {
      return this.density(x.doubleValue());
   }

   @Override
   public double density(double x) {
      return x < 0.0 ? 0.0 : FastMath.exp(-x / this.mean) / this.mean;
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      double ret;
      if (x <= 0.0) {
         ret = 0.0;
      } else {
         ret = 1.0 - FastMath.exp(-x / this.mean);
      }

      return ret;
   }

   @Override
   public double inverseCumulativeProbability(double p) throws MathException {
      if (!(p < 0.0) && !(p > 1.0)) {
         double ret;
         if (p == 1.0) {
            ret = Double.POSITIVE_INFINITY;
         } else {
            ret = -this.mean * FastMath.log(1.0 - p);
         }

         return ret;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   @Override
   public double sample() throws MathException {
      return this.randomData.nextExponential(this.mean);
   }

   @Override
   protected double getDomainLowerBound(double p) {
      return 0.0;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      return p < 0.5 ? this.mean : Double.MAX_VALUE;
   }

   @Override
   protected double getInitialDomain(double p) {
      return p < 0.5 ? this.mean * 0.5 : this.mean;
   }

   @Override
   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public double getSupportLowerBound() {
      return 0.0;
   }

   public double getSupportUpperBound() {
      return Double.POSITIVE_INFINITY;
   }

   public double getNumericalMean() {
      return this.getMean();
   }

   public double getNumericalVariance() {
      double m = this.getMean();
      return m * m;
   }
}
