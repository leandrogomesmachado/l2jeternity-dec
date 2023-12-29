package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.util.FastMath;

public abstract class EmbeddedRungeKuttaIntegrator extends AdaptiveStepsizeIntegrator {
   private final boolean fsal;
   private final double[] c;
   private final double[][] a;
   private final double[] b;
   private final RungeKuttaStepInterpolator prototype;
   private final double exp;
   private double safety;
   private double minReduction;
   private double maxGrowth;

   protected EmbeddedRungeKuttaIntegrator(
      String name,
      boolean fsal,
      double[] c,
      double[][] a,
      double[] b,
      RungeKuttaStepInterpolator prototype,
      double minStep,
      double maxStep,
      double scalAbsoluteTolerance,
      double scalRelativeTolerance
   ) {
      super(name, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
      this.fsal = fsal;
      this.c = c;
      this.a = a;
      this.b = b;
      this.prototype = prototype;
      this.exp = -1.0 / (double)this.getOrder();
      this.setSafety(0.9);
      this.setMinReduction(0.2);
      this.setMaxGrowth(10.0);
   }

   protected EmbeddedRungeKuttaIntegrator(
      String name,
      boolean fsal,
      double[] c,
      double[][] a,
      double[] b,
      RungeKuttaStepInterpolator prototype,
      double minStep,
      double maxStep,
      double[] vecAbsoluteTolerance,
      double[] vecRelativeTolerance
   ) {
      super(name, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
      this.fsal = fsal;
      this.c = c;
      this.a = a;
      this.b = b;
      this.prototype = prototype;
      this.exp = -1.0 / (double)this.getOrder();
      this.setSafety(0.9);
      this.setMinReduction(0.2);
      this.setMaxGrowth(10.0);
   }

   public abstract int getOrder();

   public double getSafety() {
      return this.safety;
   }

   public void setSafety(double safety) {
      this.safety = safety;
   }

   @Override
   public double integrate(FirstOrderDifferentialEquations equations, double t0, double[] y0, double t, double[] y) throws DerivativeException, IntegratorException {
      this.sanityChecks(equations, t0, y0, t, y);
      this.setEquations(equations);
      this.resetEvaluations();
      boolean forward = t > t0;
      int stages = this.c.length + 1;
      if (y != y0) {
         System.arraycopy(y0, 0, y, 0, y0.length);
      }

      double[][] yDotK = new double[stages][y0.length];
      double[] yTmp = new double[y0.length];
      double[] yDotTmp = new double[y0.length];
      AbstractStepInterpolator interpolator;
      if (this.requiresDenseOutput()) {
         RungeKuttaStepInterpolator rki = (RungeKuttaStepInterpolator)this.prototype.copy();
         rki.reinitialize(this, yTmp, yDotK, forward);
         interpolator = rki;
      } else {
         interpolator = new DummyStepInterpolator(yTmp, yDotK[stages - 1], forward);
      }

      interpolator.storeTime(t0);
      this.stepStart = t0;
      double hNew = 0.0;
      boolean firstTime = true;

      for(StepHandler handler : this.stepHandlers) {
         handler.reset();
      }

      this.setStateInitialized(false);
      this.isLastStep = false;

      do {
         interpolator.shift();
         double error = 10.0;

         while(error >= 1.0) {
            if (firstTime || !this.fsal) {
               this.computeDerivatives(this.stepStart, y, yDotK[0]);
            }

            if (firstTime) {
               double[] scale = new double[this.mainSetDimension];
               if (this.vecAbsoluteTolerance == null) {
                  for(int i = 0; i < scale.length; ++i) {
                     scale[i] = this.scalAbsoluteTolerance + this.scalRelativeTolerance * FastMath.abs(y[i]);
                  }
               } else {
                  for(int i = 0; i < scale.length; ++i) {
                     scale[i] = this.vecAbsoluteTolerance[i] + this.vecRelativeTolerance[i] * FastMath.abs(y[i]);
                  }
               }

               hNew = this.initializeStep(equations, forward, this.getOrder(), scale, this.stepStart, y, yDotK[0], yTmp, yDotK[1]);
               firstTime = false;
            }

            this.stepSize = hNew;

            for(int k = 1; k < stages; ++k) {
               for(int j = 0; j < y0.length; ++j) {
                  double sum = this.a[k - 1][0] * yDotK[0][j];

                  for(int l = 1; l < k; ++l) {
                     sum += this.a[k - 1][l] * yDotK[l][j];
                  }

                  yTmp[j] = y[j] + this.stepSize * sum;
               }

               this.computeDerivatives(this.stepStart + this.c[k - 1] * this.stepSize, yTmp, yDotK[k]);
            }

            for(int j = 0; j < y0.length; ++j) {
               double sum = this.b[0] * yDotK[0][j];

               for(int l = 1; l < stages; ++l) {
                  sum += this.b[l] * yDotK[l][j];
               }

               yTmp[j] = y[j] + this.stepSize * sum;
            }

            error = this.estimateError(yDotK, y, yTmp, this.stepSize);
            if (error >= 1.0) {
               double factor = FastMath.min(this.maxGrowth, FastMath.max(this.minReduction, this.safety * FastMath.pow(error, this.exp)));
               hNew = this.filterStep(this.stepSize * factor, forward, false);
            }
         }

         interpolator.storeTime(this.stepStart + this.stepSize);
         System.arraycopy(yTmp, 0, y, 0, y0.length);
         System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
         this.stepStart = this.acceptStep(interpolator, y, yDotTmp, t);
         if (!this.isLastStep) {
            interpolator.storeTime(this.stepStart);
            if (this.fsal) {
               System.arraycopy(yDotTmp, 0, yDotK[0], 0, y0.length);
            }

            double factor = FastMath.min(this.maxGrowth, FastMath.max(this.minReduction, this.safety * FastMath.pow(error, this.exp)));
            double scaledH = this.stepSize * factor;
            double nextT = this.stepStart + scaledH;
            boolean nextIsLast = forward ? nextT >= t : nextT <= t;
            hNew = this.filterStep(scaledH, forward, nextIsLast);
            double filteredNextT = this.stepStart + hNew;
            boolean filteredNextIsLast = forward ? filteredNextT >= t : filteredNextT <= t;
            if (filteredNextIsLast) {
               hNew = t - this.stepStart;
            }
         }
      } while(!this.isLastStep);

      double stopTime = this.stepStart;
      this.resetInternalState();
      return stopTime;
   }

   public double getMinReduction() {
      return this.minReduction;
   }

   public void setMinReduction(double minReduction) {
      this.minReduction = minReduction;
   }

   public double getMaxGrowth() {
      return this.maxGrowth;
   }

   public void setMaxGrowth(double maxGrowth) {
      this.maxGrowth = maxGrowth;
   }

   protected abstract double estimateError(double[][] var1, double[] var2, double[] var3, double var4);
}
