package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Erf;
import org.apache.commons.math.util.FastMath;

public class NormalDistributionImpl extends AbstractContinuousDistribution implements NormalDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = 8589540077390120676L;
   private static final double SQRT2PI = FastMath.sqrt(Math.PI * 2);
   private double mean = 0.0;
   private double standardDeviation = 1.0;
   private final double solverAbsoluteAccuracy;

   public NormalDistributionImpl(double mean, double sd) {
      this(mean, sd, 1.0E-9);
   }

   public NormalDistributionImpl(double mean, double sd, double inverseCumAccuracy) {
      this.setMeanInternal(mean);
      this.setStandardDeviationInternal(sd);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   public NormalDistributionImpl() {
      this(0.0, 1.0);
   }

   @Override
   public double getMean() {
      return this.mean;
   }

   @Deprecated
   @Override
   public void setMean(double mean) {
      this.setMeanInternal(mean);
   }

   private void setMeanInternal(double newMean) {
      this.mean = newMean;
   }

   @Override
   public double getStandardDeviation() {
      return this.standardDeviation;
   }

   @Deprecated
   @Override
   public void setStandardDeviation(double sd) {
      this.setStandardDeviationInternal(sd);
   }

   private void setStandardDeviationInternal(double sd) {
      if (sd <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_STANDARD_DEVIATION, sd);
      } else {
         this.standardDeviation = sd;
      }
   }

   @Deprecated
   @Override
   public double density(Double x) {
      return this.density(x.doubleValue());
   }

   @Override
   public double density(double x) {
      double x0 = x - this.mean;
      return FastMath.exp(-x0 * x0 / (2.0 * this.standardDeviation * this.standardDeviation)) / (this.standardDeviation * SQRT2PI);
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      double dev = x - this.mean;
      if (FastMath.abs(dev) > 40.0 * this.standardDeviation) {
         return dev < 0.0 ? 0.0 : 1.0;
      } else {
         return 0.5 * (1.0 + Erf.erf(dev / (this.standardDeviation * FastMath.sqrt(2.0))));
      }
   }

   @Override
   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   @Override
   public double inverseCumulativeProbability(double p) throws MathException {
      if (p == 0.0) {
         return Double.NEGATIVE_INFINITY;
      } else {
         return p == 1.0 ? Double.POSITIVE_INFINITY : super.inverseCumulativeProbability(p);
      }
   }

   @Override
   public double sample() throws MathException {
      return this.randomData.nextGaussian(this.mean, this.standardDeviation);
   }

   @Override
   protected double getDomainLowerBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = -Double.MAX_VALUE;
      } else {
         ret = this.mean;
      }

      return ret;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.mean;
      } else {
         ret = Double.MAX_VALUE;
      }

      return ret;
   }

   @Override
   protected double getInitialDomain(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.mean - this.standardDeviation;
      } else if (p > 0.5) {
         ret = this.mean + this.standardDeviation;
      } else {
         ret = this.mean;
      }

      return ret;
   }

   public double getSupportLowerBound() {
      return Double.NEGATIVE_INFINITY;
   }

   public double getSupportUpperBound() {
      return Double.POSITIVE_INFINITY;
   }

   public double getNumericalVariance() {
      double s = this.getStandardDeviation();
      return s * s;
   }
}
