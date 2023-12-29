package org.apache.commons.math.stat.descriptive.summary;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.util.FastMath;

public class SumOfLogs extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -370076995648386763L;
   private int n;
   private double value;

   public SumOfLogs() {
      this.value = 0.0;
      this.n = 0;
   }

   public SumOfLogs(SumOfLogs original) {
      copy(original, this);
   }

   @Override
   public void increment(double d) {
      this.value += FastMath.log(d);
      ++this.n;
   }

   @Override
   public double getResult() {
      return this.n > 0 ? this.value : Double.NaN;
   }

   @Override
   public long getN() {
      return (long)this.n;
   }

   @Override
   public void clear() {
      this.value = 0.0;
      this.n = 0;
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      double sumLog = Double.NaN;
      if (this.test(values, begin, length)) {
         sumLog = 0.0;

         for(int i = begin; i < begin + length; ++i) {
            sumLog += FastMath.log(values[i]);
         }
      }

      return sumLog;
   }

   public SumOfLogs copy() {
      SumOfLogs result = new SumOfLogs();
      copy(this, result);
      return result;
   }

   public static void copy(SumOfLogs source, SumOfLogs dest) {
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }
}
