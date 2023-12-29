package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;

class EulerStepInterpolator extends RungeKuttaStepInterpolator {
   private static final long serialVersionUID = -7179861704951334960L;

   public EulerStepInterpolator() {
   }

   public EulerStepInterpolator(EulerStepInterpolator interpolator) {
      super(interpolator);
   }

   @Override
   protected StepInterpolator doCopy() {
      return new EulerStepInterpolator(this);
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) throws DerivativeException {
      for(int i = 0; i < this.interpolatedState.length; ++i) {
         this.interpolatedState[i] = this.currentState[i] - oneMinusThetaH * this.yDotK[0][i];
      }

      System.arraycopy(this.yDotK[0], 0, this.interpolatedDerivatives, 0, this.interpolatedDerivatives.length);
   }
}
