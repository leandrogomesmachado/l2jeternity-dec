package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.StackMapFrameCodeStream;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ConstructorDeclaration extends AbstractMethodDeclaration {
   public ExplicitConstructorCall constructorCall;
   public TypeParameter[] typeParameters;

   public ConstructorDeclaration(CompilationResult compilationResult) {
      super(compilationResult);
   }

   public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo, int initialReachMode) {
      if (!this.ignoreFurtherInvestigation) {
         int nonStaticFieldInfoReachMode;
         MethodBinding methodBinding;
         nonStaticFieldInfoReachMode = flowInfo.reachMode();
         flowInfo.setReachMode(initialReachMode);
         MethodBinding constructorBinding = this.binding;
         label184:
         if (this.binding != null
            && (this.bits & 128) == 0
            && !constructorBinding.isUsed()
            && (
               constructorBinding.isPrivate()
                  ? (this.binding.declaringClass.tagBits & 1152921504606846976L) != 0L
                  : constructorBinding.isOrEnclosedByPrivateType()
            )
            && this.constructorCall != null) {
            if (this.constructorCall.accessMode != 3) {
               ReferenceBinding superClass = constructorBinding.declaringClass.superclass();
               if (superClass == null) {
                  break label184;
               }

               methodBinding = superClass.getExactConstructor(Binding.NO_PARAMETERS);
               if (methodBinding == null || !methodBinding.canBeSeenBy(SuperReference.implicitSuperConstructorCall(), this.scope)) {
                  break label184;
               }

               ReferenceBinding declaringClass = constructorBinding.declaringClass;
               if (constructorBinding.isPublic()
                  && constructorBinding.parameters.length == 0
                  && declaringClass.isStatic()
                  && declaringClass.findSuperTypeOriginatingFrom(56, false) != null) {
                  break label184;
               }
            }

            this.scope.problemReporter().unusedPrivateConstructor(this);
         }

         if (this.isRecursive(null)) {
            this.scope.problemReporter().recursiveConstructorInvocation(this.constructorCall);
         }

         if (this.typeParameters != null && !this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
            int i = 0;

            for(int length = this.typeParameters.length; i < length; ++i) {
               methodBinding = this.typeParameters[i];
               if ((methodBinding.binding.modifiers & 134217728) == 0) {
                  this.scope.problemReporter().unusedTypeParameter(methodBinding);
               }
            }
         }

         try {
            ExceptionHandlingFlowContext constructorContext = new ExceptionHandlingFlowContext(
               initializerFlowContext.parent, this, this.binding.thrownExceptions, initializerFlowContext, this.scope, FlowInfo.DEAD_END
            );
            initializerFlowContext.checkInitializerExceptions(this.scope, constructorContext, flowInfo);
            if (this.binding.declaringClass.isAnonymousType()) {
               ArrayList computedExceptions = constructorContext.extendedExceptions;
               if (computedExceptions != null && (methodBinding = computedExceptions.size()) > 0) {
                  ReferenceBinding[] actuallyThrownExceptions;
                  computedExceptions.toArray(actuallyThrownExceptions = new ReferenceBinding[methodBinding]);
                  this.binding.thrownExceptions = actuallyThrownExceptions;
               }
            }

            analyseArguments(classScope.environment(), flowInfo, this.arguments, this.binding);
            if (this.constructorCall != null) {
               if (this.constructorCall.accessMode == 3) {
                  FieldBinding[] fields = this.binding.declaringClass.fields();
                  int i = 0;

                  for(int count = fields.length; i < count; ++i) {
                     FieldBinding field;
                     if (!(field = fields[i]).isStatic()) {
                        flowInfo.markAsDefinitelyAssigned(field);
                     }
                  }
               }

               flowInfo = this.constructorCall.analyseCode(this.scope, constructorContext, flowInfo);
            }

            flowInfo.setReachMode(nonStaticFieldInfoReachMode);
            if (this.statements != null) {
               boolean enableSyntacticNullAnalysisForFields = this.scope.compilerOptions().enableSyntacticNullAnalysisForFields;
               int complaintLevel = (nonStaticFieldInfoReachMode & 3) == 0 ? 0 : 1;
               int i = 0;

               for(int count = this.statements.length; i < count; ++i) {
                  Statement stat = this.statements[i];
                  if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < 2) {
                     flowInfo = stat.analyseCode(this.scope, constructorContext, flowInfo);
                  }

                  if (enableSyntacticNullAnalysisForFields) {
                     constructorContext.expireNullCheckedFieldInfo();
                  }
               }
            }

            if ((flowInfo.tagBits & 1) == 0) {
               this.bits |= 64;
            }

            if (this.constructorCall != null && this.constructorCall.accessMode != 3) {
               flowInfo = flowInfo.mergedWith(constructorContext.initsOnReturn);
               FieldBinding[] fields = this.binding.declaringClass.fields();
               int i = 0;

               for(int count = fields.length; i < count; ++i) {
                  FieldBinding field = fields[i];
                  if (!field.isStatic() && !flowInfo.isDefinitelyAssigned(field)) {
                     if (field.isFinal()) {
                        this.scope
                           .problemReporter()
                           .uninitializedBlankFinalField(
                              field, (ASTNode)((this.bits & 128) != 0 ? this.scope.referenceType().declarationOf(field.original()) : this)
                           );
                     } else if (field.isNonNull() || field.type.isFreeTypeVariable()) {
                        FieldDeclaration fieldDecl = this.scope.referenceType().declarationOf(field.original());
                        if (!this.isValueProvidedUsingAnnotation(fieldDecl)) {
                           this.scope.problemReporter().uninitializedNonNullField(field, (ASTNode)((this.bits & 128) != 0 ? fieldDecl : this));
                        }
                     }
                  }
               }
            }

            constructorContext.complainIfUnusedExceptionHandlers(this);
            this.scope.checkUnusedParameters(this.binding);
            this.scope.checkUnclosedCloseables(flowInfo, null, null, null);
         } catch (AbortMethod var12) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   boolean isValueProvidedUsingAnnotation(FieldDeclaration fieldDecl) {
      if (fieldDecl.annotations != null) {
         int length = fieldDecl.annotations.length;

         for(int i = 0; i < length; ++i) {
            Annotation annotation = fieldDecl.annotations[i];
            if (annotation.resolvedType.id == 80) {
               return true;
            }

            if (annotation.resolvedType.id == 81) {
               MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
               if (memberValuePairs == Annotation.NoValuePairs) {
                  return true;
               }

               for(int j = 0; j < memberValuePairs.length; ++j) {
                  if (CharOperation.equals(memberValuePairs[j].name, TypeConstants.OPTIONAL)) {
                     return memberValuePairs[j].value instanceof FalseLiteral;
                  }
               }
            } else if (annotation.resolvedType.id == 82) {
               MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
               if (memberValuePairs == Annotation.NoValuePairs) {
                  return true;
               }

               for(int j = 0; j < memberValuePairs.length; ++j) {
                  if (CharOperation.equals(memberValuePairs[j].name, TypeConstants.REQUIRED)) {
                     return memberValuePairs[j].value instanceof TrueLiteral;
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public void generateCode(ClassScope classScope, ClassFile classFile) {
      int problemResetPC = 0;
      if (this.ignoreFurtherInvestigation) {
         if (this.binding != null) {
            CategorizedProblem[] problems = this.scope.referenceCompilationUnit().compilationResult.getProblems();
            int problemsLength;
            CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
            System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
            classFile.addProblemConstructor(this, this.binding, problemsCopy);
         }
      } else {
         boolean restart = false;
         boolean abort = false;
         CompilationResult unitResult = null;
         int problemCount = 0;
         if (classScope != null) {
            TypeDeclaration referenceContext = classScope.referenceContext;
            if (referenceContext != null) {
               unitResult = referenceContext.compilationResult();
               problemCount = unitResult.problemCount;
            }
         }

         do {
            try {
               problemResetPC = classFile.contentsOffset;
               this.internalGenerateCode(classScope, classFile);
               restart = false;
            } catch (AbortMethod var11) {
               if (var11.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
                  classFile.contentsOffset = problemResetPC;
                  --classFile.methodCount;
                  classFile.codeStream.resetInWideMode();
                  if (unitResult != null) {
                     unitResult.problemCount = problemCount;
                  }

                  restart = true;
               } else if (var11.compilationResult == CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
                  classFile.contentsOffset = problemResetPC;
                  --classFile.methodCount;
                  classFile.codeStream.resetForCodeGenUnusedLocals();
                  if (unitResult != null) {
                     unitResult.problemCount = problemCount;
                  }

                  restart = true;
               } else {
                  restart = false;
                  abort = true;
               }
            }
         } while(restart);

         if (abort) {
            CategorizedProblem[] problems = this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
            int problemsLength;
            CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
            System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
            classFile.addProblemConstructor(this, this.binding, problemsCopy, problemResetPC);
         }
      }
   }

   public void generateSyntheticFieldInitializationsIfNecessary(MethodScope methodScope, CodeStream codeStream, ReferenceBinding declaringClass) {
      if (declaringClass.isNestedType()) {
         NestedTypeBinding nestedType = (NestedTypeBinding)declaringClass;
         SyntheticArgumentBinding[] syntheticArgs = nestedType.syntheticEnclosingInstances();
         if (syntheticArgs != null) {
            int i = 0;

            for(int max = syntheticArgs.length; i < max; ++i) {
               SyntheticArgumentBinding syntheticArg;
               if ((syntheticArg = syntheticArgs[i]).matchingField != null) {
                  codeStream.aload_0();
                  codeStream.load(syntheticArg);
                  codeStream.fieldAccess((byte)-75, syntheticArg.matchingField, null);
               }
            }
         }

         syntheticArgs = nestedType.syntheticOuterLocalVariables();
         if (syntheticArgs != null) {
            int i = 0;

            for(int max = syntheticArgs.length; i < max; ++i) {
               SyntheticArgumentBinding syntheticArg;
               if ((syntheticArg = syntheticArgs[i]).matchingField != null) {
                  codeStream.aload_0();
                  codeStream.load(syntheticArg);
                  codeStream.fieldAccess((byte)-75, syntheticArg.matchingField, null);
               }
            }
         }
      }
   }

   private void internalGenerateCode(ClassScope classScope, ClassFile classFile) {
      classFile.generateMethodInfoHeader(this.binding);
      int methodAttributeOffset = classFile.contentsOffset;
      int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
      if (!this.binding.isNative() && !this.binding.isAbstract()) {
         TypeDeclaration declaringType = classScope.referenceContext;
         int codeAttributeOffset = classFile.contentsOffset;
         classFile.generateCodeAttributeHeader();
         CodeStream codeStream = classFile.codeStream;
         codeStream.reset(this, classFile);
         ReferenceBinding declaringClass = this.binding.declaringClass;
         int enumOffset = declaringClass.isEnum() ? 2 : 0;
         int argSlotSize = 1 + enumOffset;
         if (declaringClass.isNestedType()) {
            this.scope.extraSyntheticArguments = declaringClass.syntheticOuterLocalVariables();
            this.scope.computeLocalVariablePositions(declaringClass.getEnclosingInstancesSlotSize() + 1 + enumOffset, codeStream);
            argSlotSize += declaringClass.getEnclosingInstancesSlotSize();
            argSlotSize += declaringClass.getOuterLocalVariablesSlotSize();
         } else {
            this.scope.computeLocalVariablePositions(1 + enumOffset, codeStream);
         }

         if (this.arguments != null) {
            int i = 0;

            for(int max = this.arguments.length; i < max; ++i) {
               LocalVariableBinding argBinding = this.arguments[i].binding;
               codeStream.addVisibleLocalVariable(this.arguments[i].binding);
               argBinding.recordInitializationStartPC(0);
               switch(argBinding.type.id) {
                  case 7:
                  case 8:
                     argSlotSize += 2;
                     break;
                  default:
                     ++argSlotSize;
               }
            }
         }

         MethodScope initializerScope = declaringType.initializerScope;
         initializerScope.computeLocalVariablePositions(argSlotSize, codeStream);
         boolean needFieldInitializations = this.constructorCall == null || this.constructorCall.accessMode != 3;
         boolean preInitSyntheticFields = this.scope.compilerOptions().targetJDK >= 3145728L;
         if (needFieldInitializations && preInitSyntheticFields) {
            this.generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
            codeStream.recordPositionsFrom(0, this.bodyStart);
         }

         if (this.constructorCall != null) {
            this.constructorCall.generateCode(this.scope, codeStream);
         }

         if (needFieldInitializations) {
            if (!preInitSyntheticFields) {
               this.generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
            }

            if (declaringType.fields != null) {
               int i = 0;

               for(int max = declaringType.fields.length; i < max; ++i) {
                  FieldDeclaration fieldDecl;
                  if (!(fieldDecl = declaringType.fields[i]).isStatic()) {
                     fieldDecl.generateCode(initializerScope, codeStream);
                  }
               }
            }
         }

         if (this.statements != null) {
            int i = 0;

            for(int max = this.statements.length; i < max; ++i) {
               this.statements[i].generateCode(this.scope, codeStream);
            }
         }

         if (this.ignoreFurtherInvestigation) {
            throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
         }

         if ((this.bits & 64) != 0) {
            codeStream.return_();
         }

         codeStream.exitUserScope(this.scope);
         codeStream.recordPositionsFrom(0, this.bodyEnd);

         try {
            classFile.completeCodeAttribute(codeAttributeOffset);
         } catch (NegativeArraySizeException var17) {
            throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
         }

         ++attributeNumber;
         if (codeStream instanceof StackMapFrameCodeStream && needFieldInitializations && declaringType.fields != null) {
            ((StackMapFrameCodeStream)codeStream).resetSecretLocals();
         }
      }

      classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
   }

   @Override
   public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
      TypeReference fakeReturnType = new SingleTypeReference(this.selector, 0L);
      fakeReturnType.resolvedType = this.binding.declaringClass;
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(fakeReturnType, targetType, allAnnotationContexts);
      int i = 0;

      for(int max = this.annotations.length; i < max; ++i) {
         Annotation annotation = this.annotations[i];
         annotation.traverse(collector, null);
      }
   }

   @Override
   public boolean isConstructor() {
      return true;
   }

   @Override
   public boolean isDefaultConstructor() {
      return (this.bits & 128) != 0;
   }

   @Override
   public boolean isInitializationMethod() {
      return true;
   }

   public boolean isRecursive(ArrayList visited) {
      if (this.binding != null
         && this.constructorCall != null
         && this.constructorCall.binding != null
         && !this.constructorCall.isSuperAccess()
         && this.constructorCall.binding.isValidBinding()) {
         ConstructorDeclaration targetConstructor = (ConstructorDeclaration)this.scope.referenceType().declarationOf(this.constructorCall.binding.original());
         if (targetConstructor == null) {
            return false;
         } else if (this == targetConstructor) {
            return true;
         } else {
            if (visited == null) {
               visited = new ArrayList(1);
            } else {
               int index = visited.indexOf(this);
               if (index >= 0) {
                  if (index == 0) {
                     return true;
                  }

                  return false;
               }
            }

            visited.add(this);
            return targetConstructor.isRecursive(visited);
         }
      } else {
         return false;
      }
   }

   @Override
   public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
      if ((this.bits & 128) != 0 && this.constructorCall == null) {
         this.constructorCall = SuperReference.implicitSuperConstructorCall();
         this.constructorCall.sourceStart = this.sourceStart;
         this.constructorCall.sourceEnd = this.sourceEnd;
      } else {
         parser.parse(this, unit, false);
      }
   }

   @Override
   public StringBuffer printBody(int indent, StringBuffer output) {
      output.append(" {");
      if (this.constructorCall != null) {
         output.append('\n');
         this.constructorCall.printStatement(indent, output);
      }

      if (this.statements != null) {
         for(int i = 0; i < this.statements.length; ++i) {
            output.append('\n');
            this.statements[i].printStatement(indent, output);
         }
      }

      output.append('\n');
      printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
      return output;
   }

   @Override
   public void resolveJavadoc() {
      if (this.binding == null || this.javadoc != null) {
         super.resolveJavadoc();
      } else if ((this.bits & 128) == 0 && this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
         int javadocVisibility = this.binding.modifiers & 7;
         ClassScope classScope = this.scope.classScope();
         ProblemReporter reporter = this.scope.problemReporter();
         int severity = reporter.computeSeverity(-1610612250);
         if (severity != 256) {
            if (classScope != null) {
               javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
            }

            int javadocModifiers = this.binding.modifiers & -8 | javadocVisibility;
            reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
         }
      }
   }

   @Override
   public void resolveStatements() {
      SourceTypeBinding sourceType = this.scope.enclosingSourceType();
      if (!CharOperation.equals(sourceType.sourceName, this.selector)) {
         this.scope.problemReporter().missingReturnType(this);
      }

      if (this.binding != null && !this.binding.isPrivate()) {
         sourceType.tagBits |= 1152921504606846976L;
      }

      if (this.constructorCall != null) {
         if (sourceType.id == 1 && this.constructorCall.accessMode != 3) {
            if (this.constructorCall.accessMode == 2) {
               this.scope.problemReporter().cannotUseSuperInJavaLangObject(this.constructorCall);
            }

            this.constructorCall = null;
         } else {
            this.constructorCall.resolve(this.scope);
         }
      }

      if ((this.modifiers & 16777216) != 0) {
         this.scope.problemReporter().methodNeedBody(this);
      }

      super.resolveStatements();
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope classScope) {
      if (visitor.visit(this, classScope)) {
         if (this.javadoc != null) {
            this.javadoc.traverse(visitor, this.scope);
         }

         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, this.scope);
            }
         }

         if (this.typeParameters != null) {
            int typeParametersLength = this.typeParameters.length;

            for(int i = 0; i < typeParametersLength; ++i) {
               this.typeParameters[i].traverse(visitor, this.scope);
            }
         }

         if (this.arguments != null) {
            int argumentLength = this.arguments.length;

            for(int i = 0; i < argumentLength; ++i) {
               this.arguments[i].traverse(visitor, this.scope);
            }
         }

         if (this.thrownExceptions != null) {
            int thrownExceptionsLength = this.thrownExceptions.length;

            for(int i = 0; i < thrownExceptionsLength; ++i) {
               this.thrownExceptions[i].traverse(visitor, this.scope);
            }
         }

         if (this.constructorCall != null) {
            this.constructorCall.traverse(visitor, this.scope);
         }

         if (this.statements != null) {
            int statementsLength = this.statements.length;

            for(int i = 0; i < statementsLength; ++i) {
               this.statements[i].traverse(visitor, this.scope);
            }
         }
      }

      visitor.endVisit(this, classScope);
   }

   @Override
   public TypeParameter[] typeParameters() {
      return this.typeParameters;
   }
}
