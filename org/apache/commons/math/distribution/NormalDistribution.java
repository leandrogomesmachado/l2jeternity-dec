package org.apache.commons.math.distribution;

public interface NormalDistribution extends ContinuousDistribution, HasDensity<Double> {
   double getMean();

   @Deprecated
   void setMean(double var1);

   double getStandardDeviation();

   @Deprecated
   void setStandardDeviation(double var1);

   double density(Double var1);
}
