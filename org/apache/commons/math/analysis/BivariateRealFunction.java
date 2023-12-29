package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;

public interface BivariateRealFunction {
   double value(double var1, double var3) throws FunctionEvaluationException;
}
