package org.apache.commons.math.linear;

import org.apache.commons.math.FieldElement;

public class DefaultFieldMatrixPreservingVisitor<T extends FieldElement<T>> implements FieldMatrixPreservingVisitor<T> {
   private final T zero;

   public DefaultFieldMatrixPreservingVisitor(T zero) {
      this.zero = zero;
   }

   @Override
   public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
   }

   @Override
   public void visit(int row, int column, T value) throws MatrixVisitorException {
   }

   @Override
   public T end() {
      return this.zero;
   }
}
