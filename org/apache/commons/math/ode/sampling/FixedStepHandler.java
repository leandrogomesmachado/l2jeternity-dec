package org.apache.commons.math.ode.sampling;

import org.apache.commons.math.ode.DerivativeException;

public interface FixedStepHandler {
   void handleStep(double var1, double[] var3, double[] var4, boolean var5) throws DerivativeException;
}
