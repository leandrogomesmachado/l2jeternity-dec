package org.apache.commons.math.analysis.polynomials;

import java.util.Arrays;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class PolynomialSplineFunction implements DifferentiableUnivariateRealFunction {
   private final double[] knots;
   private final PolynomialFunction[] polynomials;
   private final int n;

   public PolynomialSplineFunction(double[] knots, PolynomialFunction[] polynomials) {
      if (knots.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_ENOUGH_POINTS_IN_SPLINE_PARTITION, 2, knots.length);
      } else if (knots.length - 1 != polynomials.length) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.POLYNOMIAL_INTERPOLANTS_MISMATCH_SEGMENTS, polynomials.length, knots.length
         );
      } else if (!isStrictlyIncreasing(knots)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_STRICTLY_INCREASING_KNOT_VALUES);
      } else {
         this.n = knots.length - 1;
         this.knots = new double[this.n + 1];
         System.arraycopy(knots, 0, this.knots, 0, this.n + 1);
         this.polynomials = new PolynomialFunction[this.n];
         System.arraycopy(polynomials, 0, this.polynomials, 0, this.n);
      }
   }

   @Override
   public double value(double v) throws ArgumentOutsideDomainException {
      if (!(v < this.knots[0]) && !(v > this.knots[this.n])) {
         int i = Arrays.binarySearch(this.knots, v);
         if (i < 0) {
            i = -i - 2;
         }

         if (i >= this.polynomials.length) {
            --i;
         }

         return this.polynomials[i].value(v - this.knots[i]);
      } else {
         throw new ArgumentOutsideDomainException(v, this.knots[0], this.knots[this.n]);
      }
   }

   @Override
   public UnivariateRealFunction derivative() {
      return this.polynomialSplineDerivative();
   }

   public PolynomialSplineFunction polynomialSplineDerivative() {
      PolynomialFunction[] derivativePolynomials = new PolynomialFunction[this.n];

      for(int i = 0; i < this.n; ++i) {
         derivativePolynomials[i] = this.polynomials[i].polynomialDerivative();
      }

      return new PolynomialSplineFunction(this.knots, derivativePolynomials);
   }

   public int getN() {
      return this.n;
   }

   public PolynomialFunction[] getPolynomials() {
      PolynomialFunction[] p = new PolynomialFunction[this.n];
      System.arraycopy(this.polynomials, 0, p, 0, this.n);
      return p;
   }

   public double[] getKnots() {
      double[] out = new double[this.n + 1];
      System.arraycopy(this.knots, 0, out, 0, this.n + 1);
      return out;
   }

   private static boolean isStrictlyIncreasing(double[] x) {
      for(int i = 1; i < x.length; ++i) {
         if (x[i - 1] >= x[i]) {
            return false;
         }
      }

      return true;
   }
}
