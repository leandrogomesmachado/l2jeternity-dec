package org.apache.commons.math.distribution;

public interface FDistribution extends ContinuousDistribution {
   @Deprecated
   void setNumeratorDegreesOfFreedom(double var1);

   double getNumeratorDegreesOfFreedom();

   @Deprecated
   void setDenominatorDegreesOfFreedom(double var1);

   double getDenominatorDegreesOfFreedom();
}
