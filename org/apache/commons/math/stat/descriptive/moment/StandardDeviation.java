package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.util.FastMath;

public class StandardDeviation extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = 5728716329662425188L;
   private Variance variance = null;

   public StandardDeviation() {
      this.variance = new Variance();
   }

   public StandardDeviation(SecondMoment m2) {
      this.variance = new Variance(m2);
   }

   public StandardDeviation(StandardDeviation original) {
      copy(original, this);
   }

   public StandardDeviation(boolean isBiasCorrected) {
      this.variance = new Variance(isBiasCorrected);
   }

   public StandardDeviation(boolean isBiasCorrected, SecondMoment m2) {
      this.variance = new Variance(isBiasCorrected, m2);
   }

   @Override
   public void increment(double d) {
      this.variance.increment(d);
   }

   @Override
   public long getN() {
      return this.variance.getN();
   }

   @Override
   public double getResult() {
      return FastMath.sqrt(this.variance.getResult());
   }

   @Override
   public void clear() {
      this.variance.clear();
   }

   @Override
   public double evaluate(double[] values) {
      return FastMath.sqrt(this.variance.evaluate(values));
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      return FastMath.sqrt(this.variance.evaluate(values, begin, length));
   }

   public double evaluate(double[] values, double mean, int begin, int length) {
      return FastMath.sqrt(this.variance.evaluate(values, mean, begin, length));
   }

   public double evaluate(double[] values, double mean) {
      return FastMath.sqrt(this.variance.evaluate(values, mean));
   }

   public boolean isBiasCorrected() {
      return this.variance.isBiasCorrected();
   }

   public void setBiasCorrected(boolean isBiasCorrected) {
      this.variance.setBiasCorrected(isBiasCorrected);
   }

   public StandardDeviation copy() {
      StandardDeviation result = new StandardDeviation();
      copy(this, result);
      return result;
   }

   public static void copy(StandardDeviation source, StandardDeviation dest) {
      dest.setData(source.getDataRef());
      dest.variance = source.variance.copy();
   }
}
