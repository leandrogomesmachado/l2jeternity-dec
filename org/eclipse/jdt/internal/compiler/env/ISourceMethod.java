package org.eclipse.jdt.internal.compiler.env;

public interface ISourceMethod extends IGenericMethod {
   int getDeclarationSourceEnd();

   int getDeclarationSourceStart();

   char[][] getExceptionTypeNames();

   int getNameSourceEnd();

   int getNameSourceStart();

   char[] getReturnTypeName();

   char[][] getTypeParameterNames();

   char[][][] getTypeParameterBounds();
}
