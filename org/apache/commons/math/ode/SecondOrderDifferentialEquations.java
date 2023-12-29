package org.apache.commons.math.ode;

public interface SecondOrderDifferentialEquations {
   int getDimension();

   void computeSecondDerivatives(double var1, double[] var3, double[] var4, double[] var5) throws DerivativeException;
}
