package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ConvergingAlgorithmImpl;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;

@Deprecated
public abstract class UnivariateRealSolverImpl extends ConvergingAlgorithmImpl implements UnivariateRealSolver {
   protected double functionValueAccuracy;
   protected double defaultFunctionValueAccuracy;
   protected boolean resultComputed = false;
   protected double result;
   protected double functionValue;
   @Deprecated
   protected UnivariateRealFunction f;

   @Deprecated
   protected UnivariateRealSolverImpl(UnivariateRealFunction f, int defaultMaximalIterationCount, double defaultAbsoluteAccuracy) {
      super(defaultMaximalIterationCount, defaultAbsoluteAccuracy);
      if (f == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION);
      } else {
         this.f = f;
         this.defaultFunctionValueAccuracy = 1.0E-15;
         this.functionValueAccuracy = this.defaultFunctionValueAccuracy;
      }
   }

   protected UnivariateRealSolverImpl(int defaultMaximalIterationCount, double defaultAbsoluteAccuracy) {
      super(defaultMaximalIterationCount, defaultAbsoluteAccuracy);
      this.defaultFunctionValueAccuracy = 1.0E-15;
      this.functionValueAccuracy = this.defaultFunctionValueAccuracy;
   }

   protected void checkResultComputed() throws IllegalStateException {
      if (!this.resultComputed) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.NO_RESULT_AVAILABLE);
      }
   }

   @Override
   public double getResult() {
      this.checkResultComputed();
      return this.result;
   }

   @Override
   public double getFunctionValue() {
      this.checkResultComputed();
      return this.functionValue;
   }

   @Override
   public void setFunctionValueAccuracy(double accuracy) {
      this.functionValueAccuracy = accuracy;
   }

   @Override
   public double getFunctionValueAccuracy() {
      return this.functionValueAccuracy;
   }

   @Override
   public void resetFunctionValueAccuracy() {
      this.functionValueAccuracy = this.defaultFunctionValueAccuracy;
   }

   public double solve(int maxEval, UnivariateRealFunction function, double min, double max) throws ConvergenceException, FunctionEvaluationException {
      throw MathRuntimeException.createUnsupportedOperationException(LocalizedFormats.NOT_OVERRIDEN);
   }

   public double solve(int maxEval, UnivariateRealFunction function, double min, double max, double startValue) throws ConvergenceException, FunctionEvaluationException, IllegalArgumentException {
      throw MathRuntimeException.createUnsupportedOperationException(LocalizedFormats.NOT_OVERRIDEN);
   }

   protected final void setResult(double newResult, int iterationCount) {
      this.result = newResult;
      this.iterationCount = iterationCount;
      this.resultComputed = true;
   }

   protected final void setResult(double x, double fx, int iterationCount) {
      this.result = x;
      this.functionValue = fx;
      this.iterationCount = iterationCount;
      this.resultComputed = true;
   }

   protected final void clearResult() {
      this.iterationCount = 0;
      this.resultComputed = false;
   }

   protected boolean isBracketing(double lower, double upper, UnivariateRealFunction function) throws FunctionEvaluationException {
      double f1 = function.value(lower);
      double f2 = function.value(upper);
      return f1 > 0.0 && f2 < 0.0 || f1 < 0.0 && f2 > 0.0;
   }

   protected boolean isSequence(double start, double mid, double end) {
      return start < mid && mid < end;
   }

   protected void verifyInterval(double lower, double upper) {
      if (lower >= upper) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL, lower, upper);
      }
   }

   protected void verifySequence(double lower, double initial, double upper) {
      if (!this.isSequence(lower, initial, upper)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_INTERVAL_INITIAL_VALUE_PARAMETERS, lower, initial, upper);
      }
   }

   protected void verifyBracketing(double lower, double upper, UnivariateRealFunction function) throws FunctionEvaluationException {
      this.verifyInterval(lower, upper);
      if (!this.isBracketing(lower, upper, function)) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.SAME_SIGN_AT_ENDPOINTS, lower, upper, function.value(lower), function.value(upper)
         );
      }
   }
}
