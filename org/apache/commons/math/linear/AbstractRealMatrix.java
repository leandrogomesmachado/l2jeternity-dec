package org.apache.commons.math.linear;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public abstract class AbstractRealMatrix implements RealMatrix {
   @Deprecated
   private DecompositionSolver lu;

   protected AbstractRealMatrix() {
      this.lu = null;
   }

   protected AbstractRealMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      if (rowDimension < 1) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, rowDimension, 1);
      } else if (columnDimension <= 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, columnDimension, 1);
      } else {
         this.lu = null;
      }
   }

   @Override
   public abstract RealMatrix createMatrix(int var1, int var2) throws IllegalArgumentException;

   @Override
   public abstract RealMatrix copy();

   @Override
   public RealMatrix add(RealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkAdditionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      RealMatrix out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col) + m.getEntry(row, col));
         }
      }

      return out;
   }

   @Override
   public RealMatrix subtract(RealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkSubtractionCompatible(this, m);
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      RealMatrix out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col) - m.getEntry(row, col));
         }
      }

      return out;
   }

   @Override
   public RealMatrix scalarAdd(double d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      RealMatrix out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col) + d);
         }
      }

      return out;
   }

   @Override
   public RealMatrix scalarMultiply(double d) {
      int rowCount = this.getRowDimension();
      int columnCount = this.getColumnDimension();
      RealMatrix out = this.createMatrix(rowCount, columnCount);

      for(int row = 0; row < rowCount; ++row) {
         for(int col = 0; col < columnCount; ++col) {
            out.setEntry(row, col, this.getEntry(row, col) * d);
         }
      }

      return out;
   }

   @Override
   public RealMatrix multiply(RealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkMultiplicationCompatible(this, m);
      int nRows = this.getRowDimension();
      int nCols = m.getColumnDimension();
      int nSum = this.getColumnDimension();
      RealMatrix out = this.createMatrix(nRows, nCols);

      for(int row = 0; row < nRows; ++row) {
         for(int col = 0; col < nCols; ++col) {
            double sum = 0.0;

            for(int i = 0; i < nSum; ++i) {
               sum += this.getEntry(row, i) * m.getEntry(i, col);
            }

            out.setEntry(row, col, sum);
         }
      }

      return out;
   }

   @Override
   public RealMatrix preMultiply(RealMatrix m) throws IllegalArgumentException {
      return m.multiply(this);
   }

   @Override
   public double[][] getData() {
      double[][] data = new double[this.getRowDimension()][this.getColumnDimension()];

      for(int i = 0; i < data.length; ++i) {
         double[] dataI = data[i];

         for(int j = 0; j < dataI.length; ++j) {
            dataI[j] = this.getEntry(i, j);
         }
      }

      return data;
   }

   @Override
   public double getNorm() {
      return this.walkInColumnOrder(new RealMatrixPreservingVisitor() {
         private double endRow;
         private double columnSum;
         private double maxColSum;

         @Override
         public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
            this.endRow = (double)endRow;
            this.columnSum = 0.0;
            this.maxColSum = 0.0;
         }

         @Override
         public void visit(int row, int column, double value) {
            this.columnSum += FastMath.abs(value);
            if ((double)row == this.endRow) {
               this.maxColSum = FastMath.max(this.maxColSum, this.columnSum);
               this.columnSum = 0.0;
            }
         }

         @Override
         public double end() {
            return this.maxColSum;
         }
      });
   }

   @Override
   public double getFrobeniusNorm() {
      return this.walkInOptimizedOrder(new RealMatrixPreservingVisitor() {
         private double sum;

         @Override
         public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
            this.sum = 0.0;
         }

         @Override
         public void visit(int row, int column, double value) {
            this.sum += value * value;
         }

         @Override
         public double end() {
            return FastMath.sqrt(this.sum);
         }
      });
   }

   @Override
   public RealMatrix getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      RealMatrix subMatrix = this.createMatrix(endRow - startRow + 1, endColumn - startColumn + 1);

      for(int i = startRow; i <= endRow; ++i) {
         for(int j = startColumn; j <= endColumn; ++j) {
            subMatrix.setEntry(i - startRow, j - startColumn, this.getEntry(i, j));
         }
      }

      return subMatrix;
   }

   @Override
   public RealMatrix getSubMatrix(final int[] selectedRows, final int[] selectedColumns) throws MatrixIndexException {
      MatrixUtils.checkSubMatrixIndex(this, selectedRows, selectedColumns);
      RealMatrix subMatrix = this.createMatrix(selectedRows.length, selectedColumns.length);
      subMatrix.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
         @Override
         public double visit(int row, int column, double value) {
            return AbstractRealMatrix.this.getEntry(selectedRows[row], selectedColumns[column]);
         }
      });
      return subMatrix;
   }

   @Override
   public void copySubMatrix(int startRow, int endRow, int startColumn, int endColumn, final double[][] destination) throws MatrixIndexException, IllegalArgumentException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      int rowsCount = endRow + 1 - startRow;
      int columnsCount = endColumn + 1 - startColumn;
      if (destination.length >= rowsCount && destination[0].length >= columnsCount) {
         this.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
            private int startRow;
            private int startColumn;

            @Override
            public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
               this.startRow = startRow;
               this.startColumn = startColumn;
            }

            @Override
            public void visit(int row, int column, double value) {
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
   public void copySubMatrix(int[] selectedRows, int[] selectedColumns, double[][] destination) throws MatrixIndexException, IllegalArgumentException {
      MatrixUtils.checkSubMatrixIndex(this, selectedRows, selectedColumns);
      if (destination.length >= selectedRows.length && destination[0].length >= selectedColumns.length) {
         for(int i = 0; i < selectedRows.length; ++i) {
            double[] destinationI = destination[i];

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
   public void setSubMatrix(double[][] subMatrix, int row, int column) throws MatrixIndexException {
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

            MatrixUtils.checkRowIndex(this, row);
            MatrixUtils.checkColumnIndex(this, column);
            MatrixUtils.checkRowIndex(this, nRows + row - 1);
            MatrixUtils.checkColumnIndex(this, nCols + column - 1);

            for(int i = 0; i < nRows; ++i) {
               for(int j = 0; j < nCols; ++j) {
                  this.setEntry(row + i, column + j, subMatrix[i][j]);
               }
            }

            this.lu = null;
         }
      }
   }

   @Override
   public RealMatrix getRowMatrix(int row) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      int nCols = this.getColumnDimension();
      RealMatrix out = this.createMatrix(1, nCols);

      for(int i = 0; i < nCols; ++i) {
         out.setEntry(0, i, this.getEntry(row, i));
      }

      return out;
   }

   @Override
   public void setRowMatrix(int row, RealMatrix matrix) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkRowIndex(this, row);
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
   public RealMatrix getColumnMatrix(int column) throws MatrixIndexException {
      MatrixUtils.checkColumnIndex(this, column);
      int nRows = this.getRowDimension();
      RealMatrix out = this.createMatrix(nRows, 1);

      for(int i = 0; i < nRows; ++i) {
         out.setEntry(i, 0, this.getEntry(i, column));
      }

      return out;
   }

   @Override
   public void setColumnMatrix(int column, RealMatrix matrix) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkColumnIndex(this, column);
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
   public RealVector getRowVector(int row) throws MatrixIndexException {
      return new ArrayRealVector(this.getRow(row), false);
   }

   @Override
   public void setRowVector(int row, RealVector vector) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkRowIndex(this, row);
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
   public RealVector getColumnVector(int column) throws MatrixIndexException {
      return new ArrayRealVector(this.getColumn(column), false);
   }

   @Override
   public void setColumnVector(int column, RealVector vector) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkColumnIndex(this, column);
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
   public double[] getRow(int row) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      int nCols = this.getColumnDimension();
      double[] out = new double[nCols];

      for(int i = 0; i < nCols; ++i) {
         out[i] = this.getEntry(row, i);
      }

      return out;
   }

   @Override
   public void setRow(int row, double[] array) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkRowIndex(this, row);
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
   public double[] getColumn(int column) throws MatrixIndexException {
      MatrixUtils.checkColumnIndex(this, column);
      int nRows = this.getRowDimension();
      double[] out = new double[nRows];

      for(int i = 0; i < nRows; ++i) {
         out[i] = this.getEntry(i, column);
      }

      return out;
   }

   @Override
   public void setColumn(int column, double[] array) throws MatrixIndexException, InvalidMatrixException {
      MatrixUtils.checkColumnIndex(this, column);
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
   public abstract double getEntry(int var1, int var2) throws MatrixIndexException;

   @Override
   public abstract void setEntry(int var1, int var2, double var3) throws MatrixIndexException;

   @Override
   public abstract void addToEntry(int var1, int var2, double var3) throws MatrixIndexException;

   @Override
   public abstract void multiplyEntry(int var1, int var2, double var3) throws MatrixIndexException;

   @Override
   public RealMatrix transpose() {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      final RealMatrix out = this.createMatrix(nCols, nRows);
      this.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
         @Override
         public void visit(int row, int column, double value) {
            out.setEntry(column, row, value);
         }
      });
      return out;
   }

   @Deprecated
   @Override
   public RealMatrix inverse() throws InvalidMatrixException {
      if (this.lu == null) {
         this.lu = new LUDecompositionImpl(this, Double.MIN_NORMAL).getSolver();
      }

      return this.lu.getInverse();
   }

   @Deprecated
   @Override
   public double getDeterminant() throws InvalidMatrixException {
      return new LUDecompositionImpl(this, Double.MIN_NORMAL).getDeterminant();
   }

   @Override
   public boolean isSquare() {
      return this.getColumnDimension() == this.getRowDimension();
   }

   @Deprecated
   @Override
   public boolean isSingular() {
      if (this.lu == null) {
         this.lu = new LUDecompositionImpl(this, Double.MIN_NORMAL).getSolver();
      }

      return !this.lu.isNonSingular();
   }

   @Override
   public abstract int getRowDimension();

   @Override
   public abstract int getColumnDimension();

   @Override
   public double getTrace() throws NonSquareMatrixException {
      int nRows = this.getRowDimension();
      int nCols = this.getColumnDimension();
      if (nRows != nCols) {
         throw new NonSquareMatrixException(nRows, nCols);
      } else {
         double trace = 0.0;

         for(int i = 0; i < nRows; ++i) {
            trace += this.getEntry(i, i);
         }

         return trace;
      }
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
            double sum = 0.0;

            for(int i = 0; i < nCols; ++i) {
               sum += this.getEntry(row, i) * v[i];
            }

            out[row] = sum;
         }

         return out;
      }
   }

   @Override
   public RealVector operate(RealVector v) throws IllegalArgumentException {
      try {
         return new ArrayRealVector(this.operate(((ArrayRealVector)v).getDataRef()), false);
      } catch (ClassCastException var10) {
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (v.getDimension() != nCols) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.getDimension(), nCols);
         } else {
            double[] out = new double[nRows];

            for(int row = 0; row < nRows; ++row) {
               double sum = 0.0;

               for(int i = 0; i < nCols; ++i) {
                  sum += this.getEntry(row, i) * v.getEntry(i);
               }

               out[row] = sum;
            }

            return new ArrayRealVector(out, false);
         }
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
               sum += this.getEntry(i, col) * v[i];
            }

            out[col] = sum;
         }

         return out;
      }
   }

   @Override
   public RealVector preMultiply(RealVector v) throws IllegalArgumentException {
      try {
         return new ArrayRealVector(this.preMultiply(((ArrayRealVector)v).getDataRef()), false);
      } catch (ClassCastException var10) {
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (v.getDimension() != nRows) {
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.getDimension(), nRows);
         } else {
            double[] out = new double[nCols];

            for(int col = 0; col < nCols; ++col) {
               double sum = 0.0;

               for(int i = 0; i < nRows; ++i) {
                  sum += this.getEntry(i, col) * v.getEntry(i);
               }

               out[col] = sum;
            }

            return new ArrayRealVector(out);
         }
      }
   }

   @Override
   public double walkInRowOrder(RealMatrixChangingVisitor visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int row = 0; row < rows; ++row) {
         for(int column = 0; column < columns; ++column) {
            double oldValue = this.getEntry(row, column);
            double newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      this.lu = null;
      return visitor.end();
   }

   @Override
   public double walkInRowOrder(RealMatrixPreservingVisitor visitor) throws MatrixVisitorException {
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
   public double walkInRowOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int row = startRow; row <= endRow; ++row) {
         for(int column = startColumn; column <= endColumn; ++column) {
            double oldValue = this.getEntry(row, column);
            double newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      this.lu = null;
      return visitor.end();
   }

   @Override
   public double walkInRowOrder(RealMatrixPreservingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int row = startRow; row <= endRow; ++row) {
         for(int column = startColumn; column <= endColumn; ++column) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixChangingVisitor visitor) throws MatrixVisitorException {
      int rows = this.getRowDimension();
      int columns = this.getColumnDimension();
      visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);

      for(int column = 0; column < columns; ++column) {
         for(int row = 0; row < rows; ++row) {
            double oldValue = this.getEntry(row, column);
            double newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      this.lu = null;
      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixPreservingVisitor visitor) throws MatrixVisitorException {
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
   public double walkInColumnOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int column = startColumn; column <= endColumn; ++column) {
         for(int row = startRow; row <= endRow; ++row) {
            double oldValue = this.getEntry(row, column);
            double newValue = visitor.visit(row, column, oldValue);
            this.setEntry(row, column, newValue);
         }
      }

      this.lu = null;
      return visitor.end();
   }

   @Override
   public double walkInColumnOrder(RealMatrixPreservingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
      visitor.start(this.getRowDimension(), this.getColumnDimension(), startRow, endRow, startColumn, endColumn);

      for(int column = startColumn; column <= endColumn; ++column) {
         for(int row = startRow; row <= endRow; ++row) {
            visitor.visit(row, column, this.getEntry(row, column));
         }
      }

      return visitor.end();
   }

   @Override
   public double walkInOptimizedOrder(RealMatrixChangingVisitor visitor) throws MatrixVisitorException {
      return this.walkInRowOrder(visitor);
   }

   @Override
   public double walkInOptimizedOrder(RealMatrixPreservingVisitor visitor) throws MatrixVisitorException {
      return this.walkInRowOrder(visitor);
   }

   @Override
   public double walkInOptimizedOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      return this.walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
   }

   @Override
   public double walkInOptimizedOrder(RealMatrixPreservingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) throws MatrixIndexException, MatrixVisitorException {
      return this.walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
   }

   @Deprecated
   @Override
   public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
      if (this.lu == null) {
         this.lu = new LUDecompositionImpl(this, Double.MIN_NORMAL).getSolver();
      }

      return this.lu.solve(b);
   }

   @Deprecated
   @Override
   public RealMatrix solve(RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
      if (this.lu == null) {
         this.lu = new LUDecompositionImpl(this, Double.MIN_NORMAL).getSolver();
      }

      return this.lu.solve(b);
   }

   @Deprecated
   public void luDecompose() throws InvalidMatrixException {
      if (this.lu == null) {
         this.lu = new LUDecompositionImpl(this, Double.MIN_NORMAL).getSolver();
      }
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
      } else if (!(object instanceof RealMatrix)) {
         return false;
      } else {
         RealMatrix m = (RealMatrix)object;
         int nRows = this.getRowDimension();
         int nCols = this.getColumnDimension();
         if (m.getColumnDimension() == nCols && m.getRowDimension() == nRows) {
            for(int row = 0; row < nRows; ++row) {
               for(int col = 0; col < nCols; ++col) {
                  if (this.getEntry(row, col) != m.getEntry(row, col)) {
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
         for(int col = 0; col < nCols; ++col) {
            ret = ret * 31 + (11 * (row + 1) + 17 * (col + 1)) * MathUtils.hash(this.getEntry(row, col));
         }
      }

      return ret;
   }
}
