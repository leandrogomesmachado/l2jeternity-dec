package org.eclipse.jdt.internal.compiler.lookup;

public class SyntheticFactoryMethodBinding extends MethodBinding {
   private MethodBinding staticFactoryFor;
   private LookupEnvironment environment;
   private ReferenceBinding enclosingType;

   public SyntheticFactoryMethodBinding(MethodBinding method, LookupEnvironment environment, ReferenceBinding enclosingType) {
      super(method.modifiers | 8, TypeConstants.SYNTHETIC_STATIC_FACTORY, null, null, null, method.declaringClass);
      this.environment = environment;
      this.staticFactoryFor = method;
      this.enclosingType = enclosingType;
   }

   public MethodBinding getConstructor() {
      return this.staticFactoryFor;
   }

   public ParameterizedMethodBinding applyTypeArgumentsOnConstructor(
      TypeBinding[] typeArguments, TypeBinding[] constructorTypeArguments, boolean inferredWithUncheckedConversion
   ) {
      ReferenceBinding parameterizedType = this.environment.createParameterizedType(this.declaringClass, typeArguments, this.enclosingType);

      MethodBinding[] var8;
      for(MethodBinding parameterizedMethod : var8 = parameterizedType.methods()) {
         if (parameterizedMethod.original() == this.staticFactoryFor) {
            return (ParameterizedMethodBinding)(constructorTypeArguments.length <= 0 && !inferredWithUncheckedConversion
               ? (ParameterizedMethodBinding)parameterizedMethod
               : this.environment.createParameterizedGenericMethod(parameterizedMethod, constructorTypeArguments, inferredWithUncheckedConversion, false));
         }

         if (parameterizedMethod instanceof ProblemMethodBinding) {
            MethodBinding closestMatch = ((ProblemMethodBinding)parameterizedMethod).closestMatch;
            if (closestMatch instanceof ParameterizedMethodBinding && closestMatch.original() == this.staticFactoryFor) {
               return (ParameterizedMethodBinding)closestMatch;
            }
         }
      }

      throw new IllegalArgumentException("Type doesn't have its own method?");
   }
}
