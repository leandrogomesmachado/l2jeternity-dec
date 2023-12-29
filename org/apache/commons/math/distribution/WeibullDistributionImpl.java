package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.FastMath;

public class WeibullDistributionImpl extends AbstractContinuousDistribution implements WeibullDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = 8589540077390120676L;
   private double shape;
   private double scale;
   private final double solverAbsoluteAccuracy;
   private double numericalMean = Double.NaN;
   private boolean numericalMeanIsCalculated = false;
   private double numericalVariance = Double.NaN;
   private boolean numericalVarianceIsCalculated = false;

   public WeibullDistributionImpl(double alpha, double beta) {
      this(alpha, beta, 1.0E-9);
   }

   public WeibullDistributionImpl(double alpha, double beta, double inverseCumAccuracy) {
      this.setShapeInternal(alpha);
      this.setScaleInternal(beta);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Override
   public double cumulativeProbability(double x) {
      double ret;
      if (x <= 0.0) {
         ret = 0.0;
      } else {
         ret = 1.0 - FastMath.exp(-FastMath.pow(x / this.scale, this.shape));
      }

      return ret;
   }

   @Override
   public double getShape() {
      return this.shape;
   }

   @Override
   public double getScale() {
      return this.scale;
   }

   @Override
   public double density(double x) {
      if (x < 0.0) {
         return 0.0;
      } else {
         double xscale = x / this.scale;
         double xscalepow = FastMath.pow(xscale, this.shape - 1.0);
         double xscalepowshape = xscalepow * xscale;
         return this.shape / this.scale * xscalepow * FastMath.exp(-xscalepowshape);
      }
   }

   @Override
   public double inverseCumulativeProbability(double p) {
      if (!(p < 0.0) && !(p > 1.0)) {
         double ret;
         if (p == 0.0) {
            ret = 0.0;
         } else if (p == 1.0) {
            ret = Double.POSITIVE_INFINITY;
         } else {
            ret = this.scale * FastMath.pow(-FastMath.log(1.0 - p), 1.0 / this.shape);
         }

         return ret;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   @Deprecated
   @Override
   public void setShape(double alpha) {
      this.setShapeInternal(alpha);
      this.invalidateParameterDependentMoments();
   }

   private void setShapeInternal(double alpha) {
      if (alpha <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SHAPE, alpha);
      } else {
         this.shape = alpha;
      }
   }

   @Deprecated
   @Override
   public void setScale(double beta) {
      this.setScaleInternal(beta);
      this.invalidateParameterDependentMoments();
   }

   private void setScaleInternal(double beta) {
      if (beta <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SCALE, beta);
      } else {
         this.scale = beta;
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
      return FastMath.pow(this.scale * FastMath.log(2.0), 1.0 / this.shape);
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

   protected double calculateNumericalMean() {
      double sh = this.getShape();
      double sc = this.getScale();
      return sc * FastMath.exp(Gamma.logGamma(1.0 + 1.0 / sh));
   }

   private double calculateNumericalVariance() {
      double sh = this.getShape();
      double sc = this.getScale();
      double mn = this.getNumericalMean();
      return sc * sc * FastMath.exp(Gamma.logGamma(1.0 + 2.0 / sh)) - mn * mn;
   }

   public double getNumericalMean() {
      if (!this.numericalMeanIsCalculated) {
         this.numericalMean = this.calculateNumericalMean();
         this.numericalMeanIsCalculated = true;
      }

      return this.numericalMean;
   }

   public double getNumericalVariance() {
      if (!this.numericalVarianceIsCalculated) {
         this.numericalVariance = this.calculateNumericalVariance();
         this.numericalVarianceIsCalculated = true;
      }

      return this.numericalVariance;
   }

   private void invalidateParameterDependentMoments() {
      this.numericalMeanIsCalculated = false;
      this.numericalVarianceIsCalculated = false;
   }
}
