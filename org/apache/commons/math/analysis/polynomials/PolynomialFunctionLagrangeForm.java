package org.apache.commons.math.analysis.polynomials;

import org.apache.commons.math.DuplicateSampleAbscissaException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class PolynomialFunctionLagrangeForm implements UnivariateRealFunction {
   private double[] coefficients;
   private final double[] x;
   private final double[] y;
   private boolean coefficientsComputed;

   public PolynomialFunctionLagrangeForm(double[] x, double[] y) throws IllegalArgumentException {
      verifyInterpolationArray(x, y);
      this.x = new double[x.length];
      this.y = new double[y.length];
      System.arraycopy(x, 0, this.x, 0, x.length);
      System.arraycopy(y, 0, this.y, 0, y.length);
      this.coefficientsComputed = false;
   }

   @Override
   public double value(double z) throws FunctionEvaluationException {
      try {
         return evaluate(this.x, this.y, z);
      } catch (DuplicateSampleAbscissaException var4) {
         throw new FunctionEvaluationException(z, var4.getSpecificPattern(), var4.getGeneralPattern(), var4.getArguments());
      }
   }

   public int degree() {
      return this.x.length - 1;
   }

   public double[] getInterpolatingPoints() {
      double[] out = new double[this.x.length];
      System.arraycopy(this.x, 0, out, 0, this.x.length);
      return out;
   }

   public double[] getInterpolatingValues() {
      double[] out = new double[this.y.length];
      System.arraycopy(this.y, 0, out, 0, this.y.length);
      return out;
   }

   public double[] getCoefficients() {
      if (!this.coefficientsComputed) {
         this.computeCoefficients();
      }

      double[] out = new double[this.coefficients.length];
      System.arraycopy(this.coefficients, 0, out, 0, this.coefficients.length);
      return out;
   }

   public static double evaluate(double[] x, double[] y, double z) throws DuplicateSampleAbscissaException, IllegalArgumentException {
      verifyInterpolationArray(x, y);
      int nearest = 0;
      int n = x.length;
      double[] c = new double[n];
      double[] d = new double[n];
      double min_dist = Double.POSITIVE_INFINITY;

      for(int i = 0; i < n; ++i) {
         c[i] = y[i];
         d[i] = y[i];
         double dist = FastMath.abs(z - x[i]);
         if (dist < min_dist) {
            nearest = i;
            min_dist = dist;
         }
      }

      double value = y[nearest];

      for(int i = 1; i < n; ++i) {
         for(int j = 0; j < n - i; ++j) {
            double tc = x[j] - z;
            double td = x[i + j] - z;
            double divider = x[j] - x[i + j];
            if (divider == 0.0) {
               throw new DuplicateSampleAbscissaException(x[i], i, i + j);
            }

            double w = (c[j + 1] - d[j]) / divider;
            c[j] = tc * w;
            d[j] = td * w;
         }

         if ((double)nearest < 0.5 * (double)(n - i + 1)) {
            value += c[nearest];
         } else {
            value += d[--nearest];
         }
      }

      return value;
   }

   protected void computeCoefficients() throws ArithmeticException {
      int n = this.degree() + 1;
      this.coefficients = new double[n];

      for(int i = 0; i < n; ++i) {
         this.coefficients[i] = 0.0;
      }

      double[] c = new double[n + 1];
      c[0] = 1.0;

      for(int i = 0; i < n; ++i) {
         for(int j = i; j > 0; --j) {
            c[j] = c[j - 1] - c[j] * this.x[i];
         }

         c[0] *= -this.x[i];
         c[i + 1] = 1.0;
      }

      double[] tc = new double[n];

      for(int i = 0; i < n; ++i) {
         double d = 1.0;

         for(int j = 0; j < n; ++j) {
            if (i != j) {
               d *= this.x[i] - this.x[j];
            }
         }

         if (d == 0.0) {
            for(int k = 0; k < n; ++k) {
               if (i != k && this.x[i] == this.x[k]) {
                  throw MathRuntimeException.createArithmeticException(LocalizedFormats.IDENTICAL_ABSCISSAS_DIVISION_BY_ZERO, i, k, this.x[i]);
               }
            }
         }

         double t = this.y[i] / d;
         tc[n - 1] = c[n];
         this.coefficients[n - 1] += t * tc[n - 1];

         for(int j = n - 2; j >= 0; --j) {
            tc[j] = c[j + 1] + tc[j + 1] * this.x[i];
            this.coefficients[j] += t * tc[j];
         }
      }

      this.coefficientsComputed = true;
   }

   public static void verifyInterpolationArray(double[] x, double[] y) throws IllegalArgumentException {
      if (x.length != y.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, x.length, y.length);
      } else if (x.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.WRONG_NUMBER_OF_POINTS, 2, x.length);
      }
   }
}
