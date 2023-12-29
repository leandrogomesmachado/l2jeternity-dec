package org.eclipse.jdt.internal.compiler.env;

public interface IBinaryNestedType {
   char[] getEnclosingTypeName();

   int getModifiers();

   char[] getName();
}
