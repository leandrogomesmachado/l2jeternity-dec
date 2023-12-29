package org.apache.commons.math.optimization.direct;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;

public abstract class DirectSearchOptimizer implements MultivariateRealOptimizer {
   protected RealPointValuePair[] simplex;
   private MultivariateRealFunction f;
   private RealConvergenceChecker checker;
   private int maxIterations;
   private int iterations;
   private int maxEvaluations;
   private int evaluations;
   private double[][] startConfiguration;

   protected DirectSearchOptimizer() {
      this.setConvergenceChecker(new SimpleScalarValueChecker());
      this.setMaxIterations(Integer.MAX_VALUE);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   public void setStartConfiguration(double[] steps) throws IllegalArgumentException {
      int n = steps.length;
      this.startConfiguration = new double[n][n];

      for(int i = 0; i < n; ++i) {
         double[] vertexI = this.startConfiguration[i];

         for(int j = 0; j < i + 1; ++j) {
            if (steps[j] == 0.0) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.EQUAL_VERTICES_IN_SIMPLEX, j, j + 1);
            }

            System.arraycopy(steps, 0, vertexI, 0, j + 1);
         }
      }
   }

   public void setStartConfiguration(double[][] referenceSimplex) throws IllegalArgumentException {
      int n = referenceSimplex.length - 1;
      if (n < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.SIMPLEX_NEED_ONE_POINT);
      } else {
         this.startConfiguration = new double[n][n];
         double[] ref0 = referenceSimplex[0];

         for(int i = 0; i < n + 1; ++i) {
            double[] refI = referenceSimplex[i];
            if (refI.length != n) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, refI.length, n);
            }

            for(int j = 0; j < i; ++j) {
               double[] refJ = referenceSimplex[j];
               boolean allEquals = true;

               for(int k = 0; k < n; ++k) {
                  if (refI[k] != refJ[k]) {
                     allEquals = false;
                     break;
                  }
               }

               if (allEquals) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.EQUAL_VERTICES_IN_SIMPLEX, i, j);
               }
            }

            if (i > 0) {
               double[] confI = this.startConfiguration[i - 1];

               for(int k = 0; k < n; ++k) {
                  confI[k] = refI[k] - ref0[k];
               }
            }
         }
      }
   }

   @Override
   public void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }

   @Override
   public int getMaxIterations() {
      return this.maxIterations;
   }

   @Override
   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations;
   }

   @Override
   public int getMaxEvaluations() {
      return this.maxEvaluations;
   }

   @Override
   public int getIterations() {
      return this.iterations;
   }

   @Override
   public int getEvaluations() {
      return this.evaluations;
   }

   @Override
   public void setConvergenceChecker(RealConvergenceChecker convergenceChecker) {
      this.checker = convergenceChecker;
   }

   @Override
   public RealConvergenceChecker getConvergenceChecker() {
      return this.checker;
   }

   @Override
   public RealPointValuePair optimize(MultivariateRealFunction function, final GoalType goalType, double[] startPoint) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      if (this.startConfiguration == null || this.startConfiguration.length != startPoint.length) {
         double[] unit = new double[startPoint.length];
         Arrays.fill(unit, 1.0);
         this.setStartConfiguration(unit);
      }

      this.f = function;
      Comparator<RealPointValuePair> comparator = new Comparator<RealPointValuePair>() {
         public int compare(RealPointValuePair o1, RealPointValuePair o2) {
            double v1 = o1.getValue();
            double v2 = o2.getValue();
            return goalType == GoalType.MINIMIZE ? Double.compare(v1, v2) : Double.compare(v2, v1);
         }
      };
      this.iterations = 0;
      this.evaluations = 0;
      this.buildSimplex(startPoint);
      this.evaluateSimplex(comparator);
      RealPointValuePair[] previous = new RealPointValuePair[this.simplex.length];

      while(true) {
         if (this.iterations > 0) {
            boolean converged = true;

            for(int i = 0; i < this.simplex.length; ++i) {
               converged &= this.checker.converged(this.iterations, previous[i], this.simplex[i]);
            }

            if (converged) {
               return this.simplex[0];
            }
         }

         System.arraycopy(this.simplex, 0, previous, 0, this.simplex.length);
         this.iterateSimplex(comparator);
      }
   }

   protected void incrementIterationsCounter() throws OptimizationException {
      if (++this.iterations > this.maxIterations) {
         throw new OptimizationException(new MaxIterationsExceededException(this.maxIterations));
      }
   }

   protected abstract void iterateSimplex(Comparator<RealPointValuePair> var1) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException;

   protected double evaluate(double[] x) throws FunctionEvaluationException, IllegalArgumentException {
      if (++this.evaluations > this.maxEvaluations) {
         throw new FunctionEvaluationException(new MaxEvaluationsExceededException(this.maxEvaluations), x);
      } else {
         return this.f.value(x);
      }
   }

   private void buildSimplex(double[] startPoint) throws IllegalArgumentException {
      int n = startPoint.length;
      if (n != this.startConfiguration.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, n, this.startConfiguration.length);
      } else {
         this.simplex = new RealPointValuePair[n + 1];
         this.simplex[0] = new RealPointValuePair(startPoint, Double.NaN);

         for(int i = 0; i < n; ++i) {
            double[] confI = this.startConfiguration[i];
            double[] vertexI = new double[n];

            for(int k = 0; k < n; ++k) {
               vertexI[k] = startPoint[k] + confI[k];
            }

            this.simplex[i + 1] = new RealPointValuePair(vertexI, Double.NaN);
         }
      }
   }

   protected void evaluateSimplex(Comparator<RealPointValuePair> comparator) throws FunctionEvaluationException, OptimizationException {
      for(int i = 0; i < this.simplex.length; ++i) {
         RealPointValuePair vertex = this.simplex[i];
         double[] point = vertex.getPointRef();
         if (Double.isNaN(vertex.getValue())) {
            this.simplex[i] = new RealPointValuePair(point, this.evaluate(point), false);
         }
      }

      Arrays.sort(this.simplex, comparator);
   }

   protected void replaceWorstPoint(RealPointValuePair pointValuePair, Comparator<RealPointValuePair> comparator) {
      int n = this.simplex.length - 1;

      for(int i = 0; i < n; ++i) {
         if (comparator.compare(this.simplex[i], pointValuePair) > 0) {
            RealPointValuePair tmp = this.simplex[i];
            this.simplex[i] = pointValuePair;
            pointValuePair = tmp;
         }
      }

      this.simplex[n] = pointValuePair;
   }
}
