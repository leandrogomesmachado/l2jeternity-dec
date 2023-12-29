package org.apache.commons.math.distribution;

public interface ExponentialDistribution extends ContinuousDistribution, HasDensity<Double> {
   @Deprecated
   void setMean(double var1);

   double getMean();

   double density(Double var1);
}
