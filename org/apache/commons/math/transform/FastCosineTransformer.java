package org.apache.commons.math.transform;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class FastCosineTransformer implements RealTransformer {
   @Override
   public double[] transform(double[] f) throws IllegalArgumentException {
      return this.fct(f);
   }

   @Override
   public double[] transform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      return this.fct(data);
   }

   public double[] transform2(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = FastMath.sqrt(2.0 / (double)(f.length - 1));
      return FastFourierTransformer.scaleArray(this.fct(f), scaling_coefficient);
   }

   public double[] transform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      double scaling_coefficient = FastMath.sqrt(2.0 / (double)(n - 1));
      return FastFourierTransformer.scaleArray(this.fct(data), scaling_coefficient);
   }

   @Override
   public double[] inversetransform(double[] f) throws IllegalArgumentException {
      double scaling_coefficient = 2.0 / (double)(f.length - 1);
      return FastFourierTransformer.scaleArray(this.fct(f), scaling_coefficient);
   }

   @Override
   public double[] inversetransform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] data = FastFourierTransformer.sample(f, min, max, n);
      double scaling_coefficient = 2.0 / (double)(n - 1);
      return FastFourierTransformer.scaleArray(this.fct(data), scaling_coefficient);
   }

   public double[] inversetransform2(double[] f) throws IllegalArgumentException {
      return this.transform2(f);
   }

   public double[] inversetransform2(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      return this.transform2(f, min, max, n);
   }

   protected double[] fct(double[] f) throws IllegalArgumentException {
      double[] transformed = new double[f.length];
      int n = f.length - 1;
      if (!FastFourierTransformer.isPowerOf2((long)n)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO_PLUS_ONE, f.length);
      } else if (n == 1) {
         transformed[0] = 0.5 * (f[0] + f[1]);
         transformed[1] = 0.5 * (f[0] - f[1]);
         return transformed;
      } else {
         double[] x = new double[n];
         x[0] = 0.5 * (f[0] + f[n]);
         x[n >> 1] = f[n >> 1];
         double t1 = 0.5 * (f[0] - f[n]);

         for(int i = 1; i < n >> 1; ++i) {
            double a = 0.5 * (f[i] + f[n - i]);
            double b = FastMath.sin((double)i * Math.PI / (double)n) * (f[i] - f[n - i]);
            double c = FastMath.cos((double)i * Math.PI / (double)n) * (f[i] - f[n - i]);
            x[i] = a - b;
            x[n - i] = a + b;
            t1 += c;
         }

         FastFourierTransformer transformer = new FastFourierTransformer();
         Complex[] y = transformer.transform(x);
         transformed[0] = y[0].getReal();
         transformed[1] = t1;

         for(int i = 1; i < n >> 1; ++i) {
            transformed[2 * i] = y[i].getReal();
            transformed[2 * i + 1] = transformed[2 * i - 1] - y[i].getImaginary();
         }

         transformed[n] = y[n >> 1].getReal();
         return transformed;
      }
   }
}
