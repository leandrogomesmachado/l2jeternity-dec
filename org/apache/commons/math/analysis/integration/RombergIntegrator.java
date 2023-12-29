package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class RombergIntegrator extends UnivariateRealIntegratorImpl {
   @Deprecated
   public RombergIntegrator(UnivariateRealFunction f) {
      super(f, 32);
   }

   public RombergIntegrator() {
      super(32);
   }

   @Deprecated
   @Override
   public double integrate(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException, IllegalArgumentException {
      return this.integrate(this.f, min, max);
   }

   @Override
   public double integrate(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException, IllegalArgumentException {
      int m = this.maximalIterationCount + 1;
      double[] previousRow = new double[m];
      double[] currentRow = new double[m];
      this.clearResult();
      this.verifyInterval(min, max);
      this.verifyIterationCount();
      TrapezoidIntegrator qtrap = new TrapezoidIntegrator();
      currentRow[0] = qtrap.stage(f, min, max, 0);
      double olds = currentRow[0];

      for(int i = 1; i <= this.maximalIterationCount; ++i) {
         double[] tmpRow = previousRow;
         previousRow = currentRow;
         currentRow = tmpRow;
         tmpRow[0] = qtrap.stage(f, min, max, i);

         for(int j = 1; j <= i; ++j) {
            double r = (double)((1L << 2 * j) - 1L);
            double tIJm1 = currentRow[j - 1];
            currentRow[j] = tIJm1 + (tIJm1 - previousRow[j - 1]) / r;
         }

         double s = currentRow[i];
         if (i >= this.minimalIterationCount) {
            double delta = FastMath.abs(s - olds);
            double rLimit = this.relativeAccuracy * (FastMath.abs(olds) + FastMath.abs(s)) * 0.5;
            if (delta <= rLimit || delta <= this.absoluteAccuracy) {
               this.setResult(s, i);
               return this.result;
            }
         }

         olds = s;
      }

      throw new MaxIterationsExceededException(this.maximalIterationCount);
   }

   @Override
   protected void verifyIterationCount() throws IllegalArgumentException {
      super.verifyIterationCount();
      if (this.maximalIterationCount > 32) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_ITERATIONS_LIMITS, 0, 32);
      }
   }
}
