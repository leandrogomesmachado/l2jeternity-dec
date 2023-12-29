package org.apache.commons.math.analysis.interpolation;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.BivariateRealFunction;
import org.apache.commons.math.exception.NoDataException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.util.MathUtils;

public class BicubicSplineInterpolatingFunction implements BivariateRealFunction {
   private static final double[][] AINV = new double[][]{
      {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {-3.0, 3.0, 0.0, 0.0, -2.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {2.0, -2.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -3.0, 3.0, 0.0, 0.0, -2.0, -1.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, -2.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0},
      {-3.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, -2.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, -3.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, -2.0, 0.0, -1.0, 0.0},
      {9.0, -9.0, -9.0, 9.0, 6.0, 3.0, -6.0, -3.0, 6.0, -6.0, 3.0, -3.0, 4.0, 2.0, 2.0, 1.0},
      {-6.0, 6.0, 6.0, -6.0, -3.0, -3.0, 3.0, 3.0, -4.0, 4.0, -2.0, 2.0, -2.0, -2.0, -1.0, -1.0},
      {2.0, 0.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 2.0, 0.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0},
      {-6.0, 6.0, 6.0, -6.0, -4.0, -2.0, 4.0, 2.0, -3.0, 3.0, -3.0, 3.0, -2.0, -1.0, -2.0, -1.0},
      {4.0, -4.0, -4.0, 4.0, 2.0, 2.0, -2.0, -2.0, 2.0, -2.0, 2.0, -2.0, 1.0, 1.0, 1.0, 1.0}
   };
   private final double[] xval;
   private final double[] yval;
   private final BicubicSplineFunction[][] splines;
   private BivariateRealFunction[][][] partialDerivatives = (BivariateRealFunction[][][])null;

   public BicubicSplineInterpolatingFunction(double[] x, double[] y, double[][] f, double[][] dFdX, double[][] dFdY, double[][] d2FdXdY) throws DimensionMismatchException {
      int xLen = x.length;
      int yLen = y.length;
      if (xLen != 0 && yLen != 0 && f.length != 0 && f[0].length != 0) {
         if (xLen != f.length) {
            throw new DimensionMismatchException(xLen, f.length);
         } else if (xLen != dFdX.length) {
            throw new DimensionMismatchException(xLen, dFdX.length);
         } else if (xLen != dFdY.length) {
            throw new DimensionMismatchException(xLen, dFdY.length);
         } else if (xLen != d2FdXdY.length) {
            throw new DimensionMismatchException(xLen, d2FdXdY.length);
         } else {
            MathUtils.checkOrder(x);
            MathUtils.checkOrder(y);
            this.xval = (double[])x.clone();
            this.yval = (double[])y.clone();
            int lastI = xLen - 1;
            int lastJ = yLen - 1;
            this.splines = new BicubicSplineFunction[lastI][lastJ];

            for(int i = 0; i < lastI; ++i) {
               if (f[i].length != yLen) {
                  throw new DimensionMismatchException(f[i].length, yLen);
               }

               if (dFdX[i].length != yLen) {
                  throw new DimensionMismatchException(dFdX[i].length, yLen);
               }

               if (dFdY[i].length != yLen) {
                  throw new DimensionMismatchException(dFdY[i].length, yLen);
               }

               if (d2FdXdY[i].length != yLen) {
                  throw new DimensionMismatchException(d2FdXdY[i].length, yLen);
               }

               int ip1 = i + 1;

               for(int j = 0; j < lastJ; ++j) {
                  int jp1 = j + 1;
                  double[] beta = new double[]{
                     f[i][j],
                     f[ip1][j],
                     f[i][jp1],
                     f[ip1][jp1],
                     dFdX[i][j],
                     dFdX[ip1][j],
                     dFdX[i][jp1],
                     dFdX[ip1][jp1],
                     dFdY[i][j],
                     dFdY[ip1][j],
                     dFdY[i][jp1],
                     dFdY[ip1][jp1],
                     d2FdXdY[i][j],
                     d2FdXdY[ip1][j],
                     d2FdXdY[i][jp1],
                     d2FdXdY[ip1][jp1]
                  };
                  this.splines[i][j] = new BicubicSplineFunction(this.computeSplineCoefficients(beta));
               }
            }
         }
      } else {
         throw new NoDataException();
      }
   }

   @Override
   public double value(double x, double y) {
      int i = this.searchIndex(x, this.xval);
      if (i == -1) {
         throw new OutOfRangeException(x, this.xval[0], this.xval[this.xval.length - 1]);
      } else {
         int j = this.searchIndex(y, this.yval);
         if (j == -1) {
            throw new OutOfRangeException(y, this.yval[0], this.yval[this.yval.length - 1]);
         } else {
            double xN = (x - this.xval[i]) / (this.xval[i + 1] - this.xval[i]);
            double yN = (y - this.yval[j]) / (this.yval[j + 1] - this.yval[j]);
            return this.splines[i][j].value(xN, yN);
         }
      }
   }

   public double partialDerivativeX(double x, double y) {
      return this.partialDerivative(0, x, y);
   }

   public double partialDerivativeY(double x, double y) {
      return this.partialDerivative(1, x, y);
   }

   public double partialDerivativeXX(double x, double y) {
      return this.partialDerivative(2, x, y);
   }

   public double partialDerivativeYY(double x, double y) {
      return this.partialDerivative(3, x, y);
   }

   public double partialDerivativeXY(double x, double y) {
      return this.partialDerivative(4, x, y);
   }

   private double partialDerivative(int which, double x, double y) {
      if (this.partialDerivatives == null) {
         this.computePartialDerivatives();
      }

      int i = this.searchIndex(x, this.xval);
      if (i == -1) {
         throw new OutOfRangeException(x, this.xval[0], this.xval[this.xval.length - 1]);
      } else {
         int j = this.searchIndex(y, this.yval);
         if (j == -1) {
            throw new OutOfRangeException(y, this.yval[0], this.yval[this.yval.length - 1]);
         } else {
            double xN = (x - this.xval[i]) / (this.xval[i + 1] - this.xval[i]);
            double yN = (y - this.yval[j]) / (this.yval[j + 1] - this.yval[j]);

            try {
               return this.partialDerivatives[which][i][j].value(xN, yN);
            } catch (FunctionEvaluationException var13) {
               throw new RuntimeException(var13);
            }
         }
      }
   }

   private void computePartialDerivatives() {
      int lastI = this.xval.length - 1;
      int lastJ = this.yval.length - 1;
      this.partialDerivatives = new BivariateRealFunction[5][lastI][lastJ];

      for(int i = 0; i < lastI; ++i) {
         for(int j = 0; j < lastJ; ++j) {
            BicubicSplineFunction f = this.splines[i][j];
            this.partialDerivatives[0][i][j] = f.partialDerivativeX();
            this.partialDerivatives[1][i][j] = f.partialDerivativeY();
            this.partialDerivatives[2][i][j] = f.partialDerivativeXX();
            this.partialDerivatives[3][i][j] = f.partialDerivativeYY();
            this.partialDerivatives[4][i][j] = f.partialDerivativeXY();
         }
      }
   }

   private int searchIndex(double c, double[] val) {
      if (c < val[0]) {
         return -1;
      } else {
         int max = val.length;

         for(int i = 1; i < max; ++i) {
            if (c <= val[i]) {
               return i - 1;
            }
         }

         return -1;
      }
   }

   private double[] computeSplineCoefficients(double[] beta) {
      double[] a = new double[16];

      for(int i = 0; i < 16; ++i) {
         double result = 0.0;
         double[] row = AINV[i];

         for(int j = 0; j < 16; ++j) {
            result += row[j] * beta[j];
         }

         a[i] = result;
      }

      return a;
   }
}
