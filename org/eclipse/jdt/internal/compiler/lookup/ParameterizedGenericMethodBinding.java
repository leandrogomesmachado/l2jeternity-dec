package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ParameterizedGenericMethodBinding extends ParameterizedMethodBinding implements Substitution {
   public TypeBinding[] typeArguments;
   protected LookupEnvironment environment;
   public boolean inferredReturnType;
   public boolean wasInferred;
   public boolean isRaw;
   private MethodBinding tiebreakMethod;
   public boolean inferredWithUncheckedConversion;

   public static MethodBinding computeCompatibleMethod(MethodBinding originalMethod, TypeBinding[] arguments, Scope scope, InvocationSite invocationSite) {
      TypeVariableBinding[] typeVariables = originalMethod.typeVariables;
      TypeBinding[] substitutes = invocationSite.genericTypeArguments();
      InferenceContext inferenceContext = null;
      TypeBinding[] uncheckedArguments = null;
      ParameterizedGenericMethodBinding methodSubstitute;
      if (substitutes != null) {
         if (substitutes.length != typeVariables.length) {
            return new ProblemMethodBinding(originalMethod, originalMethod.selector, substitutes, 11);
         }

         methodSubstitute = scope.environment().createParameterizedGenericMethod(originalMethod, substitutes);
      } else {
         TypeBinding[] parameters = originalMethod.parameters;
         CompilerOptions compilerOptions = scope.compilerOptions();
         if (compilerOptions.sourceLevel >= 3407872L) {
            return computeCompatibleMethod18(originalMethod, arguments, scope, invocationSite);
         }

         inferenceContext = new InferenceContext(originalMethod);
         methodSubstitute = inferFromArgumentTypes(scope, originalMethod, arguments, parameters, inferenceContext);
         if (methodSubstitute == null) {
            return null;
         }

         if (inferenceContext.hasUnresolvedTypeArgument()) {
            if (inferenceContext.isUnchecked) {
               int length = inferenceContext.substitutes.length;
               System.arraycopy(inferenceContext.substitutes, 0, uncheckedArguments = new TypeBinding[length], 0, length);
            }

            if (methodSubstitute.returnType != TypeBinding.VOID) {
               TypeBinding expectedType = invocationSite.invocationTargetType();
               if (expectedType != null) {
                  inferenceContext.hasExplicitExpectedType = true;
               } else {
                  expectedType = scope.getJavaLangObject();
               }

               inferenceContext.expectedType = expectedType;
            }

            methodSubstitute = methodSubstitute.inferFromExpectedType(scope, inferenceContext);
            if (methodSubstitute == null) {
               return null;
            }
         } else if (compilerOptions.sourceLevel == 3342336L && methodSubstitute.returnType != TypeBinding.VOID) {
            TypeBinding expectedType = invocationSite.invocationTargetType();
            if (expectedType != null && !originalMethod.returnType.mentionsAny(originalMethod.parameters, -1)) {
               TypeBinding uncaptured = methodSubstitute.returnType.uncapture(scope);
               if (!methodSubstitute.returnType.isCompatibleWith(expectedType) && expectedType.isCompatibleWith(uncaptured)) {
                  InferenceContext oldContext = inferenceContext;
                  inferenceContext = new InferenceContext(originalMethod);
                  originalMethod.returnType.collectSubstitutes(scope, expectedType, inferenceContext, 1);
                  ParameterizedGenericMethodBinding substitute = inferFromArgumentTypes(scope, originalMethod, arguments, parameters, inferenceContext);
                  if (substitute != null && substitute.returnType.isCompatibleWith(expectedType)) {
                     if (scope.parameterCompatibilityLevel(substitute, arguments, false) > -1) {
                        methodSubstitute = substitute;
                     } else {
                        inferenceContext = oldContext;
                     }
                  } else {
                     inferenceContext = oldContext;
                  }
               }
            }
         }
      }

      Substitution substitution = null;
      if (inferenceContext != null) {
         substitution = new ParameterizedGenericMethodBinding.LingeringTypeVariableEliminator(typeVariables, inferenceContext.substitutes, scope);
      } else {
         substitution = methodSubstitute;
      }

      int i = 0;

      for(int length = typeVariables.length; i < length; ++i) {
         TypeVariableBinding typeVariable = typeVariables[i];
         TypeBinding substitute = methodSubstitute.typeArguments[i];
         TypeBinding substituteForChecks;
         if (substitute instanceof TypeVariableBinding) {
            substituteForChecks = substitute;
         } else {
            substituteForChecks = Scope.substitute(
               new ParameterizedGenericMethodBinding.LingeringTypeVariableEliminator(typeVariables, null, scope), substitute
            );
         }

         if (uncheckedArguments == null || uncheckedArguments[i] != null) {
            switch(typeVariable.boundCheck(substitution, substituteForChecks, scope, null)) {
               case UNCHECKED:
                  methodSubstitute.tagBits |= 256L;
                  break;
               case MISMATCH:
                  int argLength = arguments.length;
                  TypeBinding[] augmentedArguments = new TypeBinding[argLength + 2];
                  System.arraycopy(arguments, 0, augmentedArguments, 0, argLength);
                  augmentedArguments[argLength] = substitute;
                  augmentedArguments[argLength + 1] = typeVariable;
                  return new ProblemMethodBinding(methodSubstitute, originalMethod.selector, augmentedArguments, 10);
            }
         }
      }

      return methodSubstitute;
   }

   public static MethodBinding computeCompatibleMethod18(MethodBinding originalMethod, TypeBinding[] arguments, Scope scope, InvocationSite invocationSite) {
      TypeVariableBinding[] typeVariables = originalMethod.typeVariables;
      if (invocationSite.checkingPotentialCompatibility()) {
         return scope.environment().createParameterizedGenericMethod(originalMethod, typeVariables);
      } else {
         ParameterizedGenericMethodBinding methodSubstitute = null;
         InferenceContext18 infCtx18 = invocationSite.freshInferenceContext(scope);
         if (infCtx18 == null) {
            return originalMethod;
         } else {
            TypeBinding[] parameters = originalMethod.parameters;
            CompilerOptions compilerOptions = scope.compilerOptions();
            boolean invocationTypeInferred = false;
            boolean requireBoxing = false;
            TypeBinding[] argumentsCopy = new TypeBinding[arguments.length];
            int i = 0;
            int length = arguments.length;

            for(int parametersLength = parameters.length; i < length; ++i) {
               TypeBinding parameter = i < parametersLength ? parameters[i] : parameters[parametersLength - 1];
               TypeBinding argument = arguments[i];
               if (argument.isPrimitiveType() != parameter.isPrimitiveType()) {
                  argumentsCopy[i] = scope.environment().computeBoxingType(argument);
                  requireBoxing = true;
               } else {
                  argumentsCopy[i] = argument;
               }
            }

            arguments = argumentsCopy;
            LookupEnvironment environment = scope.environment();
            InferenceContext18 previousContext = environment.currentInferenceContext;
            if (previousContext == null) {
               environment.currentInferenceContext = infCtx18;
            }

            try {
               BoundSet provisionalResult = null;
               BoundSet result = null;
               boolean isPolyExpression = invocationSite instanceof Expression && ((Expression)invocationSite).isPolyExpression(originalMethod);
               boolean isDiamond = isPolyExpression && originalMethod.isConstructor();
               if (arguments.length == parameters.length) {
                  infCtx18.inferenceKind = requireBoxing ? 2 : 1;
                  infCtx18.inferInvocationApplicability(originalMethod, arguments, isDiamond);
                  result = infCtx18.solve(true);
               }

               if (result == null && originalMethod.isVarargs()) {
                  infCtx18 = invocationSite.freshInferenceContext(scope);
                  infCtx18.inferenceKind = 3;
                  infCtx18.inferInvocationApplicability(originalMethod, arguments, isDiamond);
                  result = infCtx18.solve(true);
               }

               if (result == null) {
                  return null;
               } else if (!infCtx18.isResolved(result)) {
                  return null;
               } else {
                  infCtx18.stepCompleted = 1;
                  TypeBinding expectedType = invocationSite.invocationTargetType();
                  boolean hasReturnProblem = false;
                  if (expectedType != null || !invocationSite.getExpressionContext().definesTargetType() || !isPolyExpression) {
                     provisionalResult = result;
                     result = infCtx18.inferInvocationType(expectedType, invocationSite, originalMethod);
                     invocationTypeInferred = true;
                     hasReturnProblem |= result == null;
                     if (hasReturnProblem) {
                        result = provisionalResult;
                     }
                  }

                  if (result == null) {
                     return null;
                  } else {
                     TypeBinding[] solutions = infCtx18.getSolutions(typeVariables, invocationSite, result);
                     if (solutions == null) {
                        return null;
                     } else {
                        methodSubstitute = scope.environment()
                           .createParameterizedGenericMethod(originalMethod, solutions, infCtx18.usesUncheckedConversion, hasReturnProblem);
                        if (invocationSite instanceof Invocation) {
                           infCtx18.forwardResults(result, (Invocation)invocationSite, methodSubstitute, expectedType);
                        }

                        try {
                           if (hasReturnProblem) {
                              MethodBinding problemMethod = infCtx18.getReturnProblemMethodIfNeeded(expectedType, methodSubstitute);
                              if (problemMethod instanceof ProblemMethodBinding) {
                                 return problemMethod;
                              }
                           }

                           if (invocationTypeInferred) {
                              if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
                                 NullAnnotationMatching.checkForContradictions(methodSubstitute, invocationSite, scope);
                              }

                              MethodBinding problemMethod = methodSubstitute.boundCheck18(scope, arguments, invocationSite);
                              if (problemMethod != null) {
                                 return problemMethod;
                              }
                           } else {
                              methodSubstitute = new PolyParameterizedGenericMethodBinding(methodSubstitute);
                           }
                        } finally {
                           if (invocationSite instanceof Invocation) {
                              ((Invocation)invocationSite).registerInferenceContext(methodSubstitute, infCtx18);
                           } else if (invocationSite instanceof ReferenceExpression) {
                              ((ReferenceExpression)invocationSite).registerInferenceContext(methodSubstitute, infCtx18);
                           }
                        }

                        return methodSubstitute;
                     }
                  }
               }
            } catch (InferenceFailureException var33) {
               scope.problemReporter().genericInferenceError(var33.getMessage(), invocationSite);
               return null;
            } finally {
               environment.currentInferenceContext = previousContext;
            }
         }
      }
   }

   MethodBinding boundCheck18(Scope scope, TypeBinding[] arguments, InvocationSite site) {
      Substitution substitution = this;
      ParameterizedGenericMethodBinding methodSubstitute = this;
      TypeVariableBinding[] originalTypeVariables = this.originalMethod.typeVariables;
      int i = 0;

      for(int length = originalTypeVariables.length; i < length; ++i) {
         TypeVariableBinding typeVariable = originalTypeVariables[i];
         TypeBinding substitute = methodSubstitute.typeArguments[i];
         ASTNode location = site instanceof ASTNode ? (ASTNode)site : null;
         switch(typeVariable.boundCheck(substitution, substitute, scope, location)) {
            case UNCHECKED:
               methodSubstitute.tagBits |= 256L;
               break;
            default:
            case MISMATCH:
               int argLength = arguments.length;
               TypeBinding[] augmentedArguments = new TypeBinding[argLength + 2];
               System.arraycopy(arguments, 0, augmentedArguments, 0, argLength);
               augmentedArguments[argLength] = substitute;
               augmentedArguments[argLength + 1] = typeVariable;
               return new ProblemMethodBinding(methodSubstitute, this.originalMethod.selector, augmentedArguments, 10);
         }
      }

      return null;
   }

   private static ParameterizedGenericMethodBinding inferFromArgumentTypes(
      Scope scope, MethodBinding originalMethod, TypeBinding[] arguments, TypeBinding[] parameters, InferenceContext inferenceContext
   ) {
      if (originalMethod.isVarargs()) {
         int paramLength = parameters.length;
         int minArgLength = paramLength - 1;
         int argLength = arguments.length;

         for(int i = 0; i < minArgLength; ++i) {
            parameters[i].collectSubstitutes(scope, arguments[i], inferenceContext, 1);
            if (inferenceContext.status == 1) {
               return null;
            }
         }

         if (minArgLength < argLength) {
            TypeBinding varargType;
            label78: {
               varargType = parameters[minArgLength];
               TypeBinding lastArgument = arguments[minArgLength];
               if (paramLength == argLength) {
                  if (lastArgument == TypeBinding.NULL) {
                     break label78;
                  }

                  switch(lastArgument.dimensions()) {
                     case 0:
                        break;
                     case 1:
                        if (!lastArgument.leafComponentType().isBaseType()) {
                           break label78;
                        }
                        break;
                     default:
                        break label78;
                  }
               }

               varargType = ((ArrayBinding)varargType).elementsType();
            }

            for(int i = minArgLength; i < argLength; ++i) {
               varargType.collectSubstitutes(scope, arguments[i], inferenceContext, 1);
               if (inferenceContext.status == 1) {
                  return null;
               }
            }
         }
      } else {
         int paramLength = parameters.length;

         for(int i = 0; i < paramLength; ++i) {
            parameters[i].collectSubstitutes(scope, arguments[i], inferenceContext, 1);
            if (inferenceContext.status == 1) {
               return null;
            }
         }
      }

      TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
      if (!resolveSubstituteConstraints(scope, originalVariables, inferenceContext, false)) {
         return null;
      } else {
         TypeBinding[] inferredSustitutes = inferenceContext.substitutes;
         TypeBinding[] actualSubstitutes = inferredSustitutes;
         int i = 0;

         for(int varLength = originalVariables.length; i < varLength; ++i) {
            if (inferredSustitutes[i] == null) {
               if (actualSubstitutes == inferredSustitutes) {
                  System.arraycopy(inferredSustitutes, 0, actualSubstitutes = new TypeBinding[varLength], 0, i);
               }

               actualSubstitutes[i] = originalVariables[i];
            } else if (actualSubstitutes != inferredSustitutes) {
               actualSubstitutes[i] = inferredSustitutes[i];
            }
         }

         return scope.environment().createParameterizedGenericMethod(originalMethod, actualSubstitutes);
      }
   }

   private static boolean resolveSubstituteConstraints(
      Scope scope, TypeVariableBinding[] typeVariables, InferenceContext inferenceContext, boolean considerEXTENDSConstraints
   ) {
      TypeBinding[] substitutes = inferenceContext.substitutes;
      int varLength = typeVariables.length;

      label103:
      for(int i = 0; i < varLength; ++i) {
         TypeVariableBinding current = typeVariables[i];
         TypeBinding substitute = substitutes[i];
         if (substitute == null) {
            TypeBinding[] equalSubstitutes = inferenceContext.getSubstitutes(current, 0);
            if (equalSubstitutes != null) {
               int j = 0;

               for(int equalLength = equalSubstitutes.length; j < equalLength; ++j) {
                  TypeBinding equalSubstitute = equalSubstitutes[j];
                  if (equalSubstitute != null) {
                     if (TypeBinding.equalsEquals(equalSubstitute, current)) {
                        for(int k = j + 1; k < equalLength; ++k) {
                           equalSubstitute = equalSubstitutes[k];
                           if (TypeBinding.notEquals(equalSubstitute, current) && equalSubstitute != null) {
                              substitutes[i] = equalSubstitute;
                              continue label103;
                           }
                        }

                        substitutes[i] = current;
                        break;
                     }

                     substitutes[i] = equalSubstitute;
                     break;
                  }
               }
            }
         }
      }

      if (inferenceContext.hasUnresolvedTypeArgument()) {
         for(int i = 0; i < varLength; ++i) {
            TypeVariableBinding current = typeVariables[i];
            TypeBinding substitute = substitutes[i];
            if (substitute == null) {
               TypeBinding[] bounds = inferenceContext.getSubstitutes(current, 2);
               if (bounds != null) {
                  TypeBinding mostSpecificSubstitute = scope.lowerUpperBound(bounds);
                  if (mostSpecificSubstitute == null) {
                     return false;
                  }

                  if (mostSpecificSubstitute != TypeBinding.VOID) {
                     substitutes[i] = mostSpecificSubstitute;
                  }
               }
            }
         }
      }

      if (considerEXTENDSConstraints && inferenceContext.hasUnresolvedTypeArgument()) {
         for(int i = 0; i < varLength; ++i) {
            TypeVariableBinding current = typeVariables[i];
            TypeBinding substitute = substitutes[i];
            if (substitute == null) {
               TypeBinding[] bounds = inferenceContext.getSubstitutes(current, 1);
               if (bounds != null) {
                  TypeBinding[] glb = Scope.greaterLowerBound(bounds, scope, scope.environment());
                  TypeBinding mostSpecificSubstitute = null;
                  if (glb != null) {
                     if (glb.length == 1) {
                        mostSpecificSubstitute = glb[0];
                     } else {
                        TypeBinding[] otherBounds = new TypeBinding[glb.length - 1];
                        System.arraycopy(glb, 1, otherBounds, 0, glb.length - 1);
                        mostSpecificSubstitute = scope.environment().createWildcard(null, 0, glb[0], otherBounds, 1);
                     }
                  }

                  if (mostSpecificSubstitute != null) {
                     substitutes[i] = mostSpecificSubstitute;
                  }
               }
            }
         }
      }

      return true;
   }

   public ParameterizedGenericMethodBinding(MethodBinding originalMethod, RawTypeBinding rawType, LookupEnvironment environment) {
      TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
      int length = originalVariables.length;
      TypeBinding[] rawArguments = new TypeBinding[length];

      for(int i = 0; i < length; ++i) {
         rawArguments[i] = environment.convertToRawType(originalVariables[i].erasure(), false);
      }

      this.isRaw = true;
      this.tagBits = originalMethod.tagBits;
      this.environment = environment;
      this.modifiers = originalMethod.modifiers;
      this.selector = originalMethod.selector;
      this.declaringClass = (ReferenceBinding)(rawType == null ? originalMethod.declaringClass : rawType);
      this.typeVariables = Binding.NO_TYPE_VARIABLES;
      this.typeArguments = rawArguments;
      this.originalMethod = originalMethod;
      boolean ignoreRawTypeSubstitution = rawType == null || originalMethod.isStatic();
      this.parameters = Scope.substitute(this, ignoreRawTypeSubstitution ? originalMethod.parameters : Scope.substitute(rawType, originalMethod.parameters));
      this.thrownExceptions = Scope.substitute(
         this, ignoreRawTypeSubstitution ? originalMethod.thrownExceptions : Scope.substitute(rawType, originalMethod.thrownExceptions)
      );
      if (this.thrownExceptions == null) {
         this.thrownExceptions = Binding.NO_EXCEPTIONS;
      }

      this.returnType = Scope.substitute(this, ignoreRawTypeSubstitution ? originalMethod.returnType : Scope.substitute(rawType, originalMethod.returnType));
      this.wasInferred = false;
      this.parameterNonNullness = originalMethod.parameterNonNullness;
      this.defaultNullness = originalMethod.defaultNullness;
   }

   public ParameterizedGenericMethodBinding(
      MethodBinding originalMethod,
      TypeBinding[] typeArguments,
      LookupEnvironment environment,
      boolean inferredWithUncheckConversion,
      boolean hasReturnProblem
   ) {
      this.environment = environment;
      this.inferredWithUncheckedConversion = inferredWithUncheckConversion;
      this.modifiers = originalMethod.modifiers;
      this.selector = originalMethod.selector;
      this.declaringClass = originalMethod.declaringClass;
      if (inferredWithUncheckConversion && originalMethod.isConstructor() && this.declaringClass.isParameterizedType()) {
         this.declaringClass = (ReferenceBinding)environment.convertToRawType(this.declaringClass.erasure(), false);
      }

      this.typeVariables = Binding.NO_TYPE_VARIABLES;
      this.typeArguments = typeArguments;
      this.isRaw = false;
      this.tagBits = originalMethod.tagBits;
      this.originalMethod = originalMethod;
      this.parameters = Scope.substitute(this, originalMethod.parameters);
      if (inferredWithUncheckConversion) {
         this.returnType = this.getErasure18_5_2(originalMethod.returnType, environment, hasReturnProblem);
         this.thrownExceptions = new ReferenceBinding[originalMethod.thrownExceptions.length];

         for(int i = 0; i < originalMethod.thrownExceptions.length; ++i) {
            this.thrownExceptions[i] = (ReferenceBinding)this.getErasure18_5_2(originalMethod.thrownExceptions[i], environment, false);
         }
      } else {
         this.returnType = Scope.substitute(this, originalMethod.returnType);
         this.thrownExceptions = Scope.substitute(this, originalMethod.thrownExceptions);
      }

      if (this.thrownExceptions == null) {
         this.thrownExceptions = Binding.NO_EXCEPTIONS;
      }

      if ((this.tagBits & 128L) == 0L) {
         if ((this.returnType.tagBits & 128L) != 0L) {
            this.tagBits |= 128L;
         } else {
            label81: {
               int i = 0;

               for(int max = this.parameters.length; i < max; ++i) {
                  if ((this.parameters[i].tagBits & 128L) != 0L) {
                     this.tagBits |= 128L;
                     break label81;
                  }
               }

               i = 0;

               for(int max = this.thrownExceptions.length; i < max; ++i) {
                  if ((this.thrownExceptions[i].tagBits & 128L) != 0L) {
                     this.tagBits |= 128L;
                     break;
                  }
               }
            }
         }
      }

      this.wasInferred = true;
      this.parameterNonNullness = originalMethod.parameterNonNullness;
      this.defaultNullness = originalMethod.defaultNullness;
      int len = this.parameters.length;

      for(int i = 0; i < len; ++i) {
         if (this.parameters[i] == TypeBinding.NULL) {
            long nullBits = originalMethod.parameters[i].tagBits & 108086391056891904L;
            if (nullBits == 72057594037927936L) {
               if (this.parameterNonNullness == null) {
                  this.parameterNonNullness = new Boolean[len];
               }

               this.parameterNonNullness[i] = Boolean.TRUE;
            }
         }
      }
   }

   TypeBinding getErasure18_5_2(TypeBinding type, LookupEnvironment env, boolean substitute) {
      if (substitute) {
         type = Scope.substitute(this, type);
      }

      return env.convertToRawType(type.erasure(), true);
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(this.originalMethod.computeUniqueKey(false));
      buffer.append('%');
      buffer.append('<');
      if (!this.isRaw) {
         int length = this.typeArguments.length;

         for(int i = 0; i < length; ++i) {
            TypeBinding typeArgument = this.typeArguments[i];
            buffer.append(typeArgument.computeUniqueKey(false));
         }
      }

      buffer.append('>');
      int resultLength = buffer.length();
      char[] result = new char[resultLength];
      buffer.getChars(0, resultLength, result, 0);
      return result;
   }

   @Override
   public LookupEnvironment environment() {
      return this.environment;
   }

   @Override
   public boolean hasSubstitutedParameters() {
      return this.wasInferred ? this.originalMethod.hasSubstitutedParameters() : super.hasSubstitutedParameters();
   }

   @Override
   public boolean hasSubstitutedReturnType() {
      return this.inferredReturnType ? this.originalMethod.hasSubstitutedReturnType() : super.hasSubstitutedReturnType();
   }

   private ParameterizedGenericMethodBinding inferFromExpectedType(Scope scope, InferenceContext inferenceContext) {
      TypeVariableBinding[] originalVariables = this.originalMethod.typeVariables;
      int varLength = originalVariables.length;
      if (inferenceContext.expectedType != null) {
         this.returnType.collectSubstitutes(scope, inferenceContext.expectedType, inferenceContext, 2);
         if (inferenceContext.status == 1) {
            return null;
         }
      }

      for(int i = 0; i < varLength; ++i) {
         TypeVariableBinding originalVariable = originalVariables[i];
         TypeBinding argument = this.typeArguments[i];
         boolean argAlreadyInferred = TypeBinding.notEquals(argument, originalVariable);
         if (TypeBinding.equalsEquals(originalVariable.firstBound, originalVariable.superclass)) {
            TypeBinding substitutedBound = Scope.substitute(this, originalVariable.superclass);
            argument.collectSubstitutes(scope, substitutedBound, inferenceContext, 2);
            if (inferenceContext.status == 1) {
               return null;
            }

            if (argAlreadyInferred) {
               substitutedBound.collectSubstitutes(scope, argument, inferenceContext, 1);
               if (inferenceContext.status == 1) {
                  return null;
               }
            }
         }

         int j = 0;

         for(int max = originalVariable.superInterfaces.length; j < max; ++j) {
            TypeBinding substitutedBound = Scope.substitute(this, originalVariable.superInterfaces[j]);
            argument.collectSubstitutes(scope, substitutedBound, inferenceContext, 2);
            if (inferenceContext.status == 1) {
               return null;
            }

            if (argAlreadyInferred) {
               substitutedBound.collectSubstitutes(scope, argument, inferenceContext, 1);
               if (inferenceContext.status == 1) {
                  return null;
               }
            }
         }
      }

      if (!resolveSubstituteConstraints(scope, originalVariables, inferenceContext, true)) {
         return null;
      } else {
         for(int i = 0; i < varLength; ++i) {
            TypeBinding substitute = inferenceContext.substitutes[i];
            if (substitute != null) {
               this.typeArguments[i] = substitute;
            } else {
               this.typeArguments[i] = inferenceContext.substitutes[i] = originalVariables[i].upperBound();
            }
         }

         this.typeArguments = Scope.substitute(this, this.typeArguments);
         TypeBinding oldReturnType = this.returnType;
         this.returnType = Scope.substitute(this, this.returnType);
         this.inferredReturnType = inferenceContext.hasExplicitExpectedType && TypeBinding.notEquals(this.returnType, oldReturnType);
         this.parameters = Scope.substitute(this, this.parameters);
         this.thrownExceptions = Scope.substitute(this, this.thrownExceptions);
         if (this.thrownExceptions == null) {
            this.thrownExceptions = Binding.NO_EXCEPTIONS;
         }

         if ((this.tagBits & 128L) == 0L) {
            if ((this.returnType.tagBits & 128L) != 0L) {
               this.tagBits |= 128L;
            } else {
               int i = 0;

               for(int max = this.parameters.length; i < max; ++i) {
                  if ((this.parameters[i].tagBits & 128L) != 0L) {
                     this.tagBits |= 128L;
                     return this;
                  }
               }

               i = 0;

               for(int max = this.thrownExceptions.length; i < max; ++i) {
                  if ((this.thrownExceptions[i].tagBits & 128L) != 0L) {
                     this.tagBits |= 128L;
                     break;
                  }
               }
            }
         }

         return this;
      }
   }

   @Override
   public boolean isParameterizedGeneric() {
      return true;
   }

   @Override
   public boolean isRawSubstitution() {
      return this.isRaw;
   }

   @Override
   public TypeBinding substitute(TypeVariableBinding originalVariable) {
      TypeVariableBinding[] variables = this.originalMethod.typeVariables;
      int length = variables.length;
      if (originalVariable.rank < length && TypeBinding.equalsEquals(variables[originalVariable.rank], originalVariable)) {
         TypeBinding substitute = this.typeArguments[originalVariable.rank];
         return originalVariable.combineTypeAnnotations(substitute);
      } else {
         return originalVariable;
      }
   }

   @Override
   public MethodBinding tiebreakMethod() {
      if (this.tiebreakMethod == null) {
         this.tiebreakMethod = this.originalMethod.asRawMethod(this.environment);
      }

      return this.tiebreakMethod;
   }

   @Override
   public MethodBinding genericMethod() {
      return (MethodBinding)(this.isRaw ? this : this.originalMethod);
   }

   private static class LingeringTypeVariableEliminator implements Substitution {
      private final TypeVariableBinding[] variables;
      private final TypeBinding[] substitutes;
      private final Scope scope;

      public LingeringTypeVariableEliminator(TypeVariableBinding[] variables, TypeBinding[] substitutes, Scope scope) {
         this.variables = variables;
         this.substitutes = substitutes;
         this.scope = scope;
      }

      @Override
      public TypeBinding substitute(TypeVariableBinding typeVariable) {
         if (typeVariable.rank >= this.variables.length || TypeBinding.notEquals(this.variables[typeVariable.rank], typeVariable)) {
            return typeVariable;
         } else if (this.substitutes != null) {
            return Scope.substitute(
               new ParameterizedGenericMethodBinding.LingeringTypeVariableEliminator(this.variables, null, this.scope), this.substitutes[typeVariable.rank]
            );
         } else {
            ReferenceBinding genericType = (ReferenceBinding)(typeVariable.declaringElement instanceof ReferenceBinding ? typeVariable.declaringElement : null);
            return this.scope.environment().createWildcard(genericType, typeVariable.rank, null, null, 0, typeVariable.getTypeAnnotations());
         }
      }

      @Override
      public LookupEnvironment environment() {
         return this.scope.environment();
      }

      @Override
      public boolean isRawSubstitution() {
         return false;
      }
   }
}
