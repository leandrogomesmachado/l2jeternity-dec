package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class SimpsonIntegrator extends UnivariateRealIntegratorImpl {
   @Deprecated
   public SimpsonIntegrator(UnivariateRealFunction f) {
      super(f, 64);
   }

   public SimpsonIntegrator() {
      super(64);
   }

   @Deprecated
   @Override
   public double integrate(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException, IllegalArgumentException {
      return this.integrate(this.f, min, max);
   }

   @Override
   public double integrate(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException, IllegalArgumentException {
      this.clearResult();
      this.verifyInterval(min, max);
      this.verifyIterationCount();
      TrapezoidIntegrator qtrap = new TrapezoidIntegrator();
      if (this.minimalIterationCount == 1) {
         double s = (4.0 * qtrap.stage(f, min, max, 1) - qtrap.stage(f, min, max, 0)) / 3.0;
         this.setResult(s, 1);
         return this.result;
      } else {
         double olds = 0.0;
         double oldt = qtrap.stage(f, min, max, 0);

         for(int i = 1; i <= this.maximalIterationCount; ++i) {
            double t = qtrap.stage(f, min, max, i);
            double s = (4.0 * t - oldt) / 3.0;
            if (i >= this.minimalIterationCount) {
               double delta = FastMath.abs(s - olds);
               double rLimit = this.relativeAccuracy * (FastMath.abs(olds) + FastMath.abs(s)) * 0.5;
               if (delta <= rLimit || delta <= this.absoluteAccuracy) {
                  this.setResult(s, i);
                  return this.result;
               }
            }

            olds = s;
            oldt = t;
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }

   @Override
   protected void verifyIterationCount() throws IllegalArgumentException {
      super.verifyIterationCount();
      if (this.maximalIterationCount > 64) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_ITERATIONS_LIMITS, 0, 64);
      }
   }
}
