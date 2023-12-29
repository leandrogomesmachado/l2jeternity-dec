package org.apache.commons.math.ode.sampling;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.util.FastMath;

public class NordsieckStepInterpolator extends AbstractStepInterpolator {
   private static final long serialVersionUID = -7179861704951334960L;
   protected double[] stateVariation;
   private double scalingH;
   private double referenceTime;
   private double[] scaled;
   private Array2DRowRealMatrix nordsieck;

   public NordsieckStepInterpolator() {
   }

   public NordsieckStepInterpolator(NordsieckStepInterpolator interpolator) {
      super(interpolator);
      this.scalingH = interpolator.scalingH;
      this.referenceTime = interpolator.referenceTime;
      if (interpolator.scaled != null) {
         this.scaled = (double[])interpolator.scaled.clone();
      }

      if (interpolator.nordsieck != null) {
         this.nordsieck = new Array2DRowRealMatrix(interpolator.nordsieck.getDataRef(), true);
      }

      if (interpolator.stateVariation != null) {
         this.stateVariation = (double[])interpolator.stateVariation.clone();
      }
   }

   @Override
   protected StepInterpolator doCopy() {
      return new NordsieckStepInterpolator(this);
   }

   @Override
   public void reinitialize(double[] y, boolean forward) {
      super.reinitialize(y, forward);
      this.stateVariation = new double[y.length];
   }

   public void reinitialize(double time, double stepSize, double[] scaledDerivative, Array2DRowRealMatrix nordsieckVector) {
      this.referenceTime = time;
      this.scalingH = stepSize;
      this.scaled = scaledDerivative;
      this.nordsieck = nordsieckVector;
      this.setInterpolatedTime(this.getInterpolatedTime());
   }

   public void rescale(double stepSize) {
      double ratio = stepSize / this.scalingH;

      for(int i = 0; i < this.scaled.length; ++i) {
         this.scaled[i] *= ratio;
      }

      double[][] nData = this.nordsieck.getDataRef();
      double power = ratio;

      for(int i = 0; i < nData.length; ++i) {
         power *= ratio;
         double[] nDataI = nData[i];

         for(int j = 0; j < nDataI.length; ++j) {
            nDataI[j] *= power;
         }
      }

      this.scalingH = stepSize;
   }

   public double[] getInterpolatedStateVariation() throws DerivativeException {
      this.getInterpolatedState();
      return this.stateVariation;
   }

   @Override
   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
      double x = this.interpolatedTime - this.referenceTime;
      double normalizedAbscissa = x / this.scalingH;
      Arrays.fill(this.stateVariation, 0.0);
      Arrays.fill(this.interpolatedDerivatives, 0.0);
      double[][] nData = this.nordsieck.getDataRef();

      for(int i = nData.length - 1; i >= 0; --i) {
         int order = i + 2;
         double[] nDataI = nData[i];
         double power = FastMath.pow(normalizedAbscissa, (double)order);

         for(int j = 0; j < nDataI.length; ++j) {
            double d = nDataI[j] * power;
            this.stateVariation[j] += d;
            this.interpolatedDerivatives[j] += (double)order * d;
         }
      }

      for(int j = 0; j < this.currentState.length; ++j) {
         this.stateVariation[j] += this.scaled[j] * normalizedAbscissa;
         this.interpolatedState[j] = this.currentState[j] + this.stateVariation[j];
         this.interpolatedDerivatives[j] = (this.interpolatedDerivatives[j] + this.scaled[j] * normalizedAbscissa) / x;
      }
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      this.writeBaseExternal(out);
      out.writeDouble(this.scalingH);
      out.writeDouble(this.referenceTime);
      int n = this.currentState == null ? -1 : this.currentState.length;
      if (this.scaled == null) {
         out.writeBoolean(false);
      } else {
         out.writeBoolean(true);

         for(int j = 0; j < n; ++j) {
            out.writeDouble(this.scaled[j]);
         }
      }

      if (this.nordsieck == null) {
         out.writeBoolean(false);
      } else {
         out.writeBoolean(true);
         out.writeObject(this.nordsieck);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      double t = this.readBaseExternal(in);
      this.scalingH = in.readDouble();
      this.referenceTime = in.readDouble();
      int n = this.currentState == null ? -1 : this.currentState.length;
      boolean hasScaled = in.readBoolean();
      if (hasScaled) {
         this.scaled = new double[n];

         for(int j = 0; j < n; ++j) {
            this.scaled[j] = in.readDouble();
         }
      } else {
         this.scaled = null;
      }

      boolean hasNordsieck = in.readBoolean();
      if (hasNordsieck) {
         this.nordsieck = (Array2DRowRealMatrix)in.readObject();
      } else {
         this.nordsieck = null;
      }

      if (hasScaled && hasNordsieck) {
         this.stateVariation = new double[n];
         this.setInterpolatedTime(t);
      } else {
         this.stateVariation = null;
      }
   }
}
