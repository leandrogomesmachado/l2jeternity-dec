package org.apache.commons.math.linear;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class LUDecompositionImpl implements LUDecomposition {
   private static final double DEFAULT_TOO_SMALL = 1.0E-11;
   private double[][] lu;
   private int[] pivot;
   private boolean even;
   private boolean singular;
   private RealMatrix cachedL;
   private RealMatrix cachedU;
   private RealMatrix cachedP;

   public LUDecompositionImpl(RealMatrix matrix) throws InvalidMatrixException {
      this(matrix, 1.0E-11);
   }

   public LUDecompositionImpl(RealMatrix matrix, double singularityThreshold) throws NonSquareMatrixException {
      if (!matrix.isSquare()) {
         throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
      } else {
         int m = matrix.getColumnDimension();
         this.lu = matrix.getData();
         this.pivot = new int[m];
         this.cachedL = null;
         this.cachedU = null;
         this.cachedP = null;
         int row = 0;

         while(row < m) {
            this.pivot[row] = row++;
         }

         this.even = true;
         this.singular = false;

         for(int col = 0; col < m; ++col) {
            double sum = 0.0;

            for(int rowx = 0; rowx < col; ++rowx) {
               double[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < rowx; ++i) {
                  sum -= luRow[i] * this.lu[i][col];
               }

               luRow[col] = sum;
            }

            int max = col;
            double largest = Double.NEGATIVE_INFINITY;

            for(int rowx = col; rowx < m; ++rowx) {
               double[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < col; ++i) {
                  sum -= luRow[i] * this.lu[i][col];
               }

               luRow[col] = sum;
               if (FastMath.abs(sum) > largest) {
                  largest = FastMath.abs(sum);
                  max = rowx;
               }
            }

            if (FastMath.abs(this.lu[max][col]) < singularityThreshold) {
               this.singular = true;
               return;
            }

            if (max != col) {
               double tmp = 0.0;
               double[] luMax = this.lu[max];
               double[] luCol = this.lu[col];

               for(int i = 0; i < m; ++i) {
                  tmp = luMax[i];
                  luMax[i] = luCol[i];
                  luCol[i] = tmp;
               }

               int temp = this.pivot[max];
               this.pivot[max] = this.pivot[col];
               this.pivot[col] = temp;
               this.even = !this.even;
            }

            double luDiag = this.lu[col][col];

            for(int rowx = col + 1; rowx < m; ++rowx) {
               this.lu[rowx][col] /= luDiag;
            }
         }
      }
   }

   @Override
   public RealMatrix getL() {
      if (this.cachedL == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedL = MatrixUtils.createRealMatrix(m, m);

         for(int i = 0; i < m; ++i) {
            double[] luI = this.lu[i];

            for(int j = 0; j < i; ++j) {
               this.cachedL.setEntry(i, j, luI[j]);
            }

            this.cachedL.setEntry(i, i, 1.0);
         }
      }

      return this.cachedL;
   }

   @Override
   public RealMatrix getU() {
      if (this.cachedU == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedU = MatrixUtils.createRealMatrix(m, m);

         for(int i = 0; i < m; ++i) {
            double[] luI = this.lu[i];

            for(int j = i; j < m; ++j) {
               this.cachedU.setEntry(i, j, luI[j]);
            }
         }
      }

      return this.cachedU;
   }

   @Override
   public RealMatrix getP() {
      if (this.cachedP == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedP = MatrixUtils.createRealMatrix(m, m);

         for(int i = 0; i < m; ++i) {
            this.cachedP.setEntry(i, this.pivot[i], 1.0);
         }
      }

      return this.cachedP;
   }

   @Override
   public int[] getPivot() {
      return (int[])this.pivot.clone();
   }

   @Override
   public double getDeterminant() {
      if (this.singular) {
         return 0.0;
      } else {
         int m = this.pivot.length;
         double determinant = this.even ? 1.0 : -1.0;

         for(int i = 0; i < m; ++i) {
            determinant *= this.lu[i][i];
         }

         return determinant;
      }
   }

   @Override
   public DecompositionSolver getSolver() {
      return new LUDecompositionImpl.Solver(this.lu, this.pivot, this.singular);
   }

   private static class Solver implements DecompositionSolver {
      private final double[][] lu;
      private final int[] pivot;
      private final boolean singular;

      private Solver(double[][] lu, int[] pivot, boolean singular) {
         this.lu = lu;
         this.pivot = pivot;
         this.singular = singular;
      }

      @Override
      public boolean isNonSingular() {
         return !this.singular;
      }

      @Override
      public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.pivot.length;
         if (b.length != m) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, m);
         } else if (this.singular) {
            throw new SingularMatrixException();
         } else {
            double[] bp = new double[m];

            for(int row = 0; row < m; ++row) {
               bp[row] = b[this.pivot[row]];
            }

            for(int col = 0; col < m; ++col) {
               double bpCol = bp[col];

               for(int i = col + 1; i < m; ++i) {
                  bp[i] -= bpCol * this.lu[i][col];
               }
            }

            for(int col = m - 1; col >= 0; --col) {
               bp[col] /= this.lu[col][col];
               double bpCol = bp[col];

               for(int i = 0; i < col; ++i) {
                  bp[i] -= bpCol * this.lu[i][col];
               }
            }

            return bp;
         }
      }

      @Override
      public RealVector solve(RealVector b) throws IllegalArgumentException, InvalidMatrixException {
         try {
            return this.solve((ArrayRealVector)b);
         } catch (ClassCastException var9) {
            int m = this.pivot.length;
            if (b.getDimension() != m) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.getDimension(), m);
            } else if (this.singular) {
               throw new SingularMatrixException();
            } else {
               double[] bp = new double[m];

               for(int row = 0; row < m; ++row) {
                  bp[row] = b.getEntry(this.pivot[row]);
               }

               for(int col = 0; col < m; ++col) {
                  double bpCol = bp[col];

                  for(int i = col + 1; i < m; ++i) {
                     bp[i] -= bpCol * this.lu[i][col];
                  }
               }

               for(int col = m - 1; col >= 0; --col) {
                  bp[col] /= this.lu[col][col];
                  double bpCol = bp[col];

                  for(int i = 0; i < col; ++i) {
                     bp[i] -= bpCol * this.lu[i][col];
                  }
               }

               return new ArrayRealVector(bp, false);
            }
         }
      }

      public ArrayRealVector solve(ArrayRealVector b) throws IllegalArgumentException, InvalidMatrixException {
         return new ArrayRealVector(this.solve(b.getDataRef()), false);
      }

      @Override
      public RealMatrix solve(RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.pivot.length;
         if (b.getRowDimension() != m) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), m, "n"
            );
         } else if (this.singular) {
            throw new SingularMatrixException();
         } else {
            int nColB = b.getColumnDimension();
            double[][] bp = new double[m][nColB];

            for(int row = 0; row < m; ++row) {
               double[] bpRow = bp[row];
               int pRow = this.pivot[row];

               for(int col = 0; col < nColB; ++col) {
                  bpRow[col] = b.getEntry(pRow, col);
               }
            }

            for(int col = 0; col < m; ++col) {
               double[] bpCol = bp[col];

               for(int i = col + 1; i < m; ++i) {
                  double[] bpI = bp[i];
                  double luICol = this.lu[i][col];

                  for(int j = 0; j < nColB; ++j) {
                     bpI[j] -= bpCol[j] * luICol;
                  }
               }
            }

            for(int col = m - 1; col >= 0; --col) {
               double[] bpCol = bp[col];
               double luDiag = this.lu[col][col];

               for(int j = 0; j < nColB; ++j) {
                  bpCol[j] /= luDiag;
               }

               for(int i = 0; i < col; ++i) {
                  double[] bpI = bp[i];
                  double luICol = this.lu[i][col];

                  for(int j = 0; j < nColB; ++j) {
                     bpI[j] -= bpCol[j] * luICol;
                  }
               }
            }

            return new Array2DRowRealMatrix(bp, false);
         }
      }

      @Override
      public RealMatrix getInverse() throws InvalidMatrixException {
         return this.solve(MatrixUtils.createRealIdentityMatrix(this.pivot.length));
      }
   }
}
