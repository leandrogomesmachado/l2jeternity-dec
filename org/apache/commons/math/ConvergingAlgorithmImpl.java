package org.apache.commons.math;

/** @deprecated */
public abstract class ConvergingAlgorithmImpl implements ConvergingAlgorithm {
   protected double absoluteAccuracy;
   protected double relativeAccuracy;
   protected int maximalIterationCount;
   protected double defaultAbsoluteAccuracy;
   protected double defaultRelativeAccuracy;
   protected int defaultMaximalIterationCount;
   protected int iterationCount;

   @Deprecated
   protected ConvergingAlgorithmImpl(int defaultMaximalIterationCount, double defaultAbsoluteAccuracy) {
      this.defaultAbsoluteAccuracy = defaultAbsoluteAccuracy;
      this.defaultRelativeAccuracy = 1.0E-14;
      this.absoluteAccuracy = defaultAbsoluteAccuracy;
      this.relativeAccuracy = this.defaultRelativeAccuracy;
      this.defaultMaximalIterationCount = defaultMaximalIterationCount;
      this.maximalIterationCount = defaultMaximalIterationCount;
      this.iterationCount = 0;
   }

   @Deprecated
   protected ConvergingAlgorithmImpl() {
   }

   @Override
   public int getIterationCount() {
      return this.iterationCount;
   }

   @Override
   public void setAbsoluteAccuracy(double accuracy) {
      this.absoluteAccuracy = accuracy;
   }

   @Override
   public double getAbsoluteAccuracy() {
      return this.absoluteAccuracy;
   }

   @Override
   public void resetAbsoluteAccuracy() {
      this.absoluteAccuracy = this.defaultAbsoluteAccuracy;
   }

   @Override
   public void setMaximalIterationCount(int count) {
      this.maximalIterationCount = count;
   }

   @Override
   public int getMaximalIterationCount() {
      return this.maximalIterationCount;
   }

   @Override
   public void resetMaximalIterationCount() {
      this.maximalIterationCount = this.defaultMaximalIterationCount;
   }

   @Override
   public void setRelativeAccuracy(double accuracy) {
      this.relativeAccuracy = accuracy;
   }

   @Override
   public double getRelativeAccuracy() {
      return this.relativeAccuracy;
   }

   @Override
   public void resetRelativeAccuracy() {
      this.relativeAccuracy = this.defaultRelativeAccuracy;
   }

   protected void resetIterationsCounter() {
      this.iterationCount = 0;
   }

   protected void incrementIterationsCounter() throws MaxIterationsExceededException {
      if (++this.iterationCount > this.maximalIterationCount) {
         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }
}
