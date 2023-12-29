package org.apache.commons.math.linear;

public interface DecompositionSolver {
   double[] solve(double[] var1) throws IllegalArgumentException, InvalidMatrixException;

   RealVector solve(RealVector var1) throws IllegalArgumentException, InvalidMatrixException;

   RealMatrix solve(RealMatrix var1) throws IllegalArgumentException, InvalidMatrixException;

   boolean isNonSingular();

   RealMatrix getInverse() throws InvalidMatrixException;
}
