package org.apache.commons.math.distribution;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class CauchyDistributionImpl extends AbstractContinuousDistribution implements CauchyDistribution, Serializable {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = 8589540077390120676L;
   private double median = 0.0;
   private double scale = 1.0;
   private final double solverAbsoluteAccuracy;

   public CauchyDistributionImpl() {
      this(0.0, 1.0);
   }

   public CauchyDistributionImpl(double median, double s) {
      this(median, s, 1.0E-9);
   }

   public CauchyDistributionImpl(double median, double s, double inverseCumAccuracy) {
      this.setMedianInternal(median);
      this.setScaleInternal(s);
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   @Override
   public double cumulativeProbability(double x) {
      return 0.5 + FastMath.atan((x - this.median) / this.scale) / Math.PI;
   }

   @Override
   public double getMedian() {
      return this.median;
   }

   @Override
   public double getScale() {
      return this.scale;
   }

   @Override
   public double density(double x) {
      double dev = x - this.median;
      return 0.3183098861837907 * (this.scale / (dev * dev + this.scale * this.scale));
   }

   @Override
   public double inverseCumulativeProbability(double p) {
      if (!(p < 0.0) && !(p > 1.0)) {
         double ret;
         if (p == 0.0) {
            ret = Double.NEGATIVE_INFINITY;
         } else if (p == 1.0) {
            ret = Double.POSITIVE_INFINITY;
         } else {
            ret = this.median + this.scale * FastMath.tan(Math.PI * (p - 0.5));
         }

         return ret;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, p, 0.0, 1.0);
      }
   }

   @Deprecated
   @Override
   public void setMedian(double median) {
      this.setMedianInternal(median);
   }

   private void setMedianInternal(double newMedian) {
      this.median = newMedian;
   }

   @Deprecated
   @Override
   public void setScale(double s) {
      this.setScaleInternal(s);
   }

   private void setScaleInternal(double s) {
      if (s <= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_SCALE, s);
      } else {
         this.scale = s;
      }
   }

   @Override
   protected double getDomainLowerBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = -Double.MAX_VALUE;
      } else {
         ret = this.median;
      }

      return ret;
   }

   @Override
   protected double getDomainUpperBound(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.median;
      } else {
         ret = Double.MAX_VALUE;
      }

      return ret;
   }

   @Override
   protected double getInitialDomain(double p) {
      double ret;
      if (p < 0.5) {
         ret = this.median - this.scale;
      } else if (p > 0.5) {
         ret = this.median + this.scale;
      } else {
         ret = this.median;
      }

      return ret;
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
      return Double.NaN;
   }

   public double getNumericalVariance() {
      return Double.NaN;
   }
}
