package org.apache.commons.math.stat.descriptive.rank;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;

public class Min extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -2941995784909003131L;
   private long n;
   private double value;

   public Min() {
      this.n = 0L;
      this.value = Double.NaN;
   }

   public Min(Min original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (d < this.value || Double.isNaN(this.value)) {
         this.value = d;
      }

      ++this.n;
   }

   @Override
   public void clear() {
      this.value = Double.NaN;
      this.n = 0L;
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
   public double evaluate(double[] values, int begin, int length) {
      double min = Double.NaN;
      if (this.test(values, begin, length)) {
         min = values[begin];

         for(int i = begin; i < begin + length; ++i) {
            if (!Double.isNaN(values[i])) {
               min = min < values[i] ? min : values[i];
            }
         }
      }

      return min;
   }

   public Min copy() {
      Min result = new Min();
      copy(this, result);
      return result;
   }

   public static void copy(Min source, Min dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
