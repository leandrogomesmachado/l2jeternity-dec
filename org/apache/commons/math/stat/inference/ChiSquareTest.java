package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;

public interface ChiSquareTest {
   double chiSquare(double[] var1, long[] var2) throws IllegalArgumentException;

   double chiSquareTest(double[] var1, long[] var2) throws IllegalArgumentException, MathException;

   boolean chiSquareTest(double[] var1, long[] var2, double var3) throws IllegalArgumentException, MathException;

   double chiSquare(long[][] var1) throws IllegalArgumentException;

   double chiSquareTest(long[][] var1) throws IllegalArgumentException, MathException;

   boolean chiSquareTest(long[][] var1, double var2) throws IllegalArgumentException, MathException;
}
