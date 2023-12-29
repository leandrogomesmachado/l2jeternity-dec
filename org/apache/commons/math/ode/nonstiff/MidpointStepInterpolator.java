package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;

class MidpointStepInterpolator extends RungeKuttaStepInterpolator {
   private static final long serialVersionUID = -865524111506042509L;

   public MidpointStepInterpolator() {
   }

   public MidpointStepInterpolator(MidpointStepInterpolator interpolator) {
      super(interpolator);
   }

   @Override
   protected StepInterpolator doCopy() {
      return new MidpointStepInterpolator(this);
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) throws DerivativeException {
      double coeff1 = oneMinusThetaH * theta;
      double coeff2 = oneMinusThetaH * (1.0 + theta);
      double coeffDot2 = 2.0 * theta;
      double coeffDot1 = 1.0 - coeffDot2;

      for(int i = 0; i < this.interpolatedState.length; ++i) {
         double yDot1 = this.yDotK[0][i];
         double yDot2 = this.yDotK[1][i];
         this.interpolatedState[i] = this.currentState[i] + coeff1 * yDot1 - coeff2 * yDot2;
         this.interpolatedDerivatives[i] = coeffDot1 * yDot1 + coeffDot2 * yDot2;
      }
   }
}
