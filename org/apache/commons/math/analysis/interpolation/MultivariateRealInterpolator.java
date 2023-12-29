package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;

public interface MultivariateRealInterpolator {
   MultivariateRealFunction interpolate(double[][] var1, double[] var2) throws MathException, IllegalArgumentException;
}
