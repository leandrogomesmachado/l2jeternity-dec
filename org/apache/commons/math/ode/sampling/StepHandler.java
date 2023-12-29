package org.apache.commons.math.ode.sampling;

import org.apache.commons.math.ode.DerivativeException;

public interface StepHandler {
   boolean requiresDenseOutput();

   void reset();

   void handleStep(StepInterpolator var1, boolean var2) throws DerivativeException;
}
