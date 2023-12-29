package org.apache.commons.math.optimization;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.random.RandomVectorGenerator;

public class MultiStartMultivariateRealOptimizer implements MultivariateRealOptimizer {
   private final MultivariateRealOptimizer optimizer;
   private int maxIterations;
   private int maxEvaluations;
   private int totalIterations;
   private int totalEvaluations;
   private int starts;
   private RandomVectorGenerator generator;
   private RealPointValuePair[] optima;

   public MultiStartMultivariateRealOptimizer(MultivariateRealOptimizer optimizer, int starts, RandomVectorGenerator generator) {
      this.optimizer = optimizer;
      this.totalIterations = 0;
      this.totalEvaluations = 0;
      this.starts = starts;
      this.generator = generator;
      this.optima = null;
      this.setMaxIterations(Integer.MAX_VALUE);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   public RealPointValuePair[] getOptima() throws IllegalStateException {
      if (this.optima == null) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
      } else {
         return (RealPointValuePair[])this.optima.clone();
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
      return this.totalIterations;
   }

   @Override
   public int getEvaluations() {
      return this.totalEvaluations;
   }

   @Override
   public void setConvergenceChecker(RealConvergenceChecker checker) {
      this.optimizer.setConvergenceChecker(checker);
   }

   @Override
   public RealConvergenceChecker getConvergenceChecker() {
      return this.optimizer.getConvergenceChecker();
   }

   @Override
   public RealPointValuePair optimize(MultivariateRealFunction f, final GoalType goalType, double[] startPoint) throws FunctionEvaluationException, OptimizationException, FunctionEvaluationException {
      this.optima = new RealPointValuePair[this.starts];
      this.totalIterations = 0;
      this.totalEvaluations = 0;

      for(int i = 0; i < this.starts; ++i) {
         try {
            this.optimizer.setMaxIterations(this.maxIterations - this.totalIterations);
            this.optimizer.setMaxEvaluations(this.maxEvaluations - this.totalEvaluations);
            this.optima[i] = this.optimizer.optimize(f, goalType, i == 0 ? startPoint : this.generator.nextVector());
         } catch (FunctionEvaluationException var6) {
            this.optima[i] = null;
         } catch (OptimizationException var7) {
            this.optima[i] = null;
         }

         this.totalIterations += this.optimizer.getIterations();
         this.totalEvaluations += this.optimizer.getEvaluations();
      }

      Arrays.sort(this.optima, new Comparator<RealPointValuePair>() {
         public int compare(RealPointValuePair o1, RealPointValuePair o2) {
            if (o1 == null) {
               return o2 == null ? 0 : 1;
            } else if (o2 == null) {
               return -1;
            } else {
               double v1 = o1.getValue();
               double v2 = o2.getValue();
               return goalType == GoalType.MINIMIZE ? Double.compare(v1, v2) : Double.compare(v2, v1);
            }
         }
      });
      if (this.optima[0] == null) {
         throw new OptimizationException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT, this.starts);
      } else {
         return this.optima[0];
      }
   }
}
