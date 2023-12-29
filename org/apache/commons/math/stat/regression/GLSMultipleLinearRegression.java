package org.apache.commons.math.stat.regression;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

public class GLSMultipleLinearRegression extends AbstractMultipleLinearRegression {
   private RealMatrix Omega;
   private RealMatrix OmegaInverse;

   public void newSampleData(double[] y, double[][] x, double[][] covariance) {
      this.validateSampleData(x, y);
      this.newYSampleData(y);
      this.newXSampleData(x);
      this.validateCovarianceData(x, covariance);
      this.newCovarianceData(covariance);
   }

   protected void newCovarianceData(double[][] omega) {
      this.Omega = new Array2DRowRealMatrix(omega);
      this.OmegaInverse = null;
   }

   protected RealMatrix getOmegaInverse() {
      if (this.OmegaInverse == null) {
         this.OmegaInverse = new LUDecompositionImpl(this.Omega).getSolver().getInverse();
      }

      return this.OmegaInverse;
   }

   @Override
   protected RealVector calculateBeta() {
      RealMatrix OI = this.getOmegaInverse();
      RealMatrix XT = this.X.transpose();
      RealMatrix XTOIX = XT.multiply(OI).multiply(this.X);
      RealMatrix inverse = new LUDecompositionImpl(XTOIX).getSolver().getInverse();
      return inverse.multiply(XT).multiply(OI).operate(this.Y);
   }

   @Override
   protected RealMatrix calculateBetaVariance() {
      RealMatrix OI = this.getOmegaInverse();
      RealMatrix XTOIX = this.X.transpose().multiply(OI).multiply(this.X);
      return new LUDecompositionImpl(XTOIX).getSolver().getInverse();
   }

   @Override
   protected double calculateErrorVariance() {
      RealVector residuals = this.calculateResiduals();
      double t = residuals.dotProduct(this.getOmegaInverse().operate(residuals));
      return t / (double)(this.X.getRowDimension() - this.X.getColumnDimension());
   }
}
