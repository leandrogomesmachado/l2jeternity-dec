package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

public class FieldDeclaration extends AbstractVariableDeclaration {
   public FieldBinding binding;
   public Javadoc javadoc;
   public int endPart1Position;
   public int endPart2Position;

   public FieldDeclaration() {
   }

   public FieldDeclaration(char[] name, int sourceStart, int sourceEnd) {
      this.name = name;
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
   }

   public FlowInfo analyseCode(MethodScope initializationScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.binding != null
         && !this.binding.isUsed()
         && this.binding.isOrEnclosedByPrivateType()
         && !initializationScope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
         initializationScope.problemReporter().unusedPrivateField(this);
      }

      if (this.binding != null
         && this.binding.isValidBinding()
         && this.binding.isStatic()
         && this.binding.constant(initializationScope) == Constant.NotAConstant
         && this.binding.declaringClass.isNestedType()
         && !this.binding.declaringClass.isStatic()) {
         initializationScope.problemReporter().unexpectedStaticModifierForField((SourceTypeBinding)this.binding.declaringClass, this);
      }

      if (this.initialization != null) {
         flowInfo = this.initialization.analyseCode(initializationScope, flowContext, flowInfo).unconditionalInits();
         flowInfo.markAsDefinitelyAssigned(this.binding);
      }

      if (this.initialization != null && this.binding != null) {
         CompilerOptions options = initializationScope.compilerOptions();
         if (options.isAnnotationBasedNullAnalysisEnabled && (this.binding.isNonNull() || options.sourceLevel >= 3407872L)) {
            int nullStatus = this.initialization.nullStatus(flowInfo, flowContext);
            NullAnnotationMatching.checkAssignment(
               initializationScope, flowContext, this.binding, flowInfo, nullStatus, this.initialization, this.initialization.resolvedType
            );
         }

         this.initialization.checkNPEbyUnboxing(initializationScope, flowContext, flowInfo);
      }

      return flowInfo;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         boolean isStatic;
         if (this.initialization != null && (!(isStatic = this.binding.isStatic()) || this.binding.constant() == Constant.NotAConstant)) {
            if (!isStatic) {
               codeStream.aload_0();
            }

            this.initialization.generateCode(currentScope, codeStream, true);
            if (isStatic) {
               codeStream.fieldAccess((byte)-77, this.binding, null);
            } else {
               codeStream.fieldAccess((byte)-75, this.binding, null);
            }
         }

         codeStream.recordPositionsFrom(pc, this.sourceStart);
      }
   }

   public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(this.type, targetType, allAnnotationContexts);
      int i = 0;

      for(int max = this.annotations.length; i < max; ++i) {
         Annotation annotation = this.annotations[i];
         annotation.traverse(collector, null);
      }
   }

   @Override
   public int getKind() {
      return this.type == null ? 3 : 1;
   }

   public boolean isStatic() {
      if (this.binding != null) {
         return this.binding.isStatic();
      } else {
         return (this.modifiers & 8) != 0;
      }
   }

   public boolean isFinal() {
      if (this.binding != null) {
         return this.binding.isFinal();
      } else {
         return (this.modifiers & 16) != 0;
      }
   }

   @Override
   public StringBuffer printStatement(int indent, StringBuffer output) {
      if (this.javadoc != null) {
         this.javadoc.print(indent, output);
      }

      return super.printStatement(indent, output);
   }

   public void resolve(MethodScope initializationScope) {
      if ((this.bits & 16) == 0) {
         if (this.binding != null && this.binding.isValidBinding()) {
            ClassScope classScope;
            this.bits |= 16;
            classScope = initializationScope.enclosingClassScope();
            label261:
            if (classScope != null) {
               SourceTypeBinding declaringType = classScope.enclosingSourceType();
               if (declaringType.superclass != null) {
                  FieldBinding existingVariable = classScope.findField(declaringType.superclass, this.name, this, false, true);
                  if (existingVariable != null
                     && existingVariable.isValidBinding()
                     && existingVariable.original() != this.binding
                     && existingVariable.canBeSeenBy(declaringType, this, initializationScope)) {
                     initializationScope.problemReporter().fieldHiding(this, existingVariable);
                     break label261;
                  }
               }

               Scope outerScope = classScope.parent;
               if (outerScope.kind != 4) {
                  Binding existingVariable = outerScope.getBinding(this.name, 3, this, false);
                  label252:
                  if (existingVariable != null && existingVariable.isValidBinding() && existingVariable != this.binding) {
                     if (existingVariable instanceof FieldBinding) {
                        FieldBinding existingField = (FieldBinding)existingVariable;
                        if (existingField.original() == this.binding || !existingField.isStatic() && declaringType.isStatic()) {
                           break label252;
                        }
                     }

                     initializationScope.problemReporter().fieldHiding(this, existingVariable);
                  }
               }
            }

            if (this.type != null) {
               this.type.resolvedType = this.binding.type;
            }

            FieldBinding previousField = initializationScope.initializedField;
            int previousFieldID = initializationScope.lastVisibleFieldID;

            try {
               initializationScope.initializedField = this.binding;
               initializationScope.lastVisibleFieldID = this.binding.id;
               resolveAnnotations(initializationScope, this.annotations, this.binding);
               if (this.annotations != null) {
                  int i = 0;

                  for(int max = this.annotations.length; i < max; ++i) {
                     TypeBinding resolvedAnnotationType = this.annotations[i].resolvedType;
                     if (resolvedAnnotationType != null && (resolvedAnnotationType.getAnnotationTagBits() & 9007199254740992L) != 0L) {
                        this.bits |= 1048576;
                        break;
                     }
                  }
               }

               if ((this.binding.getAnnotationTagBits() & 70368744177664L) == 0L
                  && (this.binding.modifiers & 1048576) != 0
                  && initializationScope.compilerOptions().sourceLevel >= 3211264L) {
                  initializationScope.problemReporter().missingDeprecatedAnnotationForField(this);
               }

               if (this.initialization == null) {
                  this.binding.setConstant(Constant.NotAConstant);
               } else {
                  this.binding.setConstant(Constant.NotAConstant);
                  TypeBinding fieldType = this.binding.type;
                  this.initialization.setExpressionContext(ExpressionContext.ASSIGNMENT_CONTEXT);
                  this.initialization.setExpectedType(fieldType);
                  if (this.initialization instanceof ArrayInitializer) {
                     TypeBinding initializationType;
                     if ((initializationType = this.initialization.resolveTypeExpecting(initializationScope, fieldType)) != null) {
                        ((ArrayInitializer)this.initialization).binding = (ArrayBinding)initializationType;
                        this.initialization.computeConversion(initializationScope, fieldType, initializationType);
                     }
                  } else {
                     TypeBinding initializationType;
                     if ((initializationType = this.initialization.resolveType(initializationScope)) != null) {
                        if (TypeBinding.notEquals(fieldType, initializationType)) {
                           initializationScope.compilationUnitScope().recordTypeConversion(fieldType, initializationType);
                        }

                        if (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, fieldType)
                           || initializationType.isCompatibleWith(fieldType, classScope)) {
                           this.initialization.computeConversion(initializationScope, fieldType, initializationType);
                           if (initializationType.needsUncheckedConversion(fieldType)) {
                              initializationScope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, fieldType);
                           }

                           if (this.initialization instanceof CastExpression && (this.initialization.bits & 16384) == 0) {
                              CastExpression.checkNeedForAssignedCast(initializationScope, fieldType, (CastExpression)this.initialization);
                           }
                        } else if (this.isBoxingCompatible(initializationType, fieldType, this.initialization, initializationScope)) {
                           this.initialization.computeConversion(initializationScope, fieldType, initializationType);
                           if (this.initialization instanceof CastExpression && (this.initialization.bits & 16384) == 0) {
                              CastExpression.checkNeedForAssignedCast(initializationScope, fieldType, (CastExpression)this.initialization);
                           }
                        } else if ((fieldType.tagBits & 128L) == 0L) {
                           initializationScope.problemReporter().typeMismatchError(initializationType, fieldType, this.initialization, null);
                        }

                        if (this.binding.isFinal()) {
                           this.binding.setConstant(this.initialization.constant.castTo((this.binding.type.id << 4) + this.initialization.constant.typeID()));
                        }
                     } else {
                        this.binding.setConstant(Constant.NotAConstant);
                     }
                  }

                  if (this.binding == Expression.getDirectBinding(this.initialization)) {
                     initializationScope.problemReporter().assignmentHasNoEffect(this, this.name);
                  }
               }

               if (this.javadoc != null) {
                  this.javadoc.resolve(initializationScope);
               } else if (this.binding != null && this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
                  int javadocVisibility = this.binding.modifiers & 7;
                  ProblemReporter reporter = initializationScope.problemReporter();
                  int severity = reporter.computeSeverity(-1610612250);
                  if (severity != 256) {
                     if (classScope != null) {
                        javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
                     }

                     int javadocModifiers = this.binding.modifiers & -8 | javadocVisibility;
                     reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
                  }
               }
            } finally {
               initializationScope.initializedField = previousField;
               initializationScope.lastVisibleFieldID = previousFieldID;
               if (this.binding.constant(initializationScope) == null) {
                  this.binding.setConstant(Constant.NotAConstant);
               }
            }
         }
      }
   }

   public void traverse(ASTVisitor visitor, MethodScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.javadoc != null) {
            this.javadoc.traverse(visitor, scope);
         }

         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, scope);
            }
         }

         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.initialization != null) {
            this.initialization.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }
}
