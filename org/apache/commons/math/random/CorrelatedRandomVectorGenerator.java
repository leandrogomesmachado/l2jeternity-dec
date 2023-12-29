package org.apache.commons.math.random;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.NotPositiveDefiniteMatrixException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.util.FastMath;

public class CorrelatedRandomVectorGenerator implements RandomVectorGenerator {
   private final double[] mean;
   private final NormalizedRandomGenerator generator;
   private final double[] normalized;
   private RealMatrix root;
   private int rank;

   public CorrelatedRandomVectorGenerator(double[] mean, RealMatrix covariance, double small, NormalizedRandomGenerator generator) throws NotPositiveDefiniteMatrixException, DimensionMismatchException {
      int order = covariance.getRowDimension();
      if (mean.length != order) {
         throw new DimensionMismatchException(mean.length, order);
      } else {
         this.mean = (double[])mean.clone();
         this.decompose(covariance, small);
         this.generator = generator;
         this.normalized = new double[this.rank];
      }
   }

   public CorrelatedRandomVectorGenerator(RealMatrix covariance, double small, NormalizedRandomGenerator generator) throws NotPositiveDefiniteMatrixException {
      int order = covariance.getRowDimension();
      this.mean = new double[order];

      for(int i = 0; i < order; ++i) {
         this.mean[i] = 0.0;
      }

      this.decompose(covariance, small);
      this.generator = generator;
      this.normalized = new double[this.rank];
   }

   public NormalizedRandomGenerator getGenerator() {
      return this.generator;
   }

   public RealMatrix getRootMatrix() {
      return this.root;
   }

   public int getRank() {
      return this.rank;
   }

   private void decompose(RealMatrix covariance, double small) throws NotPositiveDefiniteMatrixException {
      int order = covariance.getRowDimension();
      double[][] c = covariance.getData();
      double[][] b = new double[order][order];
      int[] swap = new int[order];
      int[] index = new int[order];
      int i = 0;

      while(i < order) {
         index[i] = i++;
      }

      this.rank = 0;
      boolean loop = true;

      while(loop) {
         swap[this.rank] = this.rank;

         for(int ix = this.rank + 1; ix < order; ++ix) {
            int ii = index[ix];
            int isi = index[swap[ix]];
            if (c[ii][ii] > c[isi][isi]) {
               swap[this.rank] = ix;
            }
         }

         if (swap[this.rank] != this.rank) {
            int tmp = index[this.rank];
            index[this.rank] = index[swap[this.rank]];
            index[swap[this.rank]] = tmp;
         }

         int ir = index[this.rank];
         if (c[ir][ir] < small) {
            if (this.rank == 0) {
               throw new NotPositiveDefiniteMatrixException();
            }

            for(int ix = this.rank; ix < order; ++ix) {
               if (c[index[ix]][index[ix]] < -small) {
                  throw new NotPositiveDefiniteMatrixException();
               }
            }

            ++this.rank;
            loop = false;
         } else {
            double sqrt = FastMath.sqrt(c[ir][ir]);
            b[this.rank][this.rank] = sqrt;
            double inverse = 1.0 / sqrt;

            for(int ix = this.rank + 1; ix < order; ++ix) {
               int ii = index[ix];
               double e = inverse * c[ii][ir];
               b[ix][this.rank] = e;
               c[ii][ii] -= e * e;

               for(int j = this.rank + 1; j < ix; ++j) {
                  int ij = index[j];
                  double f = c[ii][ij] - e * b[j][this.rank];
                  c[ii][ij] = f;
                  c[ij][ii] = f;
               }
            }

            loop = ++this.rank < order;
         }
      }

      this.root = MatrixUtils.createRealMatrix(order, this.rank);

      for(int ix = 0; ix < order; ++ix) {
         for(int j = 0; j < this.rank; ++j) {
            this.root.setEntry(index[ix], j, b[ix][j]);
         }
      }
   }

   @Override
   public double[] nextVector() {
      for(int i = 0; i < this.rank; ++i) {
         this.normalized[i] = this.generator.nextNormalizedDouble();
      }

      double[] correlated = new double[this.mean.length];

      for(int i = 0; i < correlated.length; ++i) {
         correlated[i] = this.mean[i];

         for(int j = 0; j < this.rank; ++j) {
            correlated[i] += this.root.getEntry(i, j) * this.normalized[j];
         }
      }

      return correlated;
   }
}
