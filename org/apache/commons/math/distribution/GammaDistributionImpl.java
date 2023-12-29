package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.FastMath;

public class GammaDistributionImpl extends AbstractContinuousDistribution implements GammaDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -3239549463135430361L;
   private double alpha;
   private double beta;
   private final double solverAbsoluteAccuracy;

   public GammaDistributionImpl(double alpha, double beta) {
      this(alpha, beta, 1.0E-9);
   }

   public GammaDistributionImpl(double alpha, double beta, double inverseCumAccuracy) {
      this.setAlphaInternal(alpha);
      this.setBetaInternal(beta);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      double ret;
      if (x <= 0.0) {
         ret = 0.0;
      } else {
         ret = Gamma.regularizedGammaP(this.alpha, x / this.beta);
      }

      return ret;
   }

   @Override
   public double inverseCumulativeProbability(double p) throws MathException {
      if (p == 0.0) {
         return 0.0;
      } else {
         return p == 1.0 ? Double.POSITIVE_INFINITY : super.inverseCumulativeProbability(p);
      }
   }

   @Deprecated
   @Override
   public void setAlpha(double alpha) {
      this.setAlphaInternal(alpha);
   }

   private void setAlphaInternal(double newAlpha) {
      if (newAlpha <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_ALPHA, newAlpha);
      } else {
         this.alpha = newAlpha;
      }
   }

   @Override
   public double getAlpha() {
      return this.alpha;
   }

   @Deprecated
   @Override
   public void setBeta(double newBeta) {
      this.setBetaInternal(newBeta);
   }

   private void setBetaInternal(double newBeta) {
      if (newBeta <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_BETA, newBeta);
      } else {
         this.beta = newBeta;
      }
   }

   @Override
   public double getBeta() {
      return this.beta;
   }

   @Override
   public double density(double x) {
      return x < 0.0
         ? 0.0
         : FastMath.pow(x / this.beta, this.alpha - 1.0) / this.beta * FastMath.exp(-x / this.beta) / FastMath.exp(Gamma.logGamma(this.alpha));
   }

   @Deprecated
   @Override
   public double density(Double x) {
      return this.density(x.doubleValue());
   }

   @Override
   protected double getDomainLowerBound(double p) {
      return Double.MIN_VALUE;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.alpha * this.beta;
      } else {
         ret = Double.MAX_VALUE;
      }

      return ret;
   }

   @Override
   protected double getInitialDomain(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.alpha * this.beta * 0.5;
      } else {
         ret = this.alpha * this.beta;
      }

      return ret;
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
      return this.getAlpha() * this.getBeta();
   }

   public double getNumericalVariance() {
      double b = this.getBeta();
      return this.getAlpha() * b * b;
   }
}
