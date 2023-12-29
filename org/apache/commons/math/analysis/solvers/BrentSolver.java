package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class BrentSolver extends UnivariateRealSolverImpl {
   public static final double DEFAULT_ABSOLUTE_ACCURACY = 1.0E-6;
   public static final int DEFAULT_MAXIMUM_ITERATIONS = 100;
   private static final long serialVersionUID = 7694577816772532779L;

   @Deprecated
   public BrentSolver(UnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   @Deprecated
   public BrentSolver() {
      super(100, 1.0E-6);
   }

   public BrentSolver(double absoluteAccuracy) {
      super(100, absoluteAccuracy);
   }

   public BrentSolver(int maximumIterations, double absoluteAccuracy) {
      super(maximumIterations, absoluteAccuracy);
   }

   @Deprecated
   @Override
   public double solve(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max);
   }

   @Deprecated
   @Override
   public double solve(double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max, initial);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.clearResult();
      if (!(initial < min) && !(initial > max)) {
         double yInitial = f.value(initial);
         if (FastMath.abs(yInitial) <= this.functionValueAccuracy) {
            this.setResult(initial, 0);
            return this.result;
         } else {
            double yMin = f.value(min);
            if (FastMath.abs(yMin) <= this.functionValueAccuracy) {
               this.setResult(min, 0);
               return this.result;
            } else if (yInitial * yMin < 0.0) {
               return this.solve(f, min, yMin, initial, yInitial, min, yMin);
            } else {
               double yMax = f.value(max);
               if (FastMath.abs(yMax) <= this.functionValueAccuracy) {
                  this.setResult(max, 0);
                  return this.result;
               } else if (yInitial * yMax < 0.0) {
                  return this.solve(f, initial, yInitial, max, yMax, initial, yInitial);
               } else {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SAME_SIGN_AT_ENDPOINTS, min, max, yMin, yMax);
               }
            }
         }
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_INTERVAL_INITIAL_VALUE_PARAMETERS, min, initial, max);
      }
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max, initial);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.clearResult();
      this.verifyInterval(min, max);
      double ret = Double.NaN;
      double yMin = f.value(min);
      double yMax = f.value(max);
      double sign = yMin * yMax;
      if (sign > 0.0) {
         if (FastMath.abs(yMin) <= this.functionValueAccuracy) {
            this.setResult(min, 0);
            ret = min;
         } else {
            if (!(FastMath.abs(yMax) <= this.functionValueAccuracy)) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SAME_SIGN_AT_ENDPOINTS, min, max, yMin, yMax);
            }

            this.setResult(max, 0);
            ret = max;
         }
      } else if (sign < 0.0) {
         ret = this.solve(f, min, yMin, max, yMax, min, yMin);
      } else if (yMin == 0.0) {
         ret = min;
      } else {
         ret = max;
      }

      return ret;
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max);
   }

   private double solve(UnivariateRealFunction f, double x0, double y0, double x1, double y1, double x2, double y2) throws MaxIterationsExceededException, FunctionEvaluationException {
      double delta = x1 - x0;
      double oldDelta = delta;

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

         double dx = x2 - x1;
         double tolerance = FastMath.max(this.relativeAccuracy * FastMath.abs(x1), this.absoluteAccuracy);
         if (FastMath.abs(dx) <= tolerance) {
            this.setResult(x1, i);
            return this.result;
         }

         if (!(FastMath.abs(oldDelta) < tolerance) && !(FastMath.abs(y0) <= FastMath.abs(y1))) {
            double r3 = y1 / y0;
            double p;
            double p1;
            if (x0 == x2) {
               p = dx * r3;
               p1 = 1.0 - r3;
            } else {
               double r1 = y0 / y2;
               double r2 = y1 / y2;
               p = r3 * (dx * r1 * (r1 - r2) - (x1 - x0) * (r2 - 1.0));
               p1 = (r1 - 1.0) * (r2 - 1.0) * (r3 - 1.0);
            }

            if (p > 0.0) {
               p1 = -p1;
            } else {
               p = -p;
            }

            if (!(2.0 * p >= 1.5 * dx * p1 - FastMath.abs(tolerance * p1)) && !(p >= FastMath.abs(0.5 * oldDelta * p1))) {
               oldDelta = delta;
               delta = p / p1;
            } else {
               delta = 0.5 * dx;
               oldDelta = delta;
            }
         } else {
            delta = 0.5 * dx;
            oldDelta = delta;
         }

         x0 = x1;
         y0 = y1;
         if (FastMath.abs(delta) > tolerance) {
            x1 += delta;
         } else if (dx > 0.0) {
            x1 += 0.5 * tolerance;
         } else if (dx <= 0.0) {
            x1 -= 0.5 * tolerance;
         }

         y1 = f.value(x1);
         if (y1 > 0.0 == y2 > 0.0) {
            x2 = x0;
            y2 = y0;
            delta = x1 - x0;
            oldDelta = delta;
         }
      }

      throw new MaxIterationsExceededException(this.maximalIterationCount);
   }
}
