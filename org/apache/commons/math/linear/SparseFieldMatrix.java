package org.apache.commons.math.linear;

import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.util.OpenIntToFieldHashMap;

public class SparseFieldMatrix<T extends FieldElement<T>> extends AbstractFieldMatrix<T> {
   private static final long serialVersionUID = 9078068119297757342L;
   private final OpenIntToFieldHashMap<T> entries;
   private final int rows;
   private final int columns;

   public SparseFieldMatrix(Field<T> field) {
      super(field);
      this.rows = 0;
      this.columns = 0;
      this.entries = new OpenIntToFieldHashMap<>(field);
   }

   public SparseFieldMatrix(Field<T> field, int rowDimension, int columnDimension) throws IllegalArgumentException {
      super(field, rowDimension, columnDimension);
      this.rows = rowDimension;
      this.columns = columnDimension;
      this.entries = new OpenIntToFieldHashMap<>(field);
   }

   public SparseFieldMatrix(SparseFieldMatrix<T> other) {
      super(other.getField(), other.getRowDimension(), other.getColumnDimension());
      this.rows = other.getRowDimension();
      this.columns = other.getColumnDimension();
      this.entries = new OpenIntToFieldHashMap<>(other.entries);
   }

   public SparseFieldMatrix(FieldMatrix<T> other) {
      super(other.getField(), other.getRowDimension(), other.getColumnDimension());
      this.rows = other.getRowDimension();
      this.columns = other.getColumnDimension();
      this.entries = new OpenIntToFieldHashMap<>(this.getField());

      for(int i = 0; i < this.rows; ++i) {
         for(int j = 0; j < this.columns; ++j) {
            this.setEntry(i, j, other.getEntry(i, j));
         }
      }
   }

   @Override
   public void addToEntry(int row, int column, T increment) throws MatrixIndexException {
      this.checkRowIndex(row);
      this.checkColumnIndex(column);
      int key = this.computeKey(row, column);
      T value = this.entries.get(key).add(increment);
      if (this.getField().getZero().equals(value)) {
         this.entries.remove(key);
      } else {
         this.entries.put(key, value);
      }
   }

   @Override
   public FieldMatrix<T> copy() {
      return new SparseFieldMatrix<>(this);
   }

   @Override
   public FieldMatrix<T> createMatrix(int rowDimension, int columnDimension) throws IllegalArgumentException {
      return new SparseFieldMatrix<>(this.getField(), rowDimension, columnDimension);
   }

   @Override
   public int getColumnDimension() {
      return this.columns;
   }

   @Override
   public T getEntry(int row, int column) throws MatrixIndexException {
      this.checkRowIndex(row);
      this.checkColumnIndex(column);
      return this.entries.get(this.computeKey(row, column));
   }

   @Override
   public int getRowDimension() {
      return this.rows;
   }

   @Override
   public void multiplyEntry(int row, int column, T factor) throws MatrixIndexException {
      this.checkRowIndex(row);
      this.checkColumnIndex(column);
      int key = this.computeKey(row, column);
      T value = this.entries.get(key).multiply(factor);
      if (this.getField().getZero().equals(value)) {
         this.entries.remove(key);
      } else {
         this.entries.put(key, value);
      }
   }

   @Override
   public void setEntry(int row, int column, T value) throws MatrixIndexException {
      this.checkRowIndex(row);
      this.checkColumnIndex(column);
      if (this.getField().getZero().equals(value)) {
         this.entries.remove(this.computeKey(row, column));
      } else {
         this.entries.put(this.computeKey(row, column), value);
      }
   }

   private int computeKey(int row, int column) {
      return row * this.columns + column;
   }
}
