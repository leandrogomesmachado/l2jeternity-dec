package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.util.FastMath;

public class BisectionSolver extends UnivariateRealSolverImpl {
   @Deprecated
   public BisectionSolver(UnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   public BisectionSolver() {
      super(100, 1.0E-6);
   }

   @Deprecated
   @Override
   public double solve(double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max);
   }

   @Deprecated
   @Override
   public double solve(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(f, min, max);
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max, double initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(maxEval, f, min, max);
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

      for(int i = 0; i < this.maximalIterationCount; ++i) {
         double m = UnivariateRealSolverUtils.midpoint(min, max);
         double fmin = f.value(min);
         double fm = f.value(m);
         if (fm * fmin > 0.0) {
            min = m;
         } else {
            max = m;
         }

         if (FastMath.abs(max - min) <= this.absoluteAccuracy) {
            m = UnivariateRealSolverUtils.midpoint(min, max);
            this.setResult(m, i);
            return m;
         }
      }

      throw new MaxIterationsExceededException(this.maximalIterationCount);
   }
}
