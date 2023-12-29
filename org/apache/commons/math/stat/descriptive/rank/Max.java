package org.apache.commons.math.stat.descriptive.rank;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;

public class Max extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -5593383832225844641L;
   private long n;
   private double value;

   public Max() {
      this.n = 0L;
      this.value = Double.NaN;
   }

   public Max(Max original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      if (d > this.value || Double.isNaN(this.value)) {
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
      double max = Double.NaN;
      if (this.test(values, begin, length)) {
         max = values[begin];

         for(int i = begin; i < begin + length; ++i) {
            if (!Double.isNaN(values[i])) {
               max = max > values[i] ? max : values[i];
            }
         }
      }

      return max;
   }

   public Max copy() {
      Max result = new Max();
      copy(this, result);
      return result;
   }

   public static void copy(Max source, Max dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
