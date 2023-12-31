package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.impl.Constant;

public class SyntheticFieldBinding extends FieldBinding {
   public int index;

   public SyntheticFieldBinding(char[] name, TypeBinding type, int modifiers, ReferenceBinding declaringClass, Constant constant, int index) {
      super(name, type, modifiers, declaringClass, constant);
      this.index = index;
      this.tagBits |= 25769803776L;
   }
}
