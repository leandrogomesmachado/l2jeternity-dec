package org.apache.commons.math.linear;

public interface RealMatrix extends AnyMatrix {
   RealMatrix createMatrix(int var1, int var2);

   RealMatrix copy();

   RealMatrix add(RealMatrix var1) throws IllegalArgumentException;

   RealMatrix subtract(RealMatrix var1) throws IllegalArgumentException;

   RealMatrix scalarAdd(double var1);

   RealMatrix scalarMultiply(double var1);

   RealMatrix multiply(RealMatrix var1) throws IllegalArgumentException;

   RealMatrix preMultiply(RealMatrix var1) throws IllegalArgumentException;

   double[][] getData();

   double getNorm();

   double getFrobeniusNorm();

   RealMatrix getSubMatrix(int var1, int var2, int var3, int var4) throws MatrixIndexException;

   RealMatrix getSubMatrix(int[] var1, int[] var2) throws MatrixIndexException;

   void copySubMatrix(int var1, int var2, int var3, int var4, double[][] var5) throws MatrixIndexException, IllegalArgumentException;

   void copySubMatrix(int[] var1, int[] var2, double[][] var3) throws MatrixIndexException, IllegalArgumentException;

   void setSubMatrix(double[][] var1, int var2, int var3) throws MatrixIndexException;

   RealMatrix getRowMatrix(int var1) throws MatrixIndexException;

   void setRowMatrix(int var1, RealMatrix var2) throws MatrixIndexException, InvalidMatrixException;

   RealMatrix getColumnMatrix(int var1) throws MatrixIndexException;

   void setColumnMatrix(int var1, RealMatrix var2) throws MatrixIndexException, InvalidMatrixException;

   RealVector getRowVector(int var1) throws MatrixIndexException;

   void setRowVector(int var1, RealVector var2) throws MatrixIndexException, InvalidMatrixException;

   RealVector getColumnVector(int var1) throws MatrixIndexException;

   void setColumnVector(int var1, RealVector var2) throws MatrixIndexException, InvalidMatrixException;

   double[] getRow(int var1) throws MatrixIndexException;

   void setRow(int var1, double[] var2) throws MatrixIndexException, InvalidMatrixException;

   double[] getColumn(int var1) throws MatrixIndexException;

   void setColumn(int var1, double[] var2) throws MatrixIndexException, InvalidMatrixException;

   double getEntry(int var1, int var2) throws MatrixIndexException;

   void setEntry(int var1, int var2, double var3) throws MatrixIndexException;

   void addToEntry(int var1, int var2, double var3) throws MatrixIndexException;

   void multiplyEntry(int var1, int var2, double var3) throws MatrixIndexException;

   RealMatrix transpose();

   @Deprecated
   RealMatrix inverse() throws InvalidMatrixException;

   @Deprecated
   double getDeterminant();

   @Deprecated
   boolean isSingular();

   double getTrace() throws NonSquareMatrixException;

   double[] operate(double[] var1) throws IllegalArgumentException;

   RealVector operate(RealVector var1) throws IllegalArgumentException;

   double[] preMultiply(double[] var1) throws IllegalArgumentException;

   RealVector preMultiply(RealVector var1) throws IllegalArgumentException;

   double walkInRowOrder(RealMatrixChangingVisitor var1) throws MatrixVisitorException;

   double walkInRowOrder(RealMatrixPreservingVisitor var1) throws MatrixVisitorException;

   double walkInRowOrder(RealMatrixChangingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   double walkInRowOrder(RealMatrixPreservingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   double walkInColumnOrder(RealMatrixChangingVisitor var1) throws MatrixVisitorException;

   double walkInColumnOrder(RealMatrixPreservingVisitor var1) throws MatrixVisitorException;

   double walkInColumnOrder(RealMatrixChangingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   double walkInColumnOrder(RealMatrixPreservingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   double walkInOptimizedOrder(RealMatrixChangingVisitor var1) throws MatrixVisitorException;

   double walkInOptimizedOrder(RealMatrixPreservingVisitor var1) throws MatrixVisitorException;

   double walkInOptimizedOrder(RealMatrixChangingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   double walkInOptimizedOrder(RealMatrixPreservingVisitor var1, int var2, int var3, int var4, int var5) throws MatrixIndexException, MatrixVisitorException;

   @Deprecated
   double[] solve(double[] var1) throws IllegalArgumentException, InvalidMatrixException;

   @Deprecated
   RealMatrix solve(RealMatrix var1) throws IllegalArgumentException, InvalidMatrixException;
}
