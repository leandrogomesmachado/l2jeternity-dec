package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;

public class GaussianFitter {
   private final CurveFitter fitter;

   public GaussianFitter(DifferentiableMultivariateVectorialOptimizer optimizer) {
      this.fitter = new CurveFitter(optimizer);
   }

   public void addObservedPoint(double x, double y) {
      this.addObservedPoint(1.0, x, y);
   }

   public void addObservedPoint(double weight, double x, double y) {
      this.fitter.addObservedPoint(weight, x, y);
   }

   public GaussianFunction fit() throws FunctionEvaluationException, OptimizationException {
      return new GaussianFunction(this.fitter.fit(new ParametricGaussianFunction(), this.createParametersGuesser(this.fitter.getObservations()).guess()));
   }

   protected GaussianParametersGuesser createParametersGuesser(WeightedObservedPoint[] observations) {
      return new GaussianParametersGuesser(observations);
   }
}
