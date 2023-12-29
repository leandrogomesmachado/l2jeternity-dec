package org.apache.commons.math.analysis.polynomials;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NoDataException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class PolynomialFunction implements DifferentiableUnivariateRealFunction, Serializable {
   private static final long serialVersionUID = -7726511984200295583L;
   private final double[] coefficients;

   public PolynomialFunction(double[] c) {
      int n = c.length;
      if (n == 0) {
         throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
      } else {
         while(n > 1 && c[n - 1] == 0.0) {
            --n;
         }

         this.coefficients = new double[n];
         System.arraycopy(c, 0, this.coefficients, 0, n);
      }
   }

   @Override
   public double value(double x) {
      return evaluate(this.coefficients, x);
   }

   public int degree() {
      return this.coefficients.length - 1;
   }

   public double[] getCoefficients() {
      return (double[])this.coefficients.clone();
   }

   protected static double evaluate(double[] coefficients, double argument) {
      int n = coefficients.length;
      if (n == 0) {
         throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
      } else {
         double result = coefficients[n - 1];

         for(int j = n - 2; j >= 0; --j) {
            result = argument * result + coefficients[j];
         }

         return result;
      }
   }

   public PolynomialFunction add(PolynomialFunction p) {
      int lowLength = FastMath.min(this.coefficients.length, p.coefficients.length);
      int highLength = FastMath.max(this.coefficients.length, p.coefficients.length);
      double[] newCoefficients = new double[highLength];

      for(int i = 0; i < lowLength; ++i) {
         newCoefficients[i] = this.coefficients[i] + p.coefficients[i];
      }

      System.arraycopy(
         this.coefficients.length < p.coefficients.length ? p.coefficients : this.coefficients, lowLength, newCoefficients, lowLength, highLength - lowLength
      );
      return new PolynomialFunction(newCoefficients);
   }

   public PolynomialFunction subtract(PolynomialFunction p) {
      int lowLength = FastMath.min(this.coefficients.length, p.coefficients.length);
      int highLength = FastMath.max(this.coefficients.length, p.coefficients.length);
      double[] newCoefficients = new double[highLength];

      for(int i = 0; i < lowLength; ++i) {
         newCoefficients[i] = this.coefficients[i] - p.coefficients[i];
      }

      if (this.coefficients.length < p.coefficients.length) {
         for(int i = lowLength; i < highLength; ++i) {
            newCoefficients[i] = -p.coefficients[i];
         }
      } else {
         System.arraycopy(this.coefficients, lowLength, newCoefficients, lowLength, highLength - lowLength);
      }

      return new PolynomialFunction(newCoefficients);
   }

   public PolynomialFunction negate() {
      double[] newCoefficients = new double[this.coefficients.length];

      for(int i = 0; i < this.coefficients.length; ++i) {
         newCoefficients[i] = -this.coefficients[i];
      }

      return new PolynomialFunction(newCoefficients);
   }

   public PolynomialFunction multiply(PolynomialFunction p) {
      double[] newCoefficients = new double[this.coefficients.length + p.coefficients.length - 1];

      for(int i = 0; i < newCoefficients.length; ++i) {
         newCoefficients[i] = 0.0;

         for(int j = FastMath.max(0, i + 1 - p.coefficients.length); j < FastMath.min(this.coefficients.length, i + 1); ++j) {
            newCoefficients[i] += this.coefficients[j] * p.coefficients[i - j];
         }
      }

      return new PolynomialFunction(newCoefficients);
   }

   protected static double[] differentiate(double[] coefficients) {
      int n = coefficients.length;
      if (n == 0) {
         throw new NoDataException(LocalizedFormats.EMPTY_POLYNOMIALS_COEFFICIENTS_ARRAY);
      } else if (n == 1) {
         return new double[]{0.0};
      } else {
         double[] result = new double[n - 1];

         for(int i = n - 1; i > 0; --i) {
            result[i - 1] = (double)i * coefficients[i];
         }

         return result;
      }
   }

   public PolynomialFunction polynomialDerivative() {
      return new PolynomialFunction(differentiate(this.coefficients));
   }

   @Override
   public UnivariateRealFunction derivative() {
      return this.polynomialDerivative();
   }

   @Override
   public String toString() {
      StringBuilder s = new StringBuilder();
      if (this.coefficients[0] == 0.0) {
         if (this.coefficients.length == 1) {
            return "0";
         }
      } else {
         s.append(Double.toString(this.coefficients[0]));
      }

      for(int i = 1; i < this.coefficients.length; ++i) {
         if (this.coefficients[i] != 0.0) {
            if (s.length() > 0) {
               if (this.coefficients[i] < 0.0) {
                  s.append(" - ");
               } else {
                  s.append(" + ");
               }
            } else if (this.coefficients[i] < 0.0) {
               s.append("-");
            }

            double absAi = FastMath.abs(this.coefficients[i]);
            if (absAi - 1.0 != 0.0) {
               s.append(Double.toString(absAi));
               s.append(' ');
            }

            s.append("x");
            if (i > 1) {
               s.append('^');
               s.append(Integer.toString(i));
            }
         }
      }

      return s.toString();
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + Arrays.hashCode(this.coefficients);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof PolynomialFunction)) {
         return false;
      } else {
         PolynomialFunction other = (PolynomialFunction)obj;
         return Arrays.equals(this.coefficients, other.coefficients);
      }
   }
}
