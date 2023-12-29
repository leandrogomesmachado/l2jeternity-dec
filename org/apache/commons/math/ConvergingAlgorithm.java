package org.apache.commons.math;

@Deprecated
public interface ConvergingAlgorithm {
   void setMaximalIterationCount(int var1);

   int getMaximalIterationCount();

   void resetMaximalIterationCount();

   void setAbsoluteAccuracy(double var1);

   double getAbsoluteAccuracy();

   void resetAbsoluteAccuracy();

   void setRelativeAccuracy(double var1);

   double getRelativeAccuracy();

   void resetRelativeAccuracy();

   int getIterationCount();
}
