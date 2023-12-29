package org.apache.commons.math.optimization.fitting;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;

public class CurveFitter {
   private final DifferentiableMultivariateVectorialOptimizer optimizer;
   private final List<WeightedObservedPoint> observations;

   public CurveFitter(DifferentiableMultivariateVectorialOptimizer optimizer) {
      this.optimizer = optimizer;
      this.observations = new ArrayList<>();
   }

   public void addObservedPoint(double x, double y) {
      this.addObservedPoint(1.0, x, y);
   }

   public void addObservedPoint(double weight, double x, double y) {
      this.observations.add(new WeightedObservedPoint(weight, x, y));
   }

   public void addObservedPoint(WeightedObservedPoint observed) {
      this.observations.add(observed);
   }

   public WeightedObservedPoint[] getObservations() {
      return this.observations.toArray(new WeightedObservedPoint[this.observations.size()]);
   }

   public void clearObservations() {
      this.observations.clear();
   }

   public double[] fit(ParametricRealFunction f, double[] initialGuess) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      double[] target = new double[this.observations.size()];
      double[] weights = new double[this.observations.size()];
      int i = 0;

      for(WeightedObservedPoint point : this.observations) {
         target[i] = point.getY();
         weights[i] = point.getWeight();
         ++i;
      }

      VectorialPointValuePair optimum = this.optimizer.optimize(new CurveFitter.TheoreticalValuesFunction(f), target, weights, initialGuess);
      return optimum.getPointRef();
   }

   private class TheoreticalValuesFunction implements DifferentiableMultivariateVectorialFunction {
      private final ParametricRealFunction f;

      public TheoreticalValuesFunction(ParametricRealFunction f) {
         this.f = f;
      }

      @Override
      public MultivariateMatrixFunction jacobian() {
         return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
               double[][] jacobian = new double[CurveFitter.this.observations.size()][];
               int i = 0;

               for(WeightedObservedPoint observed : CurveFitter.this.observations) {
                  jacobian[i++] = TheoreticalValuesFunction.this.f.gradient(observed.getX(), point);
               }

               return jacobian;
            }
         };
      }

      @Override
      public double[] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
         double[] values = new double[CurveFitter.this.observations.size()];
         int i = 0;

         for(WeightedObservedPoint observed : CurveFitter.this.observations) {
            values[i++] = this.f.value(observed.getX(), point);
         }

         return values;
      }
   }
}
