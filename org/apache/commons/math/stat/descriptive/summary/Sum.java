package org.apache.commons.math.stat.descriptive.summary;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;

public class Sum extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -8231831954703408316L;
   private long n;
   private double value;

   public Sum() {
      this.n = 0L;
      this.value = Double.NaN;
   }

   public Sum(Sum original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.n == 0L) {
         this.value = d;
      } else {
         this.value += d;
      }

      ++this.n;
   }

   @Override
   public double getResult() {
      return this.value;
   }

   @Override
   public long getN() {
      return this.n;
   }

   @Override
   public void clear() {
      this.value = Double.NaN;
      this.n = 0L;
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      double sum = Double.NaN;
      if (this.test(values, begin, length)) {
         sum = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            sum += values[i];
         }
      }

      return sum;
   }

   public double evaluate(double[] values, double[] weights, int begin, int length) {
      double sum = Double.NaN;
      if (this.test(values, weights, begin, length)) {
         sum = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            sum += values[i] * weights[i];
         }
      }

      return sum;
   }

   public double evaluate(double[] values, double[] weights) {
      return this.evaluate(values, weights, 0, values.length);
   }

   public Sum copy() {
      Sum result = new Sum();
      copy(this, result);
      return result;
   }

   public static void copy(Sum source, Sum dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
