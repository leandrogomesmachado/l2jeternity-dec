package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.util.FastMath;

public class FDistributionImpl extends AbstractContinuousDistribution implements FDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -8516354193418641566L;
   private double numeratorDegreesOfFreedom;
   private double denominatorDegreesOfFreedom;
   private final double solverAbsoluteAccuracy;

   public FDistributionImpl(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom) {
      this(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom, 1.0E-9);
   }

   public FDistributionImpl(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom, double inverseCumAccuracy) {
      this.setNumeratorDegreesOfFreedomInternal(numeratorDegreesOfFreedom);
      this.setDenominatorDegreesOfFreedomInternal(denominatorDegreesOfFreedom);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Override
   public double density(double x) {
      double nhalf = this.numeratorDegreesOfFreedom / 2.0;
      double mhalf = this.denominatorDegreesOfFreedom / 2.0;
      double logx = FastMath.log(x);
      double logn = FastMath.log(this.numeratorDegreesOfFreedom);
      double logm = FastMath.log(this.denominatorDegreesOfFreedom);
      double lognxm = FastMath.log(this.numeratorDegreesOfFreedom * x + this.denominatorDegreesOfFreedom);
      return FastMath.exp(nhalf * logn + nhalf * logx - logx + mhalf * logm - nhalf * lognxm - mhalf * lognxm - Beta.logBeta(nhalf, mhalf));
   }

   @Override
   public double cumulativeProbability(double x) throws MathException {
      double ret;
      if (x <= 0.0) {
         ret = 0.0;
      } else {
         double n = this.numeratorDegreesOfFreedom;
         double m = this.denominatorDegreesOfFreedom;
         ret = Beta.regularizedBeta(n * x / (m + n * x), 0.5 * n, 0.5 * m);
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

   @Override
   protected double getDomainLowerBound(double p) {
      return 0.0;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      return Double.MAX_VALUE;
   }

   @Override
   protected double getInitialDomain(double p) {
      double ret = 1.0;
      double d = this.denominatorDegreesOfFreedom;
      if (d > 2.0) {
         ret = d / (d - 2.0);
      }

      return ret;
   }

   @Deprecated
   @Override
   public void setNumeratorDegreesOfFreedom(double degreesOfFreedom) {
      this.setNumeratorDegreesOfFreedomInternal(degreesOfFreedom);
   }

   private void setNumeratorDegreesOfFreedomInternal(double degreesOfFreedom) {
      if (degreesOfFreedom <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_DEGREES_OF_FREEDOM, degreesOfFreedom);
      } else {
         this.numeratorDegreesOfFreedom = degreesOfFreedom;
      }
   }

   @Override
   public double getNumeratorDegreesOfFreedom() {
      return this.numeratorDegreesOfFreedom;
   }

   @Deprecated
   @Override
   public void setDenominatorDegreesOfFreedom(double degreesOfFreedom) {
      this.setDenominatorDegreesOfFreedomInternal(degreesOfFreedom);
   }

   private void setDenominatorDegreesOfFreedomInternal(double degreesOfFreedom) {
      if (degreesOfFreedom <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_DEGREES_OF_FREEDOM, degreesOfFreedom);
      } else {
         this.denominatorDegreesOfFreedom = degreesOfFreedom;
      }
   }

   @Override
   public double getDenominatorDegreesOfFreedom() {
      return this.denominatorDegreesOfFreedom;
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
      double denominatorDF = this.getDenominatorDegreesOfFreedom();
      return denominatorDF > 2.0 ? denominatorDF / (denominatorDF - 2.0) : Double.NaN;
   }

   public double getNumericalVariance() {
      double denominatorDF = this.getDenominatorDegreesOfFreedom();
      if (denominatorDF > 4.0) {
         double numeratorDF = this.getNumeratorDegreesOfFreedom();
         double denomDFMinusTwo = denominatorDF - 2.0;
         return 2.0
            * denominatorDF
            * denominatorDF
            * (numeratorDF + denominatorDF - 2.0)
            / (numeratorDF * denomDFMinusTwo * denomDFMinusTwo * (denominatorDF - 4.0));
      } else {
         return Double.NaN;
      }
   }
}
