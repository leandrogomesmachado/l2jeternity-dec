package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.FunctionEvaluationException;

public interface ParametricRealFunction {
   double value(double var1, double[] var3) throws FunctionEvaluationException;

   double[] gradient(double var1, double[] var3) throws FunctionEvaluationException;
}
