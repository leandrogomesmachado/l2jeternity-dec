package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.TrivariateRealFunction;

public interface TrivariateRealGridInterpolator {
   TrivariateRealFunction interpolate(double[] var1, double[] var2, double[] var3, double[][][] var4) throws MathException;
}
