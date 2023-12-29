package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class MullerSolver extends UnivariateRealSolverImpl {
   @Deprecated
   public MullerSolver(UnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   @Deprecated
   public MullerSolver() {
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
      double x0 = min;
      double y0 = f.value(min);
      double x2 = max;
      double y2 = f.value(max);
      double x1 = 0.5 * (min + max);
      double y1 = f.value(x1);
      if (y0 == 0.0) {
         return min;
      } else if (y2 == 0.0) {
         return max;
      } else {
         this.verifyBracketing(min, max, f);
         double oldx = Double.POSITIVE_INFINITY;

         for(int i = 1; i <= this.maximalIterationCount; ++i) {
            double d01 = (y1 - y0) / (x1 - x0);
            double d12 = (y2 - y1) / (x2 - x1);
            double d012 = (d12 - d01) / (x2 - x0);
            double c1 = d01 + (x1 - x0) * d012;
            double delta = c1 * c1 - 4.0 * y1 * d012;
            double xplus = x1 + -2.0 * y1 / (c1 + FastMath.sqrt(delta));
            double xminus = x1 + -2.0 * y1 / (c1 - FastMath.sqrt(delta));
            double x = this.isSequence(x0, xplus, x2) ? xplus : xminus;
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

            boolean bisect = x < x1 && x1 - x0 > 0.95 * (x2 - x0) || x > x1 && x2 - x1 > 0.95 * (x2 - x0) || x == x1;
            if (!bisect) {
               x0 = x < x1 ? x0 : x1;
               y0 = x < x1 ? y0 : y1;
               x2 = x > x1 ? x2 : x1;
               y2 = x > x1 ? y2 : y1;
               x1 = x;
               y1 = y;
               oldx = x;
            } else {
               double xm = 0.5 * (x0 + x2);
               double ym = f.value(xm);
               if (MathUtils.sign(y0) + MathUtils.sign(ym) == 0.0) {
                  x2 = xm;
                  y2 = ym;
               } else {
                  x0 = xm;
                  y0 = ym;
               }

               x1 = 0.5 * (x0 + x2);
               y1 = f.value(x1);
               oldx = Double.POSITIVE_INFINITY;
            }
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }

   @Deprecated
   public double solve2(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve2(this.f, min, max);
   }

   @Deprecated
   public double solve2(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      double x0 = min;
      double y0 = f.value(min);
      double x1 = max;
      double y1 = f.value(max);
      double x2 = 0.5 * (min + max);
      double y2 = f.value(x2);
      if (y0 == 0.0) {
         return min;
      } else if (y1 == 0.0) {
         return max;
      } else {
         this.verifyBracketing(min, max, f);
         double oldx = Double.POSITIVE_INFINITY;

         for(int i = 1; i <= this.maximalIterationCount; ++i) {
            double q = (x2 - x1) / (x1 - x0);
            double a = q * (y2 - (1.0 + q) * y1 + q * y0);
            double b = (2.0 * q + 1.0) * y2 - (1.0 + q) * (1.0 + q) * y1 + q * q * y0;
            double c = (1.0 + q) * y2;
            double delta = b * b - 4.0 * a * c;
            double denominator;
            if (delta >= 0.0) {
               double dplus = b + FastMath.sqrt(delta);
               double dminus = b - FastMath.sqrt(delta);
               denominator = FastMath.abs(dplus) > FastMath.abs(dminus) ? dplus : dminus;
            } else {
               denominator = FastMath.sqrt(b * b - delta);
            }

            double x;
            if (denominator == 0.0) {
               x = min + FastMath.random() * (max - min);
               oldx = Double.POSITIVE_INFINITY;
            } else {
               x = x2 - 2.0 * c * (x2 - x1) / denominator;

               while(x == x1 || x == x2) {
                  x += this.absoluteAccuracy;
               }
            }

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

            x0 = x1;
            y0 = y1;
            x1 = x2;
            y1 = y2;
            x2 = x;
            y2 = y;
            oldx = x;
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }
}
