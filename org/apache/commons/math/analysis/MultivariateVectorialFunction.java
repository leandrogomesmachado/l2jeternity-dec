package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;

public interface MultivariateVectorialFunction {
   double[] value(double[] var1) throws FunctionEvaluationException, IllegalArgumentException;
}
