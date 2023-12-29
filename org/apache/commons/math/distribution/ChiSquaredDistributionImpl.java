package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;

public class ChiSquaredDistributionImpl extends AbstractContinuousDistribution implements ChiSquaredDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -8352658048349159782L;
   private GammaDistribution gamma;
   private final double solverAbsoluteAccuracy;

   public ChiSquaredDistributionImpl(double df) {
      this(df, new GammaDistributionImpl(df / 2.0, 2.0));
   }

   @Deprecated
   public ChiSquaredDistributionImpl(double df, GammaDistribution g) {
      this.setGammaInternal(g);
      this.setDegreesOfFreedomInternal(df);
      this.solverAbsoluteAccuracy = 1.0E-9;
   }

   public ChiSquaredDistributionImpl(double df, double inverseCumAccuracy) {
      this.gamma = new GammaDistributionImpl(df / 2.0, 2.0);
      this.setDegreesOfFreedomInternal(df);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Deprecated
   @Override
   public void setDegreesOfFreedom(double degreesOfFreedom) {
      this.setDegreesOfFreedomInternal(degreesOfFreedom);
   }

   private void setDegreesOfFreedomInternal(double degreesOfFreedom) {
      this.gamma.setAlpha(degreesOfFreedom / 2.0);
   }

   @Override
   public double getDegreesOfFreedom() {
      return this.gamma.getAlpha() * 2.0;
   }

   @Deprecated
   @Override
   public double density(Double x) {
      return this.density(x.doubleValue());
   }

   @Override
   public double density(double x) {
      return this.gamma.density(x);
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      return this.gamma.cumulativeProbability(x);
   }

   @Override
   public double inverseCumulativeProbability(double p) throws MathException {
      if (p == 0.0) {
         return 0.0;
      } else {
         return p == 1.0 ? Double.POSITIVE_INFINITY : super.inverseCumulativeProbability(p);
      }
   }

   @Override
   protected double getDomainLowerBound(double p) {
      return (Double.MIN_VALUE) * this.gamma.getBeta();
   }

   @Override
   protected double getDomainUpperBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.getDegreesOfFreedom();
      } else {
         ret = Double.MAX_VALUE;
      }

      return ret;
   }

   @Override
   protected double getInitialDomain(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.getDegreesOfFreedom() * 0.5;
      } else {
         ret = this.getDegreesOfFreedom();
      }

      return ret;
   }

   @Deprecated
   public void setGamma(GammaDistribution g) {
      this.setGammaInternal(g);
   }

   private void setGammaInternal(GammaDistribution g) {
      this.gamma = g;
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
      return this.getDegreesOfFreedom();
   }

   public double getNumericalVariance() {
      return 2.0 * this.getDegreesOfFreedom();
   }
}
