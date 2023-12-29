package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class SyntheticArgumentBinding extends LocalVariableBinding {
   public LocalVariableBinding actualOuterLocalVariable;
   public FieldBinding matchingField;

   public SyntheticArgumentBinding(LocalVariableBinding actualOuterLocalVariable) {
      super(CharOperation.concat(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, actualOuterLocalVariable.name), actualOuterLocalVariable.type, 16, true);
      this.tagBits |= 1024L;
      this.useFlag = 1;
      this.actualOuterLocalVariable = actualOuterLocalVariable;
   }

   public SyntheticArgumentBinding(ReferenceBinding enclosingType) {
      super(
         CharOperation.concat(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, String.valueOf(enclosingType.depth()).toCharArray()), enclosingType, 16, true
      );
      this.tagBits |= 1024L;
      this.useFlag = 1;
   }
}
