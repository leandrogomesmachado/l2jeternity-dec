package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.MultistepIntegrator;

public abstract class AdamsIntegrator extends MultistepIntegrator {
   private final AdamsNordsieckTransformer transformer;

   public AdamsIntegrator(String name, int nSteps, int order, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance) throws IllegalArgumentException {
      super(name, nSteps, order, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
      this.transformer = AdamsNordsieckTransformer.getInstance(nSteps);
   }

   public AdamsIntegrator(String name, int nSteps, int order, double minStep, double maxStep, double[] vecAbsoluteTolerance, double[] vecRelativeTolerance) throws IllegalArgumentException {
      super(name, nSteps, order, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
      this.transformer = AdamsNordsieckTransformer.getInstance(nSteps);
   }

   @Override
   public abstract double integrate(FirstOrderDifferentialEquations var1, double var2, double[] var4, double var5, double[] var7) throws DerivativeException, IntegratorException;

   @Override
   protected Array2DRowRealMatrix initializeHighOrderDerivatives(double[] first, double[][] multistep) {
      return this.transformer.initializeHighOrderDerivatives(first, multistep);
   }

   public Array2DRowRealMatrix updateHighOrderDerivativesPhase1(Array2DRowRealMatrix highOrder) {
      return this.transformer.updateHighOrderDerivativesPhase1(highOrder);
   }

   public void updateHighOrderDerivativesPhase2(double[] start, double[] end, Array2DRowRealMatrix highOrder) {
      this.transformer.updateHighOrderDerivativesPhase2(start, end, highOrder);
   }
}
