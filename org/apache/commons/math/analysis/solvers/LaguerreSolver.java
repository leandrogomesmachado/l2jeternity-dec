package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class LaguerreSolver extends UnivariateRealSolverImpl {
   @Deprecated
   private final PolynomialFunction p;

   @Deprecated
   public LaguerreSolver(UnivariateRealFunction f) throws IllegalArgumentException {
      super(f, 100, 1.0E-6);
      if (f instanceof PolynomialFunction) {
         this.p = (PolynomialFunction)f;
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FUNCTION_NOT_POLYNOMIAL);
      }
   }

   @Deprecated
   public LaguerreSolver() {
      super(100, 1.0E-6);
      this.p = null;
   }

   @Deprecated
   public PolynomialFunction getPolynomialFunction() {
      return new PolynomialFunction(this.p.getCoefficients());
   }

   @Deprecated
   @Override
   public double solve(double min, double max) throws ConvergenceException, FunctionEvaluationException {
      return this.solve(this.p, min, max);
   }

   @Deprecated
   @Override
   public double solve(double min, double max, double initial) throws ConvergenceException, FunctionEvaluationException {
      return this.solve(this.p, min, max, initial);
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max, double initial) throws ConvergenceException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max, initial);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max, double initial) throws ConvergenceException, FunctionEvaluationException {
      if (f.value(min) == 0.0) {
         return min;
      } else if (f.value(max) == 0.0) {
         return max;
      } else if (f.value(initial) == 0.0) {
         return initial;
      } else {
         this.verifyBracketing(min, max, f);
         this.verifySequence(min, initial, max);
         return this.isBracketing(min, initial, f) ? this.solve(f, min, initial) : this.solve(f, initial, max);
      }
   }

   @Override
   public double solve(int maxEval, UnivariateRealFunction f, double min, double max) throws ConvergenceException, FunctionEvaluationException {
      this.setMaximalIterationCount(maxEval);
      return this.solve(f, min, max);
   }

   @Deprecated
   @Override
   public double solve(UnivariateRealFunction f, double min, double max) throws ConvergenceException, FunctionEvaluationException {
      if (!(f instanceof PolynomialFunction)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FUNCTION_NOT_POLYNOMIAL);
      } else if (f.value(min) == 0.0) {
         return min;
      } else if (f.value(max) == 0.0) {
         return max;
      } else {
         this.verifyBracketing(min, max, f);
         double[] coefficients = ((PolynomialFunction)f).getCoefficients();
         Complex[] c = new Complex[coefficients.length];

         for(int i = 0; i < coefficients.length; ++i) {
            c[i] = new Complex(coefficients[i], 0.0);
         }

         Complex initial = new Complex(0.5 * (min + max), 0.0);
         Complex z = this.solve(c, initial);
         if (this.isRootOK(min, max, z)) {
            this.setResult(z.getReal(), this.iterationCount);
            return this.result;
         } else {
            Complex[] root = this.solveAll(c, initial);

            for(int i = 0; i < root.length; ++i) {
               if (this.isRootOK(min, max, root[i])) {
                  this.setResult(root[i].getReal(), this.iterationCount);
                  return this.result;
               }
            }

            throw new ConvergenceException();
         }
      }
   }

   protected boolean isRootOK(double min, double max, Complex z) {
      double tolerance = FastMath.max(this.relativeAccuracy * z.abs(), this.absoluteAccuracy);
      return this.isSequence(min, z.getReal(), max) && (FastMath.abs(z.getImaginary()) <= tolerance || z.abs() <= this.functionValueAccuracy);
   }

   @Deprecated
   public Complex[] solveAll(double[] coefficients, double initial) throws ConvergenceException, FunctionEvaluationException {
      Complex[] c = new Complex[coefficients.length];
      Complex z = new Complex(initial, 0.0);

      for(int i = 0; i < c.length; ++i) {
         c[i] = new Complex(coefficients[i], 0.0);
      }

      return this.solveAll(c, z);
   }

   @Deprecated
   public Complex[] solveAll(Complex[] coefficients, Complex initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      int n = coefficients.length - 1;
      int iterationCount = 0;
      if (n < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NON_POSITIVE_POLYNOMIAL_DEGREE, n);
      } else {
         Complex[] c = new Complex[n + 1];

         for(int i = 0; i <= n; ++i) {
            c[i] = coefficients[i];
         }

         Complex[] root = new Complex[n];

         for(int i = 0; i < n; ++i) {
            Complex[] subarray = new Complex[n - i + 1];
            System.arraycopy(c, 0, subarray, 0, subarray.length);
            root[i] = this.solve(subarray, initial);
            Complex newc = c[n - i];
            Complex oldc = null;

            for(int j = n - i - 1; j >= 0; --j) {
               oldc = c[j];
               c[j] = newc;
               newc = oldc.add(newc.multiply(root[i]));
            }

            iterationCount += this.iterationCount;
         }

         this.resultComputed = true;
         this.iterationCount = iterationCount;
         return root;
      }
   }

   @Deprecated
   public Complex solve(Complex[] coefficients, Complex initial) throws MaxIterationsExceededException, FunctionEvaluationException {
      int n = coefficients.length - 1;
      if (n < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NON_POSITIVE_POLYNOMIAL_DEGREE, n);
      } else {
         Complex N = new Complex((double)n, 0.0);
         Complex N1 = new Complex((double)(n - 1), 0.0);
         int i = 1;
         Complex pv = null;
         Complex dv = null;
         Complex d2v = null;
         Complex G = null;
         Complex G2 = null;
         Complex H = null;
         Complex delta = null;
         Complex denominator = null;
         Complex z = initial;

         for(Complex oldz = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); i <= this.maximalIterationCount; ++i) {
            pv = coefficients[n];
            dv = Complex.ZERO;
            d2v = Complex.ZERO;

            for(int j = n - 1; j >= 0; --j) {
               d2v = dv.add(z.multiply(d2v));
               dv = pv.add(z.multiply(dv));
               pv = coefficients[j].add(z.multiply(pv));
            }

            d2v = d2v.multiply(new Complex(2.0, 0.0));
            double tolerance = FastMath.max(this.relativeAccuracy * z.abs(), this.absoluteAccuracy);
            if (z.subtract(oldz).abs() <= tolerance) {
               this.resultComputed = true;
               this.iterationCount = i;
               return z;
            }

            if (pv.abs() <= this.functionValueAccuracy) {
               this.resultComputed = true;
               this.iterationCount = i;
               return z;
            }

            G = dv.divide(pv);
            G2 = G.multiply(G);
            H = G2.subtract(d2v.divide(pv));
            delta = N1.multiply(N.multiply(H).subtract(G2));
            Complex deltaSqrt = delta.sqrt();
            Complex dplus = G.add(deltaSqrt);
            Complex dminus = G.subtract(deltaSqrt);
            denominator = dplus.abs() > dminus.abs() ? dplus : dminus;
            if (denominator.equals(new Complex(0.0, 0.0))) {
               z = z.add(new Complex(this.absoluteAccuracy, this.absoluteAccuracy));
               oldz = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            } else {
               oldz = z;
               z = z.subtract(N.divide(denominator));
            }
         }

         throw new MaxIterationsExceededException(this.maximalIterationCount);
      }
   }
}
