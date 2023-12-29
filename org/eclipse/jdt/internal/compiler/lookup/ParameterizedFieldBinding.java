package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.impl.Constant;

public class ParameterizedFieldBinding extends FieldBinding {
   public FieldBinding originalField;

   public ParameterizedFieldBinding(ParameterizedTypeBinding parameterizedDeclaringClass, FieldBinding originalField) {
      super(
         originalField.name,
         (TypeBinding)((originalField.modifiers & 16384) != 0
            ? parameterizedDeclaringClass
            : ((originalField.modifiers & 8) != 0 ? originalField.type : Scope.substitute(parameterizedDeclaringClass, originalField.type))),
         originalField.modifiers,
         parameterizedDeclaringClass,
         null
      );
      this.originalField = originalField;
      this.tagBits = originalField.tagBits;
      this.id = originalField.id;
   }

   @Override
   public Constant constant() {
      return this.originalField.constant();
   }

   @Override
   public FieldBinding original() {
      return this.originalField.original();
   }

   @Override
   public void setConstant(Constant constant) {
      this.originalField.setConstant(constant);
   }
}
