package org.eclipse.jdt.internal.compiler.lookup;

public class ProblemPackageBinding extends PackageBinding {
   private int problemId;

   ProblemPackageBinding(char[][] compoundName, int problemId) {
      this.compoundName = compoundName;
      this.problemId = problemId;
   }

   ProblemPackageBinding(char[] name, int problemId) {
      this(new char[][]{name}, problemId);
   }

   @Override
   public final int problemId() {
      return this.problemId;
   }
}
