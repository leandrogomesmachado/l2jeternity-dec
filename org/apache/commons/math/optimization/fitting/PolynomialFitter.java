package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;

public class PolynomialFitter {
   private final CurveFitter fitter;
   private final int degree;

   public PolynomialFitter(int degree, DifferentiableMultivariateVectorialOptimizer optimizer) {
      this.fitter = new CurveFitter(optimizer);
      this.degree = degree;
   }

   public void addObservedPoint(double weight, double x, double y) {
      this.fitter.addObservedPoint(weight, x, y);
   }

   public void clearObservations() {
      this.fitter.clearObservations();
   }

   public PolynomialFunction fit() throws OptimizationException {
      try {
         return new PolynomialFunction(this.fitter.fit(new PolynomialFitter.ParametricPolynomial(), new double[this.degree + 1]));
      } catch (FunctionEvaluationException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static class ParametricPolynomial implements ParametricRealFunction {
      private ParametricPolynomial() {
      }

      @Override
      public double[] gradient(double x, double[] parameters) {
         double[] gradient = new double[parameters.length];
         double xn = 1.0;

         for(int i = 0; i < parameters.length; ++i) {
            gradient[i] = xn;
            xn *= x;
         }

         return gradient;
      }

      @Override
      public double value(double x, double[] parameters) {
         double y = 0.0;

         for(int i = parameters.length - 1; i >= 0; --i) {
            y = y * x + parameters[i];
         }

         return y;
      }
   }
}
