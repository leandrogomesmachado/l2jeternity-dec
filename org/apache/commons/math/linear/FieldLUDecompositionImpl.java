package org.apache.commons.math.linear;

import java.lang.reflect.Array;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class FieldLUDecompositionImpl<T extends FieldElement<T>> implements FieldLUDecomposition<T> {
   private final Field<T> field;
   private T[][] lu;
   private int[] pivot;
   private boolean even;
   private boolean singular;
   private FieldMatrix<T> cachedL;
   private FieldMatrix<T> cachedU;
   private FieldMatrix<T> cachedP;

   public FieldLUDecompositionImpl(FieldMatrix<T> matrix) throws NonSquareMatrixException {
      if (!matrix.isSquare()) {
         throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
      } else {
         int m = matrix.getColumnDimension();
         this.field = matrix.getField();
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
            T sum = this.field.getZero();

            for(int rowx = 0; rowx < col; ++rowx) {
               T[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < rowx; ++i) {
                  sum = sum.subtract(luRow[i].multiply(this.lu[i][col]));
               }

               luRow[col] = sum;
            }

            int nonZero = col;

            for(int rowx = col; rowx < m; ++rowx) {
               T[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < col; ++i) {
                  sum = sum.subtract(luRow[i].multiply(this.lu[i][col]));
               }

               luRow[col] = sum;
               if (this.lu[nonZero][col].equals(this.field.getZero())) {
                  ++nonZero;
               }
            }

            if (nonZero >= m) {
               this.singular = true;
               return;
            }

            if (nonZero != col) {
               T tmp = this.field.getZero();

               for(int i = 0; i < m; ++i) {
                  tmp = this.lu[nonZero][i];
                  this.lu[nonZero][i] = this.lu[col][i];
                  this.lu[col][i] = tmp;
               }

               int temp = this.pivot[nonZero];
               this.pivot[nonZero] = this.pivot[col];
               this.pivot[col] = temp;
               this.even = !this.even;
            }

            T luDiag = this.lu[col][col];

            for(int rowx = col + 1; rowx < m; ++rowx) {
               T[] luRow = this.lu[rowx];
               luRow[col] = luRow[col].divide(luDiag);
            }
         }
      }
   }

   @Override
   public FieldMatrix<T> getL() {
      if (this.cachedL == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedL = new Array2DRowFieldMatrix<>(this.field, m, m);

         for(int i = 0; i < m; ++i) {
            T[] luI = this.lu[i];

            for(int j = 0; j < i; ++j) {
               this.cachedL.setEntry(i, j, luI[j]);
            }

            this.cachedL.setEntry(i, i, this.field.getOne());
         }
      }

      return this.cachedL;
   }

   @Override
   public FieldMatrix<T> getU() {
      if (this.cachedU == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedU = new Array2DRowFieldMatrix<>(this.field, m, m);

         for(int i = 0; i < m; ++i) {
            T[] luI = this.lu[i];

            for(int j = i; j < m; ++j) {
               this.cachedU.setEntry(i, j, luI[j]);
            }
         }
      }

      return this.cachedU;
   }

   @Override
   public FieldMatrix<T> getP() {
      if (this.cachedP == null && !this.singular) {
         int m = this.pivot.length;
         this.cachedP = new Array2DRowFieldMatrix<>(this.field, m, m);

         for(int i = 0; i < m; ++i) {
            this.cachedP.setEntry(i, this.pivot[i], this.field.getOne());
         }
      }

      return this.cachedP;
   }

   @Override
   public int[] getPivot() {
      return (int[])this.pivot.clone();
   }

   @Override
   public T getDeterminant() {
      if (this.singular) {
         return this.field.getZero();
      } else {
         int m = this.pivot.length;
         T determinant = this.even ? this.field.getOne() : this.field.getZero().subtract(this.field.getOne());

         for(int i = 0; i < m; ++i) {
            determinant = determinant.multiply(this.lu[i][i]);
         }

         return determinant;
      }
   }

   @Override
   public FieldDecompositionSolver<T> getSolver() {
      return new FieldLUDecompositionImpl.Solver<>(this.field, this.lu, this.pivot, this.singular);
   }

   private static class Solver<T extends FieldElement<T>> implements FieldDecompositionSolver<T> {
      private static final long serialVersionUID = -6353105415121373022L;
      private final Field<T> field;
      private final T[][] lu;
      private final int[] pivot;
      private final boolean singular;

      private Solver(Field<T> field, T[][] lu, int[] pivot, boolean singular) {
         this.field = field;
         this.lu = lu;
         this.pivot = pivot;
         this.singular = singular;
      }

      @Override
      public boolean isNonSingular() {
         return !this.singular;
      }

      @Override
      public T[] solve(T[] b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.pivot.length;
         if (b.length != m) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, m);
         } else if (this.singular) {
            throw new SingularMatrixException();
         } else {
            T[] bp = (FieldElement[])Array.newInstance(this.field.getZero().getClass(), m);

            for(int row = 0; row < m; ++row) {
               bp[row] = b[this.pivot[row]];
            }

            for(int col = 0; col < m; ++col) {
               T bpCol = bp[col];

               for(int i = col + 1; i < m; ++i) {
                  bp[i] = bp[i].subtract(bpCol.multiply(this.lu[i][col]));
               }
            }

            for(int col = m - 1; col >= 0; --col) {
               bp[col] = bp[col].divide(this.lu[col][col]);
               T bpCol = bp[col];

               for(int i = 0; i < col; ++i) {
                  bp[i] = bp[i].subtract(bpCol.multiply(this.lu[i][col]));
               }
            }

            return bp;
         }
      }

      @Override
      public FieldVector<T> solve(FieldVector<T> b) throws IllegalArgumentException, InvalidMatrixException {
         try {
            return this.solve((ArrayFieldVector<T>)b);
         } catch (ClassCastException var8) {
            int m = this.pivot.length;
            if (b.getDimension() != m) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.getDimension(), m);
            } else if (this.singular) {
               throw new SingularMatrixException();
            } else {
               T[] bp = (FieldElement[])Array.newInstance(this.field.getZero().getClass(), m);

               for(int row = 0; row < m; ++row) {
                  bp[row] = b.getEntry(this.pivot[row]);
               }

               for(int col = 0; col < m; ++col) {
                  T bpCol = bp[col];

                  for(int i = col + 1; i < m; ++i) {
                     bp[i] = bp[i].subtract(bpCol.multiply(this.lu[i][col]));
                  }
               }

               for(int col = m - 1; col >= 0; --col) {
                  bp[col] = bp[col].divide(this.lu[col][col]);
                  T bpCol = bp[col];

                  for(int i = 0; i < col; ++i) {
                     bp[i] = bp[i].subtract(bpCol.multiply(this.lu[i][col]));
                  }
               }

               return new ArrayFieldVector<>(bp, false);
            }
         }
      }

      public ArrayFieldVector<T> solve(ArrayFieldVector<T> b) throws IllegalArgumentException, InvalidMatrixException {
         return new ArrayFieldVector<>(this.solve(b.getDataRef()), false);
      }

      @Override
      public FieldMatrix<T> solve(FieldMatrix<T> b) throws IllegalArgumentException, InvalidMatrixException {
         int m = this.pivot.length;
         if (b.getRowDimension() != m) {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), m, "n"
            );
         } else if (this.singular) {
            throw new SingularMatrixException();
         } else {
            int nColB = b.getColumnDimension();
            T[][] bp = (FieldElement[][])Array.newInstance(this.field.getZero().getClass(), m, nColB);

            for(int row = 0; row < m; ++row) {
               T[] bpRow = bp[row];
               int pRow = this.pivot[row];

               for(int col = 0; col < nColB; ++col) {
                  bpRow[col] = b.getEntry(pRow, col);
               }
            }

            for(int col = 0; col < m; ++col) {
               T[] bpCol = bp[col];

               for(int i = col + 1; i < m; ++i) {
                  T[] bpI = bp[i];
                  T luICol = this.lu[i][col];

                  for(int j = 0; j < nColB; ++j) {
                     bpI[j] = bpI[j].subtract(bpCol[j].multiply(luICol));
                  }
               }
            }

            for(int col = m - 1; col >= 0; --col) {
               T[] bpCol = bp[col];
               T luDiag = this.lu[col][col];

               for(int j = 0; j < nColB; ++j) {
                  bpCol[j] = bpCol[j].divide(luDiag);
               }

               for(int i = 0; i < col; ++i) {
                  T[] bpI = bp[i];
                  T luICol = this.lu[i][col];

                  for(int j = 0; j < nColB; ++j) {
                     bpI[j] = bpI[j].subtract(bpCol[j].multiply(luICol));
                  }
               }
            }

            return new Array2DRowFieldMatrix<>(bp, false);
         }
      }

      @Override
      public FieldMatrix<T> getInverse() throws InvalidMatrixException {
         int m = this.pivot.length;
         T one = this.field.getOne();
         FieldMatrix<T> identity = new Array2DRowFieldMatrix<>(this.field, m, m);

         for(int i = 0; i < m; ++i) {
            identity.setEntry(i, i, one);
         }

         return this.solve(identity);
      }
   }
}
