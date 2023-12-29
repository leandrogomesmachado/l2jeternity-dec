package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;

public class Covariance {
   private final RealMatrix covarianceMatrix;
   private final int n;

   public Covariance() {
      this.covarianceMatrix = null;
      this.n = 0;
   }

   public Covariance(double[][] data, boolean biasCorrected) {
      this(new BlockRealMatrix(data), biasCorrected);
   }

   public Covariance(double[][] data) {
      this(data, true);
   }

   public Covariance(RealMatrix matrix, boolean biasCorrected) {
      this.checkSufficientData(matrix);
      this.n = matrix.getRowDimension();
      this.covarianceMatrix = this.computeCovarianceMatrix(matrix, biasCorrected);
   }

   public Covariance(RealMatrix matrix) {
      this(matrix, true);
   }

   public RealMatrix getCovarianceMatrix() {
      return this.covarianceMatrix;
   }

   public int getN() {
      return this.n;
   }

   protected RealMatrix computeCovarianceMatrix(RealMatrix matrix, boolean biasCorrected) {
      int dimension = matrix.getColumnDimension();
      Variance variance = new Variance(biasCorrected);
      RealMatrix outMatrix = new BlockRealMatrix(dimension, dimension);

      for(int i = 0; i < dimension; ++i) {
         for(int j = 0; j < i; ++j) {
            double cov = this.covariance(matrix.getColumn(i), matrix.getColumn(j), biasCorrected);
            outMatrix.setEntry(i, j, cov);
            outMatrix.setEntry(j, i, cov);
         }

         outMatrix.setEntry(i, i, variance.evaluate(matrix.getColumn(i)));
      }

      return outMatrix;
   }

   protected RealMatrix computeCovarianceMatrix(RealMatrix matrix) {
      return this.computeCovarianceMatrix(matrix, true);
   }

   protected RealMatrix computeCovarianceMatrix(double[][] data, boolean biasCorrected) {
      return this.computeCovarianceMatrix(new BlockRealMatrix(data), biasCorrected);
   }

   protected RealMatrix computeCovarianceMatrix(double[][] data) {
      return this.computeCovarianceMatrix(data, true);
   }

   public double covariance(double[] xArray, double[] yArray, boolean biasCorrected) throws IllegalArgumentException {
      Mean mean = new Mean();
      double result = 0.0;
      int length = xArray.length;
      if (length != yArray.length) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, length, yArray.length);
      } else if (length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, length, 2);
      } else {
         double xMean = mean.evaluate(xArray);
         double yMean = mean.evaluate(yArray);

         for(int i = 0; i < length; ++i) {
            double xDev = xArray[i] - xMean;
            double yDev = yArray[i] - yMean;
            result += (xDev * yDev - result) / (double)(i + 1);
         }

         return biasCorrected ? result * ((double)length / (double)(length - 1)) : result;
      }
   }

   public double covariance(double[] xArray, double[] yArray) throws IllegalArgumentException {
      return this.covariance(xArray, yArray, true);
   }

   private void checkSufficientData(RealMatrix matrix) {
      int nRows = matrix.getRowDimension();
      int nCols = matrix.getColumnDimension();
      if (nRows < 2 || nCols < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS, nRows, nCols);
      }
   }
}
