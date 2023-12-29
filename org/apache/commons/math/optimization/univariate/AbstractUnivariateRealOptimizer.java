package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.ConvergingAlgorithmImpl;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxEvaluationsExceededException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.MathUnsupportedOperationException;
import org.apache.commons.math.exception.NoDataException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.UnivariateRealOptimizer;

public abstract class AbstractUnivariateRealOptimizer extends ConvergingAlgorithmImpl implements UnivariateRealOptimizer {
   protected boolean resultComputed;
   protected double result;
   protected double functionValue;
   private int maxEvaluations;
   private int evaluations;
   private GoalType optimizationGoal;
   private double searchMin;
   private double searchMax;
   private double searchStart;
   private UnivariateRealFunction function;

   @Deprecated
   protected AbstractUnivariateRealOptimizer(int defaultMaximalIterationCount, double defaultAbsoluteAccuracy) {
      super(defaultMaximalIterationCount, defaultAbsoluteAccuracy);
      this.resultComputed = false;
      this.setMaxEvaluations(Integer.MAX_VALUE);
   }

   protected AbstractUnivariateRealOptimizer() {
   }

   @Deprecated
   protected void checkResultComputed() {
      if (!this.resultComputed) {
         throw new NoDataException();
      }
   }

   @Override
   public double getResult() {
      if (!this.resultComputed) {
         throw new NoDataException();
      } else {
         return this.result;
      }
   }

   @Override
   public double getFunctionValue() throws FunctionEvaluationException {
      if (Double.isNaN(this.functionValue)) {
         double opt = this.getResult();
         this.functionValue = this.function.value(opt);
      }

      return this.functionValue;
   }

   @Deprecated
   protected final void setResult(double x, double fx, int iterationCount) {
      this.result = x;
      this.functionValue = fx;
      this.iterationCount = iterationCount;
      this.resultComputed = true;
   }

   @Deprecated
   protected final void clearResult() {
      this.resultComputed = false;
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
      return this.evaluations;
   }

   public GoalType getGoalType() {
      return this.optimizationGoal;
   }

   public double getMin() {
      return this.searchMin;
   }

   public double getMax() {
      return this.searchMax;
   }

   public double getStartValue() {
      return this.searchStart;
   }

   @Deprecated
   protected double computeObjectiveValue(UnivariateRealFunction f, double point) throws FunctionEvaluationException {
      if (++this.evaluations > this.maxEvaluations) {
         throw new FunctionEvaluationException(new MaxEvaluationsExceededException(this.maxEvaluations), point);
      } else {
         return f.value(point);
      }
   }

   protected double computeObjectiveValue(double point) throws FunctionEvaluationException {
      if (++this.evaluations > this.maxEvaluations) {
         this.resultComputed = false;
         throw new FunctionEvaluationException(new MaxEvaluationsExceededException(this.maxEvaluations), point);
      } else {
         return this.function.value(point);
      }
   }

   @Override
   public double optimize(UnivariateRealFunction f, GoalType goal, double min, double max, double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
      this.searchMin = min;
      this.searchMax = max;
      this.searchStart = startValue;
      this.optimizationGoal = goal;
      this.function = f;
      this.functionValue = Double.NaN;
      this.evaluations = 0;
      this.resetIterationsCounter();
      this.result = this.doOptimize();
      this.resultComputed = true;
      return this.result;
   }

   protected void setFunctionValue(double functionValue) {
      this.functionValue = functionValue;
   }

   @Override
   public double optimize(UnivariateRealFunction f, GoalType goal, double min, double max) throws MaxIterationsExceededException, FunctionEvaluationException {
      return this.optimize(f, goal, min, max, min + 0.5 * (max - min));
   }

   protected double doOptimize() throws MaxIterationsExceededException, FunctionEvaluationException {
      throw new MathUnsupportedOperationException(LocalizedFormats.NOT_OVERRIDEN);
   }
}
