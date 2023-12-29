package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

public class AbortMethod extends AbortType {
   private static final long serialVersionUID = -1480267398969840003L;

   public AbortMethod(CompilationResult compilationResult, CategorizedProblem problem) {
      super(compilationResult, problem);
   }
}
