package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.optimization.DifferentiableMultivariateRealOptimizer;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;

public abstract class AbstractScalarDifferentiableOptimizer implements DifferentiableMultivariateRealOptimizer {
   public static final int DEFAULT_MAX_ITERATIONS = 100;
   @Deprecated
   protected RealConvergenceChecker checker;
   @Deprecated
   protected GoalType goal;
   @Deprecated
   protected double[] point;
   private int maxIterations;
   private int iterations;
   private int maxEvaluations;
   private int evaluations;
   private int gradientEvaluations;
   private DifferentiableMultivariateRealFunction function;
   private MultivariateVectorialFunction gradient;

   protected AbstractScalarDifferentiableOptimizer() {
      this.setConvergenceChecker(new SimpleScalarValueChecker());
      this.setMaxIterations(100);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   @Override
   public void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }

   @Override
   public int getMaxIterations() {
      return this.maxIterations;
   }

   @Override
   public int getIterations() {
      return this.iterations;
   }

   @Override
   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations;
   }

   @Override
   public int getMaxEvaluations() {
      return this.maxEvaluations;
   }

   @Override
   public int getEvaluations() {
      return this.evaluations;
   }

   @Override
   public int getGradientEvaluations() {
      return this.gradientEvaluations;
   }

   @Override
   public void setConvergenceChecker(RealConvergenceChecker convergenceChecker) {
      this.checker = convergenceChecker;
   }

   @Override
   public RealConvergenceChecker getConvergenceChecker() {
      return this.checker;
   }

   protected void incrementIterationsCounter() throws OptimizationException {
      if (++this.iterations > this.maxIterations) {
         throw new OptimizationException(new MaxIterationsExceededException(this.maxIterations));
      }
   }

   protected double[] computeObjectiveGradient(double[] evaluationPoint) throws FunctionEvaluationException {
      ++this.gradientEvaluations;
      return this.gradient.value(evaluationPoint);
   }

   protected double computeObjectiveValue(double[] evaluationPoint) throws FunctionEvaluationException {
      if (++this.evaluations > this.maxEvaluations) {
         throw new FunctionEvaluationException(new MaxEvaluationsExceededException(this.maxEvaluations), evaluationPoint);
      } else {
         return this.function.value(evaluationPoint);
      }
   }

   @Override
   public RealPointValuePair optimize(DifferentiableMultivariateRealFunction f, GoalType goalType, double[] startPoint) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      this.iterations = 0;
      this.evaluations = 0;
      this.gradientEvaluations = 0;
      this.function = f;
      this.gradient = f.gradient();
      this.goal = goalType;
      this.point = (double[])startPoint.clone();
      return this.doOptimize();
   }

   protected abstract RealPointValuePair doOptimize() throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;
}
