package org.apache.commons.math.stat.descriptive.moment;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;

public class VectorialCovariance implements Serializable {
   private static final long serialVersionUID = 4118372414238930270L;
   private final double[] sums;
   private final double[] productsSums;
   private final boolean isBiasCorrected;
   private long n;

   public VectorialCovariance(int dimension, boolean isBiasCorrected) {
      this.sums = new double[dimension];
      this.productsSums = new double[dimension * (dimension + 1) / 2];
      this.n = 0L;
      this.isBiasCorrected = isBiasCorrected;
   }

   public void increment(double[] v) throws DimensionMismatchException {
      if (v.length != this.sums.length) {
         throw new DimensionMismatchException(v.length, this.sums.length);
      } else {
         int k = 0;

         for(int i = 0; i < v.length; ++i) {
            this.sums[i] += v[i];

            for(int j = 0; j <= i; ++j) {
               int var10001 = k++;
               this.productsSums[var10001] += v[i] * v[j];
            }
         }

         ++this.n;
      }
   }

   public RealMatrix getResult() {
      int dimension = this.sums.length;
      RealMatrix result = MatrixUtils.createRealMatrix(dimension, dimension);
      if (this.n > 1L) {
         double c = 1.0 / (double)(this.n * (this.isBiasCorrected ? this.n - 1L : this.n));
         int k = 0;

         for(int i = 0; i < dimension; ++i) {
            for(int j = 0; j <= i; ++j) {
               double e = c * ((double)this.n * this.productsSums[k++] - this.sums[i] * this.sums[j]);
               result.setEntry(i, j, e);
               result.setEntry(j, i, e);
            }
         }
      }

      return result;
   }

   public long getN() {
      return this.n;
   }

   public void clear() {
      this.n = 0L;
      Arrays.fill(this.sums, 0.0);
      Arrays.fill(this.productsSums, 0.0);
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.isBiasCorrected ? 1231 : 1237);
      result = 31 * result + (int)(this.n ^ this.n >>> 32);
      result = 31 * result + Arrays.hashCode(this.productsSums);
      return 31 * result + Arrays.hashCode(this.sums);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof VectorialCovariance)) {
         return false;
      } else {
         VectorialCovariance other = (VectorialCovariance)obj;
         if (this.isBiasCorrected != other.isBiasCorrected) {
            return false;
         } else if (this.n != other.n) {
            return false;
         } else if (!Arrays.equals(this.productsSums, other.productsSums)) {
            return false;
         } else {
            return Arrays.equals(this.sums, other.sums);
         }
      }
   }
}
