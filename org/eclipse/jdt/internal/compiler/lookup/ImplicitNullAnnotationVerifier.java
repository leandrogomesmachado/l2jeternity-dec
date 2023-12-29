package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ImplicitNullAnnotationVerifier {
   ImplicitNullAnnotationVerifier buddyImplicitNullAnnotationsVerifier;
   private boolean inheritNullAnnotations;
   protected LookupEnvironment environment;

   public ImplicitNullAnnotationVerifier(LookupEnvironment environment, boolean inheritNullAnnotations) {
      this.buddyImplicitNullAnnotationsVerifier = this;
      this.inheritNullAnnotations = inheritNullAnnotations;
      this.environment = environment;
   }

   ImplicitNullAnnotationVerifier(LookupEnvironment environment) {
      CompilerOptions options = environment.globalOptions;
      this.buddyImplicitNullAnnotationsVerifier = new ImplicitNullAnnotationVerifier(environment, options.inheritNullAnnotations);
      this.inheritNullAnnotations = options.inheritNullAnnotations;
      this.environment = environment;
   }

   public void checkImplicitNullAnnotations(MethodBinding currentMethod, AbstractMethodDeclaration srcMethod, boolean complain, Scope scope) {
      try {
         ReferenceBinding currentType = currentMethod.declaringClass;
         if (currentType.id == 1) {
            return;
         }

         boolean usesTypeAnnotations = scope.environment().usesNullTypeAnnotations();
         boolean needToApplyReturnNonNullDefault = currentMethod.hasNonNullDefaultFor(16, usesTypeAnnotations);
         boolean needToApplyParameterNonNullDefault = currentMethod.hasNonNullDefaultFor(8, usesTypeAnnotations);
         boolean needToApplyNonNullDefault = needToApplyReturnNonNullDefault | needToApplyParameterNonNullDefault;
         boolean isInstanceMethod = !currentMethod.isConstructor() && !currentMethod.isStatic();
         complain &= isInstanceMethod;
         if (needToApplyNonNullDefault || complain || this.inheritNullAnnotations && isInstanceMethod) {
            if (isInstanceMethod) {
               List superMethodList = new ArrayList();
               if (currentType instanceof SourceTypeBinding && !currentType.isHierarchyConnected() && !currentType.isAnonymousType()) {
                  ((SourceTypeBinding)currentType).scope.connectTypeHierarchy();
               }

               int paramLen = currentMethod.parameters.length;
               this.findAllOverriddenMethods(currentMethod.original(), currentMethod.selector, paramLen, currentType, new HashSet(), superMethodList);
               ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo[] inheritedNonNullnessInfos = new ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo[paramLen
                  + 1];

               for(int i = 0; i < paramLen + 1; ++i) {
                  inheritedNonNullnessInfos[i] = new ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo();
               }

               int length = superMethodList.size();

               for(int i = length; --i >= 0; needToApplyNonNullDefault = false) {
                  MethodBinding currentSuper = (MethodBinding)superMethodList.get(i);
                  if ((currentSuper.tagBits & 4096L) == 0L) {
                     this.checkImplicitNullAnnotations(currentSuper, null, false, scope);
                  }

                  this.checkNullSpecInheritance(
                     currentMethod,
                     srcMethod,
                     needToApplyReturnNonNullDefault,
                     needToApplyParameterNonNullDefault,
                     complain,
                     currentSuper,
                     null,
                     scope,
                     inheritedNonNullnessInfos
                  );
               }

               ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo info = inheritedNonNullnessInfos[0];
               if (!info.complained) {
                  long tagBits = 0L;
                  if (info.inheritedNonNullness == Boolean.TRUE) {
                     tagBits = 72057594037927936L;
                  } else if (info.inheritedNonNullness == Boolean.FALSE) {
                     tagBits = 36028797018963968L;
                  }

                  if (tagBits != 0L) {
                     if (!usesTypeAnnotations) {
                        currentMethod.tagBits |= tagBits;
                     } else if (!currentMethod.returnType.isBaseType()) {
                        LookupEnvironment env = scope.environment();
                        currentMethod.returnType = env.createAnnotatedType(currentMethod.returnType, env.nullAnnotationsFromTagBits(tagBits));
                     }
                  }
               }

               for(int i = 0; i < paramLen; ++i) {
                  info = inheritedNonNullnessInfos[i + 1];
                  if (!info.complained && info.inheritedNonNullness != null) {
                     Argument currentArg = srcMethod == null ? null : srcMethod.arguments[i];
                     if (!usesTypeAnnotations) {
                        this.recordArgNonNullness(currentMethod, paramLen, i, currentArg, info.inheritedNonNullness);
                     } else {
                        this.recordArgNonNullness18(currentMethod, i, currentArg, info.inheritedNonNullness, scope.environment());
                     }
                  }
               }
            }

            if (needToApplyNonNullDefault) {
               if (!usesTypeAnnotations) {
                  currentMethod.fillInDefaultNonNullness(srcMethod);
               } else {
                  currentMethod.fillInDefaultNonNullness18(srcMethod, scope.environment());
               }

               return;
            }

            return;
         }
      } finally {
         currentMethod.tagBits |= 4096L;
      }
   }

   private void findAllOverriddenMethods(
      MethodBinding original, char[] selector, int suggestedParameterLength, ReferenceBinding currentType, Set ifcsSeen, List result
   ) {
      if (currentType.id != 1) {
         ReferenceBinding superclass = currentType.superclass();
         if (superclass != null) {
            this.collectOverriddenMethods(original, selector, suggestedParameterLength, superclass, ifcsSeen, result);

            for(ReferenceBinding currentIfc : currentType.superInterfaces()) {
               if (ifcsSeen.add(currentIfc.original())) {
                  this.collectOverriddenMethods(original, selector, suggestedParameterLength, currentIfc, ifcsSeen, result);
               }
            }
         }
      }
   }

   private void collectOverriddenMethods(
      MethodBinding original, char[] selector, int suggestedParameterLength, ReferenceBinding superType, Set ifcsSeen, List result
   ) {
      MethodBinding[] ifcMethods = superType.getMethods(selector, suggestedParameterLength);
      int length = ifcMethods.length;
      boolean added = false;

      for(int i = 0; i < length; ++i) {
         MethodBinding currentMethod = ifcMethods[i];
         if (!currentMethod.isStatic() && MethodVerifier.doesMethodOverride(original, currentMethod, this.environment)) {
            result.add(currentMethod);
            added = true;
         }
      }

      if (!added) {
         this.findAllOverriddenMethods(original, selector, suggestedParameterLength, superType, ifcsSeen, result);
      }
   }

   void checkNullSpecInheritance(
      MethodBinding currentMethod,
      AbstractMethodDeclaration srcMethod,
      boolean hasReturnNonNullDefault,
      boolean hasParameterNonNullDefault,
      boolean shouldComplain,
      MethodBinding inheritedMethod,
      MethodBinding[] allInheritedMethods,
      Scope scope,
      ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo[] inheritedNonNullnessInfos
   ) {
      if ((inheritedMethod.tagBits & 4096L) == 0L) {
         this.buddyImplicitNullAnnotationsVerifier.checkImplicitNullAnnotations(inheritedMethod, null, false, scope);
      }

      boolean useTypeAnnotations;
      boolean shouldInherit;
      useTypeAnnotations = this.environment.usesNullTypeAnnotations();
      long inheritedNullnessBits = this.getReturnTypeNullnessTagBits(inheritedMethod, useTypeAnnotations);
      long currentNullnessBits = this.getReturnTypeNullnessTagBits(currentMethod, useTypeAnnotations);
      shouldInherit = this.inheritNullAnnotations;
      label248:
      if (currentMethod.returnType != null && !currentMethod.returnType.isBaseType()) {
         if (currentNullnessBits == 0L) {
            if (shouldInherit && inheritedNullnessBits != 0L) {
               if (hasReturnNonNullDefault && shouldComplain && inheritedNullnessBits == 36028797018963968L) {
                  scope.problemReporter().conflictingNullAnnotations(currentMethod, ((MethodDeclaration)srcMethod).returnType, inheritedMethod);
               }

               if (inheritedNonNullnessInfos != null && srcMethod != null) {
                  this.recordDeferredInheritedNullness(
                     scope,
                     ((MethodDeclaration)srcMethod).returnType,
                     inheritedMethod,
                     inheritedNullnessBits == 72057594037927936L,
                     inheritedNonNullnessInfos[0]
                  );
               } else {
                  this.applyReturnNullBits(currentMethod, inheritedNullnessBits);
               }
               break label248;
            }

            if (hasReturnNonNullDefault && (!useTypeAnnotations || currentMethod.returnType.acceptsNonNullDefault())) {
               currentNullnessBits = 72057594037927936L;
               this.applyReturnNullBits(currentMethod, currentNullnessBits);
            }
         }

         if (shouldComplain) {
            if ((inheritedNullnessBits & 72057594037927936L) != 0L && currentNullnessBits != 72057594037927936L) {
               if (srcMethod == null) {
                  scope.problemReporter().cannotImplementIncompatibleNullness(currentMethod, inheritedMethod, useTypeAnnotations);
                  return;
               }

               scope.problemReporter().illegalReturnRedefinition(srcMethod, inheritedMethod, this.environment.getNonNullAnnotationName());
            } else if (useTypeAnnotations) {
               TypeBinding substituteReturnType = null;
               TypeVariableBinding[] typeVariables = inheritedMethod.original().typeVariables;
               if (typeVariables != null && currentMethod.returnType.id != 6) {
                  ParameterizedGenericMethodBinding substitute = this.environment.createParameterizedGenericMethod(currentMethod, typeVariables);
                  substituteReturnType = substitute.returnType;
               }

               if (NullAnnotationMatching.analyse(
                     inheritedMethod.returnType,
                     currentMethod.returnType,
                     substituteReturnType,
                     null,
                     0,
                     null,
                     NullAnnotationMatching.CheckMode.OVERRIDE_RETURN
                  )
                  .isAnyMismatch()) {
                  if (srcMethod != null) {
                     scope.problemReporter().illegalReturnRedefinition(srcMethod, inheritedMethod, this.environment.getNonNullAnnotationName());
                  } else {
                     scope.problemReporter().cannotImplementIncompatibleNullness(currentMethod, inheritedMethod, useTypeAnnotations);
                  }

                  return;
               }
            }
         }
      }

      TypeBinding[] substituteParameters = null;
      if (shouldComplain) {
         TypeVariableBinding[] typeVariables = currentMethod.original().typeVariables;
         if (typeVariables != Binding.NO_TYPE_VARIABLES) {
            ParameterizedGenericMethodBinding substitute = this.environment.createParameterizedGenericMethod(inheritedMethod, typeVariables);
            substituteParameters = substitute.parameters;
         }
      }

      Argument[] currentArguments = srcMethod == null ? null : srcMethod.arguments;
      int length = 0;
      if (currentArguments != null) {
         length = currentArguments.length;
      }

      if (useTypeAnnotations) {
         length = currentMethod.parameters.length;
      } else if (inheritedMethod.parameterNonNullness != null) {
         length = inheritedMethod.parameterNonNullness.length;
      } else if (currentMethod.parameterNonNullness != null) {
         length = currentMethod.parameterNonNullness.length;
      }

      label224:
      for(int i = 0; i < length; ++i) {
         if (!currentMethod.parameters[i].isBaseType()) {
            Argument currentArgument = currentArguments == null ? null : currentArguments[i];
            Boolean inheritedNonNullNess = this.getParameterNonNullness(inheritedMethod, i, useTypeAnnotations);
            Boolean currentNonNullNess = this.getParameterNonNullness(currentMethod, i, useTypeAnnotations);
            if (currentNonNullNess == null) {
               if (inheritedNonNullNess != null && shouldInherit) {
                  if (hasParameterNonNullDefault && shouldComplain && inheritedNonNullNess == Boolean.FALSE && currentArgument != null) {
                     scope.problemReporter().conflictingNullAnnotations(currentMethod, currentArgument, inheritedMethod);
                  }

                  if (inheritedNonNullnessInfos != null && srcMethod != null) {
                     this.recordDeferredInheritedNullness(
                        scope, srcMethod.arguments[i].type, inheritedMethod, inheritedNonNullNess, inheritedNonNullnessInfos[i + 1]
                     );
                     continue;
                  }

                  if (!useTypeAnnotations) {
                     this.recordArgNonNullness(currentMethod, length, i, currentArgument, inheritedNonNullNess);
                  } else {
                     this.recordArgNonNullness18(currentMethod, i, currentArgument, inheritedNonNullNess, this.environment);
                  }
                  continue;
               }

               if (hasParameterNonNullDefault) {
                  currentNonNullNess = Boolean.TRUE;
                  if (!useTypeAnnotations) {
                     this.recordArgNonNullness(currentMethod, length, i, currentArgument, Boolean.TRUE);
                  } else if (currentMethod.parameters[i].acceptsNonNullDefault()) {
                     this.recordArgNonNullness18(currentMethod, i, currentArgument, Boolean.TRUE, this.environment);
                  } else {
                     currentNonNullNess = null;
                  }
               }
            }

            if (shouldComplain) {
               char[][] annotationName;
               if (inheritedNonNullNess == Boolean.TRUE) {
                  annotationName = this.environment.getNonNullAnnotationName();
               } else {
                  annotationName = this.environment.getNullableAnnotationName();
               }

               if (inheritedNonNullNess != Boolean.TRUE && currentNonNullNess == Boolean.TRUE) {
                  if (currentArgument != null) {
                     scope.problemReporter()
                        .illegalRedefinitionToNonNullParameter(
                           currentArgument, inheritedMethod.declaringClass, inheritedNonNullNess == null ? null : this.environment.getNullableAnnotationName()
                        );
                  } else {
                     scope.problemReporter().cannotImplementIncompatibleNullness(currentMethod, inheritedMethod, false);
                  }
               } else {
                  if (currentNonNullNess == null) {
                     if (inheritedNonNullNess == Boolean.FALSE) {
                        if (currentArgument != null) {
                           scope.problemReporter().parameterLackingNullableAnnotation(currentArgument, inheritedMethod.declaringClass, annotationName);
                        } else {
                           scope.problemReporter().cannotImplementIncompatibleNullness(currentMethod, inheritedMethod, false);
                        }
                        continue;
                     }

                     if (inheritedNonNullNess == Boolean.TRUE) {
                        if (allInheritedMethods != null) {
                           for(MethodBinding one : allInheritedMethods) {
                              if (TypeBinding.equalsEquals(inheritedMethod.declaringClass, one.declaringClass)
                                 && this.getParameterNonNullness(one, i, useTypeAnnotations) != Boolean.TRUE) {
                                 continue label224;
                              }
                           }
                        }

                        scope.problemReporter().parameterLackingNonnullAnnotation(currentArgument, inheritedMethod.declaringClass, annotationName);
                        continue;
                     }
                  }

                  if (useTypeAnnotations) {
                     TypeBinding inheritedParameter = inheritedMethod.parameters[i];
                     TypeBinding substituteParameter = substituteParameters != null ? substituteParameters[i] : null;
                     if (NullAnnotationMatching.analyse(
                           currentMethod.parameters[i], inheritedParameter, substituteParameter, null, 0, null, NullAnnotationMatching.CheckMode.OVERRIDE
                        )
                        .isAnyMismatch()) {
                        if (currentArgument != null) {
                           scope.problemReporter().illegalParameterRedefinition(currentArgument, inheritedMethod.declaringClass, inheritedParameter);
                        } else {
                           scope.problemReporter().cannotImplementIncompatibleNullness(currentMethod, inheritedMethod, false);
                        }
                     }
                  }
               }
            }
         }
      }

      if (shouldComplain && useTypeAnnotations && srcMethod != null) {
         TypeVariableBinding[] currentTypeVariables = currentMethod.typeVariables();
         TypeVariableBinding[] inheritedTypeVariables = inheritedMethod.typeVariables();
         if (currentTypeVariables != Binding.NO_TYPE_VARIABLES && currentTypeVariables.length == inheritedTypeVariables.length) {
            for(int i = 0; i < currentTypeVariables.length; ++i) {
               TypeVariableBinding inheritedVariable = inheritedTypeVariables[i];
               if (NullAnnotationMatching.analyse(
                     inheritedVariable, currentTypeVariables[i], null, null, -1, null, NullAnnotationMatching.CheckMode.BOUND_CHECK
                  )
                  .isAnyMismatch()) {
                  scope.problemReporter().cannotRedefineTypeArgumentNullity(inheritedVariable, inheritedMethod, srcMethod.typeParameters()[i]);
               }
            }
         }
      }
   }

   void applyReturnNullBits(MethodBinding method, long nullnessBits) {
      if (this.environment.usesNullTypeAnnotations()) {
         if (!method.returnType.isBaseType()) {
            method.returnType = this.environment.createAnnotatedType(method.returnType, this.environment.nullAnnotationsFromTagBits(nullnessBits));
         }
      } else {
         method.tagBits |= nullnessBits;
      }
   }

   private Boolean getParameterNonNullness(MethodBinding method, int i, boolean useTypeAnnotations) {
      if (useTypeAnnotations) {
         TypeBinding parameter = method.parameters[i];
         if (parameter != null) {
            long nullBits = NullAnnotationMatching.validNullTagBits(parameter.tagBits);
            if (nullBits != 0L) {
               return nullBits == 72057594037927936L;
            }
         }

         return null;
      } else {
         return method.parameterNonNullness == null ? null : method.parameterNonNullness[i];
      }
   }

   private long getReturnTypeNullnessTagBits(MethodBinding method, boolean useTypeAnnotations) {
      if (useTypeAnnotations) {
         return method.returnType == null ? 0L : NullAnnotationMatching.validNullTagBits(method.returnType.tagBits);
      } else {
         return method.tagBits & 108086391056891904L;
      }
   }

   protected void recordDeferredInheritedNullness(
      Scope scope,
      ASTNode location,
      MethodBinding inheritedMethod,
      Boolean inheritedNonNullness,
      ImplicitNullAnnotationVerifier.InheritedNonNullnessInfo nullnessInfo
   ) {
      if (nullnessInfo.inheritedNonNullness != null && nullnessInfo.inheritedNonNullness != inheritedNonNullness) {
         scope.problemReporter()
            .conflictingInheritedNullAnnotations(
               location, nullnessInfo.inheritedNonNullness, nullnessInfo.annotationOrigin, inheritedNonNullness, inheritedMethod
            );
         nullnessInfo.complained = true;
      } else {
         nullnessInfo.inheritedNonNullness = inheritedNonNullness;
         nullnessInfo.annotationOrigin = inheritedMethod;
      }
   }

   void recordArgNonNullness(MethodBinding method, int paramCount, int paramIdx, Argument currentArgument, Boolean nonNullNess) {
      if (method.parameterNonNullness == null) {
         method.parameterNonNullness = new Boolean[paramCount];
      }

      method.parameterNonNullness[paramIdx] = nonNullNess;
      if (currentArgument != null) {
         currentArgument.binding.tagBits |= nonNullNess ? 72057594037927936L : 36028797018963968L;
      }
   }

   void recordArgNonNullness18(MethodBinding method, int paramIdx, Argument currentArgument, Boolean nonNullNess, LookupEnvironment env) {
      AnnotationBinding annotationBinding = nonNullNess ? env.getNonNullAnnotation() : env.getNullableAnnotation();
      method.parameters[paramIdx] = env.createAnnotatedType(method.parameters[paramIdx], new AnnotationBinding[]{annotationBinding});
      if (currentArgument != null) {
         currentArgument.binding.type = method.parameters[paramIdx];
      }
   }

   static boolean areParametersEqual(MethodBinding one, MethodBinding two) {
      TypeBinding[] oneArgs = one.parameters;
      TypeBinding[] twoArgs = two.parameters;
      if (oneArgs == twoArgs) {
         return true;
      } else {
         int length = oneArgs.length;
         if (length != twoArgs.length) {
            return false;
         } else {
            int i;
            for(i = 0; i < length; ++i) {
               if (!areTypesEqual(oneArgs[i], twoArgs[i])) {
                  if (!oneArgs[i].leafComponentType().isRawType()
                     || oneArgs[i].dimensions() != twoArgs[i].dimensions()
                     || !oneArgs[i].leafComponentType().isEquivalentTo(twoArgs[i].leafComponentType())) {
                     return false;
                  }

                  if (one.typeVariables != Binding.NO_TYPE_VARIABLES) {
                     return false;
                  }

                  for(int j = 0; j < i; ++j) {
                     if (oneArgs[j].leafComponentType().isParameterizedTypeWithActualArguments()) {
                        return false;
                     }
                  }
                  break;
               }
            }

            ++i;

            for(; i < length; ++i) {
               if (!areTypesEqual(oneArgs[i], twoArgs[i])) {
                  if (!oneArgs[i].leafComponentType().isRawType()
                     || oneArgs[i].dimensions() != twoArgs[i].dimensions()
                     || !oneArgs[i].leafComponentType().isEquivalentTo(twoArgs[i].leafComponentType())) {
                     return false;
                  }
               } else if (oneArgs[i].leafComponentType().isParameterizedTypeWithActualArguments()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   static boolean areTypesEqual(TypeBinding one, TypeBinding two) {
      if (TypeBinding.equalsEquals(one, two)) {
         return true;
      } else {
         label33:
         switch(one.kind()) {
            case 4:
               switch(two.kind()) {
                  case 260:
                  case 1028:
                     if (TypeBinding.equalsEquals(one, two.erasure())) {
                        return true;
                     }
                  default:
                     break label33;
               }
            case 260:
            case 1028:
               switch(two.kind()) {
                  case 4:
                     if (TypeBinding.equalsEquals(one.erasure(), two)) {
                        return true;
                     }
               }
         }

         if (!one.isParameterizedType() || !two.isParameterizedType()) {
            return false;
         } else {
            return one.isEquivalentTo(two) && two.isEquivalentTo(one);
         }
      }
   }

   static class InheritedNonNullnessInfo {
      Boolean inheritedNonNullness;
      MethodBinding annotationOrigin;
      boolean complained;
   }
}
