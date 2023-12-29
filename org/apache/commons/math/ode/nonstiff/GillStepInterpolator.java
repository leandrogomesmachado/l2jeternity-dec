package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.util.FastMath;

class GillStepInterpolator extends RungeKuttaStepInterpolator {
   private static final double TWO_MINUS_SQRT_2 = 2.0 - FastMath.sqrt(2.0);
   private static final double TWO_PLUS_SQRT_2 = 2.0 + FastMath.sqrt(2.0);
   private static final long serialVersionUID = -107804074496313322L;

   public GillStepInterpolator() {
   }

   public GillStepInterpolator(GillStepInterpolator interpolator) {
      super(interpolator);
   }

   @Override
   protected StepInterpolator doCopy() {
      return new GillStepInterpolator(this);
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) throws DerivativeException {
      double twoTheta = 2.0 * theta;
      double fourTheta = 4.0 * theta;
      double s = oneMinusThetaH / 6.0;
      double oMt = 1.0 - theta;
      double soMt = s * oMt;
      double c23 = soMt * (1.0 + twoTheta);
      double coeff1 = soMt * (1.0 - fourTheta);
      double coeff2 = c23 * TWO_MINUS_SQRT_2;
      double coeff3 = c23 * TWO_PLUS_SQRT_2;
      double coeff4 = s * (1.0 + theta * (1.0 + fourTheta));
      double coeffDot1 = theta * (twoTheta - 3.0) + 1.0;
      double cDot23 = theta * oMt;
      double coeffDot2 = cDot23 * TWO_MINUS_SQRT_2;
      double coeffDot3 = cDot23 * TWO_PLUS_SQRT_2;
      double coeffDot4 = theta * (twoTheta - 1.0);

      for(int i = 0; i < this.interpolatedState.length; ++i) {
         double yDot1 = this.yDotK[0][i];
         double yDot2 = this.yDotK[1][i];
         double yDot3 = this.yDotK[2][i];
         double yDot4 = this.yDotK[3][i];
         this.interpolatedState[i] = this.currentState[i] - coeff1 * yDot1 - coeff2 * yDot2 - coeff3 * yDot3 - coeff4 * yDot4;
         this.interpolatedDerivatives[i] = coeffDot1 * yDot1 + coeffDot2 * yDot2 + coeffDot3 * yDot3 + coeffDot4 * yDot4;
      }
   }
}
