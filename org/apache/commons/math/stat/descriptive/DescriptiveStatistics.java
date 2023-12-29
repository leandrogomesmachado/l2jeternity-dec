package org.apache.commons.math.stat.descriptive;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Skewness;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.ResizableDoubleArray;

public class DescriptiveStatistics implements StatisticalSummary, Serializable {
   public static final int INFINITE_WINDOW = -1;
   private static final long serialVersionUID = 4133067267405273064L;
   private static final String SET_QUANTILE_METHOD_NAME = "setQuantile";
   protected int windowSize = -1;
   protected ResizableDoubleArray eDA = new ResizableDoubleArray();
   private UnivariateStatistic meanImpl = new Mean();
   private UnivariateStatistic geometricMeanImpl = new GeometricMean();
   private UnivariateStatistic kurtosisImpl = new Kurtosis();
   private UnivariateStatistic maxImpl = new Max();
   private UnivariateStatistic minImpl = new Min();
   private UnivariateStatistic percentileImpl = new Percentile();
   private UnivariateStatistic skewnessImpl = new Skewness();
   private UnivariateStatistic varianceImpl = new Variance();
   private UnivariateStatistic sumsqImpl = new SumOfSquares();
   private UnivariateStatistic sumImpl = new Sum();

   public DescriptiveStatistics() {
   }

   public DescriptiveStatistics(int window) {
      this.setWindowSize(window);
   }

   public DescriptiveStatistics(double[] initialDoubleArray) {
      if (initialDoubleArray != null) {
         this.eDA = new ResizableDoubleArray(initialDoubleArray);
      }
   }

   public DescriptiveStatistics(DescriptiveStatistics original) {
      copy(original, this);
   }

   public void addValue(double v) {
      if (this.windowSize != -1) {
         if (this.getN() == (long)this.windowSize) {
            this.eDA.addElementRolling(v);
         } else if (this.getN() < (long)this.windowSize) {
            this.eDA.addElement(v);
         }
      } else {
         this.eDA.addElement(v);
      }
   }

   public void removeMostRecentValue() {
      this.eDA.discardMostRecentElements(1);
   }

   public double replaceMostRecentValue(double v) {
      return this.eDA.substituteMostRecentElement(v);
   }

   @Override
   public double getMean() {
      return this.apply(this.meanImpl);
   }

   public double getGeometricMean() {
      return this.apply(this.geometricMeanImpl);
   }

   @Override
   public double getVariance() {
      return this.apply(this.varianceImpl);
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

   public double getSkewness() {
      return this.apply(this.skewnessImpl);
   }

   public double getKurtosis() {
      return this.apply(this.kurtosisImpl);
   }

   @Override
   public double getMax() {
      return this.apply(this.maxImpl);
   }

   @Override
   public double getMin() {
      return this.apply(this.minImpl);
   }

   @Override
   public long getN() {
      return (long)this.eDA.getNumElements();
   }

   @Override
   public double getSum() {
      return this.apply(this.sumImpl);
   }

   public double getSumsq() {
      return this.apply(this.sumsqImpl);
   }

   public void clear() {
      this.eDA.clear();
   }

   public int getWindowSize() {
      return this.windowSize;
   }

   public void setWindowSize(int windowSize) {
      if (windowSize < 1 && windowSize != -1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_WINDOW_SIZE, windowSize);
      } else {
         this.windowSize = windowSize;
         if (windowSize != -1 && windowSize < this.eDA.getNumElements()) {
            this.eDA.discardFrontElements(this.eDA.getNumElements() - windowSize);
         }
      }
   }

   public double[] getValues() {
      return this.eDA.getElements();
   }

   public double[] getSortedValues() {
      double[] sort = this.getValues();
      Arrays.sort(sort);
      return sort;
   }

   public double getElement(int index) {
      return this.eDA.getElement(index);
   }

   public double getPercentile(double p) {
      if (this.percentileImpl instanceof Percentile) {
         ((Percentile)this.percentileImpl).setQuantile(p);
      } else {
         try {
            this.percentileImpl.getClass().getMethod("setQuantile", Double.TYPE).invoke(this.percentileImpl, p);
         } catch (NoSuchMethodException var4) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.PERCENTILE_IMPLEMENTATION_UNSUPPORTED_METHOD, this.percentileImpl.getClass().getName(), "setQuantile"
            );
         } catch (IllegalAccessException var5) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.PERCENTILE_IMPLEMENTATION_CANNOT_ACCESS_METHOD, "setQuantile", this.percentileImpl.getClass().getName()
            );
         } catch (InvocationTargetException var6) {
            throw MathRuntimeException.createIllegalArgumentException(var6.getCause());
         }
      }

      return this.apply(this.percentileImpl);
   }

   @Override
   public String toString() {
      StringBuilder outBuffer = new StringBuilder();
      String endl = "\n";
      outBuffer.append("DescriptiveStatistics:").append(endl);
      outBuffer.append("n: ").append(this.getN()).append(endl);
      outBuffer.append("min: ").append(this.getMin()).append(endl);
      outBuffer.append("max: ").append(this.getMax()).append(endl);
      outBuffer.append("mean: ").append(this.getMean()).append(endl);
      outBuffer.append("std dev: ").append(this.getStandardDeviation()).append(endl);
      outBuffer.append("median: ").append(this.getPercentile(50.0)).append(endl);
      outBuffer.append("skewness: ").append(this.getSkewness()).append(endl);
      outBuffer.append("kurtosis: ").append(this.getKurtosis()).append(endl);
      return outBuffer.toString();
   }

   public double apply(UnivariateStatistic stat) {
      return stat.evaluate(this.eDA.getInternalValues(), this.eDA.start(), this.eDA.getNumElements());
   }

   public synchronized UnivariateStatistic getMeanImpl() {
      return this.meanImpl;
   }

   public synchronized void setMeanImpl(UnivariateStatistic meanImpl) {
      this.meanImpl = meanImpl;
   }

   public synchronized UnivariateStatistic getGeometricMeanImpl() {
      return this.geometricMeanImpl;
   }

   public synchronized void setGeometricMeanImpl(UnivariateStatistic geometricMeanImpl) {
      this.geometricMeanImpl = geometricMeanImpl;
   }

   public synchronized UnivariateStatistic getKurtosisImpl() {
      return this.kurtosisImpl;
   }

   public synchronized void setKurtosisImpl(UnivariateStatistic kurtosisImpl) {
      this.kurtosisImpl = kurtosisImpl;
   }

   public synchronized UnivariateStatistic getMaxImpl() {
      return this.maxImpl;
   }

   public synchronized void setMaxImpl(UnivariateStatistic maxImpl) {
      this.maxImpl = maxImpl;
   }

   public synchronized UnivariateStatistic getMinImpl() {
      return this.minImpl;
   }

   public synchronized void setMinImpl(UnivariateStatistic minImpl) {
      this.minImpl = minImpl;
   }

   public synchronized UnivariateStatistic getPercentileImpl() {
      return this.percentileImpl;
   }

   public synchronized void setPercentileImpl(UnivariateStatistic percentileImpl) {
      try {
         percentileImpl.getClass().getMethod("setQuantile", Double.TYPE).invoke(percentileImpl, 50.0);
      } catch (NoSuchMethodException var3) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.PERCENTILE_IMPLEMENTATION_UNSUPPORTED_METHOD, percentileImpl.getClass().getName(), "setQuantile"
         );
      } catch (IllegalAccessException var4) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.PERCENTILE_IMPLEMENTATION_CANNOT_ACCESS_METHOD, "setQuantile", percentileImpl.getClass().getName()
         );
      } catch (InvocationTargetException var5) {
         throw MathRuntimeException.createIllegalArgumentException(var5.getCause());
      }

      this.percentileImpl = percentileImpl;
   }

   public synchronized UnivariateStatistic getSkewnessImpl() {
      return this.skewnessImpl;
   }

   public synchronized void setSkewnessImpl(UnivariateStatistic skewnessImpl) {
      this.skewnessImpl = skewnessImpl;
   }

   public synchronized UnivariateStatistic getVarianceImpl() {
      return this.varianceImpl;
   }

   public synchronized void setVarianceImpl(UnivariateStatistic varianceImpl) {
      this.varianceImpl = varianceImpl;
   }

   public synchronized UnivariateStatistic getSumsqImpl() {
      return this.sumsqImpl;
   }

   public synchronized void setSumsqImpl(UnivariateStatistic sumsqImpl) {
      this.sumsqImpl = sumsqImpl;
   }

   public synchronized UnivariateStatistic getSumImpl() {
      return this.sumImpl;
   }

   public synchronized void setSumImpl(UnivariateStatistic sumImpl) {
      this.sumImpl = sumImpl;
   }

   public DescriptiveStatistics copy() {
      DescriptiveStatistics result = new DescriptiveStatistics();
      copy(this, result);
      return result;
   }

   public static void copy(DescriptiveStatistics source, DescriptiveStatistics dest) {
      dest.eDA = source.eDA.copy();
      dest.windowSize = source.windowSize;
      dest.maxImpl = source.maxImpl.copy();
      dest.meanImpl = source.meanImpl.copy();
      dest.minImpl = source.minImpl.copy();
      dest.sumImpl = source.sumImpl.copy();
      dest.varianceImpl = source.varianceImpl.copy();
      dest.sumsqImpl = source.sumsqImpl.copy();
      dest.geometricMeanImpl = source.geometricMeanImpl.copy();
      dest.kurtosisImpl = source.kurtosisImpl;
      dest.skewnessImpl = source.skewnessImpl;
      dest.percentileImpl = source.percentileImpl;
   }
}
