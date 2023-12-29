package org.apache.commons.math.linear;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class Array2DRowRealMatrix extends AbstractRealMatrix implements Serializable {
   private static final long serialVersionUID = -1067294169172445528L;
   protected double[][] data;

   public Array2DRowRealMatrix() {
   }

   public Array2DRowRealMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      super(rowDimension, columnDimension);
      this.data = new double[rowDimension][columnDimension];
   }

   public Array2DRowRealMatrix(double[][] d) throws IllegalArgumentException, NullPointerException {
      this.copyIn(d);
   }

   public Array2DRowRealMatrix(double[][] d, boolean copyArray) throws IllegalArgumentException, NullPointerException {
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

   public Array2DRowRealMatrix(double[] v) {
      int nRows = v.length;
      this.data = new double[nRows][1];

      for(int row = 0; row < nRows; ++row) {
         this.data[row][0] = v[row];
      }
   }

   @Override
   public RealMatrix createMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      return new Array2DRowRealMatrix(rowDimension, columnDimension);
   }

   @Override
   public RealMatrix copy() {
      return new Array2DRowRealMatrix(this.copyOut(), false);
   }

   @Override
   public RealMatrix add(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.add((Array2DRowRealMatrix)m);
      } catch (ClassCastException var3) {
         return super.add(m);
      }
   }

   public Array2DRowRealMatrix add(Array2DRowRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkAdditionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      double[][] outData = new double[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         double[] dataRow = this.data[row];
         double[] mRow = m.data[row];
         double[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col] + mRow[col];
         }
      }

      return new Array2DRowRealMatrix(outData, false);
   }

   @Override
   public RealMatrix subtract(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.subtract((Array2DRowRealMatrix)m);
      } catch (ClassCastException var3) {
         return super.subtract(m);
      }
   }

   public Array2DRowRealMatrix subtract(Array2DRowRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkSubtractionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      double[][] outData = new double[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         double[] dataRow = this.data[row];
         double[] mRow = m.data[row];
         double[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col] - mRow[col];
         }
      }

      return new Array2DRowRealMatrix(outData, false);
   }

   @Override
   public RealMatrix multiply(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.multiply((Array2DRowRealMatrix)m);
      } catch (ClassCastException var3) {
         return super.multiply(m);
      }
   }

   public Array2DRowRealMatrix multiply(Array2DRowRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkMultiplicationCompatible(this, m);
      int nRows = this.getRowDimension();
      int nCols = m.getColumnDimension();
      int nSum = this.getColumnDimension();
      double[][] outData = new double[nRows][nCols];

      for(int row = 0; row < nRows; ++row) {
         double[] dataRow = this.data[row];
         double[] outDataRow = outData[row];

         for(int col = 0; col < nCols; ++col) {
            double sum = 0.0;

            for(int i = 0; i < nSum; ++i) {
               sum += dataRow[i] * m.data[i][col];
            }

            outDataRow[col] = sum;
         }
      }

      return new Array2DRowRealMatrix(outData, false);
   }

   @Override
   public double[][] getData() {
      return this.copyOut();
   }

   public double[][] getDataRef() {
      return this.data;
   }

   @Override
   public void setSubMatrix(double[][] subMatrix, int row, int column) throws MatrixIndexException {
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

         this.data = new double[subMatrix.length][nCols];

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
   public double getEntry(int row, int column) throws MatrixIndexException {
      try {
         return this.data[row][column];
      } catch (ArrayIndexOutOfBoundsException var4) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void setEntry(int row, int column, double value) throws MatrixIndexException {
      try {
         this.data[row][column] = value;
      } catch (ArrayIndexOutOfBoundsException var6) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void addToEntry(int row, int column, double increment) throws MatrixIndexException {
      try {
         this.data[row][column] += increment;
      } catch (ArrayIndexOutOfBoundsException var6) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public void multiplyEntry(int row, int column, double factor) throws MatrixIndexException {
      try {
         this.data[row][column] *= factor;
      } catch (ArrayIndexOutOfBoundsException var6) {
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
   public double[] operate(double[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nCols) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nCols);
      } else {
         double[] out = new double[nRows];

         for(int row = 0; row < nRows; ++row) {
            double[] dataRow = this.data[row];
            double sum = 0.0;

            for(int i = 0; i < nCols; ++i) {
               sum += dataRow[i] * v[i];
            }

            out[row] = sum;
         }

         return out;
      }
   }

   @Override
   public double[] preMultiply(double[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (v.length != nRows) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nRows);
      } else {
         double[] out = new double[nCols];

         for(int col = 0; col < nCols; ++col) {
            double sum = 0.0;

            for(int i = 0; i < nRows; ++i) {
               sum += this.data[i][col] * v[i];
            }

            out[col] = sum;
         }

         return out;
      }
   }

   @Override
   public double walkInRowOrder(RealMatrixChangingVisitor visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int i = 0; i < rows; ++i) {
         double[] rowI = this.data[i];

         for(int j = 0; j < columns; ++j) {
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInRowOrder(RealMatrixPreservingVisitor visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int i = 0; i < rows; ++i) {
         double[] rowI = this.data[i];

         for(int j = 0; j < columns; ++j) {
            visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInRowOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int i = startRow; i <= endRow; ++i) {
         double[] rowI = this.data[i];

         for(int j = startColumn; j <= endColumn; ++j) {
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInRowOrder(RealMatrixPreservingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int i = startRow; i <= endRow; ++i) {
         double[] rowI = this.data[i];

         for(int j = startColumn; j <= endColumn; ++j) {
            visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixChangingVisitor visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int j = 0; j < columns; ++j) {
         for(int i = 0; i < rows; ++i) {
            double[] rowI = this.data[i];
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixPreservingVisitor visitor) throws MatrixVisitorException {
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
   public double walkInColumnOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int j = startColumn; j <= endColumn; ++j) {
         for(int i = startRow; i <= endRow; ++i) {
            double[] rowI = this.data[i];
            rowI[j] = visitor.visit(i, j, rowI[j]);
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixPreservingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int j = startColumn; j <= endColumn; ++j) {
         for(int i = startRow; i <= endRow; ++i) {
            visitor.visit(i, j, this.data[i][j]);
         }
      }

      return visitor.end();
   }

   private double[][] copyOut() {
      int nRows = this.getRowDimension();
      double[][] out = new double[nRows][this.getColumnDimension()];

      for(int i = 0; i < nRows; ++i) {
         System.arraycopy(this.data[i], 0, out[i], 0, this.data[i].length);
      }

      return out;
   }

   private void copyIn(double[][] in) {
      this.setSubMatrix(in, 0, 0);
   }
}
