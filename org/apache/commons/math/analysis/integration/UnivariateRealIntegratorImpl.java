package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.ConvergingAlgorithmImpl;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class UnivariateRealIntegratorImpl extends ConvergingAlgorithmImpl implements UnivariateRealIntegrator {
   private static final long serialVersionUID = 6248808456637441533L;
   protected int minimalIterationCount;
   protected int defaultMinimalIterationCount;
   protected boolean resultComputed = false;
   protected double result;
   @Deprecated
   protected UnivariateRealFunction f;

   @Deprecated
   protected UnivariateRealIntegratorImpl(UnivariateRealFunction f, int defaultMaximalIterationCount) throws IllegalArgumentException {
      super(defaultMaximalIterationCount, 1.0E-15);
      if (f == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION);
      } else {
         this.f = f;
         this.setRelativeAccuracy(1.0E-6);
         this.defaultMinimalIterationCount = 3;
         this.minimalIterationCount = this.defaultMinimalIterationCount;
         this.verifyIterationCount();
      }
   }

   protected UnivariateRealIntegratorImpl(int defaultMaximalIterationCount) throws IllegalArgumentException {
      super(defaultMaximalIterationCount, 1.0E-15);
      this.setRelativeAccuracy(1.0E-6);
      this.defaultMinimalIterationCount = 3;
      this.minimalIterationCount = this.defaultMinimalIterationCount;
      this.verifyIterationCount();
   }

   @Override
   public double getResult() throws IllegalStateException {
      if (this.resultComputed) {
         return this.result;
      } else {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_RESULT_AVAILABLE);
      }
   }

   protected final void setResult(double newResult, int iterationCount) {
      this.result = newResult;
      this.iterationCount = iterationCount;
      this.resultComputed = true;
   }

   protected final void clearResult() {
      this.iterationCount = 0;
      this.resultComputed = false;
   }

   @Override
   public void setMinimalIterationCount(int count) {
      this.minimalIterationCount = count;
   }

   @Override
   public int getMinimalIterationCount() {
      return this.minimalIterationCount;
   }

   @Override
   public void resetMinimalIterationCount() {
      this.minimalIterationCount = this.defaultMinimalIterationCount;
   }

   protected void verifyInterval(double lower, double upper) throws IllegalArgumentException {
      if (lower >= upper) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL, lower, upper);
      }
   }

   protected void verifyIterationCount() throws IllegalArgumentException {
      if (this.minimalIterationCount <= 0 || this.maximalIterationCount <= this.minimalIterationCount) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.INVALID_ITERATIONS_LIMITS, this.minimalIterationCount, this.maximalIterationCount
         );
      }
   }
}
