package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;

class ClassicalRungeKuttaStepInterpolator extends RungeKuttaStepInterpolator {
   private static final long serialVersionUID = -6576285612589783992L;

   public ClassicalRungeKuttaStepInterpolator() {
   }

   public ClassicalRungeKuttaStepInterpolator(ClassicalRungeKuttaStepInterpolator interpolator) {
      super(interpolator);
   }

   @Override
   protected StepInterpolator doCopy() {
      return new ClassicalRungeKuttaStepInterpolator(this);
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) throws DerivativeException {
      double fourTheta = 4.0 * theta;
      double oneMinusTheta = 1.0 - theta;
      double oneMinus2Theta = 1.0 - 2.0 * theta;
      double s = oneMinusThetaH / 6.0;
      double coeff1 = s * ((-fourTheta + 5.0) * theta - 1.0);
      double coeff23 = s * ((fourTheta - 2.0) * theta - 2.0);
      double coeff4 = s * ((-fourTheta - 1.0) * theta - 1.0);
      double coeffDot1 = oneMinusTheta * oneMinus2Theta;
      double coeffDot23 = 2.0 * theta * oneMinusTheta;
      double coeffDot4 = -theta * oneMinus2Theta;

      for(int i = 0; i < this.interpolatedState.length; ++i) {
         double yDot1 = this.yDotK[0][i];
         double yDot23 = this.yDotK[1][i] + this.yDotK[2][i];
         double yDot4 = this.yDotK[3][i];
         this.interpolatedState[i] = this.currentState[i] + coeff1 * yDot1 + coeff23 * yDot23 + coeff4 * yDot4;
         this.interpolatedDerivatives[i] = coeffDot1 * yDot1 + coeffDot23 * yDot23 + coeffDot4 * yDot4;
      }
   }
}
