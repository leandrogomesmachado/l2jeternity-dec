package org.apache.commons.math.stat.descriptive;

import org.apache.commons.math.linear.RealMatrix;

public interface StatisticalMultivariateSummary {
   int getDimension();

   double[] getMean();

   RealMatrix getCovariance();

   double[] getStandardDeviation();

   double[] getMax();

   double[] getMin();

   long getN();

   double[] getGeometricMean();

   double[] getSum();

   double[] getSumSq();

   double[] getSumLog();
}
