package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class MethodScope extends BlockScope {
   public ReferenceContext referenceContext;
   public boolean isStatic;
   public boolean isConstructorCall = false;
   public FieldBinding initializedField;
   public int lastVisibleFieldID = -1;
   public int analysisIndex;
   public boolean isPropagatingInnerClassEmulation;
   public int lastIndex = 0;
   public long[] definiteInits = new long[4];
   public long[][] extraDefiniteInits = new long[4][];
   public SyntheticArgumentBinding[] extraSyntheticArguments;
   public boolean hasMissingSwitchDefault;

   public MethodScope(Scope parent, ReferenceContext context, boolean isStatic) {
      super(2, parent);
      this.locals = new LocalVariableBinding[5];
      this.referenceContext = context;
      this.isStatic = isStatic;
      this.startIndex = 0;
   }

   public MethodScope(Scope parent, ReferenceContext context, boolean isStatic, int lastVisibleFieldID) {
      this(parent, context, isStatic);
      this.lastVisibleFieldID = lastVisibleFieldID;
   }

   @Override
   String basicToString(int tab) {
      String newLine = "\n";
      int i = tab;

      while(--i >= 0) {
         newLine = newLine + "\t";
      }

      String s = newLine + "--- Method Scope ---";
      newLine = newLine + "\t";
      String var7 = s + newLine + "locals:";

      for(int ix = 0; ix < this.localIndex; ++ix) {
         var7 = var7 + newLine + "\t" + this.locals[ix].toString();
      }

      String var8 = var7 + newLine + "startIndex = " + this.startIndex;
      String var9 = var8 + newLine + "isConstructorCall = " + this.isConstructorCall;
      String var10 = var9 + newLine + "initializedField = " + this.initializedField;
      String var11 = var10 + newLine + "lastVisibleFieldID = " + this.lastVisibleFieldID;
      return var11 + newLine + "referenceContext = " + this.referenceContext;
   }

   private void checkAndSetModifiersForConstructor(MethodBinding methodBinding) {
      int modifiers = methodBinding.modifiers;
      ReferenceBinding declaringClass = methodBinding.declaringClass;
      if ((modifiers & 4194304) != 0) {
         this.problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
      }

      int flags;
      if ((((ConstructorDeclaration)this.referenceContext).bits & 128) != 0 && (flags = declaringClass.modifiers & 16389) != 0) {
         if ((flags & 16384) != 0) {
            modifiers &= -8;
            modifiers |= 2;
         } else {
            modifiers &= -8;
            modifiers |= flags;
         }
      }

      flags = modifiers & 65535;
      if (declaringClass.isEnum() && (((ConstructorDeclaration)this.referenceContext).bits & 128) == 0) {
         if ((flags & -2051) != 0) {
            this.problemReporter().illegalModifierForEnumConstructor((AbstractMethodDeclaration)this.referenceContext);
            modifiers &= -63486;
         } else if ((((AbstractMethodDeclaration)this.referenceContext).modifiers & 2048) != 0) {
            this.problemReporter().illegalModifierForMethod((AbstractMethodDeclaration)this.referenceContext);
         }

         modifiers |= 2;
      } else if ((flags & -2056) != 0) {
         this.problemReporter().illegalModifierForMethod((AbstractMethodDeclaration)this.referenceContext);
         modifiers &= -63481;
      } else if ((((AbstractMethodDeclaration)this.referenceContext).modifiers & 2048) != 0) {
         this.problemReporter().illegalModifierForMethod((AbstractMethodDeclaration)this.referenceContext);
      }

      int accessorBits = flags & 7;
      if ((accessorBits & accessorBits - 1) != 0) {
         this.problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
         if ((accessorBits & 1) != 0) {
            if ((accessorBits & 4) != 0) {
               modifiers &= -5;
            }

            if ((accessorBits & 2) != 0) {
               modifiers &= -3;
            }
         } else if ((accessorBits & 4) != 0 && (accessorBits & 2) != 0) {
            modifiers &= -3;
         }
      }

      methodBinding.modifiers = modifiers;
   }

   private void checkAndSetModifiersForMethod(MethodBinding methodBinding) {
      int modifiers = methodBinding.modifiers;
      ReferenceBinding declaringClass = methodBinding.declaringClass;
      if ((modifiers & 4194304) != 0) {
         this.problemReporter().duplicateModifierForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
      }

      int realModifiers = modifiers & 65535;
      if (declaringClass.isInterface()) {
         int expectedModifiers = 1025;
         boolean isDefaultMethod = (modifiers & 65536) != 0;
         boolean reportIllegalModifierCombination = false;
         boolean isJDK18orGreater = false;
         if (this.compilerOptions().sourceLevel >= 3407872L && !declaringClass.isAnnotationType()) {
            expectedModifiers |= 67592;
            isJDK18orGreater = true;
            if (!methodBinding.isAbstract()) {
               reportIllegalModifierCombination = isDefaultMethod && methodBinding.isStatic();
            } else {
               reportIllegalModifierCombination = isDefaultMethod || methodBinding.isStatic();
               if (methodBinding.isStrictfp()) {
                  this.problemReporter().illegalAbstractModifierCombinationForMethod((AbstractMethodDeclaration)this.referenceContext);
               }
            }

            if (reportIllegalModifierCombination) {
               this.problemReporter().illegalModifierCombinationForInterfaceMethod((AbstractMethodDeclaration)this.referenceContext);
            }

            if (isDefaultMethod) {
               realModifiers |= 65536;
            }
         }

         if ((realModifiers & ~expectedModifiers) != 0) {
            if ((declaringClass.modifiers & 8192) != 0) {
               this.problemReporter().illegalModifierForAnnotationMember((AbstractMethodDeclaration)this.referenceContext);
            } else {
               this.problemReporter().illegalModifierForInterfaceMethod((AbstractMethodDeclaration)this.referenceContext, isJDK18orGreater);
            }
         }
      } else {
         if ((realModifiers & -3392) != 0) {
            this.problemReporter().illegalModifierForMethod((AbstractMethodDeclaration)this.referenceContext);
            modifiers &= -62145;
         }

         int accessorBits = realModifiers & 7;
         if ((accessorBits & accessorBits - 1) != 0) {
            this.problemReporter().illegalVisibilityModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
            if ((accessorBits & 1) != 0) {
               if ((accessorBits & 4) != 0) {
                  modifiers &= -5;
               }

               if ((accessorBits & 2) != 0) {
                  modifiers &= -3;
               }
            } else if ((accessorBits & 4) != 0 && (accessorBits & 2) != 0) {
               modifiers &= -3;
            }
         }

         if ((modifiers & 1024) != 0) {
            int incompatibleWithAbstract = 2362;
            if ((modifiers & incompatibleWithAbstract) != 0) {
               this.problemReporter().illegalAbstractModifierCombinationForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
            }

            if (!methodBinding.declaringClass.isAbstract()) {
               this.problemReporter().abstractMethodInAbstractClass((SourceTypeBinding)declaringClass, (AbstractMethodDeclaration)this.referenceContext);
            }
         }

         if ((modifiers & 256) != 0 && (modifiers & 2048) != 0) {
            this.problemReporter().nativeMethodsCannotBeStrictfp(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
         }

         if ((realModifiers & 8) != 0 && declaringClass.isNestedType() && !declaringClass.isStatic()) {
            this.problemReporter().unexpectedStaticModifierForMethod(declaringClass, (AbstractMethodDeclaration)this.referenceContext);
         }

         methodBinding.modifiers = modifiers;
      }
   }

   public void checkUnusedParameters(MethodBinding method) {
      if (!method.isAbstract()
         && (!method.isImplementing() || this.compilerOptions().reportUnusedParameterWhenImplementingAbstract)
         && (!method.isOverriding() || method.isImplementing() || this.compilerOptions().reportUnusedParameterWhenOverridingConcrete)
         && !method.isMain()) {
         int i = 0;

         for(int maxLocals = this.localIndex; i < maxLocals; ++i) {
            LocalVariableBinding local = this.locals[i];
            if (local == null || (local.tagBits & 1024L) == 0L) {
               break;
            }

            if (local.useFlag == 0 && (local.declaration.bits & 1073741824) != 0) {
               this.problemReporter().unusedArgument(local.declaration);
            }
         }
      }
   }

   public void computeLocalVariablePositions(int initOffset, CodeStream codeStream) {
      this.offset = initOffset;
      this.maxOffset = initOffset;
      int ilocal = 0;

      for(int maxLocals = this.localIndex; ilocal < maxLocals; ++ilocal) {
         LocalVariableBinding local = this.locals[ilocal];
         if (local == null || (local.tagBits & 1024L) == 0L) {
            break;
         }

         codeStream.record(local);
         local.resolvedPosition = this.offset;
         if (!TypeBinding.equalsEquals(local.type, TypeBinding.LONG) && !TypeBinding.equalsEquals(local.type, TypeBinding.DOUBLE)) {
            ++this.offset;
         } else {
            this.offset += 2;
         }

         if (this.offset > 255) {
            this.problemReporter().noMoreAvailableSpaceForArgument(local, local.declaration);
         }
      }

      if (this.extraSyntheticArguments != null) {
         int iarg = 0;

         for(int maxArguments = this.extraSyntheticArguments.length; iarg < maxArguments; ++iarg) {
            SyntheticArgumentBinding argument = this.extraSyntheticArguments[iarg];
            argument.resolvedPosition = this.offset;
            if (!TypeBinding.equalsEquals(argument.type, TypeBinding.LONG) && !TypeBinding.equalsEquals(argument.type, TypeBinding.DOUBLE)) {
               ++this.offset;
            } else {
               this.offset += 2;
            }

            if (this.offset > 255) {
               this.problemReporter().noMoreAvailableSpaceForArgument(argument, (ASTNode)this.referenceContext);
            }
         }
      }

      this.computeLocalVariablePositions(ilocal, this.offset, codeStream);
   }

   MethodBinding createMethod(AbstractMethodDeclaration method) {
      this.referenceContext = method;
      method.scope = this;
      SourceTypeBinding declaringClass = this.referenceType().binding;
      int modifiers = method.modifiers | 33554432;
      if (method.isConstructor()) {
         if (method.isDefaultConstructor()) {
            modifiers |= 67108864;
         }

         method.binding = new MethodBinding(modifiers, null, null, declaringClass);
         this.checkAndSetModifiersForConstructor(method.binding);
      } else {
         if (declaringClass.isInterface()) {
            if (!method.isDefaultMethod() && !method.isStatic()) {
               modifiers |= 1025;
            } else {
               modifiers |= 1;
            }
         }

         method.binding = new MethodBinding(modifiers, method.selector, null, null, null, declaringClass);
         this.checkAndSetModifiersForMethod(method.binding);
      }

      this.isStatic = method.binding.isStatic();
      Argument[] argTypes = method.arguments;
      int argLength = argTypes == null ? 0 : argTypes.length;
      long sourceLevel = this.compilerOptions().sourceLevel;
      if (argLength > 0) {
         Argument argument = argTypes[--argLength];
         if (argument.isVarArgs() && sourceLevel >= 3211264L) {
            method.binding.modifiers |= 128;
         }

         if (CharOperation.equals(argument.name, ConstantPool.This)) {
            this.problemReporter().illegalThisDeclaration(argument);
         }

         while(--argLength >= 0) {
            argument = argTypes[argLength];
            if (argument.isVarArgs() && sourceLevel >= 3211264L) {
               this.problemReporter().illegalVararg(argument, method);
            }

            if (CharOperation.equals(argument.name, ConstantPool.This)) {
               this.problemReporter().illegalThisDeclaration(argument);
            }
         }
      }

      if (method.receiver != null) {
         if (sourceLevel <= 3342336L) {
            this.problemReporter().illegalSourceLevelForThis(method.receiver);
         }

         if (method.receiver.annotations != null) {
            method.bits |= 1048576;
         }
      }

      TypeParameter[] typeParameters = method.typeParameters();
      if (typeParameters != null && typeParameters.length != 0) {
         method.binding.typeVariables = this.createTypeVariables(typeParameters, method.binding);
         method.binding.modifiers |= 1073741824;
      } else {
         method.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
      }

      return method.binding;
   }

   @Override
   public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve) {
      FieldBinding field = super.findField(receiverType, fieldName, invocationSite, needResolve);
      if (field == null) {
         return null;
      } else if (!field.isValidBinding()) {
         return field;
      } else if (receiverType.isInterface() && invocationSite.isQualifiedSuper()) {
         return new ProblemFieldBinding(field, field.declaringClass, fieldName, 28);
      } else if (field.isStatic()) {
         return field;
      } else if (!this.isConstructorCall || TypeBinding.notEquals(receiverType, this.enclosingSourceType())) {
         return field;
      } else if (invocationSite instanceof SingleNameReference) {
         return new ProblemFieldBinding(field, field.declaringClass, fieldName, 6);
      } else {
         if (invocationSite instanceof QualifiedNameReference) {
            QualifiedNameReference name = (QualifiedNameReference)invocationSite;
            if (name.binding == null) {
               return new ProblemFieldBinding(field, field.declaringClass, fieldName, 6);
            }
         }

         return field;
      }
   }

   public boolean isInsideConstructor() {
      return this.referenceContext instanceof ConstructorDeclaration;
   }

   public boolean isInsideInitializer() {
      return this.referenceContext instanceof TypeDeclaration;
   }

   @Override
   public boolean isLambdaScope() {
      return this.referenceContext instanceof LambdaExpression;
   }

   public boolean isInsideInitializerOrConstructor() {
      return this.referenceContext instanceof TypeDeclaration || this.referenceContext instanceof ConstructorDeclaration;
   }

   @Override
   public ProblemReporter problemReporter() {
      ProblemReporter problemReporter = this.referenceCompilationUnit().problemReporter;
      problemReporter.referenceContext = this.referenceContext;
      return problemReporter;
   }

   public final int recordInitializationStates(FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) != 0) {
         return -1;
      } else {
         UnconditionalFlowInfo unconditionalFlowInfo = flowInfo.unconditionalInitsWithoutSideEffect();
         long[] extraInits = unconditionalFlowInfo.extra == null ? null : unconditionalFlowInfo.extra[0];
         long inits = unconditionalFlowInfo.definiteInits;
         int i = this.lastIndex;

         label62:
         while(--i >= 0) {
            if (this.definiteInits[i] == inits) {
               long[] otherInits = this.extraDefiniteInits[i];
               if (extraInits != null && otherInits != null) {
                  if (extraInits.length == otherInits.length) {
                     int j = 0;

                     for(int max = extraInits.length; j < max; ++j) {
                        if (extraInits[j] != otherInits[j]) {
                           continue label62;
                        }
                     }

                     return i;
                  }
               } else if (extraInits == null && otherInits == null) {
                  return i;
               }
            }
         }

         if (this.definiteInits.length == this.lastIndex) {
            System.arraycopy(this.definiteInits, 0, this.definiteInits = new long[this.lastIndex + 20], 0, this.lastIndex);
            System.arraycopy(this.extraDefiniteInits, 0, this.extraDefiniteInits = new long[this.lastIndex + 20][], 0, this.lastIndex);
         }

         this.definiteInits[this.lastIndex] = inits;
         if (extraInits != null) {
            this.extraDefiniteInits[this.lastIndex] = new long[extraInits.length];
            System.arraycopy(extraInits, 0, this.extraDefiniteInits[this.lastIndex], 0, extraInits.length);
         }

         return this.lastIndex++;
      }
   }

   public AbstractMethodDeclaration referenceMethod() {
      return this.referenceContext instanceof AbstractMethodDeclaration ? (AbstractMethodDeclaration)this.referenceContext : null;
   }

   public MethodBinding referenceMethodBinding() {
      if (this.referenceContext instanceof LambdaExpression) {
         return ((LambdaExpression)this.referenceContext).binding;
      } else {
         return this.referenceContext instanceof AbstractMethodDeclaration ? ((AbstractMethodDeclaration)this.referenceContext).binding : null;
      }
   }

   @Override
   public TypeDeclaration referenceType() {
      ClassScope scope = this.enclosingClassScope();
      return scope == null ? null : scope.referenceContext;
   }

   @Override
   void resolveTypeParameter(TypeParameter typeParameter) {
      typeParameter.resolve(this);
   }

   @Override
   public boolean hasDefaultNullnessFor(int location) {
      if (this.referenceContext instanceof AbstractMethodDeclaration) {
         MethodBinding binding = ((AbstractMethodDeclaration)this.referenceContext).binding;
         if (binding != null && binding.defaultNullness != 0) {
            if ((binding.defaultNullness & location) != 0) {
               return true;
            }

            return false;
         }
      }

      return this.parent.hasDefaultNullnessFor(location);
   }
}
