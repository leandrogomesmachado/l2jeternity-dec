package org.apache.commons.math.linear;

import java.io.Serializable;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class Array2DRowFieldMatrix<T extends FieldElement<T>> extends AbstractFieldMatrix<T> implements Serializable {
   private static final long serialVersionUID = 7260756672015356458L;
   protected T[][] data;

   public Array2DRowFieldMatrix(Field<T> field) {
      super(field);
   }

   public Array2DRowFieldMatrix(Field<T> field, int rowDimension, int columnDimension) throws IllegalArgumentException {
      super(field, rowDimension, columnDimension);
      this.data = buildArray(field, rowDimension, columnDimension);
   }

   public Array2DRowFieldMatrix(T[][] d) throws IllegalArgumentException, NullPointerException {
      super(extractField(d));
      this.copyIn(d);
   }

   public Array2DRowFieldMatrix(T[][] d, boolean copyArray) throws IllegalArgumentException, NullPointerException {
      super(extractField(d));
      if (copyArray) {
         this.copyIn(d);
      } else {
         if (d == null) {
            throw new NullPointerException();
         }

         int nRows = d.length;
         if (nRows == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
         }

         int nCols = d[0].length;
         if (nCols == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
         }

         for(int r = 1; r < nRows; ++r) {
            if (d[r].length != nCols) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, d[r].length);
            }
         }

         this.data = d;
      }
   }

   public Array2DRowFieldMatrix(T[] v) {
      super(extractField(v));
      int nRows = v.length;
      this.data = buildArray(this.getField(), nRows, 1);

      for(int row = 0; row < nRows; ++row) {
         this.data[row][0] = v[row];
      }
   }

   @Override
   public FieldMatrix<T> createMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      return new Array2DRowFieldMatrix<>(this.getField(), rowDimension, columnDimension);
   }

   @Override
   public FieldMatrix<T> copy() {
      return new Array2DRowFieldMatrix<>(this.copyOut(), false);
   }

   @Override
   public FieldMatrix<T> add(FieldMatrix<T> m) throws IllegalArgumentException {
      try {
         return this.add((Array2DRowFieldMatrix<T>)m);
      } catch (ClassCastException var3) {
         return super.add(m);
      }
   }

   public Array2DRowFieldMatrix<T> add(Array2DRowFieldMatrix<T> m) throws IllegalArgumentException {
      this.checkAdditionCompatible(m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      T[][] outData = buildArray(this.getField(), rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         T[] dataRow = this.data[row];
         T[] mRow = m.data[row];
         T[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].add(mRow[col]);
         }
      }

      return new Array2DRowFieldMatrix<>(outData, false);
   }

   @Override
   public FieldMatrix<T> subtract(FieldMatrix<T> m) throws IllegalArgumentException {
      try {
         return this.subtract((Array2DRowFieldMatrix<T>)m);
      } catch (ClassCastException var3) {
         return super.subtract(m);
      }
   }

   public Array2DRowFieldMatrix<T> subtract(Array2DRowFieldMatrix<T> m) throws IllegalArgumentException {
      this.checkSubtractionCompatible(m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      T[][] outData = buildArray(this.getField(), rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         T[] dataRow = this.data[row];
         T[] mRow = m.data[row];
         T[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].subtract(mRow[col]);
         }
      }

      return new Array2DRowFieldMatrix<>(outData, false);
   }

   @Override
   public FieldMatrix<T> multiply(FieldMatrix<T> m) throws IllegalArgumentException {
      try {
         return this.multiply((Array2DRowFieldMatrix<T>)m);
      } catch (ClassCastException var3) {
         return super.multiply(m);
      }
   }

   public Array2DRowFieldMatrix<T> multiply(Array2DRowFieldMatrix<T> m) throws IllegalArgumentException {
      this.checkMultiplicationCompatible(m);
      int nRows = this.getRowDimension();
      int nCols = m.getColumnDimension();
      int nSum = this.getColumnDimension();
      T[][] outData = buildArray(this.getField(), nRows, nCols);

      for(int row = 0; row < nRows; ++row) {
         T[] dataRow = this.data[row];
         T[] outDataRow = outData[row];

         for(int col = 0; col < nCols; ++col) {
            T sum = this.getField().getZero();

            for(int i = 0; i < nSum; ++i) {
               sum = sum.add(dataRow[i].multiply(m.data[i][col]));
            }

            outDataRow[col] = sum;
         }
      }

      return new Array2DRowFieldMatrix<>(outData, false);
   }

   @Override
   public T[][] getData() {
      return this.copyOut();
   }

   public T[][] getDataRef() {
      return this.data;
   }

   @Override
   public void setSubMatrix(T[][] subMatrix, int row, int column) throws MatrixIndexException {
      if (this.data == null) {
         if (row > 0) {
            throw MathRuntimeException.createIllegalStateException(LocalizedFormats.FIRST_ROWS_NOT_INITIALIZED_YET, row);
         }

         if (column > 0) {
            throw MathRuntimeException.createIllegalStateException(LocalizedFormats.FIRST_COLUMNS_NOT_INITIALIZED_YET, column);
         }

         int nRows = subMatrix.length;
         if (nRows == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
         }

         int nCols = subMatrix[0].length;
         if (nCols == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
         }

         this.data = buildArray(this.getField(), subMatrix.length, nCols);

         for(int i = 0; i < this.data.length; ++i) {
            if (subMatrix[i].length != nCols) {
               throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, subMatrix[i].length);
            }

            System.arraycopy(subMatrix[i], 0, this.data[i + row], column, nCols);
         }
      } else {
         super.setSubMatrix(subMatrix, row, column);
      }
   }

   @Override
   public T getEntry(int row, int column) throws MatrixIndexException {
      try {
         return this.data[row][column];
      } catch (ArrayIndexOutOfBoundsException var4) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void setEntry(int row, int column, T value) throws MatrixIndexException {
      try {
         this.data[row][column] = value;
      } catch (ArrayIndexOutOfBoundsException var5) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void addToEntry(int row, int column, T increment) throws MatrixIndexException {
      try {
         this.data[row][column] = this.data[row][column].add(increment);
      } catch (ArrayIndexOutOfBoundsException var5) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void multiplyEntry(int row, int column, T factor) throws MatrixIndexException {
      try {
         this.data[row][column] = this.data[row][column].multiply(factor);
      } catch (ArrayIndexOutOfBoundsException var5) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public int getRowDimension() {
      return this.data == null ? 0 : this.data.length;
   }

   @Override
   public int getColumnDimension() {
      return this.data != null && this.data[0] != null ? this.data[0].length : 0;
   }

   @Override
   public T[] operate(T[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nCols) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nCols);
      } else {
         T[] out = buildArray(this.getField(), nRows);

         for(int row = 0; row < nRows; ++row) {
            T[] dataRow = this.data[row];
            T sum = this.getField().getZero();

            for(int i = 0; i < nCols; ++i) {
               sum = sum.add(dataRow[i].multiply(v[i]));
            }

            out[row] = sum;
         }

         return out;
      }
   }

   @Override
   public T[] preMultiply(T[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nRows) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nRows);
      } else {
         T[] out = buildArray(this.getField(), nCols);

         for(int col = 0; col < nCols; ++col) {
            T sum = this.getField().getZero();

            for(int i = 0; i < nRows; ++i) {
               sum = sum.add(this.data[i][col].multiply(v[i]));
            }

            out[col] = sum;
         }

         return out;
      }
   }

   @Override
   public T walkInRowOrder(FieldMatrixChangingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int i = 0; i < rows; ++i) {
         T[] rowI = this.data[i];

         for(int j = 0; j < columns; ++j) {
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixPreservingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int i = 0; i < rows; ++i) {
         T[] rowI = this.data[i];

         for(int j = 0; j < columns; ++j) {
            visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixChangingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int i = startRow; i <= endRow; ++i) {
         T[] rowI = this.data[i];

         for(int j = startColumn; j <= endColumn; ++j) {
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInRowOrder(FieldMatrixPreservingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int i = startRow; i <= endRow; ++i) {
         T[] rowI = this.data[i];

         for(int j = startColumn; j <= endColumn; ++j) {
            visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixChangingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int j = 0; j < columns; ++j) {
         for(int i = 0; i < rows; ++i) {
            T[] rowI = this.data[i];
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixPreservingVisitor<T> visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int j = 0; j < columns; ++j) {
         for(int i = 0; i < rows; ++i) {
            visitor.visit(i, j, this.data[i][j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixChangingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int j = startColumn; j <= endColumn; ++j) {
         for(int i = startRow; i <= endRow; ++i) {
            T[] rowI = this.data[i];
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public T walkInColumnOrder(FieldMatrixPreservingVisitor<T> visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      this.checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int j = startColumn; j <= endColumn; ++j) {
         for(int i = startRow; i <= endRow; ++i) {
            visitor.visit(i, j, this.data[i][j]);
         }
      }

      return visitor.end();
   }

   private T[][] copyOut() {
      int nRows = this.getRowDimension();
      T[][] out = buildArray(this.getField(), nRows, this.getColumnDimension());

      for(int i = 0; i < nRows; ++i) {
         System.arraycopy(this.data[i], 0, out[i], 0, this.data[i].length);
      }

      return out;
   }

   private void copyIn(T[][] in) {
      this.setSubMatrix(in, 0, 0);
   }
}
