package org.apache.commons.math.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;

public interface DifferentiableMultivariateVectorialOptimizer {
   void setMaxIterations(int var1);

   int getMaxIterations();

   int getIterations();

   void setMaxEvaluations(int var1);

   int getMaxEvaluations();

   int getEvaluations();

   int getJacobianEvaluations();

   void setConvergenceChecker(VectorialConvergenceChecker var1);

   VectorialConvergenceChecker getConvergenceChecker();

   VectorialPointValuePair optimize(DifferentiableMultivariateVectorialFunction var1, double[] var2, double[] var3, double[] var4) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;
}
