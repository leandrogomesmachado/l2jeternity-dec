package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.ExtendedFirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.util.FastMath;

public abstract class AdaptiveStepsizeIntegrator extends AbstractIntegrator {
   protected final double scalAbsoluteTolerance;
   protected final double scalRelativeTolerance;
   protected final double[] vecAbsoluteTolerance;
   protected final double[] vecRelativeTolerance;
   protected int mainSetDimension;
   private double initialStep;
   private final double minStep;
   private final double maxStep;

   public AdaptiveStepsizeIntegrator(String name, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance) {
      super(name);
      this.minStep = FastMath.abs(minStep);
      this.maxStep = FastMath.abs(maxStep);
      this.initialStep = -1.0;
      this.scalAbsoluteTolerance = scalAbsoluteTolerance;
      this.scalRelativeTolerance = scalRelativeTolerance;
      this.vecAbsoluteTolerance = null;
      this.vecRelativeTolerance = null;
      this.resetInternalState();
   }

   public AdaptiveStepsizeIntegrator(String name, double minStep, double maxStep, double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) {
      super(name);
      this.minStep = minStep;
      this.maxStep = maxStep;
      this.initialStep = -1.0;
      this.scalAbsoluteTolerance = 0.0;
      this.scalRelativeTolerance = 0.0;
      this.vecAbsoluteTolerance = (double[])vecAbsoluteTolerance.clone();
      this.vecRelativeTolerance = (double[])vecRelativeTolerance.clone();
      this.resetInternalState();
   }

   public void setInitialStepSize(double initialStepSize) {
      if (!(initialStepSize < this.minStep) && !(initialStepSize > this.maxStep)) {
         this.initialStep = initialStepSize;
      } else {
         this.initialStep = -1.0;
      }
   }

   @Override
   protected void sanityChecks(FirstOrderDifferentialEquations equations, double t0, double[] y0, double t, double[] y) throws IntegratorException {
      super.sanityChecks(equations, t0, y0, t, y);
      if (equations instanceof ExtendedFirstOrderDifferentialEquations) {
         this.mainSetDimension = ((ExtendedFirstOrderDifferentialEquations)equations).getMainSetDimension();
      } else {
         this.mainSetDimension = equations.getDimension();
      }

      if (this.vecAbsoluteTolerance != null && this.vecAbsoluteTolerance.length != this.mainSetDimension) {
         throw new IntegratorException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, this.mainSetDimension, this.vecAbsoluteTolerance.length);
      } else if (this.vecRelativeTolerance != null && this.vecRelativeTolerance.length != this.mainSetDimension) {
         throw new IntegratorException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, this.mainSetDimension, this.vecRelativeTolerance.length);
      }
   }

   public double initializeStep(
      FirstOrderDifferentialEquations equations,
      boolean forward,
      int order,
      double[] scale,
      double t0,
      double[] y0,
      double[] yDot0,
      double[] y1,
      double[] yDot1
   ) throws DerivativeException {
      if (this.initialStep > 0.0) {
         return forward ? this.initialStep : -this.initialStep;
      } else {
         double yOnScale2 = 0.0;
         double yDotOnScale2 = 0.0;

         for(int j = 0; j < scale.length; ++j) {
            double ratio = y0[j] / scale[j];
            yOnScale2 += ratio * ratio;
            ratio = yDot0[j] / scale[j];
            yDotOnScale2 += ratio * ratio;
         }

         double h = !(yOnScale2 < 1.0E-10) && !(yDotOnScale2 < 1.0E-10) ? 0.01 * FastMath.sqrt(yOnScale2 / yDotOnScale2) : 1.0E-6;
         if (!forward) {
            h = -h;
         }

         for(int j = 0; j < y0.length; ++j) {
            y1[j] = y0[j] + h * yDot0[j];
         }

         this.computeDerivatives(t0 + h, y1, yDot1);
         double yDDotOnScale = 0.0;

         for(int j = 0; j < scale.length; ++j) {
            double ratio = (yDot1[j] - yDot0[j]) / scale[j];
            yDDotOnScale += ratio * ratio;
         }

         yDDotOnScale = FastMath.sqrt(yDDotOnScale) / h;
         double maxInv2 = FastMath.max(FastMath.sqrt(yDotOnScale2), yDDotOnScale);
         double h1 = maxInv2 < 1.0E-15 ? FastMath.max(1.0E-6, 0.001 * FastMath.abs(h)) : FastMath.pow(0.01 / maxInv2, 1.0 / (double)order);
         h = FastMath.min(100.0 * FastMath.abs(h), h1);
         h = FastMath.max(h, 1.0E-12 * FastMath.abs(t0));
         if (h < this.getMinStep()) {
            h = this.getMinStep();
         }

         if (h > this.getMaxStep()) {
            h = this.getMaxStep();
         }

         if (!forward) {
            h = -h;
         }

         return h;
      }
   }

   protected double filterStep(double h, boolean forward, boolean acceptSmall) throws IntegratorException {
      double filteredH = h;
      if (FastMath.abs(h) < this.minStep) {
         if (!acceptSmall) {
            throw new IntegratorException(LocalizedFormats.MINIMAL_STEPSIZE_REACHED_DURING_INTEGRATION, this.minStep, FastMath.abs(h));
         }

         filteredH = forward ? this.minStep : -this.minStep;
      }

      if (filteredH > this.maxStep) {
         filteredH = this.maxStep;
      } else if (filteredH < -this.maxStep) {
         filteredH = -this.maxStep;
      }

      return filteredH;
   }

   @Override
   public abstract double integrate(FirstOrderDifferentialEquations var1, double var2, double[] var4, double var5, double[] var7) throws DerivativeException, IntegratorException;

   @Override
   public double getCurrentStepStart() {
      return this.stepStart;
   }

   protected void resetInternalState() {
      this.stepStart = Double.NaN;
      this.stepSize = FastMath.sqrt(this.minStep * this.maxStep);
   }

   public double getMinStep() {
      return this.minStep;
   }

   public double getMaxStep() {
      return this.maxStep;
   }
}
