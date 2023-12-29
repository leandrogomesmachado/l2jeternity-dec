package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ConvergingAlgorithm;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface UnivariateRealSolver extends ConvergingAlgorithm {
   void setFunctionValueAccuracy(double var1);

   double getFunctionValueAccuracy();

   void resetFunctionValueAccuracy();

   @Deprecated
   double solve(double var1, double var3) throws ConvergenceException, FunctionEvaluationException;

   @Deprecated
   double solve(UnivariateRealFunction var1, double var2, double var4) throws ConvergenceException, FunctionEvaluationException;

   @Deprecated
   double solve(double var1, double var3, double var5) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException;

   @Deprecated
   double solve(UnivariateRealFunction var1, double var2, double var4, double var6) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException;

   double getResult();

   double getFunctionValue();
}
