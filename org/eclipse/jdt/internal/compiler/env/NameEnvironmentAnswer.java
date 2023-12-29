package org.eclipse.jdt.internal.compiler.env;

public class NameEnvironmentAnswer {
   IBinaryType binaryType;
   ICompilationUnit compilationUnit;
   ISourceType[] sourceTypes;
   AccessRestriction accessRestriction;
   String externalAnnotationPath;

   public NameEnvironmentAnswer(IBinaryType binaryType, AccessRestriction accessRestriction) {
      this.binaryType = binaryType;
      this.accessRestriction = accessRestriction;
   }

   public NameEnvironmentAnswer(ICompilationUnit compilationUnit, AccessRestriction accessRestriction) {
      this.compilationUnit = compilationUnit;
      this.accessRestriction = accessRestriction;
   }

   public NameEnvironmentAnswer(ISourceType[] sourceTypes, AccessRestriction accessRestriction, String externalAnnotationPath) {
      this.sourceTypes = sourceTypes;
      this.accessRestriction = accessRestriction;
      this.externalAnnotationPath = externalAnnotationPath;
   }

   public AccessRestriction getAccessRestriction() {
      return this.accessRestriction;
   }

   public IBinaryType getBinaryType() {
      return this.binaryType;
   }

   public ICompilationUnit getCompilationUnit() {
      return this.compilationUnit;
   }

   public String getExternalAnnotationPath() {
      return this.externalAnnotationPath;
   }

   public ISourceType[] getSourceTypes() {
      return this.sourceTypes;
   }

   public boolean isBinaryType() {
      return this.binaryType != null;
   }

   public boolean isCompilationUnit() {
      return this.compilationUnit != null;
   }

   public boolean isSourceType() {
      return this.sourceTypes != null;
   }

   public boolean ignoreIfBetter() {
      return this.accessRestriction != null && this.accessRestriction.ignoreIfBetter();
   }

   public boolean isBetter(NameEnvironmentAnswer otherAnswer) {
      if (otherAnswer == null) {
         return true;
      } else if (this.accessRestriction == null) {
         return true;
      } else {
         return otherAnswer.accessRestriction != null && this.accessRestriction.getProblemId() < otherAnswer.accessRestriction.getProblemId();
      }
   }
}
