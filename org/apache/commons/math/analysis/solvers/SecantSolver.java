package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class SecantSolver extends UnivariateRealSolverImpl {
   @Deprecated
   public SecantSolver(UnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   @Deprecated
   public SecantSolver() {
      super(100, 1.0E-6);
   }

   @Deprecated
   @Override
   public double solve(double min, double max) throws ConvergenceException, FunctionEvaluationException {
      return this.solve(this.f, min, max);
   }

   @Deprecated
   @Override
   public double solve(double min, double max, double initial) throws ConvergenceException, FunctionEvaluationException {
      return this.solve(this.f, min, max, initial);
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max, initial);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(f, min, max);
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.clearResult();
      this.verifyInterval(min, max);
      double x0 = min;
      double x1 = max;
      double y0 = f.value(min);
      double y1 = f.value(max);
      if (y0 * y1 >= 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SAME_SIGN_AT_ENDPOINTS, min, max, y0, y1);
      } else {
         double x2 = min;
         double y2 = y0;
         double oldDelta = min - max;

         for(int i = 0; i < this.maximalIterationCount; ++i) {
            if (FastMath.abs(y2) < FastMath.abs(y1)) {
               x0 = x1;
               x1 = x2;
               x2 = x0;
               y0 = y1;
               y1 = y2;
               y2 = y0;
            }

            if (FastMath.abs(y1) <= this.functionValueAccuracy) {
               this.setResult(x1, i);
               return this.result;
            }

            if (FastMath.abs(oldDelta) < FastMath.max(this.relativeAccuracy * FastMath.abs(x1), this.absoluteAccuracy)) {
               this.setResult(x1, i);
               return this.result;
            }

            double delta;
            if (FastMath.abs(y1) > FastMath.abs(y0)) {
               delta = 0.5 * oldDelta;
            } else {
               delta = (x0 - x1) / (1.0 - y0 / y1);
               if (delta / oldDelta > 1.0) {
                  delta = 0.5 * oldDelta;
               }
            }

            x0 = x1;
            y0 = y1;
            x1 += delta;
            y1 = f.value(x1);
            if (y1 > 0.0 == y2 > 0.0) {
               x2 = x0;
               y2 = y0;
            }

            oldDelta = x2 - x1;
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }
}
