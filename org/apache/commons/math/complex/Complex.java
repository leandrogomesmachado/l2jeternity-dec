package org.apache.commons.math.complex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class Complex implements FieldElement<Complex>, Serializable {
   public static final Complex I = new Complex(0.0, 1.0);
   public static final Complex NaN = new Complex(Double.NaN, Double.NaN);
   public static final Complex INF = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   public static final Complex ONE = new Complex(1.0, 0.0);
   public static final Complex ZERO = new Complex(0.0, 0.0);
   private static final long serialVersionUID = -6195664516687396620L;
   private final double imaginary;
   private final double real;
   private final transient boolean isNaN;
   private final transient boolean isInfinite;

   public Complex(double real, double imaginary) {
      this.real = real;
      this.imaginary = imaginary;
      this.isNaN = Double.isNaN(real) || Double.isNaN(imaginary);
      this.isInfinite = !this.isNaN && (Double.isInfinite(real) || Double.isInfinite(imaginary));
   }

   public double abs() {
      if (this.isNaN()) {
         return Double.NaN;
      } else if (this.isInfinite()) {
         return Double.POSITIVE_INFINITY;
      } else if (FastMath.abs(this.real) < FastMath.abs(this.imaginary)) {
         if (this.imaginary == 0.0) {
            return FastMath.abs(this.real);
         } else {
            double q = this.real / this.imaginary;
            return FastMath.abs(this.imaginary) * FastMath.sqrt(1.0 + q * q);
         }
      } else if (this.real == 0.0) {
         return FastMath.abs(this.imaginary);
      } else {
         double q = this.imaginary / this.real;
         return FastMath.abs(this.real) * FastMath.sqrt(1.0 + q * q);
      }
   }

   public Complex add(Complex rhs) {
      return this.createComplex(this.real + rhs.getReal(), this.imaginary + rhs.getImaginary());
   }

   public Complex conjugate() {
      return this.isNaN() ? NaN : this.createComplex(this.real, -this.imaginary);
   }

   public Complex divide(Complex rhs) {
      if (!this.isNaN() && !rhs.isNaN()) {
         double c = rhs.getReal();
         double d = rhs.getImaginary();
         if (c == 0.0 && d == 0.0) {
            return NaN;
         } else if (rhs.isInfinite() && !this.isInfinite()) {
            return ZERO;
         } else if (FastMath.abs(c) < FastMath.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;
            return this.createComplex((this.real * q + this.imaginary) / denominator, (this.imaginary * q - this.real) / denominator);
         } else {
            double q = d / c;
            double denominator = d * q + c;
            return this.createComplex((this.imaginary * q + this.real) / denominator, (this.imaginary - this.real * q) / denominator);
         }
      } else {
         return NaN;
      }
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other instanceof Complex) {
         Complex rhs = (Complex)other;
         if (rhs.isNaN()) {
            return this.isNaN();
         } else {
            return this.real == rhs.real && this.imaginary == rhs.imaginary;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.isNaN() ? 7 : 37 * (17 * MathUtils.hash(this.imaginary) + MathUtils.hash(this.real));
   }

   public double getImaginary() {
      return this.imaginary;
   }

   public double getReal() {
      return this.real;
   }

   public boolean isNaN() {
      return this.isNaN;
   }

   public boolean isInfinite() {
      return this.isInfinite;
   }

   public Complex multiply(Complex rhs) {
      if (this.isNaN() || rhs.isNaN()) {
         return NaN;
      } else {
         return !Double.isInfinite(this.real) && !Double.isInfinite(this.imaginary) && !Double.isInfinite(rhs.real) && !Double.isInfinite(rhs.imaginary)
            ? this.createComplex(this.real * rhs.real - this.imaginary * rhs.imaginary, this.real * rhs.imaginary + this.imaginary * rhs.real)
            : INF;
      }
   }

   public Complex multiply(double rhs) {
      if (this.isNaN() || Double.isNaN(rhs)) {
         return NaN;
      } else {
         return !Double.isInfinite(this.real) && !Double.isInfinite(this.imaginary) && !Double.isInfinite(rhs)
            ? this.createComplex(this.real * rhs, this.imaginary * rhs)
            : INF;
      }
   }

   public Complex negate() {
      return this.isNaN() ? NaN : this.createComplex(-this.real, -this.imaginary);
   }

   public Complex subtract(Complex rhs) {
      return !this.isNaN() && !rhs.isNaN() ? this.createComplex(this.real - rhs.getReal(), this.imaginary - rhs.getImaginary()) : NaN;
   }

   public Complex acos() {
      return this.isNaN() ? NaN : this.add(this.sqrt1z().multiply(I)).log().multiply(I.negate());
   }

   public Complex asin() {
      return this.isNaN() ? NaN : this.sqrt1z().add(this.multiply(I)).log().multiply(I.negate());
   }

   public Complex atan() {
      return this.isNaN() ? NaN : this.add(I).divide(I.subtract(this)).log().multiply(I.divide(this.createComplex(2.0, 0.0)));
   }

   public Complex cos() {
      return this.isNaN()
         ? NaN
         : this.createComplex(FastMath.cos(this.real) * MathUtils.cosh(this.imaginary), -FastMath.sin(this.real) * MathUtils.sinh(this.imaginary));
   }

   public Complex cosh() {
      return this.isNaN()
         ? NaN
         : this.createComplex(MathUtils.cosh(this.real) * FastMath.cos(this.imaginary), MathUtils.sinh(this.real) * FastMath.sin(this.imaginary));
   }

   public Complex exp() {
      if (this.isNaN()) {
         return NaN;
      } else {
         double expReal = FastMath.exp(this.real);
         return this.createComplex(expReal * FastMath.cos(this.imaginary), expReal * FastMath.sin(this.imaginary));
      }
   }

   public Complex log() {
      return this.isNaN() ? NaN : this.createComplex(FastMath.log(this.abs()), FastMath.atan2(this.imaginary, this.real));
   }

   public Complex pow(Complex x) {
      if (x == null) {
         throw new NullPointerException();
      } else {
         return this.log().multiply(x).exp();
      }
   }

   public Complex sin() {
      return this.isNaN()
         ? NaN
         : this.createComplex(FastMath.sin(this.real) * MathUtils.cosh(this.imaginary), FastMath.cos(this.real) * MathUtils.sinh(this.imaginary));
   }

   public Complex sinh() {
      return this.isNaN()
         ? NaN
         : this.createComplex(MathUtils.sinh(this.real) * FastMath.cos(this.imaginary), MathUtils.cosh(this.real) * FastMath.sin(this.imaginary));
   }

   public Complex sqrt() {
      if (this.isNaN()) {
         return NaN;
      } else if (this.real == 0.0 && this.imaginary == 0.0) {
         return this.createComplex(0.0, 0.0);
      } else {
         double t = FastMath.sqrt((FastMath.abs(this.real) + this.abs()) / 2.0);
         return this.real >= 0.0
            ? this.createComplex(t, this.imaginary / (2.0 * t))
            : this.createComplex(FastMath.abs(this.imaginary) / (2.0 * t), MathUtils.indicator(this.imaginary) * t);
      }
   }

   public Complex sqrt1z() {
      return this.createComplex(1.0, 0.0).subtract(this.multiply(this)).sqrt();
   }

   public Complex tan() {
      if (this.isNaN()) {
         return NaN;
      } else {
         double real2 = 2.0 * this.real;
         double imaginary2 = 2.0 * this.imaginary;
         double d = FastMath.cos(real2) + MathUtils.cosh(imaginary2);
         return this.createComplex(FastMath.sin(real2) / d, MathUtils.sinh(imaginary2) / d);
      }
   }

   public Complex tanh() {
      if (this.isNaN()) {
         return NaN;
      } else {
         double real2 = 2.0 * this.real;
         double imaginary2 = 2.0 * this.imaginary;
         double d = MathUtils.cosh(real2) + FastMath.cos(imaginary2);
         return this.createComplex(MathUtils.sinh(real2) / d, FastMath.sin(imaginary2) / d);
      }
   }

   public double getArgument() {
      return FastMath.atan2(this.getImaginary(), this.getReal());
   }

   public List<Complex> nthRoot(int n) throws IllegalArgumentException {
      if (n <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_COMPUTE_NTH_ROOT_FOR_NEGATIVE_N, n);
      } else {
         List<Complex> result = new ArrayList<>();
         if (this.isNaN()) {
            result.add(NaN);
            return result;
         } else if (this.isInfinite()) {
            result.add(INF);
            return result;
         } else {
            double nthRootOfAbs = FastMath.pow(this.abs(), 1.0 / (double)n);
            double nthPhi = this.getArgument() / (double)n;
            double slice = (Math.PI * 2) / (double)n;
            double innerPart = nthPhi;

            for(int k = 0; k < n; ++k) {
               double realPart = nthRootOfAbs * FastMath.cos(innerPart);
               double imaginaryPart = nthRootOfAbs * FastMath.sin(innerPart);
               result.add(this.createComplex(realPart, imaginaryPart));
               innerPart += slice;
            }

            return result;
         }
      }
   }

   protected Complex createComplex(double realPart, double imaginaryPart) {
      return new Complex(realPart, imaginaryPart);
   }

   protected final Object readResolve() {
      return this.createComplex(this.real, this.imaginary);
   }

   public ComplexField getField() {
      return ComplexField.getInstance();
   }
}
