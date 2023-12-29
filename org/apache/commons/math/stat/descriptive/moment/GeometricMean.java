package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.util.FastMath;

public class GeometricMean extends AbstractStorelessUnivariateStatistic implements Serializable {
   private static final long serialVersionUID = -8178734905303459453L;
   private StorelessUnivariateStatistic sumOfLogs;

   public GeometricMean() {
      this.sumOfLogs = new SumOfLogs();
   }

   public GeometricMean(GeometricMean original) {
      copy(original, this);
   }

   public GeometricMean(SumOfLogs sumOfLogs) {
      this.sumOfLogs = sumOfLogs;
   }

   public GeometricMean copy() {
      GeometricMean result = new GeometricMean();
      copy(this, result);
      return result;
   }

   @Override
   public void increment(double d) {
      this.sumOfLogs.increment(d);
   }

   @Override
   public double getResult() {
      return this.sumOfLogs.getN() > 0L ? FastMath.exp(this.sumOfLogs.getResult() / (double)this.sumOfLogs.getN()) : Double.NaN;
   }

   @Override
   public void clear() {
      this.sumOfLogs.clear();
   }

   @Override
   public double evaluate(double[] values, int begin, int length) {
      return FastMath.exp(this.sumOfLogs.evaluate(values, begin, length) / (double)length);
   }

   @Override
   public long getN() {
      return this.sumOfLogs.getN();
   }

   public void setSumLogImpl(StorelessUnivariateStatistic sumLogImpl) {
      this.checkEmpty();
      this.sumOfLogs = sumLogImpl;
   }

   public StorelessUnivariateStatistic getSumLogImpl() {
      return this.sumOfLogs;
   }

   public static void copy(GeometricMean source, GeometricMean dest) {
      dest.setData(source.getDataRef());
      dest.sumOfLogs = source.sumOfLogs.copy();
   }

   private void checkEmpty() {
      if (this.getN() > 0L) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.VALUES_ADDED_BEFORE_CONFIGURING_STATISTIC, this.getN());
      }
   }
}
