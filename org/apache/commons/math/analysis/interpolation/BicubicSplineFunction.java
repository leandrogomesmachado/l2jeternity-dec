package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.analysis.BivariateRealFunction;
import org.apache.commons.math.exception.OutOfRangeException;

class BicubicSplineFunction implements BivariateRealFunction {
   private static final short N = 4;
   private final double[][] a = new double[4][4];
   private BivariateRealFunction partialDerivativeX;
   private BivariateRealFunction partialDerivativeY;
   private BivariateRealFunction partialDerivativeXX;
   private BivariateRealFunction partialDerivativeYY;
   private BivariateRealFunction partialDerivativeXY;

   public BicubicSplineFunction(double[] a) {
      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 4; ++j) {
            this.a[i][j] = a[i + 4 * j];
         }
      }
   }

   @Override
   public double value(double x, double y) {
      if (x < 0.0 || x > 1.0) {
         throw new OutOfRangeException(x, 0, 1);
      } else if (!(y < 0.0) && !(y > 1.0)) {
         double x2 = x * x;
         double x3 = x2 * x;
         double[] pX = new double[]{1.0, x, x2, x3};
         double y2 = y * y;
         double y3 = y2 * y;
         double[] pY = new double[]{1.0, y, y2, y3};
         return this.apply(pX, pY, this.a);
      } else {
         throw new OutOfRangeException(y, 0, 1);
      }
   }

   private double apply(double[] pX, double[] pY, double[][] coeff) {
      double result = 0.0;

      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 4; ++j) {
            result += coeff[i][j] * pX[i] * pY[j];
         }
      }

      return result;
   }

   public BivariateRealFunction partialDerivativeX() {
      if (this.partialDerivativeX == null) {
         this.computePartialDerivatives();
      }

      return this.partialDerivativeX;
   }

   public BivariateRealFunction partialDerivativeY() {
      if (this.partialDerivativeY == null) {
         this.computePartialDerivatives();
      }

      return this.partialDerivativeY;
   }

   public BivariateRealFunction partialDerivativeXX() {
      if (this.partialDerivativeXX == null) {
         this.computePartialDerivatives();
      }

      return this.partialDerivativeXX;
   }

   public BivariateRealFunction partialDerivativeYY() {
      if (this.partialDerivativeYY == null) {
         this.computePartialDerivatives();
      }

      return this.partialDerivativeYY;
   }

   public BivariateRealFunction partialDerivativeXY() {
      if (this.partialDerivativeXY == null) {
         this.computePartialDerivatives();
      }

      return this.partialDerivativeXY;
   }

   private void computePartialDerivatives() {
      final double[][] aX = new double[4][4];
      final double[][] aY = new double[4][4];
      final double[][] aXX = new double[4][4];
      final double[][] aYY = new double[4][4];
      final double[][] aXY = new double[4][4];

      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 4; ++j) {
            double c = this.a[i][j];
            aX[i][j] = (double)i * c;
            aY[i][j] = (double)j * c;
            aXX[i][j] = (double)(i - 1) * aX[i][j];
            aYY[i][j] = (double)(j - 1) * aY[i][j];
            aXY[i][j] = (double)j * aX[i][j];
         }
      }

      this.partialDerivativeX = new BivariateRealFunction() {
         @Override
         public double value(double x, double y) {
            double x2 = x * x;
            double[] pX = new double[]{0.0, 1.0, x, x2};
            double y2 = y * y;
            double y3 = y2 * y;
            double[] pY = new double[]{1.0, y, y2, y3};
            return BicubicSplineFunction.this.apply(pX, pY, aX);
         }
      };
      this.partialDerivativeY = new BivariateRealFunction() {
         @Override
         public double value(double x, double y) {
            double x2 = x * x;
            double x3 = x2 * x;
            double[] pX = new double[]{1.0, x, x2, x3};
            double y2 = y * y;
            double[] pY = new double[]{0.0, 1.0, y, y2};
            return BicubicSplineFunction.this.apply(pX, pY, aY);
         }
      };
      this.partialDerivativeXX = new BivariateRealFunction() {
         @Override
         public double value(double x, double y) {
            double[] pX = new double[]{0.0, 0.0, 1.0, x};
            double y2 = y * y;
            double y3 = y2 * y;
            double[] pY = new double[]{1.0, y, y2, y3};
            return BicubicSplineFunction.this.apply(pX, pY, aXX);
         }
      };
      this.partialDerivativeYY = new BivariateRealFunction() {
         @Override
         public double value(double x, double y) {
            double x2 = x * x;
            double x3 = x2 * x;
            double[] pX = new double[]{1.0, x, x2, x3};
            double[] pY = new double[]{0.0, 0.0, 1.0, y};
            return BicubicSplineFunction.this.apply(pX, pY, aYY);
         }
      };
      this.partialDerivativeXY = new BivariateRealFunction() {
         @Override
         public double value(double x, double y) {
            double x2 = x * x;
            double[] pX = new double[]{0.0, 1.0, x, x2};
            double y2 = y * y;
            double[] pY = new double[]{0.0, 1.0, y, y2};
            return BicubicSplineFunction.this.apply(pX, pY, aXY);
         }
      };
   }
}
