package org.eclipse.jdt.internal.compiler.lookup;

public interface Substitution {
   TypeBinding substitute(TypeVariableBinding var1);

   LookupEnvironment environment();

   boolean isRawSubstitution();
}
