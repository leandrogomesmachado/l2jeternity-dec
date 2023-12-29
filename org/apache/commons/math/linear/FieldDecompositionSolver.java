package org.apache.commons.math.linear;

import org.apache.commons.math.FieldElement;

public interface FieldDecompositionSolver<T extends FieldElement<T>> {
   T[] solve(T[] var1) throws IllegalArgumentException, InvalidMatrixException;

   FieldVector<T> solve(FieldVector<T> var1) throws IllegalArgumentException, InvalidMatrixException;

   FieldMatrix<T> solve(FieldMatrix<T> var1) throws IllegalArgumentException, InvalidMatrixException;

   boolean isNonSingular();

   FieldMatrix<T> getInverse() throws InvalidMatrixException;
}
