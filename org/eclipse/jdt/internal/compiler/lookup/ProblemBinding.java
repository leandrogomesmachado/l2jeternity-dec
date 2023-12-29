package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class ProblemBinding extends Binding {
   public char[] name;
   public ReferenceBinding searchType;
   private int problemId;

   public ProblemBinding(char[][] compoundName, int problemId) {
      this(CharOperation.concatWith(compoundName, '.'), problemId);
   }

   public ProblemBinding(char[][] compoundName, ReferenceBinding searchType, int problemId) {
      this(CharOperation.concatWith(compoundName, '.'), searchType, problemId);
   }

   ProblemBinding(char[] name, int problemId) {
      this.name = name;
      this.problemId = problemId;
   }

   ProblemBinding(char[] name, ReferenceBinding searchType, int problemId) {
      this(name, problemId);
      this.searchType = searchType;
   }

   @Override
   public final int kind() {
      return 7;
   }

   @Override
   public final int problemId() {
      return this.problemId;
   }

   @Override
   public char[] readableName() {
      return this.name;
   }
}
