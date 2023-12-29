package org.apache.commons.math.linear;

import org.apache.commons.math.FieldElement;

public interface FieldLUDecomposition<T extends FieldElement<T>> {
   FieldMatrix<T> getL();

   FieldMatrix<T> getU();

   FieldMatrix<T> getP();

   int[] getPivot();

   T getDeterminant();

   FieldDecompositionSolver<T> getSolver();
}
