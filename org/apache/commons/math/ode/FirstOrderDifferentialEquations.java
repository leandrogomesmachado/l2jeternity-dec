package org.apache.commons.math.ode;

public interface FirstOrderDifferentialEquations {
   int getDimension();

   void computeDerivatives(double var1, double[] var3, double[] var4) throws DerivativeException;
}
