package org.apache.commons.math.linear;

import org.apache.commons.math.FieldElement;

public interface FieldMatrixPreservingVisitor<T extends FieldElement<?>> {
   void start(int var1, int var2, int var3, int var4, int var5, int var6);

   void visit(int var1, int var2, T var3) throws MatrixVisitorException;

   T end();
}
