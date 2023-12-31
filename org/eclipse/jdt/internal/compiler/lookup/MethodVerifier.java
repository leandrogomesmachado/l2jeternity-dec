package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.Sorting;

public abstract class MethodVerifier extends ImplicitNullAnnotationVerifier {
   SourceTypeBinding type = null;
   HashtableOfObject inheritedMethods = null;
   HashtableOfObject currentMethods = null;
   HashtableOfObject inheritedOverriddenMethods = null;

   MethodVerifier(LookupEnvironment environment) {
      super(environment);
   }

   boolean areMethodsCompatible(MethodBinding one, MethodBinding two) {
      return areMethodsCompatible(one, two, this.environment);
   }

   static boolean areMethodsCompatible(MethodBinding one, MethodBinding two, LookupEnvironment environment) {
      one = one.original();
      two = one.findOriginalInheritedMethod(two);
      return two == null ? false : isParameterSubsignature(one, two, environment);
   }

   boolean areReturnTypesCompatible(MethodBinding one, MethodBinding two) {
      return areReturnTypesCompatible(one, two, this.type.scope.environment());
   }

   public static boolean areReturnTypesCompatible(MethodBinding one, MethodBinding two, LookupEnvironment environment) {
      if (TypeBinding.equalsEquals(one.returnType, two.returnType)) {
         return true;
      } else if (environment.globalOptions.sourceLevel >= 3211264L) {
         if (one.returnType.isBaseType()) {
            return false;
         } else {
            return !one.declaringClass.isInterface() && one.declaringClass.id == 1
               ? two.returnType.isCompatibleWith(one.returnType)
               : one.returnType.isCompatibleWith(two.returnType);
         }
      } else {
         return areTypesEqual(one.returnType.erasure(), two.returnType.erasure());
      }
   }

   boolean canSkipInheritedMethods() {
      if (this.type.superclass() != null && this.type.superclass().isAbstract()) {
         return false;
      } else {
         return this.type.superInterfaces() == Binding.NO_SUPERINTERFACES;
      }
   }

   boolean canSkipInheritedMethods(MethodBinding one, MethodBinding two) {
      return two == null || TypeBinding.equalsEquals(one.declaringClass, two.declaringClass);
   }

   void checkAbstractMethod(MethodBinding abstractMethod) {
      if (this.mustImplementAbstractMethod(abstractMethod.declaringClass)) {
         TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
         if (typeDeclaration != null) {
            MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(abstractMethod);
            missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, abstractMethod);
         } else {
            this.problemReporter().abstractMethodMustBeImplemented(this.type, abstractMethod);
         }
      }
   }

   void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length, MethodBinding[] allInheritedMethods) {
      if (this.type.isAnnotationType()) {
         this.problemReporter().annotationCannotOverrideMethod(currentMethod, methods[length - 1]);
      } else {
         CompilerOptions options = this.type.scope.compilerOptions();
         int[] overriddenInheritedMethods = length > 1 ? this.findOverriddenInheritedMethods(methods, length) : null;
         int i = length;

         label133:
         while(--i >= 0) {
            MethodBinding inheritedMethod = methods[i];
            if (overriddenInheritedMethods == null || overriddenInheritedMethods[i] == 0) {
               if (currentMethod.isStatic() != inheritedMethod.isStatic()) {
                  this.problemReporter(currentMethod).staticAndInstanceConflict(currentMethod, inheritedMethod);
                  continue;
               }

               if (inheritedMethod.isAbstract()) {
                  if (inheritedMethod.declaringClass.isInterface()) {
                     currentMethod.modifiers |= 536870912;
                  } else {
                     currentMethod.modifiers |= 805306368;
                  }
               } else if (inheritedMethod.isPublic() || !this.type.isInterface()) {
                  if (currentMethod.isDefaultMethod() && !inheritedMethod.isFinal() && inheritedMethod.declaringClass.id == 1) {
                     this.problemReporter(currentMethod).defaultMethodOverridesObjectMethod(currentMethod);
                  } else if (inheritedMethod.isDefaultMethod()) {
                     currentMethod.modifiers |= 536870912;
                  } else {
                     currentMethod.modifiers |= 268435456;
                  }
               }

               if (!this.areReturnTypesCompatible(currentMethod, inheritedMethod)
                  && (currentMethod.returnType.tagBits & 128L) == 0L
                  && this.reportIncompatibleReturnTypeError(currentMethod, inheritedMethod)) {
                  continue;
               }

               this.reportRawReferences(currentMethod, inheritedMethod);
               if (currentMethod.thrownExceptions != Binding.NO_EXCEPTIONS) {
                  this.checkExceptions(currentMethod, inheritedMethod);
               }

               if (inheritedMethod.isFinal()) {
                  this.problemReporter(currentMethod).finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
               }

               if (!this.isAsVisible(currentMethod, inheritedMethod)) {
                  this.problemReporter(currentMethod).visibilityConflict(currentMethod, inheritedMethod);
               }

               if (inheritedMethod.isSynchronized() && !currentMethod.isSynchronized()) {
                  this.problemReporter(currentMethod).missingSynchronizedOnInheritedMethod(currentMethod, inheritedMethod);
               }

               if (options.reportDeprecationWhenOverridingDeprecatedMethod
                  && inheritedMethod.isViewedAsDeprecated()
                  && (!currentMethod.isViewedAsDeprecated() || options.reportDeprecationInsideDeprecatedCode)) {
                  ReferenceBinding declaringClass = inheritedMethod.declaringClass;
                  if (declaringClass.isInterface()) {
                     int j = length;

                     while(--j >= 0) {
                        if (i != j && methods[j].declaringClass.implementsInterface(declaringClass, false)) {
                           continue label133;
                        }
                     }
                  }

                  this.problemReporter(currentMethod).overridesDeprecatedMethod(currentMethod, inheritedMethod);
               }
            }

            if (!inheritedMethod.isStatic() && !inheritedMethod.isFinal()) {
               this.checkForBridgeMethod(currentMethod, inheritedMethod, allInheritedMethods);
            }
         }

         MethodBinding[] overridden = (MethodBinding[])this.inheritedOverriddenMethods.get(currentMethod.selector);
         if (overridden != null) {
            int ix = overridden.length;

            while(--ix >= 0) {
               MethodBinding inheritedMethod = overridden[ix];
               if (this.isParameterSubsignature(currentMethod, inheritedMethod) && !inheritedMethod.isStatic() && !inheritedMethod.isFinal()) {
                  this.checkForBridgeMethod(currentMethod, inheritedMethod, allInheritedMethods);
               }
            }
         }
      }
   }

   void addBridgeMethodCandidate(MethodBinding overriddenMethod) {
      MethodBinding[] existing = (MethodBinding[])this.inheritedOverriddenMethods.get(overriddenMethod.selector);
      if (existing == null) {
         existing = new MethodBinding[]{overriddenMethod};
      } else {
         int length = existing.length;
         System.arraycopy(existing, 0, existing = new MethodBinding[length + 1], 0, length);
         existing[length] = overriddenMethod;
      }

      this.inheritedOverriddenMethods.put(overriddenMethod.selector, existing);
   }

   public void reportRawReferences(MethodBinding currentMethod, MethodBinding inheritedMethod) {
   }

   void checkConcreteInheritedMethod(MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
      if (concreteMethod.isStatic()) {
         this.problemReporter().staticInheritedMethodConflicts(this.type, concreteMethod, abstractMethods);
      }

      if (!concreteMethod.isPublic()) {
         int index = 0;
         int length = abstractMethods.length;
         if (concreteMethod.isProtected()) {
            while(index < length && !abstractMethods[index].isPublic()) {
               ++index;
            }
         } else if (concreteMethod.isDefault()) {
            while(index < length && abstractMethods[index].isDefault()) {
               ++index;
            }
         }

         if (index < length) {
            this.problemReporter().inheritedMethodReducesVisibility(this.type, concreteMethod, abstractMethods);
         }
      }

      if (concreteMethod.thrownExceptions != Binding.NO_EXCEPTIONS) {
         int i = abstractMethods.length;

         while(--i >= 0) {
            this.checkExceptions(concreteMethod, abstractMethods[i]);
         }
      }

      if (concreteMethod.isOrEnclosedByPrivateType()) {
         MethodBinding var10000 = concreteMethod.original();
         var10000.modifiers |= 134217728;
      }
   }

   void checkExceptions(MethodBinding newMethod, MethodBinding inheritedMethod) {
      ReferenceBinding[] newExceptions = this.resolvedExceptionTypesFor(newMethod);
      ReferenceBinding[] inheritedExceptions = this.resolvedExceptionTypesFor(inheritedMethod);
      int i = newExceptions.length;

      while(--i >= 0) {
         ReferenceBinding newException = newExceptions[i];
         int j = inheritedExceptions.length;

         do {
            --j;
         } while(j > -1 && !this.isSameClassOrSubclassOf(newException, inheritedExceptions[j]));

         if (j == -1 && !newException.isUncheckedException(false) && (newException.tagBits & 128L) == 0L) {
            this.problemReporter(newMethod).incompatibleExceptionInThrowsClause(this.type, newMethod, inheritedMethod, newException);
         }
      }
   }

   void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod, MethodBinding[] allInheritedMethods) {
   }

   void checkForMissingHashCodeMethod() {
      MethodBinding[] choices = this.type.getMethods(TypeConstants.EQUALS);
      boolean overridesEquals = false;

      for(int i = choices.length; !overridesEquals; overridesEquals = choices[i].parameters.length == 1 && choices[i].parameters[0].id == 1) {
         if (--i < 0) {
            break;
         }
      }

      if (overridesEquals) {
         MethodBinding hashCodeMethod = this.type.getExactMethod(TypeConstants.HASHCODE, Binding.NO_PARAMETERS, null);
         if (hashCodeMethod != null && hashCodeMethod.declaringClass.id == 1) {
            this.problemReporter().shouldImplementHashcode(this.type);
         }
      }
   }

   void checkForRedundantSuperinterfaces(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
      if (superInterfaces != Binding.NO_SUPERINTERFACES) {
         SimpleSet interfacesToCheck = new SimpleSet(superInterfaces.length);
         SimpleSet redundantInterfaces = null;
         int i = 0;

         for(int l = superInterfaces.length; i < l; ++i) {
            ReferenceBinding toCheck = superInterfaces[i];

            for(int j = 0; j < l; ++j) {
               ReferenceBinding implementedInterface = superInterfaces[j];
               if (i != j && toCheck.implementsInterface(implementedInterface, true)) {
                  if (redundantInterfaces == null) {
                     redundantInterfaces = new SimpleSet(3);
                  } else if (redundantInterfaces.includes(implementedInterface)) {
                     continue;
                  }

                  redundantInterfaces.add(implementedInterface);
                  TypeReference[] refs = this.type.scope.referenceContext.superInterfaces;
                  int r = 0;

                  for(int rl = refs.length; r < rl; ++r) {
                     if (TypeBinding.equalsEquals(refs[r].resolvedType, toCheck)) {
                        this.problemReporter().redundantSuperInterface(this.type, refs[j], implementedInterface, toCheck);
                        break;
                     }
                  }
               }
            }

            interfacesToCheck.add(toCheck);
         }

         ReferenceBinding[] itsInterfaces = null;
         SimpleSet inheritedInterfaces = new SimpleSet(5);

         for(ReferenceBinding superType = superclass; superType != null && superType.isValidBinding(); superType = superType.superclass()) {
            ReferenceBinding[] var18;
            if ((var18 = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
               int ix = 0;

               for(int l = var18.length; ix < l; ++ix) {
                  ReferenceBinding inheritedInterface = var18[ix];
                  if (!inheritedInterfaces.includes(inheritedInterface) && inheritedInterface.isValidBinding()) {
                     if (interfacesToCheck.includes(inheritedInterface)) {
                        if (redundantInterfaces == null) {
                           redundantInterfaces = new SimpleSet(3);
                        } else if (redundantInterfaces.includes(inheritedInterface)) {
                           continue;
                        }

                        redundantInterfaces.add(inheritedInterface);
                        TypeReference[] refs = this.type.scope.referenceContext.superInterfaces;
                        int r = 0;

                        for(int rl = refs.length; r < rl; ++r) {
                           if (TypeBinding.equalsEquals(refs[r].resolvedType, inheritedInterface)) {
                              this.problemReporter().redundantSuperInterface(this.type, refs[r], inheritedInterface, superType);
                              break;
                           }
                        }
                     } else {
                        inheritedInterfaces.add(inheritedInterface);
                     }
                  }
               }
            }
         }

         int nextPosition = inheritedInterfaces.elementSize;
         if (nextPosition != 0) {
            ReferenceBinding[] interfacesToVisit = new ReferenceBinding[nextPosition];
            inheritedInterfaces.asArray(interfacesToVisit);

            for(int ix = 0; ix < nextPosition; ++ix) {
               ReferenceBinding var22 = interfacesToVisit[ix];
               ReferenceBinding[] var19;
               if ((var19 = var22.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
                  int itsLength = var19.length;
                  if (nextPosition + itsLength >= interfacesToVisit.length) {
                     System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                  }

                  for(int a = 0; a < itsLength; ++a) {
                     ReferenceBinding inheritedInterface = var19[a];
                     if (!inheritedInterfaces.includes(inheritedInterface) && inheritedInterface.isValidBinding()) {
                        if (interfacesToCheck.includes(inheritedInterface)) {
                           if (redundantInterfaces == null) {
                              redundantInterfaces = new SimpleSet(3);
                           } else if (redundantInterfaces.includes(inheritedInterface)) {
                              continue;
                           }

                           redundantInterfaces.add(inheritedInterface);
                           TypeReference[] refs = this.type.scope.referenceContext.superInterfaces;
                           int r = 0;

                           for(int rl = refs.length; r < rl; ++r) {
                              if (TypeBinding.equalsEquals(refs[r].resolvedType, inheritedInterface)) {
                                 this.problemReporter().redundantSuperInterface(this.type, refs[r], inheritedInterface, var22);
                                 break;
                              }
                           }
                        } else {
                           inheritedInterfaces.add(inheritedInterface);
                           interfacesToVisit[nextPosition++] = inheritedInterface;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void checkInheritedMethods(MethodBinding[] methods, int length, boolean[] isOverridden, boolean[] isInherited) {
      MethodBinding concreteMethod = !this.type.isInterface() && !methods[0].isAbstract() ? methods[0] : null;
      if (concreteMethod == null) {
         MethodBinding bestAbstractMethod = length == 1 ? methods[0] : this.findBestInheritedAbstractOrDefaultMethod(methods, length);
         boolean noMatch = bestAbstractMethod == null;
         if (noMatch) {
            bestAbstractMethod = methods[0];
         }

         if (this.mustImplementAbstractMethod(bestAbstractMethod.declaringClass)) {
            TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
            MethodBinding superclassAbstractMethod = methods[0];
            if (superclassAbstractMethod != bestAbstractMethod && !superclassAbstractMethod.declaringClass.isInterface()) {
               if (typeDeclaration != null) {
                  MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(bestAbstractMethod);
                  missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, bestAbstractMethod, superclassAbstractMethod);
               } else {
                  this.problemReporter().abstractMethodMustBeImplemented(this.type, bestAbstractMethod, superclassAbstractMethod);
               }
            } else if (typeDeclaration != null) {
               MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(bestAbstractMethod);
               missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, bestAbstractMethod);
            } else {
               this.problemReporter().abstractMethodMustBeImplemented(this.type, bestAbstractMethod);
            }
         } else if (noMatch) {
            this.problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length, isOverridden);
         }
      } else if (length >= 2) {
         int index = length;

         do {
            --index;
         } while(index > 0 && this.checkInheritedReturnTypes(concreteMethod, methods[index]));

         if (index > 0) {
            MethodBinding bestAbstractMethod = this.findBestInheritedAbstractOrDefaultMethod(methods, length);
            if (bestAbstractMethod == null) {
               this.problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length, isOverridden);
            } else {
               this.problemReporter().abstractMethodMustBeImplemented(this.type, bestAbstractMethod, concreteMethod);
            }
         } else {
            MethodBinding[] abstractMethods = new MethodBinding[length - 1];
            index = 0;

            for(int i = 0; i < length; ++i) {
               if (methods[i].isAbstract() || methods[i] != concreteMethod && methods[i].isDefaultMethod()) {
                  abstractMethods[index++] = methods[i];
               }
            }

            if (index != 0) {
               if (index < abstractMethods.length) {
                  System.arraycopy(abstractMethods, 0, abstractMethods = new MethodBinding[index], 0, index);
               }

               this.checkConcreteInheritedMethod(concreteMethod, abstractMethods);
            }
         }
      }
   }

   boolean checkInheritedReturnTypes(MethodBinding method, MethodBinding otherMethod) {
      if (this.areReturnTypesCompatible(method, otherMethod)) {
         return true;
      } else {
         return !this.type.isInterface()
            && (method.declaringClass.isClass() || !this.type.implementsInterface(method.declaringClass, false))
            && (otherMethod.declaringClass.isClass() || !this.type.implementsInterface(otherMethod.declaringClass, false));
      }
   }

   abstract void checkMethods();

   void checkPackagePrivateAbstractMethod(MethodBinding abstractMethod) {
      PackageBinding necessaryPackage = abstractMethod.declaringClass.fPackage;
      if (necessaryPackage != this.type.fPackage) {
         ReferenceBinding superType = this.type.superclass();
         char[] selector = abstractMethod.selector;

         while(superType.isValidBinding()) {
            if (!superType.isAbstract()) {
               return;
            }

            if (necessaryPackage == superType.fPackage) {
               MethodBinding[] methods = superType.getMethods(selector);
               int m = methods.length;

               while(--m >= 0) {
                  MethodBinding method = methods[m];
                  if (!method.isPrivate() && !method.isConstructor() && !method.isDefaultAbstract() && this.areMethodsCompatible(method, abstractMethod)) {
                     return;
                  }
               }
            }

            if (!TypeBinding.notEquals(superType = superType.superclass(), abstractMethod.declaringClass)) {
               this.problemReporter().abstractMethodCannotBeOverridden(this.type, abstractMethod);
               return;
            }
         }
      }
   }

   void computeInheritedMethods() {
      ReferenceBinding superclass = this.type.isInterface() ? this.type.scope.getJavaLangObject() : this.type.superclass();
      this.computeInheritedMethods(superclass, this.type.superInterfaces());
      this.checkForRedundantSuperinterfaces(superclass, this.type.superInterfaces());
   }

   void computeInheritedMethods(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
      this.inheritedMethods = new HashtableOfObject(51);
      this.inheritedOverriddenMethods = new HashtableOfObject(11);
      ReferenceBinding superType = superclass;
      HashtableOfObject nonVisibleDefaultMethods = new HashtableOfObject(3);

      label213:
      while(superType != null && superType.isValidBinding()) {
         MethodBinding[] methods = superType.unResolvedMethods();
         int m = methods.length;

         while(true) {
            MethodBinding inheritedMethod;
            MethodBinding existingMethod;
            label209:
            while(true) {
               if (--m < 0) {
                  superType = superType.superclass();
                  continue label213;
               }

               inheritedMethod = methods[m];
               if (!inheritedMethod.isPrivate() && !inheritedMethod.isConstructor() && !inheritedMethod.isDefaultAbstract()) {
                  MethodBinding[] existingMethods = (MethodBinding[])this.inheritedMethods.get(inheritedMethod.selector);
                  if (existingMethods != null) {
                     int i = 0;

                     for(int length = existingMethods.length; i < length; ++i) {
                        existingMethod = existingMethods[i];
                        if (TypeBinding.notEquals(existingMethod.declaringClass, inheritedMethod.declaringClass)
                           && this.areMethodsCompatible(existingMethod, inheritedMethod)
                           && !this.canOverridingMethodDifferInErasure(existingMethod, inheritedMethod)) {
                           if (!inheritedMethod.isDefault()) {
                              break label209;
                           }

                           if (inheritedMethod.isAbstract()) {
                              this.checkPackagePrivateAbstractMethod(inheritedMethod);
                              break label209;
                           }

                           if (existingMethod.declaringClass.fPackage == inheritedMethod.declaringClass.fPackage
                              || this.type.fPackage != inheritedMethod.declaringClass.fPackage
                              || this.areReturnTypesCompatible(inheritedMethod, existingMethod)) {
                              break label209;
                           }
                        }
                     }
                  }

                  if (inheritedMethod.isDefault() && inheritedMethod.declaringClass.fPackage != this.type.fPackage) {
                     MethodBinding[] nonVisible = (MethodBinding[])nonVisibleDefaultMethods.get(inheritedMethod.selector);
                     if (nonVisible != null && inheritedMethod.isAbstract()) {
                        int i = 0;

                        for(int l = nonVisible.length; i < l; ++i) {
                           if (this.areMethodsCompatible(nonVisible[i], inheritedMethod)) {
                              continue label209;
                           }
                        }
                     }

                     if (nonVisible == null) {
                        nonVisible = new MethodBinding[]{inheritedMethod};
                     } else {
                        int length = nonVisible.length;
                        System.arraycopy(nonVisible, 0, nonVisible = new MethodBinding[length + 1], 0, length);
                        nonVisible[length] = inheritedMethod;
                     }

                     nonVisibleDefaultMethods.put(inheritedMethod.selector, nonVisible);
                     if (inheritedMethod.isAbstract() && !this.type.isAbstract()) {
                        this.problemReporter().abstractMethodCannotBeOverridden(this.type, inheritedMethod);
                     }

                     MethodBinding[] current = (MethodBinding[])this.currentMethods.get(inheritedMethod.selector);
                     if (current != null && !inheritedMethod.isStatic()) {
                        int i = 0;

                        for(int length = current.length; i < length; ++i) {
                           if (!current[i].isStatic() && this.areMethodsCompatible(current[i], inheritedMethod)) {
                              this.problemReporter().overridesPackageDefaultMethod(current[i], inheritedMethod);
                              break;
                           }
                        }
                     }
                  } else {
                     if (existingMethods == null) {
                        existingMethods = new MethodBinding[]{inheritedMethod};
                     } else {
                        int length = existingMethods.length;
                        System.arraycopy(existingMethods, 0, existingMethods = new MethodBinding[length + 1], 0, length);
                        existingMethods[length] = inheritedMethod;
                     }

                     this.inheritedMethods.put(inheritedMethod.selector, existingMethods);
                  }
               }
            }

            if (TypeBinding.notEquals(inheritedMethod.returnType.erasure(), existingMethod.returnType.erasure())
               && this.areReturnTypesCompatible(existingMethod, inheritedMethod)) {
               this.addBridgeMethodCandidate(inheritedMethod);
            }
         }
      }

      List superIfcList = new ArrayList();
      HashSet seenTypes = new HashSet();
      this.collectAllDistinctSuperInterfaces(superInterfaces, seenTypes, superIfcList);

      for(ReferenceBinding currentSuper = superclass; currentSuper != null && currentSuper.id != 1; currentSuper = currentSuper.superclass()) {
         this.collectAllDistinctSuperInterfaces(currentSuper.superInterfaces(), seenTypes, superIfcList);
      }

      if (superIfcList.size() != 0) {
         if (superIfcList.size() == 1) {
            superInterfaces = new ReferenceBinding[]{(ReferenceBinding)superIfcList.get(0)};
         } else {
            superInterfaces = superIfcList.toArray(new ReferenceBinding[superIfcList.size()]);
            superInterfaces = Sorting.sortTypes(superInterfaces);
         }

         SimpleSet skip = this.findSuperinterfaceCollisions(superclass, superInterfaces);
         int len = superInterfaces.length;

         for(int i = len - 1; i >= 0; --i) {
            superType = superInterfaces[i];
            if (superType.isValidBinding() && (skip == null || !skip.includes(superType))) {
               MethodBinding[] methods = superType.unResolvedMethods();
               int m = methods.length;

               label131:
               while(--m >= 0) {
                  MethodBinding inheritedMethod = methods[m];
                  if (!inheritedMethod.isStatic()) {
                     MethodBinding[] existingMethods = (MethodBinding[])this.inheritedMethods.get(inheritedMethod.selector);
                     if (existingMethods == null) {
                        existingMethods = new MethodBinding[]{inheritedMethod};
                     } else {
                        int length = existingMethods.length;

                        for(int e = 0; e < length; ++e) {
                           if (this.isInterfaceMethodImplemented(inheritedMethod, existingMethods[e], superType)) {
                              if (TypeBinding.notEquals(inheritedMethod.returnType.erasure(), existingMethods[e].returnType.erasure())) {
                                 this.addBridgeMethodCandidate(inheritedMethod);
                              }

                              if (!this.canOverridingMethodDifferInErasure(existingMethods[e], inheritedMethod)) {
                                 continue label131;
                              }
                           }
                        }

                        System.arraycopy(existingMethods, 0, existingMethods = new MethodBinding[length + 1], 0, length);
                        existingMethods[length] = inheritedMethod;
                     }

                     this.inheritedMethods.put(inheritedMethod.selector, existingMethods);
                  }
               }
            }
         }
      }
   }

   void collectAllDistinctSuperInterfaces(ReferenceBinding[] superInterfaces, Set seen, List result) {
      for(ReferenceBinding superInterface : superInterfaces) {
         if (seen.add(superInterface)) {
            result.add(superInterface);
            this.collectAllDistinctSuperInterfaces(superInterface.superInterfaces(), seen, result);
         }
      }
   }

   protected boolean canOverridingMethodDifferInErasure(MethodBinding overridingMethod, MethodBinding inheritedMethod) {
      return false;
   }

   void computeMethods() {
      MethodBinding[] methods = this.type.methods();
      int size = methods.length;
      this.currentMethods = new HashtableOfObject(size == 0 ? 1 : size);
      int m = size;

      while(--m >= 0) {
         MethodBinding method = methods[m];
         if (!method.isConstructor() && !method.isDefaultAbstract()) {
            MethodBinding[] existingMethods = (MethodBinding[])this.currentMethods.get(method.selector);
            if (existingMethods == null) {
               existingMethods = new MethodBinding[1];
            } else {
               System.arraycopy(existingMethods, 0, existingMethods = new MethodBinding[existingMethods.length + 1], 0, existingMethods.length - 1);
            }

            existingMethods[existingMethods.length - 1] = method;
            this.currentMethods.put(method.selector, existingMethods);
         }
      }
   }

   MethodBinding computeSubstituteMethod(MethodBinding inheritedMethod, MethodBinding currentMethod) {
      return computeSubstituteMethod(inheritedMethod, currentMethod, this.environment);
   }

   public static MethodBinding computeSubstituteMethod(MethodBinding inheritedMethod, MethodBinding currentMethod, LookupEnvironment environment) {
      if (inheritedMethod == null) {
         return null;
      } else if (currentMethod.parameters.length != inheritedMethod.parameters.length) {
         return null;
      } else {
         if (currentMethod.declaringClass instanceof BinaryTypeBinding) {
            ((BinaryTypeBinding)currentMethod.declaringClass).resolveTypesFor(currentMethod);
         }

         if (inheritedMethod.declaringClass instanceof BinaryTypeBinding) {
            ((BinaryTypeBinding)inheritedMethod.declaringClass).resolveTypesFor(inheritedMethod);
         }

         TypeVariableBinding[] inheritedTypeVariables = inheritedMethod.typeVariables;
         int inheritedLength = inheritedTypeVariables.length;
         if (inheritedLength == 0) {
            return inheritedMethod;
         } else {
            TypeVariableBinding[] typeVariables = currentMethod.typeVariables;
            int length = typeVariables.length;
            if (length == 0) {
               return inheritedMethod.asRawMethod(environment);
            } else if (length != inheritedLength) {
               return inheritedMethod;
            } else {
               TypeBinding[] arguments = new TypeBinding[length];
               System.arraycopy(typeVariables, 0, arguments, 0, length);
               ParameterizedGenericMethodBinding substitute = environment.createParameterizedGenericMethod(inheritedMethod, arguments);

               for(int i = 0; i < inheritedLength; ++i) {
                  TypeVariableBinding inheritedTypeVariable = inheritedTypeVariables[i];
                  TypeVariableBinding typeVariable = (TypeVariableBinding)arguments[i];
                  if (TypeBinding.equalsEquals(typeVariable.firstBound, inheritedTypeVariable.firstBound)) {
                     if (typeVariable.firstBound == null) {
                        continue;
                     }
                  } else if (typeVariable.firstBound != null
                     && inheritedTypeVariable.firstBound != null
                     && typeVariable.firstBound.isClass() != inheritedTypeVariable.firstBound.isClass()) {
                     return inheritedMethod;
                  }

                  if (TypeBinding.notEquals(Scope.substitute(substitute, inheritedTypeVariable.superclass), typeVariable.superclass)) {
                     return inheritedMethod;
                  }

                  int interfaceLength = inheritedTypeVariable.superInterfaces.length;
                  ReferenceBinding[] interfaces = typeVariable.superInterfaces;
                  if (interfaceLength != interfaces.length) {
                     return inheritedMethod;
                  }

                  label79:
                  for(int j = 0; j < interfaceLength; ++j) {
                     TypeBinding superType = Scope.substitute(substitute, inheritedTypeVariable.superInterfaces[j]);

                     for(int k = 0; k < interfaceLength; ++k) {
                        if (TypeBinding.equalsEquals(superType, interfaces[k])) {
                           continue label79;
                        }
                     }

                     return inheritedMethod;
                  }
               }

               return substitute;
            }
         }
      }
   }

   static boolean couldMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
      if (!CharOperation.equals(method.selector, inheritedMethod.selector)) {
         return false;
      } else if (method == inheritedMethod || method.isStatic() || inheritedMethod.isStatic()) {
         return false;
      } else if (inheritedMethod.isPrivate()) {
         return false;
      } else if (inheritedMethod.isDefault() && method.declaringClass.getPackage() != inheritedMethod.declaringClass.getPackage()) {
         return false;
      } else {
         if (!method.isPublic()) {
            if (inheritedMethod.isPublic()) {
               return false;
            }

            if (inheritedMethod.isProtected() && !method.isProtected()) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
      return doesMethodOverride(method, inheritedMethod, this.environment);
   }

   public static boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod, LookupEnvironment environment) {
      return couldMethodOverride(method, inheritedMethod) && areMethodsCompatible(method, inheritedMethod, environment);
   }

   SimpleSet findSuperinterfaceCollisions(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
      return null;
   }

   MethodBinding findBestInheritedAbstractOrDefaultMethod(MethodBinding[] methods, int length) {
      label37:
      for(int i = 0; i < length; ++i) {
         MethodBinding method = methods[i];
         if (method.isAbstract() || method.isDefaultMethod()) {
            for(int j = 0; j < length; ++j) {
               if (i != j && !this.checkInheritedReturnTypes(method, methods[j])) {
                  if (this.type.isInterface() && methods[j].declaringClass.id == 1) {
                     return method;
                  }
                  continue label37;
               }
            }

            return method;
         }
      }

      return null;
   }

   int[] findOverriddenInheritedMethods(MethodBinding[] methods, int length) {
      int[] toSkip = null;
      int i = 0;
      ReferenceBinding declaringClass = methods[i].declaringClass;
      if (!declaringClass.isInterface()) {
         ReferenceBinding declaringClass2;
         for(declaringClass2 = methods[++i].declaringClass;
            TypeBinding.equalsEquals(declaringClass, declaringClass2);
            declaringClass2 = methods[i].declaringClass
         ) {
            if (++i == length) {
               return null;
            }
         }

         if (!declaringClass2.isInterface()) {
            if (declaringClass.fPackage != declaringClass2.fPackage && methods[i].isDefault()) {
               return null;
            }

            toSkip = new int[length];

            do {
               toSkip[i] = -1;
               if (++i == length) {
                  return toSkip;
               }

               declaringClass2 = methods[i].declaringClass;
            } while(!declaringClass2.isInterface());
         }
      }

      for(; i < length; ++i) {
         if (toSkip == null || toSkip[i] != -1) {
            declaringClass = methods[i].declaringClass;

            for(int j = i + 1; j < length; ++j) {
               if (toSkip == null || toSkip[j] != -1) {
                  ReferenceBinding declaringClass2 = methods[j].declaringClass;
                  if (!TypeBinding.equalsEquals(declaringClass, declaringClass2)) {
                     if (declaringClass.implementsInterface(declaringClass2, true)) {
                        if (toSkip == null) {
                           toSkip = new int[length];
                        }

                        toSkip[j] = -1;
                     } else if (declaringClass2.implementsInterface(declaringClass, true)) {
                        if (toSkip == null) {
                           toSkip = new int[length];
                        }

                        toSkip[i] = -1;
                        break;
                     }
                  }
               }
            }
         }
      }

      return toSkip;
   }

   boolean isAsVisible(MethodBinding newMethod, MethodBinding inheritedMethod) {
      if (inheritedMethod.modifiers == newMethod.modifiers) {
         return true;
      } else if (newMethod.isPublic()) {
         return true;
      } else if (inheritedMethod.isPublic()) {
         return false;
      } else if (newMethod.isProtected()) {
         return true;
      } else if (inheritedMethod.isProtected()) {
         return false;
      } else {
         return !newMethod.isPrivate();
      }
   }

   boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
      return areParametersEqual(existingMethod, inheritedMethod) && existingMethod.declaringClass.implementsInterface(superType, true);
   }

   public boolean isMethodSubsignature(MethodBinding method, MethodBinding inheritedMethod) {
      return CharOperation.equals(method.selector, inheritedMethod.selector) && this.isParameterSubsignature(method, inheritedMethod);
   }

   boolean isParameterSubsignature(MethodBinding method, MethodBinding inheritedMethod) {
      return isParameterSubsignature(method, inheritedMethod, this.environment);
   }

   static boolean isParameterSubsignature(MethodBinding method, MethodBinding inheritedMethod, LookupEnvironment environment) {
      MethodBinding substitute = computeSubstituteMethod(inheritedMethod, method, environment);
      return substitute != null && isSubstituteParameterSubsignature(method, substitute, environment);
   }

   boolean isSubstituteParameterSubsignature(MethodBinding method, MethodBinding substituteMethod) {
      return isSubstituteParameterSubsignature(method, substituteMethod, this.environment);
   }

   public static boolean isSubstituteParameterSubsignature(MethodBinding method, MethodBinding substituteMethod, LookupEnvironment environment) {
      if (!areParametersEqual(method, substituteMethod)) {
         if (substituteMethod.hasSubstitutedParameters() && method.areParameterErasuresEqual(substituteMethod)) {
            return method.typeVariables == Binding.NO_TYPE_VARIABLES && !hasGenericParameter(method);
         } else {
            return method.declaringClass.isRawType()
                  && substituteMethod.declaringClass.isRawType()
                  && method.hasSubstitutedParameters()
                  && substituteMethod.hasSubstitutedParameters()
               ? areMethodsCompatible(method, substituteMethod, environment)
               : false;
         }
      } else if (substituteMethod instanceof ParameterizedGenericMethodBinding) {
         if (method.typeVariables != Binding.NO_TYPE_VARIABLES) {
            return !((ParameterizedGenericMethodBinding)substituteMethod).isRaw;
         } else {
            return !hasGenericParameter(method);
         }
      } else {
         return method.typeVariables == Binding.NO_TYPE_VARIABLES;
      }
   }

   static boolean hasGenericParameter(MethodBinding method) {
      if (method.genericSignature() == null) {
         return false;
      } else {
         TypeBinding[] params = method.parameters;
         int i = 0;

         for(int l = params.length; i < l; ++i) {
            TypeBinding param = params[i].leafComponentType();
            if (param instanceof ReferenceBinding) {
               int modifiers = ((ReferenceBinding)param).modifiers;
               if ((modifiers & 1073741824) != 0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   boolean isSameClassOrSubclassOf(ReferenceBinding testClass, ReferenceBinding superclass) {
      while(!TypeBinding.equalsEquals(testClass, superclass)) {
         if ((testClass = testClass.superclass()) == null) {
            return false;
         }
      }

      return true;
   }

   boolean mustImplementAbstractMethod(ReferenceBinding declaringClass) {
      if (!this.mustImplementAbstractMethods()) {
         return false;
      } else {
         ReferenceBinding superclass = this.type.superclass();
         if (declaringClass.isClass()) {
            while(superclass.isAbstract() && TypeBinding.notEquals(superclass, declaringClass)) {
               superclass = superclass.superclass();
            }
         } else {
            if (this.type.implementsInterface(declaringClass, false) && !superclass.implementsInterface(declaringClass, true)) {
               return true;
            }

            while(superclass.isAbstract() && !superclass.implementsInterface(declaringClass, false)) {
               superclass = superclass.superclass();
            }
         }

         return superclass.isAbstract();
      }
   }

   boolean mustImplementAbstractMethods() {
      return !this.type.isInterface() && !this.type.isAbstract();
   }

   ProblemReporter problemReporter() {
      return this.type.scope.problemReporter();
   }

   ProblemReporter problemReporter(MethodBinding currentMethod) {
      ProblemReporter reporter = this.problemReporter();
      if (TypeBinding.equalsEquals(currentMethod.declaringClass, this.type) && currentMethod.sourceMethod() != null) {
         reporter.referenceContext = currentMethod.sourceMethod();
      }

      return reporter;
   }

   boolean reportIncompatibleReturnTypeError(MethodBinding currentMethod, MethodBinding inheritedMethod) {
      this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
      return true;
   }

   ReferenceBinding[] resolvedExceptionTypesFor(MethodBinding method) {
      ReferenceBinding[] exceptions = method.thrownExceptions;
      if ((method.modifiers & 33554432) == 0) {
         return exceptions;
      } else if (!(method.declaringClass instanceof BinaryTypeBinding)) {
         return Binding.NO_EXCEPTIONS;
      } else {
         int i = exceptions.length;

         while(--i >= 0) {
            exceptions[i] = (ReferenceBinding)BinaryTypeBinding.resolveType(exceptions[i], this.environment, true);
         }

         return exceptions;
      }
   }

   void verify() {
      this.computeMethods();
      this.computeInheritedMethods();
      this.checkMethods();
      if (this.type.isClass()) {
         this.checkForMissingHashCodeMethod();
      }
   }

   void verify(SourceTypeBinding someType) {
      if (this.type == null) {
         try {
            this.type = someType;
            this.verify();
         } finally {
            this.type = null;
         }
      } else {
         this.environment.newMethodVerifier().verify(someType);
      }
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer(10);
      buffer.append("MethodVerifier for type: ");
      buffer.append(this.type.readableName());
      buffer.append('\n');
      buffer.append("\t-inherited methods: ");
      buffer.append(this.inheritedMethods);
      return buffer.toString();
   }
}
