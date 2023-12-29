package org.apache.commons.math.linear;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class EigenDecompositionImpl implements EigenDecomposition {
   private byte maxIter = 30;
   private double[] main;
   private double[] secondary;
   private TriDiagonalTransformer transformer;
   private double[] realEigenvalues;
   private double[] imagEigenvalues;
   private ArrayRealVector[] eigenvectors;
   private RealMatrix cachedV;
   private RealMatrix cachedD;
   private RealMatrix cachedVt;

   public EigenDecompositionImpl(RealMatrix matrix, double splitTolerance) throws InvalidMatrixException {
      if (this.isSymmetric(matrix)) {
         this.transformToTridiagonal(matrix);
         this.findEigenVectors(this.transformer.getQ().getData());
      } else {
         throw new InvalidMatrixException(LocalizedFormats.ASSYMETRIC_EIGEN_NOT_SUPPORTED);
      }
   }

   public EigenDecompositionImpl(double[] main, double[] secondary, double splitTolerance) throws InvalidMatrixException {
      this.main = (double[])main.clone();
      this.secondary = (double[])secondary.clone();
      this.transformer = null;
      int size = main.length;
      double[][] z = new double[size][size];

      for(int i = 0; i < size; ++i) {
         z[i][i] = 1.0;
      }

      this.findEigenVectors(z);
   }

   private boolean isSymmetric(RealMatrix matrix) {
      int rows = matrix.getRowDimension();
      int columns = matrix.getColumnDimension();
      double eps = (double)(10 * rows * columns) * 1.110223E-16F;

      for(int i = 0; i < rows; ++i) {
         for(int j = i + 1; j < columns; ++j) {
            double mij = matrix.getEntry(i, j);
            double mji = matrix.getEntry(j, i);
            if (FastMath.abs(mij - mji) > FastMath.max(FastMath.abs(mij), FastMath.abs(mji)) * eps) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public RealMatrix getV() throws InvalidMatrixException {
      if (this.cachedV == null) {
         int m = this.eigenvectors.length;
         this.cachedV = MatrixUtils.createRealMatrix(m, m);

         for(int k = 0; k < m; ++k) {
            this.cachedV.setColumnVector(k, this.eigenvectors[k]);
         }
      }

      return this.cachedV;
   }

   @Override
   public RealMatrix getD() throws InvalidMatrixException {
      if (this.cachedD == null) {
         this.cachedD = MatrixUtils.createRealDiagonalMatrix(this.realEigenvalues);
      }

      return this.cachedD;
   }

   @Override
   public RealMatrix getVT() throws InvalidMatrixException {
      if (this.cachedVt == null) {
         int m = this.eigenvectors.length;
         this.cachedVt = MatrixUtils.createRealMatrix(m, m);

         for(int k = 0; k < m; ++k) {
            this.cachedVt.setRowVector(k, this.eigenvectors[k]);
         }
      }

      return this.cachedVt;
   }

   @Override
   public double[] getRealEigenvalues() throws InvalidMatrixException {
      return (double[])this.realEigenvalues.clone();
   }

   @Override
   public double getRealEigenvalue(int i) throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
      return this.realEigenvalues[i];
   }

   @Override
   public double[] getImagEigenvalues() throws InvalidMatrixException {
      return (double[])this.imagEigenvalues.clone();
   }

   @Override
   public double getImagEigenvalue(int i) throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
      return this.imagEigenvalues[i];
   }

   @Override
   public RealVector getEigenvector(int i) throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
      return this.eigenvectors[i].copy();
   }

   @Override
   public double getDeterminant() {
      double determinant = 1.0;

      for(double lambda : this.realEigenvalues) {
         determinant *= lambda;
      }

      return determinant;
   }

   @Override
   public DecompositionSolver getSolver() {
      return new EigenDecompositionImpl.Solver(this.realEigenvalues, this.imagEigenvalues, this.eigenvectors);
   }

   private void transformToTridiagonal(RealMatrix matrix) {
      this.transformer = new TriDiagonalTransformer(matrix);
      this.main = this.transformer.getMainDiagonalRef();
      this.secondary = this.transformer.getSecondaryDiagonalRef();
   }

   private void findEigenVectors(double[][] householderMatrix) {
      double[][] z = (double[][])householderMatrix.clone();
      int n = this.main.length;
      this.realEigenvalues = new double[n];
      this.imagEigenvalues = new double[n];
      double[] e = new double[n];

      for(int i = 0; i < n - 1; ++i) {
         this.realEigenvalues[i] = this.main[i];
         e[i] = this.secondary[i];
      }

      this.realEigenvalues[n - 1] = this.main[n - 1];
      e[n - 1] = 0.0;
      double maxAbsoluteValue = 0.0;

      for(int i = 0; i < n; ++i) {
         if (FastMath.abs(this.realEigenvalues[i]) > maxAbsoluteValue) {
            maxAbsoluteValue = FastMath.abs(this.realEigenvalues[i]);
         }

         if (FastMath.abs(e[i]) > maxAbsoluteValue) {
            maxAbsoluteValue = FastMath.abs(e[i]);
         }
      }

      if (maxAbsoluteValue != 0.0) {
         for(int i = 0; i < n; ++i) {
            if (FastMath.abs(this.realEigenvalues[i]) <= 1.110223E-16F * maxAbsoluteValue) {
               this.realEigenvalues[i] = 0.0;
            }

            if (FastMath.abs(e[i]) <= 1.110223E-16F * maxAbsoluteValue) {
               e[i] = 0.0;
            }
         }
      }

      for(int j = 0; j < n; ++j) {
         int its = 0;

         int m;
         do {
            for(m = j; m < n - 1; ++m) {
               double delta = FastMath.abs(this.realEigenvalues[m]) + FastMath.abs(this.realEigenvalues[m + 1]);
               if (FastMath.abs(e[m]) + delta == delta) {
                  break;
               }
            }

            if (m != j) {
               if (its == this.maxIter) {
                  throw new InvalidMatrixException(new MaxIterationsExceededException(this.maxIter));
               }

               ++its;
               double q = (this.realEigenvalues[j + 1] - this.realEigenvalues[j]) / (2.0 * e[j]);
               double t = FastMath.sqrt(1.0 + q * q);
               if (q < 0.0) {
                  q = this.realEigenvalues[m] - this.realEigenvalues[j] + e[j] / (q - t);
               } else {
                  q = this.realEigenvalues[m] - this.realEigenvalues[j] + e[j] / (q + t);
               }

               double u = 0.0;
               double s = 1.0;
               double c = 1.0;

               int i;
               for(i = m - 1; i >= j; --i) {
                  double p = s * e[i];
                  double h = c * e[i];
                  if (FastMath.abs(p) >= FastMath.abs(q)) {
                     c = q / p;
                     t = FastMath.sqrt(c * c + 1.0);
                     e[i + 1] = p * t;
                     s = 1.0 / t;
                     c *= s;
                  } else {
                     s = p / q;
                     t = FastMath.sqrt(s * s + 1.0);
                     e[i + 1] = q * t;
                     c = 1.0 / t;
                     s *= c;
                  }

                  if (e[i + 1] == 0.0) {
                     this.realEigenvalues[i + 1] -= u;
                     e[m] = 0.0;
                     break;
                  }

                  q = this.realEigenvalues[i + 1] - u;
                  t = (this.realEigenvalues[i] - q) * s + 2.0 * c * h;
                  u = s * t;
                  this.realEigenvalues[i + 1] = q + u;
                  q = c * t - h;

                  for(int ia = 0; ia < n; ++ia) {
                     p = z[ia][i + 1];
                     z[ia][i + 1] = s * z[ia][i] + c * p;
                     z[ia][i] = c * z[ia][i] - s * p;
                  }
               }

               if (t != 0.0 || i < j) {
                  this.realEigenvalues[j] -= u;
                  e[j] = q;
                  e[m] = 0.0;
               }
            }
         } while(m == j);
      }

      for(int i = 0; i < n; ++i) {
         int k = i;
         double p = this.realEigenvalues[i];

         for(int j = i + 1; j < n; ++j) {
            if (this.realEigenvalues[j] > p) {
               k = j;
               p = this.realEigenvalues[j];
            }
         }

         if (k != i) {
            this.realEigenvalues[k] = this.realEigenvalues[i];
            this.realEigenvalues[i] = p;

            for(int j = 0; j < n; ++j) {
               p = z[j][i];
               z[j][i] = z[j][k];
               z[j][k] = p;
            }
         }
      }

      maxAbsoluteValue = 0.0;

      for(int i = 0; i < n; ++i) {
         if (FastMath.abs(this.realEigenvalues[i]) > maxAbsoluteValue) {
            maxAbsoluteValue = FastMath.abs(this.realEigenvalues[i]);
         }
      }

      if (maxAbsoluteValue != 0.0) {
         for(int i = 0; i < n; ++i) {
            if (FastMath.abs(this.realEigenvalues[i]) < 1.110223E-16F * maxAbsoluteValue) {
               this.realEigenvalues[i] = 0.0;
            }
         }
      }

      this.eigenvectors = new ArrayRealVector[n];
      double[] tmp = new double[n];

      for(int i = 0; i < n; ++i) {
         for(int j = 0; j < n; ++j) {
            tmp[j] = z[j][i];
         }

         this.eigenvectors[i] = new ArrayRealVector(tmp);
      }
   }

   private static class Solver implements DecompositionSolver {
      private double[] realEigenvalues;
      private double[] imagEigenvalues;
      private final ArrayRealVector[] eigenvectors;

      private Solver(double[] realEigenvalues, double[] imagEigenvalues, ArrayRealVector[] eigenvectors) {
         this.realEigenvalues = realEigenvalues;
         this.imagEigenvalues = imagEigenvalues;
         this.eigenvectors = eigenvectors;
      }

      @Override
      public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
         if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            int m = this.realEigenvalues.length;
            if (b.length != m) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, m);
            } else {
               double[] bp = new double[m];

               for(int i = 0; i < m; ++i) {
                  ArrayRealVector v = this.eigenvectors[i];
                  double[] vData = v.getDataRef();
                  double s = v.dotProduct(b) / this.realEigenvalues[i];

                  for(int j = 0; j < m; ++j) {
                     bp[j] += s * vData[j];
                  }
               }

               return bp;
            }
         }
      }

      @Override
      public RealVector solve(RealVector b) throws IllegalArgumentException, InvalidMatrixException {
         if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            int m = this.realEigenvalues.length;
            if (b.getDimension() != m) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.getDimension(), m);
            } else {
               double[] bp = new double[m];

               for(int i = 0; i < m; ++i) {
                  ArrayRealVector v = this.eigenvectors[i];
                  double[] vData = v.getDataRef();
                  double s = v.dotProduct(b) / this.realEigenvalues[i];

                  for(int j = 0; j < m; ++j) {
                     bp[j] += s * vData[j];
                  }
               }

               return new ArrayRealVector(bp, false);
            }
         }
      }

      @Override
      public RealMatrix solve(RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
         if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            int m = this.realEigenvalues.length;
            if (b.getRowDimension() != m) {
               throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), m, "n"
               );
            } else {
               int nColB = b.getColumnDimension();
               double[][] bp = new double[m][nColB];

               for(int k = 0; k < nColB; ++k) {
                  for(int i = 0; i < m; ++i) {
                     ArrayRealVector v = this.eigenvectors[i];
                     double[] vData = v.getDataRef();
                     double s = 0.0;

                     for(int j = 0; j < m; ++j) {
                        s += v.getEntry(j) * b.getEntry(j, k);
                     }

                     s /= this.realEigenvalues[i];

                     for(int j = 0; j < m; ++j) {
                        bp[j][k] += s * vData[j];
                     }
                  }
               }

               return MatrixUtils.createRealMatrix(bp);
            }
         }
      }

      @Override
      public boolean isNonSingular() {
         for(int i = 0; i < this.realEigenvalues.length; ++i) {
            if (this.realEigenvalues[i] == 0.0 && this.imagEigenvalues[i] == 0.0) {
               return false;
            }
         }

         return true;
      }

      @Override
      public RealMatrix getInverse() throws InvalidMatrixException {
         if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            int m = this.realEigenvalues.length;
            double[][] invData = new double[m][m];

            for(int i = 0; i < m; ++i) {
               double[] invI = invData[i];

               for(int j = 0; j < m; ++j) {
                  double invIJ = 0.0;

                  for(int k = 0; k < m; ++k) {
                     double[] vK = this.eigenvectors[k].getDataRef();
                     invIJ += vK[i] * vK[j] / this.realEigenvalues[k];
                  }

                  invI[j] = invIJ;
               }
            }

            return MatrixUtils.createRealMatrix(invData);
         }
      }
   }
}
