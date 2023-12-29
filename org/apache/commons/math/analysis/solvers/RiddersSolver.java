package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class RiddersSolver extends UnivariateRealSolverImpl {
   @Deprecated
   public RiddersSolver(UnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   @Deprecated
   public RiddersSolver() {
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
      if (f.value(min) == 0.0) {
         return min;
      } else if (f.value(max) == 0.0) {
         return max;
      } else if (f.value(initial) == 0.0) {
         return initial;
      } else {
         this.verifyBracketing(min, max, f);
         this.verifySequence(min, initial, max);
         return this.isBracketing(min, initial, f) ? this.solve(f, min, initial) : this.solve(f, initial, max);
      }
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      double x1 = min;
      double y1 = f.value(min);
      double x2 = max;
      double y2 = f.value(max);
      if (y1 == 0.0) {
         return min;
      } else if (y2 == 0.0) {
         return max;
      } else {
         this.verifyBracketing(min, max, f);
         int i = 1;

         for(double oldx = Double.POSITIVE_INFINITY; i <= this.maximalIterationCount; ++i) {
            double x3 = 0.5 * (x1 + x2);
            double y3 = f.value(x3);
            if (FastMath.abs(y3) <= this.functionValueAccuracy) {
               this.setResult(x3, i);
               return this.result;
            }

            double delta = 1.0 - y1 * y2 / (y3 * y3);
            double correction = MathUtils.sign(y2) * MathUtils.sign(y3) * (x3 - x1) / FastMath.sqrt(delta);
            double x = x3 - correction;
            double y = f.value(x);
            double tolerance = FastMath.max(this.relativeAccuracy * FastMath.abs(x), this.absoluteAccuracy);
            if (FastMath.abs(x - oldx) <= tolerance) {
               this.setResult(x, i);
               return this.result;
            }

            if (FastMath.abs(y) <= this.functionValueAccuracy) {
               this.setResult(x, i);
               return this.result;
            }

            if (correction > 0.0) {
               if (MathUtils.sign(y1) + MathUtils.sign(y) == 0.0) {
                  x2 = x;
                  y2 = y;
               } else {
                  x1 = x;
                  x2 = x3;
                  y1 = y;
                  y2 = y3;
               }
            } else if (MathUtils.sign(y2) + MathUtils.sign(y) == 0.0) {
               x1 = x;
               y1 = y;
            } else {
               x1 = x3;
               x2 = x;
               y1 = y3;
               y2 = y;
            }

            oldx = x;
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }
}
