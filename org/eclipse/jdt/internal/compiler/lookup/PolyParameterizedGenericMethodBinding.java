package org.eclipse.jdt.internal.compiler.lookup;

public class PolyParameterizedGenericMethodBinding extends ParameterizedGenericMethodBinding {
   private ParameterizedGenericMethodBinding wrappedBinding;

   public PolyParameterizedGenericMethodBinding(ParameterizedGenericMethodBinding applicableMethod) {
      super(applicableMethod.originalMethod, applicableMethod.typeArguments, applicableMethod.environment, false, false);
      this.wrappedBinding = applicableMethod;
   }

   @Override
   public boolean equals(Object other) {
      if (other instanceof PolyParameterizedGenericMethodBinding) {
         PolyParameterizedGenericMethodBinding ppgmb = (PolyParameterizedGenericMethodBinding)other;
         return this.wrappedBinding.equals(ppgmb.wrappedBinding);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.wrappedBinding.hashCode();
   }
}
