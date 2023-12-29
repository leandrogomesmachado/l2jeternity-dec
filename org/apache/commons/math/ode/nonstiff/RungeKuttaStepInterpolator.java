package org.apache.commons.math.ode.nonstiff;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;

abstract class RungeKuttaStepInterpolator extends AbstractStepInterpolator {
   protected double[][] yDotK;
   protected AbstractIntegrator integrator;

   protected RungeKuttaStepInterpolator() {
      this.yDotK = (double[][])null;
      this.integrator = null;
   }

   public RungeKuttaStepInterpolator(RungeKuttaStepInterpolator interpolator) {
      super(interpolator);
      if (interpolator.currentState != null) {
         int dimension = this.currentState.length;
         this.yDotK = new double[interpolator.yDotK.length][];

         for(int k = 0; k < interpolator.yDotK.length; ++k) {
            this.yDotK[k] = new double[dimension];
            System.arraycopy(interpolator.yDotK[k], 0, this.yDotK[k], 0, dimension);
         }
      } else {
         this.yDotK = (double[][])null;
      }

      this.integrator = null;
   }

   public void reinitialize(AbstractIntegrator rkIntegrator, double[] y, double[][] yDotArray, boolean forward) {
      this.reinitialize(y, forward);
      this.yDotK = yDotArray;
      this.integrator = rkIntegrator;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      this.writeBaseExternal(out);
      int n = this.currentState == null ? -1 : this.currentState.length;
      int kMax = this.yDotK == null ? -1 : this.yDotK.length;
      out.writeInt(kMax);

      for(int k = 0; k < kMax; ++k) {
         for(int i = 0; i < n; ++i) {
            out.writeDouble(this.yDotK[k][i]);
         }
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException {
      double t = this.readBaseExternal(in);
      int n = this.currentState == null ? -1 : this.currentState.length;
      int kMax = in.readInt();
      this.yDotK = kMax < 0 ? (double[][])null : new double[kMax][];

      for(int k = 0; k < kMax; ++k) {
         this.yDotK[k] = n < 0 ? null : new double[n];

         for(int i = 0; i < n; ++i) {
            this.yDotK[k][i] = in.readDouble();
         }
      }

      this.integrator = null;
      if (this.currentState != null) {
         this.setInterpolatedTime(t);
      } else {
         this.interpolatedTime = t;
      }
   }
}
