package org.apache.commons.math.distribution;

public interface PascalDistribution extends IntegerDistribution {
   int getNumberOfSuccesses();

   double getProbabilityOfSuccess();

   @Deprecated
   void setNumberOfSuccesses(int var1);

   @Deprecated
   void setProbabilityOfSuccess(double var1);
}
