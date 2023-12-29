package org.apache.commons.math.ode.sampling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DummyStepInterpolator extends AbstractStepInterpolator {
   private static final long serialVersionUID = 1708010296707839488L;
   private double[] currentDerivative;

   public DummyStepInterpolator() {
      this.currentDerivative = null;
   }

   public DummyStepInterpolator(double[] y, double[] yDot, boolean forward) {
      super(y, forward);
      this.currentDerivative = yDot;
   }

   public DummyStepInterpolator(DummyStepInterpolator interpolator) {
      super(interpolator);
      this.currentDerivative = (double[])interpolator.currentDerivative.clone();
   }

   @Override
   protected StepInterpolator doCopy() {
      return new DummyStepInterpolator(this);
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
      System.arraycopy(this.currentState, 0, this.interpolatedState, 0, this.currentState.length);
      System.arraycopy(this.currentDerivative, 0, this.interpolatedDerivatives, 0, this.currentDerivative.length);
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      this.writeBaseExternal(out);
      if (this.currentDerivative != null) {
         for(int i = 0; i < this.currentDerivative.length; ++i) {
            out.writeDouble(this.currentDerivative[i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException {
      double t = this.readBaseExternal(in);
      if (this.currentState == null) {
         this.currentDerivative = null;
      } else {
         this.currentDerivative = new double[this.currentState.length];

         for(int i = 0; i < this.currentDerivative.length; ++i) {
            this.currentDerivative[i] = in.readDouble();
         }
      }

      this.setInterpolatedTime(t);
   }
}
