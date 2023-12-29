package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;

public class ParameterizedMethodBinding extends MethodBinding {
   protected MethodBinding originalMethod;

   public ParameterizedMethodBinding(final ParameterizedTypeBinding parameterizedDeclaringClass, MethodBinding originalMethod) {
      super(
         originalMethod.modifiers,
         originalMethod.selector,
         originalMethod.returnType,
         originalMethod.parameters,
         originalMethod.thrownExceptions,
         parameterizedDeclaringClass
      );
      this.originalMethod = originalMethod;
      this.tagBits = originalMethod.tagBits & -129L;
      this.parameterNonNullness = originalMethod.parameterNonNullness;
      this.defaultNullness = originalMethod.defaultNullness;
      final TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
      Substitution substitution = null;
      final int length = originalVariables.length;
      final boolean isStatic = originalMethod.isStatic();
      if (length == 0) {
         this.typeVariables = Binding.NO_TYPE_VARIABLES;
         if (!isStatic) {
            substitution = parameterizedDeclaringClass;
         }
      } else {
         final TypeVariableBinding[] substitutedVariables = new TypeVariableBinding[length];

         for(int i = 0; i < length; ++i) {
            TypeVariableBinding originalVariable = originalVariables[i];
            substitutedVariables[i] = new TypeVariableBinding(
               originalVariable.sourceName, this, originalVariable.rank, parameterizedDeclaringClass.environment
            );
            substitutedVariables[i].tagBits |= originalVariable.tagBits & 108086391057940480L;
         }

         this.typeVariables = substitutedVariables;
         substitution = new Substitution() {
            @Override
            public LookupEnvironment environment() {
               return parameterizedDeclaringClass.environment;
            }

            @Override
            public boolean isRawSubstitution() {
               return !isStatic && parameterizedDeclaringClass.isRawSubstitution();
            }

            @Override
            public TypeBinding substitute(TypeVariableBinding typeVariable) {
               if (typeVariable.rank < length && TypeBinding.equalsEquals(originalVariables[typeVariable.rank], typeVariable)) {
                  TypeBinding substitute = substitutedVariables[typeVariable.rank];
                  return typeVariable.hasTypeAnnotations()
                     ? this.environment().createAnnotatedType(substitute, typeVariable.getTypeAnnotations())
                     : substitute;
               } else {
                  return (TypeBinding)(!isStatic ? parameterizedDeclaringClass.substitute(typeVariable) : typeVariable);
               }
            }
         };

         for(int i = 0; i < length; ++i) {
            TypeVariableBinding originalVariable = originalVariables[i];
            TypeVariableBinding substitutedVariable = substitutedVariables[i];
            TypeBinding substitutedSuperclass = Scope.substitute(substitution, originalVariable.superclass);
            ReferenceBinding[] substitutedInterfaces = Scope.substitute(substitution, originalVariable.superInterfaces);
            if (originalVariable.firstBound != null) {
               TypeBinding firstBound = (TypeBinding)(TypeBinding.equalsEquals(originalVariable.firstBound, originalVariable.superclass)
                  ? substitutedSuperclass
                  : substitutedInterfaces[0]);
               substitutedVariable.setFirstBound(firstBound);
            }

            switch(substitutedSuperclass.kind()) {
               case 68:
                  substitutedVariable.setSuperClass(parameterizedDeclaringClass.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                  substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                  break;
               default:
                  if (substitutedSuperclass.isInterface()) {
                     substitutedVariable.setSuperClass(parameterizedDeclaringClass.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                     int interfaceCount = substitutedInterfaces.length;
                     ReferenceBinding[] var24;
                     System.arraycopy(substitutedInterfaces, 0, var24 = new ReferenceBinding[interfaceCount + 1], 1, interfaceCount);
                     var24[0] = (ReferenceBinding)substitutedSuperclass;
                     substitutedVariable.setSuperInterfaces(var24);
                  } else {
                     substitutedVariable.setSuperClass((ReferenceBinding)substitutedSuperclass);
                     substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                  }
            }
         }
      }

      if (substitution != null) {
         this.returnType = Scope.substitute(substitution, this.returnType);
         this.parameters = Scope.substitute(substitution, this.parameters);
         this.thrownExceptions = Scope.substitute(substitution, this.thrownExceptions);
         if (this.thrownExceptions == null) {
            this.thrownExceptions = Binding.NO_EXCEPTIONS;
         }

         if (parameterizedDeclaringClass.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
            long returnNullBits = NullAnnotationMatching.validNullTagBits(this.returnType.tagBits);
            if (returnNullBits != 0L) {
               this.tagBits &= -108086391056891905L;
               this.tagBits |= returnNullBits;
            }

            int parametersLen = this.parameters.length;

            for(int i = 0; i < parametersLen; ++i) {
               long paramTagBits = NullAnnotationMatching.validNullTagBits(this.parameters[i].tagBits);
               if (paramTagBits != 0L) {
                  if (this.parameterNonNullness == null) {
                     this.parameterNonNullness = new Boolean[parametersLen];
                  }

                  this.parameterNonNullness[i] = paramTagBits == 72057594037927936L;
               }
            }
         }
      }

      if ((this.tagBits & 128L) == 0L) {
         if ((this.returnType.tagBits & 128L) != 0L) {
            this.tagBits |= 128L;
         } else {
            int i = 0;

            for(int max = this.parameters.length; i < max; ++i) {
               if ((this.parameters[i].tagBits & 128L) != 0L) {
                  this.tagBits |= 128L;
                  return;
               }
            }

            i = 0;

            for(int max = this.thrownExceptions.length; i < max; ++i) {
               if ((this.thrownExceptions[i].tagBits & 128L) != 0L) {
                  this.tagBits |= 128L;
                  break;
               }
            }
         }
      }
   }

   public ParameterizedMethodBinding(
      ReferenceBinding declaringClass, MethodBinding originalMethod, char[][] alternateParamaterNames, final LookupEnvironment environment
   ) {
      super(
         originalMethod.modifiers,
         originalMethod.selector,
         originalMethod.returnType,
         originalMethod.parameters,
         originalMethod.thrownExceptions,
         declaringClass
      );
      this.originalMethod = originalMethod;
      this.tagBits = originalMethod.tagBits & -129L;
      this.parameterNonNullness = originalMethod.parameterNonNullness;
      this.defaultNullness = originalMethod.defaultNullness;
      final TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
      Substitution substitution = null;
      final int length = originalVariables.length;
      if (length == 0) {
         this.typeVariables = Binding.NO_TYPE_VARIABLES;
      } else {
         final TypeVariableBinding[] substitutedVariables = new TypeVariableBinding[length];

         for(int i = 0; i < length; ++i) {
            TypeVariableBinding originalVariable = originalVariables[i];
            substitutedVariables[i] = new TypeVariableBinding(
               alternateParamaterNames == null ? originalVariable.sourceName : alternateParamaterNames[i], this, originalVariable.rank, environment
            );
            substitutedVariables[i].tagBits |= originalVariable.tagBits & 108086391057940480L;
         }

         this.typeVariables = substitutedVariables;
         substitution = new Substitution() {
            @Override
            public LookupEnvironment environment() {
               return environment;
            }

            @Override
            public boolean isRawSubstitution() {
               return false;
            }

            @Override
            public TypeBinding substitute(TypeVariableBinding typeVariable) {
               if (typeVariable.rank < length && TypeBinding.equalsEquals(originalVariables[typeVariable.rank], typeVariable)) {
                  TypeBinding substitute = substitutedVariables[typeVariable.rank];
                  return typeVariable.hasTypeAnnotations()
                     ? this.environment().createAnnotatedType(substitute, typeVariable.getTypeAnnotations())
                     : substitute;
               } else {
                  return typeVariable;
               }
            }
         };

         for(int i = 0; i < length; ++i) {
            TypeVariableBinding originalVariable = originalVariables[i];
            TypeVariableBinding substitutedVariable = substitutedVariables[i];
            TypeBinding substitutedSuperclass = Scope.substitute(substitution, originalVariable.superclass);
            ReferenceBinding[] substitutedInterfaces = Scope.substitute(substitution, originalVariable.superInterfaces);
            if (originalVariable.firstBound != null) {
               TypeBinding firstBound = (TypeBinding)(TypeBinding.equalsEquals(originalVariable.firstBound, originalVariable.superclass)
                  ? substitutedSuperclass
                  : substitutedInterfaces[0]);
               substitutedVariable.setFirstBound(firstBound);
            }

            switch(substitutedSuperclass.kind()) {
               case 68:
                  substitutedVariable.setSuperClass(environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                  substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                  break;
               default:
                  if (substitutedSuperclass.isInterface()) {
                     substitutedVariable.setSuperClass(environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                     int interfaceCount = substitutedInterfaces.length;
                     ReferenceBinding[] var21;
                     System.arraycopy(substitutedInterfaces, 0, var21 = new ReferenceBinding[interfaceCount + 1], 1, interfaceCount);
                     var21[0] = (ReferenceBinding)substitutedSuperclass;
                     substitutedVariable.setSuperInterfaces(var21);
                  } else {
                     substitutedVariable.setSuperClass((ReferenceBinding)substitutedSuperclass);
                     substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                  }
            }
         }
      }

      if (substitution != null) {
         this.returnType = Scope.substitute(substitution, this.returnType);
         this.parameters = Scope.substitute(substitution, this.parameters);
         this.thrownExceptions = Scope.substitute(substitution, this.thrownExceptions);
         if (this.thrownExceptions == null) {
            this.thrownExceptions = Binding.NO_EXCEPTIONS;
         }
      }

      if ((this.tagBits & 128L) == 0L) {
         if ((this.returnType.tagBits & 128L) != 0L) {
            this.tagBits |= 128L;
         } else {
            int i = 0;

            for(int max = this.parameters.length; i < max; ++i) {
               if ((this.parameters[i].tagBits & 128L) != 0L) {
                  this.tagBits |= 128L;
                  return;
               }
            }

            i = 0;

            for(int max = this.thrownExceptions.length; i < max; ++i) {
               if ((this.thrownExceptions[i].tagBits & 128L) != 0L) {
                  this.tagBits |= 128L;
                  break;
               }
            }
         }
      }
   }

   public ParameterizedMethodBinding() {
   }

   public static ParameterizedMethodBinding instantiateGetClass(TypeBinding receiverType, MethodBinding originalMethod, Scope scope) {
      ParameterizedMethodBinding method = new ParameterizedMethodBinding();
      method.modifiers = originalMethod.modifiers;
      method.selector = originalMethod.selector;
      method.declaringClass = originalMethod.declaringClass;
      method.typeVariables = Binding.NO_TYPE_VARIABLES;
      method.originalMethod = originalMethod;
      method.parameters = originalMethod.parameters;
      method.thrownExceptions = originalMethod.thrownExceptions;
      method.tagBits = originalMethod.tagBits;
      ReferenceBinding genericClassType = scope.getJavaLangClass();
      LookupEnvironment environment = scope.environment();
      TypeBinding rawType = environment.convertToRawType(receiverType.erasure(), false);
      if (environment.usesNullTypeAnnotations()) {
         rawType = environment.createAnnotatedType(rawType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
      }

      method.returnType = environment.createParameterizedType(
         genericClassType, new TypeBinding[]{environment.createWildcard(genericClassType, 0, rawType, null, 1)}, null
      );
      if (environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
         if (environment.usesNullTypeAnnotations()) {
            method.returnType = environment.createAnnotatedType(method.returnType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
         } else {
            method.tagBits |= 72057594037927936L;
         }
      }

      if ((method.returnType.tagBits & 128L) != 0L) {
         method.tagBits |= 128L;
      }

      return method;
   }

   @Override
   public boolean hasSubstitutedParameters() {
      return this.parameters != this.originalMethod.parameters;
   }

   @Override
   public boolean hasSubstitutedReturnType() {
      return this.returnType != this.originalMethod.returnType;
   }

   @Override
   public MethodBinding original() {
      return this.originalMethod.original();
   }

   @Override
   public MethodBinding shallowOriginal() {
      return this.originalMethod;
   }
}
