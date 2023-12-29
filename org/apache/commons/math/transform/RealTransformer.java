package org.apache.commons.math.transform;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface RealTransformer {
   double[] transform(double[] var1) throws IllegalArgumentException;

   double[] transform(UnivariateRealFunction var1, double var2, double var4, int var6) throws FunctionEvaluationException, IllegalArgumentException;

   double[] inversetransform(double[] var1) throws IllegalArgumentException;

   double[] inversetransform(UnivariateRealFunction var1, double var2, double var4, int var6) throws FunctionEvaluationException, IllegalArgumentException;
}
