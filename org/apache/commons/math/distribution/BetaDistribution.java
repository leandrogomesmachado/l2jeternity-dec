package org.apache.commons.math.distribution;

import org.apache.commons.math.MathException;

public interface BetaDistribution extends ContinuousDistribution, HasDensity<Double> {
   @Deprecated
   void setAlpha(double var1);

   double getAlpha();

   @Deprecated
   void setBeta(double var1);

   double getBeta();

   double density(Double var1) throws MathException;
}
