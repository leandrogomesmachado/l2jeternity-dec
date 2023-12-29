package org.eclipse.jdt.internal.compiler.problem;

import java.io.IOException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

public class AbortCompilationUnit extends AbortCompilation {
   private static final long serialVersionUID = -4253893529982226734L;
   public String encoding;

   public AbortCompilationUnit(CompilationResult compilationResult, CategorizedProblem problem) {
      super(compilationResult, problem);
   }

   public AbortCompilationUnit(CompilationResult compilationResult, IOException exception, String encoding) {
      super(compilationResult, exception);
      this.encoding = encoding;
   }
}
