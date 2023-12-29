package org.apache.commons.math.linear;

import java.io.Serializable;
import java.lang.reflect.Array;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.OpenIntToFieldHashMap;

public class SparseFieldVector<T extends FieldElement<T>> implements FieldVector<T>, Serializable {
   private static final long serialVersionUID = 7841233292190413362L;
   private final Field<T> field;
   private final OpenIntToFieldHashMap<T> entries;
   private final int virtualSize;

   public SparseFieldVector(Field<T> field) {
      this(field, 0);
   }

   public SparseFieldVector(Field<T> field, int dimension) {
      this.field = field;
      this.virtualSize = dimension;
      this.entries = new OpenIntToFieldHashMap<>(field);
   }

   protected SparseFieldVector(SparseFieldVector<T> v, int resize) {
      this.field = v.field;
      this.virtualSize = v.getDimension() + resize;
      this.entries = new OpenIntToFieldHashMap<>(v.entries);
   }

   public SparseFieldVector(Field<T> field, int dimension, int expectedSize) {
      this.field = field;
      this.virtualSize = dimension;
      this.entries = new OpenIntToFieldHashMap<>(field, expectedSize);
   }

   public SparseFieldVector(Field<T> field, T[] values) {
      this.field = field;
      this.virtualSize = values.length;
      this.entries = new OpenIntToFieldHashMap<>(field);

      for(int key = 0; key < values.length; ++key) {
         T value = values[key];
         this.entries.put(key, value);
      }
   }

   public SparseFieldVector(SparseFieldVector<T> v) {
      this.field = v.field;
      this.virtualSize = v.getDimension();
      this.entries = new OpenIntToFieldHashMap<>(v.getEntries());
   }

   private OpenIntToFieldHashMap<T> getEntries() {
      return this.entries;
   }

   public FieldVector<T> add(SparseFieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      SparseFieldVector<T> res = (SparseFieldVector)this.copy();
      OpenIntToFieldHashMap<T>.Iterator iter = v.getEntries().iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         T value = iter.value();
         if (this.entries.containsKey(key)) {
            res.setEntry(key, this.entries.get(key).add(value));
         } else {
            res.setEntry(key, value);
         }
      }

      return res;
   }

   @Override
   public FieldVector<T> add(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      SparseFieldVector<T> res = new SparseFieldVector<>(this.field, this.getDimension());

      for(int i = 0; i < v.length; ++i) {
         res.setEntry(i, v[i].add(this.getEntry(i)));
      }

      return res;
   }

   public FieldVector<T> append(SparseFieldVector<T> v) {
      SparseFieldVector<T> res = new SparseFieldVector<>(this, v.getDimension());
      OpenIntToFieldHashMap<T>.Iterator iter = v.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key() + this.virtualSize, (T)iter.value());
      }

      return res;
   }

   @Override
   public FieldVector<T> append(FieldVector<T> v) {
      return v instanceof SparseFieldVector ? this.append((SparseFieldVector<T>)v) : this.append(v.toArray());
   }

   @Override
   public FieldVector<T> append(T d) {
      FieldVector<T> res = new SparseFieldVector<>(this, 1);
      res.setEntry(this.virtualSize, d);
      return res;
   }

   @Override
   public FieldVector<T> append(T[] a) {
      FieldVector<T> res = new SparseFieldVector<>(this, a.length);

      for(int i = 0; i < a.length; ++i) {
         res.setEntry(i + this.virtualSize, a[i]);
      }

      return res;
   }

   @Override
   public FieldVector<T> copy() {
      return new SparseFieldVector<>(this);
   }

   @Override
   public T dotProduct(FieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      T res = this.field.getZero();

      for(OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator(); iter.hasNext(); res = res.add(v.getEntry(iter.key()).multiply((T)iter.value()))) {
         iter.advance();
      }

      return res;
   }

   @Override
   public T dotProduct(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T res = this.field.getZero();

      T value;
      for(OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator(); iter.hasNext(); res = res.add(value.multiply((T)iter.value()))) {
         int idx = iter.key();
         value = this.field.getZero();
         if (idx < v.length) {
            value = v[idx];
         }
      }

      return res;
   }

   @Override
   public FieldVector<T> ebeDivide(FieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      SparseFieldVector<T> res = new SparseFieldVector<>(this);
      OpenIntToFieldHashMap<T>.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), (T)iter.value().divide(v.getEntry(iter.key())));
      }

      return res;
   }

   @Override
   public FieldVector<T> ebeDivide(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      SparseFieldVector<T> res = new SparseFieldVector<>(this);
      OpenIntToFieldHashMap<T>.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), (T)iter.value().divide(v[iter.key()]));
      }

      return res;
   }

   @Override
   public FieldVector<T> ebeMultiply(FieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      SparseFieldVector<T> res = new SparseFieldVector<>(this);
      OpenIntToFieldHashMap<T>.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), (T)iter.value().multiply(v.getEntry(iter.key())));
      }

      return res;
   }

   @Override
   public FieldVector<T> ebeMultiply(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      SparseFieldVector<T> res = new SparseFieldVector<>(this);
      OpenIntToFieldHashMap<T>.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), (T)iter.value().multiply(v[iter.key()]));
      }

      return res;
   }

   @Override
   public T[] getData() {
      T[] res = this.buildArray(this.virtualSize);

      for(OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator(); iter.hasNext(); res[iter.key()] = iter.value()) {
         iter.advance();
      }

      return res;
   }

   @Override
   public int getDimension() {
      return this.virtualSize;
   }

   @Override
   public T getEntry(int index) throws MatrixIndexException {
      this.checkIndex(index);
      return this.entries.get(index);
   }

   @Override
   public Field<T> getField() {
      return this.field;
   }

   @Override
   public FieldVector<T> getSubVector(int index, int n) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + n - 1);
      SparseFieldVector<T> res = new SparseFieldVector<>(this.field, n);
      int end = index + n;
      OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (key >= index && key < end) {
            res.setEntry(key - index, (T)iter.value());
         }
      }

      return res;
   }

   @Override
   public FieldVector<T> mapAdd(T d) {
      return this.copy().mapAddToSelf(d);
   }

   @Override
   public FieldVector<T> mapAddToSelf(T d) {
      for(int i = 0; i < this.virtualSize; ++i) {
         this.setEntry(i, this.getEntry(i).add(d));
      }

      return this;
   }

   @Override
   public FieldVector<T> mapDivide(T d) {
      return this.copy().mapDivideToSelf(d);
   }

   @Override
   public FieldVector<T> mapDivideToSelf(T d) {
      OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.entries.put(iter.key(), (T)iter.value().divide(d));
      }

      return this;
   }

   @Override
   public FieldVector<T> mapInv() {
      return this.copy().mapInvToSelf();
   }

   @Override
   public FieldVector<T> mapInvToSelf() {
      for(int i = 0; i < this.virtualSize; ++i) {
         this.setEntry(i, this.field.getOne().divide(this.getEntry(i)));
      }

      return this;
   }

   @Override
   public FieldVector<T> mapMultiply(T d) {
      return this.copy().mapMultiplyToSelf(d);
   }

   @Override
   public FieldVector<T> mapMultiplyToSelf(T d) {
      OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         this.entries.put(iter.key(), (T)iter.value().multiply(d));
      }

      return this;
   }

   @Override
   public FieldVector<T> mapSubtract(T d) {
      return this.copy().mapSubtractToSelf(d);
   }

   @Override
   public FieldVector<T> mapSubtractToSelf(T d) {
      return this.mapAddToSelf(this.field.getZero().subtract(d));
   }

   public FieldMatrix<T> outerProduct(SparseFieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      SparseFieldMatrix<T> res = new SparseFieldMatrix<>(this.field, this.virtualSize, this.virtualSize);
      OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         OpenIntToFieldHashMap<T>.Iterator iter2 = v.entries.iterator();

         while(iter2.hasNext()) {
            iter2.advance();
            res.setEntry(iter.key(), iter2.key(), (T)iter.value().multiply(iter2.value()));
         }
      }

      return res;
   }

   @Override
   public FieldMatrix<T> outerProduct(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      FieldMatrix<T> res = new SparseFieldMatrix<>(this.field, this.virtualSize, this.virtualSize);
      OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         int row = iter.key();
         FieldElement<T> value = iter.value();

         for(int col = 0; col < this.virtualSize; ++col) {
            res.setEntry(row, col, value.multiply(v[col]));
         }
      }

      return res;
   }

   @Override
   public FieldMatrix<T> outerProduct(FieldVector<T> v) throws IllegalArgumentException {
      return v instanceof SparseFieldVector ? this.outerProduct((SparseFieldVector<T>)v) : this.outerProduct(v.toArray());
   }

   @Override
   public FieldVector<T> projection(FieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v.mapMultiply(this.dotProduct(v).divide(v.dotProduct(v)));
   }

   @Override
   public FieldVector<T> projection(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      return this.projection(new SparseFieldVector<>(this.field, v));
   }

   @Override
   public void set(T value) {
      for(int i = 0; i < this.virtualSize; ++i) {
         this.setEntry(i, value);
      }
   }

   @Override
   public void setEntry(int index, T value) throws MatrixIndexException {
      this.checkIndex(index);
      this.entries.put(index, value);
   }

   @Override
   public void setSubVector(int index, FieldVector<T> v) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + v.getDimension() - 1);
      this.setSubVector(index, v.getData());
   }

   @Override
   public void setSubVector(int index, T[] v) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + v.length - 1);

      for(int i = 0; i < v.length; ++i) {
         this.setEntry(i + index, v[i]);
      }
   }

   public SparseFieldVector<T> subtract(SparseFieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      SparseFieldVector<T> res = (SparseFieldVector)this.copy();
      OpenIntToFieldHashMap<T>.Iterator iter = v.getEntries().iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (this.entries.containsKey(key)) {
            res.setEntry(key, this.entries.get(key).subtract((T)iter.value()));
         } else {
            res.setEntry(key, this.field.getZero().subtract((T)iter.value()));
         }
      }

      return res;
   }

   @Override
   public FieldVector<T> subtract(FieldVector<T> v) throws IllegalArgumentException {
      return (FieldVector<T>)(v instanceof SparseFieldVector ? this.subtract((SparseFieldVector<T>)v) : this.subtract(v.toArray()));
   }

   @Override
   public FieldVector<T> subtract(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      SparseFieldVector<T> res = new SparseFieldVector<>(this);

      for(int i = 0; i < v.length; ++i) {
         if (this.entries.containsKey(i)) {
            res.setEntry(i, this.entries.get(i).subtract(v[i]));
         } else {
            res.setEntry(i, this.field.getZero().subtract(v[i]));
         }
      }

      return res;
   }

   @Override
   public T[] toArray() {
      return this.getData();
   }

   private void checkIndex(int index) throws MatrixIndexException {
      if (index < 0 || index >= this.getDimension()) {
         throw new MatrixIndexException(LocalizedFormats.INDEX_OUT_OF_RANGE, index, 0, this.getDimension() - 1);
      }
   }

   protected void checkVectorDimensions(int n) throws IllegalArgumentException {
      if (this.getDimension() != n) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, this.getDimension(), n);
      }
   }

   @Override
   public FieldVector<T> add(FieldVector<T> v) throws IllegalArgumentException {
      return v instanceof SparseFieldVector ? this.add((SparseFieldVector<T>)v) : this.add(v.toArray());
   }

   private T[] buildArray(int length) {
      return (T[])((FieldElement[])Array.newInstance(this.field.getZero().getClass(), length));
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.field == null ? 0 : this.field.hashCode());
      result = 31 * result + this.virtualSize;

      int temp;
      for(OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator(); iter.hasNext(); result = 31 * result + temp) {
         iter.advance();
         temp = iter.value().hashCode();
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof SparseFieldVector)) {
         return false;
      } else {
         SparseFieldVector<T> other = (SparseFieldVector)obj;
         if (this.field == null) {
            if (other.field != null) {
               return false;
            }
         } else if (!this.field.equals(other.field)) {
            return false;
         }

         if (this.virtualSize != other.virtualSize) {
            return false;
         } else {
            OpenIntToFieldHashMap<T>.Iterator iter = this.entries.iterator();

            while(iter.hasNext()) {
               iter.advance();
               T test = other.getEntry(iter.key());
               if (!test.equals(iter.value())) {
                  return false;
               }
            }

            iter = other.getEntries().iterator();

            while(iter.hasNext()) {
               iter.advance();
               T test = iter.value();
               if (!test.equals(this.getEntry(iter.key()))) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
