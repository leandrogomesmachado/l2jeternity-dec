package org.eclipse.jdt.internal.compiler.apt.dispatch;

import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class AptProblem extends DefaultProblem {
   private static final String MARKER_ID = "org.eclipse.jdt.apt.pluggable.core.compileProblem";
   public final ReferenceContext _referenceContext;

   public AptProblem(
      ReferenceContext referenceContext,
      char[] originatingFileName,
      String message,
      int id,
      String[] stringArguments,
      int severity,
      int startPosition,
      int endPosition,
      int line,
      int column
   ) {
      super(originatingFileName, message, id, stringArguments, severity, startPosition, endPosition, line, column);
      this._referenceContext = referenceContext;
   }

   @Override
   public int getCategoryID() {
      return 0;
   }

   @Override
   public String getMarkerType() {
      return "org.eclipse.jdt.apt.pluggable.core.compileProblem";
   }
}
