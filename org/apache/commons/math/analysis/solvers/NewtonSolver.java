package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class NewtonSolver extends UnivariateRealSolverImpl {
   @Deprecated
   public NewtonSolver(DifferentiableUnivariateRealFunction f) {
      super(f, 100, 1.0E-6);
   }

   @Deprecated
   public NewtonSolver() {
      super(100, 1.0E-6);
   }

   @Deprecated
   @Override
   public double solve(double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max);
   }

   @Deprecated
   @Override
   public double solve(double min, double max, double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(this.f, min, max, startValue);
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.solve(f, min, max, UnivariateRealSolverUtils.midpoint(min, max));
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max, double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max, startValue);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max, double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
      try {
         UnivariateRealFunction derivative = ((DifferentiableUnivariateRealFunction)f).derivative();
         this.clearResult();
         this.verifySequence(min, startValue, max);
         double x0 = startValue;

         for(int i = 0; i < this.maximalIterationCount; ++i) {
            double x1 = x0 - f.value(x0) / derivative.value(x0);
            if (FastMath.abs(x1 - x0) <= this.absoluteAccuracy) {
               this.setResult(x1, i);
               return x1;
            }

            x0 = x1;
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      } catch (ClassCastException var14) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FUNCTION_NOT_DIFFERENTIABLE);
      }
   }
}
