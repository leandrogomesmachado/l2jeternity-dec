package org.apache.commons.math.transform;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class FastSineTransformer implements RealTransformer {
   @Override
   public double[] transform(double[] f) throws IllegalArgumentException {
      return this.fst(f);
   }

   @Override
   public double[] transform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      data[0] = 0.0;
      return this.fst(data);
   }

   public double[] transform2(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = FastMath.sqrt(2.0 / (double)f.length);
      return FastFourierTransformer.scaleArray(this.fst(f), scaling_coefficient);
   }

   public double[] transform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      data[0] = 0.0;
      double scaling_coefficient = FastMath.sqrt(2.0 / (double)n);
      return FastFourierTransformer.scaleArray(this.fst(data), scaling_coefficient);
   }

   @Override
   public double[] inversetransform(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = 2.0 / (double)f.length;
      return FastFourierTransformer.scaleArray(this.fst(f), scaling_coefficient);
   }

   @Override
   public double[] inversetransform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      data[0] = 0.0;
      double scaling_coefficient = 2.0 / (double)n;
      return FastFourierTransformer.scaleArray(this.fst(data), scaling_coefficient);
   }

   public double[] inversetransform2(double[] f) throws IllegalArgumentException {
      return this.transform2(f);
   }

   public double[] inversetransform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      return this.transform2(f, min, max, n);
   }

   protected double[] fst(double[] f) throws IllegalArgumentException {
      double[] transformed = new double[f.length];
      FastFourierTransformer.verifyDataSet(f);
      if (f[0] != 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.FIRST_ELEMENT_NOT_ZERO, f[0]);
      } else {
         int n = f.length;
         if (n == 1) {
            transformed[0] = 0.0;
            return transformed;
         } else {
            double[] x = new double[n];
            x[0] = 0.0;
            x[n >> 1] = 2.0 * f[n >> 1];

            for(int i = 1; i < n >> 1; ++i) {
               double a = FastMath.sin((double)i * Math.PI / (double)n) * (f[i] + f[n - i]);
               double b = 0.5 * (f[i] - f[n - i]);
               x[i] = a + b;
               x[n - i] = a - b;
            }

            FastFourierTransformer transformer = new FastFourierTransformer();
            Complex[] y = transformer.transform(x);
            transformed[0] = 0.0;
            transformed[1] = 0.5 * y[0].getReal();

            for(int i = 1; i < n >> 1; ++i) {
               transformed[2 * i] = -y[i].getImaginary();
               transformed[2 * i + 1] = y[i].getReal() + transformed[2 * i - 1];
            }

            return transformed;
         }
      }
   }
}
