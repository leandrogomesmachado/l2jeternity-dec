package org.apache.commons.math.stat.descriptive;

public interface StorelessUnivariateStatistic extends UnivariateStatistic {
   void increment(double var1);

   void incrementAll(double[] var1);

   void incrementAll(double[] var1, int var2, int var3);

   double getResult();

   long getN();

   void clear();

   StorelessUnivariateStatistic copy();
}
