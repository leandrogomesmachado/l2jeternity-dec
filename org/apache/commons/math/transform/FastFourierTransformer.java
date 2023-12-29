package org.apache.commons.math.transform;

import java.io.Serializable;
import java.lang.reflect.Array;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class FastFourierTransformer implements Serializable {
   static final long serialVersionUID = 5138259215438106000L;
   private FastFourierTransformer.RootsOfUnity roots = new FastFourierTransformer.RootsOfUnity();

   public Complex[] transform(double[] f) throws IllegalArgumentException {
      return this.fft(f, false);
   }

   public Complex[] transform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = sample(f, min, max, n);
      return this.fft(data, false);
   }

   public Complex[] transform(Complex[] f) throws IllegalArgumentException {
      this.roots.computeOmega(f.length);
      return this.fft(f);
   }

   public Complex[] transform2(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)f.length);
      return scaleArray(this.fft(f, false), scaling_coefficient);
   }

   public Complex[] transform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = sample(f, min, max, n);
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)n);
      return scaleArray(this.fft(data, false), scaling_coefficient);
   }

   public Complex[] transform2(Complex[] f) throws IllegalArgumentException {
      this.roots.computeOmega(f.length);
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)f.length);
      return scaleArray(this.fft(f), scaling_coefficient);
   }

   public Complex[] inversetransform(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = 1.0 / (double)f.length;
      return scaleArray(this.fft(f, true), scaling_coefficient);
   }

   public Complex[] inversetransform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = sample(f, min, max, n);
      double scaling_coefficient = 1.0 / (double)n;
      return scaleArray(this.fft(data, true), scaling_coefficient);
   }

   public Complex[] inversetransform(Complex[] f) throws IllegalArgumentException {
      this.roots.computeOmega(-f.length);
      double scaling_coefficient = 1.0 / (double)f.length;
      return scaleArray(this.fft(f), scaling_coefficient);
   }

   public Complex[] inversetransform2(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)f.length);
      return scaleArray(this.fft(f, true), scaling_coefficient);
   }

   public Complex[] inversetransform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = sample(f, min, max, n);
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)n);
      return scaleArray(this.fft(data, true), scaling_coefficient);
   }

   public Complex[] inversetransform2(Complex[] f) throws IllegalArgumentException {
      this.roots.computeOmega(-f.length);
      double scaling_coefficient = 1.0 / FastMath.sqrt((double)f.length);
      return scaleArray(this.fft(f), scaling_coefficient);
   }

   protected Complex[] fft(double[] f, boolean isInverse) throws IllegalArgumentException {
      verifyDataSet(f);
      Complex[] F = new Complex[f.length];
      if (f.length == 1) {
         F[0] = new Complex(f[0], 0.0);
         return F;
      } else {
         int N = f.length >> 1;
         Complex[] c = new Complex[N];

         for(int i = 0; i < N; ++i) {
            c[i] = new Complex(f[2 * i], f[2 * i + 1]);
         }

         this.roots.computeOmega(isInverse ? -N : N);
         Complex[] z = this.fft(c);
         this.roots.computeOmega(isInverse ? -2 * N : 2 * N);
         F[0] = new Complex(2.0 * (z[0].getReal() + z[0].getImaginary()), 0.0);
         F[N] = new Complex(2.0 * (z[0].getReal() - z[0].getImaginary()), 0.0);

         for(int i = 1; i < N; ++i) {
            Complex A = z[N - i].conjugate();
            Complex B = z[i].add(A);
            Complex C = z[i].subtract(A);
            Complex D = new Complex(-this.roots.getOmegaImaginary(i), this.roots.getOmegaReal(i));
            F[i] = B.subtract(C.multiply(D));
            F[2 * N - i] = F[i].conjugate();
         }

         return scaleArray(F, 0.5);
      }
   }

   protected Complex[] fft(Complex[] data) throws IllegalArgumentException {
      int n = data.length;
      Complex[] f = new Complex[n];
      verifyDataSet((Object[])data);
      if (n == 1) {
         f[0] = data[0];
         return f;
      } else if (n == 2) {
         f[0] = data[0].add(data[1]);
         f[1] = data[0].subtract(data[1]);
         return f;
      } else {
         int ii = 0;

         for(int i = 0; i < n; ++i) {
            f[i] = data[ii];

            int k;
            for(k = n >> 1; ii >= k && k > 0; k >>= 1) {
               ii -= k;
            }

            ii += k;
         }

         for(int i = 0; i < n; i += 4) {
            Complex a = f[i].add(f[i + 1]);
            Complex b = f[i + 2].add(f[i + 3]);
            Complex c = f[i].subtract(f[i + 1]);
            Complex d = f[i + 2].subtract(f[i + 3]);
            Complex e1 = c.add(d.multiply(Complex.I));
            Complex e2 = c.subtract(d.multiply(Complex.I));
            f[i] = a.add(b);
            f[i + 2] = a.subtract(b);
            f[i + 1] = this.roots.isForward() ? e2 : e1;
            f[i + 3] = this.roots.isForward() ? e1 : e2;
         }

         for(int i = 4; i < n; i <<= 1) {
            int m = n / (i << 1);

            for(int j = 0; j < n; j += i << 1) {
               for(int k = 0; k < i; ++k) {
                  int k_times_m = k * m;
                  double omega_k_times_m_real = this.roots.getOmegaReal(k_times_m);
                  double omega_k_times_m_imaginary = this.roots.getOmegaImaginary(k_times_m);
                  Complex z = new Complex(
                     f[i + j + k].getReal() * omega_k_times_m_real - f[i + j + k].getImaginary() * omega_k_times_m_imaginary,
                     f[i + j + k].getReal() * omega_k_times_m_imaginary + f[i + j + k].getImaginary() * omega_k_times_m_real
                  );
                  f[i + j + k] = f[j + k].subtract(z);
                  f[j + k] = f[j + k].add(z);
               }
            }
         }

         return f;
      }
   }

   public static double[] sample(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      if (n <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_NUMBER_OF_SAMPLES, n);
      } else {
         verifyInterval(min, max);
         double[] s = new double[n];
         double h = (max - min) / (double)n;

         for(int i = 0; i < n; ++i) {
            s[i] = f.value(min + (double)i * h);
         }

         return s;
      }
   }

   public static double[] scaleArray(double[] f, double d) {
      for(int i = 0; i < f.length; ++i) {
         f[i] *= d;
      }

      return f;
   }

   public static Complex[] scaleArray(Complex[] f, double d) {
      for(int i = 0; i < f.length; ++i) {
         f[i] = new Complex(d * f[i].getReal(), d * f[i].getImaginary());
      }

      return f;
   }

   public static boolean isPowerOf2(long n) {
      return n > 0L && (n & n - 1L) == 0L;
   }

   public static void verifyDataSet(double[] d) throws IllegalArgumentException {
      if (!isPowerOf2((long)d.length)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_CONSIDER_PADDING, d.length);
      }
   }

   public static void verifyDataSet(Object[] o) throws IllegalArgumentException {
      if (!isPowerOf2((long)o.length)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_CONSIDER_PADDING, o.length);
      }
   }

   public static void verifyInterval(double lower, double upper) throws IllegalArgumentException {
      if (lower >= upper) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL, lower, upper);
      }
   }

   public Object mdfft(Object mdca, boolean forward) throws IllegalArgumentException {
      FastFourierTransformer.MultiDimensionalComplexMatrix mdcm = (FastFourierTransformer.MultiDimensionalComplexMatrix)new FastFourierTransformer.MultiDimensionalComplexMatrix(
            mdca
         )
         .clone();
      int[] dimensionSize = mdcm.getDimensionSizes();

      for(int i = 0; i < dimensionSize.length; ++i) {
         this.mdfft(mdcm, forward, i, new int[0]);
      }

      return mdcm.getArray();
   }

   private void mdfft(FastFourierTransformer.MultiDimensionalComplexMatrix mdcm, boolean forward, int d, int[] subVector) throws IllegalArgumentException {
      int[] dimensionSize = mdcm.getDimensionSizes();
      if (subVector.length == dimensionSize.length) {
         Complex[] temp = new Complex[dimensionSize[d]];

         for(int i = 0; i < dimensionSize[d]; ++i) {
            subVector[d] = i;
            temp[i] = mdcm.get(subVector);
         }

         if (forward) {
            temp = this.transform2(temp);
         } else {
            temp = this.inversetransform2(temp);
         }

         for(int i = 0; i < dimensionSize[d]; ++i) {
            subVector[d] = i;
            mdcm.set(temp[i], subVector);
         }
      } else {
         int[] vector = new int[subVector.length + 1];
         System.arraycopy(subVector, 0, vector, 0, subVector.length);
         if (subVector.length == d) {
            vector[d] = 0;
            this.mdfft(mdcm, forward, d, vector);
         } else {
            for(int i = 0; i < dimensionSize[subVector.length]; ++i) {
               vector[subVector.length] = i;
               this.mdfft(mdcm, forward, d, vector);
            }
         }
      }
   }

   private static class MultiDimensionalComplexMatrix implements Cloneable {
      protected int[] dimensionSize;
      protected Object multiDimensionalComplexArray;

      public MultiDimensionalComplexMatrix(Object multiDimensionalComplexArray) {
         this.multiDimensionalComplexArray = multiDimensionalComplexArray;
         int numOfDimensions = 0;

         Object[] array;
         for(Object lastDimension = multiDimensionalComplexArray; lastDimension instanceof Object[]; lastDimension = array[0]) {
            array = (Object[])lastDimension;
            ++numOfDimensions;
         }

         this.dimensionSize = new int[numOfDimensions];
         numOfDimensions = 0;

         for(Object lastDimension = multiDimensionalComplexArray; lastDimension instanceof Object[]; lastDimension = array[0]) {
            array = (Object[])lastDimension;
            this.dimensionSize[numOfDimensions++] = array.length;
         }
      }

      public Complex get(int... vector) throws IllegalArgumentException {
         if (vector == null) {
            if (this.dimensionSize.length > 0) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, 0, this.dimensionSize.length);
            } else {
               return null;
            }
         } else if (vector.length != this.dimensionSize.length) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, vector.length, this.dimensionSize.length);
         } else {
            Object lastDimension = this.multiDimensionalComplexArray;

            for(int i = 0; i < this.dimensionSize.length; ++i) {
               lastDimension = lastDimension[vector[i]];
            }

            return (Complex)lastDimension;
         }
      }

      public Complex set(Complex magnitude, int... vector) throws IllegalArgumentException {
         if (vector == null) {
            if (this.dimensionSize.length > 0) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, 0, this.dimensionSize.length);
            } else {
               return null;
            }
         } else if (vector.length != this.dimensionSize.length) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, vector.length, this.dimensionSize.length);
         } else {
            Object[] lastDimension = (Object[])this.multiDimensionalComplexArray;

            for(int i = 0; i < this.dimensionSize.length - 1; ++i) {
               lastDimension = (Object[])lastDimension[vector[i]];
            }

            Complex lastValue = (Complex)lastDimension[vector[this.dimensionSize.length - 1]];
            lastDimension[vector[this.dimensionSize.length - 1]] = magnitude;
            return lastValue;
         }
      }

      public int[] getDimensionSizes() {
         return (int[])this.dimensionSize.clone();
      }

      public Object getArray() {
         return this.multiDimensionalComplexArray;
      }

      @Override
      public Object clone() {
         FastFourierTransformer.MultiDimensionalComplexMatrix mdcm = new FastFourierTransformer.MultiDimensionalComplexMatrix(
            Array.newInstance(Complex.class, this.dimensionSize)
         );
         this.clone(mdcm);
         return mdcm;
      }

      private void clone(FastFourierTransformer.MultiDimensionalComplexMatrix mdcm) {
         int[] vector = new int[this.dimensionSize.length];
         int size = 1;

         for(int i = 0; i < this.dimensionSize.length; ++i) {
            size *= this.dimensionSize[i];
         }

         int[][] vectorList = new int[size][this.dimensionSize.length];

         for(int[] nextVector : vectorList) {
            System.arraycopy(vector, 0, nextVector, 0, this.dimensionSize.length);

            for(int i = 0; i < this.dimensionSize.length; ++i) {
               vector[i]++;
               if (vector[i] < this.dimensionSize[i]) {
                  break;
               }

               vector[i] = 0;
            }
         }

         for(int[] nextVector : vectorList) {
            mdcm.set(this.get(nextVector), nextVector);
         }
      }
   }

   private static class RootsOfUnity implements Serializable {
      private static final long serialVersionUID = 6404784357747329667L;
      private int omegaCount = 0;
      private double[] omegaReal = null;
      private double[] omegaImaginaryForward = null;
      private double[] omegaImaginaryInverse = null;
      private boolean isForward = true;

      public RootsOfUnity() {
      }

      public synchronized boolean isForward() throws IllegalStateException {
         if (this.omegaCount == 0) {
            throw MathRuntimeException.createIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
         } else {
            return this.isForward;
         }
      }

      public synchronized void computeOmega(int n) throws IllegalArgumentException {
         if (n == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_COMPUTE_0TH_ROOT_OF_UNITY);
         } else {
            this.isForward = n > 0;
            int absN = FastMath.abs(n);
            if (absN != this.omegaCount) {
               double t = (Math.PI * 2) / (double)absN;
               double cosT = FastMath.cos(t);
               double sinT = FastMath.sin(t);
               this.omegaReal = new double[absN];
               this.omegaImaginaryForward = new double[absN];
               this.omegaImaginaryInverse = new double[absN];
               this.omegaReal[0] = 1.0;
               this.omegaImaginaryForward[0] = 0.0;
               this.omegaImaginaryInverse[0] = 0.0;

               for(int i = 1; i < absN; ++i) {
                  this.omegaReal[i] = this.omegaReal[i - 1] * cosT + this.omegaImaginaryForward[i - 1] * sinT;
                  this.omegaImaginaryForward[i] = this.omegaImaginaryForward[i - 1] * cosT - this.omegaReal[i - 1] * sinT;
                  this.omegaImaginaryInverse[i] = -this.omegaImaginaryForward[i];
               }

               this.omegaCount = absN;
            }
         }
      }

      public synchronized double getOmegaReal(int k) throws IllegalStateException, IllegalArgumentException {
         if (this.omegaCount == 0) {
            throw MathRuntimeException.createIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
         } else if (k >= 0 && k < this.omegaCount) {
            return this.omegaReal[k];
         } else {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_ROOT_OF_UNITY_INDEX, k, 0, this.omegaCount - 1);
         }
      }

      public synchronized double getOmegaImaginary(int k) throws IllegalStateException, IllegalArgumentException {
         if (this.omegaCount == 0) {
            throw MathRuntimeException.createIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
         } else if (k >= 0 && k < this.omegaCount) {
            return this.isForward ? this.omegaImaginaryForward[k] : this.omegaImaginaryInverse[k];
         } else {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_RANGE_ROOT_OF_UNITY_INDEX, k, 0, this.omegaCount - 1);
         }
      }
   }
}
