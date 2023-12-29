package org.apache.commons.math.linear;

import java.lang.reflect.Array;
import java.util.Arrays;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public abstract class AbstractFieldMatrix<T extends FieldElement<T>> implements FieldMatrix<T> {
   private final Field<T> field;

   protected AbstractFieldMatrix() {
      this.field = null;
   }

   protected AbstractFieldMatrix(Field<T> field) {
      this.field = field;
   }

   protected AbstractFieldMatrix(Field<T> field, int rowDimension, int columnDimension) throws IllegalArgumentException {
      if (rowDimension < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, rowDimension, 1);
      } else if (columnDimension < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, columnDimension, 1);
      } else {
         this.field = field;
      }
   }

   protected static <T extends FieldElement<T>> Field<T> extractField(T[][] d) throws IllegalArgumentException {
      if (d.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
      } else if (d[0].length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
      } else {
         return d[0][0].getField();
      }
   }

   protected static <T extends FieldElement<T>> Field<T> extractField(T[] d) throws IllegalArgumentException {
      if (d.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
      } else {
         return d[0].getField();
      }
   }

   protected static <T extends FieldElement<T>> T[][] buildArray(Field<T> field, int rows, int columns) {
      if (columns < 0) {
         T[] dummyRow = (T[])Array.newInstance(field.getZero().getClass(), 0);
         return (T[][])((FieldElement[][])Array.newInstance(dummyRow.getClass(), rows));
      } else {
         T[][] array = (T[][])Array.newInstance(field.getZero().getClass(), rows, columns);

         for(int i = 0; i < array.length; ++i) {
            Arrays.fill(array[i], field.getZero());
         }

         return array;
      }
   }

   protected static <T extends FieldElement<T>> T[] buildArray(Field<T> field, int length) {
      T[] array = (T[])Array.newInstance(field.getZero().getClass(), length);
      Arrays.fill(array, field.getZero());
      return array;
   }

   @Override
   public Field<T> getField() {
      return this.field;
   }

   @Override
   public abstract FieldMatrix<T> createMatrix(int var1, int var2) throws IllegalArgumentException;

   @Override
   public abstract FieldMatrix<T> copy();

   @Override
   public FieldMatrix<T> add(FieldMatrix<T> m) throws IllegalArgumentException {
      this.checkAdditionCompatible(m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col).add(m.getEntry(row, col)));
         }
      }

      return out;
   }

   @Override
   public FieldMatrix<T> subtract(FieldMatrix<T> m) throws IllegalArgumentException {
      this.checkSubtractionCompatible(m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col).subtract(m.getEntry(row, col)));
         }
      }

      return out;
   }

   @Override
   public FieldMatrix<T> scalarAdd(T d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col).add(d));
         }
      }

      return out;
   }

   @Override
   public FieldMatrix<T> scalarMultiply(T d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col).multiply(d));
         }
      }

      return out;
   }

   @Override
   public FieldMatrix<T> multiply(FieldMatrix<T> m) throws IllegalArgumentException {
      this.checkMultiplicationCompatible(m);
      int nRows = this.getRowDimension();
      int nCols = m.getColumnDimension();
      int nSum = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(nRows, nCols);

      for(int row = 0; row < nRows; ++row) {
         for(int col = 0; col < nCols; ++col) {
            T sum = this.field.getZero();

            for(int i = 0; i < nSum; ++i) {
               sum = sum.add(this.getEntry(row, i).multiply(m.getEntry(i, col)));
            }

            out.setEntry(row, col, sum);
         }
      }

      return out;
   }

   @Override
   public FieldMatrix<T> preMultiply(FieldMatrix<T> m) throws IllegalArgumentException {
      return m.multiply(this);
   }

   @Override
   public T[][] getData() {
      T[][] data = buildArray(this.field, this.getRowDimension(), this.getColumnDimension());

      for(int i = 0; i < data.length; ++i) {
         T[] dataI = data[i];

         for(int j = 0; j < dataI.length; ++j) {
            dataI[j] = this.getEntry(i, j);
         }
      }

      return data;
   }

   @Override
   public FieldMatrix<T> getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      FieldMatrix<T> subMatrix = this.createMatrix(endRow - startRow + 1, endColumn - startColumn + 1);

      for(int i = startRow; i <= endRow; ++i) {
         for(int j = startColumn; j <= endColumn; ++j) {
            subMatrix.setEntry(i - startRow, j - startColumn, this.getEntry(i, j));
         }
      }

      return subMatrix;
   }

   @Override
   public FieldMatrix<T> getSubMatrix(final int[] selectedRows, final int[] selectedColumns) throws MatrixIndexException {
      this.checkSubMatrixIndex(selectedRows, selectedColumns);
      FieldMatrix<T> subMatrix = this.createMatrix(selectedRows.length, selectedColumns.length);
      subMatrix.walkInOptimizedOrder(new DefaultFieldMatrixChangingVisitor<T>(this.field.getZero()) {
         @Override
         public T visit(int row, int column, T value) {
            return (T)AbstractFieldMatrix.this.getEntry(selectedRows[row], selectedColumns[column]);
         }
      });
      return subMatrix;
   }

   @Override
   public void copySubMatrix(int startRow, int endRow, int startColumn, int endColumn, final T[][] destination) throws MatrixIndexException, IllegalArgumentException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      int rowsCount = endRow + 1 - startRow;
      int columnsCount = endColumn + 1 - startColumn;
      if (destination.length >= rowsCount && destination[0].length >= columnsCount) {
         this.walkInOptimizedOrder(new DefaultFieldMatrixPreservingVisitor<T>(this.field.getZero()) {
            private int startRow;
            private int startColumn;

            @Override
            public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
               this.startRow = startRow;
               this.startColumn = startColumn;
            }

            @Override
            public void visit(int row, int column, T value) {
               destination[row - this.startRow][column - this.startColumn] = value;
            }
         }, startRow, endRow, startColumn, endColumn);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.DIMENSIONS_MISMATCH_2x2, destination.length, destination[0].length, rowsCount, columnsCount
         );
      }
   }

   @Override
   public void copySubMatrix(int[] selectedRows, int[] selectedColumns, T[][] destination) throws MatrixIndexException, IllegalArgumentException {
      this.checkSubMatrixIndex(selectedRows, selectedColumns);
      if (destination.length >= selectedRows.length && destination[0].length >= selectedColumns.length) {
         for(int i = 0; i < selectedRows.length; ++i) {
            T[] destinationI = destination[i];

            for(int j = 0; j < selectedColumns.length; ++j) {
               destinationI[j] = this.getEntry(selectedRows[i], selectedColumns[j]);
            }
         }
      } else {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.DIMENSIONS_MISMATCH_2x2, destination.length, destination[0].length, selectedRows.length, selectedColumns.length
         );
      }
   }

   @Override
   public void setSubMatrix(T[][] subMatrix, int row, int column) throws MatrixIndexException {
      int nRows = subMatrix.length;
      if (nRows == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
      } else {
         int nCols = subMatrix[0].length;
         if (nCols == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
         } else {
            for(int r = 1; r < nRows; ++r) {
               if (subMatrix[r].length != nCols) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, subMatrix[r].length);
               }
            }

            this.checkRowIndex(row);
            this.checkColumnIndex(column);
            this.checkRowIndex(nRows + row - 1);
            this.checkColumnIndex(nCols + column - 1);

            for(int i = 0; i < nRows; ++i) {
               for(int j = 0; j < nCols; ++j) {
                  this.setEntry(row + i, column + j, subMatrix[i][j]);
               }
            }
         }
      }
   }

   @Override
   public FieldMatrix<T> getRowMatrix(int row) throws MatrixIndexException {
      this.checkRowIndex(row);
      int nCols = this.getColumnDimension();
      FieldMatrix<T> out = this.createMatrix(1, nCols);

      for(int i = 0; i < nCols; ++i) {
         out.setEntry(0, i, this.getEntry(row, i));
      }

      return out;
   }

   @Override
   public void setRowMatrix(int row, FieldMatrix<T> matrix) throws MatrixIndexException, InvalidMatrixException {
      this.checkRowIndex(row);
      int nCols = this.getColumnDimension();
      if (matrix.getRowDimension() == 1 && matrix.getColumnDimension() == nCols) {
         for(int i = 0; i < nCols; ++i) {
            this.setEntry(row, i, matrix.getEntry(0, i));
         }
      } else {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, matrix.getRowDimension(), matrix.getColumnDimension(), 1, nCols);
      }
   }

   @Override
   public FieldMatrix<T> getColumnMatrix(int column) throws MatrixIndexException {
      this.checkColumnIndex(column);
      int nRows = this.getRowDimension();
      FieldMatrix<T> out = this.createMatrix(nRows, 1);

      for(int i = 0; i < nRows; ++i) {
         out.setEntry(i, 0, this.getEntry(i, column));
      }

      return out;
   }

   @Override
   public void setColumnMatrix(int column, FieldMatrix<T> matrix) throws MatrixIndexException, InvalidMatrixException {
      this.checkColumnIndex(column);
      int nRows = this.getRowDimension();
      if (matrix.getRowDimension() == nRows && matrix.getColumnDimension() == 1) {
         for(int i = 0; i < nRows; ++i) {
            this.setEntry(i, column, matrix.getEntry(i, 0));
         }
      } else {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, matrix.getRowDimension(), matrix.getColumnDimension(), nRows, 1);
      }
   }

   @Override
   public FieldVector<T> getRowVector(int row) throws MatrixIndexException {
      return new ArrayFieldVector<>(this.getRow(row), false);
   }

   @Override
   public void setRowVector(int row, FieldVector<T> vector) throws MatrixIndexException, InvalidMatrixException {
      this.checkRowIndex(row);
      int nCols = this.getColumnDimension();
      if (vector.getDimension() != nCols) {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, 1, vector.getDimension(), 1, nCols);
      } else {
         for(int i = 0; i < nCols; ++i) {
            this.setEntry(row, i, vector.getEntry(i));
         }
      }
   }

   @Override
   public FieldVector<T> getColumnVector(int column) throws MatrixIndexException {
      return new ArrayFieldVector<>(this.getColumn(column), false);
   }

   @Override
   public void setColumnVector(int column, FieldVector<T> vector) throws MatrixIndexException, InvalidMatrixException {
      this.checkColumnIndex(column);
      int nRows = this.getRowDimension();
      if (vector.getDimension() != nRows) {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, vector.getDimension(), 1, nRows, 1);
      } else {
         for(int i = 0; i < nRows; ++i) {
            this.setEntry(i, column, vector.getEntry(i));
         }
      }
   }

   @Override
   public T[] getRow(int row) throws MatrixIndexException {
      this.checkRowIndex(row);
      int nCols = this.getColumnDimension();
      T[] out = buildArray(this.field, nCols);

      for(int i = 0; i < nCols; ++i) {
         out[i] = this.getEntry(row, i);
      }

      return out;
   }

   @Override
   public void setRow(int row, T[] array) throws MatrixIndexException, InvalidMatrixException {
      this.checkRowIndex(row);
      int nCols = this.getColumnDimension();
      if (array.length != nCols) {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, 1, array.length, 1, nCols);
      } else {
         for(int i = 0; i < nCols; ++i) {
            this.setEntry(row, i, array[i]);
         }
      }
   }

   @Override
   public T[] getColumn(int column) throws MatrixIndexException {
      this.checkColumnIndex(column);
      int nRows = this.getRowDimension();
      T[] out = buildArray(this.field, nRows);

      for(int i = 0; i < nRows; ++i) {
         out[i] = this.getEntry(i, column);
      }

      return out;
   }

   @Override
   public void setColumn(int column, T[] array) throws MatrixIndexException, InvalidMatrixException {
      this.checkColumnIndex(column);
      int nRows = this.getRowDimension();
      if (array.length != nRows) {
         throw new InvalidMatrixException(LocalizedFormats.DIMENSIONS_MISMATCH_2x2, array.length, 1, nRows, 1);
      } else {
         for(int i = 0; i < nRows; ++i) {
            this.setEntry(i, column, array[i]);
         }
      }
   }

   @Override
   public abstract T getEntry(int var1, int var2) throws MatrixIndexException;

   @Override
   public abstract void setEntry(int var1, int var2, T var3) throws MatrixIndexException;

   @Override
   public abstract void addToEntry(int var1, int var2, T var3) throws MatrixIndexException;

   @Override
   public abstract void multiplyEntry(int var1, int var2, T var3) throws MatrixIndexException;

   @Override
   public FieldMatrix<T> transpose() {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      final FieldMatrix<T> out = this.createMatrix(nCols, nRows);
      this.walkInOptimizedOrder(new DefaultFieldMatrixPreservingVisitor<T>(this.field.getZero()) {
         @Override
         public void visit(int row, int column, T value) {
            out.setEntry(column, row, value);
         }
      });
      return out;
   }

   @Override
   public boolean isSquare() {
      return this.getColumnDimension() == this.getRowDimension();
   }

   @Override
   public abstract int getRowDimension();

   @Override
   public abstract int getColumnDimension();

   @Override
   public T getTrace() throws NonSquareMatrixException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (nRows != nCols) {
         throw new NonSquareMatrixException(nRows, nCols);
      } else {
         T trace = this.field.getZero();

         for(int i = 0; i < nRows; ++i) {
            trace = trace.add(this.getEntry(i, i));
         }

         return trace;
      }
   }

   @Override
   public T[] operate(T[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nCols) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nCols);
      } else {
         T[] out = buildArray(this.field, nRows);

         for(int row = 0; row < nRows; ++row) {
            T sum = this.field.getZero();

            for(int i = 0; i < nCols; ++i) {
               sum = sum.add(this.getEntry(row, i).multiply(v[i]));
            }

            out[row] = sum;
         }

         return out;
      }
   }

   @Override
   public FieldVector<T> operate(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return new ArrayFieldVector<>(this.operate(((ArrayFieldVector)v).getDataRef()), false);
      } catch (ClassCastException var9) {
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (v.getDimension() != nCols) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.getDimension(), nCols);
         } else {
            T[] out = buildArray(this.field, nRows);

            for(int row = 0; row < nRows; ++row) {
               T sum = this.field.getZero();

               for(int i = 0; i < nCols; ++i) {
                  sum = sum.add(this.getEntry(row, i).multiply(v.getEntry(i)));
               }

               out[row] = sum;
            }

            return new ArrayFieldVector<>(out, false);
         }
      }
   }

   @Override
   public T[] preMultiply(T[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nRows) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nRows);
      } else {
         T[] out = buildArray(this.field, nCols);

         for(int col = 0; col < nCols; ++col) {
            T sum = this.field.getZero();

            for(int i = 0; i < nRows; ++i) {
               sum = sum.add(this.getEntry(i, col).multiply(v[i]));
            }

            out[col] = sum;
         }

         return out;
      }
   }

   @Override
   public FieldVector<T> preMultiply(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return new ArrayFieldVector<>(this.preMultiply(((ArrayFieldVector)v).getDataRef()), false);
      } catch (ClassCastException var9) {
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (v.getDimension() != nRows) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.getDimension(), nRows);
         } else {
            T[] out = buildArray(this.field, nCols);

            for(int col = 0; col < nCols; ++col) {
               T sum = this.field.getZero();

               for(int i = 0; i < nRows; ++i) {
                  sum = sum.add(this.getEntry(i, col).multiply(v.getEntry(i)));
               }

               out[col] = sum;
            }

            return new ArrayFieldVector<>(out);
         }
      }
   }

   @Override
   public T walkInRowOrder(FieldMatrixChangingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int row = 0; row < rows; ++row) {
         for(int column = 0; column < columns; ++column) {
            T oldValue = this.getEntry(row, column);
            T newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixPreservingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int row = 0; row < rows; ++row) {
         for(int column = 0; column < columns; ++column) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixChangingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int row = startRow; row <= endRow; ++row) {
         for(int column = startColumn; column <= endColumn; ++column) {
            T oldValue = this.getEntry(row, column);
            T newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixPreservingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int row = startRow; row <= endRow; ++row) {
         for(int column = startColumn; column <= endColumn; ++column) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixChangingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int column = 0; column < columns; ++column) {
         for(int row = 0; row < rows; ++row) {
            T oldValue = this.getEntry(row, column);
            T newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixPreservingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int column = 0; column < columns; ++column) {
         for(int row = 0; row < rows; ++row) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixChangingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int column = startColumn; column <= endColumn; ++column) {
         for(int row = startRow; row <= endRow; ++row) {
            T oldValue = this.getEntry(row, column);
            T newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixPreservingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int column = startColumn; column <= endColumn; ++column) {
         for(int row = startRow; row <= endRow; ++row) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInOptimizedOrder(FieldMatrixChangingVisitor<T> visitor) throws MatrixVisitorException {
      return this.walkInRowOrder(visitor);
   }

   @Override
   public T walkInOptimizedOrder(FieldMatrixPreservingVisitor<T> visitor) throws MatrixVisitorException {
      return this.walkInRowOrder(visitor);
   }

   @Override
   public T walkInOptimizedOrder(FieldMatrixChangingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      return this.walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
   }

   @Override
   public T walkInOptimizedOrder(FieldMatrixPreservingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      return this.walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
   }

   @Override
   public String toString() {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      StringBuilder res = new StringBuilder();
      String fullClassName = this.getClass().getName();
      String shortClassName = fullClassName.substring(fullClassName.lastIndexOf(46) + 1);
      res.append(shortClassName).append("{");

      for(int i = 0; i < nRows; ++i) {
         if (i > 0) {
            res.append(",");
         }

         res.append("{");

         for(int j = 0; j < nCols; ++j) {
            if (j > 0) {
               res.append(",");
            }

            res.append(this.getEntry(i, j));
         }

         res.append("}");
      }

      res.append("}");
      return res.toString();
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof FieldMatrix)) {
         return false;
      } else {
         FieldMatrix<?> m = (FieldMatrix)object;
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (m.getColumnDimension() == nCols && m.getRowDimension() == nRows) {
            for(int row = 0; row < nRows; ++row) {
               for(int col = 0; col < nCols; ++col) {
                  if (!this.getEntry(row, col).equals(m.getEntry(row, col))) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int ret = 322562;
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      ret = ret * 31 + nRows;
      ret = ret * 31 + nCols;

      for(int row = 0; row < nRows; ++row) {
         for(int col = 0; col < nCols; ++col) {
            ret = ret * 31 + (11 * (row + 1) + 17 * (col + 1)) * this.getEntry(row, col).hashCode();
         }
      }

      return ret;
   }

   protected void checkRowIndex(int row) {
      if (row < 0 || row >= this.getRowDimension()) {
         throw new MatrixIndexException(LocalizedFormats.ROW_INDEX_OUT_OF_RANGE, row, 0, this.getRowDimension() - 1);
      }
   }

   protected void checkColumnIndex(int column) throws MatrixIndexException {
      if (column < 0 || column >= this.getColumnDimension()) {
         throw new MatrixIndexException(LocalizedFormats.COLUMN_INDEX_OUT_OF_RANGE, column, 0, this.getColumnDimension() - 1);
      }
   }

   protected void checkSubMatrixIndex(int startRow, int endRow, int startColumn, int endColumn) {
      this.checkRowIndex(startRow);
      this.checkRowIndex(endRow);
      if (startRow > endRow) {
         throw new MatrixIndexException(LocalizedFormats.INITIAL_ROW_AFTER_FINAL_ROW, startRow, endRow);
      } else {
         this.checkColumnIndex(startColumn);
         this.checkColumnIndex(endColumn);
         if (startColumn > endColumn) {
            throw new MatrixIndexException(LocalizedFormats.INITIAL_COLUMN_AFTER_FINAL_COLUMN, startColumn, endColumn);
         }
      }
   }

   protected void checkSubMatrixIndex(int[] selectedRows, int[] selectedColumns) {
      if (selectedRows.length * selectedColumns.length == 0) {
         if (selectedRows.length == 0) {
            throw new MatrixIndexException(LocalizedFormats.EMPTY_SELECTED_ROW_INDEX_ARRAY);
         } else {
            throw new MatrixIndexException(LocalizedFormats.EMPTY_SELECTED_COLUMN_INDEX_ARRAY);
         }
      } else {
         for(int row : selectedRows) {
            this.checkRowIndex(row);
         }

         for(int column : selectedColumns) {
            this.checkColumnIndex(column);
         }
      }
   }

   protected void checkAdditionCompatible(FieldMatrix<T> m) {
      if (this.getRowDimension() != m.getRowDimension() || this.getColumnDimension() != m.getColumnDimension()) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.NOT_ADDITION_COMPATIBLE_MATRICES, this.getRowDimension(), this.getColumnDimension(), m.getRowDimension(), m.getColumnDimension()
         );
      }
   }

   protected void checkSubtractionCompatible(FieldMatrix<T> m) {
      if (this.getRowDimension() != m.getRowDimension() || this.getColumnDimension() != m.getColumnDimension()) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.NOT_SUBTRACTION_COMPATIBLE_MATRICES,
            this.getRowDimension(),
            this.getColumnDimension(),
            m.getRowDimension(),
            m.getColumnDimension()
         );
      }
   }

   protected void checkMultiplicationCompatible(FieldMatrix<T> m) {
      if (this.getColumnDimension() != m.getRowDimension()) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.NOT_MULTIPLICATION_COMPATIBLE_MATRICES,
            this.getRowDimension(),
            this.getColumnDimension(),
            m.getRowDimension(),
            m.getColumnDimension()
         );
      }
   }
}
