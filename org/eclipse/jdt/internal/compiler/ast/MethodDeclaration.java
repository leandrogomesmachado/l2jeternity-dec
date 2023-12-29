package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public class MethodDeclaration extends AbstractMethodDeclaration {
   public TypeReference returnType;
   public TypeParameter[] typeParameters;

   public MethodDeclaration(CompilationResult compilationResult) {
      super(compilationResult);
      this.bits |= 256;
   }

   public void analyseCode(ClassScope classScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (!this.ignoreFurtherInvestigation) {
         try {
            if (this.binding == null) {
               return;
            }

            if (!this.binding.isUsed()
               && !this.binding.isAbstract()
               && (this.binding.isPrivate() || (this.binding.modifiers & 805306368) == 0 && this.binding.isOrEnclosedByPrivateType())
               && !classScope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
               this.scope.problemReporter().unusedPrivateMethod(this);
            }

            if (this.binding.declaringClass.isEnum() && (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF)) {
               return;
            }

            if (this.binding.isAbstract() || this.binding.isNative()) {
               return;
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

            ExceptionHandlingFlowContext methodContext = new ExceptionHandlingFlowContext(
               flowContext, this, this.binding.thrownExceptions, null, this.scope, FlowInfo.DEAD_END
            );
            analyseArguments(classScope.environment(), flowInfo, this.arguments, this.binding);
            if (this.binding.declaringClass instanceof MemberTypeBinding && !this.binding.declaringClass.isStatic()) {
               this.bits &= -257;
            }

            if (this.statements != null) {
               boolean enableSyntacticNullAnalysisForFields = this.scope.compilerOptions().enableSyntacticNullAnalysisForFields;
               int complaintLevel = (flowInfo.reachMode() & 3) == 0 ? 0 : 1;
               int i = 0;

               for(int count = this.statements.length; i < count; ++i) {
                  Statement stat = this.statements[i];
                  if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < 2) {
                     flowInfo = stat.analyseCode(this.scope, methodContext, flowInfo);
                  }

                  if (enableSyntacticNullAnalysisForFields) {
                     methodContext.expireNullCheckedFieldInfo();
                  }
               }
            } else {
               this.bits &= -257;
            }

            TypeBinding returnTypeBinding = this.binding.returnType;
            if (returnTypeBinding != TypeBinding.VOID && !this.isAbstract()) {
               if (flowInfo != FlowInfo.DEAD_END) {
                  this.scope.problemReporter().shouldReturn(returnTypeBinding, this);
               }
            } else if ((flowInfo.tagBits & 1) == 0) {
               this.bits |= 64;
            }

            methodContext.complainIfUnusedExceptionHandlers(this);
            this.scope.checkUnusedParameters(this.binding);
            if (!this.binding.isStatic()
               && (this.bits & 256) != 0
               && !this.isDefaultMethod()
               && !this.binding.isOverriding()
               && !this.binding.isImplementing()) {
               if (!this.binding.isPrivate() && !this.binding.isFinal() && !this.binding.declaringClass.isFinal()) {
                  this.scope.problemReporter().methodCanBePotentiallyDeclaredStatic(this);
               } else {
                  this.scope.problemReporter().methodCanBeDeclaredStatic(this);
               }
            }

            this.scope.checkUnclosedCloseables(flowInfo, null, null, null);
         } catch (AbortMethod var10) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   @Override
   public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(this.returnType, targetType, allAnnotationContexts);
      int i = 0;

      for(int max = this.annotations.length; i < max; ++i) {
         Annotation annotation = this.annotations[i];
         annotation.traverse(collector, null);
      }
   }

   public boolean hasNullTypeAnnotation(TypeReference.AnnotationPosition position) {
      return TypeReference.containsNullAnnotation(this.annotations) || this.returnType != null && this.returnType.hasNullTypeAnnotation(position);
   }

   @Override
   public boolean isDefaultMethod() {
      return (this.modifiers & 65536) != 0;
   }

   @Override
   public boolean isMethod() {
      return true;
   }

   @Override
   public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
      parser.parse(this, unit);
   }

   @Override
   public StringBuffer printReturnType(int indent, StringBuffer output) {
      return this.returnType == null ? output : this.returnType.printExpression(0, output).append(' ');
   }

   @Override
   public void resolveStatements() {
      if (this.returnType != null && this.binding != null) {
         this.bits |= this.returnType.bits & 1048576;
         this.returnType.resolvedType = this.binding.returnType;
      }

      if (CharOperation.equals(this.scope.enclosingSourceType().sourceName, this.selector)) {
         this.scope.problemReporter().methodWithConstructorName(this);
      }

      boolean returnsUndeclTypeVar = false;
      if (this.returnType != null && this.returnType.resolvedType instanceof TypeVariableBinding) {
         returnsUndeclTypeVar = true;
      }

      if (this.typeParameters != null) {
         int i = 0;

         for(int length = this.typeParameters.length; i < length; ++i) {
            TypeParameter typeParameter = this.typeParameters[i];
            this.bits |= typeParameter.bits & 1048576;
            if (returnsUndeclTypeVar && TypeBinding.equalsEquals(this.typeParameters[i].binding, this.returnType.resolvedType)) {
               returnsUndeclTypeVar = false;
            }
         }
      }

      CompilerOptions compilerOptions = this.scope.compilerOptions();
      if (this.binding != null) {
         long complianceLevel = compilerOptions.complianceLevel;
         if (complianceLevel >= 3211264L) {
            int bindingModifiers = this.binding.modifiers;
            boolean hasOverrideAnnotation = (this.binding.tagBits & 562949953421312L) != 0L;
            boolean hasUnresolvedArguments = (this.binding.tagBits & 512L) != 0L;
            if (hasOverrideAnnotation && !hasUnresolvedArguments) {
               if ((bindingModifiers & 268435464) != 268435456 && (complianceLevel < 3276800L || (bindingModifiers & 536870920) != 536870912)) {
                  this.scope.problemReporter().methodMustOverride(this, complianceLevel);
               }
            } else if (!this.binding.declaringClass.isInterface()) {
               if ((bindingModifiers & 268435464) == 268435456) {
                  this.scope.problemReporter().missingOverrideAnnotation(this);
               } else if (complianceLevel >= 3276800L
                  && compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
                  && this.binding.isImplementing()) {
                  this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
               }
            } else if (complianceLevel >= 3276800L
               && compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
               && ((bindingModifiers & 268435464) == 268435456 || this.binding.isImplementing())) {
               this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
            }
         }
      }

      switch(TypeDeclaration.kind(this.scope.referenceType().modifiers)) {
         case 2:
            if (compilerOptions.sourceLevel >= 3407872L && (this.modifiers & 16778240) == 16777216 && (this.modifiers & 65544) != 0) {
               this.scope.problemReporter().methodNeedBody(this);
            }
            break;
         case 3:
            if (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF) {
               break;
            }
         case 1:
            if ((this.modifiers & 16777216) != 0) {
               if ((this.modifiers & 256) == 0 && (this.modifiers & 1024) == 0) {
                  this.scope.problemReporter().methodNeedBody(this);
               }
            } else if ((this.modifiers & 256) != 0 || (this.modifiers & 1024) != 0) {
               this.scope.problemReporter().methodNeedingNoBody(this);
            } else if (this.binding == null || this.binding.isStatic() || this.binding.declaringClass instanceof LocalTypeBinding || returnsUndeclTypeVar) {
               this.bits &= -257;
            }
      }

      super.resolveStatements();
      if (compilerOptions.getSeverity(537919488) != 256 && this.binding != null) {
         int bindingModifiers = this.binding.modifiers;
         if ((bindingModifiers & 805306368) == 268435456 && (this.bits & 16) == 0) {
            this.scope.problemReporter().overridesMethodWithoutSuperInvocation(this.binding);
         }
      }
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

         if (this.returnType != null) {
            this.returnType.traverse(visitor, this.scope);
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
