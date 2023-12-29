package org.apache.commons.math.distribution;

public interface CauchyDistribution extends ContinuousDistribution {
   double getMedian();

   double getScale();

   @Deprecated
   void setMedian(double var1);

   @Deprecated
   void setScale(double var1);
}
