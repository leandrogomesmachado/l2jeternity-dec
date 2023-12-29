package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.FastMath;

public class BetaDistributionImpl extends AbstractContinuousDistribution implements BetaDistribution {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -1221965979403477668L;
   private double alpha;
   private double beta;
   private double z;
   private final double solverAbsoluteAccuracy;

   public BetaDistributionImpl(double alpha, double beta, double inverseCumAccuracy) {
      this.alpha = alpha;
      this.beta = beta;
      this.z = Double.NaN;
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   public BetaDistributionImpl(double alpha, double beta) {
      this(alpha, beta, 1.0E-9);
   }

   @Deprecated
   @Override
   public void setAlpha(double alpha) {
      this.alpha = alpha;
      this.z = Double.NaN;
   }

   @Override
   public double getAlpha() {
      return this.alpha;
   }

   @Deprecated
   @Override
   public void setBeta(double beta) {
      this.beta = beta;
      this.z = Double.NaN;
   }

   @Override
   public double getBeta() {
      return this.beta;
   }

   private void recomputeZ() {
      if (Double.isNaN(this.z)) {
         this.z = Gamma.logGamma(this.alpha) + Gamma.logGamma(this.beta) - Gamma.logGamma(this.alpha + this.beta);
      }
   }

   @Deprecated
   @Override
   public double density(Double x) {
      return this.density(x.doubleValue());
   }

   @Override
   public double density(double x) {
      this.recomputeZ();
      if (x < 0.0 || x > 1.0) {
         return 0.0;
      } else if (x == 0.0) {
         if (this.alpha < 1.0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_0_FOR_SOME_ALPHA, this.alpha);
         } else {
            return 0.0;
         }
      } else if (x != 1.0) {
         double logX = FastMath.log(x);
         double log1mX = FastMath.log1p(-x);
         return FastMath.exp((this.alpha - 1.0) * logX + (this.beta - 1.0) * log1mX - this.z);
      } else if (this.beta < 1.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_1_FOR_SOME_BETA, this.beta);
      } else {
         return 0.0;
      }
   }

   @Override
   public double inverseCumulativeProbability(double p) throws MathException {
      if (p == 0.0) {
         return 0.0;
      } else {
         return p == 1.0 ? 1.0 : super.inverseCumulativeProbability(p);
      }
   }

   @Override
   protected double getInitialDomain(double p) {
      return p;
   }

   @Override
   protected double getDomainLowerBound(double p) {
      return 0.0;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      return 1.0;
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      if (x <= 0.0) {
         return 0.0;
      } else {
         return x >= 1.0 ? 1.0 : Beta.regularizedBeta(x, this.alpha, this.beta);
      }
   }

   @Override
   public double cumulativeProbability(double x0, double x1) throws MathException {
      return this.cumulativeProbability(x1) - this.cumulativeProbability(x0);
   }

   @Override
   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public double getSupportLowerBound() {
      return 0.0;
   }

   public double getSupportUpperBound() {
      return 1.0;
   }

   public double getNumericalMean() {
      double a = this.getAlpha();
      return a / (a + this.getBeta());
   }

   public double getNumericalVariance() {
      double a = this.getAlpha();
      double b = this.getBeta();
      double alphabetasum = a + b;
      return a * b / (alphabetasum * alphabetasum * (alphabetasum + 1.0));
   }
}
