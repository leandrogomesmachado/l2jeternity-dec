package org.apache.commons.math.stat.descriptive;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class AbstractUnivariateStatistic implements UnivariateStatistic {
   private double[] storedData;

   public void setData(double[] values) {
      this.storedData = values == null ? null : (double[])values.clone();
   }

   public double[] getData() {
      return this.storedData == null ? null : (double[])this.storedData.clone();
   }

   protected double[] getDataRef() {
      return this.storedData;
   }

   public void setData(double[] values, int begin, int length) {
      this.storedData = new double[length];
      System.arraycopy(values, begin, this.storedData, 0, length);
   }

   public double evaluate() {
      return this.evaluate(this.storedData);
   }

   @Override
   public double evaluate(double[] values) {
      this.test(values, 0, 0);
      return this.evaluate(values, 0, values.length);
   }

   @Override
   public abstract double evaluate(double[] var1, int var2, int var3);

   @Override
   public abstract UnivariateStatistic copy();

   protected boolean test(double[] values, int begin, int length) {
      if (values == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else if (begin < 0) {
         throw new NotPositiveException(LocalizedFormats.START_POSITION, begin);
      } else if (length < 0) {
         throw new NotPositiveException(LocalizedFormats.LENGTH, length);
      } else if (begin + length > values.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SUBARRAY_ENDS_AFTER_ARRAY_END);
      } else {
         return length != 0;
      }
   }

   protected boolean test(double[] values, double[] weights, int begin, int length) {
      if (weights == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else if (weights.length != values.length) {
         throw new DimensionMismatchException(weights.length, values.length);
      } else {
         boolean containsPositiveWeight = false;

         for(int i = begin; i < begin + length; ++i) {
            if (Double.isNaN(weights[i])) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NAN_ELEMENT_AT_INDEX, i);
            }

            if (Double.isInfinite(weights[i])) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INFINITE_ARRAY_ELEMENT, weights[i], i);
            }

            if (weights[i] < 0.0) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NEGATIVE_ELEMENT_AT_INDEX, i, weights[i]);
            }

            if (!containsPositiveWeight && weights[i] > 0.0) {
               containsPositiveWeight = true;
            }
         }

         if (!containsPositiveWeight) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.WEIGHT_AT_LEAST_ONE_NON_ZERO);
         } else {
            return this.test(values, begin, length);
         }
      }
   }
}
