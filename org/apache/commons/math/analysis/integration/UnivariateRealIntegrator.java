package org.apache.commons.math.analysis.integration;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ConvergingAlgorithm;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface UnivariateRealIntegrator extends ConvergingAlgorithm {
   void setMinimalIterationCount(int var1);

   int getMinimalIterationCount();

   void resetMinimalIterationCount();

   @Deprecated
   double integrate(double var1, double var3) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException;

   double integrate(UnivariateRealFunction var1, double var2, double var4) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException;

   double getResult() throws IllegalStateException;
}
