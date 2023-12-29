package org.apache.commons.math.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ConvergingAlgorithm;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface UnivariateRealOptimizer extends ConvergingAlgorithm {
   void setMaxEvaluations(int var1);

   int getMaxEvaluations();

   int getEvaluations();

   double optimize(UnivariateRealFunction var1, GoalType var2, double var3, double var5) throws ConvergenceException, FunctionEvaluationException;

   double optimize(UnivariateRealFunction var1, GoalType var2, double var3, double var5, double var7) throws ConvergenceException, FunctionEvaluationException;

   double getResult();

   double getFunctionValue() throws FunctionEvaluationException;
}
