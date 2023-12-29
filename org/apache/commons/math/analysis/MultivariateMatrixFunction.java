package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;

public interface MultivariateMatrixFunction {
   double[][] value(double[] var1) throws FunctionEvaluationException, IllegalArgumentException;
}
