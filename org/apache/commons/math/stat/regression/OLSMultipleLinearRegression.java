package org.apache.commons.math.stat.regression;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.SecondMoment;

public class OLSMultipleLinearRegression extends AbstractMultipleLinearRegression {
   private QRDecomposition qr = null;

   public void newSampleData(double[] y, double[][] x) {
      this.validateSampleData(x, y);
      this.newYSampleData(y);
      this.newXSampleData(x);
   }

   @Override
   public void newSampleData(double[] data, int nobs, int nvars) {
      super.newSampleData(data, nobs, nvars);
      this.qr = new QRDecompositionImpl(this.X);
   }

   public RealMatrix calculateHat() {
      RealMatrix Q = this.qr.getQ();
      int p = this.qr.getR().getColumnDimension();
      int n = Q.getColumnDimension();
      Array2DRowRealMatrix augI = new Array2DRowRealMatrix(n, n);
      double[][] augIData = augI.getDataRef();

      for(int i = 0; i < n; ++i) {
         for(int j = 0; j < n; ++j) {
            if (i == j && i < p) {
               augIData[i][j] = 1.0;
            } else {
               augIData[i][j] = 0.0;
            }
         }
      }

      return Q.multiply(augI).multiply(Q.transpose());
   }

   public double calculateTotalSumOfSquares() {
      return this.isNoIntercept() ? StatUtils.sumSq(this.Y.getData()) : new SecondMoment().evaluate(this.Y.getData());
   }

   public double calculateResidualSumOfSquares() {
      RealVector residuals = this.calculateResiduals();
      return residuals.dotProduct(residuals);
   }

   public double calculateRSquared() {
      return 1.0 - this.calculateResidualSumOfSquares() / this.calculateTotalSumOfSquares();
   }

   public double calculateAdjustedRSquared() {
      double n = (double)this.X.getRowDimension();
      return this.isNoIntercept()
         ? 1.0 - (1.0 - this.calculateRSquared()) * (n / (n - (double)this.X.getColumnDimension()))
         : 1.0 - this.calculateResidualSumOfSquares() * (n - 1.0) / (this.calculateTotalSumOfSquares() * (n - (double)this.X.getColumnDimension()));
   }

   @Override
   protected void newXSampleData(double[][] x) {
      super.newXSampleData(x);
      this.qr = new QRDecompositionImpl(this.X);
   }

   @Override
   protected RealVector calculateBeta() {
      return this.qr.getSolver().solve(this.Y);
   }

   @Override
   protected RealMatrix calculateBetaVariance() {
      int p = this.X.getColumnDimension();
      RealMatrix Raug = this.qr.getR().getSubMatrix(0, p - 1, 0, p - 1);
      RealMatrix Rinv = new LUDecompositionImpl(Raug).getSolver().getInverse();
      return Rinv.multiply(Rinv.transpose());
   }
}
