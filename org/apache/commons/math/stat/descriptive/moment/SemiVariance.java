package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.AbstractUnivariateStatistic;

public class SemiVariance extends AbstractUnivariateStatistic implements Serializable {
   public static final SemiVariance.Direction UPSIDE_VARIANCE = SemiVariance.Direction.UPSIDE;
   public static final SemiVariance.Direction DOWNSIDE_VARIANCE = SemiVariance.Direction.DOWNSIDE;
   private static final long serialVersionUID = -2653430366886024994L;
   private boolean biasCorrected = true;
   private SemiVariance.Direction varianceDirection = SemiVariance.Direction.DOWNSIDE;

   public SemiVariance() {
   }

   public SemiVariance(boolean biasCorrected) {
      this.biasCorrected = biasCorrected;
   }

   public SemiVariance(SemiVariance.Direction direction) {
      this.varianceDirection = direction;
   }

   public SemiVariance(boolean corrected, SemiVariance.Direction direction) {
      this.biasCorrected = corrected;
      this.varianceDirection = direction;
   }

   public SemiVariance(SemiVariance original) {
      copy(original, this);
   }

   public SemiVariance copy() {
      SemiVariance result = new SemiVariance();
      copy(this, result);
      return result;
   }

   public static void copy(SemiVariance source, SemiVariance dest) {
      dest.setData(source.getDataRef());
      dest.biasCorrected = source.biasCorrected;
      dest.varianceDirection = source.varianceDirection;
   }

   @Override
   public double evaluate(double[] values) {
      if (values == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else {
         return this.evaluate(values, 0, values.length);
      }
   }

   @Override
   public double evaluate(double[] values, int start, int length) {
      double m = new Mean().evaluate(values, start, length);
      return this.evaluate(values, m, this.varianceDirection, this.biasCorrected, 0, values.length);
   }

   public double evaluate(double[] values, SemiVariance.Direction direction) {
      double m = new Mean().evaluate(values);
      return this.evaluate(values, m, direction, this.biasCorrected, 0, values.length);
   }

   public double evaluate(double[] values, double cutoff) {
      return this.evaluate(values, cutoff, this.varianceDirection, this.biasCorrected, 0, values.length);
   }

   public double evaluate(double[] values, double cutoff, SemiVariance.Direction direction) {
      return this.evaluate(values, cutoff, direction, this.biasCorrected, 0, values.length);
   }

   public double evaluate(double[] values, double cutoff, SemiVariance.Direction direction, boolean corrected, int start, int length) {
      this.test(values, start, length);
      if (values.length == 0) {
         return Double.NaN;
      } else if (values.length == 1) {
         return 0.0;
      } else {
         boolean booleanDirection = direction.getDirection();
         double dev = 0.0;
         double sumsq = 0.0;

         for(int i = start; i < length; ++i) {
            if (values[i] > cutoff == booleanDirection) {
               dev = values[i] - cutoff;
               sumsq += dev * dev;
            }
         }

         return corrected ? sumsq / ((double)length - 1.0) : sumsq / (double)length;
      }
   }

   public boolean isBiasCorrected() {
      return this.biasCorrected;
   }

   public void setBiasCorrected(boolean biasCorrected) {
      this.biasCorrected = biasCorrected;
   }

   public SemiVariance.Direction getVarianceDirection() {
      return this.varianceDirection;
   }

   public void setVarianceDirection(SemiVariance.Direction varianceDirection) {
      this.varianceDirection = varianceDirection;
   }

   public static enum Direction {
      UPSIDE(true),
      DOWNSIDE(false);

      private boolean direction;

      private Direction(boolean b) {
         this.direction = b;
      }

      boolean getDirection() {
         return this.direction;
      }
   }
}
