package org.apache.commons.math.linear;

import java.io.Serializable;
import java.math.BigDecimal;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

@Deprecated
public class BigMatrixImpl implements BigMatrix, Serializable {
   static final BigDecimal ZERO = new BigDecimal(0);
   static final BigDecimal ONE = new BigDecimal(1);
   private static final BigDecimal TOO_SMALL = new BigDecimal(1.0E-11);
   private static final long serialVersionUID = -1011428905656140431L;
   protected BigDecimal[][] data = (BigDecimal[][])null;
   protected BigDecimal[][] lu = (BigDecimal[][])null;
   protected int[] permutation = null;
   protected int parity = 1;
   private int roundingMode = 4;
   private int scale = 64;

   public BigMatrixImpl() {
   }

   public BigMatrixImpl(int rowDimension, int columnDimension) {
      if (rowDimension < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, rowDimension, 1);
      } else if (columnDimension < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, columnDimension, 1);
      } else {
         this.data = new BigDecimal[rowDimension][columnDimension];
         this.lu = (BigDecimal[][])null;
      }
   }

   public BigMatrixImpl(BigDecimal[][] d) {
      this.copyIn(d);
      this.lu = (BigDecimal[][])null;
   }

   public BigMatrixImpl(BigDecimal[][] d, boolean copyArray) {
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

      this.lu = (BigDecimal[][])null;
   }

   public BigMatrixImpl(double[][] d) {
      int nRows = d.length;
      if (nRows == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
      } else {
         int nCols = d[0].length;
         if (nCols == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
         } else {
            for(int row = 1; row < nRows; ++row) {
               if (d[row].length != nCols) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, d[row].length);
               }
            }

            this.copyIn(d);
            this.lu = (BigDecimal[][])null;
         }
      }
   }

   public BigMatrixImpl(String[][] d) {
      int nRows = d.length;
      if (nRows == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_ROW);
      } else {
         int nCols = d[0].length;
         if (nCols == 0) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
         } else {
            for(int row = 1; row < nRows; ++row) {
               if (d[row].length != nCols) {
                  throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, d[row].length);
               }
            }

            this.copyIn(d);
            this.lu = (BigDecimal[][])null;
         }
      }
   }

   public BigMatrixImpl(BigDecimal[] v) {
      int nRows = v.length;
      this.data = new BigDecimal[nRows][1];

      for(int row = 0; row < nRows; ++row) {
         this.data[row][0] = v[row];
      }
   }

   @Override
   public BigMatrix copy() {
      return new BigMatrixImpl(this.copyOut(), false);
   }

   @Override
   public BigMatrix add(BigMatrix m) throws IllegalArgumentException {
      try {
         return this.add((BigMatrixImpl)m);
      } catch (ClassCastException var10) {
         MatrixUtils.checkAdditionCompatible(this, m);
         int rowCount = this.getRowDimension();
         int columnCount = this.getColumnDimension();
         BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

         for(int row = 0; row < rowCount; ++row) {
            BigDecimal[] dataRow = this.data[row];
            BigDecimal[] outDataRow = outData[row];

            for(int col = 0; col < columnCount; ++col) {
               outDataRow[col] = dataRow[col].add(m.getEntry(row, col));
            }
         }

         return new BigMatrixImpl(outData, false);
      }
   }

   public BigMatrixImpl add(BigMatrixImpl m) throws IllegalArgumentException {
      MatrixUtils.checkAdditionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         BigDecimal[] dataRow = this.data[row];
         BigDecimal[] mRow = m.data[row];
         BigDecimal[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].add(mRow[col]);
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix subtract(BigMatrix m) throws IllegalArgumentException {
      try {
         return this.subtract((BigMatrixImpl)m);
      } catch (ClassCastException var10) {
         MatrixUtils.checkSubtractionCompatible(this, m);
         int rowCount = this.getRowDimension();
         int columnCount = this.getColumnDimension();
         BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

         for(int row = 0; row < rowCount; ++row) {
            BigDecimal[] dataRow = this.data[row];
            BigDecimal[] outDataRow = outData[row];

            for(int col = 0; col < columnCount; ++col) {
               outDataRow[col] = dataRow[col].subtract(this.getEntry(row, col));
            }
         }

         return new BigMatrixImpl(outData, false);
      }
   }

   public BigMatrixImpl subtract(BigMatrixImpl m) throws IllegalArgumentException {
      MatrixUtils.checkSubtractionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         BigDecimal[] dataRow = this.data[row];
         BigDecimal[] mRow = m.data[row];
         BigDecimal[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].subtract(mRow[col]);
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix scalarAdd(BigDecimal d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         BigDecimal[] dataRow = this.data[row];
         BigDecimal[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].add(d);
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix scalarMultiply(BigDecimal d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[rowCount][columnCount];

      for(int row = 0; row < rowCount; ++row) {
         BigDecimal[] dataRow = this.data[row];
         BigDecimal[] outDataRow = outData[row];

         for(int col = 0; col < columnCount; ++col) {
            outDataRow[col] = dataRow[col].multiply(d);
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix multiply(BigMatrix m) throws IllegalArgumentException {
      try {
         return this.multiply((BigMatrixImpl)m);
      } catch (ClassCastException var13) {
         MatrixUtils.checkMultiplicationCompatible(this, m);
         int nRows = this.getRowDimension();
         int nCols = m.getColumnDimension();
         int nSum = this.getColumnDimension();
         BigDecimal[][] outData = new BigDecimal[nRows][nCols];

         for(int row = 0; row < nRows; ++row) {
            BigDecimal[] dataRow = this.data[row];
            BigDecimal[] outDataRow = outData[row];

            for(int col = 0; col < nCols; ++col) {
               BigDecimal sum = ZERO;

               for(int i = 0; i < nSum; ++i) {
                  sum = sum.add(dataRow[i].multiply(m.getEntry(i, col)));
               }

               outDataRow[col] = sum;
            }
         }

         return new BigMatrixImpl(outData, false);
      }
   }

   public BigMatrixImpl multiply(BigMatrixImpl m) throws IllegalArgumentException {
      MatrixUtils.checkMultiplicationCompatible(this, m);
      int nRows = this.getRowDimension();
      int nCols = m.getColumnDimension();
      int nSum = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[nRows][nCols];

      for(int row = 0; row < nRows; ++row) {
         BigDecimal[] dataRow = this.data[row];
         BigDecimal[] outDataRow = outData[row];

         for(int col = 0; col < nCols; ++col) {
            BigDecimal sum = ZERO;

            for(int i = 0; i < nSum; ++i) {
               sum = sum.add(dataRow[i].multiply(m.data[i][col]));
            }

            outDataRow[col] = sum;
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix preMultiply(BigMatrix m) throws IllegalArgumentException {
      return m.multiply(this);
   }

   @Override
   public BigDecimal[][] getData() {
      return this.copyOut();
   }

   @Override
   public double[][] getDataAsDoubleArray() {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      double[][] d = new double[nRows][nCols];

      for(int i = 0; i < nRows; ++i) {
         for(int j = 0; j < nCols; ++j) {
            d[i][j] = this.data[i][j].doubleValue();
         }
      }

      return d;
   }

   public BigDecimal[][] getDataRef() {
      return this.data;
   }

   @Override
   public int getRoundingMode() {
      return this.roundingMode;
   }

   public void setRoundingMode(int roundingMode) {
      this.roundingMode = roundingMode;
   }

   public int getScale() {
      return this.scale;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   @Override
   public BigDecimal getNorm() {
      BigDecimal maxColSum = ZERO;

      for(int col = 0; col < this.getColumnDimension(); ++col) {
         BigDecimal sum = ZERO;

         for(int row = 0; row < this.getRowDimension(); ++row) {
            sum = sum.add(this.data[row][col].abs());
         }

         maxColSum = maxColSum.max(sum);
      }

      return maxColSum;
   }

   @Override
   public BigMatrix getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, startRow);
      MatrixUtils.checkRowIndex(this, endRow);
      if (startRow > endRow) {
         throw new MatrixIndexException(LocalizedFormats.INITIAL_ROW_AFTER_FINAL_ROW, startRow, endRow);
      } else {
         MatrixUtils.checkColumnIndex(this, startColumn);
         MatrixUtils.checkColumnIndex(this, endColumn);
         if (startColumn > endColumn) {
            throw new MatrixIndexException(LocalizedFormats.INITIAL_COLUMN_AFTER_FINAL_COLUMN, startColumn, endColumn);
         } else {
            BigDecimal[][] subMatrixData = new BigDecimal[endRow - startRow + 1][endColumn - startColumn + 1];

            for(int i = startRow; i <= endRow; ++i) {
               System.arraycopy(this.data[i], startColumn, subMatrixData[i - startRow], 0, endColumn - startColumn + 1);
            }

            return new BigMatrixImpl(subMatrixData, false);
         }
      }
   }

   @Override
   public BigMatrix getSubMatrix(int[] selectedRows, int[] selectedColumns) throws MatrixIndexException {
      if (selectedRows.length * selectedColumns.length == 0) {
         if (selectedRows.length == 0) {
            throw new MatrixIndexException(LocalizedFormats.EMPTY_SELECTED_ROW_INDEX_ARRAY);
         } else {
            throw new MatrixIndexException(LocalizedFormats.EMPTY_SELECTED_COLUMN_INDEX_ARRAY);
         }
      } else {
         BigDecimal[][] subMatrixData = new BigDecimal[selectedRows.length][selectedColumns.length];

         try {
            for(int i = 0; i < selectedRows.length; ++i) {
               BigDecimal[] subI = subMatrixData[i];
               BigDecimal[] dataSelectedI = this.data[selectedRows[i]];

               for(int j = 0; j < selectedColumns.length; ++j) {
                  subI[j] = dataSelectedI[selectedColumns[j]];
               }
            }
         } catch (ArrayIndexOutOfBoundsException var9) {
            for(int row : selectedRows) {
               MatrixUtils.checkRowIndex(this, row);
            }

            for(int column : selectedColumns) {
               MatrixUtils.checkColumnIndex(this, column);
            }
         }

         return new BigMatrixImpl(subMatrixData, false);
      }
   }

   public void setSubMatrix(BigDecimal[][] subMatrix, int row, int column) throws MatrixIndexException {
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

            if (this.data == null) {
               if (row > 0) {
                  throw MathRuntimeException.createIllegalStateException(LocalizedFormats.FIRST_ROWS_NOT_INITIALIZED_YET, row);
               }

               if (column > 0) {
                  throw MathRuntimeException.createIllegalStateException(LocalizedFormats.FIRST_COLUMNS_NOT_INITIALIZED_YET, column);
               }

               this.data = new BigDecimal[nRows][nCols];
               System.arraycopy(subMatrix, 0, this.data, 0, subMatrix.length);
            } else {
               MatrixUtils.checkRowIndex(this, row);
               MatrixUtils.checkColumnIndex(this, column);
               MatrixUtils.checkRowIndex(this, nRows + row - 1);
               MatrixUtils.checkColumnIndex(this, nCols + column - 1);
            }

            for(int i = 0; i < nRows; ++i) {
               System.arraycopy(subMatrix[i], 0, this.data[row + i], column, nCols);
            }

            this.lu = (BigDecimal[][])null;
         }
      }
   }

   @Override
   public BigMatrix getRowMatrix(int row) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      int ncols = this.getColumnDimension();
      BigDecimal[][] out = new BigDecimal[1][ncols];
      System.arraycopy(this.data[row], 0, out[0], 0, ncols);
      return new BigMatrixImpl(out, false);
   }

   @Override
   public BigMatrix getColumnMatrix(int column) throws MatrixIndexException {
      MatrixUtils.checkColumnIndex(this, column);
      int nRows = this.getRowDimension();
      BigDecimal[][] out = new BigDecimal[nRows][1];

      for(int row = 0; row < nRows; ++row) {
         out[row][0] = this.data[row][column];
      }

      return new BigMatrixImpl(out, false);
   }

   @Override
   public BigDecimal[] getRow(int row) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      int ncols = this.getColumnDimension();
      BigDecimal[] out = new BigDecimal[ncols];
      System.arraycopy(this.data[row], 0, out, 0, ncols);
      return out;
   }

   @Override
   public double[] getRowAsDoubleArray(int row) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      int ncols = this.getColumnDimension();
      double[] out = new double[ncols];

      for(int i = 0; i < ncols; ++i) {
         out[i] = this.data[row][i].doubleValue();
      }

      return out;
   }

   @Override
   public BigDecimal[] getColumn(int col) throws MatrixIndexException {
      MatrixUtils.checkColumnIndex(this, col);
      int nRows = this.getRowDimension();
      BigDecimal[] out = new BigDecimal[nRows];

      for(int i = 0; i < nRows; ++i) {
         out[i] = this.data[i][col];
      }

      return out;
   }

   @Override
   public double[] getColumnAsDoubleArray(int col) throws MatrixIndexException {
      MatrixUtils.checkColumnIndex(this, col);
      int nrows = this.getRowDimension();
      double[] out = new double[nrows];

      for(int i = 0; i < nrows; ++i) {
         out[i] = this.data[i][col].doubleValue();
      }

      return out;
   }

   @Override
   public BigDecimal getEntry(int row, int column) throws MatrixIndexException {
      try {
         return this.data[row][column];
      } catch (ArrayIndexOutOfBoundsException var4) {
         throw new MatrixIndexException(LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, this.getRowDimension(), this.getColumnDimension());
      }
   }

   @Override
   public double getEntryAsDouble(int row, int column) throws MatrixIndexException {
      return this.getEntry(row, column).doubleValue();
   }

   @Override
   public BigMatrix transpose() {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      BigDecimal[][] outData = new BigDecimal[nCols][nRows];

      for(int row = 0; row < nRows; ++row) {
         BigDecimal[] dataRow = this.data[row];

         for(int col = 0; col < nCols; ++col) {
            outData[col][row] = dataRow[col];
         }
      }

      return new BigMatrixImpl(outData, false);
   }

   @Override
   public BigMatrix inverse() throws InvalidMatrixException {
      return this.solve(MatrixUtils.createBigIdentityMatrix(this.getRowDimension()));
   }

   @Override
   public BigDecimal getDeterminant() throws InvalidMatrixException {
      if (!this.isSquare()) {
         throw new NonSquareMatrixException(this.getRowDimension(), this.getColumnDimension());
      } else if (this.isSingular()) {
         return ZERO;
      } else {
         BigDecimal det = this.parity == 1 ? ONE : ONE.negate();

         for(int i = 0; i < this.getRowDimension(); ++i) {
            det = det.multiply(this.lu[i][i]);
         }

         return det;
      }
   }

   @Override
   public boolean isSquare() {
      return this.getColumnDimension() == this.getRowDimension();
   }

   public boolean isSingular() {
      if (this.lu == null) {
         try {
            this.luDecompose();
            return false;
         } catch (InvalidMatrixException var2) {
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int getRowDimension() {
      return this.data.length;
   }

   @Override
   public int getColumnDimension() {
      return this.data[0].length;
   }

   @Override
   public BigDecimal getTrace() throws IllegalArgumentException {
      if (!this.isSquare()) {
         throw new NonSquareMatrixException(this.getRowDimension(), this.getColumnDimension());
      } else {
         BigDecimal trace = this.data[0][0];

         for(int i = 1; i < this.getRowDimension(); ++i) {
            trace = trace.add(this.data[i][i]);
         }

         return trace;
      }
   }

   @Override
   public BigDecimal[] operate(BigDecimal[] v) throws IllegalArgumentException {
      if (v.length != this.getColumnDimension()) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, this.getColumnDimension());
      } else {
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         BigDecimal[] out = new BigDecimal[nRows];

         for(int row = 0; row < nRows; ++row) {
            BigDecimal sum = ZERO;

            for(int i = 0; i < nCols; ++i) {
               sum = sum.add(this.data[row][i].multiply(v[i]));
            }

            out[row] = sum;
         }

         return out;
      }
   }

   public BigDecimal[] operate(double[] v) throws IllegalArgumentException {
      BigDecimal[] bd = new BigDecimal[v.length];

      for(int i = 0; i < bd.length; ++i) {
         bd[i] = new BigDecimal(v[i]);
      }

      return this.operate(bd);
   }

   @Override
   public BigDecimal[] preMultiply(BigDecimal[] v) throws IllegalArgumentException {
      int nRows = this.getRowDimension();
      if (v.length != nRows) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nRows);
      } else {
         int nCols = this.getColumnDimension();
         BigDecimal[] out = new BigDecimal[nCols];

         for(int col = 0; col < nCols; ++col) {
            BigDecimal sum = ZERO;

            for(int i = 0; i < nRows; ++i) {
               sum = sum.add(this.data[i][col].multiply(v[i]));
            }

            out[col] = sum;
         }

         return out;
      }
   }

   @Override
   public BigDecimal[] solve(BigDecimal[] b) throws IllegalArgumentException, InvalidMatrixException {
      int nRows = this.getRowDimension();
      if (b.length != nRows) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, b.length, nRows);
      } else {
         BigMatrix bMatrix = new BigMatrixImpl(b);
         BigDecimal[][] solution = ((BigMatrixImpl)this.solve(bMatrix)).getDataRef();
         BigDecimal[] out = new BigDecimal[nRows];

         for(int row = 0; row < nRows; ++row) {
            out[row] = solution[row][0];
         }

         return out;
      }
   }

   public BigDecimal[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
      BigDecimal[] bd = new BigDecimal[b.length];

      for(int i = 0; i < bd.length; ++i) {
         bd[i] = new BigDecimal(b[i]);
      }

      return this.solve(bd);
   }

   @Override
   public BigMatrix solve(BigMatrix b) throws IllegalArgumentException, InvalidMatrixException {
      if (b.getRowDimension() != this.getRowDimension()) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.DIMENSIONS_MISMATCH_2x2, b.getRowDimension(), b.getColumnDimension(), this.getRowDimension(), "n"
         );
      } else if (!this.isSquare()) {
         throw new NonSquareMatrixException(this.getRowDimension(), this.getColumnDimension());
      } else if (this.isSingular()) {
         throw new SingularMatrixException();
      } else {
         int nCol = this.getColumnDimension();
         int nColB = b.getColumnDimension();
         int nRowB = b.getRowDimension();
         BigDecimal[][] bp = new BigDecimal[nRowB][nColB];

         for(int row = 0; row < nRowB; ++row) {
            BigDecimal[] bpRow = bp[row];

            for(int col = 0; col < nColB; ++col) {
               bpRow[col] = b.getEntry(this.permutation[row], col);
            }
         }

         for(int col = 0; col < nCol; ++col) {
            for(int i = col + 1; i < nCol; ++i) {
               BigDecimal[] bpI = bp[i];
               BigDecimal[] luI = this.lu[i];

               for(int j = 0; j < nColB; ++j) {
                  bpI[j] = bpI[j].subtract(bp[col][j].multiply(luI[col]));
               }
            }
         }

         for(int col = nCol - 1; col >= 0; --col) {
            BigDecimal[] bpCol = bp[col];
            BigDecimal luDiag = this.lu[col][col];

            for(int j = 0; j < nColB; ++j) {
               bpCol[j] = bpCol[j].divide(luDiag, this.scale, this.roundingMode);
            }

            for(int i = 0; i < col; ++i) {
               BigDecimal[] bpI = bp[i];
               BigDecimal[] luI = this.lu[i];

               for(int j = 0; j < nColB; ++j) {
                  bpI[j] = bpI[j].subtract(bp[col][j].multiply(luI[col]));
               }
            }
         }

         return new BigMatrixImpl(bp, false);
      }
   }

   public void luDecompose() throws InvalidMatrixException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (nRows != nCols) {
         throw new NonSquareMatrixException(this.getRowDimension(), this.getColumnDimension());
      } else {
         this.lu = this.getData();
         this.permutation = new int[nRows];
         int row = 0;

         while(row < nRows) {
            this.permutation[row] = row++;
         }

         this.parity = 1;

         for(int col = 0; col < nCols; ++col) {
            BigDecimal sum = ZERO;

            for(int rowx = 0; rowx < col; ++rowx) {
               BigDecimal[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < rowx; ++i) {
                  sum = sum.subtract(luRow[i].multiply(this.lu[i][col]));
               }

               luRow[col] = sum;
            }

            int max = col;
            BigDecimal largest = ZERO;

            for(int rowx = col; rowx < nRows; ++rowx) {
               BigDecimal[] luRow = this.lu[rowx];
               sum = luRow[col];

               for(int i = 0; i < col; ++i) {
                  sum = sum.subtract(luRow[i].multiply(this.lu[i][col]));
               }

               luRow[col] = sum;
               if (sum.abs().compareTo(largest) == 1) {
                  largest = sum.abs();
                  max = rowx;
               }
            }

            if (this.lu[max][col].abs().compareTo(TOO_SMALL) <= 0) {
               this.lu = (BigDecimal[][])null;
               throw new SingularMatrixException();
            }

            if (max != col) {
               BigDecimal tmp = ZERO;

               for(int i = 0; i < nCols; ++i) {
                  tmp = this.lu[max][i];
                  this.lu[max][i] = this.lu[col][i];
                  this.lu[col][i] = tmp;
               }

               int temp = this.permutation[max];
               this.permutation[max] = this.permutation[col];
               this.permutation[col] = temp;
               this.parity = -this.parity;
            }

            BigDecimal luDiag = this.lu[col][col];

            for(int rowx = col + 1; rowx < nRows; ++rowx) {
               BigDecimal[] luRow = this.lu[rowx];
               luRow[col] = luRow[col].divide(luDiag, this.scale, this.roundingMode);
            }
         }
      }
   }

   @Override
   public String toString() {
      StringBuilder res = new StringBuilder();
      res.append("BigMatrixImpl{");
      if (this.data != null) {
         for(int i = 0; i < this.data.length; ++i) {
            if (i > 0) {
               res.append(",");
            }

            res.append("{");

            for(int j = 0; j < this.data[0].length; ++j) {
               if (j > 0) {
                  res.append(",");
               }

               res.append(this.data[i][j]);
            }

            res.append("}");
         }
      }

      res.append("}");
      return res.toString();
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof BigMatrixImpl)) {
         return false;
      } else {
         BigMatrix m = (BigMatrix)object;
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (m.getColumnDimension() == nCols && m.getRowDimension() == nRows) {
            for(int row = 0; row < nRows; ++row) {
               BigDecimal[] dataRow = this.data[row];

               for(int col = 0; col < nCols; ++col) {
                  if (!dataRow[col].equals(m.getEntry(row, col))) {
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
      int ret = 7;
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      ret = ret * 31 + nRows;
      ret = ret * 31 + nCols;

      for(int row = 0; row < nRows; ++row) {
         BigDecimal[] dataRow = this.data[row];

         for(int col = 0; col < nCols; ++col) {
            ret = ret * 31 + (11 * (row + 1) + 17 * (col + 1)) * dataRow[col].hashCode();
         }
      }

      return ret;
   }

   protected BigMatrix getLUMatrix() throws InvalidMatrixException {
      if (this.lu == null) {
         this.luDecompose();
      }

      return new BigMatrixImpl(this.lu);
   }

   protected int[] getPermutation() {
      int[] out = new int[this.permutation.length];
      System.arraycopy(this.permutation, 0, out, 0, this.permutation.length);
      return out;
   }

   private BigDecimal[][] copyOut() {
      int nRows = this.getRowDimension();
      BigDecimal[][] out = new BigDecimal[nRows][this.getColumnDimension()];

      for(int i = 0; i < nRows; ++i) {
         System.arraycopy(this.data[i], 0, out[i], 0, this.data[i].length);
      }

      return out;
   }

   private void copyIn(BigDecimal[][] in) {
      this.setSubMatrix(in, 0, 0);
   }

   private void copyIn(double[][] in) {
      int nRows = in.length;
      int nCols = in[0].length;
      this.data = new BigDecimal[nRows][nCols];

      for(int i = 0; i < nRows; ++i) {
         BigDecimal[] dataI = this.data[i];
         double[] inI = in[i];

         for(int j = 0; j < nCols; ++j) {
            dataI[j] = new BigDecimal(inI[j]);
         }
      }

      this.lu = (BigDecimal[][])null;
   }

   private void copyIn(String[][] in) {
      int nRows = in.length;
      int nCols = in[0].length;
      this.data = new BigDecimal[nRows][nCols];

      for(int i = 0; i < nRows; ++i) {
         BigDecimal[] dataI = this.data[i];
         String[] inI = in[i];

         for(int j = 0; j < nCols; ++j) {
            dataI[j] = new BigDecimal(inI[j]);
         }
      }

      this.lu = (BigDecimal[][])null;
   }
}
