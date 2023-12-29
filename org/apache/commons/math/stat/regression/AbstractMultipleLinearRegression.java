package org.apache.commons.math.stat.regression;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractMultipleLinearRegression implements MultipleLinearRegression {
   protected RealMatrix X;
   protected RealVector Y;
   private boolean noIntercept = false;

   public boolean isNoIntercept() {
      return this.noIntercept;
   }

   public void setNoIntercept(boolean noIntercept) {
      this.noIntercept = noIntercept;
   }

   public void newSampleData(double[] data, int nobs, int nvars) {
      if (data == null) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);
      } else if (data.length != nobs * (nvars + 1)) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INVALID_REGRESSION_ARRAY, data.length, nobs, nvars);
      } else if (nobs <= nvars) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_ENOUGH_DATA_FOR_NUMBER_OF_PREDICTORS);
      } else {
         double[] y = new double[nobs];
         int cols = this.noIntercept ? nvars : nvars + 1;
         double[][] x = new double[nobs][cols];
         int pointer = 0;

         for(int i = 0; i < nobs; ++i) {
            y[i] = data[pointer++];
            if (!this.noIntercept) {
               x[i][0] = 1.0;
            }

            for(int j = this.noIntercept ? 0 : 1; j < cols; ++j) {
               x[i][j] = data[pointer++];
            }
         }

         this.X = new Array2DRowRealMatrix(x);
         this.Y = new ArrayRealVector(y);
      }
   }

   protected void newYSampleData(double[] y) {
      if (y == null) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);
      } else if (y.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NO_DATA);
      } else {
         this.Y = new ArrayRealVector(y);
      }
   }

   protected void newXSampleData(double[][] x) {
      if (x == null) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NULL_NOT_ALLOWED);
      } else if (x.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NO_DATA);
      } else {
         if (this.noIntercept) {
            this.X = new Array2DRowRealMatrix(x, true);
         } else {
            int nVars = x[0].length;
            double[][] xAug = new double[x.length][nVars + 1];

            for(int i = 0; i < x.length; ++i) {
               if (x[i].length != nVars) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, x[i].length, nVars);
               }

               xAug[i][0] = 1.0;
               System.arraycopy(x[i], 0, xAug[i], 1, nVars);
            }

            this.X = new Array2DRowRealMatrix(xAug, false);
         }
      }
   }

   protected void validateSampleData(double[][] x, double[] y) {
      if (x == null || y == null || x.length != y.length) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, x == null ? 0 : x.length, y == null ? 0 : y.length
         );
      } else if (x.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NO_DATA);
      } else if (x[0].length + 1 > x.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_ENOUGH_DATA_FOR_NUMBER_OF_PREDICTORS, x.length, x[0].length);
      }
   }

   protected void validateCovarianceData(double[][] x, double[][] covariance) {
      if (x.length != covariance.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, x.length, covariance.length);
      } else if (covariance.length > 0 && covariance.length != covariance[0].length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NON_SQUARE_MATRIX, covariance.length, covariance[0].length);
      }
   }

   @Override
   public double[] estimateRegressionParameters() {
      RealVector b = this.calculateBeta();
      return b.getData();
   }

   @Override
   public double[] estimateResiduals() {
      RealVector b = this.calculateBeta();
      RealVector e = this.Y.subtract(this.X.operate(b));
      return e.getData();
   }

   @Override
   public double[][] estimateRegressionParametersVariance() {
      return this.calculateBetaVariance().getData();
   }

   @Override
   public double[] estimateRegressionParametersStandardErrors() {
      double[][] betaVariance = this.estimateRegressionParametersVariance();
      double sigma = this.calculateErrorVariance();
      int length = betaVariance[0].length;
      double[] result = new double[length];

      for(int i = 0; i < length; ++i) {
         result[i] = FastMath.sqrt(sigma * betaVariance[i][i]);
      }

      return result;
   }

   @Override
   public double estimateRegressandVariance() {
      return this.calculateYVariance();
   }

   public double estimateErrorVariance() {
      return this.calculateErrorVariance();
   }

   public double estimateRegressionStandardError() {
      return Math.sqrt(this.estimateErrorVariance());
   }

   protected abstract RealVector calculateBeta();

   protected abstract RealMatrix calculateBetaVariance();

   protected double calculateYVariance() {
      return new Variance().evaluate(this.Y.getData());
   }

   protected double calculateErrorVariance() {
      RealVector residuals = this.calculateResiduals();
      return residuals.dotProduct(residuals) / (double)(this.X.getRowDimension() - this.X.getColumnDimension());
   }

   protected RealVector calculateResiduals() {
      RealVector b = this.calculateBeta();
      return this.Y.subtract(this.X.operate(b));
   }
}
