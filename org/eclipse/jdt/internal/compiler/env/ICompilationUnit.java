package org.eclipse.jdt.internal.compiler.env;

public interface ICompilationUnit extends IDependent {
   char[] getContents();

   char[] getMainTypeName();

   char[][] getPackageName();

   boolean ignoreOptionalProblems();
}
