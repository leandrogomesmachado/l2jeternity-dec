package org.apache.commons.math.linear;

public interface CholeskyDecomposition {
   RealMatrix getL();

   RealMatrix getLT();

   double getDeterminant();

   DecompositionSolver getSolver();
}
