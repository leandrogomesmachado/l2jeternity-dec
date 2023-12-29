package org.apache.commons.math.linear;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class CholeskyDecompositionImpl implements CholeskyDecomposition {
   public static final double DEFAULT_RELATIVE_SYMMETRY_THRESHOLD = 1.0E-15;
   public static final double DEFAULT_ABSOLUTE_POSITIVITY_THRESHOLD = 1.0E-10;
   private double[][] lTData;
   private RealMatrix cachedL;
   private RealMatrix cachedLT;

   public CholeskyDecompositionImpl(RealMatrix matrix) throws NonSquareMatrixException, NotSymmetricMatrixException, NotPositiveDefiniteMatrixException {
      this(matrix, 1.0E-15, 1.0E-10);
   }

   public CholeskyDecompositionImpl(RealMatrix matrix, double relativeSymmetryThreshold, double absolutePositivityThreshold) throws NonSquareMatrixException, NotSymmetricMatrixException, NotPositiveDefiniteMatrixException {
      if (!matrix.isSquare()) {
         throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
      } else {
         int order = matrix.getRowDimension();
         this.lTData = matrix.getData();
         this.cachedL = null;
         this.cachedLT = null;

         for(int i = 0; i < order; ++i) {
            double[] lI = this.lTData[i];

            for(int j = i + 1; j < order; ++j) {
               double[] lJ = this.lTData[j];
               double lIJ = lI[j];
               double lJI = lJ[i];
               double maxDelta = relativeSymmetryThreshold * FastMath.max(FastMath.abs(lIJ), FastMath.abs(lJI));
               if (FastMath.abs(lIJ - lJI) > maxDelta) {
                  throw new NotSymmetricMatrixException();
               }

               lJ[i] = 0.0;
            }
         }

         for(int i = 0; i < order; ++i) {
            double[] ltI = this.lTData[i];
            if (ltI[i] < absolutePositivityThreshold) {
               throw new NotPositiveDefiniteMatrixException();
            }

            ltI[i] = FastMath.sqrt(ltI[i]);
            double inverse = 1.0 / ltI[i];

            for(int q = order - 1; q > i; --q) {
               ltI[q] *= inverse;
               double[] ltQ = this.lTData[q];

               for(int p = q; p < order; ++p) {
                  ltQ[p] -= ltI[q] * ltI[p];
               }
            }
         }
      }
   }

   @Override
   public RealMatrix getL() {
      if (this.cachedL == null) {
         this.cachedL = this.getLT().transpose();
      }

      return this.cachedL;
   }

   @Override
   public RealMatrix getLT() {
      if (this.cachedLT == null) {
         this.cachedLT = MatrixUtils.createRealMatrix(this.lTData);
      }

      return this.cachedLT;
   }

   @Override
   public double getDeterminant() {
      double determinant = 1.0;

      for(int i = 0; i < this.lTData.length; ++i) {
         double lTii = this.lTData[i][i];
         determinant *= lTii * lTii;
      }

      return determinant;
   }

   @Override
   public DecompositionSolver getSolver() {
      return new CholeskyDecompositionImpl.Solver(this.lTData);
   }

   private static class Solver implements DecompositionSolver {
      private final double[][] lTData;

      private Solver(double[][] lTData) {
         this.lTData = lTData;
      }

      @Override
      public boolean isNonSingular() {
         return true;
      }

      @Override
      public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.lTData.length;
         if (b.length != m) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, m);
         } else {
            double[] x = (double[])b.clone();

            for(int j = 0; j < m; ++j) {
               double[] lJ = this.lTData[j];
               x[j] /= lJ[j];
               double xJ = x[j];

               for(int i = j + 1; i < m; ++i) {
                  x[i] -= xJ * lJ[i];
               }
            }

            for(int j = m - 1; j >= 0; --j) {
               x[j] /= this.lTData[j][j];
               double xJ = x[j];

               for(int i = 0; i < j; ++i) {
                  x[i] -= xJ * this.lTData[i][j];
               }
            }

            return x;
         }
      }

      @Override
      public RealVector solve(RealVector b) throws IllegalArgumentException, InvalidMatrixException {
         try {
            return this.solve((ArrayRealVector)b);
         } catch (ClassCastException var10) {
            int m = this.lTData.length;
            if (b.getDimension() != m) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.getDimension(), m);
            } else {
               double[] x = b.getData();

               for(int j = 0; j < m; ++j) {
                  double[] lJ = this.lTData[j];
                  x[j] /= lJ[j];
                  double xJ = x[j];

                  for(int i = j + 1; i < m; ++i) {
                     x[i] -= xJ * lJ[i];
                  }
               }

               for(int j = m - 1; j >= 0; --j) {
                  x[j] /= this.lTData[j][j];
                  double xJ = x[j];

                  for(int i = 0; i < j; ++i) {
                     x[i] -= xJ * this.lTData[i][j];
                  }
               }

               return new ArrayRealVector(x, false);
            }
         }
      }

      public ArrayRealVector solve(ArrayRealVector b) throws IllegalArgumentException, InvalidMatrixException {
         return new ArrayRealVector(this.solve(b.getDataRef()), false);
      }

      @Override
      public RealMatrix solve(RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.lTData.length;
         if (b.getRowDimension() != m) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), m, "n"
            );
         } else {
            int nColB = b.getColumnDimension();
            double[][] x = b.getData();

            for(int j = 0; j < m; ++j) {
               double[] lJ = this.lTData[j];
               double lJJ = lJ[j];
               double[] xJ = x[j];

               for(int k = 0; k < nColB; ++k) {
                  xJ[k] /= lJJ;
               }

               for(int i = j + 1; i < m; ++i) {
                  double[] xI = x[i];
                  double lJI = lJ[i];

                  for(int k = 0; k < nColB; ++k) {
                     xI[k] -= xJ[k] * lJI;
                  }
               }
            }

            for(int j = m - 1; j >= 0; --j) {
               double lJJ = this.lTData[j][j];
               double[] xJ = x[j];

               for(int k = 0; k < nColB; ++k) {
                  xJ[k] /= lJJ;
               }

               for(int i = 0; i < j; ++i) {
                  double[] xI = x[i];
                  double lIJ = this.lTData[i][j];

                  for(int k = 0; k < nColB; ++k) {
                     xI[k] -= xJ[k] * lIJ;
                  }
               }
            }

            return new Array2DRowRealMatrix(x, false);
         }
      }

      @Override
      public RealMatrix getInverse() throws InvalidMatrixException {
         return this.solve(MatrixUtils.createRealIdentityMatrix(this.lTData.length));
      }
   }
}
