package org.apache.commons.math.linear;

import java.math.BigDecimal;

@Deprecated
public interface BigMatrix extends AnyMatrix {
   BigMatrix copy();

   BigMatrix add(BigMatrix var1) throws IllegalArgumentException;

   BigMatrix subtract(BigMatrix var1) throws IllegalArgumentException;

   BigMatrix scalarAdd(BigDecimal var1);

   BigMatrix scalarMultiply(BigDecimal var1);

   BigMatrix multiply(BigMatrix var1) throws IllegalArgumentException;

   BigMatrix preMultiply(BigMatrix var1) throws IllegalArgumentException;

   BigDecimal[][] getData();

   double[][] getDataAsDoubleArray();

   int getRoundingMode();

   BigDecimal getNorm();

   BigMatrix getSubMatrix(int var1, int var2, int var3, int var4) throws MatrixIndexException;

   BigMatrix getSubMatrix(int[] var1, int[] var2) throws MatrixIndexException;

   BigMatrix getRowMatrix(int var1) throws MatrixIndexException;

   BigMatrix getColumnMatrix(int var1) throws MatrixIndexException;

   BigDecimal[] getRow(int var1) throws MatrixIndexException;

   double[] getRowAsDoubleArray(int var1) throws MatrixIndexException;

   BigDecimal[] getColumn(int var1) throws MatrixIndexException;

   double[] getColumnAsDoubleArray(int var1) throws MatrixIndexException;

   BigDecimal getEntry(int var1, int var2) throws MatrixIndexException;

   double getEntryAsDouble(int var1, int var2) throws MatrixIndexException;

   BigMatrix transpose();

   BigMatrix inverse() throws InvalidMatrixException;

   BigDecimal getDeterminant() throws InvalidMatrixException;

   BigDecimal getTrace();

   BigDecimal[] operate(BigDecimal[] var1) throws IllegalArgumentException;

   BigDecimal[] preMultiply(BigDecimal[] var1) throws IllegalArgumentException;

   BigDecimal[] solve(BigDecimal[] var1) throws IllegalArgumentException, InvalidMatrixException;

   BigMatrix solve(BigMatrix var1) throws IllegalArgumentException, InvalidMatrixException;
}
