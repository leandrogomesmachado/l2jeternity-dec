package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

public interface PoissonDistribution extends IntegerDistribution {
   double getMean();

   @Deprecated
   void setMean(double var1);

   double normalApproximateProbability(int var1) throws MathException;
}
