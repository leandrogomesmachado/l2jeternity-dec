package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;

public interface UnknownDistributionChiSquareTest extends ChiSquareTest {
   double chiSquareDataSetsComparison(long[] var1, long[] var2) throws IllegalArgumentException;

   double chiSquareTestDataSetsComparison(long[] var1, long[] var2) throws IllegalArgumentException, MathException;

   boolean chiSquareTestDataSetsComparison(long[] var1, long[] var2, double var3) throws IllegalArgumentException, MathException;
}
