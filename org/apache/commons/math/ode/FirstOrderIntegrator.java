package org.apache.commons.math.ode;

public interface FirstOrderIntegrator extends ODEIntegrator {
   double integrate(FirstOrderDifferentialEquations var1, double var2, double[] var4, double var5, double[] var7) throws DerivativeException, IntegratorException;
}
