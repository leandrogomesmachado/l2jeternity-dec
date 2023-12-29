package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

public class AbortType extends AbortCompilationUnit {
   private static final long serialVersionUID = -5882417089349134385L;

   public AbortType(CompilationResult compilationResult, CategorizedProblem problem) {
      super(compilationResult, problem);
   }
}
