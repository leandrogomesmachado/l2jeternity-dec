package org.apache.commons.math.ode;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.apache.commons.math.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.util.FastMath;

public abstract class MultistepIntegrator extends AdaptiveStepsizeIntegrator {
   protected double[] scaled;
   protected Array2DRowRealMatrix nordsieck;
   private FirstOrderIntegrator starter;
   private final int nSteps;
   private double exp;
   private double safety;
   private double minReduction;
   private double maxGrowth;

   protected MultistepIntegrator(
      String name, int nSteps, int order, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance
   ) {
      super(name, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
      if (nSteps <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INTEGRATION_METHOD_NEEDS_AT_LEAST_ONE_PREVIOUS_POINT, name);
      } else {
         this.starter = new DormandPrince853Integrator(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
         this.nSteps = nSteps;
         this.exp = -1.0 / (double)order;
         this.setSafety(0.9);
         this.setMinReduction(0.2);
         this.setMaxGrowth(FastMath.pow(2.0, -this.exp));
      }
   }

   protected MultistepIntegrator(
      String name, int nSteps, int order, double minStep, double maxStep, double[] vecAbsoluteTolerance, double[] vecRelativeTolerance
   ) {
      super(name, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
      this.starter = new DormandPrince853Integrator(minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
      this.nSteps = nSteps;
      this.exp = -1.0 / (double)order;
      this.setSafety(0.9);
      this.setMinReduction(0.2);
      this.setMaxGrowth(FastMath.pow(2.0, -this.exp));
   }

   public ODEIntegrator getStarterIntegrator() {
      return this.starter;
   }

   public void setStarterIntegrator(FirstOrderIntegrator starterIntegrator) {
      this.starter = starterIntegrator;
   }

   protected void start(double t0, double[] y0, double t) throws DerivativeException, IntegratorException {
      this.starter.clearEventHandlers();
      this.starter.clearStepHandlers();
      this.starter.addStepHandler(new MultistepIntegrator.NordsieckInitializer(y0.length));

      try {
         this.starter.integrate(new MultistepIntegrator.CountingDifferentialEquations(y0.length), t0, y0, t, new double[y0.length]);
      } catch (DerivativeException var7) {
         if (!(var7 instanceof MultistepIntegrator.InitializationCompletedMarkerException)) {
            throw var7;
         }
      }

      this.starter.clearStepHandlers();
   }

   protected abstract Array2DRowRealMatrix initializeHighOrderDerivatives(double[] var1, double[][] var2);

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

   public double getSafety() {
      return this.safety;
   }

   public void setSafety(double safety) {
      this.safety = safety;
   }

   protected double computeStepGrowShrinkFactor(double error) {
      return FastMath.min(this.maxGrowth, FastMath.max(this.minReduction, this.safety * FastMath.pow(error, this.exp)));
   }

   private class CountingDifferentialEquations implements ExtendedFirstOrderDifferentialEquations {
      private final int dimension;

      public CountingDifferentialEquations(int dimension) {
         this.dimension = dimension;
      }

      @Override
      public void computeDerivatives(double t, double[] y, double[] dot) throws DerivativeException {
         MultistepIntegrator.this.computeDerivatives(t, y, dot);
      }

      @Override
      public int getDimension() {
         return this.dimension;
      }

      @Override
      public int getMainSetDimension() {
         return MultistepIntegrator.this.mainSetDimension;
      }
   }

   private static class InitializationCompletedMarkerException extends DerivativeException {
      private static final long serialVersionUID = -4105805787353488365L;

      public InitializationCompletedMarkerException() {
         super((Throwable)null);
      }
   }

   private class NordsieckInitializer implements StepHandler {
      private final int n;

      public NordsieckInitializer(int n) {
         this.n = n;
      }

      @Override
      public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
         double prev = interpolator.getPreviousTime();
         double curr = interpolator.getCurrentTime();
         MultistepIntegrator.this.stepStart = prev;
         MultistepIntegrator.this.stepSize = (curr - prev) / (double)(MultistepIntegrator.this.nSteps + 1);
         interpolator.setInterpolatedTime(prev);
         MultistepIntegrator.this.scaled = (double[])interpolator.getInterpolatedDerivatives().clone();

         for(int j = 0; j < this.n; ++j) {
            MultistepIntegrator.this.scaled[j] *= MultistepIntegrator.this.stepSize;
         }

         double[][] multistep = new double[MultistepIntegrator.this.nSteps][];

         for(int i = 1; i <= MultistepIntegrator.this.nSteps; ++i) {
            interpolator.setInterpolatedTime(prev + MultistepIntegrator.this.stepSize * (double)i);
            double[] msI = (double[])interpolator.getInterpolatedDerivatives().clone();

            for(int j = 0; j < this.n; ++j) {
               msI[j] *= MultistepIntegrator.this.stepSize;
            }

            multistep[i - 1] = msI;
         }

         MultistepIntegrator.this.nordsieck = MultistepIntegrator.this.initializeHighOrderDerivatives(MultistepIntegrator.this.scaled, multistep);
         throw new MultistepIntegrator.InitializationCompletedMarkerException();
      }

      @Override
      public boolean requiresDenseOutput() {
         return true;
      }

      @Override
      public void reset() {
      }
   }

   public interface NordsieckTransformer {
      RealMatrix initializeHighOrderDerivatives(double[] var1, double[][] var2);
   }
}
