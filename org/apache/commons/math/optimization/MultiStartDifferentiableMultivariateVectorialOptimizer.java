package org.apache.commons.math.optimization;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.random.RandomVectorGenerator;

public class MultiStartDifferentiableMultivariateVectorialOptimizer implements DifferentiableMultivariateVectorialOptimizer {
   private static final long serialVersionUID = 9206382258980561530L;
   private final DifferentiableMultivariateVectorialOptimizer optimizer;
   private int maxIterations;
   private int totalIterations;
   private int maxEvaluations;
   private int totalEvaluations;
   private int totalJacobianEvaluations;
   private int starts;
   private RandomVectorGenerator generator;
   private VectorialPointValuePair[] optima;

   public MultiStartDifferentiableMultivariateVectorialOptimizer(
      DifferentiableMultivariateVectorialOptimizer optimizer, int starts, RandomVectorGenerator generator
   ) {
      this.optimizer = optimizer;
      this.totalIterations = 0;
      this.totalEvaluations = 0;
      this.totalJacobianEvaluations = 0;
      this.starts = starts;
      this.generator = generator;
      this.optima = null;
      this.setMaxIterations(Integer.MAX_VALUE);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   public VectorialPointValuePair[] getOptima() throws IllegalStateException {
      if (this.optima == null) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
      } else {
         return (VectorialPointValuePair[])this.optima.clone();
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
   public int getIterations() {
      return this.totalIterations;
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
   public int getEvaluations() {
      return this.totalEvaluations;
   }

   @Override
   public int getJacobianEvaluations() {
      return this.totalJacobianEvaluations;
   }

   @Override
   public void setConvergenceChecker(VectorialConvergenceChecker checker) {
      this.optimizer.setConvergenceChecker(checker);
   }

   @Override
   public VectorialConvergenceChecker getConvergenceChecker() {
      return this.optimizer.getConvergenceChecker();
   }

   @Override
   public VectorialPointValuePair optimize(DifferentiableMultivariateVectorialFunction f, final double[] target, final double[] weights, double[] startPoint) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
      this.optima = new VectorialPointValuePair[this.starts];
      this.totalIterations = 0;
      this.totalEvaluations = 0;
      this.totalJacobianEvaluations = 0;

      for(int i = 0; i < this.starts; ++i) {
         try {
            this.optimizer.setMaxIterations(this.maxIterations - this.totalIterations);
            this.optimizer.setMaxEvaluations(this.maxEvaluations - this.totalEvaluations);
            this.optima[i] = this.optimizer.optimize(f, target, weights, i == 0 ? startPoint : this.generator.nextVector());
         } catch (FunctionEvaluationException var7) {
            this.optima[i] = null;
         } catch (OptimizationException var8) {
            this.optima[i] = null;
         }

         this.totalIterations += this.optimizer.getIterations();
         this.totalEvaluations += this.optimizer.getEvaluations();
         this.totalJacobianEvaluations += this.optimizer.getJacobianEvaluations();
      }

      Arrays.sort(this.optima, new Comparator<VectorialPointValuePair>() {
         public int compare(VectorialPointValuePair o1, VectorialPointValuePair o2) {
            if (o1 == null) {
               return o2 == null ? 0 : 1;
            } else {
               return o2 == null ? -1 : Double.compare(this.weightedResidual(o1), this.weightedResidual(o2));
            }
         }

         private double weightedResidual(VectorialPointValuePair pv) {
            double[] value = pv.getValueRef();
            double sum = 0.0;

            for(int i = 0; i < value.length; ++i) {
               double ri = value[i] - target[i];
               sum += weights[i] * ri * ri;
            }

            return sum;
         }
      });
      if (this.optima[0] == null) {
         throw new OptimizationException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT, this.starts);
      } else {
         return this.optima[0];
      }
   }
}
