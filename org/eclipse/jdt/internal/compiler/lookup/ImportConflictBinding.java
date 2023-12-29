package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class ImportConflictBinding extends ImportBinding {
   public ReferenceBinding conflictingTypeBinding;

   public ImportConflictBinding(char[][] compoundName, Binding methodBinding, ReferenceBinding conflictingTypeBinding, ImportReference reference) {
      super(compoundName, false, methodBinding, reference);
      this.conflictingTypeBinding = conflictingTypeBinding;
   }

   @Override
   public char[] readableName() {
      return CharOperation.concatWith(this.compoundName, '.');
   }

   @Override
   public String toString() {
      return "method import : " + new String(this.readableName());
   }
}
