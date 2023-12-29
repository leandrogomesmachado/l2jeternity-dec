package org.apache.commons.math.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;

public interface DifferentiableMultivariateRealOptimizer {
   void setMaxIterations(int var1);

   int getMaxIterations();

   int getIterations();

   void setMaxEvaluations(int var1);

   int getMaxEvaluations();

   int getEvaluations();

   int getGradientEvaluations();

   void setConvergenceChecker(RealConvergenceChecker var1);

   RealConvergenceChecker getConvergenceChecker();

   RealPointValuePair optimize(DifferentiableMultivariateRealFunction var1, GoalType var2, double[] var3) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;
}
