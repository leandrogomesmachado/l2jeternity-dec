package org.apache.commons.math.ode.jacobians;

import org.apache.commons.math.ode.events.EventException;

@Deprecated
public interface EventHandlerWithJacobians {
   int STOP = 0;
   int RESET_STATE = 1;
   int RESET_DERIVATIVES = 2;
   int CONTINUE = 3;

   double g(double var1, double[] var3, double[][] var4, double[][] var5) throws EventException;

   int eventOccurred(double var1, double[] var3, double[][] var4, double[][] var5, boolean var6) throws EventException;

   void resetState(double var1, double[] var3, double[][] var4, double[][] var5) throws EventException;
}
