package org.apache.commons.math.stat.descriptive;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class SummaryStatistics implements StatisticalSummary, Serializable {
   private static final long serialVersionUID = -2021321786743555871L;
   protected long n = 0L;
   protected SecondMoment secondMoment = new SecondMoment();
   protected Sum sum = new Sum();
   protected SumOfSquares sumsq = new SumOfSquares();
   protected Min min = new Min();
   protected Max max = new Max();
   protected SumOfLogs sumLog = new SumOfLogs();
   protected GeometricMean geoMean = new GeometricMean(this.sumLog);
   protected Mean mean = new Mean();
   protected Variance variance = new Variance();
   private StorelessUnivariateStatistic sumImpl = this.sum;
   private StorelessUnivariateStatistic sumsqImpl = this.sumsq;
   private StorelessUnivariateStatistic minImpl = this.min;
   private StorelessUnivariateStatistic maxImpl = this.max;
   private StorelessUnivariateStatistic sumLogImpl = this.sumLog;
   private StorelessUnivariateStatistic geoMeanImpl = this.geoMean;
   private StorelessUnivariateStatistic meanImpl = this.mean;
   private StorelessUnivariateStatistic varianceImpl = this.variance;

   public SummaryStatistics() {
   }

   public SummaryStatistics(SummaryStatistics original) {
      copy(original, this);
   }

   public StatisticalSummary getSummary() {
      return new StatisticalSummaryValues(this.getMean(), this.getVariance(), this.getN(), this.getMax(), this.getMin(), this.getSum());
   }

   public void addValue(double value) {
      this.sumImpl.increment(value);
      this.sumsqImpl.increment(value);
      this.minImpl.increment(value);
      this.maxImpl.increment(value);
      this.sumLogImpl.increment(value);
      this.secondMoment.increment(value);
      if (!(this.meanImpl instanceof Mean)) {
         this.meanImpl.increment(value);
      }

      if (!(this.varianceImpl instanceof Variance)) {
         this.varianceImpl.increment(value);
      }

      if (!(this.geoMeanImpl instanceof GeometricMean)) {
         this.geoMeanImpl.increment(value);
      }

      ++this.n;
   }

   @Override
   public long getN() {
      return this.n;
   }

   @Override
   public double getSum() {
      return this.sumImpl.getResult();
   }

   public double getSumsq() {
      return this.sumsqImpl.getResult();
   }

   @Override
   public double getMean() {
      return this.mean == this.meanImpl ? new Mean(this.secondMoment).getResult() : this.meanImpl.getResult();
   }

   @Override
   public double getStandardDeviation() {
      double stdDev = Double.NaN;
      if (this.getN() > 0L) {
         if (this.getN() > 1L) {
            stdDev = FastMath.sqrt(this.getVariance());
         } else {
            stdDev = 0.0;
         }
      }

      return stdDev;
   }

   @Override
   public double getVariance() {
      return this.varianceImpl == this.variance ? new Variance(this.secondMoment).getResult() : this.varianceImpl.getResult();
   }

   @Override
   public double getMax() {
      return this.maxImpl.getResult();
   }

   @Override
   public double getMin() {
      return this.minImpl.getResult();
   }

   public double getGeometricMean() {
      return this.geoMeanImpl.getResult();
   }

   public double getSumOfLogs() {
      return this.sumLogImpl.getResult();
   }

   public double getSecondMoment() {
      return this.secondMoment.getResult();
   }

   @Override
   public String toString() {
      StringBuilder outBuffer = new StringBuilder();
      String endl = "\n";
      outBuffer.append("SummaryStatistics:").append(endl);
      outBuffer.append("n: ").append(this.getN()).append(endl);
      outBuffer.append("min: ").append(this.getMin()).append(endl);
      outBuffer.append("max: ").append(this.getMax()).append(endl);
      outBuffer.append("mean: ").append(this.getMean()).append(endl);
      outBuffer.append("geometric mean: ").append(this.getGeometricMean()).append(endl);
      outBuffer.append("variance: ").append(this.getVariance()).append(endl);
      outBuffer.append("sum of squares: ").append(this.getSumsq()).append(endl);
      outBuffer.append("standard deviation: ").append(this.getStandardDeviation()).append(endl);
      outBuffer.append("sum of logs: ").append(this.getSumOfLogs()).append(endl);
      return outBuffer.toString();
   }

   public void clear() {
      this.n = 0L;
      this.minImpl.clear();
      this.maxImpl.clear();
      this.sumImpl.clear();
      this.sumLogImpl.clear();
      this.sumsqImpl.clear();
      this.geoMeanImpl.clear();
      this.secondMoment.clear();
      if (this.meanImpl != this.mean) {
         this.meanImpl.clear();
      }

      if (this.varianceImpl != this.variance) {
         this.varianceImpl.clear();
      }
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof SummaryStatistics)) {
         return false;
      } else {
         SummaryStatistics stat = (SummaryStatistics)object;
         return MathUtils.equalsIncludingNaN(stat.getGeometricMean(), this.getGeometricMean())
            && MathUtils.equalsIncludingNaN(stat.getMax(), this.getMax())
            && MathUtils.equalsIncludingNaN(stat.getMean(), this.getMean())
            && MathUtils.equalsIncludingNaN(stat.getMin(), this.getMin())
            && MathUtils.equalsIncludingNaN((float)stat.getN(), (float)this.getN())
            && MathUtils.equalsIncludingNaN(stat.getSum(), this.getSum())
            && MathUtils.equalsIncludingNaN(stat.getSumsq(), this.getSumsq())
            && MathUtils.equalsIncludingNaN(stat.getVariance(), this.getVariance());
      }
   }

   @Override
   public int hashCode() {
      int result = 31 + MathUtils.hash(this.getGeometricMean());
      result = result * 31 + MathUtils.hash(this.getGeometricMean());
      result = result * 31 + MathUtils.hash(this.getMax());
      result = result * 31 + MathUtils.hash(this.getMean());
      result = result * 31 + MathUtils.hash(this.getMin());
      result = result * 31 + MathUtils.hash((double)this.getN());
      result = result * 31 + MathUtils.hash(this.getSum());
      result = result * 31 + MathUtils.hash(this.getSumsq());
      return result * 31 + MathUtils.hash(this.getVariance());
   }

   public StorelessUnivariateStatistic getSumImpl() {
      return this.sumImpl;
   }

   public void setSumImpl(StorelessUnivariateStatistic sumImpl) {
      this.checkEmpty();
      this.sumImpl = sumImpl;
   }

   public StorelessUnivariateStatistic getSumsqImpl() {
      return this.sumsqImpl;
   }

   public void setSumsqImpl(StorelessUnivariateStatistic sumsqImpl) {
      this.checkEmpty();
      this.sumsqImpl = sumsqImpl;
   }

   public StorelessUnivariateStatistic getMinImpl() {
      return this.minImpl;
   }

   public void setMinImpl(StorelessUnivariateStatistic minImpl) {
      this.checkEmpty();
      this.minImpl = minImpl;
   }

   public StorelessUnivariateStatistic getMaxImpl() {
      return this.maxImpl;
   }

   public void setMaxImpl(StorelessUnivariateStatistic maxImpl) {
      this.checkEmpty();
      this.maxImpl = maxImpl;
   }

   public StorelessUnivariateStatistic getSumLogImpl() {
      return this.sumLogImpl;
   }

   public void setSumLogImpl(StorelessUnivariateStatistic sumLogImpl) {
      this.checkEmpty();
      this.sumLogImpl = sumLogImpl;
      this.geoMean.setSumLogImpl(sumLogImpl);
   }

   public StorelessUnivariateStatistic getGeoMeanImpl() {
      return this.geoMeanImpl;
   }

   public void setGeoMeanImpl(StorelessUnivariateStatistic geoMeanImpl) {
      this.checkEmpty();
      this.geoMeanImpl = geoMeanImpl;
   }

   public StorelessUnivariateStatistic getMeanImpl() {
      return this.meanImpl;
   }

   public void setMeanImpl(StorelessUnivariateStatistic meanImpl) {
      this.checkEmpty();
      this.meanImpl = meanImpl;
   }

   public StorelessUnivariateStatistic getVarianceImpl() {
      return this.varianceImpl;
   }

   public void setVarianceImpl(StorelessUnivariateStatistic varianceImpl) {
      this.checkEmpty();
      this.varianceImpl = varianceImpl;
   }

   private void checkEmpty() {
      if (this.n > 0L) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.VALUES_ADDED_BEFORE_CONFIGURING_STATISTIC, this.n);
      }
   }

   public SummaryStatistics copy() {
      SummaryStatistics result = new SummaryStatistics();
      copy(this, result);
      return result;
   }

   public static void copy(SummaryStatistics source, SummaryStatistics dest) {
      dest.maxImpl = source.maxImpl.copy();
      dest.meanImpl = source.meanImpl.copy();
      dest.minImpl = source.minImpl.copy();
      dest.sumImpl = source.sumImpl.copy();
      dest.varianceImpl = source.varianceImpl.copy();
      dest.sumLogImpl = source.sumLogImpl.copy();
      dest.sumsqImpl = source.sumsqImpl.copy();
      if (source.getGeoMeanImpl() instanceof GeometricMean) {
         dest.geoMeanImpl = new GeometricMean((SumOfLogs)dest.sumLogImpl);
      } else {
         dest.geoMeanImpl = source.geoMeanImpl.copy();
      }

      SecondMoment.copy(source.secondMoment, dest.secondMoment);
      dest.n = source.n;
      if (source.geoMean == source.geoMeanImpl) {
         dest.geoMean = (GeometricMean)dest.geoMeanImpl;
      } else {
         GeometricMean.copy(source.geoMean, dest.geoMean);
      }

      if (source.max == source.maxImpl) {
         dest.max = (Max)dest.maxImpl;
      } else {
         Max.copy(source.max, dest.max);
      }

      if (source.mean == source.meanImpl) {
         dest.mean = (Mean)dest.meanImpl;
      } else {
         Mean.copy(source.mean, dest.mean);
      }

      if (source.min == source.minImpl) {
         dest.min = (Min)dest.minImpl;
      } else {
         Min.copy(source.min, dest.min);
      }

      if (source.sum == source.sumImpl) {
         dest.sum = (Sum)dest.sumImpl;
      } else {
         Sum.copy(source.sum, dest.sum);
      }

      if (source.variance == source.varianceImpl) {
         dest.variance = (Variance)dest.varianceImpl;
      } else {
         Variance.copy(source.variance, dest.variance);
      }

      if (source.sumLog == source.sumLogImpl) {
         dest.sumLog = (SumOfLogs)dest.sumLogImpl;
      } else {
         SumOfLogs.copy(source.sumLog, dest.sumLog);
      }

      if (source.sumsq == source.sumsqImpl) {
         dest.sumsq = (SumOfSquares)dest.sumsqImpl;
      } else {
         SumOfSquares.copy(source.sumsq, dest.sumsq);
      }
   }
}
