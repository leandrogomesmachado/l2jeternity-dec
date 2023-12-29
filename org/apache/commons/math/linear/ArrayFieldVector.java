package org.apache.commons.math.linear;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class ArrayFieldVector<T extends FieldElement<T>> implements FieldVector<T>, Serializable {
   private static final long serialVersionUID = 7648186910365927050L;
   protected T[] data;
   private final Field<T> field;

   public ArrayFieldVector(Field<T> field) {
      this(field, 0);
   }

   public ArrayFieldVector(Field<T> field, int size) {
      this.field = field;
      this.data = this.buildArray(size);
      Arrays.fill(this.data, field.getZero());
   }

   public ArrayFieldVector(int size, T preset) {
      this(preset.getField(), size);
      Arrays.fill(this.data, preset);
   }

   public ArrayFieldVector(T[] d) throws IllegalArgumentException {
      try {
         this.field = d[0].getField();
         this.data = (FieldElement[])d.clone();
      } catch (ArrayIndexOutOfBoundsException var3) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_MUST_HAVE_AT_LEAST_ONE_ELEMENT);
      }
   }

   public ArrayFieldVector(Field<T> field, T[] d) {
      this.field = field;
      this.data = (FieldElement[])d.clone();
   }

   public ArrayFieldVector(T[] d, boolean copyArray) throws NullPointerException, IllegalArgumentException {
      if (d.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_MUST_HAVE_AT_LEAST_ONE_ELEMENT);
      } else {
         this.field = d[0].getField();
         this.data = copyArray ? (FieldElement[])d.clone() : d;
      }
   }

   public ArrayFieldVector(Field<T> field, T[] d, boolean copyArray) {
      this.field = field;
      this.data = copyArray ? (FieldElement[])d.clone() : d;
   }

   public ArrayFieldVector(T[] d, int pos, int size) {
      if (d.length < pos + size) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POSITION_SIZE_MISMATCH_INPUT_ARRAY, pos, size, d.length);
      } else {
         this.field = d[0].getField();
         this.data = this.buildArray(size);
         System.arraycopy(d, pos, this.data, 0, size);
      }
   }

   public ArrayFieldVector(FieldVector<T> v) {
      this.field = v.getField();
      this.data = this.buildArray(v.getDimension());

      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = v.getEntry(i);
      }
   }

   public ArrayFieldVector(ArrayFieldVector<T> v) {
      this.field = v.getField();
      this.data = (FieldElement[])v.data.clone();
   }

   public ArrayFieldVector(ArrayFieldVector<T> v, boolean deep) {
      this.field = v.getField();
      this.data = deep ? (FieldElement[])v.data.clone() : v.data;
   }

   public ArrayFieldVector(ArrayFieldVector<T> v1, ArrayFieldVector<T> v2) {
      this.field = v1.getField();
      this.data = this.buildArray(v1.data.length + v2.data.length);
      System.arraycopy(v1.data, 0, this.data, 0, v1.data.length);
      System.arraycopy(v2.data, 0, this.data, v1.data.length, v2.data.length);
   }

   public ArrayFieldVector(ArrayFieldVector<T> v1, T[] v2) {
      this.field = v1.getField();
      this.data = this.buildArray(v1.data.length + v2.length);
      System.arraycopy(v1.data, 0, this.data, 0, v1.data.length);
      System.arraycopy(v2, 0, this.data, v1.data.length, v2.length);
   }

   public ArrayFieldVector(T[] v1, ArrayFieldVector<T> v2) {
      this.field = v2.getField();
      this.data = this.buildArray(v1.length + v2.data.length);
      System.arraycopy(v1, 0, this.data, 0, v1.length);
      System.arraycopy(v2.data, 0, this.data, v1.length, v2.data.length);
   }

   public ArrayFieldVector(T[] v1, T[] v2) {
      try {
         this.data = this.buildArray(v1.length + v2.length);
         System.arraycopy(v1, 0, this.data, 0, v1.length);
         System.arraycopy(v2, 0, this.data, v1.length, v2.length);
         this.field = this.data[0].getField();
      } catch (ArrayIndexOutOfBoundsException var4) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_MUST_HAVE_AT_LEAST_ONE_ELEMENT);
      }
   }

   public ArrayFieldVector(Field<T> field, T[] v1, T[] v2) {
      if (v1.length + v2.length == 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_MUST_HAVE_AT_LEAST_ONE_ELEMENT);
      } else {
         this.data = this.buildArray(v1.length + v2.length);
         System.arraycopy(v1, 0, this.data, 0, v1.length);
         System.arraycopy(v2, 0, this.data, v1.length, v2.length);
         this.field = this.data[0].getField();
      }
   }

   private T[] buildArray(int length) {
      return (T[])((FieldElement[])Array.newInstance(this.field.getZero().getClass(), length));
   }

   @Override
   public Field<T> getField() {
      return this.field;
   }

   @Override
   public FieldVector<T> copy() {
      return new ArrayFieldVector<>(this, true);
   }

   @Override
   public FieldVector<T> add(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.add((ArrayFieldVector<T>)v);
      } catch (ClassCastException var5) {
         this.checkVectorDimensions(v);
         T[] out = this.buildArray(this.data.length);

         for(int i = 0; i < this.data.length; ++i) {
            out[i] = this.data[i].add(v.getEntry(i));
         }

         return new ArrayFieldVector<>(out);
      }
   }

   @Override
   public FieldVector<T> add(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].add(v[i]);
      }

      return new ArrayFieldVector<>(out);
   }

   public ArrayFieldVector<T> add(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return (ArrayFieldVector<T>)this.add(v.data);
   }

   @Override
   public FieldVector<T> subtract(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.subtract((ArrayFieldVector<T>)v);
      } catch (ClassCastException var5) {
         this.checkVectorDimensions(v);
         T[] out = this.buildArray(this.data.length);

         for(int i = 0; i < this.data.length; ++i) {
            out[i] = this.data[i].subtract(v.getEntry(i));
         }

         return new ArrayFieldVector<>(out);
      }
   }

   @Override
   public FieldVector<T> subtract(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].subtract(v[i]);
      }

      return new ArrayFieldVector<>(out);
   }

   public ArrayFieldVector<T> subtract(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return (ArrayFieldVector<T>)this.subtract(v.data);
   }

   @Override
   public FieldVector<T> mapAdd(T d) {
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].add(d);
      }

      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> mapAddToSelf(T d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = this.data[i].add(d);
      }

      return this;
   }

   @Override
   public FieldVector<T> mapSubtract(T d) {
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].subtract(d);
      }

      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> mapSubtractToSelf(T d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = this.data[i].subtract(d);
      }

      return this;
   }

   @Override
   public FieldVector<T> mapMultiply(T d) {
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].multiply(d);
      }

      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> mapMultiplyToSelf(T d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = this.data[i].multiply(d);
      }

      return this;
   }

   @Override
   public FieldVector<T> mapDivide(T d) {
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].divide(d);
      }

      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> mapDivideToSelf(T d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = this.data[i].divide(d);
      }

      return this;
   }

   @Override
   public FieldVector<T> mapInv() {
      T[] out = this.buildArray(this.data.length);
      T one = this.field.getOne();

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = one.divide(this.data[i]);
      }

      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> mapInvToSelf() {
      T one = this.field.getOne();

      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = one.divide(this.data[i]);
      }

      return this;
   }

   @Override
   public FieldVector<T> ebeMultiply(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.ebeMultiply((ArrayFieldVector<T>)v);
      } catch (ClassCastException var5) {
         this.checkVectorDimensions(v);
         T[] out = this.buildArray(this.data.length);

         for(int i = 0; i < this.data.length; ++i) {
            out[i] = this.data[i].multiply(v.getEntry(i));
         }

         return new ArrayFieldVector<>(out);
      }
   }

   @Override
   public FieldVector<T> ebeMultiply(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].multiply(v[i]);
      }

      return new ArrayFieldVector<>(out);
   }

   public ArrayFieldVector<T> ebeMultiply(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return (ArrayFieldVector<T>)this.ebeMultiply(v.data);
   }

   @Override
   public FieldVector<T> ebeDivide(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.ebeDivide((ArrayFieldVector<T>)v);
      } catch (ClassCastException var5) {
         this.checkVectorDimensions(v);
         T[] out = this.buildArray(this.data.length);

         for(int i = 0; i < this.data.length; ++i) {
            out[i] = this.data[i].divide(v.getEntry(i));
         }

         return new ArrayFieldVector<>(out);
      }
   }

   @Override
   public FieldVector<T> ebeDivide(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T[] out = this.buildArray(this.data.length);

      for(int i = 0; i < this.data.length; ++i) {
         out[i] = this.data[i].divide(v[i]);
      }

      return new ArrayFieldVector<>(out);
   }

   public ArrayFieldVector<T> ebeDivide(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return (ArrayFieldVector<T>)this.ebeDivide(v.data);
   }

   @Override
   public T[] getData() {
      return (T[])((FieldElement[])this.data.clone());
   }

   public T[] getDataRef() {
      return this.data;
   }

   @Override
   public T dotProduct(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.dotProduct((ArrayFieldVector<T>)v);
      } catch (ClassCastException var5) {
         this.checkVectorDimensions(v);
         T dot = this.field.getZero();

         for(int i = 0; i < this.data.length; ++i) {
            dot = dot.add(this.data[i].multiply(v.getEntry(i)));
         }

         return dot;
      }
   }

   @Override
   public T dotProduct(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      T dot = this.field.getZero();

      for(int i = 0; i < this.data.length; ++i) {
         dot = dot.add(this.data[i].multiply(v[i]));
      }

      return dot;
   }

   public T dotProduct(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return this.dotProduct(v.data);
   }

   @Override
   public FieldVector<T> projection(FieldVector<T> v) {
      return v.mapMultiply(this.dotProduct(v).divide(v.dotProduct(v)));
   }

   @Override
   public FieldVector<T> projection(T[] v) {
      return this.projection(new ArrayFieldVector<>(v, false));
   }

   public ArrayFieldVector<T> projection(ArrayFieldVector<T> v) {
      return (ArrayFieldVector<T>)v.mapMultiply(this.dotProduct(v).divide(v.dotProduct(v)));
   }

   @Override
   public FieldMatrix<T> outerProduct(FieldVector<T> v) throws IllegalArgumentException {
      try {
         return this.outerProduct((ArrayFieldVector<T>)v);
      } catch (ClassCastException var7) {
         this.checkVectorDimensions(v);
         int m = this.data.length;
         FieldMatrix<T> out = new Array2DRowFieldMatrix<>(this.field, m, m);

         for(int i = 0; i < this.data.length; ++i) {
            for(int j = 0; j < this.data.length; ++j) {
               out.setEntry(i, j, this.data[i].multiply(v.getEntry(j)));
            }
         }

         return out;
      }
   }

   public FieldMatrix<T> outerProduct(ArrayFieldVector<T> v) throws IllegalArgumentException {
      return this.outerProduct(v.data);
   }

   @Override
   public FieldMatrix<T> outerProduct(T[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      int m = this.data.length;
      FieldMatrix<T> out = new Array2DRowFieldMatrix<>(this.field, m, m);

      for(int i = 0; i < this.data.length; ++i) {
         for(int j = 0; j < this.data.length; ++j) {
            out.setEntry(i, j, this.data[i].multiply(v[j]));
         }
      }

      return out;
   }

   @Override
   public T getEntry(int index) throws MatrixIndexException {
      return this.data[index];
   }

   @Override
   public int getDimension() {
      return this.data.length;
   }

   @Override
   public FieldVector<T> append(FieldVector<T> v) {
      try {
         return this.append((ArrayFieldVector<T>)v);
      } catch (ClassCastException var3) {
         return new ArrayFieldVector<>(this, new ArrayFieldVector<>(v));
      }
   }

   public ArrayFieldVector<T> append(ArrayFieldVector<T> v) {
      return new ArrayFieldVector<>(this, v);
   }

   @Override
   public FieldVector<T> append(T in) {
      T[] out = this.buildArray(this.data.length + 1);
      System.arraycopy(this.data, 0, out, 0, this.data.length);
      out[this.data.length] = in;
      return new ArrayFieldVector<>(out);
   }

   @Override
   public FieldVector<T> append(T[] in) {
      return new ArrayFieldVector<>(this, in);
   }

   @Override
   public FieldVector<T> getSubVector(int index, int n) {
      ArrayFieldVector<T> out = new ArrayFieldVector<>(this.field, n);

      try {
         System.arraycopy(this.data, index, out.data, 0, n);
      } catch (IndexOutOfBoundsException var5) {
         this.checkIndex(index);
         this.checkIndex(index + n - 1);
      }

      return out;
   }

   @Override
   public void setEntry(int index, T value) {
      try {
         this.data[index] = value;
      } catch (IndexOutOfBoundsException var4) {
         this.checkIndex(index);
      }
   }

   @Override
   public void setSubVector(int index, FieldVector<T> v) {
      try {
         try {
            this.set(index, (ArrayFieldVector<T>)v);
         } catch (ClassCastException var5) {
            for(int i = index; i < index + v.getDimension(); ++i) {
               this.data[i] = v.getEntry(i - index);
            }
         }
      } catch (IndexOutOfBoundsException var6) {
         this.checkIndex(index);
         this.checkIndex(index + v.getDimension() - 1);
      }
   }

   @Override
   public void setSubVector(int index, T[] v) {
      try {
         System.arraycopy(v, 0, this.data, index, v.length);
      } catch (IndexOutOfBoundsException var4) {
         this.checkIndex(index);
         this.checkIndex(index + v.length - 1);
      }
   }

   public void set(int index, ArrayFieldVector<T> v) throws MatrixIndexException {
      this.setSubVector(index, v.data);
   }

   @Override
   public void set(T value) {
      Arrays.fill(this.data, value);
   }

   @Override
   public T[] toArray() {
      return (T[])((FieldElement[])this.data.clone());
   }

   protected void checkVectorDimensions(FieldVector<T> v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
   }

   protected void checkVectorDimensions(int n) throws IllegalArgumentException {
      if (this.data.length != n) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, this.data.length, n);
      }
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other == null) {
         return false;
      } else {
         try {
            FieldVector<T> rhs = (FieldVector)other;
            if (this.data.length != rhs.getDimension()) {
               return false;
            } else {
               for(int i = 0; i < this.data.length; ++i) {
                  if (!this.data[i].equals(rhs.getEntry(i))) {
                     return false;
                  }
               }

               return true;
            }
         } catch (ClassCastException var4) {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int h = 3542;

      for(T a : this.data) {
         h ^= a.hashCode();
      }

      return h;
   }

   private void checkIndex(int index) throws MatrixIndexException {
      if (index < 0 || index >= this.getDimension()) {
         throw new MatrixIndexException(LocalizedFormats.INDEX_OUT_OF_RANGE, index, 0, this.getDimension() - 1);
      }
   }
}
