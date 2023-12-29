package org.eclipse.jdt.internal.compiler.env;

public interface IBinaryAnnotation {
   char[] getTypeName();

   IBinaryElementValuePair[] getElementValuePairs();
}
