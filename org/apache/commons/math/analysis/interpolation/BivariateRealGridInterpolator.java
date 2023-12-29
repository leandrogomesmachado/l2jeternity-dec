package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.BivariateRealFunction;

public interface BivariateRealGridInterpolator {
   BivariateRealFunction interpolate(double[] var1, double[] var2, double[][] var3) throws MathException;
}
