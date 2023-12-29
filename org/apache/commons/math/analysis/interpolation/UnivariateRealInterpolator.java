package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface UnivariateRealInterpolator {
   UnivariateRealFunction interpolate(double[] var1, double[] var2) throws MathException;
}
