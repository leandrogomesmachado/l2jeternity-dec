package org.eclipse.jdt.internal.compiler.env;

public interface IGenericMethod {
   int getModifiers();

   boolean isConstructor();

   char[][] getArgumentNames();
}
