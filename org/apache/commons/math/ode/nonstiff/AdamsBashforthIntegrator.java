package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.sampling.NordsieckStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.util.FastMath;

public class AdamsBashforthIntegrator extends AdamsIntegrator {
   private static final String METHOD_NAME = "Adams-Bashforth";

   public AdamsBashforthIntegrator(int nSteps, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance) throws IllegalArgumentException {
      super("Adams-Bashforth", nSteps, nSteps, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
   }

   public AdamsBashforthIntegrator(int nSteps, double minStep, double maxStep, double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) throws IllegalArgumentException {
      super("Adams-Bashforth", nSteps, nSteps, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
   }

   @Override
   public double integrate(FirstOrderDifferentialEquations equations, double t0, double[] y0, double t, double[] y) throws DerivativeException, IntegratorException {
      int n = y0.length;
      this.sanityChecks(equations, t0, y0, t, y);
      this.setEquations(equations);
      this.resetEvaluations();
      boolean forward = t > t0;
      if (y != y0) {
         System.arraycopy(y0, 0, y, 0, n);
      }

      double[] yDot = new double[n];
      NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
      interpolator.reinitialize(y, forward);

      for(StepHandler handler : this.stepHandlers) {
         handler.reset();
      }

      this.setStateInitialized(false);
      this.start(t0, y, t);
      interpolator.reinitialize(this.stepStart, this.stepSize, this.scaled, this.nordsieck);
      interpolator.storeTime(this.stepStart);
      int lastRow = this.nordsieck.getRowDimension() - 1;
      double hNew = this.stepSize;
      interpolator.rescale(hNew);
      this.isLastStep = false;

      do {
         double error = 10.0;

         while(error >= 1.0) {
            this.stepSize = hNew;
            error = 0.0;

            for(int i = 0; i < this.mainSetDimension; ++i) {
               double yScale = FastMath.abs(y[i]);
               double tol = this.vecAbsoluteTolerance == null
                  ? this.scalAbsoluteTolerance + this.scalRelativeTolerance * yScale
                  : this.vecAbsoluteTolerance[i] + this.vecRelativeTolerance[i] * yScale;
               double ratio = this.nordsieck.getEntry(lastRow, i) / tol;
               error += ratio * ratio;
            }

            error = FastMath.sqrt(error / (double)this.mainSetDimension);
            if (error >= 1.0) {
               double factor = this.computeStepGrowShrinkFactor(error);
               hNew = this.filterStep(this.stepSize * factor, forward, false);
               interpolator.rescale(hNew);
            }
         }

         double stepEnd = this.stepStart + this.stepSize;
         interpolator.shift();
         interpolator.setInterpolatedTime(stepEnd);
         System.arraycopy(interpolator.getInterpolatedState(), 0, y, 0, y0.length);
         this.computeDerivatives(stepEnd, y, yDot);
         double[] predictedScaled = new double[y0.length];

         for(int j = 0; j < y0.length; ++j) {
            predictedScaled[j] = this.stepSize * yDot[j];
         }

         Array2DRowRealMatrix nordsieckTmp = this.updateHighOrderDerivativesPhase1(this.nordsieck);
         this.updateHighOrderDerivativesPhase2(this.scaled, predictedScaled, nordsieckTmp);
         interpolator.reinitialize(stepEnd, this.stepSize, predictedScaled, nordsieckTmp);
         interpolator.storeTime(stepEnd);
         this.stepStart = this.acceptStep(interpolator, y, yDot, t);
         this.scaled = predictedScaled;
         this.nordsieck = nordsieckTmp;
         interpolator.reinitialize(stepEnd, this.stepSize, this.scaled, this.nordsieck);
         if (!this.isLastStep) {
            interpolator.storeTime(this.stepStart);
            if (this.resetOccurred) {
               this.start(this.stepStart, y, t);
               interpolator.reinitialize(this.stepStart, this.stepSize, this.scaled, this.nordsieck);
            }

            double factor = this.computeStepGrowShrinkFactor(error);
            double scaledH = this.stepSize * factor;
            double nextT = this.stepStart + scaledH;
            boolean nextIsLast = forward ? nextT >= t : nextT <= t;
            hNew = this.filterStep(scaledH, forward, nextIsLast);
            double filteredNextT = this.stepStart + hNew;
            boolean filteredNextIsLast = forward ? filteredNextT >= t : filteredNextT <= t;
            if (filteredNextIsLast) {
               hNew = t - this.stepStart;
            }

            interpolator.rescale(hNew);
         }
      } while(!this.isLastStep);

      double stopTime = this.stepStart;
      this.resetInternalState();
      return stopTime;
   }
}
