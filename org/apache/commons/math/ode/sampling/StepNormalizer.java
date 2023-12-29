package org.apache.commons.math.ode.sampling;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.util.FastMath;

public class StepNormalizer implements StepHandler {
   private double h;
   private final FixedStepHandler handler;
   private double lastTime;
   private double[] lastState;
   private double[] lastDerivatives;
   private boolean forward;

   public StepNormalizer(double h, FixedStepHandler handler) {
      this.h = FastMath.abs(h);
      this.handler = handler;
      this.reset();
   }

   @Override
   public boolean requiresDenseOutput() {
      return true;
   }

   @Override
   public void reset() {
      this.lastTime = Double.NaN;
      this.lastState = null;
      this.lastDerivatives = null;
      this.forward = true;
   }

   @Override
   public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
      if (this.lastState == null) {
         this.lastTime = interpolator.getPreviousTime();
         interpolator.setInterpolatedTime(this.lastTime);
         this.lastState = (double[])interpolator.getInterpolatedState().clone();
         this.lastDerivatives = (double[])interpolator.getInterpolatedDerivatives().clone();
         this.forward = interpolator.getCurrentTime() >= this.lastTime;
         if (!this.forward) {
            this.h = -this.h;
         }
      }

      double nextTime = this.lastTime + this.h;

      for(boolean nextInStep = this.forward ^ nextTime > interpolator.getCurrentTime();
         nextInStep;
         nextInStep = this.forward ^ nextTime > interpolator.getCurrentTime()
      ) {
         this.handler.handleStep(this.lastTime, this.lastState, this.lastDerivatives, false);
         this.lastTime = nextTime;
         interpolator.setInterpolatedTime(this.lastTime);
         System.arraycopy(interpolator.getInterpolatedState(), 0, this.lastState, 0, this.lastState.length);
         System.arraycopy(interpolator.getInterpolatedDerivatives(), 0, this.lastDerivatives, 0, this.lastDerivatives.length);
         nextTime += this.h;
      }

      if (isLast) {
         this.handler.handleStep(this.lastTime, this.lastState, this.lastDerivatives, true);
      }
   }
}
