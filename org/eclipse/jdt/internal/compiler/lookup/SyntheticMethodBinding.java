package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;

public class SyntheticMethodBinding extends MethodBinding {
   public FieldBinding targetReadField;
   public FieldBinding targetWriteField;
   public MethodBinding targetMethod;
   public TypeBinding targetEnumType;
   public LambdaExpression lambda;
   public ReferenceExpression serializableMethodRef;
   public int purpose;
   public int startIndex;
   public int endIndex;
   public static final int FieldReadAccess = 1;
   public static final int FieldWriteAccess = 2;
   public static final int SuperFieldReadAccess = 3;
   public static final int SuperFieldWriteAccess = 4;
   public static final int MethodAccess = 5;
   public static final int ConstructorAccess = 6;
   public static final int SuperMethodAccess = 7;
   public static final int BridgeMethod = 8;
   public static final int EnumValues = 9;
   public static final int EnumValueOf = 10;
   public static final int SwitchTable = 11;
   public static final int TooManyEnumsConstants = 12;
   public static final int LambdaMethod = 13;
   public static final int ArrayConstructor = 14;
   public static final int ArrayClone = 15;
   public static final int FactoryMethod = 16;
   public static final int DeserializeLambda = 17;
   public static final int SerializableMethodReference = 18;
   public int sourceStart = 0;
   public int index;
   public int fakePaddedParameters = 0;

   public SyntheticMethodBinding(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess, ReferenceBinding declaringClass) {
      this.modifiers = 4104;
      this.tagBits |= 25769803776L;
      SourceTypeBinding declaringSourceType = (SourceTypeBinding)declaringClass;
      SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
      this.selector = CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(methodId).toCharArray());
      if (isReadAccess) {
         this.returnType = targetField.type;
         if (targetField.isStatic()) {
            this.parameters = Binding.NO_PARAMETERS;
         } else {
            this.parameters = new TypeBinding[1];
            this.parameters[0] = declaringSourceType;
         }

         this.targetReadField = targetField;
         this.purpose = isSuperAccess ? 3 : 1;
      } else {
         this.returnType = TypeBinding.VOID;
         if (targetField.isStatic()) {
            this.parameters = new TypeBinding[1];
            this.parameters[0] = targetField.type;
         } else {
            this.parameters = new TypeBinding[2];
            this.parameters[0] = declaringSourceType;
            this.parameters[1] = targetField.type;
         }

         this.targetWriteField = targetField;
         this.purpose = isSuperAccess ? 4 : 2;
      }

      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      this.declaringClass = declaringSourceType;

      boolean needRename;
      do {
         label112: {
            needRename = false;
            MethodBinding[] methods = declaringSourceType.methods();
            long range;
            if ((range = ReferenceBinding.binarySearch(this.selector, methods)) >= 0L) {
               int paramCount = this.parameters.length;
               int imethod = (int)range;

               label101:
               for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
                  MethodBinding method = methods[imethod];
                  if (method.parameters.length == paramCount) {
                     TypeBinding[] toMatch = method.parameters;

                     for(int i = 0; i < paramCount; ++i) {
                        if (TypeBinding.notEquals(toMatch[i], this.parameters[i])) {
                           continue label101;
                        }
                     }

                     needRename = true;
                     break label112;
                  }
               }
            }

            if (knownAccessMethods != null) {
               int i = 0;

               for(int length = knownAccessMethods.length; i < length; ++i) {
                  if (knownAccessMethods[i] != null
                     && CharOperation.equals(this.selector, knownAccessMethods[i].selector)
                     && this.areParametersEqual(methods[i])) {
                     needRename = true;
                     break;
                  }
               }
            }
         }

         if (needRename) {
            this.setSelector(CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(++methodId).toCharArray()));
         }
      } while(needRename);

      FieldDeclaration[] fieldDecls = declaringSourceType.scope.referenceContext.fields;
      if (fieldDecls != null) {
         int i = 0;

         for(int max = fieldDecls.length; i < max; ++i) {
            if (fieldDecls[i].binding == targetField) {
               this.sourceStart = fieldDecls[i].sourceStart;
               return;
            }
         }
      }

      this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart;
   }

   public SyntheticMethodBinding(FieldBinding targetField, ReferenceBinding declaringClass, TypeBinding enumBinding, char[] selector) {
      this.modifiers = (declaringClass.isInterface() ? 1 : 0) | 8 | 4096;
      this.tagBits |= 25769803776L;
      SourceTypeBinding declaringSourceType = (SourceTypeBinding)declaringClass;
      SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
      this.selector = selector;
      this.returnType = declaringSourceType.scope.createArrayType(TypeBinding.INT, 1);
      this.parameters = Binding.NO_PARAMETERS;
      this.targetReadField = targetField;
      this.targetEnumType = enumBinding;
      this.purpose = 11;
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      this.declaringClass = declaringSourceType;
      if (declaringSourceType.isStrictfp()) {
         this.modifiers |= 2048;
      }

      boolean needRename;
      do {
         label77: {
            needRename = false;
            MethodBinding[] methods = declaringSourceType.methods();
            long range;
            if ((range = ReferenceBinding.binarySearch(this.selector, methods)) >= 0L) {
               int paramCount = this.parameters.length;
               int imethod = (int)range;

               label68:
               for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
                  MethodBinding method = methods[imethod];
                  if (method.parameters.length == paramCount) {
                     TypeBinding[] toMatch = method.parameters;

                     for(int i = 0; i < paramCount; ++i) {
                        if (TypeBinding.notEquals(toMatch[i], this.parameters[i])) {
                           continue label68;
                        }
                     }

                     needRename = true;
                     break label77;
                  }
               }
            }

            if (knownAccessMethods != null) {
               int i = 0;

               for(int length = knownAccessMethods.length; i < length; ++i) {
                  if (knownAccessMethods[i] != null
                     && CharOperation.equals(this.selector, knownAccessMethods[i].selector)
                     && this.areParametersEqual(methods[i])) {
                     needRename = true;
                     break;
                  }
               }
            }
         }

         if (needRename) {
            this.setSelector(CharOperation.concat(selector, String.valueOf(++methodId).toCharArray()));
         }
      } while(needRename);

      this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart;
   }

   public SyntheticMethodBinding(MethodBinding targetMethod, boolean isSuperAccess, ReferenceBinding declaringClass) {
      if (targetMethod.isConstructor()) {
         this.initializeConstructorAccessor(targetMethod);
      } else {
         this.initializeMethodAccessor(targetMethod, isSuperAccess, declaringClass);
      }
   }

   public SyntheticMethodBinding(MethodBinding overridenMethodToBridge, MethodBinding targetMethod, SourceTypeBinding declaringClass) {
      this.declaringClass = declaringClass;
      this.selector = overridenMethodToBridge.selector;
      this.modifiers = (targetMethod.modifiers | 64 | 4096) & -1073743153;
      this.tagBits |= 25769803776L;
      this.returnType = overridenMethodToBridge.returnType;
      this.parameters = overridenMethodToBridge.parameters;
      this.thrownExceptions = overridenMethodToBridge.thrownExceptions;
      this.targetMethod = targetMethod;
      this.purpose = 8;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(SourceTypeBinding declaringEnum, char[] selector) {
      this.declaringClass = declaringEnum;
      this.selector = selector;
      this.modifiers = 9;
      this.tagBits |= 25769803776L;
      LookupEnvironment environment = declaringEnum.scope.environment();
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      if (selector == TypeConstants.VALUES) {
         this.returnType = environment.createArrayType(environment.convertToParameterizedType(declaringEnum), 1);
         this.parameters = Binding.NO_PARAMETERS;
         this.purpose = 9;
      } else if (selector == TypeConstants.VALUEOF) {
         this.returnType = environment.convertToParameterizedType(declaringEnum);
         this.parameters = new TypeBinding[]{declaringEnum.scope.getJavaLangString()};
         this.purpose = 10;
      }

      SyntheticMethodBinding[] knownAccessMethods = ((SourceTypeBinding)this.declaringClass).syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
      if (declaringEnum.isStrictfp()) {
         this.modifiers |= 2048;
      }
   }

   public SyntheticMethodBinding(SourceTypeBinding declaringClass) {
      this.declaringClass = declaringClass;
      this.selector = TypeConstants.DESERIALIZE_LAMBDA;
      this.modifiers = 4106;
      this.tagBits |= 25769803776L;
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      this.returnType = declaringClass.scope.getJavaLangObject();
      this.parameters = new TypeBinding[]{declaringClass.scope.getJavaLangInvokeSerializedLambda()};
      this.purpose = 17;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(SourceTypeBinding declaringEnum, int startIndex, int endIndex) {
      this.declaringClass = declaringEnum;
      SyntheticMethodBinding[] knownAccessMethods = declaringEnum.syntheticMethods();
      this.index = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      StringBuffer buffer = new StringBuffer();
      buffer.append(TypeConstants.SYNTHETIC_ENUM_CONSTANT_INITIALIZATION_METHOD_PREFIX).append(this.index);
      this.selector = String.valueOf(buffer).toCharArray();
      this.modifiers = 10;
      this.tagBits |= 25769803776L;
      this.purpose = 12;
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      this.returnType = TypeBinding.VOID;
      this.parameters = Binding.NO_PARAMETERS;
      this.startIndex = startIndex;
      this.endIndex = endIndex;
   }

   public SyntheticMethodBinding(MethodBinding overridenMethodToBridge, SourceTypeBinding declaringClass) {
      this.declaringClass = declaringClass;
      this.selector = overridenMethodToBridge.selector;
      this.modifiers = (overridenMethodToBridge.modifiers | 64 | 4096) & -1073743153;
      this.tagBits |= 25769803776L;
      this.returnType = overridenMethodToBridge.returnType;
      this.parameters = overridenMethodToBridge.parameters;
      this.thrownExceptions = overridenMethodToBridge.thrownExceptions;
      this.targetMethod = overridenMethodToBridge;
      this.purpose = 7;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(int purpose, ArrayBinding arrayType, char[] selector, SourceTypeBinding declaringClass) {
      this.declaringClass = declaringClass;
      this.selector = selector;
      this.modifiers = 4106;
      this.tagBits |= 25769803776L;
      this.returnType = arrayType;
      LookupEnvironment environment = declaringClass.environment;
      if (environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
         if (environment.usesNullTypeAnnotations()) {
            this.returnType = environment.createAnnotatedType(this.returnType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
         } else {
            this.tagBits |= 72057594037927936L;
         }
      }

      this.parameters = new TypeBinding[]{(TypeBinding)(purpose == 14 ? TypeBinding.INT : arrayType)};
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
      this.purpose = purpose;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(LambdaExpression lambda, char[] lambdaName, SourceTypeBinding declaringClass) {
      this.lambda = lambda;
      this.declaringClass = declaringClass;
      this.selector = lambdaName;
      this.modifiers = lambda.binding.modifiers;
      this.tagBits |= 25769803776L | lambda.binding.tagBits & 1024L;
      this.returnType = lambda.binding.returnType;
      this.parameters = lambda.binding.parameters;
      this.thrownExceptions = lambda.binding.thrownExceptions;
      this.purpose = 13;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(ReferenceExpression ref, SourceTypeBinding declaringClass) {
      this.serializableMethodRef = ref;
      this.declaringClass = declaringClass;
      this.selector = ref.binding.selector;
      this.modifiers = ref.binding.modifiers;
      this.tagBits |= 25769803776L | ref.binding.tagBits & 1024L;
      this.returnType = ref.binding.returnType;
      this.parameters = ref.binding.parameters;
      this.thrownExceptions = ref.binding.thrownExceptions;
      this.purpose = 18;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public SyntheticMethodBinding(
      MethodBinding privateConstructor, MethodBinding publicConstructor, char[] selector, TypeBinding[] enclosingInstances, SourceTypeBinding declaringClass
   ) {
      this.declaringClass = declaringClass;
      this.selector = selector;
      this.modifiers = 4106;
      this.tagBits |= 25769803776L;
      this.returnType = publicConstructor.declaringClass;
      int realParametersLength = privateConstructor.parameters.length;
      int enclosingInstancesLength = enclosingInstances.length;
      int parametersLength = enclosingInstancesLength + realParametersLength;
      this.parameters = new TypeBinding[parametersLength];
      System.arraycopy(enclosingInstances, 0, this.parameters, 0, enclosingInstancesLength);
      System.arraycopy(privateConstructor.parameters, 0, this.parameters, enclosingInstancesLength, realParametersLength);
      this.fakePaddedParameters = publicConstructor.parameters.length - realParametersLength;
      this.thrownExceptions = publicConstructor.thrownExceptions;
      this.purpose = 16;
      this.targetMethod = publicConstructor;
      SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
   }

   public void initializeConstructorAccessor(MethodBinding accessedConstructor) {
      this.targetMethod = accessedConstructor;
      this.modifiers = 4096;
      this.tagBits |= 25769803776L;
      SourceTypeBinding sourceType = (SourceTypeBinding)accessedConstructor.declaringClass;
      SyntheticMethodBinding[] knownSyntheticMethods = sourceType.syntheticMethods();
      this.index = knownSyntheticMethods == null ? 0 : knownSyntheticMethods.length;
      this.selector = accessedConstructor.selector;
      this.returnType = accessedConstructor.returnType;
      this.purpose = 6;
      int parametersLength = accessedConstructor.parameters.length;
      this.parameters = new TypeBinding[parametersLength + 1];
      System.arraycopy(accessedConstructor.parameters, 0, this.parameters, 0, parametersLength);
      this.parameters[parametersLength] = accessedConstructor.declaringClass;
      this.thrownExceptions = accessedConstructor.thrownExceptions;
      this.declaringClass = sourceType;

      boolean needRename;
      do {
         needRename = false;
         MethodBinding[] methods = sourceType.methods();
         int i = 0;
         int length = methods.length;

         label63:
         while(true) {
            if (i >= length) {
               if (knownSyntheticMethods == null) {
                  break;
               }

               i = 0;
               length = knownSyntheticMethods.length;

               while(true) {
                  if (i >= length) {
                     break label63;
                  }

                  if (knownSyntheticMethods[i] != null
                     && CharOperation.equals(this.selector, knownSyntheticMethods[i].selector)
                     && this.areParameterErasuresEqual(knownSyntheticMethods[i])) {
                     needRename = true;
                     break label63;
                  }

                  ++i;
               }
            }

            if (CharOperation.equals(this.selector, methods[i].selector) && this.areParameterErasuresEqual(methods[i])) {
               needRename = true;
               break;
            }

            ++i;
         }

         if (needRename) {
            int lengthx = this.parameters.length;
            System.arraycopy(this.parameters, 0, this.parameters = new TypeBinding[lengthx + 1], 0, lengthx);
            this.parameters[lengthx] = this.declaringClass;
         }
      } while(needRename);

      AbstractMethodDeclaration[] methodDecls = sourceType.scope.referenceContext.methods;
      if (methodDecls != null) {
         int i = 0;

         for(int length = methodDecls.length; i < length; ++i) {
            if (methodDecls[i].binding == accessedConstructor) {
               this.sourceStart = methodDecls[i].sourceStart;
               return;
            }
         }
      }
   }

   public void initializeMethodAccessor(MethodBinding accessedMethod, boolean isSuperAccess, ReferenceBinding receiverType) {
      this.targetMethod = accessedMethod;
      if (isSuperAccess && receiverType.isInterface() && !accessedMethod.isStatic()) {
         this.modifiers = 4098;
      } else {
         this.modifiers = 4104;
      }

      this.tagBits |= 25769803776L;
      SourceTypeBinding declaringSourceType = (SourceTypeBinding)receiverType;
      SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
      int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
      this.index = methodId;
      this.selector = CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(methodId).toCharArray());
      this.returnType = accessedMethod.returnType;
      this.purpose = isSuperAccess ? 7 : 5;
      if (!accessedMethod.isStatic() && (!isSuperAccess || !receiverType.isInterface())) {
         this.parameters = new TypeBinding[accessedMethod.parameters.length + 1];
         this.parameters[0] = declaringSourceType;
         System.arraycopy(accessedMethod.parameters, 0, this.parameters, 1, accessedMethod.parameters.length);
      } else {
         this.parameters = accessedMethod.parameters;
      }

      this.thrownExceptions = accessedMethod.thrownExceptions;
      this.declaringClass = declaringSourceType;

      boolean needRename;
      do {
         needRename = false;
         MethodBinding[] methods = declaringSourceType.methods();
         int i = 0;
         int length = methods.length;

         label77:
         while(true) {
            if (i >= length) {
               if (knownAccessMethods == null) {
                  break;
               }

               i = 0;
               length = knownAccessMethods.length;

               while(true) {
                  if (i >= length) {
                     break label77;
                  }

                  if (knownAccessMethods[i] != null
                     && CharOperation.equals(this.selector, knownAccessMethods[i].selector)
                     && this.areParameterErasuresEqual(knownAccessMethods[i])) {
                     needRename = true;
                     break label77;
                  }

                  ++i;
               }
            }

            if (CharOperation.equals(this.selector, methods[i].selector) && this.areParameterErasuresEqual(methods[i])) {
               needRename = true;
               break;
            }

            ++i;
         }

         if (needRename) {
            this.setSelector(CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(++methodId).toCharArray()));
         }
      } while(needRename);

      AbstractMethodDeclaration[] methodDecls = declaringSourceType.scope.referenceContext.methods;
      if (methodDecls != null) {
         int i = 0;

         for(int length = methodDecls.length; i < length; ++i) {
            if (methodDecls[i].binding == accessedMethod) {
               this.sourceStart = methodDecls[i].sourceStart;
               return;
            }
         }
      }
   }

   protected boolean isConstructorRelated() {
      return this.purpose == 6;
   }

   @Override
   public LambdaExpression sourceLambda() {
      return this.lambda;
   }

   public void markNonNull(LookupEnvironment environment) {
      markNonNull(this, this.purpose, environment);
   }

   static void markNonNull(MethodBinding method, int purpose, LookupEnvironment environment) {
      switch(purpose) {
         case 9:
            if (environment.usesNullTypeAnnotations()) {
               TypeBinding elementType = ((ArrayBinding)method.returnType).leafComponentType();
               AnnotationBinding nonNullAnnotation = environment.getNonNullAnnotation();
               elementType = environment.createAnnotatedType(elementType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
               method.returnType = environment.createArrayType(elementType, 1, new AnnotationBinding[]{nonNullAnnotation, null});
            } else {
               method.tagBits |= 72057594037927936L;
            }

            return;
         case 10:
            if (environment.usesNullTypeAnnotations()) {
               method.returnType = environment.createAnnotatedType(method.returnType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
            } else {
               method.tagBits |= 72057594037927936L;
            }

            return;
      }
   }
}
