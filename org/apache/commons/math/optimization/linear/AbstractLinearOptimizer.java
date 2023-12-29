package org.apache.commons.math.optimization.linear;

import java.util.Collection;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;

public abstract class AbstractLinearOptimizer implements LinearOptimizer {
   public static final int DEFAULT_MAX_ITERATIONS = 100;
   protected LinearObjectiveFunction function;
   protected Collection<LinearConstraint> linearConstraints;
   protected GoalType goal;
   protected boolean nonNegative;
   private int maxIterations;
   private int iterations;

   protected AbstractLinearOptimizer() {
      this.setMaxIterations(100);
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
      return this.iterations;
   }

   protected void incrementIterationsCounter() throws OptimizationException {
      if (++this.iterations > this.maxIterations) {
         throw new OptimizationException(new MaxIterationsExceededException(this.maxIterations));
      }
   }

   @Override
   public RealPointValuePair optimize(LinearObjectiveFunction f, Collection<LinearConstraint> constraints, GoalType goalType, boolean restrictToNonNegative) throws OptimizationException {
      this.function = f;
      this.linearConstraints = constraints;
      this.goal = goalType;
      this.nonNegative = restrictToNonNegative;
      this.iterations = 0;
      return this.doOptimize();
   }

   protected abstract RealPointValuePair doOptimize() throws OptimizationException;
}
