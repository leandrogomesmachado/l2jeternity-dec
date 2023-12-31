package org.apache.commons.math.linear;

public interface LUDecomposition {
   RealMatrix getL();

   RealMatrix getU();

   RealMatrix getP();

   int[] getPivot();

   double getDeterminant();

   DecompositionSolver getSolver();
}
