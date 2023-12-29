package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;

public class MethodBinding extends Binding {
   public int modifiers;
   public char[] selector;
   public TypeBinding returnType;
   public TypeBinding[] parameters;
   public TypeBinding receiver;
   public ReferenceBinding[] thrownExceptions;
   public ReferenceBinding declaringClass;
   public TypeVariableBinding[] typeVariables = Binding.NO_TYPE_VARIABLES;
   char[] signature;
   public long tagBits;
   protected AnnotationBinding[] typeAnnotations = Binding.NO_ANNOTATIONS;
   public Boolean[] parameterNonNullness;
   public int defaultNullness;
   public char[][] parameterNames = Binding.NO_PARAMETER_NAMES;

   protected MethodBinding() {
   }

   public MethodBinding(
      int modifiers, char[] selector, TypeBinding returnType, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass
   ) {
      this.modifiers = modifiers;
      this.selector = selector;
      this.returnType = returnType;
      this.parameters = parameters != null && parameters.length != 0 ? parameters : Binding.NO_PARAMETERS;
      this.thrownExceptions = thrownExceptions != null && thrownExceptions.length != 0 ? thrownExceptions : Binding.NO_EXCEPTIONS;
      this.declaringClass = declaringClass;
      if (this.declaringClass != null && this.declaringClass.isStrictfp() && !this.isNative() && !this.isAbstract()) {
         this.modifiers |= 2048;
      }
   }

   public MethodBinding(int modifiers, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
      this(modifiers, TypeConstants.INIT, TypeBinding.VOID, parameters, thrownExceptions, declaringClass);
   }

   public MethodBinding(MethodBinding initialMethodBinding, ReferenceBinding declaringClass) {
      this.modifiers = initialMethodBinding.modifiers;
      this.selector = initialMethodBinding.selector;
      this.returnType = initialMethodBinding.returnType;
      this.parameters = initialMethodBinding.parameters;
      this.thrownExceptions = initialMethodBinding.thrownExceptions;
      this.declaringClass = declaringClass;
      declaringClass.storeAnnotationHolder(this, initialMethodBinding.declaringClass.retrieveAnnotationHolder(initialMethodBinding, true));
   }

   public final boolean areParameterErasuresEqual(MethodBinding method) {
      TypeBinding[] args = method.parameters;
      if (this.parameters == args) {
         return true;
      } else {
         int length = this.parameters.length;
         if (length != args.length) {
            return false;
         } else {
            for(int i = 0; i < length; ++i) {
               if (TypeBinding.notEquals(this.parameters[i], args[i]) && TypeBinding.notEquals(this.parameters[i].erasure(), args[i].erasure())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public final boolean areParametersCompatibleWith(TypeBinding[] arguments) {
      int paramLength = this.parameters.length;
      int argLength = arguments.length;
      int lastIndex = argLength;
      if (this.isVarargs()) {
         lastIndex = paramLength - 1;
         if (paramLength == argLength) {
            TypeBinding varArgType = this.parameters[lastIndex];
            TypeBinding lastArgument = arguments[lastIndex];
            if (TypeBinding.notEquals(varArgType, lastArgument) && !lastArgument.isCompatibleWith(varArgType)) {
               return false;
            }
         } else if (paramLength < argLength) {
            TypeBinding varArgType = ((ArrayBinding)this.parameters[lastIndex]).elementsType();

            for(int i = lastIndex; i < argLength; ++i) {
               if (TypeBinding.notEquals(varArgType, arguments[i]) && !arguments[i].isCompatibleWith(varArgType)) {
                  return false;
               }
            }
         } else if (lastIndex != argLength) {
            return false;
         }
      }

      for(int i = 0; i < lastIndex; ++i) {
         if (TypeBinding.notEquals(this.parameters[i], arguments[i]) && !arguments[i].isCompatibleWith(this.parameters[i])) {
            return false;
         }
      }

      return true;
   }

   public final boolean areParametersEqual(MethodBinding method) {
      TypeBinding[] args = method.parameters;
      if (this.parameters == args) {
         return true;
      } else {
         int length = this.parameters.length;
         if (length != args.length) {
            return false;
         } else {
            for(int i = 0; i < length; ++i) {
               if (TypeBinding.notEquals(this.parameters[i], args[i])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public final boolean areTypeVariableErasuresEqual(MethodBinding method) {
      TypeVariableBinding[] vars = method.typeVariables;
      if (this.typeVariables == vars) {
         return true;
      } else {
         int length = this.typeVariables.length;
         if (length != vars.length) {
            return false;
         } else {
            for(int i = 0; i < length; ++i) {
               if (TypeBinding.notEquals(this.typeVariables[i], vars[i]) && TypeBinding.notEquals(this.typeVariables[i].erasure(), vars[i].erasure())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public MethodBinding asRawMethod(LookupEnvironment env) {
      if (this.typeVariables == Binding.NO_TYPE_VARIABLES) {
         return this;
      } else {
         int length = this.typeVariables.length;
         TypeBinding[] arguments = new TypeBinding[length];

         for(int i = 0; i < length; ++i) {
            TypeVariableBinding var = this.typeVariables[i];
            if (var.boundsCount() <= 1) {
               arguments[i] = env.convertToRawType(var.upperBound(), false);
            } else {
               TypeBinding[] itsSuperinterfaces = var.superInterfaces();
               int superLength = itsSuperinterfaces.length;
               TypeBinding rawFirstBound = null;
               TypeBinding[] rawOtherBounds = null;
               if (var.boundsCount() == superLength) {
                  rawFirstBound = env.convertToRawType(itsSuperinterfaces[0], false);
                  rawOtherBounds = new TypeBinding[superLength - 1];

                  for(int s = 1; s < superLength; ++s) {
                     rawOtherBounds[s - 1] = env.convertToRawType(itsSuperinterfaces[s], false);
                  }
               } else {
                  rawFirstBound = env.convertToRawType(var.superclass(), false);
                  rawOtherBounds = new TypeBinding[superLength];

                  for(int s = 0; s < superLength; ++s) {
                     rawOtherBounds[s] = env.convertToRawType(itsSuperinterfaces[s], false);
                  }
               }

               arguments[i] = env.createWildcard(null, 0, rawFirstBound, rawOtherBounds, 1);
            }
         }

         return env.createParameterizedGenericMethod(this, arguments);
      }
   }

   public final boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
      if (this.isPublic()) {
         return true;
      } else {
         SourceTypeBinding invocationType = scope.enclosingSourceType();
         if (TypeBinding.equalsEquals(invocationType, this.declaringClass)) {
            return true;
         } else if (this.isProtected()) {
            return invocationType.fPackage == this.declaringClass.fPackage ? true : invocationSite.isSuperAccess();
         } else if (!this.isPrivate()) {
            return invocationType.fPackage == this.declaringClass.fPackage;
         } else {
            ReferenceBinding outerInvocationType = invocationType;

            for(ReferenceBinding temp = invocationType.enclosingType(); temp != null; temp = temp.enclosingType()) {
               outerInvocationType = temp;
            }

            ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.declaringClass.erasure();

            for(ReferenceBinding var7 = outerDeclaringClass.enclosingType(); var7 != null; var7 = var7.enclosingType()) {
               outerDeclaringClass = var7;
            }

            return TypeBinding.equalsEquals(outerInvocationType, outerDeclaringClass);
         }
      }
   }

   public final boolean canBeSeenBy(PackageBinding invocationPackage) {
      if (this.isPublic()) {
         return true;
      } else if (this.isPrivate()) {
         return false;
      } else {
         return invocationPackage == this.declaringClass.getPackage();
      }
   }

   public final boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
      SourceTypeBinding invocationType = scope.enclosingSourceType();
      if (this.declaringClass.isInterface() && this.isStatic()) {
         if (scope.compilerOptions().sourceLevel < 3407872L) {
            return false;
         } else {
            return (invocationSite.isTypeAccess() || invocationSite.receiverIsImplicitThis()) && TypeBinding.equalsEquals(receiverType, this.declaringClass);
         }
      } else if (this.isPublic()) {
         return true;
      } else if (TypeBinding.equalsEquals(invocationType, this.declaringClass) && TypeBinding.equalsEquals(invocationType, receiverType)) {
         return true;
      } else if (invocationType == null) {
         return !this.isPrivate() && scope.getCurrentPackage() == this.declaringClass.fPackage;
      } else if (this.isProtected()) {
         if (TypeBinding.equalsEquals(invocationType, this.declaringClass)) {
            return true;
         } else if (invocationType.fPackage == this.declaringClass.fPackage) {
            return true;
         } else {
            ReferenceBinding currentType = invocationType;
            TypeBinding receiverErasure = receiverType.erasure();
            ReferenceBinding declaringErasure = (ReferenceBinding)this.declaringClass.erasure();
            int depth = 0;

            do {
               if (currentType.findSuperTypeOriginatingFrom(declaringErasure) != null) {
                  if (invocationSite.isSuperAccess()) {
                     return true;
                  }

                  if (receiverType instanceof ArrayBinding) {
                     return false;
                  }

                  if (this.isStatic()) {
                     if (depth > 0) {
                        invocationSite.setDepth(depth);
                     }

                     return true;
                  }

                  if (TypeBinding.equalsEquals(currentType, receiverErasure) || receiverErasure.findSuperTypeOriginatingFrom(currentType) != null) {
                     if (depth > 0) {
                        invocationSite.setDepth(depth);
                     }

                     return true;
                  }
               }

               ++depth;
               currentType = currentType.enclosingType();
            } while(currentType != null);

            return false;
         }
      } else if (!this.isPrivate()) {
         PackageBinding declaringPackage = this.declaringClass.fPackage;
         if (invocationType.fPackage != declaringPackage) {
            return false;
         } else if (receiverType instanceof ArrayBinding) {
            return false;
         } else {
            TypeBinding originalDeclaringClass = this.declaringClass.original();
            ReferenceBinding currentType = (ReferenceBinding)receiverType;

            do {
               if (currentType.isCapture()) {
                  if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.erasure().original())) {
                     return true;
                  }
               } else if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.original())) {
                  return true;
               }

               PackageBinding currentPackage = currentType.fPackage;
               if (!currentType.isCapture() && currentPackage != null && currentPackage != declaringPackage) {
                  return false;
               }
            } while((currentType = currentType.superclass()) != null);

            return false;
         }
      } else if (!TypeBinding.notEquals(receiverType, this.declaringClass)
         || scope.compilerOptions().complianceLevel <= 3276800L
            && receiverType.isTypeVariable()
            && ((TypeVariableBinding)receiverType).isErasureBoundTo(this.declaringClass.erasure())) {
         if (TypeBinding.notEquals(invocationType, this.declaringClass)) {
            ReferenceBinding outerInvocationType = invocationType;

            for(ReferenceBinding temp = invocationType.enclosingType(); temp != null; temp = temp.enclosingType()) {
               outerInvocationType = temp;
            }

            ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.declaringClass.erasure();

            for(ReferenceBinding var11 = outerDeclaringClass.enclosingType(); var11 != null; var11 = var11.enclosingType()) {
               outerDeclaringClass = var11;
            }

            if (TypeBinding.notEquals(outerInvocationType, outerDeclaringClass)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
      if ((this.tagBits & 128L) != 0L) {
         missingTypes = this.returnType.collectMissingTypes(missingTypes);
         int i = 0;

         for(int max = this.parameters.length; i < max; ++i) {
            missingTypes = this.parameters[i].collectMissingTypes(missingTypes);
         }

         i = 0;

         for(int max = this.thrownExceptions.length; i < max; ++i) {
            missingTypes = this.thrownExceptions[i].collectMissingTypes(missingTypes);
         }

         i = 0;

         for(int max = this.typeVariables.length; i < max; ++i) {
            TypeVariableBinding variable = this.typeVariables[i];
            missingTypes = variable.superclass().collectMissingTypes(missingTypes);
            ReferenceBinding[] interfaces = variable.superInterfaces();
            int j = 0;

            for(int length = interfaces.length; j < length; ++j) {
               missingTypes = interfaces[j].collectMissingTypes(missingTypes);
            }
         }
      }

      return missingTypes;
   }

   MethodBinding computeSubstitutedMethod(MethodBinding method, LookupEnvironment env) {
      int length = this.typeVariables.length;
      TypeVariableBinding[] vars = method.typeVariables;
      if (length != vars.length) {
         return null;
      } else {
         ParameterizedGenericMethodBinding substitute = env.createParameterizedGenericMethod(method, this.typeVariables);

         for(int i = 0; i < length; ++i) {
            if (!this.typeVariables[i].isInterchangeableWith(vars[i], substitute)) {
               return null;
            }
         }

         return substitute;
      }
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      char[] declaringKey = this.declaringClass.computeUniqueKey(false);
      int declaringLength = declaringKey.length;
      int selectorLength = this.selector == TypeConstants.INIT ? 0 : this.selector.length;
      char[] sig = this.genericSignature();
      boolean isGeneric = sig != null;
      if (!isGeneric) {
         sig = this.signature();
      }

      int signatureLength = sig.length;
      int thrownExceptionsLength = this.thrownExceptions.length;
      int thrownExceptionsSignatureLength = 0;
      char[][] thrownExceptionsSignatures = null;
      boolean addThrownExceptions = thrownExceptionsLength > 0 && (!isGeneric || CharOperation.lastIndexOf('^', sig) < 0);
      if (addThrownExceptions) {
         thrownExceptionsSignatures = new char[thrownExceptionsLength][];

         for(int i = 0; i < thrownExceptionsLength; ++i) {
            if (this.thrownExceptions[i] != null) {
               thrownExceptionsSignatures[i] = this.thrownExceptions[i].signature();
               thrownExceptionsSignatureLength += thrownExceptionsSignatures[i].length + 1;
            }
         }
      }

      char[] uniqueKey = new char[declaringLength + 1 + selectorLength + signatureLength + thrownExceptionsSignatureLength];
      int index = 0;
      System.arraycopy(declaringKey, 0, uniqueKey, index, declaringLength);
      index = declaringLength + 1;
      uniqueKey[declaringLength] = '.';
      System.arraycopy(this.selector, 0, uniqueKey, index, selectorLength);
      index += selectorLength;
      System.arraycopy(sig, 0, uniqueKey, index, signatureLength);
      if (thrownExceptionsSignatureLength > 0) {
         index += signatureLength;

         for(int i = 0; i < thrownExceptionsLength; ++i) {
            char[] thrownExceptionSignature = thrownExceptionsSignatures[i];
            if (thrownExceptionSignature != null) {
               uniqueKey[index++] = '|';
               int length = thrownExceptionSignature.length;
               System.arraycopy(thrownExceptionSignature, 0, uniqueKey, index, length);
               index += length;
            }
         }
      }

      return uniqueKey;
   }

   public final char[] constantPoolName() {
      return this.selector;
   }

   protected void fillInDefaultNonNullness(AbstractMethodDeclaration sourceMethod) {
      if (this.parameterNonNullness == null) {
         this.parameterNonNullness = new Boolean[this.parameters.length];
      }

      boolean added = false;
      int length = this.parameterNonNullness.length;

      for(int i = 0; i < length; ++i) {
         if (!this.parameters[i].isBaseType()) {
            if (this.parameterNonNullness[i] == null) {
               added = true;
               this.parameterNonNullness[i] = Boolean.TRUE;
               if (sourceMethod != null) {
                  sourceMethod.arguments[i].binding.tagBits |= 72057594037927936L;
               }
            } else if (sourceMethod != null && this.parameterNonNullness[i]) {
               sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, i);
            }
         }
      }

      if (added) {
         this.tagBits |= 1024L;
      }

      if (this.returnType != null && !this.returnType.isBaseType() && (this.tagBits & 108086391056891904L) == 0L) {
         this.tagBits |= 72057594037927936L;
      } else if (sourceMethod != null && (this.tagBits & 72057594037927936L) != 0L) {
         sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, -1);
      }
   }

   protected void fillInDefaultNonNullness18(AbstractMethodDeclaration sourceMethod, LookupEnvironment env) {
      if (this.hasNonNullDefaultFor(8, true)) {
         boolean added = false;
         int length = this.parameters.length;

         for(int i = 0; i < length; ++i) {
            TypeBinding parameter = this.parameters[i];
            if (parameter.acceptsNonNullDefault()) {
               long existing = parameter.tagBits & 108086391056891904L;
               if (existing == 0L) {
                  added = true;
                  if (!parameter.isBaseType()) {
                     this.parameters[i] = env.createAnnotatedType(parameter, new AnnotationBinding[]{env.getNonNullAnnotation()});
                     if (sourceMethod != null) {
                        sourceMethod.arguments[i].binding.type = this.parameters[i];
                     }
                  }
               } else if (sourceMethod != null
                  && (parameter.tagBits & 72057594037927936L) != 0L
                  && sourceMethod.arguments[i].hasNullTypeAnnotation(TypeReference.AnnotationPosition.MAIN_TYPE)) {
                  sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, i);
               }
            }
         }

         if (added) {
            this.tagBits |= 1024L;
         }
      }

      if (this.returnType != null && this.hasNonNullDefaultFor(16, true) && this.returnType.acceptsNonNullDefault()) {
         if ((this.returnType.tagBits & 108086391056891904L) == 0L) {
            this.returnType = env.createAnnotatedType(this.returnType, new AnnotationBinding[]{env.getNonNullAnnotation()});
         } else if (sourceMethod instanceof MethodDeclaration
            && (this.returnType.tagBits & 72057594037927936L) != 0L
            && ((MethodDeclaration)sourceMethod).hasNullTypeAnnotation(TypeReference.AnnotationPosition.MAIN_TYPE)) {
            sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, -1);
         }
      }
   }

   public MethodBinding findOriginalInheritedMethod(MethodBinding inheritedMethod) {
      MethodBinding inheritedOriginal = inheritedMethod.original();
      TypeBinding superType = this.declaringClass.findSuperTypeOriginatingFrom(inheritedOriginal.declaringClass);
      if (superType != null && superType instanceof ReferenceBinding) {
         if (TypeBinding.notEquals(inheritedOriginal.declaringClass, superType)) {
            MethodBinding[] superMethods = ((ReferenceBinding)superType).getMethods(inheritedOriginal.selector, inheritedOriginal.parameters.length);
            int m = 0;

            for(int l = superMethods.length; m < l; ++m) {
               if (superMethods[m].original() == inheritedOriginal) {
                  return superMethods[m];
               }
            }
         }

         return inheritedOriginal;
      } else {
         return null;
      }
   }

   public char[] genericSignature() {
      if ((this.modifiers & 1073741824) == 0) {
         return null;
      } else {
         StringBuffer sig = new StringBuffer(10);
         if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
            sig.append('<');
            int i = 0;

            for(int length = this.typeVariables.length; i < length; ++i) {
               sig.append(this.typeVariables[i].genericSignature());
            }

            sig.append('>');
         }

         sig.append('(');
         int i = 0;

         for(int length = this.parameters.length; i < length; ++i) {
            sig.append(this.parameters[i].genericTypeSignature());
         }

         sig.append(')');
         if (this.returnType != null) {
            sig.append(this.returnType.genericTypeSignature());
         }

         boolean needExceptionSignatures = false;
         int length = this.thrownExceptions.length;

         for(int ix = 0; ix < length; ++ix) {
            if ((this.thrownExceptions[ix].modifiers & 1073741824) != 0) {
               needExceptionSignatures = true;
               break;
            }
         }

         if (needExceptionSignatures) {
            for(int ix = 0; ix < length; ++ix) {
               sig.append('^');
               sig.append(this.thrownExceptions[ix].genericTypeSignature());
            }
         }

         int sigLength = sig.length();
         char[] genericSignature = new char[sigLength];
         sig.getChars(0, sigLength, genericSignature, 0);
         return genericSignature;
      }
   }

   public final int getAccessFlags() {
      return this.modifiers & 131071;
   }

   @Override
   public AnnotationBinding[] getAnnotations() {
      MethodBinding originalMethod = this.original();
      return originalMethod.declaringClass.retrieveAnnotations(originalMethod);
   }

   @Override
   public long getAnnotationTagBits() {
      MethodBinding originalMethod = this.original();
      if ((originalMethod.tagBits & 8589934592L) == 0L && originalMethod.declaringClass instanceof SourceTypeBinding) {
         ClassScope scope = ((SourceTypeBinding)originalMethod.declaringClass).scope;
         if (scope != null) {
            TypeDeclaration typeDecl = scope.referenceContext;
            AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(originalMethod);
            if (methodDecl != null) {
               ASTNode.resolveAnnotations(methodDecl.scope, methodDecl.annotations, originalMethod);
            }

            CompilerOptions options = scope.compilerOptions();
            if (options.isAnnotationBasedNullAnalysisEnabled) {
               boolean usesNullTypeAnnotations = scope.environment().usesNullTypeAnnotations();
               long nullDefaultBits = usesNullTypeAnnotations ? (long)this.defaultNullness : this.tagBits & 432345564227567616L;
               if (nullDefaultBits != 0L && this.declaringClass instanceof SourceTypeBinding) {
                  SourceTypeBinding declaringSourceType = (SourceTypeBinding)this.declaringClass;
                  if (declaringSourceType.checkRedundantNullnessDefaultOne(methodDecl, methodDecl.annotations, nullDefaultBits, usesNullTypeAnnotations)) {
                     declaringSourceType.checkRedundantNullnessDefaultRecurse(methodDecl, methodDecl.annotations, nullDefaultBits, usesNullTypeAnnotations);
                  }
               }
            }
         }
      }

      return originalMethod.tagBits;
   }

   public Object getDefaultValue() {
      MethodBinding originalMethod = this.original();
      if ((originalMethod.tagBits & 576460752303423488L) == 0L) {
         if (originalMethod.declaringClass instanceof SourceTypeBinding) {
            SourceTypeBinding sourceType = (SourceTypeBinding)originalMethod.declaringClass;
            if (sourceType.scope != null) {
               AbstractMethodDeclaration methodDeclaration = originalMethod.sourceMethod();
               if (methodDeclaration != null && methodDeclaration.isAnnotationMethod()) {
                  methodDeclaration.resolve(sourceType.scope);
               }
            }
         }

         originalMethod.tagBits |= 576460752303423488L;
      }

      AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
      return holder == null ? null : holder.getDefaultValue();
   }

   public AnnotationBinding[][] getParameterAnnotations() {
      int length;
      if ((length = this.parameters.length) == 0) {
         return null;
      } else {
         MethodBinding originalMethod = this.original();
         AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
         AnnotationBinding[][] allParameterAnnotations = holder == null ? null : holder.getParameterAnnotations();
         if (allParameterAnnotations == null && (this.tagBits & 1024L) != 0L) {
            allParameterAnnotations = new AnnotationBinding[length][];
            if (this.declaringClass instanceof SourceTypeBinding) {
               SourceTypeBinding sourceType = (SourceTypeBinding)this.declaringClass;
               if (sourceType.scope != null) {
                  AbstractMethodDeclaration methodDecl = sourceType.scope.referenceType().declarationOf(originalMethod);

                  for(int i = 0; i < length; ++i) {
                     Argument argument = methodDecl.arguments[i];
                     if (argument.annotations != null) {
                        ASTNode.resolveAnnotations(methodDecl.scope, argument.annotations, argument.binding);
                        allParameterAnnotations[i] = argument.binding.getAnnotations();
                     } else {
                        allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
                     }
                  }
               } else {
                  for(int i = 0; i < length; ++i) {
                     allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
                  }
               }
            } else {
               for(int i = 0; i < length; ++i) {
                  allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
               }
            }

            this.setParameterAnnotations(allParameterAnnotations);
         }

         return allParameterAnnotations;
      }
   }

   public TypeVariableBinding getTypeVariable(char[] variableName) {
      int i = this.typeVariables.length;

      while(--i >= 0) {
         if (CharOperation.equals(this.typeVariables[i].sourceName, variableName)) {
            return this.typeVariables[i];
         }
      }

      return null;
   }

   public TypeVariableBinding[] getAllTypeVariables(boolean isDiamond) {
      TypeVariableBinding[] allTypeVariables = this.typeVariables;
      if (isDiamond) {
         TypeVariableBinding[] classTypeVariables = this.declaringClass.typeVariables();
         int l1 = allTypeVariables.length;
         int l2 = classTypeVariables.length;
         if (l1 == 0) {
            allTypeVariables = classTypeVariables;
         } else if (l2 != 0) {
            System.arraycopy(allTypeVariables, 0, allTypeVariables = new TypeVariableBinding[l1 + l2], 0, l1);
            System.arraycopy(classTypeVariables, 0, allTypeVariables, l1, l2);
         }
      }

      return allTypeVariables;
   }

   public boolean hasSubstitutedParameters() {
      return false;
   }

   public boolean hasSubstitutedReturnType() {
      return false;
   }

   public final boolean isAbstract() {
      return (this.modifiers & 1024) != 0;
   }

   public final boolean isBridge() {
      return (this.modifiers & 64) != 0;
   }

   public final boolean isConstructor() {
      return this.selector == TypeConstants.INIT;
   }

   public final boolean isDefault() {
      return !this.isPublic() && !this.isProtected() && !this.isPrivate();
   }

   public final boolean isDefaultAbstract() {
      return (this.modifiers & 524288) != 0;
   }

   public boolean isDefaultMethod() {
      return (this.modifiers & 65536) != 0;
   }

   public final boolean isDeprecated() {
      return (this.modifiers & 1048576) != 0;
   }

   public final boolean isFinal() {
      return (this.modifiers & 16) != 0;
   }

   public final boolean isImplementing() {
      return (this.modifiers & 536870912) != 0;
   }

   public final boolean isMain() {
      if (this.selector.length == 4
         && CharOperation.equals(this.selector, TypeConstants.MAIN)
         && (this.modifiers & 9) != 0
         && TypeBinding.VOID == this.returnType
         && this.parameters.length == 1) {
         TypeBinding paramType = this.parameters[0];
         if (paramType.dimensions() == 1 && paramType.leafComponentType().id == 11) {
            return true;
         }
      }

      return false;
   }

   public final boolean isNative() {
      return (this.modifiers & 256) != 0;
   }

   public final boolean isOverriding() {
      return (this.modifiers & 268435456) != 0;
   }

   public final boolean isPrivate() {
      return (this.modifiers & 2) != 0;
   }

   public final boolean isOrEnclosedByPrivateType() {
      if ((this.modifiers & 2) != 0) {
         return true;
      } else {
         return this.declaringClass != null && this.declaringClass.isOrEnclosedByPrivateType();
      }
   }

   public final boolean isProtected() {
      return (this.modifiers & 4) != 0;
   }

   public final boolean isPublic() {
      return (this.modifiers & 1) != 0;
   }

   public final boolean isStatic() {
      return (this.modifiers & 8) != 0;
   }

   public final boolean isStrictfp() {
      return (this.modifiers & 2048) != 0;
   }

   public final boolean isSynchronized() {
      return (this.modifiers & 32) != 0;
   }

   public final boolean isSynthetic() {
      return (this.modifiers & 4096) != 0;
   }

   public final boolean isUsed() {
      return (this.modifiers & 134217728) != 0;
   }

   public boolean isVarargs() {
      return (this.modifiers & 128) != 0;
   }

   public boolean isParameterizedGeneric() {
      return false;
   }

   public boolean isPolymorphic() {
      return false;
   }

   public final boolean isViewedAsDeprecated() {
      return (this.modifiers & 3145728) != 0;
   }

   @Override
   public final int kind() {
      return 8;
   }

   public MethodBinding original() {
      return this;
   }

   public MethodBinding shallowOriginal() {
      return this.original();
   }

   public MethodBinding genericMethod() {
      return this;
   }

   @Override
   public char[] readableName() {
      StringBuffer buffer = new StringBuffer(this.parameters.length + 20);
      if (this.isConstructor()) {
         buffer.append(this.declaringClass.sourceName());
      } else {
         buffer.append(this.selector);
      }

      buffer.append('(');
      if (this.parameters != Binding.NO_PARAMETERS) {
         int i = 0;

         for(int length = this.parameters.length; i < length; ++i) {
            if (i > 0) {
               buffer.append(", ");
            }

            buffer.append(this.parameters[i].sourceName());
         }
      }

      buffer.append(')');
      return buffer.toString().toCharArray();
   }

   public final AnnotationBinding[] getTypeAnnotations() {
      return this.typeAnnotations;
   }

   public void setTypeAnnotations(AnnotationBinding[] annotations) {
      this.typeAnnotations = annotations;
   }

   @Override
   public void setAnnotations(AnnotationBinding[] annotations) {
      this.declaringClass.storeAnnotations(this, annotations);
   }

   public void setAnnotations(AnnotationBinding[] annotations, AnnotationBinding[][] parameterAnnotations, Object defaultValue, LookupEnvironment optionalEnv) {
      this.declaringClass.storeAnnotationHolder(this, AnnotationHolder.storeAnnotations(annotations, parameterAnnotations, defaultValue, optionalEnv));
   }

   public void setDefaultValue(Object defaultValue) {
      MethodBinding originalMethod = this.original();
      originalMethod.tagBits |= 576460752303423488L;
      AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
      if (holder == null) {
         this.setAnnotations(null, null, defaultValue, null);
      } else {
         this.setAnnotations(holder.getAnnotations(), holder.getParameterAnnotations(), defaultValue, null);
      }
   }

   public void setParameterAnnotations(AnnotationBinding[][] parameterAnnotations) {
      AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
      if (holder == null) {
         this.setAnnotations(null, parameterAnnotations, null, null);
      } else {
         this.setAnnotations(holder.getAnnotations(), parameterAnnotations, holder.getDefaultValue(), null);
      }
   }

   protected final void setSelector(char[] selector) {
      this.selector = selector;
      this.signature = null;
   }

   @Override
   public char[] shortReadableName() {
      StringBuffer buffer = new StringBuffer(this.parameters.length + 20);
      if (this.isConstructor()) {
         buffer.append(this.declaringClass.shortReadableName());
      } else {
         buffer.append(this.selector);
      }

      buffer.append('(');
      if (this.parameters != Binding.NO_PARAMETERS) {
         int i = 0;

         for(int length = this.parameters.length; i < length; ++i) {
            if (i > 0) {
               buffer.append(", ");
            }

            buffer.append(this.parameters[i].shortReadableName());
         }
      }

      buffer.append(')');
      int nameLength = buffer.length();
      char[] shortReadableName = new char[nameLength];
      buffer.getChars(0, nameLength, shortReadableName, 0);
      return shortReadableName;
   }

   public final char[] signature() {
      if (this.signature != null) {
         return this.signature;
      } else {
         StringBuffer buffer = new StringBuffer(this.parameters.length + 20);
         buffer.append('(');
         TypeBinding[] targetParameters = this.parameters;
         boolean isConstructor = this.isConstructor();
         if (isConstructor && this.declaringClass.isEnum()) {
            buffer.append(ConstantPool.JavaLangStringSignature);
            buffer.append(TypeBinding.INT.signature());
         }

         boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
         if (needSynthetics) {
            ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
            if (syntheticArgumentTypes != null) {
               int i = 0;

               for(int count = syntheticArgumentTypes.length; i < count; ++i) {
                  buffer.append(syntheticArgumentTypes[i].signature());
               }
            }

            if (this instanceof SyntheticMethodBinding) {
               targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
            }
         }

         if (targetParameters != Binding.NO_PARAMETERS) {
            for(int i = 0; i < targetParameters.length; ++i) {
               buffer.append(targetParameters[i].signature());
            }
         }

         if (needSynthetics) {
            SyntheticArgumentBinding[] syntheticOuterArguments = this.declaringClass.syntheticOuterLocalVariables();
            int count = syntheticOuterArguments == null ? 0 : syntheticOuterArguments.length;

            for(int i = 0; i < count; ++i) {
               buffer.append(syntheticOuterArguments[i].type.signature());
            }

            int i = targetParameters.length;

            for(int extraLength = this.parameters.length; i < extraLength; ++i) {
               buffer.append(this.parameters[i].signature());
            }
         }

         buffer.append(')');
         if (this.returnType != null) {
            buffer.append(this.returnType.signature());
         }

         int nameLength = buffer.length();
         this.signature = new char[nameLength];
         buffer.getChars(0, nameLength, this.signature, 0);
         return this.signature;
      }
   }

   public final char[] signature(ClassFile classFile) {
      if (this.signature != null) {
         if ((this.tagBits & 2048L) != 0L) {
            boolean isConstructor = this.isConstructor();
            TypeBinding[] targetParameters = this.parameters;
            boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
            if (needSynthetics) {
               ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
               if (syntheticArgumentTypes != null) {
                  int i = 0;

                  for(int count = syntheticArgumentTypes.length; i < count; ++i) {
                     ReferenceBinding syntheticArgumentType = syntheticArgumentTypes[i];
                     if ((syntheticArgumentType.tagBits & 2048L) != 0L) {
                        Util.recordNestedType(classFile, syntheticArgumentType);
                     }
                  }
               }

               if (this instanceof SyntheticMethodBinding) {
                  targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
               }
            }

            if (targetParameters != Binding.NO_PARAMETERS) {
               int i = 0;

               for(int max = targetParameters.length; i < max; ++i) {
                  TypeBinding targetParameter = targetParameters[i];
                  TypeBinding leafTargetParameterType = targetParameter.leafComponentType();
                  if ((leafTargetParameterType.tagBits & 2048L) != 0L) {
                     Util.recordNestedType(classFile, leafTargetParameterType);
                  }
               }
            }

            if (needSynthetics) {
               int i = targetParameters.length;

               for(int extraLength = this.parameters.length; i < extraLength; ++i) {
                  TypeBinding parameter = this.parameters[i];
                  TypeBinding leafParameterType = parameter.leafComponentType();
                  if ((leafParameterType.tagBits & 2048L) != 0L) {
                     Util.recordNestedType(classFile, leafParameterType);
                  }
               }
            }

            if (this.returnType != null) {
               TypeBinding ret = this.returnType.leafComponentType();
               if ((ret.tagBits & 2048L) != 0L) {
                  Util.recordNestedType(classFile, ret);
               }
            }
         }

         return this.signature;
      } else {
         StringBuffer buffer = new StringBuffer((this.parameters.length + 1) * 20);
         buffer.append('(');
         TypeBinding[] targetParameters = this.parameters;
         boolean isConstructor = this.isConstructor();
         if (isConstructor && this.declaringClass.isEnum()) {
            buffer.append(ConstantPool.JavaLangStringSignature);
            buffer.append(TypeBinding.INT.signature());
         }

         boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
         if (needSynthetics) {
            ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
            if (syntheticArgumentTypes != null) {
               int i = 0;

               for(int count = syntheticArgumentTypes.length; i < count; ++i) {
                  ReferenceBinding syntheticArgumentType = syntheticArgumentTypes[i];
                  if ((syntheticArgumentType.tagBits & 2048L) != 0L) {
                     this.tagBits |= 2048L;
                     Util.recordNestedType(classFile, syntheticArgumentType);
                  }

                  buffer.append(syntheticArgumentType.signature());
               }
            }

            if (this instanceof SyntheticMethodBinding) {
               targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
            }
         }

         if (targetParameters != Binding.NO_PARAMETERS) {
            int i = 0;

            for(int max = targetParameters.length; i < max; ++i) {
               TypeBinding targetParameter = targetParameters[i];
               TypeBinding leafTargetParameterType = targetParameter.leafComponentType();
               if ((leafTargetParameterType.tagBits & 2048L) != 0L) {
                  this.tagBits |= 2048L;
                  Util.recordNestedType(classFile, leafTargetParameterType);
               }

               buffer.append(targetParameter.signature());
            }
         }

         if (needSynthetics) {
            SyntheticArgumentBinding[] syntheticOuterArguments = this.declaringClass.syntheticOuterLocalVariables();
            int count = syntheticOuterArguments == null ? 0 : syntheticOuterArguments.length;

            for(int i = 0; i < count; ++i) {
               buffer.append(syntheticOuterArguments[i].type.signature());
            }

            int i = targetParameters.length;

            for(int extraLength = this.parameters.length; i < extraLength; ++i) {
               TypeBinding parameter = this.parameters[i];
               TypeBinding leafParameterType = parameter.leafComponentType();
               if ((leafParameterType.tagBits & 2048L) != 0L) {
                  this.tagBits |= 2048L;
                  Util.recordNestedType(classFile, leafParameterType);
               }

               buffer.append(parameter.signature());
            }
         }

         buffer.append(')');
         if (this.returnType != null) {
            TypeBinding ret = this.returnType.leafComponentType();
            if ((ret.tagBits & 2048L) != 0L) {
               this.tagBits |= 2048L;
               Util.recordNestedType(classFile, ret);
            }

            buffer.append(this.returnType.signature());
         }

         int nameLength = buffer.length();
         this.signature = new char[nameLength];
         buffer.getChars(0, nameLength, this.signature, 0);
         return this.signature;
      }
   }

   public final int sourceEnd() {
      AbstractMethodDeclaration method = this.sourceMethod();
      if (method == null) {
         return this.declaringClass instanceof SourceTypeBinding ? ((SourceTypeBinding)this.declaringClass).sourceEnd() : 0;
      } else {
         return method.sourceEnd;
      }
   }

   public AbstractMethodDeclaration sourceMethod() {
      if (this.isSynthetic()) {
         return null;
      } else {
         SourceTypeBinding sourceType;
         try {
            sourceType = (SourceTypeBinding)this.declaringClass;
         } catch (ClassCastException var4) {
            return null;
         }

         AbstractMethodDeclaration[] methods = sourceType.scope != null ? sourceType.scope.referenceContext.methods : null;
         if (methods != null) {
            int i = methods.length;

            while(--i >= 0) {
               if (this == methods[i].binding) {
                  return methods[i];
               }
            }
         }

         return null;
      }
   }

   public LambdaExpression sourceLambda() {
      return null;
   }

   public final int sourceStart() {
      AbstractMethodDeclaration method = this.sourceMethod();
      if (method == null) {
         return this.declaringClass instanceof SourceTypeBinding ? ((SourceTypeBinding)this.declaringClass).sourceStart() : 0;
      } else {
         return method.sourceStart;
      }
   }

   public MethodBinding tiebreakMethod() {
      return this;
   }

   @Override
   public String toString() {
      StringBuffer output = new StringBuffer(10);
      if ((this.modifiers & 33554432) != 0) {
         output.append("[unresolved] ");
      }

      ASTNode.printModifiers(this.modifiers, output);
      output.append(this.returnType != null ? this.returnType.debugName() : "<no type>");
      output.append(" ");
      output.append(this.selector != null ? new String(this.selector) : "<no selector>");
      output.append("(");
      if (this.parameters != null) {
         if (this.parameters != Binding.NO_PARAMETERS) {
            int i = 0;

            for(int length = this.parameters.length; i < length; ++i) {
               if (i > 0) {
                  output.append(", ");
               }

               output.append(this.parameters[i] != null ? this.parameters[i].debugName() : "<no argument type>");
            }
         }
      } else {
         output.append("<no argument types>");
      }

      output.append(") ");
      if (this.thrownExceptions != null) {
         if (this.thrownExceptions != Binding.NO_EXCEPTIONS) {
            output.append("throws ");
            int i = 0;

            for(int length = this.thrownExceptions.length; i < length; ++i) {
               if (i > 0) {
                  output.append(", ");
               }

               output.append(this.thrownExceptions[i] != null ? this.thrownExceptions[i].debugName() : "<no exception type>");
            }
         }
      } else {
         output.append("<no exception types>");
      }

      return output.toString();
   }

   public TypeVariableBinding[] typeVariables() {
      return this.typeVariables;
   }

   public boolean hasNonNullDefaultFor(int location, boolean useTypeAnnotations) {
      if ((this.modifiers & 67108864) != 0) {
         return false;
      } else {
         if (useTypeAnnotations) {
            if (this.defaultNullness != 0) {
               if ((this.defaultNullness & location) != 0) {
                  return true;
               }

               return false;
            }
         } else {
            if ((this.tagBits & 144115188075855872L) != 0L) {
               return true;
            }

            if ((this.tagBits & 288230376151711744L) != 0L) {
               return false;
            }
         }

         return this.declaringClass.hasNonNullDefaultFor(location, useTypeAnnotations);
      }
   }

   public boolean redeclaresPublicObjectMethod(Scope scope) {
      ReferenceBinding javaLangObject = scope.getJavaLangObject();
      MethodBinding[] methods = javaLangObject.getMethods(this.selector);
      int i = 0;

      for(int length = methods == null ? 0 : methods.length; i < length; ++i) {
         MethodBinding method = methods[i];
         if (method.isPublic()
            && !method.isStatic()
            && method.parameters.length == this.parameters.length
            && MethodVerifier.doesMethodOverride(this, method, scope.environment())) {
            return true;
         }
      }

      return false;
   }

   public boolean isVoidMethod() {
      return this.returnType == TypeBinding.VOID;
   }
}
