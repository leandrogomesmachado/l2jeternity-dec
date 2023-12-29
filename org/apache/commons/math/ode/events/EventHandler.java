package org.apache.commons.math.ode.events;

public interface EventHandler {
   int STOP = 0;
   int RESET_STATE = 1;
   int RESET_DERIVATIVES = 2;
   int CONTINUE = 3;

   double g(double var1, double[] var3) throws EventException;

   int eventOccurred(double var1, double[] var3, boolean var4) throws EventException;

   void resetState(double var1, double[] var3) throws EventException;
}
