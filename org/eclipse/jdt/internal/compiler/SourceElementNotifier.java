package org.eclipse.jdt.internal.compiler;

import java.util.ArrayList;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.FieldInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.MethodInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.ParameterInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeInfo;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor.TypeParameterInfo;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;

public class SourceElementNotifier {
   ISourceElementRequestor requestor;
   boolean reportReferenceInfo;
   char[][] typeNames;
   char[][] superTypeNames;
   int nestedTypeIndex;
   SourceElementNotifier.LocalDeclarationVisitor localDeclarationVisitor = null;
   HashtableOfObjectToInt sourceEnds;
   Map nodesToCategories;
   int initialPosition;
   int eofPosition;

   public SourceElementNotifier(ISourceElementRequestor requestor, boolean reportLocalDeclarations) {
      this.requestor = requestor;
      if (reportLocalDeclarations) {
         this.localDeclarationVisitor = new SourceElementNotifier.LocalDeclarationVisitor();
      }

      this.typeNames = new char[4][];
      this.superTypeNames = new char[4][];
      this.nestedTypeIndex = 0;
   }

   protected Object[][] getArgumentInfos(Argument[] arguments) {
      int argumentLength = arguments.length;
      char[][] argumentTypes = new char[argumentLength][];
      char[][] argumentNames = new char[argumentLength][];
      ParameterInfo[] parameterInfos = new ParameterInfo[argumentLength];

      for(int i = 0; i < argumentLength; ++i) {
         Argument argument = arguments[i];
         argumentTypes[i] = CharOperation.concatWith(argument.type.getParameterizedTypeName(), '.');
         char[] name = argument.name;
         argumentNames[i] = name;
         ParameterInfo parameterInfo = new ParameterInfo();
         parameterInfo.declarationStart = argument.declarationSourceStart;
         parameterInfo.declarationEnd = argument.declarationSourceEnd;
         parameterInfo.nameSourceStart = argument.sourceStart;
         parameterInfo.nameSourceEnd = argument.sourceEnd;
         parameterInfo.modifiers = argument.modifiers;
         parameterInfo.name = name;
         parameterInfos[i] = parameterInfo;
      }

      return new Object[][]{parameterInfos, {argumentTypes, argumentNames}};
   }

   protected char[][] getInterfaceNames(TypeDeclaration typeDeclaration) {
      char[][] interfaceNames = null;
      int superInterfacesLength = 0;
      TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
      if (superInterfaces != null) {
         superInterfacesLength = superInterfaces.length;
         interfaceNames = new char[superInterfacesLength][];
      } else if ((typeDeclaration.bits & 512) != 0) {
         QualifiedAllocationExpression alloc = typeDeclaration.allocation;
         if (alloc != null && alloc.type != null) {
            superInterfaces = new TypeReference[]{alloc.type};
            superInterfacesLength = 1;
            interfaceNames = new char[1][];
         }
      }

      if (superInterfaces != null) {
         for(int i = 0; i < superInterfacesLength; ++i) {
            interfaceNames[i] = CharOperation.concatWith(superInterfaces[i].getParameterizedTypeName(), '.');
         }
      }

      return interfaceNames;
   }

   protected char[] getSuperclassName(TypeDeclaration typeDeclaration) {
      TypeReference superclass = typeDeclaration.superclass;
      return superclass != null ? CharOperation.concatWith(superclass.getParameterizedTypeName(), '.') : null;
   }

   protected char[][] getThrownExceptions(AbstractMethodDeclaration methodDeclaration) {
      char[][] thrownExceptionTypes = null;
      TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
      if (thrownExceptions != null) {
         int thrownExceptionLength = thrownExceptions.length;
         thrownExceptionTypes = new char[thrownExceptionLength][];

         for(int i = 0; i < thrownExceptionLength; ++i) {
            thrownExceptionTypes[i] = CharOperation.concatWith(thrownExceptions[i].getParameterizedTypeName(), '.');
         }
      }

      return thrownExceptionTypes;
   }

   protected char[][] getTypeParameterBounds(TypeParameter typeParameter) {
      TypeReference firstBound = typeParameter.type;
      TypeReference[] otherBounds = typeParameter.bounds;
      char[][] typeParameterBounds = null;
      if (firstBound != null) {
         if (otherBounds != null) {
            int otherBoundsLength = otherBounds.length;
            char[][] boundNames = new char[otherBoundsLength + 1][];
            boundNames[0] = CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.');

            for(int j = 0; j < otherBoundsLength; ++j) {
               boundNames[j + 1] = CharOperation.concatWith(otherBounds[j].getParameterizedTypeName(), '.');
            }

            typeParameterBounds = boundNames;
         } else {
            typeParameterBounds = new char[][]{CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.')};
         }
      } else {
         typeParameterBounds = CharOperation.NO_CHAR_CHAR;
      }

      return typeParameterBounds;
   }

   private TypeParameterInfo[] getTypeParameterInfos(TypeParameter[] typeParameters) {
      if (typeParameters == null) {
         return null;
      } else {
         int typeParametersLength = typeParameters.length;
         TypeParameterInfo[] result = new TypeParameterInfo[typeParametersLength];

         for(int i = 0; i < typeParametersLength; ++i) {
            TypeParameter typeParameter = typeParameters[i];
            char[][] typeParameterBounds = this.getTypeParameterBounds(typeParameter);
            TypeParameterInfo typeParameterInfo = new TypeParameterInfo();
            typeParameterInfo.typeAnnotated = (typeParameter.bits & 1048576) != 0;
            typeParameterInfo.declarationStart = typeParameter.declarationSourceStart;
            typeParameterInfo.declarationEnd = typeParameter.declarationSourceEnd;
            typeParameterInfo.name = typeParameter.name;
            typeParameterInfo.nameSourceStart = typeParameter.sourceStart;
            typeParameterInfo.nameSourceEnd = typeParameter.sourceEnd;
            typeParameterInfo.bounds = typeParameterBounds;
            result[i] = typeParameterInfo;
         }

         return result;
      }
   }

   private boolean hasDeprecatedAnnotation(Annotation[] annotations) {
      if (annotations != null) {
         int i = 0;

         for(int length = annotations.length; i < length; ++i) {
            Annotation annotation = annotations[i];
            if (CharOperation.equals(annotation.type.getLastToken(), TypeConstants.JAVA_LANG_DEPRECATED[2])) {
               return true;
            }
         }
      }

      return false;
   }

   protected void notifySourceElementRequestor(AbstractMethodDeclaration methodDeclaration, TypeDeclaration declaringType, ImportReference currentPackage) {
      boolean isInRange = this.initialPosition <= methodDeclaration.declarationSourceStart && this.eofPosition >= methodDeclaration.declarationSourceEnd;
      if (methodDeclaration.isClinit()) {
         this.visitIfNeeded(methodDeclaration);
      } else if (methodDeclaration.isDefaultConstructor()) {
         if (this.reportReferenceInfo) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)methodDeclaration;
            ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
            if (constructorCall != null) {
               switch(constructorCall.accessMode) {
                  case 1:
                  case 2:
                     this.requestor
                        .acceptConstructorReference(
                           this.superTypeNames[this.nestedTypeIndex - 1],
                           constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
                           constructorCall.sourceStart
                        );
                     break;
                  case 3:
                     this.requestor
                        .acceptConstructorReference(
                           this.typeNames[this.nestedTypeIndex - 1],
                           constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
                           constructorCall.sourceStart
                        );
               }
            }
         }
      } else {
         char[][] argumentTypes = null;
         char[][] argumentNames = null;
         boolean isVarArgs = false;
         Argument[] arguments = methodDeclaration.arguments;
         ParameterInfo[] parameterInfos = null;
         MethodInfo methodInfo = new MethodInfo();
         methodInfo.typeAnnotated = (methodDeclaration.bits & 1048576) != 0;
         if (arguments != null) {
            Object[][] argumentInfos = this.getArgumentInfos(arguments);
            parameterInfos = (ParameterInfo[])argumentInfos[0];
            argumentTypes = (char[][])argumentInfos[1][0];
            argumentNames = (char[][])argumentInfos[1][1];
            isVarArgs = arguments[arguments.length - 1].isVarArgs();
         }

         char[][] thrownExceptionTypes = this.getThrownExceptions(methodDeclaration);
         int selectorSourceEnd = -1;
         if (methodDeclaration.isConstructor()) {
            selectorSourceEnd = this.sourceEnds.get(methodDeclaration);
            if (isInRange) {
               int currentModifiers = methodDeclaration.modifiers;
               currentModifiers &= 1114111;
               if (isVarArgs) {
                  currentModifiers |= 128;
               }

               if (this.hasDeprecatedAnnotation(methodDeclaration.annotations)) {
                  currentModifiers |= 1048576;
               }

               methodInfo.isConstructor = true;
               methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
               methodInfo.modifiers = currentModifiers;
               methodInfo.name = methodDeclaration.selector;
               methodInfo.nameSourceStart = methodDeclaration.sourceStart;
               methodInfo.nameSourceEnd = selectorSourceEnd;
               methodInfo.parameterTypes = argumentTypes;
               methodInfo.parameterNames = argumentNames;
               methodInfo.exceptionTypes = thrownExceptionTypes;
               methodInfo.typeParameters = this.getTypeParameterInfos(methodDeclaration.typeParameters());
               methodInfo.parameterInfos = parameterInfos;
               methodInfo.categories = (char[][])this.nodesToCategories.get(methodDeclaration);
               methodInfo.annotations = methodDeclaration.annotations;
               methodInfo.declaringPackageName = currentPackage == null ? CharOperation.NO_CHAR : CharOperation.concatWith(currentPackage.tokens, '.');
               methodInfo.declaringTypeModifiers = declaringType.modifiers;
               methodInfo.extraFlags = ExtraFlags.getExtraFlags(declaringType);
               methodInfo.node = methodDeclaration;
               this.requestor.enterConstructor(methodInfo);
            }

            if (this.reportReferenceInfo) {
               ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)methodDeclaration;
               ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
               if (constructorCall != null) {
                  switch(constructorCall.accessMode) {
                     case 1:
                     case 2:
                        this.requestor
                           .acceptConstructorReference(
                              this.superTypeNames[this.nestedTypeIndex - 1],
                              constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
                              constructorCall.sourceStart
                           );
                        break;
                     case 3:
                        this.requestor
                           .acceptConstructorReference(
                              this.typeNames[this.nestedTypeIndex - 1],
                              constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
                              constructorCall.sourceStart
                           );
                  }
               }
            }

            this.visitIfNeeded(methodDeclaration);
            if (isInRange) {
               this.requestor.exitConstructor(methodDeclaration.declarationSourceEnd);
            }
         } else {
            selectorSourceEnd = this.sourceEnds.get(methodDeclaration);
            if (isInRange) {
               int currentModifiers = methodDeclaration.modifiers;
               currentModifiers &= 1310719;
               if (isVarArgs) {
                  currentModifiers |= 128;
               }

               if (this.hasDeprecatedAnnotation(methodDeclaration.annotations)) {
                  currentModifiers |= 1048576;
               }

               TypeReference returnType = methodDeclaration instanceof MethodDeclaration ? ((MethodDeclaration)methodDeclaration).returnType : null;
               methodInfo.isAnnotation = methodDeclaration instanceof AnnotationMethodDeclaration;
               methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
               methodInfo.modifiers = currentModifiers;
               methodInfo.returnType = returnType == null ? null : CharOperation.concatWith(returnType.getParameterizedTypeName(), '.');
               methodInfo.name = methodDeclaration.selector;
               methodInfo.nameSourceStart = methodDeclaration.sourceStart;
               methodInfo.nameSourceEnd = selectorSourceEnd;
               methodInfo.parameterTypes = argumentTypes;
               methodInfo.parameterNames = argumentNames;
               methodInfo.exceptionTypes = thrownExceptionTypes;
               methodInfo.typeParameters = this.getTypeParameterInfos(methodDeclaration.typeParameters());
               methodInfo.parameterInfos = parameterInfos;
               methodInfo.categories = (char[][])this.nodesToCategories.get(methodDeclaration);
               methodInfo.annotations = methodDeclaration.annotations;
               methodInfo.node = methodDeclaration;
               methodInfo.enclosingType = declaringType;
               methodInfo.declaringPackageName = currentPackage == null ? CharOperation.NO_CHAR : CharOperation.concatWith(currentPackage.tokens, '.');
               this.requestor.enterMethod(methodInfo);
            }

            this.visitIfNeeded(methodDeclaration);
            if (isInRange) {
               if (methodDeclaration instanceof AnnotationMethodDeclaration) {
                  AnnotationMethodDeclaration annotationMethodDeclaration = (AnnotationMethodDeclaration)methodDeclaration;
                  Expression expression = annotationMethodDeclaration.defaultValue;
                  if (expression != null) {
                     this.requestor.exitMethod(methodDeclaration.declarationSourceEnd, expression);
                     return;
                  }
               }

               this.requestor.exitMethod(methodDeclaration.declarationSourceEnd, null);
            }
         }
      }
   }

   public void notifySourceElementRequestor(
      CompilationUnitDeclaration parsedUnit,
      int sourceStart,
      int sourceEnd,
      boolean reportReference,
      HashtableOfObjectToInt sourceEndsMap,
      Map nodesToCategoriesMap
   ) {
      this.initialPosition = sourceStart;
      this.eofPosition = sourceEnd;
      this.reportReferenceInfo = reportReference;
      this.sourceEnds = sourceEndsMap;
      this.nodesToCategories = nodesToCategoriesMap;

      try {
         boolean isInRange = this.initialPosition <= parsedUnit.sourceStart && this.eofPosition >= parsedUnit.sourceEnd;
         int length = 0;
         ASTNode[] nodes = null;
         if (isInRange) {
            this.requestor.enterCompilationUnit();
         }

         ImportReference currentPackage = parsedUnit.currentPackage;
         if (this.localDeclarationVisitor != null) {
            this.localDeclarationVisitor.currentPackage = currentPackage;
         }

         ImportReference[] imports = parsedUnit.imports;
         TypeDeclaration[] types = parsedUnit.types;
         length = (currentPackage == null ? 0 : 1) + (imports == null ? 0 : imports.length) + (types == null ? 0 : types.length);
         nodes = new ASTNode[length];
         int index = 0;
         if (currentPackage != null) {
            nodes[index++] = currentPackage;
         }

         if (imports != null) {
            int i = 0;

            for(int max = imports.length; i < max; ++i) {
               nodes[index++] = imports[i];
            }
         }

         if (types != null) {
            int i = 0;

            for(int max = types.length; i < max; ++i) {
               nodes[index++] = types[i];
            }
         }

         if (length > 0) {
            quickSort(nodes, 0, length - 1);

            for(int i = 0; i < length; ++i) {
               ASTNode node = nodes[i];
               if (node instanceof ImportReference) {
                  ImportReference importRef = (ImportReference)node;
                  if (node == parsedUnit.currentPackage) {
                     this.notifySourceElementRequestor(importRef, true);
                  } else {
                     this.notifySourceElementRequestor(importRef, false);
                  }
               } else {
                  this.notifySourceElementRequestor((TypeDeclaration)node, true, null, currentPackage);
               }
            }
         }

         if (isInRange) {
            this.requestor.exitCompilationUnit(parsedUnit.sourceEnd);
         }
      } finally {
         this.reset();
      }
   }

   protected void notifySourceElementRequestor(FieldDeclaration fieldDeclaration, TypeDeclaration declaringType) {
      boolean isInRange = this.initialPosition <= fieldDeclaration.declarationSourceStart && this.eofPosition >= fieldDeclaration.declarationSourceEnd;
      switch(fieldDeclaration.getKind()) {
         case 2:
            if (isInRange) {
               this.requestor.enterInitializer(fieldDeclaration.declarationSourceStart, fieldDeclaration.modifiers);
            }

            this.visitIfNeeded((Initializer)fieldDeclaration);
            if (isInRange) {
               this.requestor.exitInitializer(fieldDeclaration.declarationSourceEnd);
            }
            break;
         case 3:
            if (this.reportReferenceInfo && fieldDeclaration.initialization instanceof AllocationExpression) {
               AllocationExpression alloc = (AllocationExpression)fieldDeclaration.initialization;
               this.requestor.acceptConstructorReference(declaringType.name, alloc.arguments == null ? 0 : alloc.arguments.length, alloc.sourceStart);
            }
         case 1:
            int fieldEndPosition = this.sourceEnds.get(fieldDeclaration);
            if (fieldEndPosition == -1) {
               fieldEndPosition = fieldDeclaration.declarationSourceEnd;
            }

            if (isInRange) {
               int currentModifiers = fieldDeclaration.modifiers;
               boolean deprecated = (currentModifiers & 1048576) != 0 || this.hasDeprecatedAnnotation(fieldDeclaration.annotations);
               char[] typeName = null;
               if (fieldDeclaration.type == null) {
                  typeName = declaringType.name;
                  currentModifiers |= 16384;
               } else {
                  typeName = CharOperation.concatWith(fieldDeclaration.type.getParameterizedTypeName(), '.');
               }

               FieldInfo fieldInfo = new FieldInfo();
               fieldInfo.typeAnnotated = (fieldDeclaration.bits & 1048576) != 0;
               fieldInfo.declarationStart = fieldDeclaration.declarationSourceStart;
               fieldInfo.name = fieldDeclaration.name;
               fieldInfo.modifiers = deprecated ? currentModifiers & 65535 | 1048576 : currentModifiers & 65535;
               fieldInfo.type = typeName;
               fieldInfo.nameSourceStart = fieldDeclaration.sourceStart;
               fieldInfo.nameSourceEnd = fieldDeclaration.sourceEnd;
               fieldInfo.categories = (char[][])this.nodesToCategories.get(fieldDeclaration);
               fieldInfo.annotations = fieldDeclaration.annotations;
               fieldInfo.node = fieldDeclaration;
               this.requestor.enterField(fieldInfo);
            }

            this.visitIfNeeded(fieldDeclaration, declaringType);
            if (isInRange) {
               this.requestor
                  .exitField(
                     fieldDeclaration.initialization != null
                           && !(fieldDeclaration.initialization instanceof ArrayInitializer)
                           && !(fieldDeclaration.initialization instanceof AllocationExpression)
                           && !(fieldDeclaration.initialization instanceof ArrayAllocationExpression)
                           && !(fieldDeclaration.initialization instanceof Assignment)
                           && !(fieldDeclaration.initialization instanceof ClassLiteralAccess)
                           && !(fieldDeclaration.initialization instanceof MessageSend)
                           && !(fieldDeclaration.initialization instanceof ArrayReference)
                           && !(fieldDeclaration.initialization instanceof ThisReference)
                        ? fieldDeclaration.initialization.sourceStart
                        : -1,
                     fieldEndPosition,
                     fieldDeclaration.declarationSourceEnd
                  );
            }
      }
   }

   protected void notifySourceElementRequestor(ImportReference importReference, boolean isPackage) {
      if (isPackage) {
         this.requestor.acceptPackage(importReference);
      } else {
         boolean onDemand = (importReference.bits & 131072) != 0;
         this.requestor
            .acceptImport(
               importReference.declarationSourceStart,
               importReference.declarationSourceEnd,
               importReference.sourceStart,
               onDemand ? importReference.trailingStarPosition : importReference.sourceEnd,
               importReference.tokens,
               onDemand,
               importReference.modifiers
            );
      }
   }

   protected void notifySourceElementRequestor(
      TypeDeclaration typeDeclaration, boolean notifyTypePresence, TypeDeclaration declaringType, ImportReference currentPackage
   ) {
      if (!CharOperation.equals(TypeConstants.PACKAGE_INFO_NAME, typeDeclaration.name)) {
         boolean isInRange = this.initialPosition <= typeDeclaration.declarationSourceStart && this.eofPosition >= typeDeclaration.declarationSourceEnd;
         FieldDeclaration[] fields = typeDeclaration.fields;
         AbstractMethodDeclaration[] methods = typeDeclaration.methods;
         TypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
         int fieldCounter = fields == null ? 0 : fields.length;
         int methodCounter = methods == null ? 0 : methods.length;
         int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
         int fieldIndex = 0;
         int methodIndex = 0;
         int memberTypeIndex = 0;
         if (notifyTypePresence) {
            char[][] interfaceNames = this.getInterfaceNames(typeDeclaration);
            int kind = TypeDeclaration.kind(typeDeclaration.modifiers);
            char[] implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.typeAnnotated = (typeDeclaration.bits & 1048576) != 0;
            if (isInRange) {
               int currentModifiers = typeDeclaration.modifiers;
               boolean deprecated = (currentModifiers & 1048576) != 0 || this.hasDeprecatedAnnotation(typeDeclaration.annotations);
               boolean isEnumInit = typeDeclaration.allocation != null && typeDeclaration.allocation.enumConstant != null;
               char[] superclassName;
               if (isEnumInit) {
                  currentModifiers |= 16384;
                  superclassName = declaringType.name;
               } else {
                  superclassName = this.getSuperclassName(typeDeclaration);
               }

               if (typeDeclaration.allocation == null) {
                  typeInfo.declarationStart = typeDeclaration.declarationSourceStart;
               } else if (isEnumInit) {
                  typeInfo.declarationStart = typeDeclaration.allocation.enumConstant.sourceStart;
               } else {
                  typeInfo.declarationStart = typeDeclaration.allocation.sourceStart;
               }

               typeInfo.modifiers = deprecated ? currentModifiers & 65535 | 1048576 : currentModifiers & 65535;
               typeInfo.name = typeDeclaration.name;
               typeInfo.nameSourceStart = isEnumInit ? typeDeclaration.allocation.enumConstant.sourceStart : typeDeclaration.sourceStart;
               typeInfo.nameSourceEnd = this.sourceEnd(typeDeclaration);
               typeInfo.superclass = superclassName;
               typeInfo.superinterfaces = interfaceNames;
               typeInfo.typeParameters = this.getTypeParameterInfos(typeDeclaration.typeParameters);
               typeInfo.categories = (char[][])this.nodesToCategories.get(typeDeclaration);
               typeInfo.secondary = typeDeclaration.isSecondary();
               typeInfo.anonymousMember = typeDeclaration.allocation != null && typeDeclaration.allocation.enclosingInstance != null;
               typeInfo.annotations = typeDeclaration.annotations;
               typeInfo.extraFlags = ExtraFlags.getExtraFlags(typeDeclaration);
               typeInfo.node = typeDeclaration;
               this.requestor.enterType(typeInfo);
               switch(kind) {
                  case 1:
                     if (superclassName != null) {
                        implicitSuperclassName = superclassName;
                     }
                     break;
                  case 2:
                     implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
                     break;
                  case 3:
                     implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ENUM;
                     break;
                  case 4:
                     implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ANNOTATION_ANNOTATION;
               }
            }

            if (this.nestedTypeIndex == this.typeNames.length) {
               System.arraycopy(this.typeNames, 0, this.typeNames = new char[this.nestedTypeIndex * 2][], 0, this.nestedTypeIndex);
               System.arraycopy(this.superTypeNames, 0, this.superTypeNames = new char[this.nestedTypeIndex * 2][], 0, this.nestedTypeIndex);
            }

            this.typeNames[this.nestedTypeIndex] = typeDeclaration.name;
            this.superTypeNames[this.nestedTypeIndex++] = implicitSuperclassName;
         }

         while(fieldIndex < fieldCounter || memberTypeIndex < memberTypeCounter || methodIndex < methodCounter) {
            FieldDeclaration nextFieldDeclaration = null;
            AbstractMethodDeclaration nextMethodDeclaration = null;
            TypeDeclaration nextMemberDeclaration = null;
            int position = Integer.MAX_VALUE;
            int nextDeclarationType = -1;
            if (fieldIndex < fieldCounter) {
               nextFieldDeclaration = fields[fieldIndex];
               if (nextFieldDeclaration.declarationSourceStart < position) {
                  position = nextFieldDeclaration.declarationSourceStart;
                  nextDeclarationType = 0;
               }
            }

            if (methodIndex < methodCounter) {
               nextMethodDeclaration = methods[methodIndex];
               if (nextMethodDeclaration.declarationSourceStart < position) {
                  position = nextMethodDeclaration.declarationSourceStart;
                  nextDeclarationType = 1;
               }
            }

            if (memberTypeIndex < memberTypeCounter) {
               nextMemberDeclaration = memberTypes[memberTypeIndex];
               if (nextMemberDeclaration.declarationSourceStart < position) {
                  position = nextMemberDeclaration.declarationSourceStart;
                  nextDeclarationType = 2;
               }
            }

            switch(nextDeclarationType) {
               case 0:
                  ++fieldIndex;
                  this.notifySourceElementRequestor(nextFieldDeclaration, typeDeclaration);
                  break;
               case 1:
                  ++methodIndex;
                  this.notifySourceElementRequestor(nextMethodDeclaration, typeDeclaration, currentPackage);
                  break;
               case 2:
                  ++memberTypeIndex;
                  this.notifySourceElementRequestor(nextMemberDeclaration, true, null, currentPackage);
            }
         }

         if (notifyTypePresence) {
            if (isInRange) {
               this.requestor.exitType(typeDeclaration.declarationSourceEnd);
            }

            --this.nestedTypeIndex;
         }
      }
   }

   private static void quickSort(ASTNode[] sortedCollection, int left, int right) {
      int original_left = left;
      int original_right = right;
      ASTNode mid = sortedCollection[left + (right - left) / 2];

      do {
         while(sortedCollection[left].sourceStart < mid.sourceStart) {
            ++left;
         }

         while(mid.sourceStart < sortedCollection[right].sourceStart) {
            --right;
         }

         if (left <= right) {
            ASTNode tmp = sortedCollection[left];
            sortedCollection[left] = sortedCollection[right];
            sortedCollection[right] = tmp;
            ++left;
            --right;
         }
      } while(left <= right);

      if (original_left < right) {
         quickSort(sortedCollection, original_left, right);
      }

      if (left < original_right) {
         quickSort(sortedCollection, left, original_right);
      }
   }

   private void reset() {
      this.typeNames = new char[4][];
      this.superTypeNames = new char[4][];
      this.nestedTypeIndex = 0;
      this.sourceEnds = null;
   }

   private int sourceEnd(TypeDeclaration typeDeclaration) {
      if ((typeDeclaration.bits & 512) != 0) {
         QualifiedAllocationExpression allocation = typeDeclaration.allocation;
         return allocation.enumConstant != null ? allocation.enumConstant.sourceEnd : allocation.type.sourceEnd;
      } else {
         return typeDeclaration.sourceEnd;
      }
   }

   private void visitIfNeeded(AbstractMethodDeclaration method) {
      if (this.localDeclarationVisitor != null && (method.bits & 2) != 0) {
         if (method instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)method;
            if (constructorDeclaration.constructorCall != null) {
               constructorDeclaration.constructorCall.traverse(this.localDeclarationVisitor, method.scope);
            }
         }

         if (method.statements != null) {
            int statementsLength = method.statements.length;

            for(int i = 0; i < statementsLength; ++i) {
               method.statements[i].traverse(this.localDeclarationVisitor, method.scope);
            }
         }
      }
   }

   private void visitIfNeeded(FieldDeclaration field, TypeDeclaration declaringType) {
      if (this.localDeclarationVisitor != null && (field.bits & 2) != 0 && field.initialization != null) {
         try {
            this.localDeclarationVisitor.pushDeclaringType(declaringType);
            field.initialization.traverse(this.localDeclarationVisitor, null);
         } finally {
            this.localDeclarationVisitor.popDeclaringType();
         }
      }
   }

   private void visitIfNeeded(Initializer initializer) {
      if (this.localDeclarationVisitor != null && (initializer.bits & 2) != 0 && initializer.block != null) {
         initializer.block.traverse(this.localDeclarationVisitor, null);
      }
   }

   public class LocalDeclarationVisitor extends ASTVisitor {
      public ImportReference currentPackage;
      ArrayList declaringTypes;

      public void pushDeclaringType(TypeDeclaration declaringType) {
         if (this.declaringTypes == null) {
            this.declaringTypes = new ArrayList();
         }

         this.declaringTypes.add(declaringType);
      }

      public void popDeclaringType() {
         this.declaringTypes.remove(this.declaringTypes.size() - 1);
      }

      public TypeDeclaration peekDeclaringType() {
         if (this.declaringTypes == null) {
            return null;
         } else {
            int size = this.declaringTypes.size();
            return size == 0 ? null : (TypeDeclaration)this.declaringTypes.get(size - 1);
         }
      }

      @Override
      public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
         SourceElementNotifier.this.notifySourceElementRequestor(typeDeclaration, true, this.peekDeclaringType(), this.currentPackage);
         return false;
      }

      @Override
      public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
         SourceElementNotifier.this.notifySourceElementRequestor(typeDeclaration, true, this.peekDeclaringType(), this.currentPackage);
         return false;
      }
   }
}
