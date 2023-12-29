package org.apache.commons.math.ode.jacobians;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

@Deprecated
public interface ODEWithJacobians extends FirstOrderDifferentialEquations {
   int getParametersDimension();

   void computeJacobians(double var1, double[] var3, double[] var4, double[][] var5, double[][] var6) throws DerivativeException;
}
