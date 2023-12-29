package org.apache.commons.math.linear;

import java.io.Serializable;
import org.apache.commons.math.util.OpenIntToDoubleHashMap;

public class OpenMapRealMatrix extends AbstractRealMatrix implements SparseRealMatrix, Serializable {
   private static final long serialVersionUID = -5962461716457143437L;
   private final int rows;
   private final int columns;
   private final OpenIntToDoubleHashMap entries;

   public OpenMapRealMatrix(int rowDimension, int columnDimension) {
      super(rowDimension, columnDimension);
      this.rows = rowDimension;
      this.columns = columnDimension;
      this.entries = new OpenIntToDoubleHashMap(0.0);
   }

   public OpenMapRealMatrix(OpenMapRealMatrix matrix) {
      this.rows = matrix.rows;
      this.columns = matrix.columns;
      this.entries = new OpenIntToDoubleHashMap(matrix.entries);
   }

   public OpenMapRealMatrix copy() {
      return new OpenMapRealMatrix(this);
   }

   public OpenMapRealMatrix createMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      return new OpenMapRealMatrix(rowDimension, columnDimension);
   }

   @Override
   public int getColumnDimension() {
      return this.columns;
   }

   public OpenMapRealMatrix add(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.add((OpenMapRealMatrix)m);
      } catch (ClassCastException var3) {
         return (OpenMapRealMatrix)super.add(m);
      }
   }

   public OpenMapRealMatrix add(OpenMapRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkAdditionCompatible(this, m);
      OpenMapRealMatrix out = new OpenMapRealMatrix(this);
      OpenIntToDoubleHashMap.Iterator iterator = m.entries.iterator();

      while(iterator.hasNext()) {
         iterator.advance();
         int row = iterator.key() / this.columns;
         int col = iterator.key() - row * this.columns;
         out.setEntry(row, col, this.getEntry(row, col) + iterator.value());
      }

      return out;
   }

   public OpenMapRealMatrix subtract(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.subtract((OpenMapRealMatrix)m);
      } catch (ClassCastException var3) {
         return (OpenMapRealMatrix)super.subtract(m);
      }
   }

   public OpenMapRealMatrix subtract(OpenMapRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkAdditionCompatible(this, m);
      OpenMapRealMatrix out = new OpenMapRealMatrix(this);
      OpenIntToDoubleHashMap.Iterator iterator = m.entries.iterator();

      while(iterator.hasNext()) {
         iterator.advance();
         int row = iterator.key() / this.columns;
         int col = iterator.key() - row * this.columns;
         out.setEntry(row, col, this.getEntry(row, col) - iterator.value());
      }

      return out;
   }

   @Override
   public RealMatrix multiply(RealMatrix m) throws IllegalArgumentException {
      try {
         return this.multiply((OpenMapRealMatrix)m);
      } catch (ClassCastException var12) {
         MatrixUtils.checkMultiplicationCompatible(this, m);
         int outCols = m.getColumnDimension();
         BlockRealMatrix out = new BlockRealMatrix(this.rows, outCols);
         OpenIntToDoubleHashMap.Iterator iterator = this.entries.iterator();

         while(iterator.hasNext()) {
            iterator.advance();
            double value = iterator.value();
            int key = iterator.key();
            int i = key / this.columns;
            int k = key % this.columns;

            for(int j = 0; j < outCols; ++j) {
               out.addToEntry(i, j, value * m.getEntry(k, j));
            }
         }

         return out;
      }
   }

   public OpenMapRealMatrix multiply(OpenMapRealMatrix m) throws IllegalArgumentException {
      MatrixUtils.checkMultiplicationCompatible(this, m);
      int outCols = m.getColumnDimension();
      OpenMapRealMatrix out = new OpenMapRealMatrix(this.rows, outCols);
      OpenIntToDoubleHashMap.Iterator iterator = this.entries.iterator();

      while(iterator.hasNext()) {
         iterator.advance();
         double value = iterator.value();
         int key = iterator.key();
         int i = key / this.columns;
         int k = key % this.columns;

         for(int j = 0; j < outCols; ++j) {
            int rightKey = m.computeKey(k, j);
            if (m.entries.containsKey(rightKey)) {
               int outKey = out.computeKey(i, j);
               double outValue = out.entries.get(outKey) + value * m.entries.get(rightKey);
               if (outValue == 0.0) {
                  out.entries.remove(outKey);
               } else {
                  out.entries.put(outKey, outValue);
               }
            }
         }
      }

      return out;
   }

   @Override
   public double getEntry(int row, int column) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      MatrixUtils.checkColumnIndex(this, column);
      return this.entries.get(this.computeKey(row, column));
   }

   @Override
   public int getRowDimension() {
      return this.rows;
   }

   @Override
   public void setEntry(int row, int column, double value) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      MatrixUtils.checkColumnIndex(this, column);
      if (value == 0.0) {
         this.entries.remove(this.computeKey(row, column));
      } else {
         this.entries.put(this.computeKey(row, column), value);
      }
   }

   @Override
   public void addToEntry(int row, int column, double increment) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      MatrixUtils.checkColumnIndex(this, column);
      int key = this.computeKey(row, column);
      double value = this.entries.get(key) + increment;
      if (value == 0.0) {
         this.entries.remove(key);
      } else {
         this.entries.put(key, value);
      }
   }

   @Override
   public void multiplyEntry(int row, int column, double factor) throws MatrixIndexException {
      MatrixUtils.checkRowIndex(this, row);
      MatrixUtils.checkColumnIndex(this, column);
      int key = this.computeKey(row, column);
      double value = this.entries.get(key) * factor;
      if (value == 0.0) {
         this.entries.remove(key);
      } else {
         this.entries.put(key, value);
      }
   }

   private int computeKey(int row, int column) {
      return row * this.columns + column;
   }
}
