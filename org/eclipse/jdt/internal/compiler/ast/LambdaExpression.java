package org.eclipse.jdt.internal.compiler.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.ExceptionInferenceFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class LambdaExpression extends FunctionalExpression implements IPolyExpression, ReferenceContext, ProblemSeverities {
   public Argument[] arguments;
   private TypeBinding[] argumentTypes;
   public int arrowPosition;
   public Statement body;
   public boolean hasParentheses;
   public MethodScope scope;
   boolean voidCompatible = true;
   boolean valueCompatible = false;
   boolean returnsValue;
   private boolean requiresGenericSignature;
   boolean returnsVoid;
   public LambdaExpression original = this;
   public SyntheticArgumentBinding[] outerLocalVariables;
   private int outerLocalVariablesSlotSize;
   private boolean assistNode;
   private boolean hasIgnoredMandatoryErrors;
   private ReferenceBinding classType;
   public int ordinal;
   private Set thrownExceptions;
   public char[] text;
   private static final SyntheticArgumentBinding[] NO_SYNTHETIC_ARGUMENTS = new SyntheticArgumentBinding[0];
   private static final Block NO_BODY = new Block(0);
   private HashMap<TypeBinding, LambdaExpression> copiesPerTargetType;
   protected Expression[] resultExpressions;
   public InferenceContext18 inferenceContext;

   public LambdaExpression(CompilationResult compilationResult, boolean assistNode, boolean requiresGenericSignature) {
      super(compilationResult);
      this.outerLocalVariables = NO_SYNTHETIC_ARGUMENTS;
      this.outerLocalVariablesSlotSize = 0;
      this.assistNode = false;
      this.hasIgnoredMandatoryErrors = false;
      this.resultExpressions = NO_EXPRESSIONS;
      this.assistNode = assistNode;
      this.requiresGenericSignature = requiresGenericSignature;
      this.setArguments(NO_ARGUMENTS);
      this.setBody(NO_BODY);
   }

   public LambdaExpression(CompilationResult compilationResult, boolean assistNode) {
      this(compilationResult, assistNode, false);
   }

   public void setArguments(Argument[] arguments) {
      this.arguments = arguments != null ? arguments : ASTNode.NO_ARGUMENTS;
      this.argumentTypes = new TypeBinding[arguments != null ? arguments.length : 0];
   }

   public Argument[] arguments() {
      return this.arguments;
   }

   public TypeBinding[] argumentTypes() {
      return this.argumentTypes;
   }

   public void setBody(Statement body) {
      this.body = (Statement)(body == null ? NO_BODY : body);
   }

   public Statement body() {
      return this.body;
   }

   public Expression[] resultExpressions() {
      return this.resultExpressions;
   }

   public void setArrowPosition(int arrowPosition) {
      this.arrowPosition = arrowPosition;
   }

   public int arrowPosition() {
      return this.arrowPosition;
   }

   protected FunctionalExpression original() {
      return this.original;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      if (this.shouldCaptureInstance) {
         this.binding.modifiers &= -9;
      } else {
         this.binding.modifiers |= 8;
      }

      SourceTypeBinding sourceType = currentScope.enclosingSourceType();
      boolean firstSpill = !(this.binding instanceof SyntheticMethodBinding);
      this.binding = sourceType.addSyntheticMethod(this);
      int pc = codeStream.position;
      StringBuffer signature = new StringBuffer();
      signature.append('(');
      if (this.shouldCaptureInstance) {
         codeStream.aload_0();
         signature.append(sourceType.signature());
      }

      int i = 0;

      for(int length = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length; i < length; ++i) {
         SyntheticArgumentBinding syntheticArgument = this.outerLocalVariables[i];
         if (this.shouldCaptureInstance && firstSpill) {
            syntheticArgument.resolvedPosition = syntheticArgument.resolvedPosition + 1;
         }

         signature.append(syntheticArgument.type.signature());
         LocalVariableBinding capturedOuterLocal = syntheticArgument.actualOuterLocalVariable;
         VariableBinding[] path = currentScope.getEmulationPath(capturedOuterLocal);
         codeStream.generateOuterAccess(path, this, capturedOuterLocal, currentScope);
      }

      signature.append(')');
      if (this.expectedType instanceof IntersectionTypeBinding18) {
         signature.append(((IntersectionTypeBinding18)this.expectedType).getSAMType(currentScope).signature());
      } else {
         signature.append(this.expectedType.signature());
      }

      i = codeStream.classFile.recordBootstrapMethod(this);
      codeStream.invokeDynamic(
         i, (this.shouldCaptureInstance ? 1 : 0) + this.outerLocalVariablesSlotSize, 1, this.descriptor.selector, signature.toString().toCharArray()
      );
      if (!valueRequired) {
         codeStream.pop();
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public boolean kosherDescriptor(Scope currentScope, MethodBinding sam, boolean shouldChatter) {
      if (sam.typeVariables != Binding.NO_TYPE_VARIABLES) {
         if (shouldChatter) {
            currentScope.problemReporter().lambdaExpressionCannotImplementGenericMethod(this, sam);
         }

         return false;
      } else {
         return super.kosherDescriptor(currentScope, sam, shouldChatter);
      }
   }

   @Override
   public TypeBinding resolveType(BlockScope blockScope, boolean skipKosherCheck) {
      boolean argumentsTypeElided = this.argumentsTypeElided();
      int argumentsLength = this.arguments == null ? 0 : this.arguments.length;
      if (this.constant != Constant.NotAConstant) {
         this.constant = Constant.NotAConstant;
         this.enclosingScope = blockScope;
         if (this.original == this) {
            this.ordinal = this.recordFunctionalType(blockScope);
         }

         if (!argumentsTypeElided) {
            for(int i = 0; i < argumentsLength; ++i) {
               this.argumentTypes[i] = this.arguments[i].type.resolveType(blockScope, true);
            }
         }

         if (this.expectedType == null && this.expressionContext == ExpressionContext.INVOCATION_CONTEXT) {
            return new PolyTypeBinding(this);
         }
      }

      MethodScope methodScope = blockScope.methodScope();
      this.scope = new MethodScope(blockScope, this, methodScope.isStatic, methodScope.lastVisibleFieldID);
      this.scope.isConstructorCall = methodScope.isConstructorCall;
      super.resolveType(blockScope, skipKosherCheck);
      boolean haveDescriptor = this.descriptor != null;
      if (skipKosherCheck || haveDescriptor && this.descriptor.typeVariables == Binding.NO_TYPE_VARIABLES) {
         this.binding = new MethodBinding(
            33558530,
            CharOperation.concat(TypeConstants.ANONYMOUS_METHOD, Integer.toString(this.ordinal).toCharArray()),
            (TypeBinding)(haveDescriptor ? this.descriptor.returnType : TypeBinding.VOID),
            Binding.NO_PARAMETERS,
            haveDescriptor ? this.descriptor.thrownExceptions : Binding.NO_EXCEPTIONS,
            blockScope.enclosingSourceType()
         );
         this.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
         boolean argumentsHaveErrors = false;
         if (haveDescriptor) {
            int parametersLength = this.descriptor.parameters.length;
            if (parametersLength != argumentsLength) {
               this.scope.problemReporter().lambdaSignatureMismatched(this);
               if (argumentsTypeElided || this.original != this) {
                  return this.resolvedType = null;
               }

               this.resolvedType = null;
               argumentsHaveErrors = true;
            }
         }

         TypeBinding[] newParameters = new TypeBinding[argumentsLength];
         AnnotationBinding[][] parameterAnnotations = null;

         for(int i = 0; i < argumentsLength; ++i) {
            Argument argument = this.arguments[i];
            if (argument.isVarArgs()) {
               if (i == argumentsLength - 1) {
                  this.binding.modifiers |= 128;
               } else {
                  this.scope.problemReporter().illegalVarargInLambda(argument);
                  argumentsHaveErrors = true;
               }
            }

            TypeBinding expectedParameterType = haveDescriptor && i < this.descriptor.parameters.length ? this.descriptor.parameters[i] : null;
            TypeBinding argumentType = argumentsTypeElided ? expectedParameterType : this.argumentTypes[i];
            if (argumentType == null) {
               argumentsHaveErrors = true;
            } else if (argumentType == TypeBinding.VOID) {
               this.scope.problemReporter().argumentTypeCannotBeVoid(this, argument);
               argumentsHaveErrors = true;
            } else {
               if (!argumentType.isValidBinding()) {
                  this.binding.tagBits |= 512L;
               }

               if ((argumentType.tagBits & 128L) != 0L) {
                  this.binding.tagBits |= 128L;
               }
            }
         }

         if (!argumentsTypeElided && !argumentsHaveErrors) {
            ReferenceBinding groundType = null;
            ReferenceBinding expectedSAMType = null;
            if (this.expectedType instanceof IntersectionTypeBinding18) {
               expectedSAMType = (ReferenceBinding)((IntersectionTypeBinding18)this.expectedType).getSAMType(blockScope);
            } else if (this.expectedType instanceof ReferenceBinding) {
               expectedSAMType = (ReferenceBinding)this.expectedType;
            }

            if (expectedSAMType != null) {
               groundType = this.findGroundTargetType(blockScope, expectedSAMType, argumentsTypeElided);
            }

            if (groundType != null) {
               this.descriptor = groundType.getSingleAbstractMethod(blockScope, true);
               if (!this.descriptor.isValidBinding()) {
                  this.reportSamProblem(blockScope, this.descriptor);
               } else {
                  if (groundType != expectedSAMType && !groundType.isCompatibleWith(expectedSAMType, this.scope)) {
                     blockScope.problemReporter().typeMismatchError(groundType, this.expectedType, this, null);
                     return this.resolvedType = null;
                  }

                  this.resolvedType = groundType;
               }
            }
         }

         boolean genericSignatureNeeded = this.requiresGenericSignature || blockScope.compilerOptions().generateGenericSignatureForLambdaExpressions;

         for(int i = 0; i < argumentsLength; ++i) {
            Argument argument = this.arguments[i];
            TypeBinding expectedParameterType = haveDescriptor && i < this.descriptor.parameters.length ? this.descriptor.parameters[i] : null;
            TypeBinding argumentType = argumentsTypeElided ? expectedParameterType : this.argumentTypes[i];
            if (argumentType != null && argumentType != TypeBinding.VOID) {
               if (haveDescriptor
                  && expectedParameterType != null
                  && argumentType.isValidBinding()
                  && TypeBinding.notEquals(argumentType, expectedParameterType)
                  && expectedParameterType.isProperType(true)) {
                  this.scope.problemReporter().lambdaParameterTypeMismatched(argument, argument.type, expectedParameterType);
                  this.resolvedType = null;
               }

               if (genericSignatureNeeded) {
                  TypeBinding leafType = argumentType.leafComponentType();
                  if (leafType instanceof ReferenceBinding && (((ReferenceBinding)leafType).modifiers & 1073741824) != 0) {
                     this.binding.modifiers |= 1073741824;
                  }
               }

               newParameters[i] = argument.bind(this.scope, argumentType, false);
               if (argument.annotations == null) {
                  if (parameterAnnotations != null) {
                     parameterAnnotations[i] = Binding.NO_ANNOTATIONS;
                  }
               } else {
                  this.binding.tagBits |= 1024L;
                  if (parameterAnnotations == null) {
                     parameterAnnotations = new AnnotationBinding[argumentsLength][];

                     for(int j = 0; j < i; ++j) {
                        parameterAnnotations[j] = Binding.NO_ANNOTATIONS;
                     }
                  }

                  parameterAnnotations[i] = argument.binding.getAnnotations();
               }
            }
         }

         if (!argumentsHaveErrors) {
            this.binding.parameters = newParameters;
            if (parameterAnnotations != null) {
               this.binding.setParameterAnnotations(parameterAnnotations);
            }
         }

         if (!argumentsTypeElided
            && !argumentsHaveErrors
            && this.binding.isVarargs()
            && !this.binding.parameters[this.binding.parameters.length - 1].isReifiable()) {
            this.scope.problemReporter().possibleHeapPollutionFromVararg(this.arguments[this.arguments.length - 1]);
         }

         for(ReferenceBinding exception : this.binding.thrownExceptions) {
            if ((exception.tagBits & 128L) != 0L) {
               this.binding.tagBits |= 128L;
            }

            if (genericSignatureNeeded) {
               this.binding.modifiers |= exception.modifiers & 1073741824;
            }
         }

         TypeBinding returnType = this.binding.returnType;
         if (returnType != null) {
            if ((returnType.tagBits & 128L) != 0L) {
               this.binding.tagBits |= 128L;
            }

            if (genericSignatureNeeded) {
               TypeBinding leafType = returnType.leafComponentType();
               if (leafType instanceof ReferenceBinding && (((ReferenceBinding)leafType).modifiers & 1073741824) != 0) {
                  this.binding.modifiers |= 1073741824;
               }
            }
         }

         if (haveDescriptor && !argumentsHaveErrors && blockScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled) {
            if (!argumentsTypeElided) {
               AbstractMethodDeclaration.createArgumentBindings(this.arguments, this.binding, this.scope);
               this.mergeParameterNullAnnotations(blockScope);
            }

            this.binding.tagBits |= this.descriptor.tagBits & 108086391056891904L;
         }

         this.binding.modifiers &= -33554433;
         if (this.body instanceof Expression) {
            Expression expression = (Expression)this.body;
            new ReturnStatement(expression, expression.sourceStart, expression.sourceEnd, true).resolve(this.scope);
            if (expression.resolvedType == TypeBinding.VOID && !expression.statementExpression()) {
               this.scope.problemReporter().invalidExpressionAsStatement(expression);
            }
         } else {
            this.body.resolve(this.scope);
            if (!this.returnsVoid && !this.returnsValue) {
               this.valueCompatible = this.body.doesNotCompleteNormally();
            }
         }

         if ((this.binding.tagBits & 128L) != 0L) {
            this.scope.problemReporter().missingTypeInLambda(this, this.binding);
         }

         if (this.shouldCaptureInstance && this.scope.isConstructorCall) {
            this.scope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
         }

         return argumentsHaveErrors ? (this.resolvedType = null) : this.resolvedType;
      } else {
         return this.resolvedType = null;
      }
   }

   private ReferenceBinding findGroundTargetType(BlockScope blockScope, TypeBinding targetType, boolean argumentTypesElided) {
      if (targetType instanceof IntersectionTypeBinding18) {
         targetType = ((IntersectionTypeBinding18)targetType).getSAMType(blockScope);
      }

      if (targetType instanceof ReferenceBinding && targetType.isValidBinding()) {
         ParameterizedTypeBinding withWildCards = InferenceContext18.parameterizedWithWildcard(targetType);
         if (withWildCards == null) {
            return (ReferenceBinding)targetType;
         } else if (!argumentTypesElided) {
            InferenceContext18 freshInferenceContext = new InferenceContext18(blockScope);

            ReferenceBinding var7;
            try {
               var7 = freshInferenceContext.inferFunctionalInterfaceParameterization(this, blockScope, withWildCards);
            } finally {
               freshInferenceContext.cleanUp();
            }

            return var7;
         } else {
            return this.findGroundTargetTypeForElidedLambda(blockScope, withWildCards);
         }
      } else {
         return null;
      }
   }

   public ReferenceBinding findGroundTargetTypeForElidedLambda(BlockScope blockScope, ParameterizedTypeBinding withWildCards) {
      TypeBinding[] types = withWildCards.getNonWildcardParameterization(blockScope);
      if (types == null) {
         return null;
      } else {
         ReferenceBinding genericType = withWildCards.genericType();
         return blockScope.environment().createParameterizedType(genericType, types, withWildCards.enclosingType());
      }
   }

   @Override
   public boolean argumentsTypeElided() {
      return this.arguments.length > 0 && this.arguments[0].hasElidedType();
   }

   private void analyzeExceptions() {
      try {
         ExceptionHandlingFlowContext ehfc;
         this.body
            .analyseCode(
               this.scope,
               ehfc = new ExceptionInferenceFlowContext(null, this, Binding.NO_EXCEPTIONS, null, this.scope, FlowInfo.DEAD_END),
               UnconditionalFlowInfo.fakeInitializedFlowInfo(this.scope.outerMostMethodScope().analysisIndex, this.scope.referenceType().maxFieldCount)
            );
         this.thrownExceptions = (Set)(ehfc.extendedExceptions == null ? Collections.emptySet() : new HashSet(ehfc.extendedExceptions));
      } catch (Exception var2) {
      }
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.ignoreFurtherInvestigation) {
         return flowInfo;
      } else {
         FlowInfo lambdaInfo = flowInfo.copy();
         ExceptionHandlingFlowContext methodContext = new ExceptionHandlingFlowContext(
            flowContext, this, this.binding.thrownExceptions, flowContext.getInitializationContext(), this.scope, FlowInfo.DEAD_END
         );
         MethodBinding methodWithParameterDeclaration = this.argumentsTypeElided() ? this.descriptor : this.binding;
         AbstractMethodDeclaration.analyseArguments(currentScope.environment(), lambdaInfo, this.arguments, methodWithParameterDeclaration);
         if (this.arguments != null) {
            int i = 0;

            for(int count = this.arguments.length; i < count; ++i) {
               this.bits |= this.arguments[i].bits & 1048576;
            }
         }

         lambdaInfo = this.body.analyseCode(this.scope, methodContext, lambdaInfo);
         if (this.body instanceof Block) {
            TypeBinding returnTypeBinding = this.expectedResultType();
            if (returnTypeBinding == TypeBinding.VOID) {
               if ((lambdaInfo.tagBits & 1) == 0 || ((Block)this.body).statements == null) {
                  this.bits |= 64;
               }
            } else if (lambdaInfo != FlowInfo.DEAD_END) {
               this.scope.problemReporter().shouldReturn(returnTypeBinding, this);
            }
         } else if (currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled && lambdaInfo.reachMode() == 0) {
            Expression expression = (Expression)this.body;
            this.checkAgainstNullAnnotation(flowContext, expression, flowInfo, expression.nullStatus(lambdaInfo, flowContext));
         }

         return flowInfo;
      }
   }

   void validateNullAnnotations() {
      if (this.binding != null) {
         int length = this.binding.parameters.length;

         for(int i = 0; i < length; ++i) {
            if (!this.scope.validateNullAnnotation(this.binding.returnType.tagBits, this.arguments[i].type, this.arguments[i].annotations)) {
               this.binding.returnType = this.binding.returnType.withoutToplevelNullAnnotation();
            }
         }
      }
   }

   private void mergeParameterNullAnnotations(BlockScope currentScope) {
      LookupEnvironment env = currentScope.environment();
      TypeBinding[] ourParameters = this.binding.parameters;
      TypeBinding[] descParameters = this.descriptor.parameters;
      int len = Math.min(ourParameters.length, descParameters.length);

      for(int i = 0; i < len; ++i) {
         long ourTagBits = ourParameters[i].tagBits & 108086391056891904L;
         long descTagBits = descParameters[i].tagBits & 108086391056891904L;
         if (ourTagBits == 0L) {
            if (descTagBits != 0L && !ourParameters[i].isBaseType()) {
               AnnotationBinding[] annotations = descParameters[i].getTypeAnnotations();
               int j = 0;

               for(int length = annotations.length; j < length; ++j) {
                  AnnotationBinding annotation = annotations[j];
                  if (annotation != null && annotation.getAnnotationType().hasNullBit(96)) {
                     ourParameters[i] = env.createAnnotatedType(ourParameters[i], new AnnotationBinding[]{annotation});
                  }
               }
            }
         } else if (ourTagBits != descTagBits && ourTagBits == 72057594037927936L) {
            char[][] inheritedAnnotationName = null;
            if (descTagBits == 36028797018963968L) {
               inheritedAnnotationName = env.getNullableAnnotationName();
            }

            currentScope.problemReporter().illegalRedefinitionToNonNullParameter(this.arguments[i], this.descriptor.declaringClass, inheritedAnnotationName);
         }
      }
   }

   void checkAgainstNullAnnotation(FlowContext flowContext, Expression expression, FlowInfo flowInfo, int nullStatus) {
      if (nullStatus != 4 && (this.descriptor.returnType.tagBits & 72057594037927936L) != 0L) {
         flowContext.recordNullityMismatch(this.scope, expression, expression.resolvedType, this.descriptor.returnType, flowInfo, nullStatus, null);
      }
   }

   @Override
   public boolean isPertinentToApplicability(final TypeBinding targetType, final MethodBinding method) {
      if (targetType == null) {
         return true;
      } else if (this.argumentsTypeElided()) {
         return false;
      } else if (!super.isPertinentToApplicability(targetType, method)) {
         return false;
      } else {
         if (this.body instanceof Expression) {
            if (!((Expression)this.body).isPertinentToApplicability(targetType, method)) {
               return false;
            }
         } else {
            Expression[] returnExpressions = this.resultExpressions;
            if (returnExpressions != NO_EXPRESSIONS) {
               int i = 0;

               for(int length = returnExpressions.length; i < length; ++i) {
                  if (!returnExpressions[i].isPertinentToApplicability(targetType, method)) {
                     return false;
                  }
               }
            } else {
               class NotPertientToApplicability extends RuntimeException {
                  private static final long serialVersionUID = 1L;
               }

               try {
                  class ResultsAnalyser extends ASTVisitor {
                     @Override
                     public boolean visit(TypeDeclaration type, BlockScope skope) {
                        return false;
                     }

                     @Override
                     public boolean visit(TypeDeclaration type, ClassScope skope) {
                        return false;
                     }

                     @Override
                     public boolean visit(LambdaExpression type, BlockScope skope) {
                        return false;
                     }

                     @Override
                     public boolean visit(ReturnStatement returnStatement, BlockScope skope) {
                        if (returnStatement.expression != null && !returnStatement.expression.isPertinentToApplicability(targetType, method)) {
                           throw new NotPertientToApplicability();
                        } else {
                           return false;
                        }
                     }
                  }

                  this.body.traverse(new ResultsAnalyser(), this.scope);
               } catch (NotPertientToApplicability var6) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean isVoidCompatible() {
      return this.voidCompatible;
   }

   public boolean isValueCompatible() {
      return this.valueCompatible;
   }

   @Override
   public StringBuffer printExpression(int tab, StringBuffer output) {
      return this.printExpression(tab, output, false);
   }

   public StringBuffer printExpression(int tab, StringBuffer output, boolean makeShort) {
      int parenthesesCount = (this.bits & 534773760) >> 21;
      String suffix = "";

      for(int i = 0; i < parenthesesCount; ++i) {
         output.append('(');
         suffix = suffix + ')';
      }

      output.append('(');
      if (this.arguments != null) {
         for(int i = 0; i < this.arguments.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.arguments[i].print(0, output);
         }
      }

      output.append(") -> ");
      if (makeShort) {
         output.append("{}");
      } else if (this.body != null) {
         this.body.print(this.body instanceof Block ? tab : 0, output);
      } else {
         output.append("<@incubator>");
      }

      return output.append(suffix);
   }

   public TypeBinding expectedResultType() {
      return this.descriptor != null && this.descriptor.isValidBinding() ? this.descriptor.returnType : null;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         if (this.arguments != null) {
            int argumentsLength = this.arguments.length;

            for(int i = 0; i < argumentsLength; ++i) {
               this.arguments[i].traverse(visitor, this.scope);
            }
         }

         if (this.body != null) {
            this.body.traverse(visitor, this.scope);
         }
      }

      visitor.endVisit(this, blockScope);
   }

   public MethodScope getScope() {
      return this.scope;
   }

   private boolean enclosingScopesHaveErrors() {
      for(Scope skope = this.enclosingScope; skope != null; skope = skope.parent) {
         ReferenceContext context = skope.referenceContext();
         if (context != null && context.hasErrors()) {
            return true;
         }
      }

      return false;
   }

   private void analyzeShape() {
      if (this.body instanceof Expression) {
         this.voidCompatible = this.assistNode ? true : ((Expression)this.body).statementExpression();
         this.valueCompatible = true;
      } else {
         if (this.assistNode) {
            this.voidCompatible = true;
            this.valueCompatible = true;
         }

         class ShapeComputer extends ASTVisitor {
            @Override
            public boolean visit(TypeDeclaration type, BlockScope skope) {
               return false;
            }

            @Override
            public boolean visit(TypeDeclaration type, ClassScope skope) {
               return false;
            }

            @Override
            public boolean visit(LambdaExpression type, BlockScope skope) {
               return false;
            }

            @Override
            public boolean visit(ReturnStatement returnStatement, BlockScope skope) {
               if (returnStatement.expression != null) {
                  LambdaExpression.this.valueCompatible = true;
                  LambdaExpression.this.voidCompatible = false;
                  LambdaExpression.this.returnsValue = true;
               } else {
                  LambdaExpression.this.voidCompatible = true;
                  LambdaExpression.this.valueCompatible = false;
                  LambdaExpression.this.returnsVoid = true;
               }

               return false;
            }
         }

         this.body.traverse(new ShapeComputer(), null);
         if (!this.returnsValue && !this.returnsVoid) {
            this.valueCompatible = this.body.doesNotCompleteNormally();
         }
      }
   }

   @Override
   public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope skope) {
      if (!super.isPertinentToApplicability(targetType, null)) {
         return true;
      } else {
         MethodBinding sam = targetType.getSingleAbstractMethod(skope, true);
         if (sam != null && sam.isValidBinding()) {
            if (sam.parameters.length != this.arguments.length) {
               return false;
            } else {
               this.analyzeShape();
               if (sam.returnType.id == 6) {
                  if (!this.voidCompatible) {
                     return false;
                  }
               } else if (!this.valueCompatible) {
                  return false;
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean isCompatibleWith(TypeBinding targetType, Scope skope) {
      if (!super.isPertinentToApplicability(targetType, null)) {
         return true;
      } else {
         LambdaExpression copy = null;

         try {
            copy = this.cachedResolvedCopy(targetType, this.argumentsTypeElided(), false, null);
         } catch (LambdaExpression.CopyFailureException var8) {
            if (this.assistNode) {
               return true;
            }

            return !this.isPertinentToApplicability(targetType, null);
         }

         if (copy == null) {
            return false;
         } else {
            TypeBinding var9 = this.findGroundTargetType(this.enclosingScope, targetType, this.argumentsTypeElided());
            MethodBinding sam = var9.getSingleAbstractMethod(this.enclosingScope, true);
            if (sam != null && sam.problemId() != 17) {
               if (sam.returnType.id == 6) {
                  if (!copy.voidCompatible) {
                     return false;
                  }
               } else if (!copy.valueCompatible) {
                  return false;
               }

               if (!this.isPertinentToApplicability(var9, null)) {
                  return true;
               } else if (!this.kosherDescriptor(this.enclosingScope, sam, false)) {
                  return false;
               } else {
                  Expression[] returnExpressions = copy.resultExpressions;
                  int i = 0;

                  for(int length = returnExpressions.length; i < length; ++i) {
                     if (this.enclosingScope.parameterCompatibilityLevel(returnExpressions[i].resolvedType, sam.returnType) == -1
                        && !returnExpressions[i].isConstantValueOfTypeAssignableToType(returnExpressions[i].resolvedType, sam.returnType)
                        && (sam.returnType.id != 6 || this.body instanceof Block)) {
                        return false;
                     }
                  }

                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   private LambdaExpression cachedResolvedCopy(TypeBinding targetType, boolean anyTargetOk, boolean requireExceptionAnalysis, InferenceContext18 context) {
      TypeBinding var13 = this.findGroundTargetType(this.enclosingScope, targetType, this.argumentsTypeElided());
      if (var13 == null) {
         return null;
      } else {
         MethodBinding sam = var13.getSingleAbstractMethod(this.enclosingScope, true);
         if (sam != null && sam.isValidBinding()) {
            if (sam.parameters.length != this.arguments.length) {
               return null;
            } else {
               LambdaExpression copy = null;
               if (this.copiesPerTargetType != null) {
                  copy = this.copiesPerTargetType.get(var13);
                  if (copy == null && anyTargetOk && this.copiesPerTargetType.values().size() > 0) {
                     copy = this.copiesPerTargetType.values().iterator().next();
                  }
               }

               IErrorHandlingPolicy oldPolicy = this.enclosingScope.problemReporter().switchErrorHandlingPolicy(silentErrorHandlingPolicy);

               try {
                  if (copy == null) {
                     copy = this.copy();
                     if (copy == null) {
                        throw new LambdaExpression.CopyFailureException();
                     }

                     copy.setExpressionContext(this.expressionContext);
                     copy.setExpectedType(var13);
                     copy.inferenceContext = context;
                     TypeBinding type = copy.resolveType(this.enclosingScope, true);
                     if (type == null || !type.isValidBinding()) {
                        return null;
                     }

                     if (this.copiesPerTargetType == null) {
                        this.copiesPerTargetType = new HashMap<>();
                     }

                     this.copiesPerTargetType.put(var13, copy);
                  }

                  if (!requireExceptionAnalysis) {
                     return copy;
                  } else {
                     if (copy.thrownExceptions == null && !copy.hasIgnoredMandatoryErrors && !this.enclosingScopesHaveErrors()) {
                        copy.analyzeExceptions();
                     }

                     return copy;
                  }
               } finally {
                  this.enclosingScope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
               }
            }
         } else {
            return null;
         }
      }
   }

   public LambdaExpression resolveExpressionExpecting(TypeBinding targetType, Scope skope, InferenceContext18 context) {
      LambdaExpression copy = null;

      try {
         return this.cachedResolvedCopy(targetType, false, true, context);
      } catch (LambdaExpression.CopyFailureException var5) {
         return null;
      }
   }

   @Override
   public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope skope) {
      if (super.sIsMoreSpecific(s, t, skope)) {
         return true;
      } else if (!this.argumentsTypeElided() && t.findSuperTypeOriginatingFrom(s) == null) {
         s = s.capture(this.enclosingScope, this.sourceStart, this.sourceEnd);
         MethodBinding sSam = s.getSingleAbstractMethod(this.enclosingScope, true);
         if (sSam != null && sSam.isValidBinding()) {
            TypeBinding r1 = sSam.returnType;
            MethodBinding tSam = t.getSingleAbstractMethod(this.enclosingScope, true);
            if (tSam != null && tSam.isValidBinding()) {
               TypeBinding r2 = tSam.returnType;
               if (r2.id == 6) {
                  return true;
               } else if (r1.id == 6) {
                  return false;
               } else if (r1.isCompatibleWith(r2, skope)) {
                  return true;
               } else {
                  LambdaExpression copy = this.cachedResolvedCopy(s, true, false, null);
                  Expression[] returnExpressions = copy.resultExpressions;
                  int returnExpressionsLength = returnExpressions == null ? 0 : returnExpressions.length;
                  if (returnExpressionsLength > 0) {
                     if (r1.isBaseType() && !r2.isBaseType()) {
                        int i = 0;

                        while(i < returnExpressionsLength && !returnExpressions[i].isPolyExpression() && returnExpressions[i].resolvedType.isBaseType()) {
                           ++i;
                        }

                        if (i == returnExpressionsLength) {
                           return true;
                        }
                     }

                     if (!r1.isBaseType() && r2.isBaseType()) {
                        int i = 0;

                        while(i < returnExpressionsLength && !returnExpressions[i].resolvedType.isBaseType()) {
                           ++i;
                        }

                        if (i == returnExpressionsLength) {
                           return true;
                        }
                     }

                     if (r1.isFunctionalInterface(this.enclosingScope) && r2.isFunctionalInterface(this.enclosingScope)) {
                        int i;
                        for(i = 0; i < returnExpressionsLength; ++i) {
                           Expression resultExpression = returnExpressions[i];
                           if (!resultExpression.sIsMoreSpecific(r1, r2, skope)) {
                              break;
                           }
                        }

                        if (i == returnExpressionsLength) {
                           return true;
                        }
                     }
                  }

                  return false;
               }
            } else {
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   LambdaExpression copy() {
      Parser parser = new Parser(this.enclosingScope.problemReporter(), false);
      ICompilationUnit compilationUnit = this.compilationResult.getCompilationUnit();
      char[] source = compilationUnit != null ? compilationUnit.getContents() : this.text;
      LambdaExpression copy = (LambdaExpression)parser.parseLambdaExpression(
         source, compilationUnit != null ? this.sourceStart : 0, this.sourceEnd - this.sourceStart + 1, this.enclosingScope.referenceCompilationUnit(), false
      );
      if (copy != null) {
         copy.original = this;
         copy.assistNode = this.assistNode;
         copy.enclosingScope = this.enclosingScope;
      }

      return copy;
   }

   public void returnsExpression(Expression expression, TypeBinding resultType) {
      if (this.original != this) {
         if (!(this.body instanceof Expression)) {
            if (expression != null) {
               this.returnsValue = true;
               this.voidCompatible = false;
               this.valueCompatible = !this.returnsVoid;
               Expression[] returnExpressions = this.resultExpressions;
               int resultsLength = returnExpressions.length;
               Expression[] var5;
               System.arraycopy(returnExpressions, 0, var5 = new Expression[resultsLength + 1], 0, resultsLength);
               var5[resultsLength] = expression;
               this.resultExpressions = var5;
            } else {
               this.returnsVoid = true;
               this.valueCompatible = false;
               this.voidCompatible = !this.returnsValue;
            }
         } else {
            this.valueCompatible = resultType == null || resultType.id != 6;
            this.voidCompatible = this.assistNode ? true : ((Expression)this.body).statementExpression();
            this.resultExpressions = new Expression[]{expression};
         }
      }
   }

   @Override
   public CompilationResult compilationResult() {
      return this.compilationResult;
   }

   @Override
   public void abort(int abortLevel, CategorizedProblem problem) {
      switch(abortLevel) {
         case 2:
            throw new AbortCompilation(this.compilationResult, problem);
         case 3:
         case 5:
         case 6:
         case 7:
         default:
            throw new AbortMethod(this.compilationResult, problem);
         case 4:
            throw new AbortCompilationUnit(this.compilationResult, problem);
         case 8:
            throw new AbortType(this.compilationResult, problem);
      }
   }

   @Override
   public CompilationUnitDeclaration getCompilationUnitDeclaration() {
      return this.enclosingScope == null ? null : this.enclosingScope.compilationUnitScope().referenceContext;
   }

   @Override
   public boolean hasErrors() {
      return this.ignoreFurtherInvestigation;
   }

   @Override
   public void tagAsHavingErrors() {
      this.ignoreFurtherInvestigation = true;
      Scope parent = this.enclosingScope.parent;

      while(parent != null) {
         switch(parent.kind) {
            case 2:
            case 3:
               ReferenceContext parentAST = parent.referenceContext();
               if (parentAST != this) {
                  parentAST.tagAsHavingErrors();
                  return;
               }
            default:
               parent = parent.parent;
         }
      }
   }

   @Override
   public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
      switch(problemId) {
         case 99:
         case 16777235:
         case 67108969:
         case 67109635:
         case 553648781:
         case 553648783:
         case 553648784:
         case 553648785:
         case 553648786:
         case 553648787:
         case 603979884:
            return;
         case 16777362:
         case 16777384:
         case 16778098:
            return;
         default:
            this.hasIgnoredMandatoryErrors = true;

            for(MethodScope enclosingLambdaScope = this.scope == null ? null : this.scope.enclosingLambdaScope();
               enclosingLambdaScope != null;
               enclosingLambdaScope = enclosingLambdaScope.enclosingLambdaScope()
            ) {
               LambdaExpression enclosingLambda = (LambdaExpression)enclosingLambdaScope.referenceContext;
               enclosingLambda.hasIgnoredMandatoryErrors = true;
            }
      }
   }

   public Set<TypeBinding> getThrownExceptions() {
      return this.thrownExceptions == null ? Collections.emptySet() : this.thrownExceptions;
   }

   public void generateCode(ClassScope classScope, ClassFile classFile) {
      int problemResetPC = 0;
      classFile.codeStream.wideMode = false;
      boolean restart = false;

      do {
         try {
            problemResetPC = classFile.contentsOffset;
            this.generateCode(classFile);
            restart = false;
         } catch (AbortMethod var6) {
            if (var6.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
               classFile.contentsOffset = problemResetPC;
               --classFile.methodCount;
               classFile.codeStream.resetInWideMode();
               restart = true;
            } else {
               if (var6.compilationResult != CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
                  throw new AbortType(this.compilationResult, var6.problem);
               }

               classFile.contentsOffset = problemResetPC;
               --classFile.methodCount;
               classFile.codeStream.resetForCodeGenUnusedLocals();
               restart = true;
            }
         }
      } while(restart);
   }

   public void generateCode(ClassFile classFile) {
      classFile.generateMethodInfoHeader(this.binding);
      int methodAttributeOffset = classFile.contentsOffset;
      int attributeNumber = classFile.generateMethodInfoAttributes(this.binding);
      int codeAttributeOffset = classFile.contentsOffset;
      classFile.generateCodeAttributeHeader();
      CodeStream codeStream = classFile.codeStream;
      codeStream.reset(this, classFile);
      this.scope.computeLocalVariablePositions(this.outerLocalVariablesSlotSize + (this.binding.isStatic() ? 0 : 1), codeStream);
      if (this.outerLocalVariables != null) {
         int i = 0;

         for(int max = this.outerLocalVariables.length; i < max; ++i) {
            LocalVariableBinding argBinding;
            codeStream.addVisibleLocalVariable(argBinding = this.outerLocalVariables[i]);
            codeStream.record(argBinding);
            argBinding.recordInitializationStartPC(0);
         }
      }

      if (this.arguments != null) {
         int i = 0;

         for(int max = this.arguments.length; i < max; ++i) {
            LocalVariableBinding argBinding = this.arguments[i].binding;
            codeStream.addVisibleLocalVariable(this.arguments[i].binding);
            argBinding.recordInitializationStartPC(0);
         }
      }

      if (this.body instanceof Block) {
         this.body.generateCode(this.scope, codeStream);
         if ((this.bits & 64) != 0) {
            codeStream.return_();
         }
      } else {
         Expression expression = (Expression)this.body;
         expression.generateCode(this.scope, codeStream, true);
         if (this.binding.returnType == TypeBinding.VOID) {
            codeStream.return_();
         } else {
            codeStream.generateReturnBytecode(expression);
         }
      }

      codeStream.exitUserScope(this.scope);
      codeStream.recordPositionsFrom(0, this.sourceEnd);

      try {
         classFile.completeCodeAttribute(codeAttributeOffset);
      } catch (NegativeArraySizeException var9) {
         throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
      }

      classFile.completeMethodInfo(this.binding, methodAttributeOffset, ++attributeNumber);
   }

   public void addSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
      if (this.original == this && this.binding != null) {
         SyntheticArgumentBinding syntheticLocal = null;
         int newSlot = this.outerLocalVariables.length;

         for(int i = 0; i < newSlot; ++i) {
            if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable) {
               return;
            }
         }

         System.arraycopy(this.outerLocalVariables, 0, this.outerLocalVariables = new SyntheticArgumentBinding[newSlot + 1], 0, newSlot);
         this.outerLocalVariables[newSlot] = syntheticLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
         syntheticLocal.resolvedPosition = this.outerLocalVariablesSlotSize;
         syntheticLocal.declaringScope = this.scope;
         int parameterCount = this.binding.parameters.length;
         TypeBinding[] newParameters = new TypeBinding[parameterCount + 1];
         newParameters[newSlot] = actualOuterLocalVariable.type;
         int i = 0;

         for(int j = 0; i < parameterCount; ++j) {
            if (i == newSlot) {
               ++j;
            }

            newParameters[j] = this.binding.parameters[i];
            ++i;
         }

         this.binding.parameters = newParameters;
         switch(syntheticLocal.type.id) {
            case 7:
            case 8:
               this.outerLocalVariablesSlotSize += 2;
               break;
            default:
               ++this.outerLocalVariablesSlotSize;
         }
      }
   }

   public SyntheticArgumentBinding getSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
      int i = 0;

      for(int length = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length; i < length; ++i) {
         if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable) {
            return this.outerLocalVariables[i];
         }
      }

      return null;
   }

   @Override
   public MethodBinding getMethodBinding() {
      if (this.actualMethodBinding == null) {
         if (this.binding != null) {
            TypeBinding[] newParams = null;
            if (this.binding instanceof SyntheticMethodBinding && this.outerLocalVariables.length > 0) {
               newParams = new TypeBinding[this.binding.parameters.length - this.outerLocalVariables.length];
               System.arraycopy(this.binding.parameters, this.outerLocalVariables.length, newParams, 0, newParams.length);
            } else {
               newParams = this.binding.parameters;
            }

            this.actualMethodBinding = new MethodBinding(
               this.binding.modifiers, this.binding.selector, this.binding.returnType, newParams, this.binding.thrownExceptions, this.binding.declaringClass
            );
            this.actualMethodBinding.tagBits = this.binding.tagBits;
         } else {
            this.actualMethodBinding = new ProblemMethodBinding(CharOperation.NO_CHAR, null, 17);
         }
      }

      return this.actualMethodBinding;
   }

   @Override
   public int diagnosticsSourceEnd() {
      return this.body instanceof Block ? this.arrowPosition : this.sourceEnd;
   }

   public TypeBinding[] getMarkerInterfaces() {
      if (this.expectedType instanceof IntersectionTypeBinding18) {
         Set markerBindings = new LinkedHashSet();
         IntersectionTypeBinding18 intersectionType = (IntersectionTypeBinding18)this.expectedType;
         TypeBinding[] intersectionTypes = intersectionType.intersectingTypes;
         TypeBinding samType = intersectionType.getSAMType(this.enclosingScope);
         int i = 0;

         for(int max = intersectionTypes.length; i < max; ++i) {
            TypeBinding typeBinding = intersectionTypes[i];
            if (typeBinding.isInterface() && !TypeBinding.equalsEquals(samType, typeBinding) && typeBinding.id != 37) {
               markerBindings.add(typeBinding);
            }
         }

         if (markerBindings.size() > 0) {
            return markerBindings.toArray(new TypeBinding[markerBindings.size()]);
         }
      }

      return null;
   }

   public ReferenceBinding getTypeBinding() {
      class LambdaTypeBinding extends ReferenceBinding {
         @Override
         public MethodBinding[] methods() {
            return new MethodBinding[]{LambdaExpression.this.getMethodBinding()};
         }

         @Override
         public char[] sourceName() {
            return TypeConstants.LAMBDA_TYPE;
         }

         @Override
         public ReferenceBinding superclass() {
            return LambdaExpression.this.scope.getJavaLangObject();
         }

         @Override
         public ReferenceBinding[] superInterfaces() {
            return new ReferenceBinding[]{(ReferenceBinding)LambdaExpression.this.resolvedType};
         }

         @Override
         public char[] computeUniqueKey() {
            return LambdaExpression.this.descriptor.declaringClass.computeUniqueKey();
         }

         @Override
         public String toString() {
            StringBuffer output = new StringBuffer("()->{} implements ");
            output.append(LambdaExpression.this.descriptor.declaringClass.sourceName());
            output.append('.');
            output.append(LambdaExpression.this.descriptor.toString());
            return output.toString();
         }
      }

      return this.classType == null && this.resolvedType != null ? (this.classType = new LambdaTypeBinding()) : null;
   }

   class CopyFailureException extends RuntimeException {
      private static final long serialVersionUID = 1L;
   }
}
