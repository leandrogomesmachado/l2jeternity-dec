package org.apache.commons.math.distribution;

public interface WeibullDistribution extends ContinuousDistribution {
   double getShape();

   double getScale();

   @Deprecated
   void setShape(double var1);

   @Deprecated
   void setScale(double var1);
}
