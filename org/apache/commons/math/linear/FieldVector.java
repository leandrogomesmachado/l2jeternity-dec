package org.apache.commons.math.linear;

import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;

public interface FieldVector<T extends FieldElement<T>> {
   Field<T> getField();

   FieldVector<T> copy();

   FieldVector<T> add(FieldVector<T> var1) throws IllegalArgumentException;

   FieldVector<T> add(T[] var1) throws IllegalArgumentException;

   FieldVector<T> subtract(FieldVector<T> var1) throws IllegalArgumentException;

   FieldVector<T> subtract(T[] var1) throws IllegalArgumentException;

   FieldVector<T> mapAdd(T var1);

   FieldVector<T> mapAddToSelf(T var1);

   FieldVector<T> mapSubtract(T var1);

   FieldVector<T> mapSubtractToSelf(T var1);

   FieldVector<T> mapMultiply(T var1);

   FieldVector<T> mapMultiplyToSelf(T var1);

   FieldVector<T> mapDivide(T var1);

   FieldVector<T> mapDivideToSelf(T var1);

   FieldVector<T> mapInv();

   FieldVector<T> mapInvToSelf();

   FieldVector<T> ebeMultiply(FieldVector<T> var1) throws IllegalArgumentException;

   FieldVector<T> ebeMultiply(T[] var1) throws IllegalArgumentException;

   FieldVector<T> ebeDivide(FieldVector<T> var1) throws IllegalArgumentException;

   FieldVector<T> ebeDivide(T[] var1) throws IllegalArgumentException;

   T[] getData();

   T dotProduct(FieldVector<T> var1) throws IllegalArgumentException;

   T dotProduct(T[] var1) throws IllegalArgumentException;

   FieldVector<T> projection(FieldVector<T> var1) throws IllegalArgumentException;

   FieldVector<T> projection(T[] var1) throws IllegalArgumentException;

   FieldMatrix<T> outerProduct(FieldVector<T> var1) throws IllegalArgumentException;

   FieldMatrix<T> outerProduct(T[] var1) throws IllegalArgumentException;

   T getEntry(int var1) throws MatrixIndexException;

   void setEntry(int var1, T var2) throws MatrixIndexException;

   int getDimension();

   FieldVector<T> append(FieldVector<T> var1);

   FieldVector<T> append(T var1);

   FieldVector<T> append(T[] var1);

   FieldVector<T> getSubVector(int var1, int var2) throws MatrixIndexException;

   void setSubVector(int var1, FieldVector<T> var2) throws MatrixIndexException;

   void setSubVector(int var1, T[] var2) throws MatrixIndexException;

   void set(T var1);

   T[] toArray();
}
