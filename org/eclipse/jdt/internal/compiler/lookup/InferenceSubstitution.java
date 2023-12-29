package org.eclipse.jdt.internal.compiler.lookup;

public class InferenceSubstitution extends Scope.Substitutor implements Substitution {
   private LookupEnvironment environment;
   private InferenceVariable[] variables;
   private InvocationSite site;

   public InferenceSubstitution(LookupEnvironment environment, InferenceVariable[] variables, InvocationSite site) {
      this.environment = environment;
      this.variables = variables;
      this.site = site;
   }

   public InferenceSubstitution(InferenceContext18 context) {
      this(context.environment, context.inferenceVariables, context.currentInvocation);
   }

   @Override
   public TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
      for(int i = 0; i < this.variables.length; ++i) {
         InferenceVariable variable = this.variables[i];
         if (this.site == variable.site && TypeBinding.equalsEquals(this.getP(i), originalType)) {
            if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled && originalType.hasNullTypeAnnotations()) {
               return this.environment.createAnnotatedType(variable.withoutToplevelNullAnnotation(), originalType.getTypeAnnotations());
            }

            return variable;
         }
      }

      return super.substitute(substitution, originalType);
   }

   protected TypeBinding getP(int i) {
      return this.variables[i].typeParameter;
   }

   @Override
   public TypeBinding substitute(TypeVariableBinding typeVariable) {
      ReferenceBinding superclass = typeVariable.superclass;
      ReferenceBinding[] superInterfaces = typeVariable.superInterfaces;
      boolean hasSubstituted = false;

      for(int i = 0; i < this.variables.length; ++i) {
         InferenceVariable variable = this.variables[i];
         TypeBinding pi = this.getP(i);
         if (TypeBinding.equalsEquals(pi, typeVariable)) {
            return variable;
         }

         if (TypeBinding.equalsEquals(pi, superclass)) {
            superclass = variable;
            hasSubstituted = true;
         } else if (superInterfaces != null) {
            int ifcLen = superInterfaces.length;

            for(int j = 0; j < ifcLen; ++j) {
               if (TypeBinding.equalsEquals(pi, superInterfaces[j])) {
                  if (superInterfaces == typeVariable.superInterfaces) {
                     System.arraycopy(superInterfaces, 0, superInterfaces = new ReferenceBinding[ifcLen], 0, ifcLen);
                  }

                  superInterfaces[j] = variable;
                  hasSubstituted = true;
                  break;
               }
            }
         }
      }

      if (hasSubstituted) {
         typeVariable = new TypeVariableBinding(typeVariable.sourceName, typeVariable.declaringElement, typeVariable.rank, this.environment);
         typeVariable.superclass = superclass;
         typeVariable.superInterfaces = superInterfaces;
         typeVariable.firstBound = superclass != null ? superclass : superInterfaces[0];
         if (typeVariable.firstBound.hasNullTypeAnnotations()) {
            typeVariable.tagBits |= 1048576L;
         }
      }

      return typeVariable;
   }

   @Override
   public LookupEnvironment environment() {
      return this.environment;
   }

   @Override
   public boolean isRawSubstitution() {
      return false;
   }
}
