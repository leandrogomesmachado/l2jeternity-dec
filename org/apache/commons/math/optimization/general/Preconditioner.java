package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;

public interface Preconditioner {
   double[] precondition(double[] var1, double[] var2) throws FunctionEvaluationException, IllegalArgumentException;
}
