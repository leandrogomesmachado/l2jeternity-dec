package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.Substitution;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

public class NullAnnotationMatching {
   public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(NullAnnotationMatching.Severity.OK, 1, null);
   public static final NullAnnotationMatching NULL_ANNOTATIONS_OK_NONNULL = new NullAnnotationMatching(NullAnnotationMatching.Severity.OK, 4, null);
   public static final NullAnnotationMatching NULL_ANNOTATIONS_UNCHECKED = new NullAnnotationMatching(NullAnnotationMatching.Severity.UNCHECKED, 1, null);
   public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(NullAnnotationMatching.Severity.MISMATCH, 1, null);
   private final NullAnnotationMatching.Severity severity;
   public final TypeBinding superTypeHint;
   public final int nullStatus;

   NullAnnotationMatching(NullAnnotationMatching.Severity severity, int nullStatus, TypeBinding superTypeHint) {
      this.severity = severity;
      this.superTypeHint = superTypeHint;
      this.nullStatus = nullStatus;
   }

   public boolean isAnyMismatch() {
      return this.severity.isAnyMismatch();
   }

   public boolean isUnchecked() {
      return this.severity == NullAnnotationMatching.Severity.UNCHECKED;
   }

   public boolean isDefiniteMismatch() {
      return this.severity == NullAnnotationMatching.Severity.MISMATCH;
   }

   public boolean wantToReport() {
      return this.severity == NullAnnotationMatching.Severity.LEGACY_WARNING;
   }

   public boolean isPotentiallyNullMismatch() {
      return !this.isDefiniteMismatch() && this.nullStatus != -1 && (this.nullStatus & 16) != 0;
   }

   public String superTypeHintName(CompilerOptions options, boolean shortNames) {
      return String.valueOf(this.superTypeHint.nullAnnotatedReadableName(options, shortNames));
   }

   public static int checkAssignment(
      BlockScope currentScope,
      FlowContext flowContext,
      VariableBinding var,
      FlowInfo flowInfo,
      int nullStatus,
      Expression expression,
      TypeBinding providedType
   ) {
      if (providedType == null) {
         return 1;
      } else {
         long lhsTagBits = 0L;
         boolean hasReported = false;
         boolean usesNullTypeAnnotations = currentScope.environment().usesNullTypeAnnotations();
         if (!usesNullTypeAnnotations) {
            lhsTagBits = var.tagBits & 108086391056891904L;
         } else {
            if (expression instanceof ConditionalExpression && expression.isPolyExpression()) {
               ConditionalExpression ce = (ConditionalExpression)expression;
               int status1 = checkAssignment(currentScope, flowContext, var, flowInfo, ce.ifTrueNullStatus, ce.valueIfTrue, ce.valueIfTrue.resolvedType);
               int status2 = checkAssignment(currentScope, flowContext, var, flowInfo, ce.ifFalseNullStatus, ce.valueIfFalse, ce.valueIfFalse.resolvedType);
               if (status1 == status2) {
                  return status1;
               }

               return nullStatus;
            }

            lhsTagBits = var.type.tagBits & 108086391056891904L;
            NullAnnotationMatching annotationStatus = analyse(
               var.type, providedType, null, null, nullStatus, expression, NullAnnotationMatching.CheckMode.COMPATIBLE
            );
            if (annotationStatus.isAnyMismatch()) {
               flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, flowInfo, nullStatus, annotationStatus);
               hasReported = true;
            } else {
               if (annotationStatus.wantToReport()) {
                  annotationStatus.report(currentScope);
               }

               if (annotationStatus.nullStatus != 1) {
                  return annotationStatus.nullStatus;
               }
            }
         }

         if (lhsTagBits == 72057594037927936L && nullStatus != 4) {
            if (!hasReported) {
               flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, flowInfo, nullStatus, null);
            }

            return 4;
         } else if (lhsTagBits == 36028797018963968L && nullStatus == 1) {
            return usesNullTypeAnnotations && providedType.isTypeVariable() && (providedType.tagBits & 108086391056891904L) == 0L ? 48 : 24;
         } else {
            return nullStatus;
         }
      }
   }

   public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType, int nullStatus) {
      return analyse(requiredType, providedType, null, null, nullStatus, null, NullAnnotationMatching.CheckMode.COMPATIBLE);
   }

   public static NullAnnotationMatching analyse(
      TypeBinding requiredType,
      TypeBinding providedType,
      TypeBinding providedSubstitute,
      Substitution substitution,
      int nullStatus,
      Expression providedExpression,
      NullAnnotationMatching.CheckMode mode
   ) {
      if (!requiredType.enterRecursiveFunction()) {
         return NULL_ANNOTATIONS_OK;
      } else {
         try {
            NullAnnotationMatching.Severity severity = NullAnnotationMatching.Severity.OK;
            TypeBinding superTypeHint = null;
            NullAnnotationMatching okStatus = NULL_ANNOTATIONS_OK;
            if (areSameTypes(requiredType, providedType, providedSubstitute)) {
               return (requiredType.tagBits & 72057594037927936L) != 0L ? okNonNullStatus(providedExpression) : okStatus;
            } else {
               if (requiredType instanceof TypeVariableBinding
                  && substitution != null
                  && (
                     mode == NullAnnotationMatching.CheckMode.EXACT
                        || mode == NullAnnotationMatching.CheckMode.COMPATIBLE
                        || mode == NullAnnotationMatching.CheckMode.BOUND_SUPER_CHECK
                  )) {
                  requiredType.exitRecursiveFunction();
                  requiredType = Scope.substitute(substitution, requiredType);
                  if (!requiredType.enterRecursiveFunction()) {
                     return NULL_ANNOTATIONS_OK;
                  }

                  if (areSameTypes(requiredType, providedType, providedSubstitute)) {
                     if ((requiredType.tagBits & 72057594037927936L) != 0L) {
                        return okNonNullStatus(providedExpression);
                     }

                     return okStatus;
                  }
               }

               if (mode == NullAnnotationMatching.CheckMode.BOUND_CHECK && requiredType instanceof TypeVariableBinding) {
                  boolean passedBoundCheck = substitution instanceof ParameterizedTypeBinding
                     && (((ParameterizedTypeBinding)substitution).tagBits & 4194304L) != 0L;
                  if (!passedBoundCheck) {
                     TypeBinding superClass = requiredType.superclass();
                     if (superClass != null && (superClass.hasNullTypeAnnotations() || substitution != null)) {
                        NullAnnotationMatching status = analyse(
                           superClass, providedType, null, substitution, nullStatus, providedExpression, NullAnnotationMatching.CheckMode.BOUND_SUPER_CHECK
                        );
                        severity = severity.max(status.severity);
                        if (severity == NullAnnotationMatching.Severity.MISMATCH) {
                           return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
                        }
                     }

                     TypeBinding[] superInterfaces = requiredType.superInterfaces();
                     if (superInterfaces != null) {
                        for(int i = 0; i < superInterfaces.length; ++i) {
                           if (superInterfaces[i].hasNullTypeAnnotations() || substitution != null) {
                              NullAnnotationMatching status = analyse(
                                 superInterfaces[i],
                                 providedType,
                                 null,
                                 substitution,
                                 nullStatus,
                                 providedExpression,
                                 NullAnnotationMatching.CheckMode.BOUND_SUPER_CHECK
                              );
                              severity = severity.max(status.severity);
                              if (severity == NullAnnotationMatching.Severity.MISMATCH) {
                                 return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
                              }
                           }
                        }
                     }
                  }
               }

               if (requiredType instanceof ArrayBinding) {
                  long[] requiredDimsTagBits = ((ArrayBinding)requiredType).nullTagBitsPerDimension;
                  if (requiredDimsTagBits != null) {
                     int dims = requiredType.dimensions();
                     if (requiredType.dimensions() != providedType.dimensions()) {
                        if (providedType.id == 12 && dims > 0 && requiredDimsTagBits[0] == 72057594037927936L) {
                           return NULL_ANNOTATIONS_MISMATCH;
                        }
                     } else {
                        long[] providedDimsTagBits = ((ArrayBinding)providedType).nullTagBitsPerDimension;
                        if (providedDimsTagBits == null) {
                           providedDimsTagBits = new long[dims + 1];
                        }

                        int currentNullStatus = nullStatus;

                        for(int i = 0; i <= dims; ++i) {
                           long requiredBits = validNullTagBits(requiredDimsTagBits[i]);
                           long providedBits = validNullTagBits(providedDimsTagBits[i]);
                           if (i == 0 && requiredBits == 36028797018963968L && nullStatus != -1 && mode.requiredNullableMatchesAll()) {
                              if (nullStatus == 2) {
                                 break;
                              }
                           } else {
                              if (i > 0) {
                                 currentNullStatus = -1;
                              }

                              NullAnnotationMatching.Severity dimSeverity = computeNullProblemSeverity(
                                 requiredBits, providedBits, currentNullStatus, i == 0 ? mode : mode.toDetail(), false
                              );
                              if (i > 0
                                 && dimSeverity == NullAnnotationMatching.Severity.UNCHECKED
                                 && providedExpression instanceof ArrayAllocationExpression
                                 && providedBits == 0L
                                 && requiredBits != 0L) {
                                 Expression[] dimensions = ((ArrayAllocationExpression)providedExpression).dimensions;
                                 Expression previousDim = dimensions[i - 1];
                                 if (previousDim instanceof IntLiteral && previousDim.constant.intValue() == 0) {
                                    dimSeverity = NullAnnotationMatching.Severity.OK;
                                    nullStatus = -1;
                                    break;
                                 }
                              }

                              severity = severity.max(dimSeverity);
                              if (severity == NullAnnotationMatching.Severity.MISMATCH) {
                                 return NULL_ANNOTATIONS_MISMATCH;
                              }
                           }

                           if (severity == NullAnnotationMatching.Severity.OK) {
                              nullStatus = -1;
                           }
                        }
                     }
                  }
               } else if (requiredType.hasNullTypeAnnotations() || providedType.hasNullTypeAnnotations() || requiredType.isTypeVariable()) {
                  long requiredBits = requiredNullTagBits(requiredType, mode);
                  if (requiredBits != 36028797018963968L || nullStatus == -1 || !mode.requiredNullableMatchesAll()) {
                     long providedBits = providedNullTagBits(providedType);
                     NullAnnotationMatching.Severity s = computeNullProblemSeverity(
                        requiredBits, providedBits, nullStatus, mode, requiredType.isTypeVariable()
                     );
                     if (s.isAnyMismatch()
                        && requiredType.isWildcard()
                        && requiredBits != 0L
                        && ((WildcardBinding)requiredType).determineNullBitsFromDeclaration(null, null) == 0L) {
                        s = NullAnnotationMatching.Severity.OK;
                     }

                     severity = severity.max(s);
                     if (!severity.isAnyMismatch() && (providedBits & 72057594037927936L) != 0L) {
                        okStatus = okNonNullStatus(providedExpression);
                     }
                  }

                  if (severity != NullAnnotationMatching.Severity.MISMATCH && nullStatus != 2) {
                     TypeBinding providedSuper = providedType.findSuperTypeOriginatingFrom(requiredType);
                     TypeBinding providedSubstituteSuper = providedSubstitute != null ? providedSubstitute.findSuperTypeOriginatingFrom(requiredType) : null;
                     if (severity == NullAnnotationMatching.Severity.UNCHECKED
                        && requiredType.isTypeVariable()
                        && providedType.isTypeVariable()
                        && (providedSuper == requiredType || providedSubstituteSuper == requiredType)) {
                        severity = NullAnnotationMatching.Severity.OK;
                     }

                     if (providedSuper != providedType) {
                        superTypeHint = providedSuper;
                     }

                     if (requiredType.isParameterizedType() && providedSuper instanceof ParameterizedTypeBinding) {
                        TypeBinding[] requiredArguments = ((ParameterizedTypeBinding)requiredType).arguments;
                        TypeBinding[] providedArguments = ((ParameterizedTypeBinding)providedSuper).arguments;
                        TypeBinding[] providedSubstitutes = providedSubstituteSuper instanceof ParameterizedTypeBinding
                           ? ((ParameterizedTypeBinding)providedSubstituteSuper).arguments
                           : null;
                        if (requiredArguments != null && providedArguments != null && requiredArguments.length == providedArguments.length) {
                           for(int i = 0; i < requiredArguments.length; ++i) {
                              TypeBinding providedArgSubstitute = providedSubstitutes != null ? providedSubstitutes[i] : null;
                              NullAnnotationMatching status = analyse(
                                 requiredArguments[i], providedArguments[i], providedArgSubstitute, substitution, -1, providedExpression, mode.toDetail()
                              );
                              severity = severity.max(status.severity);
                              if (severity == NullAnnotationMatching.Severity.MISMATCH) {
                                 return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
                              }
                           }
                        }
                     }

                     TypeBinding requiredEnclosing = requiredType.enclosingType();
                     TypeBinding providedEnclosing = providedType.enclosingType();
                     if (requiredEnclosing != null && providedEnclosing != null) {
                        TypeBinding providedEnclSubstitute = providedSubstitute != null ? providedSubstitute.enclosingType() : null;
                        NullAnnotationMatching status = analyse(
                           requiredEnclosing, providedEnclosing, providedEnclSubstitute, substitution, -1, providedExpression, mode
                        );
                        severity = severity.max(status.severity);
                     }
                  }
               }

               return !severity.isAnyMismatch() ? okStatus : new NullAnnotationMatching(severity, nullStatus, superTypeHint);
            }
         } finally {
            requiredType.exitRecursiveFunction();
         }
      }
   }

   public void report(Scope scope) {
   }

   public static NullAnnotationMatching okNonNullStatus(final Expression providedExpression) {
      if (providedExpression instanceof MessageSend) {
         final MethodBinding method = ((MessageSend)providedExpression).binding;
         if (method != null && method.isValidBinding()) {
            MethodBinding originalMethod = method.original();
            TypeBinding originalDeclaringClass = originalMethod.declaringClass;
            if (originalDeclaringClass instanceof BinaryTypeBinding
               && ((BinaryTypeBinding)originalDeclaringClass).externalAnnotationStatus.isPotentiallyUnannotatedLib()
               && originalMethod.returnType.isTypeVariable()
               && (originalMethod.returnType.tagBits & 108086391056891904L) == 0L) {
               final int severity = ((BinaryTypeBinding)originalDeclaringClass).externalAnnotationStatus
                     == BinaryTypeBinding.ExternalAnnotationStatus.NO_EEA_FILE
                  ? 0
                  : 1024;
               return new NullAnnotationMatching(NullAnnotationMatching.Severity.LEGACY_WARNING, 1, null) {
                  @Override
                  public void report(Scope scope) {
                     scope.problemReporter().nonNullTypeVariableInUnannotatedBinary(scope.environment(), method, providedExpression, severity);
                  }
               };
            }
         }
      }

      return NULL_ANNOTATIONS_OK_NONNULL;
   }

   protected static boolean areSameTypes(TypeBinding requiredType, TypeBinding providedType, TypeBinding providedSubstitute) {
      if (requiredType == providedType) {
         return true;
      } else if (requiredType.isParameterizedType() || requiredType.isArrayType()) {
         return false;
      } else if (TypeBinding.notEquals(requiredType, providedType)) {
         if (requiredType instanceof CaptureBinding) {
            TypeBinding lowerBound = ((CaptureBinding)requiredType).lowerBound;
            if (lowerBound != null && areSameTypes(lowerBound, providedType, providedSubstitute)) {
               if ((requiredType.tagBits & 108086391056891904L) == (providedType.tagBits & 108086391056891904L)) {
                  return true;
               }

               return false;
            }
         } else {
            if (requiredType.kind() == 4100 && requiredType == providedSubstitute) {
               return true;
            }

            if (providedType instanceof CaptureBinding) {
               TypeBinding upperBound = ((CaptureBinding)providedType).upperBound();
               if (upperBound != null && areSameTypes(requiredType, upperBound, providedSubstitute)) {
                  if ((requiredType.tagBits & 108086391056891904L) == (providedType.tagBits & 108086391056891904L)) {
                     return true;
                  }

                  return false;
               }
            }
         }

         return false;
      } else {
         return (requiredType.tagBits & 108086391056891904L) == (providedType.tagBits & 108086391056891904L);
      }
   }

   static long requiredNullTagBits(TypeBinding type, NullAnnotationMatching.CheckMode mode) {
      long tagBits = type.tagBits & 108086391056891904L;
      if (tagBits != 0L) {
         return validNullTagBits(tagBits);
      } else if (type.isWildcard()) {
         return 108086391056891904L;
      } else {
         if (type.isTypeVariable()) {
            if (type.isCapture()) {
               TypeBinding lowerBound = ((CaptureBinding)type).lowerBound;
               if (lowerBound != null) {
                  tagBits = lowerBound.tagBits & 108086391056891904L;
                  if (tagBits == 36028797018963968L) {
                     return 36028797018963968L;
                  }
               }
            }

            switch(mode) {
               case BOUND_CHECK:
               case BOUND_SUPER_CHECK:
               case OVERRIDE_RETURN:
               case OVERRIDE:
                  break;
               default:
                  return 72057594037927936L;
            }
         }

         return 0L;
      }
   }

   static long providedNullTagBits(TypeBinding type) {
      long tagBits = type.tagBits & 108086391056891904L;
      if (tagBits != 0L) {
         return validNullTagBits(tagBits);
      } else if (type.isWildcard()) {
         return 108086391056891904L;
      } else {
         if (type.isTypeVariable()) {
            TypeVariableBinding typeVariable = (TypeVariableBinding)type;
            boolean haveNullBits = false;
            if (typeVariable.isCapture()) {
               TypeBinding lowerBound = ((CaptureBinding)typeVariable).lowerBound;
               if (lowerBound != null) {
                  tagBits = lowerBound.tagBits & 108086391056891904L;
                  if (tagBits == 36028797018963968L) {
                     return 36028797018963968L;
                  }

                  haveNullBits |= tagBits != 0L;
               }
            }

            if (typeVariable.firstBound != null) {
               long boundBits = typeVariable.firstBound.tagBits & 108086391056891904L;
               if (boundBits == 72057594037927936L) {
                  return 72057594037927936L;
               }

               haveNullBits |= boundBits != 0L;
            }

            if (haveNullBits) {
               return 108086391056891904L;
            }
         }

         return 0L;
      }
   }

   public static long validNullTagBits(long bits) {
      bits &= 108086391056891904L;
      return bits == 108086391056891904L ? 0L : bits;
   }

   public static TypeBinding moreDangerousType(TypeBinding one, TypeBinding two) {
      if (one == null) {
         return null;
      } else {
         long oneNullBits = validNullTagBits(one.tagBits);
         long twoNullBits = validNullTagBits(two.tagBits);
         if (oneNullBits != twoNullBits) {
            if (oneNullBits == 36028797018963968L) {
               return one;
            } else if (twoNullBits == 36028797018963968L) {
               return two;
            } else {
               return oneNullBits == 0L ? one : two;
            }
         } else {
            return one != two && analyse(one, two, -1).isAnyMismatch() ? two : one;
         }
      }
   }

   private static NullAnnotationMatching.Severity computeNullProblemSeverity(
      long requiredBits, long providedBits, int nullStatus, NullAnnotationMatching.CheckMode mode, boolean requiredIsTypeVariable
   ) {
      if (requiredBits == providedBits) {
         return NullAnnotationMatching.Severity.OK;
      } else {
         if (requiredBits == 0L) {
            switch(mode) {
               case COMPATIBLE:
               case EXACT:
               case BOUND_CHECK:
               case BOUND_SUPER_CHECK:
                  return NullAnnotationMatching.Severity.OK;
               case OVERRIDE_RETURN:
                  if (providedBits == 72057594037927936L) {
                     return NullAnnotationMatching.Severity.OK;
                  }

                  if (!requiredIsTypeVariable) {
                     return NullAnnotationMatching.Severity.OK;
                  }

                  return NullAnnotationMatching.Severity.UNCHECKED;
               case OVERRIDE:
                  return NullAnnotationMatching.Severity.UNCHECKED;
            }
         } else {
            if (requiredBits == 108086391056891904L) {
               return NullAnnotationMatching.Severity.OK;
            }

            if (requiredBits == 72057594037927936L) {
               switch(mode) {
                  case COMPATIBLE:
                  case BOUND_SUPER_CHECK:
                     if (nullStatus == 4) {
                        return NullAnnotationMatching.Severity.OK;
                     }
                  case EXACT:
                  case BOUND_CHECK:
                  case OVERRIDE_RETURN:
                  case OVERRIDE:
                     if (providedBits == 0L) {
                        return NullAnnotationMatching.Severity.UNCHECKED;
                     }

                     return NullAnnotationMatching.Severity.MISMATCH;
               }
            } else if (requiredBits == 36028797018963968L) {
               switch(mode) {
                  case COMPATIBLE:
                  case BOUND_SUPER_CHECK:
                  case OVERRIDE_RETURN:
                     return NullAnnotationMatching.Severity.OK;
                  case EXACT:
                  case BOUND_CHECK:
                     if (providedBits == 0L) {
                        return NullAnnotationMatching.Severity.UNCHECKED;
                     }

                     return NullAnnotationMatching.Severity.MISMATCH;
                  case OVERRIDE:
                     return NullAnnotationMatching.Severity.MISMATCH;
               }
            }
         }

         return NullAnnotationMatching.Severity.OK;
      }
   }

   public static MethodBinding checkForContradictions(MethodBinding method, Object location, Scope scope) {
      int start = 0;
      int end = 0;
      if (location instanceof InvocationSite) {
         start = ((InvocationSite)location).sourceStart();
         end = ((InvocationSite)location).sourceEnd();
      } else if (location instanceof ASTNode) {
         start = ((ASTNode)location).sourceStart;
         end = ((ASTNode)location).sourceEnd;
      }

      NullAnnotationMatching.SearchContradictions searchContradiction = new NullAnnotationMatching.SearchContradictions();
      TypeBindingVisitor.visit(searchContradiction, method.returnType);
      if (searchContradiction.typeWithContradiction != null) {
         if (scope == null) {
            return new ProblemMethodBinding(method, method.selector, method.parameters, 25);
         } else {
            scope.problemReporter().contradictoryNullAnnotationsInferred(method, start, end, location instanceof FunctionalExpression);
            return method;
         }
      } else {
         Expression[] arguments = null;
         if (location instanceof Invocation) {
            arguments = ((Invocation)location).arguments();
         }

         for(int i = 0; i < method.parameters.length; ++i) {
            TypeBindingVisitor.visit(searchContradiction, method.parameters[i]);
            if (searchContradiction.typeWithContradiction != null) {
               if (scope == null) {
                  return new ProblemMethodBinding(method, method.selector, method.parameters, 25);
               }

               if (arguments != null && i < arguments.length) {
                  scope.problemReporter().contradictoryNullAnnotationsInferred(method, arguments[i]);
               } else {
                  scope.problemReporter().contradictoryNullAnnotationsInferred(method, start, end, location instanceof FunctionalExpression);
               }

               return method;
            }
         }

         return method;
      }
   }

   public static boolean hasContradictions(TypeBinding type) {
      NullAnnotationMatching.SearchContradictions searchContradiction = new NullAnnotationMatching.SearchContradictions();
      TypeBindingVisitor.visit(searchContradiction, type);
      return searchContradiction.typeWithContradiction != null;
   }

   public static TypeBinding strongerType(TypeBinding type1, TypeBinding type2, LookupEnvironment environment) {
      return (type1.tagBits & 72057594037927936L) != 0L
         ? mergeTypeAnnotations(type1, type2, true, environment)
         : mergeTypeAnnotations(type2, type1, true, environment);
   }

   public static TypeBinding[] weakerTypes(TypeBinding[] parameters1, TypeBinding[] parameters2, LookupEnvironment environment) {
      TypeBinding[] newParameters = new TypeBinding[parameters1.length];

      for(int i = 0; i < newParameters.length; ++i) {
         long tagBits1 = parameters1[i].tagBits;
         long tagBits2 = parameters2[i].tagBits;
         if ((tagBits1 & 36028797018963968L) != 0L) {
            newParameters[i] = mergeTypeAnnotations(parameters1[i], parameters2[i], true, environment);
         } else if ((tagBits2 & 36028797018963968L) != 0L) {
            newParameters[i] = mergeTypeAnnotations(parameters2[i], parameters1[i], true, environment);
         } else if ((tagBits1 & 72057594037927936L) == 0L) {
            newParameters[i] = mergeTypeAnnotations(parameters1[i], parameters2[i], true, environment);
         } else {
            newParameters[i] = mergeTypeAnnotations(parameters2[i], parameters1[i], true, environment);
         }
      }

      return newParameters;
   }

   private static TypeBinding mergeTypeAnnotations(TypeBinding type, TypeBinding otherType, boolean top, LookupEnvironment environment) {
      TypeBinding mainType = type;
      if (!top) {
         AnnotationBinding[] otherAnnotations = otherType.getTypeAnnotations();
         if (otherAnnotations != Binding.NO_ANNOTATIONS) {
            mainType = environment.createAnnotatedType(type, otherAnnotations);
         }
      }

      if (mainType.isParameterizedType() && otherType.isParameterizedType()) {
         ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)type;
         ParameterizedTypeBinding otherPTB = (ParameterizedTypeBinding)otherType;
         TypeBinding[] typeArguments = ptb.arguments;
         TypeBinding[] otherTypeArguments = otherPTB.arguments;
         TypeBinding[] newTypeArguments = new TypeBinding[typeArguments.length];

         for(int i = 0; i < typeArguments.length; ++i) {
            newTypeArguments[i] = mergeTypeAnnotations(typeArguments[i], otherTypeArguments[i], false, environment);
         }

         return environment.createParameterizedType(ptb.genericType(), newTypeArguments, ptb.enclosingType());
      } else {
         return mainType;
      }
   }

   @Override
   public String toString() {
      if (this == NULL_ANNOTATIONS_OK) {
         return "OK";
      } else if (this == NULL_ANNOTATIONS_MISMATCH) {
         return "MISMATCH";
      } else if (this == NULL_ANNOTATIONS_OK_NONNULL) {
         return "OK NonNull";
      } else if (this == NULL_ANNOTATIONS_UNCHECKED) {
         return "UNCHECKED";
      } else {
         StringBuilder buf = new StringBuilder();
         buf.append("Analysis result: severity=" + this.severity);
         buf.append(" nullStatus=" + this.nullStatus);
         return buf.toString();
      }
   }

   public static enum CheckMode {
      COMPATIBLE {
         @Override
         boolean requiredNullableMatchesAll() {
            return true;
         }
      },
      EXACT,
      BOUND_CHECK,
      BOUND_SUPER_CHECK,
      OVERRIDE_RETURN {
         @Override
         NullAnnotationMatching.CheckMode toDetail() {
            return OVERRIDE;
         }
      },
      OVERRIDE {
         @Override
         boolean requiredNullableMatchesAll() {
            return true;
         }

         @Override
         NullAnnotationMatching.CheckMode toDetail() {
            return OVERRIDE;
         }
      };

      private CheckMode() {
      }

      boolean requiredNullableMatchesAll() {
         return false;
      }

      NullAnnotationMatching.CheckMode toDetail() {
         return EXACT;
      }
   }

   static class SearchContradictions extends TypeBindingVisitor {
      ReferenceBinding typeWithContradiction;

      @Override
      public boolean visit(ReferenceBinding referenceBinding) {
         if ((referenceBinding.tagBits & 108086391056891904L) == 108086391056891904L) {
            this.typeWithContradiction = referenceBinding;
            return false;
         } else {
            return true;
         }
      }

      @Override
      public boolean visit(TypeVariableBinding typeVariable) {
         if (!this.visit((ReferenceBinding)typeVariable)) {
            return false;
         } else {
            long allNullBits = typeVariable.tagBits & 108086391056891904L;
            if (typeVariable.firstBound != null) {
               allNullBits = typeVariable.firstBound.tagBits & 108086391056891904L;
            }

            TypeBinding[] var7;
            for(TypeBinding otherBound : var7 = typeVariable.otherUpperBounds()) {
               allNullBits |= otherBound.tagBits & 108086391056891904L;
            }

            if (allNullBits == 108086391056891904L) {
               this.typeWithContradiction = typeVariable;
               return false;
            } else {
               return true;
            }
         }
      }

      @Override
      public boolean visit(RawTypeBinding rawType) {
         return this.visit((ReferenceBinding)rawType);
      }

      @Override
      public boolean visit(WildcardBinding wildcardBinding) {
         long allNullBits = wildcardBinding.tagBits & 108086391056891904L;
         switch(wildcardBinding.boundKind) {
            case 1:
               allNullBits |= wildcardBinding.bound.tagBits & 72057594037927936L;
               break;
            case 2:
               allNullBits |= wildcardBinding.bound.tagBits & 36028797018963968L;
         }

         if (allNullBits == 108086391056891904L) {
            this.typeWithContradiction = wildcardBinding;
            return false;
         } else {
            return true;
         }
      }

      @Override
      public boolean visit(ParameterizedTypeBinding parameterizedTypeBinding) {
         return !this.visit((ReferenceBinding)parameterizedTypeBinding) ? false : super.visit(parameterizedTypeBinding);
      }
   }

   private static enum Severity {
      OK,
      LEGACY_WARNING,
      UNCHECKED,
      MISMATCH;

      public NullAnnotationMatching.Severity max(NullAnnotationMatching.Severity severity) {
         return this.compareTo(severity) < 0 ? severity : this;
      }

      public boolean isAnyMismatch() {
         return this.compareTo(LEGACY_WARNING) > 0;
      }
   }
}
