package org.eclipse.jdt.internal.compiler.lookup;

public class MostSpecificExceptionMethodBinding extends MethodBinding {
   private MethodBinding originalMethod;

   public MostSpecificExceptionMethodBinding(MethodBinding originalMethod, ReferenceBinding[] mostSpecificExceptions) {
      super(
         originalMethod.modifiers,
         originalMethod.selector,
         originalMethod.returnType,
         originalMethod.parameters,
         mostSpecificExceptions,
         originalMethod.declaringClass
      );
      this.originalMethod = originalMethod;
      this.parameterNonNullness = originalMethod.parameterNonNullness;
      this.defaultNullness = originalMethod.defaultNullness;
   }

   @Override
   public MethodBinding original() {
      return this.originalMethod.original();
   }
}
