package org.apache.commons.math.stat.descriptive;

public class SynchronizedSummaryStatistics extends SummaryStatistics {
   private static final long serialVersionUID = 1909861009042253704L;

   public SynchronizedSummaryStatistics() {
   }

   public SynchronizedSummaryStatistics(SynchronizedSummaryStatistics original) {
      copy(original, this);
   }

   @Override
   public synchronized StatisticalSummary getSummary() {
      return super.getSummary();
   }

   @Override
   public synchronized void addValue(double value) {
      super.addValue(value);
   }

   @Override
   public synchronized long getN() {
      return super.getN();
   }

   @Override
   public synchronized double getSum() {
      return super.getSum();
   }

   @Override
   public synchronized double getSumsq() {
      return super.getSumsq();
   }

   @Override
   public synchronized double getMean() {
      return super.getMean();
   }

   @Override
   public synchronized double getStandardDeviation() {
      return super.getStandardDeviation();
   }

   @Override
   public synchronized double getVariance() {
      return super.getVariance();
   }

   @Override
   public synchronized double getMax() {
      return super.getMax();
   }

   @Override
   public synchronized double getMin() {
      return super.getMin();
   }

   @Override
   public synchronized double getGeometricMean() {
      return super.getGeometricMean();
   }

   @Override
   public synchronized String toString() {
      return super.toString();
   }

   @Override
   public synchronized void clear() {
      super.clear();
   }

   @Override
   public synchronized boolean equals(Object object) {
      return super.equals(object);
   }

   @Override
   public synchronized int hashCode() {
      return super.hashCode();
   }

   @Override
   public synchronized StorelessUnivariateStatistic getSumImpl() {
      return super.getSumImpl();
   }

   @Override
   public synchronized void setSumImpl(StorelessUnivariateStatistic sumImpl) {
      super.setSumImpl(sumImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getSumsqImpl() {
      return super.getSumsqImpl();
   }

   @Override
   public synchronized void setSumsqImpl(StorelessUnivariateStatistic sumsqImpl) {
      super.setSumsqImpl(sumsqImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getMinImpl() {
      return super.getMinImpl();
   }

   @Override
   public synchronized void setMinImpl(StorelessUnivariateStatistic minImpl) {
      super.setMinImpl(minImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getMaxImpl() {
      return super.getMaxImpl();
   }

   @Override
   public synchronized void setMaxImpl(StorelessUnivariateStatistic maxImpl) {
      super.setMaxImpl(maxImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getSumLogImpl() {
      return super.getSumLogImpl();
   }

   @Override
   public synchronized void setSumLogImpl(StorelessUnivariateStatistic sumLogImpl) {
      super.setSumLogImpl(sumLogImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getGeoMeanImpl() {
      return super.getGeoMeanImpl();
   }

   @Override
   public synchronized void setGeoMeanImpl(StorelessUnivariateStatistic geoMeanImpl) {
      super.setGeoMeanImpl(geoMeanImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getMeanImpl() {
      return super.getMeanImpl();
   }

   @Override
   public synchronized void setMeanImpl(StorelessUnivariateStatistic meanImpl) {
      super.setMeanImpl(meanImpl);
   }

   @Override
   public synchronized StorelessUnivariateStatistic getVarianceImpl() {
      return super.getVarianceImpl();
   }

   @Override
   public synchronized void setVarianceImpl(StorelessUnivariateStatistic varianceImpl) {
      super.setVarianceImpl(varianceImpl);
   }

   public synchronized SynchronizedSummaryStatistics copy() {
      SynchronizedSummaryStatistics result = new SynchronizedSummaryStatistics();
      copy(this, result);
      return result;
   }

   public static void copy(SynchronizedSummaryStatistics source, SynchronizedSummaryStatistics dest) {
      synchronized(source) {
         synchronized(dest) {
            SummaryStatistics.copy(source, dest);
         }
      }
   }
}
