package org.apache.commons.math.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.random.RandomGenerator;
import org.apache.commons.math.util.FastMath;

public class MultiStartUnivariateRealOptimizer implements UnivariateRealOptimizer {
   private static final long serialVersionUID = 5983375963110961019L;
   private final UnivariateRealOptimizer optimizer;
   private int maxIterations;
   private int maxEvaluations;
   private int totalIterations;
   private int totalEvaluations;
   private int starts;
   private RandomGenerator generator;
   private double[] optima;
   private double[] optimaValues;

   public MultiStartUnivariateRealOptimizer(UnivariateRealOptimizer optimizer, int starts, RandomGenerator generator) {
      this.optimizer = optimizer;
      this.totalIterations = 0;
      this.starts = starts;
      this.generator = generator;
      this.optima = null;
      this.setMaximalIterationCount(Integer.MAX_VALUE);
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   @Override
   public double getFunctionValue() {
      return this.optimaValues[0];
   }

   @Override
   public double getResult() {
      return this.optima[0];
   }

   @Override
   public double getAbsoluteAccuracy() {
      return this.optimizer.getAbsoluteAccuracy();
   }

   @Override
   public int getIterationCount() {
      return this.totalIterations;
   }

   @Override
   public int getMaximalIterationCount() {
      return this.maxIterations;
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
   public double getRelativeAccuracy() {
      return this.optimizer.getRelativeAccuracy();
   }

   @Override
   public void resetAbsoluteAccuracy() {
      this.optimizer.resetAbsoluteAccuracy();
   }

   @Override
   public void resetMaximalIterationCount() {
      this.optimizer.resetMaximalIterationCount();
   }

   @Override
   public void resetRelativeAccuracy() {
      this.optimizer.resetRelativeAccuracy();
   }

   @Override
   public void setAbsoluteAccuracy(double accuracy) {
      this.optimizer.setAbsoluteAccuracy(accuracy);
   }

   @Override
   public void setMaximalIterationCount(int count) {
      this.maxIterations = count;
   }

   @Override
   public void setMaxEvaluations(int maxEvaluations) {
      this.maxEvaluations = maxEvaluations;
   }

   @Override
   public void setRelativeAccuracy(double accuracy) {
      this.optimizer.setRelativeAccuracy(accuracy);
   }

   public double[] getOptima() throws IllegalStateException {
      if (this.optima == null) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
      } else {
         return (double[])this.optima.clone();
      }
   }

   public double[] getOptimaValues() throws IllegalStateException {
      if (this.optimaValues == null) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
      } else {
         return (double[])this.optimaValues.clone();
      }
   }

   @Override
   public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max) throws ConvergenceException, FunctionEvaluationException {
      this.optima = new double[this.starts];
      this.optimaValues = new double[this.starts];
      this.totalIterations = 0;
      this.totalEvaluations = 0;

      for(int i = 0; i < this.starts; ++i) {
         try {
            this.optimizer.setMaximalIterationCount(this.maxIterations - this.totalIterations);
            this.optimizer.setMaxEvaluations(this.maxEvaluations - this.totalEvaluations);
            double bound1 = i == 0 ? min : min + this.generator.nextDouble() * (max - min);
            double bound2 = i == 0 ? max : min + this.generator.nextDouble() * (max - min);
            this.optima[i] = this.optimizer.optimize(f, goalType, FastMath.min(bound1, bound2), FastMath.max(bound1, bound2));
            this.optimaValues[i] = this.optimizer.getFunctionValue();
         } catch (FunctionEvaluationException var20) {
            this.optima[i] = Double.NaN;
            this.optimaValues[i] = Double.NaN;
         } catch (ConvergenceException var21) {
            this.optima[i] = Double.NaN;
            this.optimaValues[i] = Double.NaN;
         }

         this.totalIterations += this.optimizer.getIterationCount();
         this.totalEvaluations += this.optimizer.getEvaluations();
      }

      int lastNaN = this.optima.length;

      for(int i = 0; i < lastNaN; ++i) {
         if (Double.isNaN(this.optima[i])) {
            this.optima[i] = this.optima[--lastNaN];
            this.optima[lastNaN + 1] = Double.NaN;
            this.optimaValues[i] = this.optimaValues[--lastNaN];
            this.optimaValues[lastNaN + 1] = Double.NaN;
         }
      }

      double currX = this.optima[0];
      double currY = this.optimaValues[0];

      for(int j = 1; j < lastNaN; ++j) {
         double prevY = currY;
         currX = this.optima[j];
         currY = this.optimaValues[j];
         if (goalType == GoalType.MAXIMIZE ^ currY < prevY) {
            int i = j - 1;
            double mIX = this.optima[i];
            double mIY = this.optimaValues[i];

            while(i >= 0 && goalType == GoalType.MAXIMIZE ^ currY < mIY) {
               this.optima[i + 1] = mIX;
               this.optimaValues[i + 1] = mIY;
               if (i-- != 0) {
                  mIX = this.optima[i];
                  mIY = this.optimaValues[i];
               } else {
                  mIX = Double.NaN;
                  mIY = Double.NaN;
               }
            }

            this.optima[i + 1] = currX;
            this.optimaValues[i + 1] = currY;
            currX = this.optima[j];
            currY = this.optimaValues[j];
         }
      }

      if (Double.isNaN(this.optima[0])) {
         throw new OptimizationException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT, this.starts);
      } else {
         return this.optima[0];
      }
   }

   @Override
   public double optimize(UnivariateRealFunction f, GoalType goalType, double min, double max, double startValue) throws ConvergenceException, FunctionEvaluationException {
      return this.optimize(f, goalType, min, max);
   }
}
