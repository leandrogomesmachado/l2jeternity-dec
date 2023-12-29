package org.apache.commons.math.distribution;

public interface ChiSquaredDistribution extends ContinuousDistribution, HasDensity<Double> {
   @Deprecated
   void setDegreesOfFreedom(double var1);

   double getDegreesOfFreedom();

   double density(Double var1);
}
