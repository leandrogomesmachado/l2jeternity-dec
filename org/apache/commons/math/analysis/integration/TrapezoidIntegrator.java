package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class TrapezoidIntegrator extends UnivariateRealIntegratorImpl {
   private double s;

   @Deprecated
   public TrapezoidIntegrator(UnivariateRealFunction f) {
      super(f, 64);
   }

   public TrapezoidIntegrator() {
      super(64);
   }

   double stage(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException {
      if (n == 0) {
         this.s = 0.5 * (max - min) * (f.value(min) + f.value(max));
         return this.s;
      } else {
         long np = 1L << n - 1;
         double sum = 0.0;
         double spacing = (max - min) / (double)np;
         double x = min + 0.5 * spacing;

         for(long i = 0L; i < np; ++i) {
            sum += f.value(x);
            x += spacing;
         }

         this.s = 0.5 * (this.s + sum * spacing);
         return this.s;
      }
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
      double oldt = this.stage(f, min, max, 0);

      for(int i = 1; i <= this.maximalIterationCount; ++i) {
         double t = this.stage(f, min, max, i);
         if (i >= this.minimalIterationCount) {
            double delta = FastMath.abs(t - oldt);
            double rLimit = this.relativeAccuracy * (FastMath.abs(oldt) + FastMath.abs(t)) * 0.5;
            if (delta <= rLimit || delta <= this.absoluteAccuracy) {
               this.setResult(t, i);
               return this.result;
            }
         }

         oldt = t;
      }

      throw new MaxIterationsExceededException(this.maximalIterationCount);
   }

   @Override
   protected void verifyIterationCount() throws IllegalArgumentException {
      super.verifyIterationCount();
      if (this.maximalIterationCount > 64) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_ITERATIONS_LIMITS, 0, 64);
      }
   }
}
