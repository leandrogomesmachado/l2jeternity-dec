package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ParameterizedQualifiedTypeReference extends ArrayQualifiedTypeReference {
   public TypeReference[][] typeArguments;
   ReferenceBinding[] typesPerToken;

   public ParameterizedQualifiedTypeReference(char[][] tokens, TypeReference[][] typeArguments, int dim, long[] positions) {
      super(tokens, dim, positions);
      this.typeArguments = typeArguments;
      int i = 0;

      for(int max = typeArguments.length; i < max; ++i) {
         TypeReference[] typeArgumentsOnTypeComponent = typeArguments[i];
         if (typeArgumentsOnTypeComponent != null) {
            int j = 0;

            for(int max2 = typeArgumentsOnTypeComponent.length; j < max2; ++j) {
               if ((typeArgumentsOnTypeComponent[j].bits & 1048576) != 0) {
                  this.bits |= 1048576;
                  return;
               }
            }
         }
      }
   }

   public ParameterizedQualifiedTypeReference(
      char[][] tokens, TypeReference[][] typeArguments, int dim, Annotation[][] annotationsOnDimensions, long[] positions
   ) {
      this(tokens, typeArguments, dim, positions);
      this.setAnnotationsOnDimensions(annotationsOnDimensions);
      if (annotationsOnDimensions != null) {
         this.bits |= 1048576;
      }
   }

   @Override
   public void checkBounds(Scope scope) {
      if (this.resolvedType != null && this.resolvedType.isValidBinding()) {
         this.checkBounds((ReferenceBinding)this.resolvedType.leafComponentType(), scope, this.typeArguments.length - 1);
      }
   }

   public void checkBounds(ReferenceBinding type, Scope scope, int index) {
      if (index > 0) {
         ReferenceBinding enclosingType = this.typesPerToken[index - 1];
         if (enclosingType != null) {
            this.checkBounds(enclosingType, scope, index - 1);
         }
      }

      if (type.isParameterizedTypeWithActualArguments()) {
         ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)type;
         ReferenceBinding currentType = parameterizedType.genericType();
         TypeVariableBinding[] typeVariables = currentType.typeVariables();
         if (typeVariables != null) {
            parameterizedType.boundCheck(scope, this.typeArguments[index]);
         }
      }
   }

   @Override
   public TypeReference augmentTypeWithAdditionalDimensions(int additionalDimensions, Annotation[][] additionalAnnotations, boolean isVarargs) {
      int totalDimensions = this.dimensions() + additionalDimensions;
      Annotation[][] allAnnotations = this.getMergedAnnotationsOnDimensions(additionalDimensions, additionalAnnotations);
      ParameterizedQualifiedTypeReference pqtr = new ParameterizedQualifiedTypeReference(
         this.tokens, this.typeArguments, totalDimensions, allAnnotations, this.sourcePositions
      );
      pqtr.annotations = this.annotations;
      pqtr.bits |= this.bits & 1048576;
      if (!isVarargs) {
         pqtr.extendedDimensions = additionalDimensions;
      }

      return pqtr;
   }

   @Override
   public boolean isParameterizedTypeReference() {
      return true;
   }

   @Override
   public boolean hasNullTypeAnnotation(TypeReference.AnnotationPosition position) {
      if (super.hasNullTypeAnnotation(position)) {
         return true;
      } else {
         if (position == TypeReference.AnnotationPosition.ANY) {
            if (this.resolvedType != null && !this.resolvedType.hasNullTypeAnnotations()) {
               return false;
            }

            if (this.typeArguments != null) {
               for(int i = 0; i < this.typeArguments.length; ++i) {
                  TypeReference[] arguments = this.typeArguments[i];
                  if (arguments != null) {
                     for(int j = 0; j < arguments.length; ++j) {
                        if (arguments[j].hasNullTypeAnnotation(position)) {
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public char[][] getParameterizedTypeName() {
      int length = this.tokens.length;
      char[][] qParamName = new char[length][];

      for(int i = 0; i < length; ++i) {
         TypeReference[] arguments = this.typeArguments[i];
         if (arguments == null) {
            qParamName[i] = this.tokens[i];
         } else {
            StringBuffer buffer = new StringBuffer(5);
            buffer.append(this.tokens[i]);
            buffer.append('<');
            int j = 0;

            for(int argLength = arguments.length; j < argLength; ++j) {
               if (j > 0) {
                  buffer.append(',');
               }

               buffer.append(CharOperation.concatWith(arguments[j].getParameterizedTypeName(), '.'));
            }

            buffer.append('>');
            j = buffer.length();
            qParamName[i] = new char[j];
            buffer.getChars(0, j, qParamName[i], 0);
         }
      }

      int dim = this.dimensions;
      if (dim > 0) {
         char[] dimChars = new char[dim * 2];

         for(int i = 0; i < dim; ++i) {
            int index = i * 2;
            dimChars[index] = '[';
            dimChars[index + 1] = ']';
         }

         qParamName[length - 1] = CharOperation.concat(qParamName[length - 1], dimChars);
      }

      return qParamName;
   }

   @Override
   public TypeReference[][] getTypeArguments() {
      return this.typeArguments;
   }

   @Override
   protected TypeBinding getTypeBinding(Scope scope) {
      return null;
   }

   private TypeBinding internalResolveType(Scope scope, boolean checkBounds, int location) {
      this.constant = Constant.NotAConstant;
      if ((this.bits & 262144) == 0 || this.resolvedType == null) {
         this.bits |= 262144;
         TypeBinding type = this.internalResolveLeafType(scope, checkBounds);
         this.createArrayType(scope);
         this.resolveAnnotations(scope, location);
         if (this.typeArguments != null) {
            this.checkIllegalNullAnnotations(scope, this.typeArguments[this.typeArguments.length - 1]);
         }

         return type == null ? type : this.resolvedType;
      } else if (this.resolvedType.isValidBinding()) {
         return this.resolvedType;
      } else {
         switch(this.resolvedType.problemId()) {
            case 1:
            case 2:
            case 5:
               return this.resolvedType.closestMatch();
            case 3:
            case 4:
            default:
               return null;
         }
      }
   }

   private TypeBinding internalResolveLeafType(Scope scope, boolean checkBounds) {
      boolean isClassScope = scope.kind == 3;
      Binding binding = scope.getPackage(this.tokens);
      if (binding != null && !binding.isValidBinding()) {
         this.resolvedType = (ReferenceBinding)binding;
         this.reportInvalidType(scope);
         int i = 0;

         for(int max = this.tokens.length; i < max; ++i) {
            TypeReference[] args = this.typeArguments[i];
            if (args != null) {
               for(TypeReference typeArgument : args) {
                  if (isClassScope) {
                     typeArgument.resolveType((ClassScope)scope);
                  } else {
                     typeArgument.resolveType((BlockScope)scope, checkBounds);
                  }
               }
            }
         }

         return null;
      } else {
         PackageBinding packageBinding = binding == null ? null : (PackageBinding)binding;
         this.rejectAnnotationsOnPackageQualifiers(scope, packageBinding);
         boolean typeIsConsistent = true;
         ReferenceBinding qualifyingType = null;
         int max = this.tokens.length;
         this.typesPerToken = new ReferenceBinding[max];

         for(int i = packageBinding == null ? 0 : packageBinding.compoundName.length; i < max; ++i) {
            this.findNextTypeBinding(i, scope, packageBinding);
            if (!this.resolvedType.isValidBinding()) {
               this.reportInvalidType(scope);

               for(int j = i; j < max; ++j) {
                  TypeReference[] args = this.typeArguments[j];
                  if (args != null) {
                     for(TypeReference typeArgument : args) {
                        if (isClassScope) {
                           typeArgument.resolveType((ClassScope)scope);
                        } else {
                           typeArgument.resolveType((BlockScope)scope);
                        }
                     }
                  }
               }

               return null;
            }

            ReferenceBinding currentType = (ReferenceBinding)this.resolvedType;
            if (qualifyingType == null) {
               qualifyingType = currentType.enclosingType();
               if (qualifyingType != null) {
                  qualifyingType = currentType.isStatic()
                     ? (ReferenceBinding)scope.environment().convertToRawType(qualifyingType, false)
                     : scope.environment().convertToParameterizedType(qualifyingType);
               }
            } else {
               if (this.annotations != null) {
                  rejectAnnotationsOnStaticMemberQualififer(scope, currentType, this.annotations[i - 1]);
               }

               if (typeIsConsistent && currentType.isStatic() && (qualifyingType.isParameterizedTypeWithActualArguments() || qualifyingType.isGenericType())) {
                  scope.problemReporter()
                     .staticMemberOfParameterizedType(
                        this, scope.environment().createParameterizedType((ReferenceBinding)currentType.erasure(), null, qualifyingType), i
                     );
                  typeIsConsistent = false;
               }

               ReferenceBinding enclosingType = currentType.enclosingType();
               if (enclosingType != null && TypeBinding.notEquals(enclosingType.erasure(), qualifyingType.erasure())) {
                  qualifyingType = enclosingType;
               }
            }

            TypeReference[] args = this.typeArguments[i];
            if (args != null) {
               TypeReference keep = null;
               if (isClassScope) {
                  keep = ((ClassScope)scope).superTypeReference;
                  ((ClassScope)scope).superTypeReference = null;
               }

               int argLength = args.length;
               boolean isDiamond = argLength == 0 && i == max - 1 && (this.bits & 524288) != 0;
               TypeBinding[] argTypes = new TypeBinding[argLength];
               boolean argHasError = false;
               ReferenceBinding currentOriginal = (ReferenceBinding)currentType.original();

               for(int j = 0; j < argLength; ++j) {
                  TypeReference arg = args[j];
                  TypeBinding argType = isClassScope
                     ? arg.resolveTypeArgument((ClassScope)scope, currentOriginal, j)
                     : arg.resolveTypeArgument((BlockScope)scope, currentOriginal, j);
                  if (argType == null) {
                     argHasError = true;
                  } else {
                     argTypes[j] = argType;
                  }
               }

               if (argHasError) {
                  return null;
               }

               if (isClassScope) {
                  ((ClassScope)scope).superTypeReference = keep;
                  if (((ClassScope)scope).detectHierarchyCycle(currentOriginal, this)) {
                     return null;
                  }
               }

               TypeVariableBinding[] typeVariables = currentOriginal.typeVariables();
               if (typeVariables == Binding.NO_TYPE_VARIABLES) {
                  if (scope.compilerOptions().originalSourceLevel >= 3211264L) {
                     scope.problemReporter().nonGenericTypeCannotBeParameterized(i, this, currentType, argTypes);
                     return null;
                  }

                  this.resolvedType = (TypeBinding)(qualifyingType != null && qualifyingType.isParameterizedType()
                     ? scope.environment().createParameterizedType(currentOriginal, null, qualifyingType)
                     : currentType);
                  return this.resolvedType;
               }

               if (argLength != typeVariables.length && !isDiamond) {
                  scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes, i);
                  return null;
               }

               if (typeIsConsistent && !currentType.isStatic()) {
                  ReferenceBinding actualEnclosing = currentType.enclosingType();
                  if (actualEnclosing != null && actualEnclosing.isRawType()) {
                     scope.problemReporter()
                        .rawMemberTypeCannotBeParameterized(this, scope.environment().createRawType(currentOriginal, actualEnclosing), argTypes);
                     typeIsConsistent = false;
                  }
               }

               ParameterizedTypeBinding parameterizedType = scope.environment().createParameterizedType(currentOriginal, argTypes, qualifyingType);
               if (!isDiamond) {
                  if (checkBounds) {
                     parameterizedType.boundCheck(scope, args);
                  } else {
                     scope.deferBoundCheck(this);
                  }
               } else {
                  parameterizedType.arguments = ParameterizedSingleTypeReference.DIAMOND_TYPE_ARGUMENTS;
               }

               qualifyingType = parameterizedType;
            } else {
               ReferenceBinding currentOriginal = (ReferenceBinding)currentType.original();
               if (isClassScope && ((ClassScope)scope).detectHierarchyCycle(currentOriginal, this)) {
                  return null;
               }

               if (currentOriginal.isGenericType()) {
                  if (typeIsConsistent && qualifyingType != null && qualifyingType.isParameterizedType() && !currentOriginal.isStatic()) {
                     scope.problemReporter()
                        .parameterizedMemberTypeMissingArguments(this, scope.environment().createParameterizedType(currentOriginal, null, qualifyingType), i);
                     typeIsConsistent = false;
                  }

                  qualifyingType = scope.environment().createRawType(currentOriginal, qualifyingType);
               } else {
                  qualifyingType = (ReferenceBinding)(qualifyingType != null && qualifyingType.isParameterizedType()
                     ? scope.environment().createParameterizedType(currentOriginal, null, qualifyingType)
                     : currentType);
               }
            }

            if (this.isTypeUseDeprecated(qualifyingType, scope)) {
               this.reportDeprecatedType(qualifyingType, scope, i);
            }

            this.resolvedType = qualifyingType;
            this.typesPerToken[i] = qualifyingType;
            this.recordResolution(scope.environment(), this.resolvedType);
         }

         return this.resolvedType;
      }
   }

   private void createArrayType(Scope scope) {
      if (this.dimensions > 0) {
         if (this.dimensions > 255) {
            scope.problemReporter().tooManyDimensions(this);
         }

         this.resolvedType = scope.createArrayType(this.resolvedType, this.dimensions);
      }
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      int length = this.tokens.length;

      for(int i = 0; i < length - 1; ++i) {
         if (this.annotations != null && this.annotations[i] != null) {
            printAnnotations(this.annotations[i], output);
            output.append(' ');
         }

         output.append(this.tokens[i]);
         TypeReference[] typeArgument = this.typeArguments[i];
         if (typeArgument != null) {
            output.append('<');
            int typeArgumentLength = typeArgument.length;
            if (typeArgumentLength > 0) {
               int max = typeArgumentLength - 1;

               for(int j = 0; j < max; ++j) {
                  typeArgument[j].print(0, output);
                  output.append(", ");
               }

               typeArgument[max].print(0, output);
            }

            output.append('>');
         }

         output.append('.');
      }

      if (this.annotations != null && this.annotations[length - 1] != null) {
         output.append(" ");
         printAnnotations(this.annotations[length - 1], output);
         output.append(' ');
      }

      output.append(this.tokens[length - 1]);
      TypeReference[] typeArgument = this.typeArguments[length - 1];
      if (typeArgument != null) {
         output.append('<');
         int typeArgumentLength = typeArgument.length;
         if (typeArgumentLength > 0) {
            int max = typeArgumentLength - 1;

            for(int j = 0; j < max; ++j) {
               typeArgument[j].print(0, output);
               output.append(", ");
            }

            typeArgument[max].print(0, output);
         }

         output.append('>');
      }

      Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions();
      if ((this.bits & 16384) != 0) {
         for(int i = 0; i < this.dimensions - 1; ++i) {
            if (annotationsOnDimensions != null && annotationsOnDimensions[i] != null) {
               output.append(" ");
               printAnnotations(annotationsOnDimensions[i], output);
               output.append(" ");
            }

            output.append("[]");
         }

         if (annotationsOnDimensions != null && annotationsOnDimensions[this.dimensions - 1] != null) {
            output.append(" ");
            printAnnotations(annotationsOnDimensions[this.dimensions - 1], output);
            output.append(" ");
         }

         output.append("...");
      } else {
         for(int i = 0; i < this.dimensions; ++i) {
            if (annotationsOnDimensions != null && annotationsOnDimensions[i] != null) {
               output.append(" ");
               printAnnotations(annotationsOnDimensions[i], output);
               output.append(" ");
            }

            output.append("[]");
         }
      }

      return output;
   }

   @Override
   public TypeBinding resolveType(BlockScope scope, boolean checkBounds, int location) {
      return this.internalResolveType(scope, checkBounds, location);
   }

   @Override
   public TypeBinding resolveType(ClassScope scope, int location) {
      return this.internalResolveType(scope, false, location);
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLevels = this.annotations.length;

            for(int i = 0; i < annotationsLevels; ++i) {
               int annotationsLength = this.annotations[i] == null ? 0 : this.annotations[i].length;

               for(int j = 0; j < annotationsLength; ++j) {
                  this.annotations[i][j].traverse(visitor, scope);
               }
            }
         }

         Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(true);
         if (annotationsOnDimensions != null) {
            int i = 0;

            for(int max = annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = annotationsOnDimensions[i];
               int j = 0;

               for(int max2 = annotations2 == null ? 0 : annotations2.length; j < max2; ++j) {
                  Annotation annotation = annotations2[j];
                  annotation.traverse(visitor, scope);
               }
            }
         }

         int i = 0;

         for(int max = this.typeArguments.length; i < max; ++i) {
            if (this.typeArguments[i] != null) {
               int j = 0;

               for(int max2 = this.typeArguments[i].length; j < max2; ++j) {
                  this.typeArguments[i][j].traverse(visitor, scope);
               }
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLevels = this.annotations.length;

            for(int i = 0; i < annotationsLevels; ++i) {
               int annotationsLength = this.annotations[i] == null ? 0 : this.annotations[i].length;

               for(int j = 0; j < annotationsLength; ++j) {
                  this.annotations[i][j].traverse(visitor, scope);
               }
            }
         }

         Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(true);
         if (annotationsOnDimensions != null) {
            int i = 0;

            for(int max = annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = annotationsOnDimensions[i];
               int j = 0;

               for(int max2 = annotations2 == null ? 0 : annotations2.length; j < max2; ++j) {
                  Annotation annotation = annotations2[j];
                  annotation.traverse(visitor, scope);
               }
            }
         }

         int i = 0;

         for(int max = this.typeArguments.length; i < max; ++i) {
            if (this.typeArguments[i] != null) {
               int j = 0;

               for(int max2 = this.typeArguments[i].length; j < max2; ++j) {
                  this.typeArguments[i][j].traverse(visitor, scope);
               }
            }
         }
      }

      visitor.endVisit(this, scope);
   }
}
