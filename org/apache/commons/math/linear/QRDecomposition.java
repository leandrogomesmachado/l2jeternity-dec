package org.apache.commons.math.linear;

public interface QRDecomposition {
   RealMatrix getR();

   RealMatrix getQ();

   RealMatrix getQT();

   RealMatrix getH();

   DecompositionSolver getSolver();
}
