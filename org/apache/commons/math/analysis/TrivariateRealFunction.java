package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;

public interface TrivariateRealFunction {
   double value(double var1, double var3, double var5) throws FunctionEvaluationException;
}
