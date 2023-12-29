package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.util.FastMath;

public abstract class RungeKuttaIntegrator extends AbstractIntegrator {
   private final double[] c;
   private final double[][] a;
   private final double[] b;
   private final RungeKuttaStepInterpolator prototype;
   private final double step;

   protected RungeKuttaIntegrator(String name, double[] c, double[][] a, double[] b, RungeKuttaStepInterpolator prototype, double step) {
      super(name);
      this.c = c;
      this.a = a;
      this.b = b;
      this.prototype = prototype;
      this.step = FastMath.abs(step);
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

      double[][] yDotK = new double[stages][];

      for(int i = 0; i < stages; ++i) {
         yDotK[i] = new double[y0.length];
      }

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
      this.stepSize = forward ? this.step : -this.step;

      for(StepHandler handler : this.stepHandlers) {
         handler.reset();
      }

      this.setStateInitialized(false);
      this.isLastStep = false;

      do {
         interpolator.shift();
         this.computeDerivatives(this.stepStart, y, yDotK[0]);

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

         interpolator.storeTime(this.stepStart + this.stepSize);
         System.arraycopy(yTmp, 0, y, 0, y0.length);
         System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
         this.stepStart = this.acceptStep(interpolator, y, yDotTmp, t);
         if (!this.isLastStep) {
            interpolator.storeTime(this.stepStart);
            double nextT = this.stepStart + this.stepSize;
            boolean nextIsLast = forward ? nextT >= t : nextT <= t;
            if (nextIsLast) {
               this.stepSize = t - this.stepStart;
            }
         }
      } while(!this.isLastStep);

      double stopTime = this.stepStart;
      this.stepStart = Double.NaN;
      this.stepSize = Double.NaN;
      return stopTime;
   }
}
