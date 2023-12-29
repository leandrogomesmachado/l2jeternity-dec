package org.eclipse.jdt.internal.compiler.impl;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public interface ReferenceContext {
   void abort(int var1, CategorizedProblem var2);

   CompilationResult compilationResult();

   CompilationUnitDeclaration getCompilationUnitDeclaration();

   boolean hasErrors();

   void tagAsHavingErrors();

   void tagAsHavingIgnoredMandatoryErrors(int var1);
}
