package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class Javadoc extends ASTNode {
   public JavadocSingleNameReference[] paramReferences;
   public JavadocSingleTypeReference[] paramTypeParameters;
   public TypeReference[] exceptionReferences;
   public JavadocReturnStatement returnStatement;
   public Expression[] seeReferences;
   public long[] inheritedPositions = null;
   public JavadocSingleNameReference[] invalidParameters;
   public long valuePositions = -1L;

   public Javadoc(int sourceStart, int sourceEnd) {
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
      this.bits |= 65536;
   }

   boolean canBeSeen(int visibility, int modifiers) {
      if (modifiers < 0) {
         return true;
      } else {
         switch(modifiers & 7) {
            case 0:
               if (visibility != 0 && visibility != 2) {
                  return false;
               }

               return true;
            case 1:
               return true;
            case 2:
               if (visibility == 2) {
                  return true;
               }

               return false;
            case 3:
            default:
               return true;
            case 4:
               return visibility != 1;
         }
      }
   }

   public ASTNode getNodeStartingAt(int start) {
      int length = 0;
      if (this.paramReferences != null) {
         length = this.paramReferences.length;

         for(int i = 0; i < length; ++i) {
            JavadocSingleNameReference param = this.paramReferences[i];
            if (param.sourceStart == start) {
               return param;
            }
         }
      }

      if (this.invalidParameters != null) {
         length = this.invalidParameters.length;

         for(int i = 0; i < length; ++i) {
            JavadocSingleNameReference param = this.invalidParameters[i];
            if (param.sourceStart == start) {
               return param;
            }
         }
      }

      if (this.paramTypeParameters != null) {
         length = this.paramTypeParameters.length;

         for(int i = 0; i < length; ++i) {
            JavadocSingleTypeReference param = this.paramTypeParameters[i];
            if (param.sourceStart == start) {
               return param;
            }
         }
      }

      if (this.exceptionReferences != null) {
         length = this.exceptionReferences.length;

         for(int i = 0; i < length; ++i) {
            TypeReference typeRef = this.exceptionReferences[i];
            if (typeRef.sourceStart == start) {
               return typeRef;
            }
         }
      }

      if (this.seeReferences != null) {
         length = this.seeReferences.length;

         for(int i = 0; i < length; ++i) {
            Expression expression = this.seeReferences[i];
            if (expression.sourceStart == start) {
               return expression;
            }

            if (expression instanceof JavadocAllocationExpression) {
               JavadocAllocationExpression allocationExpr = (JavadocAllocationExpression)this.seeReferences[i];
               if (allocationExpr.binding != null && allocationExpr.binding.isValidBinding() && allocationExpr.arguments != null) {
                  int j = 0;

                  for(int l = allocationExpr.arguments.length; j < l; ++j) {
                     if (allocationExpr.arguments[j].sourceStart == start) {
                        return allocationExpr.arguments[j];
                     }
                  }
               }
            } else if (expression instanceof JavadocMessageSend) {
               JavadocMessageSend messageSend = (JavadocMessageSend)this.seeReferences[i];
               if (messageSend.binding != null && messageSend.binding.isValidBinding() && messageSend.arguments != null) {
                  int j = 0;

                  for(int l = messageSend.arguments.length; j < l; ++j) {
                     if (messageSend.arguments[j].sourceStart == start) {
                        return messageSend.arguments[j];
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   @Override
   public StringBuffer print(int indent, StringBuffer output) {
      printIndent(indent, output).append("/**\n");
      if (this.paramReferences != null) {
         int i = 0;

         for(int length = this.paramReferences.length; i < length; ++i) {
            printIndent(indent + 1, output).append(" * @param ");
            this.paramReferences[i].print(indent, output).append('\n');
         }
      }

      if (this.paramTypeParameters != null) {
         int i = 0;

         for(int length = this.paramTypeParameters.length; i < length; ++i) {
            printIndent(indent + 1, output).append(" * @param <");
            this.paramTypeParameters[i].print(indent, output).append(">\n");
         }
      }

      if (this.returnStatement != null) {
         printIndent(indent + 1, output).append(" * @");
         this.returnStatement.print(indent, output).append('\n');
      }

      if (this.exceptionReferences != null) {
         int i = 0;

         for(int length = this.exceptionReferences.length; i < length; ++i) {
            printIndent(indent + 1, output).append(" * @throws ");
            this.exceptionReferences[i].print(indent, output).append('\n');
         }
      }

      if (this.seeReferences != null) {
         int i = 0;

         for(int length = this.seeReferences.length; i < length; ++i) {
            printIndent(indent + 1, output).append(" * @see ");
            this.seeReferences[i].print(indent, output).append('\n');
         }
      }

      printIndent(indent, output).append(" */\n");
      return output;
   }

   public void resolve(ClassScope scope) {
      if ((this.bits & 65536) != 0) {
         if (this.inheritedPositions != null) {
            int length = this.inheritedPositions.length;

            for(int i = 0; i < length; ++i) {
               int start = (int)(this.inheritedPositions[i] >>> 32);
               int end = (int)this.inheritedPositions[i];
               scope.problemReporter().javadocUnexpectedTag(start, end);
            }
         }

         int paramTagsSize = this.paramReferences == null ? 0 : this.paramReferences.length;

         for(int i = 0; i < paramTagsSize; ++i) {
            JavadocSingleNameReference param = this.paramReferences[i];
            scope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
         }

         this.resolveTypeParameterTags(scope, true);
         if (this.returnStatement != null) {
            scope.problemReporter().javadocUnexpectedTag(this.returnStatement.sourceStart, this.returnStatement.sourceEnd);
         }

         int throwsTagsLength = this.exceptionReferences == null ? 0 : this.exceptionReferences.length;

         for(int i = 0; i < throwsTagsLength; ++i) {
            TypeReference typeRef = this.exceptionReferences[i];
            int start;
            int end;
            if (typeRef instanceof JavadocSingleTypeReference) {
               JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference)typeRef;
               start = singleRef.tagSourceStart;
               end = singleRef.tagSourceEnd;
            } else if (typeRef instanceof JavadocQualifiedTypeReference) {
               JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference)typeRef;
               start = qualifiedRef.tagSourceStart;
               end = qualifiedRef.tagSourceEnd;
            } else {
               start = typeRef.sourceStart;
               end = typeRef.sourceEnd;
            }

            scope.problemReporter().javadocUnexpectedTag(start, end);
         }

         int seeTagsLength = this.seeReferences == null ? 0 : this.seeReferences.length;

         for(int i = 0; i < seeTagsLength; ++i) {
            this.resolveReference(this.seeReferences[i], scope);
         }

         boolean source15 = scope.compilerOptions().sourceLevel >= 3211264L;
         if (!source15 && this.valuePositions != -1L) {
            scope.problemReporter().javadocUnexpectedTag((int)(this.valuePositions >>> 32), (int)this.valuePositions);
         }
      }
   }

   public void resolve(CompilationUnitScope unitScope) {
      if ((this.bits & 65536) != 0) {
         ;
      }
   }

   public void resolve(MethodScope methScope) {
      if ((this.bits & 65536) != 0) {
         AbstractMethodDeclaration methDecl = methScope.referenceMethod();
         boolean overriding = methDecl != null && methDecl.binding != null
            ? !methDecl.binding.isStatic() && (methDecl.binding.modifiers & 805306368) != 0
            : false;
         int seeTagsLength = this.seeReferences == null ? 0 : this.seeReferences.length;
         boolean superRef = false;

         for(int i = 0; i < seeTagsLength; ++i) {
            this.resolveReference(this.seeReferences[i], methScope);
            if (methDecl != null && !superRef) {
               if (!methDecl.isConstructor()) {
                  if (overriding && this.seeReferences[i] instanceof JavadocMessageSend) {
                     JavadocMessageSend messageSend = (JavadocMessageSend)this.seeReferences[i];
                     if (messageSend.binding != null && messageSend.binding.isValidBinding() && messageSend.actualReceiverType instanceof ReferenceBinding) {
                        ReferenceBinding methodReceiverType = (ReferenceBinding)messageSend.actualReceiverType;
                        TypeBinding superType = methDecl.binding.declaringClass.findSuperTypeOriginatingFrom(methodReceiverType);
                        if (superType != null
                           && TypeBinding.notEquals(superType.original(), methDecl.binding.declaringClass)
                           && CharOperation.equals(messageSend.selector, methDecl.selector)
                           && methScope.environment().methodVerifier().doesMethodOverride(methDecl.binding, messageSend.binding.original())) {
                           superRef = true;
                        }
                     }
                  }
               } else if (this.seeReferences[i] instanceof JavadocAllocationExpression) {
                  JavadocAllocationExpression allocationExpr = (JavadocAllocationExpression)this.seeReferences[i];
                  if (allocationExpr.binding != null && allocationExpr.binding.isValidBinding()) {
                     ReferenceBinding allocType = (ReferenceBinding)allocationExpr.resolvedType.original();
                     ReferenceBinding superType = (ReferenceBinding)methDecl.binding.declaringClass.findSuperTypeOriginatingFrom(allocType);
                     if (superType != null && TypeBinding.notEquals(superType.original(), methDecl.binding.declaringClass)) {
                        MethodBinding superConstructor = methScope.getConstructor(superType, methDecl.binding.parameters, allocationExpr);
                        if (superConstructor.isValidBinding() && superConstructor.original() == allocationExpr.binding.original()) {
                           MethodBinding current = methDecl.binding;
                           if (methScope.compilerOptions().sourceLevel >= 3407872L && current.typeVariables != Binding.NO_TYPE_VARIABLES) {
                              current = current.asRawMethod(methScope.environment());
                           }

                           if (superConstructor.areParametersEqual(current)) {
                              superRef = true;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!superRef && methDecl != null && methDecl.annotations != null) {
            int length = methDecl.annotations.length;

            for(int i = 0; i < length && !superRef; ++i) {
               superRef = (methDecl.binding.tagBits & 562949953421312L) != 0L;
            }
         }

         boolean reportMissing = methDecl == null
            || (!overriding || this.inheritedPositions == null)
               && !superRef
               && (methDecl.binding.declaringClass == null || !methDecl.binding.declaringClass.isLocalType());
         if (!overriding && this.inheritedPositions != null) {
            int length = this.inheritedPositions.length;

            for(int i = 0; i < length; ++i) {
               int start = (int)(this.inheritedPositions[i] >>> 32);
               int end = (int)this.inheritedPositions[i];
               methScope.problemReporter().javadocUnexpectedTag(start, end);
            }
         }

         CompilerOptions compilerOptions = methScope.compilerOptions();
         this.resolveParamTags(methScope, reportMissing, compilerOptions.reportUnusedParameterIncludeDocCommentReference);
         this.resolveTypeParameterTags(methScope, reportMissing && compilerOptions.reportMissingJavadocTagsMethodTypeParameters);
         if (this.returnStatement == null) {
            if (reportMissing && methDecl != null && methDecl.isMethod()) {
               MethodDeclaration meth = (MethodDeclaration)methDecl;
               if (meth.binding.returnType != TypeBinding.VOID) {
                  methScope.problemReporter().javadocMissingReturnTag(meth.returnType.sourceStart, meth.returnType.sourceEnd, methDecl.binding.modifiers);
               }
            }
         } else {
            this.returnStatement.resolve(methScope);
         }

         this.resolveThrowsTags(methScope, reportMissing);
         boolean source15 = compilerOptions.sourceLevel >= 3211264L;
         if (!source15 && methDecl != null && this.valuePositions != -1L) {
            methScope.problemReporter().javadocUnexpectedTag((int)(this.valuePositions >>> 32), (int)this.valuePositions);
         }

         int length = this.invalidParameters == null ? 0 : this.invalidParameters.length;

         for(int i = 0; i < length; ++i) {
            this.invalidParameters[i].resolve(methScope, false, false);
         }
      }
   }

   private void resolveReference(Expression reference, Scope scope) {
      int problemCount = scope.referenceContext().compilationResult().problemCount;
      switch(scope.kind) {
         case 2:
            reference.resolveType((MethodScope)scope);
            break;
         case 3:
            reference.resolveType((ClassScope)scope);
      }

      boolean hasProblems = scope.referenceContext().compilationResult().problemCount > problemCount;
      boolean source15 = scope.compilerOptions().sourceLevel >= 3211264L;
      int scopeModifiers = -1;
      if (reference instanceof JavadocFieldReference) {
         JavadocFieldReference fieldRef = (JavadocFieldReference)reference;
         if (fieldRef.methodBinding != null) {
            if (fieldRef.tagValue == 10) {
               if (scopeModifiers == -1) {
                  scopeModifiers = scope.getDeclarationModifiers();
               }

               scope.problemReporter().javadocInvalidValueReference(fieldRef.sourceStart, fieldRef.sourceEnd, scopeModifiers);
            } else if (fieldRef.actualReceiverType != null) {
               if (scope.enclosingSourceType().isCompatibleWith(fieldRef.actualReceiverType)) {
                  fieldRef.bits |= 16384;
               }

               ReferenceBinding resolvedType = (ReferenceBinding)fieldRef.actualReceiverType;
               if (CharOperation.equals(resolvedType.sourceName(), fieldRef.token)) {
                  fieldRef.methodBinding = scope.getConstructor(resolvedType, Binding.NO_TYPES, fieldRef);
               } else {
                  fieldRef.methodBinding = scope.findMethod(resolvedType, fieldRef.token, Binding.NO_TYPES, fieldRef, false);
               }
            }
         } else if (source15 && fieldRef.binding != null && fieldRef.binding.isValidBinding() && fieldRef.tagValue == 10 && !fieldRef.binding.isStatic()) {
            if (scopeModifiers == -1) {
               scopeModifiers = scope.getDeclarationModifiers();
            }

            scope.problemReporter().javadocInvalidValueReference(fieldRef.sourceStart, fieldRef.sourceEnd, scopeModifiers);
         }

         if (!hasProblems && fieldRef.binding != null && fieldRef.binding.isValidBinding() && fieldRef.actualReceiverType instanceof ReferenceBinding) {
            ReferenceBinding resolvedType = (ReferenceBinding)fieldRef.actualReceiverType;
            this.verifyTypeReference(fieldRef, fieldRef.receiver, scope, source15, resolvedType, fieldRef.binding.modifiers);
         }
      } else {
         if (!hasProblems
            && (reference instanceof JavadocSingleTypeReference || reference instanceof JavadocQualifiedTypeReference)
            && reference.resolvedType instanceof ReferenceBinding) {
            ReferenceBinding resolvedType = (ReferenceBinding)reference.resolvedType;
            this.verifyTypeReference(reference, reference, scope, source15, resolvedType, resolvedType.modifiers);
         }

         if (reference instanceof JavadocMessageSend) {
            JavadocMessageSend msgSend = (JavadocMessageSend)reference;
            if (source15 && msgSend.tagValue == 10) {
               if (scopeModifiers == -1) {
                  scopeModifiers = scope.getDeclarationModifiers();
               }

               scope.problemReporter().javadocInvalidValueReference(msgSend.sourceStart, msgSend.sourceEnd, scopeModifiers);
            }

            if (!hasProblems && msgSend.binding != null && msgSend.binding.isValidBinding() && msgSend.actualReceiverType instanceof ReferenceBinding) {
               ReferenceBinding resolvedType = (ReferenceBinding)msgSend.actualReceiverType;
               this.verifyTypeReference(msgSend, msgSend.receiver, scope, source15, resolvedType, msgSend.binding.modifiers);
            }
         } else if (reference instanceof JavadocAllocationExpression) {
            JavadocAllocationExpression alloc = (JavadocAllocationExpression)reference;
            if (source15 && alloc.tagValue == 10) {
               if (scopeModifiers == -1) {
                  scopeModifiers = scope.getDeclarationModifiers();
               }

               scope.problemReporter().javadocInvalidValueReference(alloc.sourceStart, alloc.sourceEnd, scopeModifiers);
            }

            if (!hasProblems && alloc.binding != null && alloc.binding.isValidBinding() && alloc.resolvedType instanceof ReferenceBinding) {
               ReferenceBinding resolvedType = (ReferenceBinding)alloc.resolvedType;
               this.verifyTypeReference(alloc, alloc.type, scope, source15, resolvedType, alloc.binding.modifiers);
            }
         } else if (reference instanceof JavadocSingleTypeReference && reference.resolvedType != null && reference.resolvedType.isTypeVariable()) {
            scope.problemReporter().javadocInvalidReference(reference.sourceStart, reference.sourceEnd);
         }
      }
   }

   private void resolveParamTags(MethodScope scope, boolean reportMissing, boolean considerParamRefAsUsage) {
      AbstractMethodDeclaration methodDecl = scope.referenceMethod();
      int paramTagsSize = this.paramReferences == null ? 0 : this.paramReferences.length;
      if (methodDecl == null) {
         for(int i = 0; i < paramTagsSize; ++i) {
            JavadocSingleNameReference param = this.paramReferences[i];
            scope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
         }
      } else {
         int argumentsSize = methodDecl.arguments == null ? 0 : methodDecl.arguments.length;
         if (paramTagsSize == 0) {
            if (reportMissing) {
               for(int i = 0; i < argumentsSize; ++i) {
                  Argument arg = methodDecl.arguments[i];
                  scope.problemReporter().javadocMissingParamTag(arg.name, arg.sourceStart, arg.sourceEnd, methodDecl.binding.modifiers);
               }
            }
         } else {
            LocalVariableBinding[] bindings = new LocalVariableBinding[paramTagsSize];
            int maxBindings = 0;

            for(int i = 0; i < paramTagsSize; ++i) {
               JavadocSingleNameReference param = this.paramReferences[i];
               param.resolve(scope, true, considerParamRefAsUsage);
               if (param.binding != null && param.binding.isValidBinding()) {
                  boolean found = false;

                  for(int j = 0; j < maxBindings && !found; ++j) {
                     if (bindings[j] == param.binding) {
                        scope.problemReporter().javadocDuplicatedParamTag(param.token, param.sourceStart, param.sourceEnd, methodDecl.binding.modifiers);
                        found = true;
                     }
                  }

                  if (!found) {
                     bindings[maxBindings++] = (LocalVariableBinding)param.binding;
                  }
               }
            }

            if (reportMissing) {
               for(int i = 0; i < argumentsSize; ++i) {
                  Argument arg = methodDecl.arguments[i];
                  boolean found = false;

                  for(int j = 0; j < maxBindings && !found; ++j) {
                     LocalVariableBinding binding = bindings[j];
                     if (arg.binding == binding) {
                        found = true;
                     }
                  }

                  if (!found) {
                     scope.problemReporter().javadocMissingParamTag(arg.name, arg.sourceStart, arg.sourceEnd, methodDecl.binding.modifiers);
                  }
               }
            }
         }
      }
   }

   private void resolveTypeParameterTags(Scope scope, boolean reportMissing) {
      int paramTypeParamLength = this.paramTypeParameters == null ? 0 : this.paramTypeParameters.length;
      TypeParameter[] parameters = null;
      TypeVariableBinding[] typeVariables = null;
      int modifiers = -1;
      switch(scope.kind) {
         case 2:
            AbstractMethodDeclaration methodDeclaration = ((MethodScope)scope).referenceMethod();
            if (methodDeclaration == null) {
               for(int i = 0; i < paramTypeParamLength; ++i) {
                  JavadocSingleTypeReference param = this.paramTypeParameters[i];
                  scope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
               }

               return;
            }

            parameters = methodDeclaration.typeParameters();
            typeVariables = methodDeclaration.binding.typeVariables;
            modifiers = methodDeclaration.binding.modifiers;
            break;
         case 3:
            TypeDeclaration typeDeclaration = ((ClassScope)scope).referenceContext;
            parameters = typeDeclaration.typeParameters;
            typeVariables = typeDeclaration.binding.typeVariables;
            modifiers = typeDeclaration.binding.modifiers;
      }

      if (typeVariables != null && typeVariables.length != 0) {
         if (parameters != null) {
            reportMissing = reportMissing && scope.compilerOptions().sourceLevel >= 3211264L;
            int typeParametersLength = parameters.length;
            if (paramTypeParamLength == 0) {
               if (reportMissing) {
                  int i = 0;

                  for(int l = typeParametersLength; i < l; ++i) {
                     scope.problemReporter().javadocMissingParamTag(parameters[i].name, parameters[i].sourceStart, parameters[i].sourceEnd, modifiers);
                  }
               }
            } else if (typeVariables.length == typeParametersLength) {
               TypeVariableBinding[] bindings = new TypeVariableBinding[paramTypeParamLength];

               for(int i = 0; i < paramTypeParamLength; ++i) {
                  JavadocSingleTypeReference param = this.paramTypeParameters[i];
                  TypeBinding paramBindind = param.internalResolveType(scope, 0);
                  if (paramBindind != null && paramBindind.isValidBinding()) {
                     if (!paramBindind.isTypeVariable()) {
                        scope.problemReporter().javadocUndeclaredParamTagName(param.token, param.sourceStart, param.sourceEnd, modifiers);
                     } else {
                        if (scope.compilerOptions().reportUnusedParameterIncludeDocCommentReference) {
                           TypeVariableBinding typeVariableBinding = (TypeVariableBinding)paramBindind;
                           typeVariableBinding.modifiers |= 134217728;
                        }

                        boolean duplicate = false;

                        for(int j = 0; j < i && !duplicate; ++j) {
                           if (TypeBinding.equalsEquals(bindings[j], param.resolvedType)) {
                              scope.problemReporter().javadocDuplicatedParamTag(param.token, param.sourceStart, param.sourceEnd, modifiers);
                              duplicate = true;
                           }
                        }

                        if (!duplicate) {
                           bindings[i] = (TypeVariableBinding)param.resolvedType;
                        }
                     }
                  }
               }

               for(int i = 0; i < typeParametersLength; ++i) {
                  TypeParameter parameter = parameters[i];
                  boolean found = false;

                  for(int j = 0; j < paramTypeParamLength && !found; ++j) {
                     if (TypeBinding.equalsEquals(parameter.binding, bindings[j])) {
                        found = true;
                        bindings[j] = null;
                     }
                  }

                  if (!found && reportMissing) {
                     scope.problemReporter().javadocMissingParamTag(parameter.name, parameter.sourceStart, parameter.sourceEnd, modifiers);
                  }
               }

               for(int i = 0; i < paramTypeParamLength; ++i) {
                  if (bindings[i] != null) {
                     JavadocSingleTypeReference param = this.paramTypeParameters[i];
                     scope.problemReporter().javadocUndeclaredParamTagName(param.token, param.sourceStart, param.sourceEnd, modifiers);
                  }
               }
            }
         }
      } else {
         for(int i = 0; i < paramTypeParamLength; ++i) {
            JavadocSingleTypeReference param = this.paramTypeParameters[i];
            scope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
         }
      }
   }

   private void resolveThrowsTags(MethodScope methScope, boolean reportMissing) {
      AbstractMethodDeclaration md = methScope.referenceMethod();
      int throwsTagsLength = this.exceptionReferences == null ? 0 : this.exceptionReferences.length;
      if (md == null) {
         for(int i = 0; i < throwsTagsLength; ++i) {
            TypeReference typeRef = this.exceptionReferences[i];
            int start = typeRef.sourceStart;
            int end = typeRef.sourceEnd;
            if (typeRef instanceof JavadocQualifiedTypeReference) {
               start = ((JavadocQualifiedTypeReference)typeRef).tagSourceStart;
               end = ((JavadocQualifiedTypeReference)typeRef).tagSourceEnd;
            } else if (typeRef instanceof JavadocSingleTypeReference) {
               start = ((JavadocSingleTypeReference)typeRef).tagSourceStart;
               end = ((JavadocSingleTypeReference)typeRef).tagSourceEnd;
            }

            methScope.problemReporter().javadocUnexpectedTag(start, end);
         }
      } else {
         int boundExceptionLength = md.binding == null ? 0 : md.binding.thrownExceptions.length;
         int thrownExceptionLength = md.thrownExceptions == null ? 0 : md.thrownExceptions.length;
         if (throwsTagsLength == 0) {
            if (reportMissing) {
               for(int i = 0; i < boundExceptionLength; ++i) {
                  ReferenceBinding exceptionBinding = md.binding.thrownExceptions[i];
                  if (exceptionBinding != null && exceptionBinding.isValidBinding()) {
                     int j = i;

                     while(j < thrownExceptionLength && TypeBinding.notEquals(exceptionBinding, md.thrownExceptions[j].resolvedType)) {
                        ++j;
                     }

                     if (j < thrownExceptionLength) {
                        methScope.problemReporter().javadocMissingThrowsTag(md.thrownExceptions[j], md.binding.modifiers);
                     }
                  }
               }
            }
         } else {
            int maxRef = 0;
            TypeReference[] typeReferences = new TypeReference[throwsTagsLength];

            for(int i = 0; i < throwsTagsLength; ++i) {
               TypeReference typeRef = this.exceptionReferences[i];
               typeRef.resolve(methScope);
               TypeBinding typeBinding = typeRef.resolvedType;
               if (typeBinding != null && typeBinding.isValidBinding() && typeBinding.isClass()) {
                  typeReferences[maxRef++] = typeRef;
               }
            }

            for(int i = 0; i < boundExceptionLength; ++i) {
               ReferenceBinding exceptionBinding = md.binding.thrownExceptions[i];
               if (exceptionBinding != null) {
                  exceptionBinding = (ReferenceBinding)exceptionBinding.erasure();
               }

               boolean found = false;

               for(int j = 0; j < maxRef && !found; ++j) {
                  if (typeReferences[j] != null) {
                     TypeBinding typeBinding = typeReferences[j].resolvedType;
                     if (TypeBinding.equalsEquals(exceptionBinding, typeBinding)) {
                        found = true;
                        typeReferences[j] = null;
                     }
                  }
               }

               if (!found && reportMissing && exceptionBinding != null && exceptionBinding.isValidBinding()) {
                  int k = i;

                  while(k < thrownExceptionLength && TypeBinding.notEquals(exceptionBinding, md.thrownExceptions[k].resolvedType)) {
                     ++k;
                  }

                  if (k < thrownExceptionLength) {
                     methScope.problemReporter().javadocMissingThrowsTag(md.thrownExceptions[k], md.binding.modifiers);
                  }
               }
            }

            for(int i = 0; i < maxRef; ++i) {
               TypeReference typeRef = typeReferences[i];
               if (typeRef != null) {
                  boolean compatible = false;

                  for(int j = 0; j < thrownExceptionLength && !compatible; ++j) {
                     TypeBinding exceptionBinding = md.thrownExceptions[j].resolvedType;
                     if (exceptionBinding != null) {
                        compatible = typeRef.resolvedType.isCompatibleWith(exceptionBinding);
                     }
                  }

                  if (!compatible && !typeRef.resolvedType.isUncheckedException(false)) {
                     methScope.problemReporter().javadocInvalidThrowsClassName(typeRef, md.binding.modifiers);
                  }
               }
            }
         }
      }
   }

   private void verifyTypeReference(
      Expression reference, Expression typeReference, Scope scope, boolean source15, ReferenceBinding resolvedType, int modifiers
   ) {
      if (resolvedType.isValidBinding()) {
         int scopeModifiers = -1;
         if (!this.canBeSeen(scope.problemReporter().options.reportInvalidJavadocTagsVisibility, modifiers)) {
            scope.problemReporter().javadocHiddenReference(typeReference.sourceStart, reference.sourceEnd, scope, modifiers);
            return;
         }

         if (reference != typeReference && !this.canBeSeen(scope.problemReporter().options.reportInvalidJavadocTagsVisibility, resolvedType.modifiers)) {
            scope.problemReporter().javadocHiddenReference(typeReference.sourceStart, typeReference.sourceEnd, scope, resolvedType.modifiers);
            return;
         }

         if (resolvedType.isMemberType()) {
            ReferenceBinding topLevelType = resolvedType;
            int packageLength = resolvedType.fPackage.compoundName.length;
            int depth = resolvedType.depth();
            int idx = depth + packageLength;
            char[][] computedCompoundName = new char[idx + 1][];

            for(computedCompoundName[idx] = resolvedType.sourceName; topLevelType.enclosingType() != null; computedCompoundName[idx] = topLevelType.sourceName) {
               topLevelType = topLevelType.enclosingType();
               --idx;
            }

            for(int i = packageLength; --i >= 0; computedCompoundName[idx] = topLevelType.fPackage.compoundName[i]) {
               --idx;
            }

            ClassScope topLevelScope = scope.classScope();
            if (topLevelScope.parent.kind != 4 || !CharOperation.equals(topLevelType.sourceName, topLevelScope.referenceContext.name)) {
               topLevelScope = topLevelScope.outerMostClassScope();
               if (typeReference instanceof JavadocSingleTypeReference
                  && (!source15 && depth == 1 || TypeBinding.notEquals(topLevelType, topLevelScope.referenceContext.binding))) {
                  boolean hasValidImport = false;
                  if (!source15) {
                     if (scopeModifiers == -1) {
                        scopeModifiers = scope.getDeclarationModifiers();
                     }

                     scope.problemReporter().javadocInvalidMemberTypeQualification(typeReference.sourceStart, typeReference.sourceEnd, scopeModifiers);
                     return;
                  }

                  CompilationUnitScope unitScope = topLevelScope.compilationUnitScope();
                  ImportBinding[] imports = unitScope.imports;
                  int length = imports == null ? 0 : imports.length;

                  label123:
                  for(int i = 0; i < length; ++i) {
                     char[][] compoundName = imports[i].compoundName;
                     int compoundNameLength = compoundName.length;
                     if (imports[i].onDemand && compoundNameLength == computedCompoundName.length - 1 || compoundNameLength == computedCompoundName.length) {
                        int j = compoundNameLength;

                        do {
                           --j;
                           if (j < 0 || !CharOperation.equals(imports[i].compoundName[j], computedCompoundName[j])) {
                              continue label123;
                           }
                        } while(j != 0);

                        hasValidImport = true;
                        ImportReference importReference = imports[i].reference;
                        if (importReference != null) {
                           importReference.bits |= 2;
                        }
                        break;
                     }
                  }

                  if (!hasValidImport) {
                     if (scopeModifiers == -1) {
                        scopeModifiers = scope.getDeclarationModifiers();
                     }

                     scope.problemReporter().javadocInvalidMemberTypeQualification(typeReference.sourceStart, typeReference.sourceEnd, scopeModifiers);
                  }
               }
            }

            if (typeReference instanceof JavadocQualifiedTypeReference && !scope.isDefinedInSameUnit(resolvedType)) {
               char[][] typeRefName = ((JavadocQualifiedTypeReference)typeReference).getTypeName();
               int skipLength = 0;
               if (topLevelScope.getCurrentPackage() == resolvedType.getPackage() && typeRefName.length < computedCompoundName.length) {
                  skipLength = resolvedType.fPackage.compoundName.length;
               }

               boolean valid = true;
               if (typeRefName.length == computedCompoundName.length - skipLength) {
                  for(int i = 0; i < typeRefName.length; ++i) {
                     if (!CharOperation.equals(typeRefName[i], computedCompoundName[i + skipLength])) {
                        valid = false;
                        break;
                     }
                  }
               } else {
                  valid = false;
               }

               if (!valid) {
                  if (scopeModifiers == -1) {
                     scopeModifiers = scope.getDeclarationModifiers();
                  }

                  scope.problemReporter().javadocInvalidMemberTypeQualification(typeReference.sourceStart, typeReference.sourceEnd, scopeModifiers);
                  return;
               }
            }
         }

         if (scope.referenceCompilationUnit().isPackageInfo()
            && typeReference instanceof JavadocSingleTypeReference
            && resolvedType.fPackage.compoundName.length > 0) {
            scope.problemReporter().javadocInvalidReference(typeReference.sourceStart, typeReference.sourceEnd);
            return;
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.paramReferences != null) {
            int i = 0;

            for(int length = this.paramReferences.length; i < length; ++i) {
               this.paramReferences[i].traverse(visitor, scope);
            }
         }

         if (this.paramTypeParameters != null) {
            int i = 0;

            for(int length = this.paramTypeParameters.length; i < length; ++i) {
               this.paramTypeParameters[i].traverse(visitor, scope);
            }
         }

         if (this.returnStatement != null) {
            this.returnStatement.traverse(visitor, scope);
         }

         if (this.exceptionReferences != null) {
            int i = 0;

            for(int length = this.exceptionReferences.length; i < length; ++i) {
               this.exceptionReferences[i].traverse(visitor, scope);
            }
         }

         if (this.seeReferences != null) {
            int i = 0;

            for(int length = this.seeReferences.length; i < length; ++i) {
               this.seeReferences[i].traverse(visitor, scope);
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.paramReferences != null) {
            int i = 0;

            for(int length = this.paramReferences.length; i < length; ++i) {
               this.paramReferences[i].traverse(visitor, scope);
            }
         }

         if (this.paramTypeParameters != null) {
            int i = 0;

            for(int length = this.paramTypeParameters.length; i < length; ++i) {
               this.paramTypeParameters[i].traverse(visitor, scope);
            }
         }

         if (this.returnStatement != null) {
            this.returnStatement.traverse(visitor, scope);
         }

         if (this.exceptionReferences != null) {
            int i = 0;

            for(int length = this.exceptionReferences.length; i < length; ++i) {
               this.exceptionReferences[i].traverse(visitor, scope);
            }
         }

         if (this.seeReferences != null) {
            int i = 0;

            for(int length = this.seeReferences.length; i < length; ++i) {
               this.seeReferences[i].traverse(visitor, scope);
            }
         }
      }

      visitor.endVisit(this, scope);
   }
}
