package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class TypeDeclaration extends Statement implements ProblemSeverities, ReferenceContext {
   public static final int CLASS_DECL = 1;
   public static final int INTERFACE_DECL = 2;
   public static final int ENUM_DECL = 3;
   public static final int ANNOTATION_TYPE_DECL = 4;
   public int modifiers = 0;
   public int modifiersSourceStart;
   public int functionalExpressionsCount = 0;
   public Annotation[] annotations;
   public char[] name;
   public TypeReference superclass;
   public TypeReference[] superInterfaces;
   public FieldDeclaration[] fields;
   public AbstractMethodDeclaration[] methods;
   public TypeDeclaration[] memberTypes;
   public SourceTypeBinding binding;
   public ClassScope scope;
   public MethodScope initializerScope;
   public MethodScope staticInitializerScope;
   public boolean ignoreFurtherInvestigation = false;
   public int maxFieldCount;
   public int declarationSourceStart;
   public int declarationSourceEnd;
   public int bodyStart;
   public int bodyEnd;
   public CompilationResult compilationResult;
   public MethodDeclaration[] missingAbstractMethods;
   public Javadoc javadoc;
   public QualifiedAllocationExpression allocation;
   public TypeDeclaration enclosingType;
   public FieldBinding enumValuesSyntheticfield;
   public int enumConstantsCounter;
   public TypeParameter[] typeParameters;

   public TypeDeclaration(CompilationResult compilationResult) {
      this.compilationResult = compilationResult;
   }

   @Override
   public void abort(int abortLevel, CategorizedProblem problem) {
      switch(abortLevel) {
         case 2:
            throw new AbortCompilation(this.compilationResult, problem);
         case 4:
            throw new AbortCompilationUnit(this.compilationResult, problem);
         case 16:
            throw new AbortMethod(this.compilationResult, problem);
         default:
            throw new AbortType(this.compilationResult, problem);
      }
   }

   public final void addClinit() {
      if (this.needClassInitMethod()) {
         AbstractMethodDeclaration[] methodDeclarations = this.methods;
         if (this.methods == null) {
            int length = 0;
            methodDeclarations = new AbstractMethodDeclaration[1];
         } else {
            int length = methodDeclarations.length;
            System.arraycopy(methodDeclarations, 0, methodDeclarations = new AbstractMethodDeclaration[length + 1], 1, length);
         }

         Clinit clinit = new Clinit(this.compilationResult);
         methodDeclarations[0] = clinit;
         clinit.declarationSourceStart = clinit.sourceStart = this.sourceStart;
         clinit.declarationSourceEnd = clinit.sourceEnd = this.sourceEnd;
         clinit.bodyEnd = this.sourceEnd;
         this.methods = methodDeclarations;
      }
   }

   public MethodDeclaration addMissingAbstractMethodFor(MethodBinding methodBinding) {
      TypeBinding[] argumentTypes = methodBinding.parameters;
      int argumentsLength = argumentTypes.length;
      MethodDeclaration methodDeclaration = new MethodDeclaration(this.compilationResult);
      methodDeclaration.selector = methodBinding.selector;
      methodDeclaration.sourceStart = this.sourceStart;
      methodDeclaration.sourceEnd = this.sourceEnd;
      methodDeclaration.modifiers = methodBinding.getAccessFlags() & -1025;
      if (argumentsLength > 0) {
         String baseName = "arg";
         Argument[] arguments = methodDeclaration.arguments = new Argument[argumentsLength];
         int i = argumentsLength;

         while(--i >= 0) {
            arguments[i] = new Argument((baseName + i).toCharArray(), 0L, null, 0);
         }
      }

      if (this.missingAbstractMethods == null) {
         this.missingAbstractMethods = new MethodDeclaration[]{methodDeclaration};
      } else {
         MethodDeclaration[] newMethods;
         System.arraycopy(
            this.missingAbstractMethods, 0, newMethods = new MethodDeclaration[this.missingAbstractMethods.length + 1], 1, this.missingAbstractMethods.length
         );
         newMethods[0] = methodDeclaration;
         this.missingAbstractMethods = newMethods;
      }

      methodDeclaration.binding = new MethodBinding(
         methodDeclaration.modifiers | 4096,
         methodBinding.selector,
         methodBinding.returnType,
         argumentsLength == 0 ? Binding.NO_PARAMETERS : argumentTypes,
         methodBinding.thrownExceptions,
         this.binding
      );
      methodDeclaration.scope = new MethodScope(this.scope, methodDeclaration, true);
      methodDeclaration.bindArguments();
      return methodDeclaration;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.ignoreFurtherInvestigation) {
         return flowInfo;
      } else {
         try {
            if ((flowInfo.tagBits & 1) == 0) {
               this.bits |= Integer.MIN_VALUE;
               LocalTypeBinding localType = (LocalTypeBinding)this.binding;
               localType.setConstantPoolName(currentScope.compilationUnitScope().computeConstantPoolName(localType));
            }

            this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
            this.updateMaxFieldCount();
            this.internalAnalyseCode(flowContext, flowInfo);
         } catch (AbortType var5) {
            this.ignoreFurtherInvestigation = true;
         }

         return flowInfo;
      }
   }

   public void analyseCode(ClassScope enclosingClassScope) {
      if (!this.ignoreFurtherInvestigation) {
         try {
            this.updateMaxFieldCount();
            this.internalAnalyseCode(null, FlowInfo.initial(this.maxFieldCount));
         } catch (AbortType var2) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   public void analyseCode(ClassScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (!this.ignoreFurtherInvestigation) {
         try {
            if ((flowInfo.tagBits & 1) == 0) {
               this.bits |= Integer.MIN_VALUE;
               LocalTypeBinding localType = (LocalTypeBinding)this.binding;
               localType.setConstantPoolName(currentScope.compilationUnitScope().computeConstantPoolName(localType));
            }

            this.manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
            this.updateMaxFieldCount();
            this.internalAnalyseCode(flowContext, flowInfo);
         } catch (AbortType var5) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   public void analyseCode(CompilationUnitScope unitScope) {
      if (!this.ignoreFurtherInvestigation) {
         try {
            this.internalAnalyseCode(null, FlowInfo.initial(this.maxFieldCount));
         } catch (AbortType var2) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   public boolean checkConstructors(Parser parser) {
      boolean hasConstructor = false;
      if (this.methods != null) {
         int i = this.methods.length;

         while(--i >= 0) {
            AbstractMethodDeclaration am;
            if ((am = this.methods[i]).isConstructor()) {
               if (!CharOperation.equals(am.selector, this.name)) {
                  ConstructorDeclaration c = (ConstructorDeclaration)am;
                  if (c.constructorCall == null || c.constructorCall.isImplicitSuper()) {
                     MethodDeclaration m = parser.convertToMethodDeclaration(c, this.compilationResult);
                     this.methods[i] = m;
                  }
               } else {
                  switch(kind(this.modifiers)) {
                     case 2:
                        parser.problemReporter().interfaceCannotHaveConstructors((ConstructorDeclaration)am);
                     case 3:
                     default:
                        break;
                     case 4:
                        parser.problemReporter().annotationTypeDeclarationCannotHaveConstructor((ConstructorDeclaration)am);
                  }

                  hasConstructor = true;
               }
            }
         }
      }

      return hasConstructor;
   }

   @Override
   public CompilationResult compilationResult() {
      return this.compilationResult;
   }

   public ConstructorDeclaration createDefaultConstructor(boolean needExplicitConstructorCall, boolean needToInsert) {
      ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
      constructor.bits |= 128;
      constructor.selector = this.name;
      constructor.modifiers = this.modifiers & 7;
      constructor.declarationSourceStart = constructor.sourceStart = this.sourceStart;
      constructor.declarationSourceEnd = constructor.sourceEnd = constructor.bodyEnd = this.sourceEnd;
      if (needExplicitConstructorCall) {
         constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
         constructor.constructorCall.sourceStart = this.sourceStart;
         constructor.constructorCall.sourceEnd = this.sourceEnd;
      }

      if (needToInsert) {
         if (this.methods == null) {
            this.methods = new AbstractMethodDeclaration[]{constructor};
         } else {
            AbstractMethodDeclaration[] newMethods;
            System.arraycopy(this.methods, 0, newMethods = new AbstractMethodDeclaration[this.methods.length + 1], 1, this.methods.length);
            newMethods[0] = constructor;
            this.methods = newMethods;
         }
      }

      return constructor;
   }

   public MethodBinding createDefaultConstructorWithBinding(MethodBinding inheritedConstructorBinding, boolean eraseThrownExceptions) {
      String baseName = "$anonymous";
      TypeBinding[] argumentTypes = inheritedConstructorBinding.parameters;
      int argumentsLength = argumentTypes.length;
      ConstructorDeclaration constructor = new ConstructorDeclaration(this.compilationResult);
      constructor.selector = new char[]{'x'};
      constructor.sourceStart = this.sourceStart;
      constructor.sourceEnd = this.sourceEnd;
      int newModifiers = this.modifiers & 7;
      if (inheritedConstructorBinding.isVarargs()) {
         newModifiers |= 128;
      }

      constructor.modifiers = newModifiers;
      constructor.bits |= 128;
      if (argumentsLength > 0) {
         Argument[] arguments = constructor.arguments = new Argument[argumentsLength];
         int i = argumentsLength;

         while(--i >= 0) {
            arguments[i] = new Argument((baseName + i).toCharArray(), 0L, null, 0);
         }
      }

      constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
      constructor.constructorCall.sourceStart = this.sourceStart;
      constructor.constructorCall.sourceEnd = this.sourceEnd;
      if (argumentsLength > 0) {
         Expression[] args = constructor.constructorCall.arguments = new Expression[argumentsLength];
         int i = argumentsLength;

         while(--i >= 0) {
            args[i] = new SingleNameReference((baseName + i).toCharArray(), 0L);
         }
      }

      if (this.methods == null) {
         this.methods = new AbstractMethodDeclaration[]{constructor};
      } else {
         AbstractMethodDeclaration[] newMethods;
         System.arraycopy(this.methods, 0, newMethods = new AbstractMethodDeclaration[this.methods.length + 1], 1, this.methods.length);
         newMethods[0] = constructor;
         this.methods = newMethods;
      }

      ReferenceBinding[] thrownExceptions = eraseThrownExceptions
         ? this.scope.environment().convertToRawTypes(inheritedConstructorBinding.thrownExceptions, true, true)
         : inheritedConstructorBinding.thrownExceptions;
      SourceTypeBinding sourceType = this.binding;
      constructor.binding = new MethodBinding(
         constructor.modifiers, argumentsLength == 0 ? Binding.NO_PARAMETERS : argumentTypes, thrownExceptions, sourceType
      );
      constructor.binding.tagBits |= inheritedConstructorBinding.tagBits & 128L;
      constructor.binding.modifiers |= 67108864;
      if (inheritedConstructorBinding.parameterNonNullness != null && argumentsLength > 0) {
         int len = inheritedConstructorBinding.parameterNonNullness.length;
         System.arraycopy(inheritedConstructorBinding.parameterNonNullness, 0, constructor.binding.parameterNonNullness = new Boolean[len], 0, len);
      }

      constructor.scope = new MethodScope(this.scope, constructor, true);
      constructor.bindArguments();
      constructor.constructorCall.resolve(constructor.scope);
      MethodBinding[] methodBindings = sourceType.methods();
      int length;
      MethodBinding[] var18;
      System.arraycopy(methodBindings, 0, var18 = new MethodBinding[(length = methodBindings.length) + 1], 1, length);
      var18[0] = constructor.binding;
      if (++length > 1) {
         ReferenceBinding.sortMethods(var18, 0, length);
      }

      sourceType.setMethods(var18);
      return constructor.binding;
   }

   public FieldDeclaration declarationOf(FieldBinding fieldBinding) {
      if (fieldBinding != null && this.fields != null) {
         int i = 0;

         for(int max = this.fields.length; i < max; ++i) {
            FieldDeclaration fieldDecl;
            if ((fieldDecl = this.fields[i]).binding == fieldBinding) {
               return fieldDecl;
            }
         }
      }

      return null;
   }

   public TypeDeclaration declarationOf(MemberTypeBinding memberTypeBinding) {
      if (memberTypeBinding != null && this.memberTypes != null) {
         int i = 0;

         for(int max = this.memberTypes.length; i < max; ++i) {
            TypeDeclaration memberTypeDecl;
            if (TypeBinding.equalsEquals((memberTypeDecl = this.memberTypes[i]).binding, memberTypeBinding)) {
               return memberTypeDecl;
            }
         }
      }

      return null;
   }

   public AbstractMethodDeclaration declarationOf(MethodBinding methodBinding) {
      if (methodBinding != null && this.methods != null) {
         int i = 0;

         for(int max = this.methods.length; i < max; ++i) {
            AbstractMethodDeclaration methodDecl;
            if ((methodDecl = this.methods[i]).binding == methodBinding) {
               return methodDecl;
            }
         }
      }

      return null;
   }

   public TypeDeclaration declarationOfType(char[][] typeName) {
      int typeNameLength = typeName.length;
      if (typeNameLength < 1 || !CharOperation.equals(typeName[0], this.name)) {
         return null;
      } else if (typeNameLength == 1) {
         return this;
      } else {
         char[][] subTypeName = new char[typeNameLength - 1][];
         System.arraycopy(typeName, 1, subTypeName, 0, typeNameLength - 1);

         for(int i = 0; i < this.memberTypes.length; ++i) {
            TypeDeclaration typeDecl = this.memberTypes[i].declarationOfType(subTypeName);
            if (typeDecl != null) {
               return typeDecl;
            }
         }

         return null;
      }
   }

   @Override
   public CompilationUnitDeclaration getCompilationUnitDeclaration() {
      return this.scope != null ? this.scope.compilationUnitScope().referenceContext : null;
   }

   public void generateCode(ClassFile enclosingClassFile) {
      if ((this.bits & 8192) == 0) {
         this.bits |= 8192;
         if (this.ignoreFurtherInvestigation) {
            if (this.binding != null) {
               ClassFile.createProblemType(this, this.scope.referenceCompilationUnit().compilationResult);
            }
         } else {
            try {
               ClassFile classFile = ClassFile.getNewInstance(this.binding);
               classFile.initialize(this.binding, enclosingClassFile, false);
               if (this.binding.isMemberType()) {
                  classFile.recordInnerClasses(this.binding);
               } else if (this.binding.isLocalType()) {
                  enclosingClassFile.recordInnerClasses(this.binding);
                  classFile.recordInnerClasses(this.binding);
               }

               TypeVariableBinding[] typeVariables = this.binding.typeVariables();
               int i = 0;

               for(int max = typeVariables.length; i < max; ++i) {
                  TypeVariableBinding typeVariableBinding = typeVariables[i];
                  if ((typeVariableBinding.tagBits & 2048L) != 0L) {
                     Util.recordNestedType(classFile, typeVariableBinding);
                  }
               }

               classFile.addFieldInfos();
               if (this.memberTypes != null) {
                  i = 0;

                  for(int max = this.memberTypes.length; i < max; ++i) {
                     TypeDeclaration memberType = this.memberTypes[i];
                     classFile.recordInnerClasses(memberType.binding);
                     memberType.generateCode(this.scope, classFile);
                  }
               }

               classFile.setForMethodInfos();
               if (this.methods != null) {
                  i = 0;

                  for(int max = this.methods.length; i < max; ++i) {
                     this.methods[i].generateCode(this.scope, classFile);
                  }
               }

               classFile.addSpecialMethods();
               if (this.ignoreFurtherInvestigation) {
                  throw new AbortType(this.scope.referenceCompilationUnit().compilationResult, null);
               }

               classFile.addAttributes();
               this.scope.referenceCompilationUnit().compilationResult.record(this.binding.constantPoolName(), classFile);
            } catch (AbortType var7) {
               if (this.binding == null) {
                  return;
               }

               ClassFile.createProblemType(this, this.scope.referenceCompilationUnit().compilationResult);
            }
         }
      }
   }

   @Override
   public void generateCode(BlockScope blockScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         if ((this.bits & 8192) == 0) {
            int pc = codeStream.position;
            if (this.binding != null) {
               SyntheticArgumentBinding[] enclosingInstances = ((NestedTypeBinding)this.binding).syntheticEnclosingInstances();
               int i = 0;
               int slotSize = 0;

               for(int count = enclosingInstances == null ? 0 : enclosingInstances.length; i < count; ++i) {
                  SyntheticArgumentBinding enclosingInstance = enclosingInstances[i];
                  enclosingInstance.resolvedPosition = ++slotSize;
                  if (slotSize > 255) {
                     blockScope.problemReporter().noMoreAvailableSpaceForArgument(enclosingInstance, blockScope.referenceType());
                  }
               }
            }

            this.generateCode(codeStream.classFile);
            codeStream.recordPositionsFrom(pc, this.sourceStart);
         }
      }
   }

   public void generateCode(ClassScope classScope, ClassFile enclosingClassFile) {
      if ((this.bits & 8192) == 0) {
         if (this.binding != null) {
            SyntheticArgumentBinding[] enclosingInstances = ((NestedTypeBinding)this.binding).syntheticEnclosingInstances();
            int i = 0;
            int slotSize = 0;

            for(int count = enclosingInstances == null ? 0 : enclosingInstances.length; i < count; ++i) {
               SyntheticArgumentBinding enclosingInstance = enclosingInstances[i];
               enclosingInstance.resolvedPosition = ++slotSize;
               if (slotSize > 255) {
                  classScope.problemReporter().noMoreAvailableSpaceForArgument(enclosingInstance, classScope.referenceType());
               }
            }
         }

         this.generateCode(enclosingClassFile);
      }
   }

   public void generateCode(CompilationUnitScope unitScope) {
      this.generateCode(null);
   }

   @Override
   public boolean hasErrors() {
      return this.ignoreFurtherInvestigation;
   }

   private void internalAnalyseCode(FlowContext flowContext, FlowInfo flowInfo) {
      if (!this.binding.isUsed() && this.binding.isOrEnclosedByPrivateType() && !this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
         this.scope.problemReporter().unusedPrivateType(this);
      }

      if (this.typeParameters != null && !this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
         int i = 0;

         for(int length = this.typeParameters.length; i < length; ++i) {
            TypeParameter typeParameter = this.typeParameters[i];
            if ((typeParameter.binding.modifiers & 134217728) == 0) {
               this.scope.problemReporter().unusedTypeParameter(typeParameter);
            }
         }
      }

      FlowContext parentContext = flowContext instanceof InitializationFlowContext ? null : flowContext;
      InitializationFlowContext initializerContext = new InitializationFlowContext(parentContext, this, flowInfo, flowContext, this.initializerScope);
      InitializationFlowContext staticInitializerContext = new InitializationFlowContext(null, this, flowInfo, flowContext, this.staticInitializerScope);
      FlowInfo nonStaticFieldInfo = flowInfo.unconditionalFieldLessCopy();
      FlowInfo staticFieldInfo = flowInfo.unconditionalFieldLessCopy();
      if (this.fields != null) {
         int i = 0;

         for(int count = this.fields.length; i < count; ++i) {
            FieldDeclaration field = this.fields[i];
            if (field.isStatic()) {
               if ((staticFieldInfo.tagBits & 1) != 0) {
                  field.bits &= Integer.MAX_VALUE;
               }

               staticInitializerContext.handledExceptions = Binding.ANY_EXCEPTION;
               staticFieldInfo = field.analyseCode(this.staticInitializerScope, staticInitializerContext, staticFieldInfo);
               if (staticFieldInfo == FlowInfo.DEAD_END) {
                  this.staticInitializerScope.problemReporter().initializerMustCompleteNormally(field);
                  staticFieldInfo = FlowInfo.initial(this.maxFieldCount).setReachMode(1);
               }
            } else {
               if ((nonStaticFieldInfo.tagBits & 1) != 0) {
                  field.bits &= Integer.MAX_VALUE;
               }

               initializerContext.handledExceptions = Binding.ANY_EXCEPTION;
               nonStaticFieldInfo = field.analyseCode(this.initializerScope, initializerContext, nonStaticFieldInfo);
               if (nonStaticFieldInfo == FlowInfo.DEAD_END) {
                  this.initializerScope.problemReporter().initializerMustCompleteNormally(field);
                  nonStaticFieldInfo = FlowInfo.initial(this.maxFieldCount).setReachMode(1);
               }
            }
         }
      }

      if (this.memberTypes != null) {
         int i = 0;

         for(int count = this.memberTypes.length; i < count; ++i) {
            if (flowContext != null) {
               this.memberTypes[i].analyseCode(this.scope, flowContext, nonStaticFieldInfo.copy().setReachMode(flowInfo.reachMode()));
            } else {
               this.memberTypes[i].analyseCode(this.scope);
            }
         }
      }

      if (this.methods != null) {
         UnconditionalFlowInfo outerInfo = flowInfo.unconditionalFieldLessCopy();
         FlowInfo constructorInfo = nonStaticFieldInfo.unconditionalInits().discardNonFieldInitializations().addInitializationsFrom(outerInfo);
         int i = 0;

         for(int count = this.methods.length; i < count; ++i) {
            AbstractMethodDeclaration method = this.methods[i];
            if (!method.ignoreFurtherInvestigation) {
               if (method.isInitializationMethod()) {
                  if (method.isStatic()) {
                     ((Clinit)method)
                        .analyseCode(
                           this.scope,
                           staticInitializerContext,
                           staticFieldInfo.unconditionalInits().discardNonFieldInitializations().addInitializationsFrom(outerInfo)
                        );
                  } else {
                     ((ConstructorDeclaration)method).analyseCode(this.scope, initializerContext, constructorInfo.copy(), flowInfo.reachMode());
                  }
               } else {
                  ((MethodDeclaration)method).analyseCode(this.scope, parentContext, flowInfo.copy());
               }
            }
         }
      }

      if (this.binding.isEnum() && !this.binding.isAnonymousType()) {
         this.enumValuesSyntheticfield = this.binding.addSyntheticFieldForEnumValues();
      }
   }

   public static final int kind(int flags) {
      switch(flags & 25088) {
         case 512:
            return 2;
         case 8704:
            return 4;
         case 16384:
            return 3;
         default:
            return 1;
      }
   }

   public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         NestedTypeBinding nestedType = (NestedTypeBinding)this.binding;
         MethodScope methodScope = currentScope.methodScope();
         if (!methodScope.isStatic && !methodScope.isConstructorCall) {
            nestedType.addSyntheticArgumentAndField(nestedType.enclosingType());
         }

         if (nestedType.isAnonymousType()) {
            ReferenceBinding superclassBinding = (ReferenceBinding)nestedType.superclass.erasure();
            if (superclassBinding.enclosingType() != null
               && !superclassBinding.isStatic()
               && (
                  !superclassBinding.isLocalType()
                     || ((NestedTypeBinding)superclassBinding).getSyntheticField(superclassBinding.enclosingType(), true) != null
                     || superclassBinding.isMemberType()
               )) {
               nestedType.addSyntheticArgument(superclassBinding.enclosingType());
            }

            if (!methodScope.isStatic && methodScope.isConstructorCall && currentScope.compilerOptions().complianceLevel >= 3211264L) {
               ReferenceBinding enclosing = nestedType.enclosingType();
               if (enclosing.isNestedType()) {
                  NestedTypeBinding nestedEnclosing = (NestedTypeBinding)enclosing;
                  SyntheticArgumentBinding syntheticEnclosingInstanceArgument = nestedEnclosing.getSyntheticArgument(
                     nestedEnclosing.enclosingType(), true, false
                  );
                  if (syntheticEnclosingInstanceArgument != null) {
                     nestedType.addSyntheticArgumentAndField(syntheticEnclosingInstanceArgument);
                  }
               }
            }
         }
      }
   }

   public void manageEnclosingInstanceAccessIfNecessary(ClassScope currentScope, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         NestedTypeBinding nestedType = (NestedTypeBinding)this.binding;
         nestedType.addSyntheticArgumentAndField(this.binding.enclosingType());
      }
   }

   public final boolean needClassInitMethod() {
      if ((this.bits & 1) != 0) {
         return true;
      } else {
         switch(kind(this.modifiers)) {
            case 2:
            case 4:
               if (this.fields != null) {
                  return true;
               }

               return false;
            case 3:
               return true;
            default:
               if (this.fields != null) {
                  int i = this.fields.length;

                  while(--i >= 0) {
                     FieldDeclaration field = this.fields[i];
                     if ((field.modifiers & 8) != 0) {
                        return true;
                     }
                  }
               }

               return false;
         }
      }
   }

   public void parseMethods(Parser parser, CompilationUnitDeclaration unit) {
      if (!unit.ignoreMethodBodies) {
         if (this.memberTypes != null) {
            int length = this.memberTypes.length;

            for(int i = 0; i < length; ++i) {
               TypeDeclaration typeDeclaration = this.memberTypes[i];
               typeDeclaration.parseMethods(parser, unit);
               this.bits |= typeDeclaration.bits & 524288;
            }
         }

         if (this.methods != null) {
            int length = this.methods.length;

            for(int i = 0; i < length; ++i) {
               AbstractMethodDeclaration abstractMethodDeclaration = this.methods[i];
               abstractMethodDeclaration.parseStatements(parser, unit);
               this.bits |= abstractMethodDeclaration.bits & 524288;
            }
         }

         if (this.fields != null) {
            int length = this.fields.length;

            for(int i = 0; i < length; ++i) {
               FieldDeclaration fieldDeclaration = this.fields[i];
               switch(fieldDeclaration.getKind()) {
                  case 2:
                     ((Initializer)fieldDeclaration).parseStatements(parser, this, unit);
                     this.bits |= fieldDeclaration.bits & 524288;
                     break;
               }
            }
         }
      }
   }

   @Override
   public StringBuffer print(int indent, StringBuffer output) {
      if (this.javadoc != null) {
         this.javadoc.print(indent, output);
      }

      if ((this.bits & 512) == 0) {
         printIndent(indent, output);
         this.printHeader(0, output);
      }

      return this.printBody(indent, output);
   }

   public StringBuffer printBody(int indent, StringBuffer output) {
      output.append(" {");
      if (this.memberTypes != null) {
         for(int i = 0; i < this.memberTypes.length; ++i) {
            if (this.memberTypes[i] != null) {
               output.append('\n');
               this.memberTypes[i].print(indent + 1, output);
            }
         }
      }

      if (this.fields != null) {
         for(int fieldI = 0; fieldI < this.fields.length; ++fieldI) {
            if (this.fields[fieldI] != null) {
               output.append('\n');
               this.fields[fieldI].print(indent + 1, output);
            }
         }
      }

      if (this.methods != null) {
         for(int i = 0; i < this.methods.length; ++i) {
            if (this.methods[i] != null) {
               output.append('\n');
               this.methods[i].print(indent + 1, output);
            }
         }
      }

      output.append('\n');
      return printIndent(indent, output).append('}');
   }

   public StringBuffer printHeader(int indent, StringBuffer output) {
      printModifiers(this.modifiers, output);
      if (this.annotations != null) {
         printAnnotations(this.annotations, output);
         output.append(' ');
      }

      switch(kind(this.modifiers)) {
         case 1:
            output.append("class ");
            break;
         case 2:
            output.append("interface ");
            break;
         case 3:
            output.append("enum ");
            break;
         case 4:
            output.append("@interface ");
      }

      output.append(this.name);
      if (this.typeParameters != null) {
         output.append("<");

         for(int i = 0; i < this.typeParameters.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.typeParameters[i].print(0, output);
         }

         output.append(">");
      }

      if (this.superclass != null) {
         output.append(" extends ");
         this.superclass.print(0, output);
      }

      if (this.superInterfaces != null && this.superInterfaces.length > 0) {
         switch(kind(this.modifiers)) {
            case 1:
            case 3:
               output.append(" implements ");
               break;
            case 2:
            case 4:
               output.append(" extends ");
         }

         for(int i = 0; i < this.superInterfaces.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.superInterfaces[i].print(0, output);
         }
      }

      return output;
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      return this.print(tab, output);
   }

   public int record(FunctionalExpression expression) {
      return this.functionalExpressionsCount++;
   }

   public void resolve() {
      SourceTypeBinding sourceType = this.binding;
      if (sourceType == null) {
         this.ignoreFurtherInvestigation = true;
      } else {
         try {
            long annotationTagBits = sourceType.getAnnotationTagBits();
            if ((annotationTagBits & 70368744177664L) == 0L && (sourceType.modifiers & 1048576) != 0 && this.scope.compilerOptions().sourceLevel >= 3211264L) {
               this.scope.problemReporter().missingDeprecatedAnnotationForType(this);
            }

            if ((annotationTagBits & 576460752303423488L) != 0L && !this.binding.isFunctionalInterface(this.scope)) {
               this.scope.problemReporter().notAFunctionalInterface(this);
            }

            if ((this.bits & 8) != 0) {
               this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart - 1, this.bodyEnd);
            }

            boolean needSerialVersion = this.scope.compilerOptions().getSeverity(536870920) != 256
               && sourceType.isClass()
               && sourceType.findSuperTypeOriginatingFrom(56, false) == null
               && sourceType.findSuperTypeOriginatingFrom(37, false) != null;
            if (needSerialVersion) {
               CompilationUnitScope compilationUnitScope;
               boolean var10000;
               label320: {
                  compilationUnitScope = this.scope.compilationUnitScope();
                  MethodBinding methodBinding = sourceType.getExactMethod(TypeConstants.WRITEREPLACE, Binding.NO_TYPES, compilationUnitScope);
                  if (methodBinding != null && methodBinding.isValidBinding() && methodBinding.returnType.id == 1) {
                     ReferenceBinding[] throwsExceptions = methodBinding.thrownExceptions;
                     if (methodBinding.thrownExceptions.length == 1 && throwsExceptions[0].id == 57) {
                        var10000 = false;
                        break label320;
                     }
                  }

                  var10000 = true;
               }

               needSerialVersion = var10000;
               if (needSerialVersion) {
                  boolean hasWriteObjectMethod = false;
                  boolean hasReadObjectMethod = false;
                  TypeBinding argumentTypeBinding = this.scope.getType(TypeConstants.JAVA_IO_OBJECTOUTPUTSTREAM, 3);
                  if (argumentTypeBinding.isValidBinding()) {
                     label309: {
                        MethodBinding var18 = sourceType.getExactMethod(
                           TypeConstants.WRITEOBJECT, new TypeBinding[]{argumentTypeBinding}, compilationUnitScope
                        );
                        if (var18 != null && var18.isValidBinding() && var18.modifiers == 2 && var18.returnType == TypeBinding.VOID) {
                           ReferenceBinding[] throwsExceptions = var18.thrownExceptions;
                           if (var18.thrownExceptions.length == 1 && throwsExceptions[0].id == 58) {
                              var10000 = true;
                              break label309;
                           }
                        }

                        var10000 = false;
                     }

                     hasWriteObjectMethod = var10000;
                  }

                  argumentTypeBinding = this.scope.getType(TypeConstants.JAVA_IO_OBJECTINPUTSTREAM, 3);
                  if (argumentTypeBinding.isValidBinding()) {
                     label296: {
                        MethodBinding var19 = sourceType.getExactMethod(TypeConstants.READOBJECT, new TypeBinding[]{argumentTypeBinding}, compilationUnitScope);
                        if (var19 != null && var19.isValidBinding() && var19.modifiers == 2 && var19.returnType == TypeBinding.VOID) {
                           ReferenceBinding[] throwsExceptions = var19.thrownExceptions;
                           if (var19.thrownExceptions.length == 1 && throwsExceptions[0].id == 58) {
                              var10000 = true;
                              break label296;
                           }
                        }

                        var10000 = false;
                     }

                     hasReadObjectMethod = var10000;
                  }

                  needSerialVersion = !hasWriteObjectMethod || !hasReadObjectMethod;
               }
            }

            if (sourceType.findSuperTypeOriginatingFrom(21, true) != null) {
               ReferenceBinding current = sourceType;

               do {
                  if (current.isGenericType()) {
                     this.scope.problemReporter().genericTypeCannotExtendThrowable(this);
                     break;
                  }

                  if (current.isStatic()) {
                     break;
                  }

                  if (current.isLocalType()) {
                     NestedTypeBinding nestedType = (NestedTypeBinding)current.erasure();
                     if (nestedType.scope.methodScope().isStatic) {
                        break;
                     }
                  }
               } while((current = current.enclosingType()) != null);
            }

            int localMaxFieldCount = 0;
            int lastVisibleFieldID = -1;
            boolean hasEnumConstants = false;
            FieldDeclaration[] enumConstantsWithoutBody = null;
            if (this.memberTypes != null) {
               int i = 0;

               for(int count = this.memberTypes.length; i < count; ++i) {
                  this.memberTypes[i].resolve(this.scope);
               }
            }

            if (this.fields != null) {
               int i = 0;

               for(int count = this.fields.length; i < count; ++i) {
                  FieldDeclaration field = this.fields[i];
                  switch(field.getKind()) {
                     case 2:
                        ((Initializer)field).lastVisibleFieldID = lastVisibleFieldID + 1;
                        break;
                     case 3:
                        hasEnumConstants = true;
                        if (!(field.initialization instanceof QualifiedAllocationExpression)) {
                           if (enumConstantsWithoutBody == null) {
                              enumConstantsWithoutBody = new FieldDeclaration[count];
                           }

                           enumConstantsWithoutBody[i] = field;
                        }
                     case 1:
                        FieldBinding fieldBinding = field.binding;
                        if (fieldBinding == null) {
                           if (field.initialization != null) {
                              field.initialization.resolve(field.isStatic() ? this.staticInitializerScope : this.initializerScope);
                           }

                           this.ignoreFurtherInvestigation = true;
                           continue;
                        }

                        if (needSerialVersion
                           && (fieldBinding.modifiers & 24) == 24
                           && CharOperation.equals(TypeConstants.SERIALVERSIONUID, fieldBinding.name)
                           && TypeBinding.equalsEquals(TypeBinding.LONG, fieldBinding.type)) {
                           needSerialVersion = false;
                        }

                        ++localMaxFieldCount;
                        lastVisibleFieldID = field.binding.id;
                  }

                  field.resolve(field.isStatic() ? this.staticInitializerScope : this.initializerScope);
               }
            }

            if (this.maxFieldCount < localMaxFieldCount) {
               this.maxFieldCount = localMaxFieldCount;
            }

            if (needSerialVersion) {
               TypeBinding javaxRmiCorbaStub = this.scope.getType(TypeConstants.JAVAX_RMI_CORBA_STUB, 4);
               if (javaxRmiCorbaStub.isValidBinding()) {
                  for(ReferenceBinding superclassBinding = this.binding.superclass;
                     superclassBinding != null;
                     superclassBinding = superclassBinding.superclass()
                  ) {
                     if (TypeBinding.equalsEquals(superclassBinding, javaxRmiCorbaStub)) {
                        needSerialVersion = false;
                        break;
                     }
                  }
               }

               if (needSerialVersion) {
                  this.scope.problemReporter().missingSerialVersion(this);
               }
            }

            switch(kind(this.modifiers)) {
               case 3:
                  if (this.binding.isAbstract()) {
                     if (!hasEnumConstants) {
                        int i = 0;

                        for(int count = this.methods.length; i < count; ++i) {
                           AbstractMethodDeclaration methodDeclaration = this.methods[i];
                           if (methodDeclaration.isAbstract() && methodDeclaration.binding != null) {
                              this.scope.problemReporter().enumAbstractMethodMustBeImplemented(methodDeclaration);
                           }
                        }
                     } else if (enumConstantsWithoutBody != null) {
                        int i = 0;

                        for(int count = this.methods.length; i < count; ++i) {
                           AbstractMethodDeclaration methodDeclaration = this.methods[i];
                           if (methodDeclaration.isAbstract() && methodDeclaration.binding != null) {
                              int f = 0;

                              for(int l = enumConstantsWithoutBody.length; f < l; ++f) {
                                 if (enumConstantsWithoutBody[f] != null) {
                                    this.scope.problemReporter().enumConstantMustImplementAbstractMethod(methodDeclaration, enumConstantsWithoutBody[f]);
                                 }
                              }
                           }
                        }
                     }
                  }
                  break;
               case 4:
                  if (this.superclass != null) {
                     this.scope.problemReporter().annotationTypeDeclarationCannotHaveSuperclass(this);
                  }

                  if (this.superInterfaces != null) {
                     this.scope.problemReporter().annotationTypeDeclarationCannotHaveSuperinterfaces(this);
                  }
            }

            int missingAbstractMethodslength = this.missingAbstractMethods == null ? 0 : this.missingAbstractMethods.length;
            int methodsLength = this.methods == null ? 0 : this.methods.length;
            if (methodsLength + missingAbstractMethodslength > 65535) {
               this.scope.problemReporter().tooManyMethods(this);
            }

            if (this.methods != null) {
               int i = 0;

               for(int count = this.methods.length; i < count; ++i) {
                  this.methods[i].resolve(this.scope);
               }
            }

            if (this.javadoc != null) {
               if (this.scope != null && this.name != TypeConstants.PACKAGE_INFO_NAME) {
                  this.javadoc.resolve(this.scope);
               }
            } else if (!sourceType.isLocalType()) {
               int visibility = sourceType.modifiers & 7;
               ProblemReporter reporter = this.scope.problemReporter();
               int severity = reporter.computeSeverity(-1610612250);
               if (severity != 256) {
                  if (this.enclosingType != null) {
                     visibility = Util.computeOuterMostVisibility(this.enclosingType, visibility);
                  }

                  int javadocModifiers = this.binding.modifiers & -8 | visibility;
                  reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
               }
            }
         } catch (AbortType var15) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   @Override
   public void resolve(BlockScope blockScope) {
      if ((this.bits & 512) == 0) {
         Binding existing = blockScope.getType(this.name);
         if (existing instanceof ReferenceBinding && existing != this.binding && existing.isValidBinding()) {
            ReferenceBinding existingType = (ReferenceBinding)existing;
            if (!(existingType instanceof TypeVariableBinding)) {
               if (existingType instanceof LocalTypeBinding && ((LocalTypeBinding)existingType).scope.methodScope() == blockScope.methodScope()) {
                  blockScope.problemReporter().duplicateNestedType(this);
               } else if (existingType instanceof LocalTypeBinding
                  && blockScope.isLambdaSubscope()
                  && blockScope.enclosingLambdaScope().enclosingMethodScope() == ((LocalTypeBinding)existingType).scope.methodScope()) {
                  blockScope.problemReporter().duplicateNestedType(this);
               } else if (blockScope.isDefinedInType(existingType)) {
                  blockScope.problemReporter().typeCollidesWithEnclosingType(this);
               } else if (blockScope.isDefinedInSameUnit(existingType)) {
                  blockScope.problemReporter().typeHiding(this, existingType);
               }
            } else {
               blockScope.problemReporter().typeHiding(this, (TypeVariableBinding)existingType);

               for(Scope outerScope = blockScope.parent; outerScope != null; outerScope = outerScope.parent) {
                  Binding existing2 = outerScope.getType(this.name);
                  if (existing2 instanceof TypeVariableBinding && existing2.isValidBinding()) {
                     TypeVariableBinding tvb = (TypeVariableBinding)existingType;
                     Binding declaringElement = tvb.declaringElement;
                     if (declaringElement instanceof ReferenceBinding && CharOperation.equals(((ReferenceBinding)declaringElement).sourceName(), this.name)) {
                        blockScope.problemReporter().typeCollidesWithEnclosingType(this);
                        break;
                     }
                  } else {
                     if (existing2 instanceof ReferenceBinding && existing2.isValidBinding() && outerScope.isDefinedInType((ReferenceBinding)existing2)) {
                        blockScope.problemReporter().typeCollidesWithEnclosingType(this);
                        break;
                     }

                     if (existing2 == null) {
                        break;
                     }
                  }
               }
            }
         }

         blockScope.addLocalType(this);
      }

      if (this.binding != null) {
         blockScope.referenceCompilationUnit().record((LocalTypeBinding)this.binding);
         this.resolve();
         this.updateMaxFieldCount();
      }
   }

   public void resolve(ClassScope upperScope) {
      if (this.binding != null && this.binding instanceof LocalTypeBinding) {
         upperScope.referenceCompilationUnit().record((LocalTypeBinding)this.binding);
      }

      this.resolve();
      this.updateMaxFieldCount();
   }

   public void resolve(CompilationUnitScope upperScope) {
      this.resolve();
      this.updateMaxFieldCount();
   }

   @Override
   public void tagAsHavingErrors() {
      this.ignoreFurtherInvestigation = true;
   }

   @Override
   public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
   }

   public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {
      try {
         if (visitor.visit(this, unitScope)) {
            if (this.javadoc != null) {
               this.javadoc.traverse(visitor, this.scope);
            }

            if (this.annotations != null) {
               int annotationsLength = this.annotations.length;

               for(int i = 0; i < annotationsLength; ++i) {
                  this.annotations[i].traverse(visitor, this.staticInitializerScope);
               }
            }

            if (this.superclass != null) {
               this.superclass.traverse(visitor, this.scope);
            }

            if (this.superInterfaces != null) {
               int length = this.superInterfaces.length;

               for(int i = 0; i < length; ++i) {
                  this.superInterfaces[i].traverse(visitor, this.scope);
               }
            }

            if (this.typeParameters != null) {
               int length = this.typeParameters.length;

               for(int i = 0; i < length; ++i) {
                  this.typeParameters[i].traverse(visitor, this.scope);
               }
            }

            if (this.memberTypes != null) {
               int length = this.memberTypes.length;

               for(int i = 0; i < length; ++i) {
                  this.memberTypes[i].traverse(visitor, this.scope);
               }
            }

            if (this.fields != null) {
               int length = this.fields.length;

               for(int i = 0; i < length; ++i) {
                  FieldDeclaration field;
                  if ((field = this.fields[i]).isStatic()) {
                     field.traverse(visitor, this.staticInitializerScope);
                  } else {
                     field.traverse(visitor, this.initializerScope);
                  }
               }
            }

            if (this.methods != null) {
               int length = this.methods.length;

               for(int i = 0; i < length; ++i) {
                  this.methods[i].traverse(visitor, this.scope);
               }
            }
         }

         visitor.endVisit(this, unitScope);
      } catch (AbortType var6) {
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      try {
         if (visitor.visit(this, blockScope)) {
            if (this.javadoc != null) {
               this.javadoc.traverse(visitor, this.scope);
            }

            if (this.annotations != null) {
               int annotationsLength = this.annotations.length;

               for(int i = 0; i < annotationsLength; ++i) {
                  this.annotations[i].traverse(visitor, this.staticInitializerScope);
               }
            }

            if (this.superclass != null) {
               this.superclass.traverse(visitor, this.scope);
            }

            if (this.superInterfaces != null) {
               int length = this.superInterfaces.length;

               for(int i = 0; i < length; ++i) {
                  this.superInterfaces[i].traverse(visitor, this.scope);
               }
            }

            if (this.typeParameters != null) {
               int length = this.typeParameters.length;

               for(int i = 0; i < length; ++i) {
                  this.typeParameters[i].traverse(visitor, this.scope);
               }
            }

            if (this.memberTypes != null) {
               int length = this.memberTypes.length;

               for(int i = 0; i < length; ++i) {
                  this.memberTypes[i].traverse(visitor, this.scope);
               }
            }

            if (this.fields != null) {
               int length = this.fields.length;

               for(int i = 0; i < length; ++i) {
                  FieldDeclaration field = this.fields[i];
                  if (!field.isStatic() || field.isFinal()) {
                     field.traverse(visitor, this.initializerScope);
                  }
               }
            }

            if (this.methods != null) {
               int length = this.methods.length;

               for(int i = 0; i < length; ++i) {
                  this.methods[i].traverse(visitor, this.scope);
               }
            }
         }

         visitor.endVisit(this, blockScope);
      } catch (AbortType var6) {
      }
   }

   public void traverse(ASTVisitor visitor, ClassScope classScope) {
      try {
         if (visitor.visit(this, classScope)) {
            if (this.javadoc != null) {
               this.javadoc.traverse(visitor, this.scope);
            }

            if (this.annotations != null) {
               int annotationsLength = this.annotations.length;

               for(int i = 0; i < annotationsLength; ++i) {
                  this.annotations[i].traverse(visitor, this.staticInitializerScope);
               }
            }

            if (this.superclass != null) {
               this.superclass.traverse(visitor, this.scope);
            }

            if (this.superInterfaces != null) {
               int length = this.superInterfaces.length;

               for(int i = 0; i < length; ++i) {
                  this.superInterfaces[i].traverse(visitor, this.scope);
               }
            }

            if (this.typeParameters != null) {
               int length = this.typeParameters.length;

               for(int i = 0; i < length; ++i) {
                  this.typeParameters[i].traverse(visitor, this.scope);
               }
            }

            if (this.memberTypes != null) {
               int length = this.memberTypes.length;

               for(int i = 0; i < length; ++i) {
                  this.memberTypes[i].traverse(visitor, this.scope);
               }
            }

            if (this.fields != null) {
               int length = this.fields.length;

               for(int i = 0; i < length; ++i) {
                  FieldDeclaration field;
                  if ((field = this.fields[i]).isStatic()) {
                     field.traverse(visitor, this.staticInitializerScope);
                  } else {
                     field.traverse(visitor, this.initializerScope);
                  }
               }
            }

            if (this.methods != null) {
               int length = this.methods.length;

               for(int i = 0; i < length; ++i) {
                  this.methods[i].traverse(visitor, this.scope);
               }
            }
         }

         visitor.endVisit(this, classScope);
      } catch (AbortType var6) {
      }
   }

   void updateMaxFieldCount() {
      if (this.binding != null) {
         TypeDeclaration outerMostType = this.scope.outerMostClassScope().referenceType();
         if (this.maxFieldCount > outerMostType.maxFieldCount) {
            outerMostType.maxFieldCount = this.maxFieldCount;
         } else {
            this.maxFieldCount = outerMostType.maxFieldCount;
         }
      }
   }

   public boolean isPackageInfo() {
      return CharOperation.equals(this.name, TypeConstants.PACKAGE_INFO_NAME);
   }

   public boolean isSecondary() {
      return (this.bits & 4096) != 0;
   }
}
