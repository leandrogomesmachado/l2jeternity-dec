package org.apache.commons.math.stat.descriptive.summary;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;

public class SumOfSquares extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = 1460986908574398008L;
   private long n;
   private double value;

   public SumOfSquares() {
      this.n = 0L;
      this.value = Double.NaN;
   }

   public SumOfSquares(SumOfSquares original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (this.n == 0L) {
         this.value = d * d;
      } else {
         this.value += d * d;
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
      double sumSq = Double.NaN;
      if (this.test(values, begin, length)) {
         sumSq = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            sumSq += values[i] * values[i];
         }
      }

      return sumSq;
   }

   public SumOfSquares copy() {
      SumOfSquares result = new SumOfSquares();
      copy(this, result);
      return result;
   }

   public static void copy(SumOfSquares source, SumOfSquares dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
