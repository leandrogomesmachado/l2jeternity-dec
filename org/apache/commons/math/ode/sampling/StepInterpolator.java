package org.apache.commons.math.ode.sampling;

import java.io.Externalizable;
import org.apache.commons.math.ode.DerivativeException;

public interface StepInterpolator extends Externalizable {
   double getPreviousTime();

   double getCurrentTime();

   double getInterpolatedTime();

   void setInterpolatedTime(double var1);

   double[] getInterpolatedState() throws DerivativeException;

   double[] getInterpolatedDerivatives() throws DerivativeException;

   boolean isForward();

   StepInterpolator copy() throws DerivativeException;
}
