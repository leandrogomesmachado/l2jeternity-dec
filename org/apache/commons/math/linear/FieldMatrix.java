package org.apache.commons.math.linear;

import org.apache.commons.math.Field;
import org.apache.commons.math.FieldElement;

public interface FieldMatrix<T extends FieldElement<T>> extends AnyMatrix {
   Field<T> getField();

   FieldMatrix<T> createMatrix(int var1, int var2);

   FieldMatrix<T> copy();

   FieldMatrix<T> add(FieldMatrix<T> var1) throws IllegalArgumentException;

   FieldMatrix<T> subtract(FieldMatrix<T> var1) throws IllegalArgumentException;

   FieldMatrix<T> scalarAdd(T var1);

   FieldMatrix<T> scalarMultiply(T var1);

   FieldMatrix<T> multiply(FieldMatrix<T> var1) throws IllegalArgumentException;

   FieldMatrix<T> preMultiply(FieldMatrix<T> var1) throws IllegalArgumentException;

   T[][] getData();

   FieldMatrix<T> getSubMatrix(int var1, int var2, int var3, int var4) throws MatrixIndexException;

   FieldMatrix<T> getSubMatrix(int[] var1, int[] var2) throws MatrixIndexException;

   void copySubMatrix(int var1, int var2, int var3, int var4, T[][] var5) throws MatrixIndexException, IllegalArgumentException;

   void copySubMatrix(int[] var1, int[] var2, T[][] var3) throws MatrixIndexException, IllegalArgumentException;

   void setSubMatrix(T[][] var1, int var2, int var3) throws MatrixIndexException;

   FieldMatrix<T> getRowMatrix(int var1) throws MatrixIndexException;

   void setRowMatrix(int var1, FieldMatrix<T> var2) throws MatrixIndexException, InvalidMatrixException;

   FieldMatrix<T> getColumnMatrix(int var1) throws MatrixIndexException;

   void setColumnMatrix(int var1, FieldMatrix<T> var2) throws MatrixIndexException, InvalidMatrixException;

   FieldVector<T> getRowVector(int var1) throws MatrixIndexException;

   void setRowVector(int var1, FieldVector<T> var2) throws MatrixIndexException, InvalidMatrixException;

   FieldVector<T> getColumnVector(int var1) throws MatrixIndexException;

   void setColumnVector(int var1, FieldVector<T> var2) throws MatrixIndexException, InvalidMatrixException;

   T[] getRow(int var1) throws MatrixIndexException;

   void setRow(int var1, T[] var2) throws MatrixIndexException, InvalidMatrixException;

   T[] getColumn(int var1) throws MatrixIndexException;

   void setColumn(int var1, T[] var2) throws MatrixIndexException, InvalidMatrixException;

   T getEntry(int var1, int var2) throws MatrixIndexException;

   void setEntry(int var1, int var2, T var3) throws MatrixIndexException;

   void addToEntry(int var1, int var2, T var3) throws MatrixIndexException;

   void multiplyEntry(int var1, int var2, T var3) throws MatrixIndexException;

   FieldMatrix<T> transpose();

   T getTrace() throws NonSquareMatrixException;

   T[] operate(T[] var1) throws IllegalArgumentException;

   FieldVector<T> operate(FieldVector<T> var1) throws IllegalArgumentException;

   T[] preMultiply(T[] var1) throws IllegalArgumentException;

   FieldVector<T> preMultiply(FieldVector<T> var1) throws IllegalArgumentException;

   T walkInRowOrder(FieldMatrixChangingVisitor<T> var1) throws MatrixVisitorException;

   T walkInRowOrder(FieldMatrixPreservingVisitor<T> var1) throws MatrixVisitorException;

   T walkInRowOrder(FieldMatrixChangingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   T walkInRowOrder(FieldMatrixPreservingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   T walkInColumnOrder(FieldMatrixChangingVisitor<T> var1) throws MatrixVisitorException;

   T walkInColumnOrder(FieldMatrixPreservingVisitor<T> var1) throws MatrixVisitorException;

   T walkInColumnOrder(FieldMatrixChangingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   T walkInColumnOrder(FieldMatrixPreservingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   T walkInOptimizedOrder(FieldMatrixChangingVisitor<T> var1) throws MatrixVisitorException;

   T walkInOptimizedOrder(FieldMatrixPreservingVisitor<T> var1) throws MatrixVisitorException;

   T walkInOptimizedOrder(FieldMatrixChangingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   T walkInOptimizedOrder(FieldMatrixPreservingVisitor<T> var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;
}
