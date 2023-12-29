package org.apache.commons.math.linear;

public interface RealMatrixPreservingVisitor {
   void start(int var1, int var2, int var3, int var4, int var5, int var6);

   void visit(int var1, int var2, double var3) throws MatrixVisitorException;

   double end();
}
