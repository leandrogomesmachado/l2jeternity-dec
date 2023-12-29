package org.apache.commons.math.stat.regression;

import java.io.Serializable;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class SimpleRegression implements Serializable {
   private static final long serialVersionUID = -3004689053607543335L;
   private TDistribution distribution;
   private double sumX = 0.0;
   private double sumXX = 0.0;
   private double sumY = 0.0;
   private double sumYY = 0.0;
   private double sumXY = 0.0;
   private long n = 0L;
   private double xbar = 0.0;
   private double ybar = 0.0;

   public SimpleRegression() {
      this(new TDistributionImpl(1.0));
   }

   @Deprecated
   public SimpleRegression(TDistribution t) {
      this.setDistribution(t);
   }

   public SimpleRegression(int degrees) {
      this.setDistribution(new TDistributionImpl((double)degrees));
   }

   public void addData(double x, double y) {
      if (this.n == 0L) {
         this.xbar = x;
         this.ybar = y;
      } else {
         double dx = x - this.xbar;
         double dy = y - this.ybar;
         this.sumXX += dx * dx * (double)this.n / ((double)this.n + 1.0);
         this.sumYY += dy * dy * (double)this.n / ((double)this.n + 1.0);
         this.sumXY += dx * dy * (double)this.n / ((double)this.n + 1.0);
         this.xbar += dx / ((double)this.n + 1.0);
         this.ybar += dy / ((double)this.n + 1.0);
      }

      this.sumX += x;
      this.sumY += y;
      ++this.n;
      if (this.n > 2L) {
         this.distribution.setDegreesOfFreedom((double)(this.n - 2L));
      }
   }

   public void removeData(double x, double y) {
      if (this.n > 0L) {
         double dx = x - this.xbar;
         double dy = y - this.ybar;
         this.sumXX -= dx * dx * (double)this.n / ((double)this.n - 1.0);
         this.sumYY -= dy * dy * (double)this.n / ((double)this.n - 1.0);
         this.sumXY -= dx * dy * (double)this.n / ((double)this.n - 1.0);
         this.xbar -= dx / ((double)this.n - 1.0);
         this.ybar -= dy / ((double)this.n - 1.0);
         this.sumX -= x;
         this.sumY -= y;
         --this.n;
         if (this.n > 2L) {
            this.distribution.setDegreesOfFreedom((double)(this.n - 2L));
         }
      }
   }

   public void addData(double[][] data) {
      for(int i = 0; i < data.length; ++i) {
         this.addData(data[i][0], data[i][1]);
      }
   }

   public void removeData(double[][] data) {
      for(int i = 0; i < data.length && this.n > 0L; ++i) {
         this.removeData(data[i][0], data[i][1]);
      }
   }

   public void clear() {
      this.sumX = 0.0;
      this.sumXX = 0.0;
      this.sumY = 0.0;
      this.sumYY = 0.0;
      this.sumXY = 0.0;
      this.n = 0L;
   }

   public long getN() {
      return this.n;
   }

   public double predict(double x) {
      double b1 = this.getSlope();
      return this.getIntercept(b1) + b1 * x;
   }

   public double getIntercept() {
      return this.getIntercept(this.getSlope());
   }

   public double getSlope() {
      if (this.n < 2L) {
         return Double.NaN;
      } else {
         return FastMath.abs(this.sumXX) < 5.0E-323 ? Double.NaN : this.sumXY / this.sumXX;
      }
   }

   public double getSumSquaredErrors() {
      return FastMath.max(0.0, this.sumYY - this.sumXY * this.sumXY / this.sumXX);
   }

   public double getTotalSumSquares() {
      return this.n < 2L ? Double.NaN : this.sumYY;
   }

   public double getXSumSquares() {
      return this.n < 2L ? Double.NaN : this.sumXX;
   }

   public double getSumOfCrossProducts() {
      return this.sumXY;
   }

   public double getRegressionSumSquares() {
      return this.getRegressionSumSquares(this.getSlope());
   }

   public double getMeanSquareError() {
      return this.n < 3L ? Double.NaN : this.getSumSquaredErrors() / (double)(this.n - 2L);
   }

   public double getR() {
      double b1 = this.getSlope();
      double result = FastMath.sqrt(this.getRSquare());
      if (b1 < 0.0) {
         result = -result;
      }

      return result;
   }

   public double getRSquare() {
      double ssto = this.getTotalSumSquares();
      return (ssto - this.getSumSquaredErrors()) / ssto;
   }

   public double getInterceptStdErr() {
      return FastMath.sqrt(this.getMeanSquareError() * (1.0 / (double)this.n + this.xbar * this.xbar / this.sumXX));
   }

   public double getSlopeStdErr() {
      return FastMath.sqrt(this.getMeanSquareError() / this.sumXX);
   }

   public double getSlopeConfidenceInterval() throws MathException {
      return this.getSlopeConfidenceInterval(0.05);
   }

   public double getSlopeConfidenceInterval(double alpha) throws MathException {
      if (!(alpha >= 1.0) && !(alpha <= 0.0)) {
         return this.getSlopeStdErr() * this.distribution.inverseCumulativeProbability(1.0 - alpha / 2.0);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL, alpha, 0.0, 1.0);
      }
   }

   public double getSignificance() throws MathException {
      return 2.0 * (1.0 - this.distribution.cumulativeProbability(FastMath.abs(this.getSlope()) / this.getSlopeStdErr()));
   }

   private double getIntercept(double slope) {
      return (this.sumY - slope * this.sumX) / (double)this.n;
   }

   private double getRegressionSumSquares(double slope) {
      return slope * slope * this.sumXX;
   }

   @Deprecated
   public void setDistribution(TDistribution value) {
      this.distribution = value;
      if (this.n > 2L) {
         this.distribution.setDegreesOfFreedom((double)(this.n - 2L));
      }
   }
}
