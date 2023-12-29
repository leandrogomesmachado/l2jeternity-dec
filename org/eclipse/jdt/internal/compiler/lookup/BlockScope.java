package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.FakedTrackingVariable;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class BlockScope extends Scope {
   public LocalVariableBinding[] locals;
   public int localIndex;
   public int startIndex;
   public int offset;
   public int maxOffset;
   public BlockScope[] shiftScopes;
   public Scope[] subscopes = new Scope[1];
   public int subscopeCount = 0;
   public CaseStatement enclosingCase;
   public static final VariableBinding[] EmulationPathToImplicitThis = new VariableBinding[0];
   public static final VariableBinding[] NoEnclosingInstanceInConstructorCall = new VariableBinding[0];
   public static final VariableBinding[] NoEnclosingInstanceInStaticContext = new VariableBinding[0];
   public boolean insideTypeAnnotation = false;
   private List trackingVariables;
   public FlowInfo finallyInfo;

   public BlockScope(BlockScope parent) {
      this(parent, true);
   }

   public BlockScope(BlockScope parent, boolean addToParentScope) {
      this(1, parent);
      this.locals = new LocalVariableBinding[5];
      if (addToParentScope) {
         parent.addSubscope(this);
      }

      this.startIndex = parent.localIndex;
   }

   public BlockScope(BlockScope parent, int variableCount) {
      this(1, parent);
      this.locals = new LocalVariableBinding[variableCount];
      parent.addSubscope(this);
      this.startIndex = parent.localIndex;
   }

   protected BlockScope(int kind, Scope parent) {
      super(kind, parent);
   }

   public final void addAnonymousType(TypeDeclaration anonymousType, ReferenceBinding superBinding) {
      ClassScope anonymousClassScope = new ClassScope(this, anonymousType);
      anonymousClassScope.buildAnonymousTypeBinding(this.enclosingSourceType(), superBinding);

      for(MethodScope methodScope = this.methodScope();
         methodScope != null && methodScope.referenceContext instanceof LambdaExpression;
         methodScope = methodScope.enclosingMethodScope()
      ) {
         LambdaExpression lambda = (LambdaExpression)methodScope.referenceContext;
         if (!lambda.scope.isStatic && !lambda.scope.isConstructorCall) {
            lambda.shouldCaptureInstance = true;
         }
      }
   }

   public final void addLocalType(TypeDeclaration localType) {
      ClassScope localTypeScope = new ClassScope(this, localType);
      this.addSubscope(localTypeScope);
      localTypeScope.buildLocalTypeBinding(this.enclosingSourceType());

      for(MethodScope methodScope = this.methodScope();
         methodScope != null && methodScope.referenceContext instanceof LambdaExpression;
         methodScope = methodScope.enclosingMethodScope()
      ) {
         LambdaExpression lambda = (LambdaExpression)methodScope.referenceContext;
         if (!lambda.scope.isStatic && !lambda.scope.isConstructorCall) {
            lambda.shouldCaptureInstance = true;
         }
      }
   }

   public final void addLocalVariable(LocalVariableBinding binding) {
      this.checkAndSetModifiersForVariable(binding);
      if (this.localIndex == this.locals.length) {
         System.arraycopy(this.locals, 0, this.locals = new LocalVariableBinding[this.localIndex * 2], 0, this.localIndex);
      }

      this.locals[this.localIndex++] = binding;
      binding.declaringScope = this;
      MethodScope var10001 = this.outerMostMethodScope();
      int var2 = var10001.analysisIndex;
      var10001.analysisIndex += 1;
      binding.id = var2;
   }

   public void addSubscope(Scope childScope) {
      if (this.subscopeCount == this.subscopes.length) {
         System.arraycopy(this.subscopes, 0, this.subscopes = new Scope[this.subscopeCount * 2], 0, this.subscopeCount);
      }

      this.subscopes[this.subscopeCount++] = childScope;
   }

   public final boolean allowBlankFinalFieldAssignment(FieldBinding binding) {
      if (TypeBinding.notEquals(this.enclosingReceiverType(), binding.declaringClass)) {
         return false;
      } else {
         MethodScope methodScope = this.methodScope();
         if (methodScope.isStatic != binding.isStatic()) {
            return false;
         } else if (methodScope.isLambdaScope()) {
            return false;
         } else {
            return methodScope.isInsideInitializer() || ((AbstractMethodDeclaration)methodScope.referenceContext).isInitializationMethod();
         }
      }
   }

   String basicToString(int tab) {
      String newLine = "\n";
      int i = tab;

      while(--i >= 0) {
         newLine = newLine + "\t";
      }

      String s = newLine + "--- Block Scope ---";
      newLine = newLine + "\t";
      String var7 = s + newLine + "locals:";

      for(int ix = 0; ix < this.localIndex; ++ix) {
         var7 = var7 + newLine + "\t" + this.locals[ix].toString();
      }

      return var7 + newLine + "startIndex = " + this.startIndex;
   }

   private void checkAndSetModifiersForVariable(LocalVariableBinding varBinding) {
      int modifiers = varBinding.modifiers;
      if ((modifiers & 4194304) != 0 && varBinding.declaration != null) {
         this.problemReporter().duplicateModifierForVariable(varBinding.declaration, this instanceof MethodScope);
      }

      int realModifiers = modifiers & 65535;
      int unexpectedModifiers = -17;
      if ((realModifiers & unexpectedModifiers) != 0 && varBinding.declaration != null) {
         this.problemReporter().illegalModifierForVariable(varBinding.declaration, this instanceof MethodScope);
      }

      varBinding.modifiers = modifiers;
   }

   void computeLocalVariablePositions(int ilocal, int initOffset, CodeStream codeStream) {
      this.offset = initOffset;
      this.maxOffset = initOffset;
      int maxLocals = this.localIndex;
      boolean hasMoreVariables = ilocal < maxLocals;
      int iscope = 0;
      int maxScopes = this.subscopeCount;
      boolean hasMoreScopes = maxScopes > 0;

      while(hasMoreVariables || hasMoreScopes) {
         if (!hasMoreScopes || hasMoreVariables && this.subscopes[iscope].startIndex() > ilocal) {
            LocalVariableBinding local = this.locals[ilocal];
            boolean generateCurrentLocalVar = local.useFlag > 0 && local.constant() == Constant.NotAConstant;
            if (local.useFlag == 0 && local.declaration != null && (local.declaration.bits & 1073741824) != 0) {
               if (local.isCatchParameter()) {
                  this.problemReporter().unusedExceptionParameter(local.declaration);
               } else {
                  this.problemReporter().unusedLocalVariable(local.declaration);
               }
            }

            if (!generateCurrentLocalVar && local.declaration != null && this.compilerOptions().preserveAllLocalVariables) {
               generateCurrentLocalVar = true;
               if (local.useFlag == 0) {
                  local.useFlag = 1;
               }
            }

            if (generateCurrentLocalVar) {
               if (local.declaration != null) {
                  codeStream.record(local);
               }

               local.resolvedPosition = this.offset;
               if (!TypeBinding.equalsEquals(local.type, TypeBinding.LONG) && !TypeBinding.equalsEquals(local.type, TypeBinding.DOUBLE)) {
                  ++this.offset;
               } else {
                  this.offset += 2;
               }

               if (this.offset > 65535) {
                  this.problemReporter()
                     .noMoreAvailableSpaceForLocal(
                        local, (ASTNode)(local.declaration == null ? (ASTNode)this.methodScope().referenceContext : local.declaration)
                     );
               }
            } else {
               local.resolvedPosition = -1;
            }

            ++ilocal;
            hasMoreVariables = ilocal < maxLocals;
         } else {
            if (this.subscopes[iscope] instanceof BlockScope) {
               BlockScope subscope = (BlockScope)this.subscopes[iscope];
               int subOffset = subscope.shiftScopes == null ? this.offset : subscope.maxShiftedOffset();
               subscope.computeLocalVariablePositions(0, subOffset, codeStream);
               if (subscope.maxOffset > this.maxOffset) {
                  this.maxOffset = subscope.maxOffset;
               }
            }

            ++iscope;
            hasMoreScopes = iscope < maxScopes;
         }
      }

      if (this.offset > this.maxOffset) {
         this.maxOffset = this.offset;
      }
   }

   public void emulateOuterAccess(LocalVariableBinding outerLocalVariable) {
      BlockScope outerVariableScope = outerLocalVariable.declaringScope;
      if (outerVariableScope != null) {
         int depth = 0;

         for(Scope scope = this; outerVariableScope != scope; scope = scope.parent) {
            switch(scope.kind) {
               case 2:
                  if (scope.isLambdaScope()) {
                     LambdaExpression lambdaExpression = (LambdaExpression)scope.referenceContext();
                     lambdaExpression.addSyntheticArgument(outerLocalVariable);
                  }
                  break;
               case 3:
                  ++depth;
            }
         }

         if (depth != 0) {
            MethodScope currentMethodScope = this.methodScope();
            if (outerVariableScope.methodScope() != currentMethodScope) {
               NestedTypeBinding currentType = (NestedTypeBinding)this.enclosingSourceType();
               if (!currentType.isLocalType()) {
                  return;
               }

               if (!currentMethodScope.isInsideInitializerOrConstructor()) {
                  currentType.addSyntheticArgumentAndField(outerLocalVariable);
               } else {
                  currentType.addSyntheticArgument(outerLocalVariable);
               }
            }
         }
      }
   }

   public final ReferenceBinding findLocalType(char[] name) {
      long compliance = this.compilerOptions().complianceLevel;

      for(int i = this.subscopeCount - 1; i >= 0; --i) {
         if (this.subscopes[i] instanceof ClassScope) {
            LocalTypeBinding sourceType = (LocalTypeBinding)((ClassScope)this.subscopes[i]).referenceContext.binding;
            if ((compliance < 3145728L || sourceType.enclosingCase == null || this.isInsideCase(sourceType.enclosingCase))
               && CharOperation.equals(sourceType.sourceName(), name)) {
               return sourceType;
            }
         }
      }

      return null;
   }

   public LocalDeclaration[] findLocalVariableDeclarations(int position) {
      int ilocal = 0;
      int maxLocals = this.localIndex;
      boolean hasMoreVariables = maxLocals > 0;
      LocalDeclaration[] localDeclarations = null;
      int declPtr = 0;
      int iscope = 0;
      int maxScopes = this.subscopeCount;
      boolean hasMoreScopes = maxScopes > 0;

      while(hasMoreVariables || hasMoreScopes) {
         if (hasMoreScopes && (!hasMoreVariables || this.subscopes[iscope].startIndex() <= ilocal)) {
            Scope subscope = this.subscopes[iscope];
            if (subscope.kind == 1) {
               localDeclarations = ((BlockScope)subscope).findLocalVariableDeclarations(position);
               if (localDeclarations != null) {
                  return localDeclarations;
               }
            }

            ++iscope;
            hasMoreScopes = iscope < maxScopes;
         } else {
            LocalVariableBinding local = this.locals[ilocal];
            if (local != null) {
               LocalDeclaration localDecl = local.declaration;
               if (localDecl != null) {
                  if (localDecl.declarationSourceStart > position) {
                     return localDeclarations;
                  }

                  if (position <= localDecl.declarationSourceEnd) {
                     if (localDeclarations == null) {
                        localDeclarations = new LocalDeclaration[maxLocals];
                     }

                     localDeclarations[declPtr++] = localDecl;
                  }
               }
            }

            ++ilocal;
            hasMoreVariables = ilocal < maxLocals;
            if (!hasMoreVariables && localDeclarations != null) {
               return localDeclarations;
            }
         }
      }

      return null;
   }

   @Override
   public LocalVariableBinding findVariable(char[] variableName) {
      int varLength = variableName.length;

      for(int i = this.localIndex - 1; i >= 0; --i) {
         LocalVariableBinding local;
         char[] localName;
         if ((localName = (local = this.locals[i]).name).length == varLength && CharOperation.equals(localName, variableName)) {
            return local;
         }
      }

      return null;
   }

   public Binding getBinding(char[][] compoundName, int mask, InvocationSite invocationSite, boolean needResolve) {
      Binding binding = this.getBinding(compoundName[0], mask | 4 | 16, invocationSite, needResolve);
      invocationSite.setFieldIndex(1);
      if (binding instanceof VariableBinding) {
         return binding;
      } else {
         CompilationUnitScope unitScope = this.compilationUnitScope();
         unitScope.recordQualifiedReference(compoundName);
         if (!binding.isValidBinding()) {
            return binding;
         } else {
            int length = compoundName.length;
            int currentIndex = 1;
            if (binding instanceof PackageBinding) {
               PackageBinding packageBinding = (PackageBinding)binding;

               while(true) {
                  if (currentIndex >= length) {
                     return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
                  }

                  unitScope.recordReference(packageBinding.compoundName, compoundName[currentIndex]);
                  binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
                  invocationSite.setFieldIndex(currentIndex);
                  if (binding == null) {
                     if (currentIndex == length) {
                        return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
                     }

                     return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), 1);
                  }

                  if (binding instanceof ReferenceBinding) {
                     if (!binding.isValidBinding()) {
                        return new ProblemReferenceBinding(
                           CharOperation.subarray(compoundName, 0, currentIndex),
                           (ReferenceBinding)((ReferenceBinding)binding).closestMatch(),
                           binding.problemId()
                        );
                     }

                     if (!((ReferenceBinding)binding).canBeSeenBy(this)) {
                        return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)binding, 2);
                     }
                     break;
                  }

                  packageBinding = (PackageBinding)binding;
               }
            }

            ReferenceBinding referenceBinding = (ReferenceBinding)binding;
            binding = this.environment().convertToRawType(referenceBinding, false);
            if (invocationSite instanceof ASTNode) {
               ASTNode invocationNode = (ASTNode)invocationSite;
               if (invocationNode.isTypeUseDeprecated(referenceBinding, this)) {
                  this.problemReporter().deprecatedType(referenceBinding, invocationNode);
               }
            }

            Binding problemFieldBinding = null;

            while(currentIndex < length) {
               referenceBinding = (ReferenceBinding)binding;
               char[] nextName = compoundName[currentIndex++];
               invocationSite.setFieldIndex(currentIndex);
               invocationSite.setActualReceiverType(referenceBinding);
               if ((mask & 1) != 0 && (binding = this.findField(referenceBinding, nextName, invocationSite, true)) != null) {
                  if (binding.isValidBinding()) {
                     break;
                  }

                  problemFieldBinding = new ProblemFieldBinding(
                     ((ProblemFieldBinding)binding).closestMatch,
                     ((ProblemFieldBinding)binding).declaringClass,
                     CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'),
                     binding.problemId()
                  );
                  if (binding.problemId() != 2) {
                     return problemFieldBinding;
                  }
               }

               if ((binding = this.findMemberType(nextName, referenceBinding)) == null) {
                  if (problemFieldBinding != null) {
                     return problemFieldBinding;
                  }

                  if ((mask & 1) != 0) {
                     return new ProblemFieldBinding(null, referenceBinding, nextName, 1);
                  }

                  if ((mask & 3) != 0) {
                     return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), referenceBinding, 1);
                  }

                  return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), referenceBinding, 1);
               }

               if (!binding.isValidBinding()) {
                  if (problemFieldBinding != null) {
                     return problemFieldBinding;
                  }

                  return new ProblemReferenceBinding(
                     CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)((ReferenceBinding)binding).closestMatch(), binding.problemId()
                  );
               }

               if (invocationSite instanceof ASTNode) {
                  referenceBinding = (ReferenceBinding)binding;
                  ASTNode invocationNode = (ASTNode)invocationSite;
                  if (invocationNode.isTypeUseDeprecated(referenceBinding, this)) {
                     this.problemReporter().deprecatedType(referenceBinding, invocationNode);
                  }
               }
            }

            if ((mask & 1) != 0 && binding instanceof FieldBinding) {
               FieldBinding field = (FieldBinding)binding;
               return (Binding)(!field.isStatic()
                  ? new ProblemFieldBinding(
                     field, field.declaringClass, CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'), 7
                  )
                  : binding);
            } else {
               return (Binding)((mask & 4) != 0 && binding instanceof ReferenceBinding
                  ? binding
                  : new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), 1));
            }
         }
      }
   }

   public final Binding getBinding(char[][] compoundName, InvocationSite invocationSite) {
      int currentIndex = 0;
      int length = compoundName.length;
      Binding binding = this.getBinding(compoundName[currentIndex++], 23, invocationSite, true);
      if (!binding.isValidBinding()) {
         return binding;
      } else {
         if (binding instanceof PackageBinding) {
            do {
               if (currentIndex >= length) {
                  return binding;
               }

               PackageBinding packageBinding = (PackageBinding)binding;
               binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
               if (binding == null) {
                  if (currentIndex == length) {
                     return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
                  }

                  return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), 1);
               }
            } while(!(binding instanceof ReferenceBinding));

            if (!binding.isValidBinding()) {
               return new ProblemReferenceBinding(
                  CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)((ReferenceBinding)binding).closestMatch(), binding.problemId()
               );
            }

            if (!((ReferenceBinding)binding).canBeSeenBy(this)) {
               return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)binding, 2);
            }
         }

         if (binding instanceof ReferenceBinding) {
            while(true) {
               if (currentIndex >= length) {
                  return binding;
               }

               ReferenceBinding typeBinding = (ReferenceBinding)binding;
               char[] nextName = compoundName[currentIndex++];
               TypeBinding receiverType = typeBinding.capture(this, invocationSite.sourceStart(), invocationSite.sourceEnd());
               if ((binding = this.findField(receiverType, nextName, invocationSite, true)) != null) {
                  if (!binding.isValidBinding()) {
                     return new ProblemFieldBinding(
                        (FieldBinding)binding,
                        ((FieldBinding)binding).declaringClass,
                        CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'),
                        binding.problemId()
                     );
                  }

                  if (!((FieldBinding)binding).isStatic()) {
                     return new ProblemFieldBinding(
                        (FieldBinding)binding,
                        ((FieldBinding)binding).declaringClass,
                        CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'),
                        7
                     );
                  }
                  break;
               }

               if ((binding = this.findMemberType(nextName, typeBinding)) == null) {
                  return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), typeBinding, 1);
               }

               if (!binding.isValidBinding()) {
                  return new ProblemReferenceBinding(
                     CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)((ReferenceBinding)binding).closestMatch(), binding.problemId()
                  );
               }
            }
         }

         VariableBinding variableBinding = (VariableBinding)binding;

         while(currentIndex < length) {
            TypeBinding typeBinding = variableBinding.type;
            if (typeBinding == null) {
               return new ProblemFieldBinding(null, null, CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'), 1);
            }

            TypeBinding receiverType = typeBinding.capture(this, invocationSite.sourceStart(), invocationSite.sourceEnd());
            variableBinding = this.findField(receiverType, compoundName[currentIndex++], invocationSite, true);
            if (variableBinding == null) {
               return new ProblemFieldBinding(
                  null,
                  receiverType instanceof ReferenceBinding ? (ReferenceBinding)receiverType : null,
                  CharOperation.concatWith(CharOperation.subarray(compoundName, 0, currentIndex), '.'),
                  1
               );
            }

            if (!variableBinding.isValidBinding()) {
               return variableBinding;
            }
         }

         return variableBinding;
      }
   }

   public VariableBinding[] getEmulationPath(LocalVariableBinding outerLocalVariable) {
      MethodScope currentMethodScope = this.methodScope();
      SourceTypeBinding sourceType = currentMethodScope.enclosingSourceType();
      BlockScope variableScope = outerLocalVariable.declaringScope;
      if (variableScope != null && currentMethodScope != variableScope.methodScope()) {
         LambdaExpression lambda;
         if (currentMethodScope.isLambdaScope()) {
            lambda = (LambdaExpression)currentMethodScope.referenceContext;
            SyntheticArgumentBinding syntheticArgument;
            if ((syntheticArgument = lambda.getSyntheticArgument(outerLocalVariable)) != null) {
               return new VariableBinding[]{syntheticArgument};
            }
         }

         if (currentMethodScope.isInsideInitializerOrConstructor()
            && sourceType.isNestedType()
            && (lambda = ((NestedTypeBinding)sourceType).getSyntheticArgument(outerLocalVariable)) != null) {
            return new VariableBinding[]{lambda};
         } else {
            return !currentMethodScope.isStatic && (lambda = sourceType.getSyntheticField(outerLocalVariable)) != null ? new VariableBinding[]{lambda} : null;
         }
      } else {
         return new VariableBinding[]{outerLocalVariable};
      }
   }

   public Object[] getEmulationPath(ReferenceBinding targetEnclosingType, boolean onlyExactMatch, boolean denyEnclosingArgInConstructorCall) {
      MethodScope currentMethodScope = this.methodScope();
      SourceTypeBinding sourceType = currentMethodScope.enclosingSourceType();
      if (currentMethodScope.isStatic
         || currentMethodScope.isConstructorCall
         || !TypeBinding.equalsEquals(sourceType, targetEnclosingType)
            && (onlyExactMatch || sourceType.findSuperTypeOriginatingFrom(targetEnclosingType) == null)) {
         if (sourceType.isNestedType() && !sourceType.isStatic()) {
            boolean insideConstructor = currentMethodScope.isInsideInitializerOrConstructor();
            SyntheticArgumentBinding syntheticArg;
            if (insideConstructor
               && (
                     syntheticArg = ((NestedTypeBinding)sourceType)
                        .getSyntheticArgument(targetEnclosingType, onlyExactMatch, currentMethodScope.isConstructorCall)
                  )
                  != null) {
               boolean isAnonymousAndHasEnclosing = sourceType.isAnonymousType() && sourceType.scope.referenceContext.allocation.enclosingInstance != null;
               return (Object[])(!denyEnclosingArgInConstructorCall
                     || !currentMethodScope.isConstructorCall
                     || isAnonymousAndHasEnclosing
                     || !TypeBinding.equalsEquals(sourceType, targetEnclosingType)
                        && (onlyExactMatch || sourceType.findSuperTypeOriginatingFrom(targetEnclosingType) == null)
                  ? new Object[]{syntheticArg}
                  : NoEnclosingInstanceInConstructorCall);
            } else if (currentMethodScope.isStatic) {
               return NoEnclosingInstanceInStaticContext;
            } else {
               if (sourceType.isAnonymousType()) {
                  ReferenceBinding enclosingType = sourceType.enclosingType();
                  if (enclosingType.isNestedType()) {
                     NestedTypeBinding nestedEnclosingType = (NestedTypeBinding)enclosingType;
                     SyntheticArgumentBinding enclosingArgument = nestedEnclosingType.getSyntheticArgument(
                        nestedEnclosingType.enclosingType(), onlyExactMatch, currentMethodScope.isConstructorCall
                     );
                     if (enclosingArgument != null) {
                        FieldBinding syntheticField = sourceType.getSyntheticField(enclosingArgument);
                        if (syntheticField != null
                           && (
                              TypeBinding.equalsEquals(syntheticField.type, targetEnclosingType)
                                 || !onlyExactMatch && ((ReferenceBinding)syntheticField.type).findSuperTypeOriginatingFrom(targetEnclosingType) != null
                           )) {
                           return new Object[]{syntheticField};
                        }
                     }
                  }
               }

               FieldBinding syntheticField = sourceType.getSyntheticField(targetEnclosingType, onlyExactMatch);
               if (syntheticField == null) {
                  Object[] path = new Object[2];
                  ReferenceBinding currentType = sourceType.enclosingType();
                  if (insideConstructor) {
                     path[0] = ((NestedTypeBinding)sourceType).getSyntheticArgument(currentType, onlyExactMatch, currentMethodScope.isConstructorCall);
                  } else {
                     if (currentMethodScope.isConstructorCall) {
                        return NoEnclosingInstanceInConstructorCall;
                     }

                     path[0] = sourceType.getSyntheticField(currentType, onlyExactMatch);
                  }

                  if (path[0] != null) {
                     int count = 1;

                     while(true) {
                        ReferenceBinding currentEnclosingType;
                        if ((currentEnclosingType = currentType.enclosingType()) != null
                           && !TypeBinding.equalsEquals(currentType, targetEnclosingType)
                           && (onlyExactMatch || currentType.findSuperTypeOriginatingFrom(targetEnclosingType) == null)) {
                           if (currentMethodScope != null) {
                              currentMethodScope = currentMethodScope.enclosingMethodScope();
                              if (currentMethodScope != null && currentMethodScope.isConstructorCall) {
                                 return NoEnclosingInstanceInConstructorCall;
                              }

                              if (currentMethodScope != null && currentMethodScope.isStatic) {
                                 return NoEnclosingInstanceInStaticContext;
                              }
                           }

                           FieldBinding var14 = ((NestedTypeBinding)currentType).getSyntheticField(currentEnclosingType, onlyExactMatch);
                           if (var14 != null) {
                              if (count == path.length) {
                                 System.arraycopy(path, 0, path = new Object[count + 1], 0, count);
                              }

                              path[count++] = ((SourceTypeBinding)var14.declaringClass).addSyntheticMethod(var14, true, false);
                              currentType = currentEnclosingType;
                              continue;
                           }
                        }

                        if (TypeBinding.equalsEquals(currentType, targetEnclosingType)
                           || !onlyExactMatch && currentType.findSuperTypeOriginatingFrom(targetEnclosingType) != null) {
                           return path;
                        }
                        break;
                     }
                  }

                  return null;
               } else {
                  return (Object[])(currentMethodScope.isConstructorCall ? NoEnclosingInstanceInConstructorCall : new Object[]{syntheticField});
               }
            }
         } else if (currentMethodScope.isConstructorCall) {
            return NoEnclosingInstanceInConstructorCall;
         } else {
            return currentMethodScope.isStatic ? NoEnclosingInstanceInStaticContext : null;
         }
      } else {
         return EmulationPathToImplicitThis;
      }
   }

   public final boolean isDuplicateLocalVariable(char[] name) {
      BlockScope current = this;

      while(true) {
         for(int i = 0; i < this.localIndex; ++i) {
            if (CharOperation.equals(name, current.locals[i].name)) {
               return true;
            }
         }

         if (current.kind != 1) {
            return false;
         }

         current = (BlockScope)current.parent;
      }
   }

   public int maxShiftedOffset() {
      int max = -1;
      if (this.shiftScopes != null) {
         int i = 0;

         for(int length = this.shiftScopes.length; i < length; ++i) {
            if (this.shiftScopes[i] != null) {
               int subMaxOffset = this.shiftScopes[i].maxOffset;
               if (subMaxOffset > max) {
                  max = subMaxOffset;
               }
            }
         }
      }

      return max;
   }

   public final boolean needBlankFinalFieldInitializationCheck(FieldBinding binding) {
      boolean isStatic = binding.isStatic();
      ReferenceBinding fieldDeclaringClass = binding.declaringClass;

      for(MethodScope methodScope = this.namedMethodScope(); methodScope != null; methodScope = methodScope.enclosingMethodScope().namedMethodScope()) {
         if (methodScope.isStatic != isStatic) {
            return false;
         }

         if (!methodScope.isInsideInitializer() && !((AbstractMethodDeclaration)methodScope.referenceContext).isInitializationMethod()) {
            return false;
         }

         ReferenceBinding enclosingType = methodScope.enclosingReceiverType();
         if (TypeBinding.equalsEquals(enclosingType, fieldDeclaringClass)) {
            return true;
         }

         if (!enclosingType.erasure().isAnonymousType()) {
            return false;
         }
      }

      return false;
   }

   @Override
   public ProblemReporter problemReporter() {
      return this.methodScope().problemReporter();
   }

   public void propagateInnerEmulation(ReferenceBinding targetType, boolean isEnclosingInstanceSupplied) {
      SyntheticArgumentBinding[] syntheticArguments;
      if ((syntheticArguments = targetType.syntheticOuterLocalVariables()) != null) {
         int i = 0;

         for(int max = syntheticArguments.length; i < max; ++i) {
            SyntheticArgumentBinding syntheticArg = syntheticArguments[i];
            if (!isEnclosingInstanceSupplied || !TypeBinding.equalsEquals(syntheticArg.type, targetType.enclosingType())) {
               this.emulateOuterAccess(syntheticArg.actualOuterLocalVariable);
            }
         }
      }
   }

   public TypeDeclaration referenceType() {
      return this.methodScope().referenceType();
   }

   public int scopeIndex() {
      if (this instanceof MethodScope) {
         return -1;
      } else {
         BlockScope parentScope = (BlockScope)this.parent;
         Scope[] parentSubscopes = parentScope.subscopes;
         int i = 0;

         for(int max = parentScope.subscopeCount; i < max; ++i) {
            if (parentSubscopes[i] == this) {
               return i;
            }
         }

         return -1;
      }
   }

   @Override
   int startIndex() {
      return this.startIndex;
   }

   @Override
   public String toString() {
      return this.toString(0);
   }

   public String toString(int tab) {
      String s = this.basicToString(tab);

      for(int i = 0; i < this.subscopeCount; ++i) {
         if (this.subscopes[i] instanceof BlockScope) {
            s = s + ((BlockScope)this.subscopes[i]).toString(tab + 1) + "\n";
         }
      }

      return s;
   }

   public int registerTrackingVariable(FakedTrackingVariable fakedTrackingVariable) {
      if (this.trackingVariables == null) {
         this.trackingVariables = new ArrayList(3);
      }

      this.trackingVariables.add(fakedTrackingVariable);
      MethodScope outerMethodScope = this.outerMostMethodScope();
      return outerMethodScope.analysisIndex++;
   }

   public void removeTrackingVar(FakedTrackingVariable trackingVariable) {
      if (trackingVariable.innerTracker != null) {
         trackingVariable.innerTracker.withdraw();
         trackingVariable.innerTracker = null;
      }

      if (this.trackingVariables == null || !this.trackingVariables.remove(trackingVariable)) {
         if (this.parent instanceof BlockScope) {
            ((BlockScope)this.parent).removeTrackingVar(trackingVariable);
         }
      }
   }

   public void pruneWrapperTrackingVar(FakedTrackingVariable trackingVariable) {
      this.trackingVariables.remove(trackingVariable);
   }

   public void checkUnclosedCloseables(FlowInfo flowInfo, FlowContext flowContext, ASTNode location, BlockScope locationScope) {
      if (this.compilerOptions().analyseResourceLeaks) {
         if (this.trackingVariables == null) {
            if (location != null && this.parent instanceof BlockScope) {
               ((BlockScope)this.parent).checkUnclosedCloseables(flowInfo, flowContext, location, locationScope);
            }
         } else if (location == null || flowInfo.reachMode() == 0) {
            FakedTrackingVariable returnVar = location instanceof ReturnStatement
               ? FakedTrackingVariable.getCloseTrackingVariable(((ReturnStatement)location).expression, flowInfo, flowContext)
               : null;
            Iterator<FakedTrackingVariable> iterator = new FakedTrackingVariable.IteratorForReporting(this.trackingVariables, this, location != null);

            while(iterator.hasNext()) {
               FakedTrackingVariable trackingVar = iterator.next();
               if ((returnVar == null || !trackingVar.isResourceBeingReturned(returnVar))
                  && (location == null || !trackingVar.hasDefinitelyNoResource(flowInfo))
                  && (location == null || flowContext == null || !flowContext.recordExitAgainstResource(this, flowInfo, trackingVar, location))) {
                  int status = trackingVar.findMostSpecificStatus(flowInfo, this, locationScope);
                  if (status == 2) {
                     this.reportResourceLeak(trackingVar, location, status);
                  } else if (location != null || !trackingVar.reportRecordedErrors(this, status, flowInfo.reachMode() != 0)) {
                     if (status == 16) {
                        this.reportResourceLeak(trackingVar, location, status);
                     } else if (status == 4 && this.environment().globalOptions.complianceLevel >= 3342336L) {
                        trackingVar.reportExplicitClosing(this.problemReporter());
                     }
                  }
               }
            }

            if (location == null) {
               for(int i = 0; i < this.localIndex; ++i) {
                  this.locals[i].closeTracker = null;
               }

               this.trackingVariables = null;
            }
         }
      }
   }

   private void reportResourceLeak(FakedTrackingVariable trackingVar, ASTNode location, int nullStatus) {
      if (location != null) {
         trackingVar.recordErrorLocation(location, nullStatus);
      } else {
         trackingVar.reportError(this.problemReporter(), null, nullStatus);
      }
   }

   public void correlateTrackingVarsIfElse(FlowInfo thenFlowInfo, FlowInfo elseFlowInfo) {
      if (this.trackingVariables != null) {
         int trackVarCount = this.trackingVariables.size();

         for(int i = 0; i < trackVarCount; ++i) {
            FakedTrackingVariable trackingVar = (FakedTrackingVariable)this.trackingVariables.get(i);
            if (trackingVar.originalBinding != null) {
               if (thenFlowInfo.isDefinitelyNonNull(trackingVar.binding) && elseFlowInfo.isDefinitelyNull(trackingVar.originalBinding)) {
                  elseFlowInfo.markAsDefinitelyNonNull(trackingVar.binding);
               } else if (elseFlowInfo.isDefinitelyNonNull(trackingVar.binding) && thenFlowInfo.isDefinitelyNull(trackingVar.originalBinding)) {
                  thenFlowInfo.markAsDefinitelyNonNull(trackingVar.binding);
               } else if (thenFlowInfo != FlowInfo.DEAD_END && elseFlowInfo != FlowInfo.DEAD_END) {
                  for(int j = i + 1; j < trackVarCount; ++j) {
                     FakedTrackingVariable var2 = (FakedTrackingVariable)this.trackingVariables.get(j);
                     if (trackingVar.originalBinding == var2.originalBinding) {
                        boolean var1SeenInThen = thenFlowInfo.hasNullInfoFor(trackingVar.binding);
                        boolean var1SeenInElse = elseFlowInfo.hasNullInfoFor(trackingVar.binding);
                        boolean var2SeenInThen = thenFlowInfo.hasNullInfoFor(var2.binding);
                        boolean var2SeenInElse = elseFlowInfo.hasNullInfoFor(var2.binding);
                        int newStatus;
                        if (!var1SeenInThen && var1SeenInElse && var2SeenInThen && !var2SeenInElse) {
                           newStatus = FlowInfo.mergeNullStatus(thenFlowInfo.nullStatus(var2.binding), elseFlowInfo.nullStatus(trackingVar.binding));
                        } else {
                           if (!var1SeenInThen || var1SeenInElse || var2SeenInThen || !var2SeenInElse) {
                              continue;
                           }

                           newStatus = FlowInfo.mergeNullStatus(thenFlowInfo.nullStatus(trackingVar.binding), elseFlowInfo.nullStatus(var2.binding));
                        }

                        thenFlowInfo.markNullStatus(trackingVar.binding, newStatus);
                        elseFlowInfo.markNullStatus(trackingVar.binding, newStatus);
                        trackingVar.originalBinding.closeTracker = trackingVar;
                        thenFlowInfo.markNullStatus(var2.binding, 4);
                        elseFlowInfo.markNullStatus(var2.binding, 4);
                     }
                  }
               }
            }
         }
      }

      if (this.parent instanceof BlockScope) {
         ((BlockScope)this.parent).correlateTrackingVarsIfElse(thenFlowInfo, elseFlowInfo);
      }
   }

   public void checkAppropriateMethodAgainstSupers(char[] selector, MethodBinding compileTimeMethod, TypeBinding[] parameters, InvocationSite site) {
      ReferenceBinding enclosingType = this.enclosingReceiverType();
      MethodBinding otherMethod = this.getMethod(enclosingType.superclass(), selector, parameters, site);
      if (this.checkAppropriate(compileTimeMethod, otherMethod, site)) {
         ReferenceBinding[] superInterfaces = enclosingType.superInterfaces();
         if (superInterfaces != null) {
            for(int i = 0; i < superInterfaces.length; ++i) {
               otherMethod = this.getMethod(superInterfaces[i], selector, parameters, site);
               if (!this.checkAppropriate(compileTimeMethod, otherMethod, site)) {
                  break;
               }
            }
         }
      }
   }

   private boolean checkAppropriate(MethodBinding compileTimeDeclaration, MethodBinding otherMethod, InvocationSite location) {
      if (otherMethod == null || !otherMethod.isValidBinding() || otherMethod == compileTimeDeclaration) {
         return true;
      } else if (MethodVerifier.doesMethodOverride(otherMethod, compileTimeDeclaration, this.environment())) {
         this.problemReporter().illegalSuperCallBypassingOverride(location, compileTimeDeclaration, otherMethod.declaringClass);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean hasDefaultNullnessFor(int location) {
      return this.parent.hasDefaultNullnessFor(location);
   }
}
