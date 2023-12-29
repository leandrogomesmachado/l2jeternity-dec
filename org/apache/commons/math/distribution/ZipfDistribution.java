package org.apache.commons.math.distribution;

public interface ZipfDistribution extends IntegerDistribution {
   int getNumberOfElements();

   @Deprecated
   void setNumberOfElements(int var1);

   double getExponent();

   @Deprecated
   void setExponent(double var1);
}
