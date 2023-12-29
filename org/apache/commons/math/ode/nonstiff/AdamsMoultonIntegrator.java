package org.apache.commons.math.ode.nonstiff;

import java.util.Arrays;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.sampling.NordsieckStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.util.FastMath;

public class AdamsMoultonIntegrator extends AdamsIntegrator {
   private static final String METHOD_NAME = "Adams-Moulton";

   public AdamsMoultonIntegrator(int nSteps, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance) throws IllegalArgumentException {
      super("Adams-Moulton", nSteps, nSteps + 1, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
   }

   public AdamsMoultonIntegrator(int nSteps, double minStep, double maxStep, double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) throws IllegalArgumentException {
      super("Adams-Moulton", nSteps, nSteps + 1, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
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

      double[] yDot = new double[y0.length];
      double[] yTmp = new double[y0.length];
      double[] predictedScaled = new double[y0.length];
      Array2DRowRealMatrix nordsieckTmp = null;
      NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
      interpolator.reinitialize(y, forward);

      for(StepHandler handler : this.stepHandlers) {
         handler.reset();
      }

      this.setStateInitialized(false);
      this.start(t0, y, t);
      interpolator.reinitialize(this.stepStart, this.stepSize, this.scaled, this.nordsieck);
      interpolator.storeTime(this.stepStart);
      double hNew = this.stepSize;
      interpolator.rescale(hNew);
      this.isLastStep = false;

      do {
         double error = 10.0;

         while(error >= 1.0) {
            this.stepSize = hNew;
            double stepEnd = this.stepStart + this.stepSize;
            interpolator.setInterpolatedTime(stepEnd);
            System.arraycopy(interpolator.getInterpolatedState(), 0, yTmp, 0, y0.length);
            this.computeDerivatives(stepEnd, yTmp, yDot);

            for(int j = 0; j < y0.length; ++j) {
               predictedScaled[j] = this.stepSize * yDot[j];
            }

            nordsieckTmp = this.updateHighOrderDerivativesPhase1(this.nordsieck);
            this.updateHighOrderDerivativesPhase2(this.scaled, predictedScaled, nordsieckTmp);
            error = nordsieckTmp.walkInOptimizedOrder(new AdamsMoultonIntegrator.Corrector(y, predictedScaled, yTmp));
            if (error >= 1.0) {
               double factor = this.computeStepGrowShrinkFactor(error);
               hNew = this.filterStep(this.stepSize * factor, forward, false);
               interpolator.rescale(hNew);
            }
         }

         double stepEnd = this.stepStart + this.stepSize;
         this.computeDerivatives(stepEnd, yTmp, yDot);
         double[] correctedScaled = new double[y0.length];

         for(int j = 0; j < y0.length; ++j) {
            correctedScaled[j] = this.stepSize * yDot[j];
         }

         this.updateHighOrderDerivativesPhase2(predictedScaled, correctedScaled, nordsieckTmp);
         System.arraycopy(yTmp, 0, y, 0, n);
         interpolator.reinitialize(stepEnd, this.stepSize, correctedScaled, nordsieckTmp);
         interpolator.storeTime(this.stepStart);
         interpolator.shift();
         interpolator.storeTime(stepEnd);
         this.stepStart = this.acceptStep(interpolator, y, yDot, t);
         this.scaled = correctedScaled;
         this.nordsieck = nordsieckTmp;
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
      this.stepStart = Double.NaN;
      this.stepSize = Double.NaN;
      return stopTime;
   }

   private class Corrector implements RealMatrixPreservingVisitor {
      private final double[] previous;
      private final double[] scaled;
      private final double[] before;
      private final double[] after;

      public Corrector(double[] previous, double[] scaled, double[] state) {
         this.previous = previous;
         this.scaled = scaled;
         this.after = state;
         this.before = (double[])state.clone();
      }

      @Override
      public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
         Arrays.fill(this.after, 0.0);
      }

      @Override
      public void visit(int row, int column, double value) {
         if ((row & 1) == 0) {
            this.after[column] -= value;
         } else {
            this.after[column] += value;
         }
      }

      @Override
      public double end() {
         double error = 0.0;

         for(int i = 0; i < this.after.length; ++i) {
            this.after[i] += this.previous[i] + this.scaled[i];
            if (i < AdamsMoultonIntegrator.this.mainSetDimension) {
               double yScale = FastMath.max(FastMath.abs(this.previous[i]), FastMath.abs(this.after[i]));
               double tol = AdamsMoultonIntegrator.this.vecAbsoluteTolerance == null
                  ? AdamsMoultonIntegrator.this.scalAbsoluteTolerance + AdamsMoultonIntegrator.this.scalRelativeTolerance * yScale
                  : AdamsMoultonIntegrator.this.vecAbsoluteTolerance[i] + AdamsMoultonIntegrator.this.vecRelativeTolerance[i] * yScale;
               double ratio = (this.after[i] - this.before[i]) / tol;
               error += ratio * ratio;
            }
         }

         return FastMath.sqrt(error / (double)AdamsMoultonIntegrator.this.mainSetDimension);
      }
   }
}
