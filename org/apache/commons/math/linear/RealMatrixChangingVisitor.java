package org.apache.commons.math.linear;

public interface RealMatrixChangingVisitor {
   void start(int var1, int var2, int var3, int var4, int var5, int var6);

   double visit(int var1, int var2, double var3) throws MatrixVisitorException;

   double end();
}
