package org.apache.commons.math.transform;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class FastHadamardTransformer implements RealTransformer {
   @Override
   public double[] transform(double[] f) throws IllegalArgumentException {
      return this.fht(f);
   }

   @Override
   public double[] transform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      return this.fht(FastFourierTransformer.sample(f, min, max, n));
   }

   @Override
   public double[] inversetransform(double[] f) throws IllegalArgumentException {
      return FastFourierTransformer.scaleArray(this.fht(f), 1.0 / (double)f.length);
   }

   @Override
   public double[] inversetransform(UnivariateRealFunction f, double min, double max, int n) throws FunctionEvaluationException, IllegalArgumentException {
      double[] unscaled = this.fht(FastFourierTransformer.sample(f, min, max, n));
      return FastFourierTransformer.scaleArray(unscaled, 1.0 / (double)n);
   }

   public int[] transform(int[] f) throws IllegalArgumentException {
      return this.fht(f);
   }

   protected double[] fht(double[] x) throws IllegalArgumentException {
      int n = x.length;
      int halfN = n / 2;
      if (!FastFourierTransformer.isPowerOf2((long)n)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO, n);
      } else {
         double[] yPrevious = new double[n];
         double[] yCurrent = (double[])x.clone();

         for(int j = 1; j < n; j <<= 1) {
            double[] yTmp = yCurrent;
            yCurrent = yPrevious;
            yPrevious = yTmp;

            for(int i = 0; i < halfN; ++i) {
               int twoI = 2 * i;
               yCurrent[i] = yPrevious[twoI] + yPrevious[twoI + 1];
            }

            for(int i = halfN; i < n; ++i) {
               int twoI = 2 * i;
               yCurrent[i] = yPrevious[twoI - n] - yPrevious[twoI - n + 1];
            }
         }

         return yCurrent;
      }
   }

   protected int[] fht(int[] x) throws IllegalArgumentException {
      int n = x.length;
      int halfN = n / 2;
      if (!FastFourierTransformer.isPowerOf2((long)n)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POWER_OF_TWO, n);
      } else {
         int[] yPrevious = new int[n];
         int[] yCurrent = (int[])x.clone();

         for(int j = 1; j < n; j <<= 1) {
            int[] yTmp = yCurrent;
            yCurrent = yPrevious;
            yPrevious = yTmp;

            for(int i = 0; i < halfN; ++i) {
               int twoI = 2 * i;
               yCurrent[i] = yPrevious[twoI] + yPrevious[twoI + 1];
            }

            for(int i = halfN; i < n; ++i) {
               int twoI = 2 * i;
               yCurrent[i] = yPrevious[twoI - n] - yPrevious[twoI - n + 1];
            }
         }

         return yCurrent;
      }
   }
}
