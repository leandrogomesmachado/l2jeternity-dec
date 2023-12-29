package org.apache.commons.math.distribution;

public interface BinomialDistribution extends IntegerDistribution {
   int getNumberOfTrials();

   double getProbabilityOfSuccess();

   @Deprecated
   void setNumberOfTrials(int var1);

   @Deprecated
   void setProbabilityOfSuccess(double var1);
}
