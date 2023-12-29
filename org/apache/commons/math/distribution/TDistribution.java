package org.apache.commons.math.distribution;

public interface TDistribution extends ContinuousDistribution {
   @Deprecated
   void setDegreesOfFreedom(double var1);

   double getDegreesOfFreedom();
}
