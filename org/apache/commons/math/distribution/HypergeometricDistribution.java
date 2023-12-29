package org.apache.commons.math.distribution;

public interface HypergeometricDistribution extends IntegerDistribution {
   int getNumberOfSuccesses();

   int getPopulationSize();

   int getSampleSize();

   @Deprecated
   void setNumberOfSuccesses(int var1);

   @Deprecated
   void setPopulationSize(int var1);

   @Deprecated
   void setSampleSize(int var1);
}
