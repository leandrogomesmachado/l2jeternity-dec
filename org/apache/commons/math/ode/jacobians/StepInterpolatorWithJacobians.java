package org.apache.commons.math.ode.jacobians;

import java.io.Externalizable;
import org.apache.commons.math.ode.DerivativeException;

@Deprecated
public interface StepInterpolatorWithJacobians extends Externalizable {
   double getPreviousTime();

   double getCurrentTime();

   double getInterpolatedTime();

   void setInterpolatedTime(double var1);

   double[] getInterpolatedY() throws DerivativeException;

   double[][] getInterpolatedDyDy0() throws DerivativeException;

   double[][] getInterpolatedDyDp() throws DerivativeException;

   double[] getInterpolatedYDot() throws DerivativeException;

   double[][] getInterpolatedDyDy0Dot() throws DerivativeException;

   double[][] getInterpolatedDyDpDot() throws DerivativeException;

   boolean isForward();

   StepInterpolatorWithJacobians copy() throws DerivativeException;
}
