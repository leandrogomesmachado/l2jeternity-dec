package org.apache.commons.math.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;

public interface MultivariateRealOptimizer {
   void setMaxIterations(int var1);

   int getMaxIterations();

   void setMaxEvaluations(int var1);

   int getMaxEvaluations();

   int getIterations();

   int getEvaluations();

   void setConvergenceChecker(RealConvergenceChecker var1);

   RealConvergenceChecker getConvergenceChecker();

   RealPointValuePair optimize(MultivariateRealFunction var1, GoalType var2, double[] var3) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;
}
