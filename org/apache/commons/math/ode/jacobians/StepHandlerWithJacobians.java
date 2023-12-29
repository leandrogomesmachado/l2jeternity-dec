package org.apache.commons.math.ode.jacobians;

import org.apache.commons.math.ode.DerivativeException;

@Deprecated
public interface StepHandlerWithJacobians {
   boolean requiresDenseOutput();

   void reset();

   void handleStep(StepInterpolatorWithJacobians var1, boolean var2) throws DerivativeException;
}
