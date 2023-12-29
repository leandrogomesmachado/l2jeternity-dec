package org.apache.commons.math.linear;

public interface EigenDecomposition {
   RealMatrix getV();

   RealMatrix getD();

   RealMatrix getVT();

   double[] getRealEigenvalues();

   double getRealEigenvalue(int var1);

   double[] getImagEigenvalues();

   double getImagEigenvalue(int var1);

   RealVector getEigenvector(int var1);

   double getDeterminant();

   DecompositionSolver getSolver();
}
