package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.FastMath;

public class TDistributionImpl extends AbstractContinuousDistribution implements TDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -5852615386664158222L;
   private double degreesOfFreedom;
   private final double solverAbsoluteAccuracy;

   public TDistributionImpl(double degreesOfFreedom, double inverseCumAccuracy) {
      this.setDegreesOfFreedomInternal(degreesOfFreedom);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   public TDistributionImpl(double degreesOfFreedom) {
      this(degreesOfFreedom, 1.0E-9);
   }

   @Deprecated
   @Override
   public void setDegreesOfFreedom(double degreesOfFreedom) {
      this.setDegreesOfFreedomInternal(degreesOfFreedom);
   }

   private void setDegreesOfFreedomInternal(double newDegreesOfFreedom) {
      if (newDegreesOfFreedom <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_DEGREES_OF_FREEDOM, newDegreesOfFreedom);
      } else {
         this.degreesOfFreedom = newDegreesOfFreedom;
      }
   }

   @Override
   public double getDegreesOfFreedom() {
      return this.degreesOfFreedom;
   }

   @Override
   public double density(double x) {
      double n = this.degreesOfFreedom;
      double nPlus1Over2 = (n + 1.0) / 2.0;
      return FastMath.exp(
         Gamma.logGamma(nPlus1Over2) - 0.5 * (FastMath.log(Math.PI) + FastMath.log(n)) - Gamma.logGamma(n / 2.0) - nPlus1Over2 * FastMath.log(1.0 + x * x / n)
      );
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      double ret;
      if (x == 0.0) {
         ret = 0.5;
      } else {
         double t = Beta.regularizedBeta(this.degreesOfFreedom / (this.degreesOfFreedom + x * x), 0.5 * this.degreesOfFreedom, 0.5);
         if (x < 0.0) {
            ret = 0.5 * t;
         } else {
            ret = 1.0 - 0.5 * t;
         }
      }

      return ret;
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
   protected double getDomainLowerBound(double p) {
      return -Double.MAX_VALUE;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      return Double.MAX_VALUE;
   }

   @Override
   protected double getInitialDomain(double p) {
      return 0.0;
   }

   @Override
   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public double getSupportLowerBound() {
      return Double.NEGATIVE_INFINITY;
   }

   public double getSupportUpperBound() {
      return Double.POSITIVE_INFINITY;
   }

   public double getNumericalMean() {
      double df = this.getDegreesOfFreedom();
      return df > 1.0 ? 0.0 : Double.NaN;
   }

   public double getNumericalVariance() {
      double df = this.getDegreesOfFreedom();
      if (df > 2.0) {
         return df / (df - 2.0);
      } else {
         return df > 1.0 && df <= 2.0 ? Double.POSITIVE_INFINITY : Double.NaN;
      }
   }
}
