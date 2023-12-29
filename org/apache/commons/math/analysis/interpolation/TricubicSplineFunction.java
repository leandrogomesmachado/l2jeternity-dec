package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.analysis.TrivariateRealFunction;
import org.apache.commons.math.exception.OutOfRangeException;

class TricubicSplineFunction implements TrivariateRealFunction {
   private static final short N = 4;
   private final double[][][] a = new double[4][4][4];

   public TricubicSplineFunction(double[] aV) {
      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 4; ++j) {
            for(int k = 0; k < 4; ++k) {
               this.a[i][j][k] = aV[i + 4 * (j + 4 * k)];
            }
         }
      }
   }

   @Override
   public double value(double x, double y, double z) {
      if (x < 0.0 || x > 1.0) {
         throw new OutOfRangeException(x, 0, 1);
      } else if (!(y < 0.0) && !(y > 1.0)) {
         if (!(z < 0.0) && !(z > 1.0)) {
            double x2 = x * x;
            double x3 = x2 * x;
            double[] pX = new double[]{1.0, x, x2, x3};
            double y2 = y * y;
            double y3 = y2 * y;
            double[] pY = new double[]{1.0, y, y2, y3};
            double z2 = z * z;
            double z3 = z2 * z;
            double[] pZ = new double[]{1.0, z, z2, z3};
            double result = 0.0;

            for(int i = 0; i < 4; ++i) {
               for(int j = 0; j < 4; ++j) {
                  for(int k = 0; k < 4; ++k) {
                     result += this.a[i][j][k] * pX[i] * pY[j] * pZ[k];
                  }
               }
            }

            return result;
         } else {
            throw new OutOfRangeException(z, 0, 1);
         }
      } else {
         throw new OutOfRangeException(y, 0, 1);
      }
   }
}
