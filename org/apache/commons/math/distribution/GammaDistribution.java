package org.apache.commons.math.distribution;

public interface GammaDistribution extends ContinuousDistribution, HasDensity<Double> {
   @Deprecated
   void setAlpha(double var1);

   double getAlpha();

   @Deprecated
   void setBeta(double var1);

   double getBeta();

   double density(Double var1);
}
