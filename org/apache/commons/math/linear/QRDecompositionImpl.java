package org.apache.commons.math.linear;

import java.util.Arrays;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class QRDecompositionImpl implements QRDecomposition {
   private double[][] qrt;
   private double[] rDiag;
   private RealMatrix cachedQ;
   private RealMatrix cachedQT;
   private RealMatrix cachedR;
   private RealMatrix cachedH;

   public QRDecompositionImpl(RealMatrix matrix) {
      int m = matrix.getRowDimension();
      int n = matrix.getColumnDimension();
      this.qrt = matrix.transpose().getData();
      this.rDiag = new double[FastMath.min(m, n)];
      this.cachedQ = null;
      this.cachedQT = null;
      this.cachedR = null;
      this.cachedH = null;

      for(int minor = 0; minor < FastMath.min(m, n); ++minor) {
         double[] qrtMinor = this.qrt[minor];
         double xNormSqr = 0.0;

         for(int row = minor; row < m; ++row) {
            double c = qrtMinor[row];
            xNormSqr += c * c;
         }

         double a = qrtMinor[minor] > 0.0 ? -FastMath.sqrt(xNormSqr) : FastMath.sqrt(xNormSqr);
         this.rDiag[minor] = a;
         if (a != 0.0) {
            qrtMinor[minor] -= a;

            for(int col = minor + 1; col < n; ++col) {
               double[] qrtCol = this.qrt[col];
               double alpha = 0.0;

               for(int row = minor; row < m; ++row) {
                  alpha -= qrtCol[row] * qrtMinor[row];
               }

               alpha /= a * qrtMinor[minor];

               for(int row = minor; row < m; ++row) {
                  qrtCol[row] -= alpha * qrtMinor[row];
               }
            }
         }
      }
   }

   @Override
   public RealMatrix getR() {
      if (this.cachedR == null) {
         int n = this.qrt.length;
         int m = this.qrt[0].length;
         this.cachedR = MatrixUtils.createRealMatrix(m, n);

         for(int row = FastMath.min(m, n) - 1; row >= 0; --row) {
            this.cachedR.setEntry(row, row, this.rDiag[row]);

            for(int col = row + 1; col < n; ++col) {
               this.cachedR.setEntry(row, col, this.qrt[col][row]);
            }
         }
      }

      return this.cachedR;
   }

   @Override
   public RealMatrix getQ() {
      if (this.cachedQ == null) {
         this.cachedQ = this.getQT().transpose();
      }

      return this.cachedQ;
   }

   @Override
   public RealMatrix getQT() {
      if (this.cachedQT == null) {
         int n = this.qrt.length;
         int m = this.qrt[0].length;
         this.cachedQT = MatrixUtils.createRealMatrix(m, m);

         for(int minor = m - 1; minor >= FastMath.min(m, n); --minor) {
            this.cachedQT.setEntry(minor, minor, 1.0);
         }

         for(int minor = FastMath.min(m, n) - 1; minor >= 0; --minor) {
            double[] qrtMinor = this.qrt[minor];
            this.cachedQT.setEntry(minor, minor, 1.0);
            if (qrtMinor[minor] != 0.0) {
               for(int col = minor; col < m; ++col) {
                  double alpha = 0.0;

                  for(int row = minor; row < m; ++row) {
                     alpha -= this.cachedQT.getEntry(col, row) * qrtMinor[row];
                  }

                  alpha /= this.rDiag[minor] * qrtMinor[minor];

                  for(int row = minor; row < m; ++row) {
                     this.cachedQT.addToEntry(col, row, -alpha * qrtMinor[row]);
                  }
               }
            }
         }
      }

      return this.cachedQT;
   }

   @Override
   public RealMatrix getH() {
      if (this.cachedH == null) {
         int n = this.qrt.length;
         int m = this.qrt[0].length;
         this.cachedH = MatrixUtils.createRealMatrix(m, n);

         for(int i = 0; i < m; ++i) {
            for(int j = 0; j < FastMath.min(i + 1, n); ++j) {
               this.cachedH.setEntry(i, j, this.qrt[j][i] / -this.rDiag[j]);
            }
         }
      }

      return this.cachedH;
   }

   @Override
   public DecompositionSolver getSolver() {
      return new QRDecompositionImpl.Solver(this.qrt, this.rDiag);
   }

   private static class Solver implements DecompositionSolver {
      private final double[][] qrt;
      private final double[] rDiag;

      private Solver(double[][] qrt, double[] rDiag) {
         this.qrt = qrt;
         this.rDiag = rDiag;
      }

      @Override
      public boolean isNonSingular() {
         for(double diag : this.rDiag) {
            if (diag == 0.0) {
               return false;
            }
         }

         return true;
      }

      @Override
      public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
         int n = this.qrt.length;
         int m = this.qrt[0].length;
         if (b.length != m) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, m);
         } else if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            double[] x = new double[n];
            double[] y = (double[])b.clone();

            for(int minor = 0; minor < FastMath.min(m, n); ++minor) {
               double[] qrtMinor = this.qrt[minor];
               double dotProduct = 0.0;

               for(int row = minor; row < m; ++row) {
                  dotProduct += y[row] * qrtMinor[row];
               }

               dotProduct /= this.rDiag[minor] * qrtMinor[minor];

               for(int row = minor; row < m; ++row) {
                  y[row] += dotProduct * qrtMinor[row];
               }
            }

            for(int row = this.rDiag.length - 1; row >= 0; --row) {
               y[row] /= this.rDiag[row];
               double yRow = y[row];
               double[] qrtRow = this.qrt[row];
               x[row] = yRow;

               for(int i = 0; i < row; ++i) {
                  y[i] -= yRow * qrtRow[i];
               }
            }

            return x;
         }
      }

      @Override
      public RealVector solve(RealVector b) throws IllegalArgumentException, InvalidMatrixException {
         try {
            return this.solve((ArrayRealVector)b);
         } catch (ClassCastException var3) {
            return new ArrayRealVector(this.solve(b.getData()), false);
         }
      }

      public ArrayRealVector solve(ArrayRealVector b) throws IllegalArgumentException, InvalidMatrixException {
         return new ArrayRealVector(this.solve(b.getDataRef()), false);
      }

      @Override
      public RealMatrix solve(RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
         int n = this.qrt.length;
         int m = this.qrt[0].length;
         if (b.getRowDimension() != m) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), m, "n"
            );
         } else if (!this.isNonSingular()) {
            throw new SingularMatrixException();
         } else {
            int columns = b.getColumnDimension();
            int blockSize = 52;
            int cBlocks = (columns + 52 - 1) / 52;
            double[][] xBlocks = BlockRealMatrix.createBlocksLayout(n, columns);
            double[][] y = new double[b.getRowDimension()][52];
            double[] alpha = new double[52];

            for(int kBlock = 0; kBlock < cBlocks; ++kBlock) {
               int kStart = kBlock * 52;
               int kEnd = FastMath.min(kStart + 52, columns);
               int kWidth = kEnd - kStart;
               b.copySubMatrix(0, m - 1, kStart, kEnd - 1, y);

               for(int minor = 0; minor < FastMath.min(m, n); ++minor) {
                  double[] qrtMinor = this.qrt[minor];
                  double factor = 1.0 / (this.rDiag[minor] * qrtMinor[minor]);
                  Arrays.fill(alpha, 0, kWidth, 0.0);

                  for(int row = minor; row < m; ++row) {
                     double d = qrtMinor[row];
                     double[] yRow = y[row];

                     for(int k = 0; k < kWidth; ++k) {
                        alpha[k] += d * yRow[k];
                     }
                  }

                  for(int k = 0; k < kWidth; ++k) {
                     alpha[k] *= factor;
                  }

                  for(int row = minor; row < m; ++row) {
                     double d = qrtMinor[row];
                     double[] yRow = y[row];

                     for(int k = 0; k < kWidth; ++k) {
                        yRow[k] += alpha[k] * d;
                     }
                  }
               }

               for(int j = this.rDiag.length - 1; j >= 0; --j) {
                  int jBlock = j / 52;
                  int jStart = jBlock * 52;
                  double factor = 1.0 / this.rDiag[j];
                  double[] yJ = y[j];
                  double[] xBlock = xBlocks[jBlock * cBlocks + kBlock];
                  int index = (j - jStart) * kWidth;

                  for(int k = 0; k < kWidth; ++k) {
                     yJ[k] *= factor;
                     xBlock[index++] = yJ[k];
                  }

                  double[] qrtJ = this.qrt[j];

                  for(int i = 0; i < j; ++i) {
                     double rIJ = qrtJ[i];
                     double[] yI = y[i];

                     for(int k = 0; k < kWidth; ++k) {
                        yI[k] -= yJ[k] * rIJ;
                     }
                  }
               }
            }

            return new BlockRealMatrix(n, columns, xBlocks, false);
         }
      }

      @Override
      public RealMatrix getInverse() throws InvalidMatrixException {
         return this.solve(MatrixUtils.createRealIdentityMatrix(this.rDiag.length));
      }
   }
}
