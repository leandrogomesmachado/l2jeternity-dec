package org.apache.commons.math.linear;

public interface SingularValueDecomposition {
   RealMatrix getU();

   RealMatrix getUT();

   RealMatrix getS();

   double[] getSingularValues();

   RealMatrix getV();

   RealMatrix getVT();

   RealMatrix getCovariance(double var1) throws IllegalArgumentException;

   double getNorm();

   double getConditionNumber();

   int getRank();

   DecompositionSolver getSolver();
}
