package org.apache.commons.math.stat.correlation;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.commons.math.util.FastMath;

public class PearsonsCorrelation {
   private final RealMatrix correlationMatrix;
   private final int nObs;

   public PearsonsCorrelation() {
      this.correlationMatrix = null;
      this.nObs = 0;
   }

   public PearsonsCorrelation(double[][] data) {
      this(new BlockRealMatrix(data));
   }

   public PearsonsCorrelation(RealMatrix matrix) {
      this.checkSufficientData(matrix);
      this.nObs = matrix.getRowDimension();
      this.correlationMatrix = this.computeCorrelationMatrix(matrix);
   }

   public PearsonsCorrelation(Covariance covariance) {
      RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
      if (covarianceMatrix == null) {
         throw new NullArgumentException(LocalizedFormats.COVARIANCE_MATRIX);
      } else {
         this.nObs = covariance.getN();
         this.correlationMatrix = this.covarianceToCorrelation(covarianceMatrix);
      }
   }

   public PearsonsCorrelation(RealMatrix covarianceMatrix, int numberOfObservations) {
      this.nObs = numberOfObservations;
      this.correlationMatrix = this.covarianceToCorrelation(covarianceMatrix);
   }

   public RealMatrix getCorrelationMatrix() {
      return this.correlationMatrix;
   }

   public RealMatrix getCorrelationStandardErrors() {
      int nVars = this.correlationMatrix.getColumnDimension();
      double[][] out = new double[nVars][nVars];

      for(int i = 0; i < nVars; ++i) {
         for(int j = 0; j < nVars; ++j) {
            double r = this.correlationMatrix.getEntry(i, j);
            out[i][j] = FastMath.sqrt((1.0 - r * r) / (double)(this.nObs - 2));
         }
      }

      return new BlockRealMatrix(out);
   }

   public RealMatrix getCorrelationPValues() throws MathException {
      TDistribution tDistribution = new TDistributionImpl((double)(this.nObs - 2));
      int nVars = this.correlationMatrix.getColumnDimension();
      double[][] out = new double[nVars][nVars];

      for(int i = 0; i < nVars; ++i) {
         for(int j = 0; j < nVars; ++j) {
            if (i == j) {
               out[i][j] = 0.0;
            } else {
               double r = this.correlationMatrix.getEntry(i, j);
               double t = FastMath.abs(r * FastMath.sqrt((double)(this.nObs - 2) / (1.0 - r * r)));
               out[i][j] = 2.0 * tDistribution.cumulativeProbability(-t);
            }
         }
      }

      return new BlockRealMatrix(out);
   }

   public RealMatrix computeCorrelationMatrix(RealMatrix matrix) {
      int nVars = matrix.getColumnDimension();
      RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);

      for(int i = 0; i < nVars; ++i) {
         for(int j = 0; j < i; ++j) {
            double corr = this.correlation(matrix.getColumn(i), matrix.getColumn(j));
            outMatrix.setEntry(i, j, corr);
            outMatrix.setEntry(j, i, corr);
         }

         outMatrix.setEntry(i, i, 1.0);
      }

      return outMatrix;
   }

   public RealMatrix computeCorrelationMatrix(double[][] data) {
      return this.computeCorrelationMatrix(new BlockRealMatrix(data));
   }

   public double correlation(double[] xArray, double[] yArray) throws IllegalArgumentException {
      SimpleRegression regression = new SimpleRegression();
      if (xArray.length != yArray.length) {
         throw new DimensionMismatchException(xArray.length, yArray.length);
      } else if (xArray.length < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
      } else {
         for(int i = 0; i < xArray.length; ++i) {
            regression.addData(xArray[i], yArray[i]);
         }

         return regression.getR();
      }
   }

   public RealMatrix covarianceToCorrelation(RealMatrix covarianceMatrix) {
      int nVars = covarianceMatrix.getColumnDimension();
      RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);

      for(int i = 0; i < nVars; ++i) {
         double sigma = FastMath.sqrt(covarianceMatrix.getEntry(i, i));
         outMatrix.setEntry(i, i, 1.0);

         for(int j = 0; j < i; ++j) {
            double entry = covarianceMatrix.getEntry(i, j) / (sigma * FastMath.sqrt(covarianceMatrix.getEntry(j, j)));
            outMatrix.setEntry(i, j, entry);
            outMatrix.setEntry(j, i, entry);
         }
      }

      return outMatrix;
   }

   private void checkSufficientData(RealMatrix matrix) {
      int nRows = matrix.getRowDimension();
      int nCols = matrix.getColumnDimension();
      if (nRows < 2 || nCols < 2) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS, nRows, nCols);
      }
   }
}
