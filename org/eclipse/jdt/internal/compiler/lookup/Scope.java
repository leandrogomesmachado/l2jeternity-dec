package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExpressionContext;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public abstract class Scope {
   public static final int BLOCK_SCOPE = 1;
   public static final int CLASS_SCOPE = 3;
   public static final int COMPILATION_UNIT_SCOPE = 4;
   public static final int METHOD_SCOPE = 2;
   public static final int NOT_COMPATIBLE = -1;
   public static final int COMPATIBLE = 0;
   public static final int AUTOBOX_COMPATIBLE = 1;
   public static final int VARARGS_COMPATIBLE = 2;
   public static final int EQUAL_OR_MORE_SPECIFIC = -1;
   public static final int NOT_RELATED = 0;
   public static final int MORE_GENERIC = 1;
   public int kind;
   public Scope parent;
   private static Scope.Substitutor defaultSubstitutor = new Scope.Substitutor();

   protected Scope(int kind, Scope parent) {
      this.kind = kind;
      this.parent = parent;
   }

   public static int compareTypes(TypeBinding left, TypeBinding right) {
      if (left.isCompatibleWith(right)) {
         return -1;
      } else {
         return right.isCompatibleWith(left) ? 1 : 0;
      }
   }

   public static TypeBinding convertEliminatingTypeVariables(TypeBinding originalType, ReferenceBinding genericType, int rank, Set eliminatedVariables) {
      if ((originalType.tagBits & 536870912L) != 0L) {
         switch(originalType.kind()) {
            case 68:
               ArrayBinding originalArrayType = (ArrayBinding)originalType;
               TypeBinding originalLeafComponentType = originalArrayType.leafComponentType;
               TypeBinding substitute = convertEliminatingTypeVariables(originalLeafComponentType, genericType, rank, eliminatedVariables);
               if (TypeBinding.notEquals(substitute, originalLeafComponentType)) {
                  return originalArrayType.environment
                     .createArrayType(substitute.leafComponentType(), substitute.dimensions() + originalArrayType.dimensions());
               }
               break;
            case 260:
               ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)originalType;
               ReferenceBinding originalEnclosing = paramType.enclosingType();
               ReferenceBinding substitutedEnclosing = originalEnclosing;
               if (originalEnclosing != null) {
                  substitutedEnclosing = (ReferenceBinding)convertEliminatingTypeVariables(originalEnclosing, genericType, rank, eliminatedVariables);
               }

               TypeBinding[] originalArguments = paramType.arguments;
               TypeBinding[] substitutedArguments = originalArguments;
               int i = 0;
               int length = originalArguments == null ? 0 : originalArguments.length;

               for(; i < length; ++i) {
                  TypeBinding originalArgument = originalArguments[i];
                  TypeBinding substitutedArgument = convertEliminatingTypeVariables(originalArgument, paramType.genericType(), i, eliminatedVariables);
                  if (TypeBinding.notEquals(substitutedArgument, originalArgument)) {
                     if (substitutedArguments == originalArguments) {
                        System.arraycopy(originalArguments, 0, substitutedArguments = new TypeBinding[length], 0, i);
                     }

                     substitutedArguments[i] = substitutedArgument;
                  } else if (substitutedArguments != originalArguments) {
                     substitutedArguments[i] = originalArgument;
                  }
               }

               if (TypeBinding.notEquals(originalEnclosing, substitutedEnclosing) || originalArguments != substitutedArguments) {
                  return paramType.environment.createParameterizedType(paramType.genericType(), substitutedArguments, substitutedEnclosing);
               }
               break;
            case 516:
               WildcardBinding wildcard = (WildcardBinding)originalType;
               TypeBinding originalBound = wildcard.bound;
               if (originalBound != null) {
                  TypeBinding var38 = convertEliminatingTypeVariables(originalBound, genericType, rank, eliminatedVariables);
                  if (TypeBinding.notEquals(var38, originalBound)) {
                     return wildcard.environment.createWildcard(wildcard.genericType, wildcard.rank, var38, null, wildcard.boundKind);
                  }
               }
            case 1028:
            default:
               break;
            case 2052:
               ReferenceBinding currentType = (ReferenceBinding)originalType;
               ReferenceBinding originalEnclosing = currentType.enclosingType();
               ReferenceBinding substitutedEnclosing = originalEnclosing;
               if (originalEnclosing != null) {
                  substitutedEnclosing = (ReferenceBinding)convertEliminatingTypeVariables(originalEnclosing, genericType, rank, eliminatedVariables);
               }

               TypeBinding[] originalArguments = currentType.typeVariables();
               TypeBinding[] substitutedArguments = originalArguments;
               int i = 0;

               for(int length = originalArguments == null ? 0 : originalArguments.length; i < length; ++i) {
                  TypeBinding originalArgument = originalArguments[i];
                  TypeBinding substitutedArgument = convertEliminatingTypeVariables(originalArgument, currentType, i, eliminatedVariables);
                  if (TypeBinding.notEquals(substitutedArgument, originalArgument)) {
                     if (substitutedArguments == originalArguments) {
                        System.arraycopy(originalArguments, 0, substitutedArguments = new TypeBinding[length], 0, i);
                     }

                     substitutedArguments[i] = substitutedArgument;
                  } else if (substitutedArguments != originalArguments) {
                     substitutedArguments[i] = originalArgument;
                  }
               }

               if (TypeBinding.notEquals(originalEnclosing, substitutedEnclosing) || originalArguments != substitutedArguments) {
                  return ((TypeVariableBinding)originalArguments[0])
                     .environment
                     .createParameterizedType(genericType, substitutedArguments, substitutedEnclosing);
               }
               break;
            case 4100:
               if (genericType != null) {
                  TypeVariableBinding originalVariable = (TypeVariableBinding)originalType;
                  if (eliminatedVariables != null && eliminatedVariables.contains(originalType)) {
                     return originalVariable.environment.createWildcard(genericType, rank, null, null, 0);
                  }

                  TypeBinding originalUpperBound = originalVariable.upperBound();
                  if (eliminatedVariables == null) {
                     eliminatedVariables = new HashSet(2);
                  }

                  eliminatedVariables.add(originalVariable);
                  TypeBinding substitutedUpperBound = convertEliminatingTypeVariables(originalUpperBound, genericType, rank, eliminatedVariables);
                  eliminatedVariables.remove(originalVariable);
                  return originalVariable.environment.createWildcard(genericType, rank, substitutedUpperBound, null, 1);
               }
               break;
            case 8196:
               WildcardBinding intersection = (WildcardBinding)originalType;
               TypeBinding originalBound = intersection.bound;
               TypeBinding substitutedBound = originalBound;
               if (originalBound != null) {
                  substitutedBound = convertEliminatingTypeVariables(originalBound, genericType, rank, eliminatedVariables);
               }

               TypeBinding[] originalOtherBounds = intersection.otherBounds;
               TypeBinding[] substitutedOtherBounds = originalOtherBounds;
               int i = 0;

               for(int length = originalOtherBounds == null ? 0 : originalOtherBounds.length; i < length; ++i) {
                  TypeBinding originalOtherBound = originalOtherBounds[i];
                  TypeBinding substitutedOtherBound = convertEliminatingTypeVariables(originalOtherBound, genericType, rank, eliminatedVariables);
                  if (TypeBinding.notEquals(substitutedOtherBound, originalOtherBound)) {
                     if (substitutedOtherBounds == originalOtherBounds) {
                        System.arraycopy(originalOtherBounds, 0, substitutedOtherBounds = new TypeBinding[length], 0, i);
                     }

                     substitutedOtherBounds[i] = substitutedOtherBound;
                  } else if (substitutedOtherBounds != originalOtherBounds) {
                     substitutedOtherBounds[i] = originalOtherBound;
                  }
               }

               if (TypeBinding.notEquals(substitutedBound, originalBound) || substitutedOtherBounds != originalOtherBounds) {
                  return intersection.environment
                     .createWildcard(intersection.genericType, intersection.rank, substitutedBound, substitutedOtherBounds, intersection.boundKind);
               }
         }
      }

      return originalType;
   }

   public static TypeBinding getBaseType(char[] name) {
      int length = name.length;
      if (length > 2 && length < 8) {
         switch(name[0]) {
            case 'b':
               if (length == 7 && name[1] == 'o' && name[2] == 'o' && name[3] == 'l' && name[4] == 'e' && name[5] == 'a' && name[6] == 'n') {
                  return TypeBinding.BOOLEAN;
               }

               if (length == 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e') {
                  return TypeBinding.BYTE;
               }
               break;
            case 'c':
               if (length == 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r') {
                  return TypeBinding.CHAR;
               }
               break;
            case 'd':
               if (length == 6 && name[1] == 'o' && name[2] == 'u' && name[3] == 'b' && name[4] == 'l' && name[5] == 'e') {
                  return TypeBinding.DOUBLE;
               }
               break;
            case 'f':
               if (length == 5 && name[1] == 'l' && name[2] == 'o' && name[3] == 'a' && name[4] == 't') {
                  return TypeBinding.FLOAT;
               }
               break;
            case 'i':
               if (length == 3 && name[1] == 'n' && name[2] == 't') {
                  return TypeBinding.INT;
               }
               break;
            case 'l':
               if (length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
                  return TypeBinding.LONG;
               }
               break;
            case 's':
               if (length == 5 && name[1] == 'h' && name[2] == 'o' && name[3] == 'r' && name[4] == 't') {
                  return TypeBinding.SHORT;
               }
               break;
            case 'v':
               if (length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd') {
                  return TypeBinding.VOID;
               }
         }
      }

      return null;
   }

   public static ReferenceBinding[] greaterLowerBound(ReferenceBinding[] types) {
      if (types == null) {
         return null;
      } else {
         int length = types.length;
         if (length == 0) {
            return null;
         } else {
            ReferenceBinding[] result = types;
            int removed = 0;

            for(int i = 0; i < length; ++i) {
               ReferenceBinding iType = result[i];
               if (iType != null) {
                  for(int j = 0; j < length; ++j) {
                     if (i != j) {
                        ReferenceBinding jType = result[j];
                        if (jType != null && iType.isCompatibleWith(jType)) {
                           if (result == types) {
                              System.arraycopy(result, 0, result = new ReferenceBinding[length], 0, length);
                           }

                           result[j] = null;
                           ++removed;
                        }
                     }
                  }
               }
            }

            if (removed == 0) {
               return result;
            } else if (length == removed) {
               return null;
            } else {
               ReferenceBinding[] trimmedResult = new ReferenceBinding[length - removed];
               int i = 0;

               for(int index = 0; i < length; ++i) {
                  ReferenceBinding iType = result[i];
                  if (iType != null) {
                     trimmedResult[index++] = iType;
                  }
               }

               return trimmedResult;
            }
         }
      }
   }

   public static TypeBinding[] greaterLowerBound(TypeBinding[] types, Scope scope, LookupEnvironment environment) {
      if (types == null) {
         return null;
      } else {
         int length = types.length;
         if (length == 0) {
            return null;
         } else {
            TypeBinding[] result = types;
            int removed = 0;

            for(int i = 0; i < length; ++i) {
               TypeBinding iType = result[i];
               if (iType != null) {
                  for(int j = 0; j < length; ++j) {
                     if (i != j) {
                        TypeBinding jType = result[j];
                        if (jType != null) {
                           if (iType.isCompatibleWith(jType, scope)) {
                              if (result == types) {
                                 System.arraycopy(result, 0, result = new TypeBinding[length], 0, length);
                              }

                              result[j] = null;
                              ++removed;
                           } else if (!jType.isCompatibleWith(iType, scope) && iType.isParameterizedType() && jType.isParameterizedType()) {
                              ParameterizedTypeBinding wideType;
                              ParameterizedTypeBinding narrowType;
                              if (iType.original().isCompatibleWith(jType.original(), scope)) {
                                 wideType = (ParameterizedTypeBinding)jType;
                                 narrowType = (ParameterizedTypeBinding)iType;
                              } else {
                                 if (!jType.original().isCompatibleWith(iType.original(), scope)) {
                                    continue;
                                 }

                                 wideType = (ParameterizedTypeBinding)iType;
                                 narrowType = (ParameterizedTypeBinding)jType;
                              }

                              if (wideType.arguments != null && narrowType.isProperType(false) && wideType.isProperType(false)) {
                                 int numTypeArgs = wideType.arguments.length;
                                 TypeBinding[] bounds = new TypeBinding[numTypeArgs];

                                 for(int k = 0; k < numTypeArgs; ++k) {
                                    TypeBinding argument = wideType.arguments[k];
                                    bounds[k] = argument.isTypeVariable() ? ((TypeVariableBinding)argument).upperBound() : argument;
                                 }

                                 ReferenceBinding wideOriginal = (ReferenceBinding)wideType.original();
                                 TypeBinding substitutedWideType = environment.createParameterizedType(wideOriginal, bounds, wideOriginal.enclosingType());
                                 if (!narrowType.isCompatibleWith(substitutedWideType, scope)) {
                                    return null;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (removed == 0) {
               return result;
            } else if (length == removed) {
               return null;
            } else {
               TypeBinding[] trimmedResult = new TypeBinding[length - removed];
               int i = 0;

               for(int index = 0; i < length; ++i) {
                  TypeBinding iType = result[i];
                  if (iType != null) {
                     trimmedResult[index++] = iType;
                  }
               }

               return trimmedResult;
            }
         }
      }
   }

   public static ReferenceBinding[] substitute(Substitution substitution, ReferenceBinding[] originalTypes) {
      return defaultSubstitutor.substitute(substitution, originalTypes);
   }

   public static TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
      return defaultSubstitutor.substitute(substitution, originalType);
   }

   public static TypeBinding[] substitute(Substitution substitution, TypeBinding[] originalTypes) {
      return defaultSubstitutor.substitute(substitution, originalTypes);
   }

   public TypeBinding boxing(TypeBinding type) {
      return !type.isBaseType() && type.kind() != 65540 ? type : this.environment().computeBoxingType(type);
   }

   public final ClassScope classScope() {
      Scope scope = this;

      while(!(scope instanceof ClassScope)) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return (ClassScope)scope;
   }

   public final CompilationUnitScope compilationUnitScope() {
      Scope lastScope = null;
      Scope scope = this;

      do {
         lastScope = scope;
         scope = scope.parent;
      } while(scope != null);

      return (CompilationUnitScope)lastScope;
   }

   public boolean isLambdaScope() {
      return false;
   }

   public boolean isLambdaSubscope() {
      for(Scope scope = this; scope != null; scope = scope.parent) {
         switch(scope.kind) {
            case 1:
            case 2:
               return scope.isLambdaScope();
            default:
               return false;
         }
      }

      return false;
   }

   public final CompilerOptions compilerOptions() {
      return this.compilationUnitScope().environment.globalOptions;
   }

   protected final MethodBinding computeCompatibleMethod(MethodBinding method, TypeBinding[] arguments, InvocationSite invocationSite) {
      return this.computeCompatibleMethod(method, arguments, invocationSite, false);
   }

   protected final MethodBinding computeCompatibleMethod(
      MethodBinding method, TypeBinding[] arguments, InvocationSite invocationSite, boolean tiebreakingVarargsMethods
   ) {
      TypeBinding[] genericTypeArguments = invocationSite.genericTypeArguments();
      TypeBinding[] parameters = method.parameters;
      TypeVariableBinding[] typeVariables = method.typeVariables;
      if (parameters == arguments
         && (method.returnType.tagBits & 536870912L) == 0L
         && genericTypeArguments == null
         && typeVariables == Binding.NO_TYPE_VARIABLES) {
         return method;
      } else {
         int argLength = arguments.length;
         int paramLength = parameters.length;
         boolean isVarArgs = method.isVarargs();
         if (argLength == paramLength || isVarArgs && argLength >= paramLength - 1) {
            CompilerOptions compilerOptions = this.compilerOptions();
            if (typeVariables != Binding.NO_TYPE_VARIABLES && compilerOptions.sourceLevel >= 3211264L) {
               TypeBinding[] newArgs = null;
               if (compilerOptions.sourceLevel < 3407872L || genericTypeArguments != null) {
                  for(int i = 0; i < argLength; ++i) {
                     TypeBinding param = i < paramLength ? parameters[i] : parameters[paramLength - 1];
                     if (arguments[i].isBaseType() != param.isBaseType()) {
                        if (newArgs == null) {
                           newArgs = new TypeBinding[argLength];
                           System.arraycopy(arguments, 0, newArgs, 0, argLength);
                        }

                        newArgs[i] = this.environment().computeBoxingType(arguments[i]);
                     }
                  }
               }

               if (newArgs != null) {
                  arguments = newArgs;
               }

               method = ParameterizedGenericMethodBinding.computeCompatibleMethod(method, arguments, this, invocationSite);
               if (method == null) {
                  return null;
               }

               if (!method.isValidBinding()) {
                  return method;
               }

               if (compilerOptions.sourceLevel >= 3407872L && method instanceof ParameterizedGenericMethodBinding && invocationSite instanceof Invocation) {
                  Invocation invocation = (Invocation)invocationSite;
                  InferenceContext18 infCtx = invocation.getInferenceContext((ParameterizedGenericMethodBinding)method);
                  if (infCtx != null) {
                     return method;
                  }
               }
            } else if (genericTypeArguments != null && compilerOptions.complianceLevel < 3342336L) {
               if (method instanceof ParameterizedGenericMethodBinding) {
                  if (!((ParameterizedGenericMethodBinding)method).wasInferred) {
                     return new ProblemMethodBinding(method, method.selector, genericTypeArguments, 13);
                  }
               } else if (!method.isOverriding() || !this.isOverriddenMethodGeneric(method)) {
                  return new ProblemMethodBinding(method, method.selector, genericTypeArguments, 11);
               }
            } else if (typeVariables == Binding.NO_TYPE_VARIABLES
               && method instanceof ParameterizedGenericMethodBinding
               && compilerOptions.sourceLevel >= 3407872L
               && invocationSite instanceof Invocation) {
               Invocation invocation = (Invocation)invocationSite;
               InferenceContext18 infCtx = invocation.getInferenceContext((ParameterizedGenericMethodBinding)method);
               if (infCtx != null) {
                  return method;
               }
            }

            if (tiebreakingVarargsMethods && CompilerOptions.tolerateIllegalAmbiguousVarargsInvocation && compilerOptions.complianceLevel < 3342336L) {
               tiebreakingVarargsMethods = false;
            }

            if (this.parameterCompatibilityLevel(method, arguments, tiebreakingVarargsMethods) > -1) {
               return (MethodBinding)((method.tagBits & 4503599627370496L) != 0L ? this.environment().createPolymorphicMethod(method, arguments) : method);
            } else if (genericTypeArguments != null && typeVariables != Binding.NO_TYPE_VARIABLES) {
               return new ProblemMethodBinding(method, method.selector, arguments, 12);
            } else {
               return method instanceof PolyParameterizedGenericMethodBinding
                  ? new ProblemMethodBinding(method, method.selector, method.parameters, 27)
                  : null;
            }
         } else {
            return null;
         }
      }
   }

   protected boolean connectTypeVariables(TypeParameter[] typeParameters, boolean checkForErasedCandidateCollisions) {
      if (typeParameters != null && typeParameters.length != 0) {
         Map invocations = new HashMap(2);
         boolean noProblems = true;

         for(TypeParameter typeParameter : typeParameters) {
            TypeVariableBinding typeVariable = typeParameter.binding;
            if (typeVariable == null) {
               return false;
            }

            typeVariable.setSuperClass(this.getJavaLangObject());
            typeVariable.setSuperInterfaces(Binding.NO_SUPERINTERFACES);
            typeVariable.setFirstBound(null);
         }

         byte paramLength;
         for(int i = 0; i < paramLength; ++i) {
            TypeParameter typeParameter = typeParameters[i];
            TypeVariableBinding typeVariable = typeParameter.binding;
            TypeReference typeRef = typeParameter.type;
            if (typeRef != null) {
               boolean isFirstBoundTypeVariable = false;
               TypeBinding superType = this.kind == 2 ? typeRef.resolveType((BlockScope)this, false, 256) : typeRef.resolveType((ClassScope)this, 256);
               if (superType == null) {
                  typeVariable.tagBits |= 131072L;
               } else {
                  label189: {
                     typeRef.resolvedType = superType;
                     switch(superType.kind()) {
                        case 68:
                           this.problemReporter().boundCannotBeArray(typeRef, superType);
                           typeVariable.tagBits |= 131072L;
                           break label189;
                        case 4100:
                           isFirstBoundTypeVariable = true;
                           TypeVariableBinding varSuperType = (TypeVariableBinding)superType;
                           if (varSuperType.rank >= typeVariable.rank
                              && varSuperType.declaringElement == typeVariable.declaringElement
                              && this.compilerOptions().complianceLevel <= 3276800L) {
                              this.problemReporter().forwardTypeVariableReference(typeParameter, varSuperType);
                              typeVariable.tagBits |= 131072L;
                              break label189;
                           }

                           if (this.compilerOptions().complianceLevel > 3276800L
                              && typeVariable.rank >= varSuperType.rank
                              && varSuperType.declaringElement == typeVariable.declaringElement) {
                              SimpleSet set = new SimpleSet(typeParameters.length);
                              set.add(typeVariable);

                              for(ReferenceBinding superBinding = varSuperType;
                                 superBinding instanceof TypeVariableBinding;
                                 superBinding = ((TypeVariableBinding)superBinding).superclass
                              ) {
                                 if (set.includes(superBinding)) {
                                    this.problemReporter().hierarchyCircularity(typeVariable, varSuperType, typeRef);
                                    typeVariable.tagBits |= 131072L;
                                    break label189;
                                 }

                                 set.add(superBinding);
                              }
                           }
                           break;
                        default:
                           if (((ReferenceBinding)superType).isFinal()) {
                              this.problemReporter().finalVariableBound(typeVariable, typeRef);
                           }
                     }

                     ReferenceBinding superRefType = (ReferenceBinding)superType;
                     if (!superType.isInterface()) {
                        typeVariable.setSuperClass(superRefType);
                     } else {
                        typeVariable.setSuperInterfaces(new ReferenceBinding[]{superRefType});
                     }

                     typeVariable.tagBits |= superType.tagBits & 2048L;
                     typeVariable.setFirstBound(superRefType);
                  }
               }

               TypeReference[] boundRefs = typeParameter.bounds;
               if (boundRefs != null) {
                  int j = 0;

                  label149:
                  for(int boundLength = boundRefs.length; j < boundLength; ++j) {
                     typeRef = boundRefs[j];
                     superType = this.kind == 2 ? typeRef.resolveType((BlockScope)this, false) : typeRef.resolveType((ClassScope)this);
                     if (superType == null) {
                        typeVariable.tagBits |= 131072L;
                     } else {
                        typeVariable.tagBits |= superType.tagBits & 2048L;
                        boolean didAlreadyComplain = !typeRef.resolvedType.isValidBinding();
                        if (isFirstBoundTypeVariable && j == 0) {
                           this.problemReporter().noAdditionalBoundAfterTypeVariable(typeRef);
                           typeVariable.tagBits |= 131072L;
                           didAlreadyComplain = true;
                        } else {
                           if (superType.isArrayType()) {
                              if (!didAlreadyComplain) {
                                 this.problemReporter().boundCannotBeArray(typeRef, superType);
                                 typeVariable.tagBits |= 131072L;
                              }
                              continue;
                           }

                           if (!superType.isInterface()) {
                              if (!didAlreadyComplain) {
                                 this.problemReporter().boundMustBeAnInterface(typeRef, superType);
                                 typeVariable.tagBits |= 131072L;
                              }
                              continue;
                           }
                        }

                        if (!checkForErasedCandidateCollisions
                           || !TypeBinding.equalsEquals(typeVariable.firstBound, typeVariable.superclass)
                           || !this.hasErasedCandidatesCollisions(superType, typeVariable.superclass, invocations, typeVariable, typeRef)) {
                           ReferenceBinding superRefType = (ReferenceBinding)superType;
                           int index = typeVariable.superInterfaces.length;

                           while(--index >= 0) {
                              ReferenceBinding previousInterface = typeVariable.superInterfaces[index];
                              if (TypeBinding.equalsEquals(previousInterface, superRefType)) {
                                 this.problemReporter().duplicateBounds(typeRef, superType);
                                 typeVariable.tagBits |= 131072L;
                                 continue label149;
                              }

                              if (checkForErasedCandidateCollisions
                                 && this.hasErasedCandidatesCollisions(superType, previousInterface, invocations, typeVariable, typeRef)) {
                                 continue label149;
                              }
                           }

                           index = typeVariable.superInterfaces.length;
                           System.arraycopy(typeVariable.superInterfaces, 0, typeVariable.setSuperInterfaces(new ReferenceBinding[index + 1]), 0, index);
                           typeVariable.superInterfaces[index] = superRefType;
                        }
                     }
                  }
               }

               noProblems &= (typeVariable.tagBits & 131072L) == 0L;
            }
         }

         boolean declaresNullTypeAnnotation = false;

         for(int i = 0; i < paramLength; ++i) {
            this.resolveTypeParameter(typeParameters[i]);
            declaresNullTypeAnnotation |= typeParameters[i].binding.hasNullTypeAnnotations();
         }

         if (declaresNullTypeAnnotation) {
            for(int i = 0; i < paramLength; ++i) {
               typeParameters[i].binding.updateTagBits();
            }
         }

         return noProblems;
      } else {
         return true;
      }
   }

   public ArrayBinding createArrayType(TypeBinding type, int dimension) {
      return this.createArrayType(type, dimension, Binding.NO_ANNOTATIONS);
   }

   public ArrayBinding createArrayType(TypeBinding type, int dimension, AnnotationBinding[] annotations) {
      return type.isValidBinding() ? this.environment().createArrayType(type, dimension, annotations) : new ArrayBinding(type, dimension, this.environment());
   }

   public TypeVariableBinding[] createTypeVariables(TypeParameter[] typeParameters, Binding declaringElement) {
      if (typeParameters != null && typeParameters.length != 0) {
         PackageBinding unitPackage = this.compilationUnitScope().fPackage;
         int length = typeParameters.length;
         TypeVariableBinding[] typeVariableBindings = new TypeVariableBinding[length];
         int count = 0;

         for(int i = 0; i < length; ++i) {
            TypeParameter typeParameter = typeParameters[i];
            TypeVariableBinding parameterBinding = new TypeVariableBinding(typeParameter.name, declaringElement, i, this.environment());
            parameterBinding.fPackage = unitPackage;
            typeParameter.binding = parameterBinding;
            if ((typeParameter.bits & 1048576) != 0) {
               switch(declaringElement.kind()) {
                  case 4:
                     if (declaringElement instanceof SourceTypeBinding) {
                        SourceTypeBinding sourceTypeBinding = (SourceTypeBinding)declaringElement;
                        TypeDeclaration typeDeclaration = sourceTypeBinding.scope.referenceContext;
                        if (typeDeclaration != null) {
                           typeDeclaration.bits |= 1048576;
                        }
                     }
                  case 5:
                  case 6:
                  case 7:
                  default:
                     break;
                  case 8:
                     MethodBinding methodBinding = (MethodBinding)declaringElement;
                     AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
                     if (sourceMethod != null) {
                        sourceMethod.bits |= 1048576;
                     }
               }
            }

            for(int j = 0; j < count; ++j) {
               TypeVariableBinding knownVar = typeVariableBindings[j];
               if (CharOperation.equals(knownVar.sourceName, typeParameter.name)) {
                  this.problemReporter().duplicateTypeParameterInType(typeParameter);
               }
            }

            typeVariableBindings[count++] = parameterBinding;
         }

         if (count != length) {
            System.arraycopy(typeVariableBindings, 0, typeVariableBindings = new TypeVariableBinding[count], 0, count);
         }

         return typeVariableBindings;
      } else {
         return Binding.NO_TYPE_VARIABLES;
      }
   }

   void resolveTypeParameter(TypeParameter typeParameter) {
   }

   public final ClassScope enclosingClassScope() {
      Scope scope = this;

      while((scope = scope.parent) != null) {
         if (scope instanceof ClassScope) {
            return (ClassScope)scope;
         }
      }

      return null;
   }

   public final MethodScope enclosingMethodScope() {
      Scope scope = this;

      while((scope = scope.parent) != null) {
         if (scope instanceof MethodScope) {
            return (MethodScope)scope;
         }
      }

      return null;
   }

   public final MethodScope enclosingLambdaScope() {
      Scope scope = this;

      while((scope = scope.parent) != null) {
         if (scope instanceof MethodScope) {
            MethodScope methodScope = (MethodScope)scope;
            if (methodScope.referenceContext instanceof LambdaExpression) {
               return methodScope;
            }
         }
      }

      return null;
   }

   public final ReferenceBinding enclosingReceiverType() {
      Scope scope = this;

      while(!(scope instanceof ClassScope)) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return this.environment().convertToParameterizedType(((ClassScope)scope).referenceContext.binding);
   }

   public ReferenceContext enclosingReferenceContext() {
      Scope current = this;

      while((current = current.parent) != null) {
         switch(current.kind) {
            case 2:
               return ((MethodScope)current).referenceContext;
            case 3:
               return ((ClassScope)current).referenceContext;
            case 4:
               return ((CompilationUnitScope)current).referenceContext;
         }
      }

      return null;
   }

   public final SourceTypeBinding enclosingSourceType() {
      Scope scope = this;

      while(!(scope instanceof ClassScope)) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return ((ClassScope)scope).referenceContext.binding;
   }

   public final LookupEnvironment environment() {
      Scope unitScope = this;

      while(true) {
         Scope scope = unitScope.parent;
         if (unitScope.parent == null) {
            return ((CompilationUnitScope)unitScope).environment;
         }

         unitScope = scope;
      }
   }

   protected MethodBinding findDefaultAbstractMethod(
      ReferenceBinding receiverType,
      char[] selector,
      TypeBinding[] argumentTypes,
      InvocationSite invocationSite,
      ReferenceBinding classHierarchyStart,
      ObjectVector found,
      MethodBinding[] concreteMatches
   ) {
      int startFoundSize = found.size;
      boolean sourceLevel18 = this.compilerOptions().sourceLevel >= 3407872L;
      ReferenceBinding currentType = classHierarchyStart;

      for(List<TypeBinding> visitedTypes = new ArrayList<>(); currentType != null; currentType = currentType.superclass()) {
         this.findMethodInSuperInterfaces(currentType, selector, found, visitedTypes, invocationSite);
      }

      int candidatesCount = concreteMatches == null ? 0 : concreteMatches.length;
      int foundSize = found.size;
      MethodBinding[] candidates = new MethodBinding[foundSize - startFoundSize + candidatesCount];
      if (concreteMatches != null) {
         System.arraycopy(concreteMatches, 0, candidates, 0, candidatesCount);
      }

      MethodBinding problemMethod = null;
      if (foundSize > startFoundSize) {
         MethodVerifier methodVerifier = this.environment().methodVerifier();

         label95:
         for(int i = startFoundSize; i < foundSize; ++i) {
            MethodBinding methodBinding = (MethodBinding)found.elementAt(i);
            MethodBinding compatibleMethod = this.computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
            if (compatibleMethod != null) {
               if (!compatibleMethod.isValidBinding()) {
                  if (problemMethod == null) {
                     problemMethod = compatibleMethod;
                  }
               } else {
                  if (concreteMatches != null) {
                     int j = 0;

                     for(int length = concreteMatches.length; j < length; ++j) {
                        if (methodVerifier.areMethodsCompatible(concreteMatches[j], compatibleMethod)) {
                        }
                     }
                  }

                  if (sourceLevel18 || !compatibleMethod.isVarargs() || !(compatibleMethod instanceof ParameterizedGenericMethodBinding)) {
                     for(int j = 0; j < startFoundSize; ++j) {
                        MethodBinding classMethod = (MethodBinding)found.elementAt(j);
                        if (classMethod != null && methodVerifier.areMethodsCompatible(classMethod, compatibleMethod)) {
                           continue label95;
                        }
                     }
                  }

                  candidates[candidatesCount++] = compatibleMethod;
               }
            }
         }
      }

      MethodBinding concreteMatch = null;
      if (candidatesCount < 2) {
         if (concreteMatches == null && candidatesCount == 0) {
            return problemMethod;
         } else {
            concreteMatch = candidates[0];
            if (concreteMatch != null) {
               this.compilationUnitScope().recordTypeReferences(concreteMatch.thrownExceptions);
            }

            return concreteMatch;
         }
      } else {
         return this.compilerOptions().complianceLevel >= 3145728L
            ? this.mostSpecificMethodBinding(candidates, candidatesCount, argumentTypes, invocationSite, receiverType)
            : this.mostSpecificInterfaceMethodBinding(candidates, candidatesCount, invocationSite);
      }
   }

   public ReferenceBinding findDirectMemberType(char[] typeName, ReferenceBinding enclosingType) {
      if ((enclosingType.tagBits & 65536L) != 0L) {
         return null;
      } else {
         ReferenceBinding enclosingReceiverType = this.enclosingReceiverType();
         CompilationUnitScope unitScope = this.compilationUnitScope();
         unitScope.recordReference(enclosingType, typeName);
         ReferenceBinding memberType = enclosingType.getMemberType(typeName);
         if (memberType == null) {
            return null;
         } else {
            unitScope.recordTypeReference(memberType);
            if (enclosingReceiverType == null) {
               if (memberType.canBeSeenBy(this.getCurrentPackage())) {
                  return memberType;
               }

               if (this instanceof CompilationUnitScope) {
                  TypeDeclaration[] types = ((CompilationUnitScope)this).referenceContext.types;
                  if (types != null) {
                     int i = 0;

                     for(int max = types.length; i < max; ++i) {
                        if (memberType.canBeSeenBy(enclosingType, types[i].binding)) {
                           return memberType;
                        }
                     }
                  }
               }
            } else if (memberType.canBeSeenBy(enclosingType, enclosingReceiverType)) {
               return memberType;
            }

            return new ProblemReferenceBinding(new char[][]{typeName}, memberType, 2);
         }
      }
   }

   public MethodBinding findExactMethod(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordTypeReferences(argumentTypes);
      MethodBinding exactMethod = receiverType.getExactMethod(selector, argumentTypes, unitScope);
      if (exactMethod != null && exactMethod.typeVariables == Binding.NO_TYPE_VARIABLES && !exactMethod.isBridge()) {
         if (this.compilerOptions().sourceLevel >= 3211264L) {
            int i = argumentTypes.length;

            while(--i >= 0) {
               TypeBinding t = argumentTypes[i].leafComponentType();
               if (t instanceof ReferenceBinding) {
                  ReferenceBinding r = (ReferenceBinding)t;
                  if (r.isHierarchyConnected()) {
                     if (this.isSubtypeOfRawType(r)) {
                        return null;
                     }
                  } else if (r.isRawType()) {
                     return null;
                  }
               }
            }
         }

         unitScope.recordTypeReferences(exactMethod.thrownExceptions);
         if (exactMethod.isAbstract() && exactMethod.thrownExceptions != Binding.NO_EXCEPTIONS) {
            return null;
         }

         if (exactMethod.canBeSeenBy(receiverType, invocationSite, this)) {
            if (argumentTypes == Binding.NO_PARAMETERS
               && CharOperation.equals(selector, TypeConstants.GETCLASS)
               && exactMethod.returnType.isParameterizedType()) {
               return this.environment().createGetClassMethod(receiverType, exactMethod, this);
            }

            if (invocationSite.genericTypeArguments() != null) {
               exactMethod = this.computeCompatibleMethod(exactMethod, argumentTypes, invocationSite);
            } else if ((exactMethod.tagBits & 4503599627370496L) != 0L) {
               return this.environment().createPolymorphicMethod(exactMethod, argumentTypes);
            }

            return exactMethod;
         }
      }

      return null;
   }

   public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve) {
      return this.findField(receiverType, fieldName, invocationSite, needResolve, false);
   }

   public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve, boolean invisibleFieldsOk) {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordTypeReference(receiverType);
      TypeBinding leafType;
      switch(receiverType.kind()) {
         case 68:
            leafType = receiverType.leafComponentType();
            break;
         case 132:
            return null;
         case 516:
         case 4100:
         case 8196:
            TypeBinding receiverErasure = receiverType.erasure();
            if (receiverErasure.isArrayType()) {
               leafType = receiverErasure.leafComponentType();
               break;
            }
         default:
            ReferenceBinding currentType = (ReferenceBinding)receiverType;
            if (!currentType.canBeSeenBy(this)) {
               return new ProblemFieldBinding(currentType, fieldName, 8);
            }

            currentType.initializeForStaticImports();
            FieldBinding field = currentType.getField(fieldName, needResolve);
            boolean insideTypeAnnotations = this instanceof MethodScope && ((MethodScope)this).insideTypeAnnotation;
            if (field != null) {
               if (invisibleFieldsOk) {
                  return field;
               }

               if (invocationSite != null && !insideTypeAnnotations
                  ? !field.canBeSeenBy(currentType, invocationSite, this)
                  : !field.canBeSeenBy(this.getCurrentPackage())) {
                  return new ProblemFieldBinding(field, field.declaringClass, fieldName, 2);
               }

               return field;
            }

            ReferenceBinding[] interfacesToVisit = null;
            int nextPosition = 0;
            FieldBinding visibleField = null;
            boolean keepLooking = true;
            FieldBinding notVisibleField = null;

            while(keepLooking) {
               ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
               if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                  if (interfacesToVisit == null) {
                     interfacesToVisit = itsInterfaces;
                     nextPosition = itsInterfaces.length;
                  } else {
                     int itsLength = itsInterfaces.length;
                     if (nextPosition + itsLength >= interfacesToVisit.length) {
                        System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                     }

                     label188:
                     for(int a = 0; a < itsLength; ++a) {
                        ReferenceBinding next = itsInterfaces[a];

                        for(int b = 0; b < nextPosition; ++b) {
                           if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                              continue label188;
                           }
                        }

                        interfacesToVisit[nextPosition++] = next;
                     }
                  }
               }

               if ((currentType = currentType.superclass()) == null) {
                  break;
               }

               unitScope.recordTypeReference(currentType);
               currentType.initializeForStaticImports();
               currentType = (ReferenceBinding)currentType.capture(
                  this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd()
               );
               if ((field = currentType.getField(fieldName, needResolve)) != null) {
                  if (invisibleFieldsOk) {
                     return field;
                  }

                  keepLooking = false;
                  if (field.canBeSeenBy(receiverType, invocationSite, this)) {
                     if (visibleField != null) {
                        return new ProblemFieldBinding(visibleField, visibleField.declaringClass, fieldName, 3);
                     }

                     visibleField = field;
                  } else if (notVisibleField == null) {
                     notVisibleField = field;
                  }
               }
            }

            if (interfacesToVisit != null) {
               ProblemFieldBinding ambiguous = null;

               for(int i = 0; i < nextPosition; ++i) {
                  ReferenceBinding anInterface = interfacesToVisit[i];
                  unitScope.recordTypeReference(anInterface);
                  if ((field = anInterface.getField(fieldName, true)) != null) {
                     if (invisibleFieldsOk) {
                        return field;
                     }

                     if (visibleField != null) {
                        ambiguous = new ProblemFieldBinding(visibleField, visibleField.declaringClass, fieldName, 3);
                        break;
                     }

                     visibleField = field;
                  } else {
                     ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
                     if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                        int itsLength = itsInterfaces.length;
                        if (nextPosition + itsLength >= interfacesToVisit.length) {
                           System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                        }

                        label165:
                        for(int a = 0; a < itsLength; ++a) {
                           ReferenceBinding next = itsInterfaces[a];

                           for(int b = 0; b < nextPosition; ++b) {
                              if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                                 continue label165;
                              }
                           }

                           interfacesToVisit[nextPosition++] = next;
                        }
                     }
                  }
               }

               if (ambiguous != null) {
                  return ambiguous;
               }
            }

            if (visibleField != null) {
               return visibleField;
            }

            if (notVisibleField != null) {
               return new ProblemFieldBinding(notVisibleField, currentType, fieldName, 2);
            }

            return null;
      }

      if (leafType instanceof ReferenceBinding && !((ReferenceBinding)leafType).canBeSeenBy(this)) {
         return new ProblemFieldBinding((ReferenceBinding)leafType, fieldName, 8);
      } else if (!CharOperation.equals(fieldName, TypeConstants.LENGTH)) {
         return null;
      } else {
         return (FieldBinding)((leafType.tagBits & 128L) != 0L
            ? new ProblemFieldBinding(ArrayBinding.ArrayLength, null, fieldName, 1)
            : ArrayBinding.ArrayLength);
      }
   }

   public ReferenceBinding findMemberType(char[] typeName, ReferenceBinding enclosingType) {
      if ((enclosingType.tagBits & 65536L) != 0L) {
         return null;
      } else {
         ReferenceBinding enclosingSourceType = this.enclosingSourceType();
         PackageBinding currentPackage = this.getCurrentPackage();
         CompilationUnitScope unitScope = this.compilationUnitScope();
         unitScope.recordReference(enclosingType, typeName);
         ReferenceBinding memberType = enclosingType.getMemberType(typeName);
         if (memberType != null) {
            unitScope.recordTypeReference(memberType);
            return (ReferenceBinding)((
                  enclosingSourceType == null || this.parent == unitScope && (enclosingSourceType.tagBits & 262144L) == 0L
                     ? !memberType.canBeSeenBy(currentPackage)
                     : !memberType.canBeSeenBy(enclosingType, enclosingSourceType)
               )
               ? new ProblemReferenceBinding(new char[][]{typeName}, memberType, 2)
               : memberType);
         } else {
            ReferenceBinding currentType = enclosingType;
            ReferenceBinding[] interfacesToVisit = null;
            int nextPosition = 0;
            ReferenceBinding visibleMemberType = null;
            boolean keepLooking = true;
            ReferenceBinding notVisible = null;

            while(keepLooking) {
               ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
               if (itsInterfaces == null) {
                  ReferenceBinding sourceType = currentType.isParameterizedType() ? ((ParameterizedTypeBinding)currentType).genericType() : currentType;
                  if (sourceType.isHierarchyBeingConnected()) {
                     return null;
                  }

                  ((SourceTypeBinding)sourceType).scope.connectTypeHierarchy();
                  itsInterfaces = currentType.superInterfaces();
               }

               if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                  if (interfacesToVisit == null) {
                     interfacesToVisit = itsInterfaces;
                     nextPosition = itsInterfaces.length;
                  } else {
                     int itsLength = itsInterfaces.length;
                     if (nextPosition + itsLength >= interfacesToVisit.length) {
                        System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                     }

                     label167:
                     for(int a = 0; a < itsLength; ++a) {
                        ReferenceBinding next = itsInterfaces[a];

                        for(int b = 0; b < nextPosition; ++b) {
                           if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                              continue label167;
                           }
                        }

                        interfacesToVisit[nextPosition++] = next;
                     }
                  }
               }

               if ((currentType = currentType.superclass()) == null) {
                  break;
               }

               unitScope.recordReference(currentType, typeName);
               if ((memberType = currentType.getMemberType(typeName)) != null) {
                  unitScope.recordTypeReference(memberType);
                  keepLooking = false;
                  if (enclosingSourceType == null ? memberType.canBeSeenBy(currentPackage) : memberType.canBeSeenBy(enclosingType, enclosingSourceType)) {
                     if (visibleMemberType != null) {
                        return new ProblemReferenceBinding(new char[][]{typeName}, visibleMemberType, 3);
                     }

                     visibleMemberType = memberType;
                  } else {
                     notVisible = memberType;
                  }
               }
            }

            if (interfacesToVisit != null) {
               ProblemReferenceBinding ambiguous = null;

               for(int i = 0; i < nextPosition; ++i) {
                  ReferenceBinding anInterface = interfacesToVisit[i];
                  unitScope.recordReference(anInterface, typeName);
                  if ((memberType = anInterface.getMemberType(typeName)) != null) {
                     unitScope.recordTypeReference(memberType);
                     if (visibleMemberType != null) {
                        ambiguous = new ProblemReferenceBinding(new char[][]{typeName}, visibleMemberType, 3);
                        break;
                     }

                     visibleMemberType = memberType;
                  } else {
                     ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
                     if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                        int itsLength = itsInterfaces.length;
                        if (nextPosition + itsLength >= interfacesToVisit.length) {
                           System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                        }

                        label134:
                        for(int a = 0; a < itsLength; ++a) {
                           ReferenceBinding next = itsInterfaces[a];

                           for(int b = 0; b < nextPosition; ++b) {
                              if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                                 continue label134;
                              }
                           }

                           interfacesToVisit[nextPosition++] = next;
                        }
                     }
                  }
               }

               if (ambiguous != null) {
                  return ambiguous;
               }
            }

            if (visibleMemberType != null) {
               return visibleMemberType;
            } else {
               return notVisible != null ? new ProblemReferenceBinding(new char[][]{typeName}, notVisible, 2) : null;
            }
         }
      }
   }

   public MethodBinding findMethod(
      ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite, boolean inStaticContext
   ) {
      MethodBinding method = this.findMethod0(receiverType, selector, argumentTypes, invocationSite, inStaticContext);
      if (method != null && method.isValidBinding() && method.isVarargs()) {
         TypeBinding elementType = method.parameters[method.parameters.length - 1].leafComponentType();
         if (elementType instanceof ReferenceBinding && !((ReferenceBinding)elementType).canBeSeenBy(this)) {
            return new ProblemMethodBinding(method, method.selector, invocationSite.genericTypeArguments(), 16);
         }
      }

      return method;
   }

   public MethodBinding findMethod0(
      ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite, boolean inStaticContext
   ) {
      ReferenceBinding currentType = receiverType;
      boolean receiverTypeIsInterface = receiverType.isInterface();
      ObjectVector found = new ObjectVector(3);
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordTypeReferences(argumentTypes);
      List<TypeBinding> visitedTypes = new ArrayList<>();
      if (receiverTypeIsInterface) {
         unitScope.recordTypeReference(receiverType);
         MethodBinding[] receiverMethods = receiverType.getMethods(selector, argumentTypes.length);
         if (receiverMethods.length > 0) {
            found.addAll(receiverMethods);
         }

         this.findMethodInSuperInterfaces(receiverType, selector, found, visitedTypes, invocationSite);
         currentType = this.getJavaLangObject();
      }

      long complianceLevel = this.compilerOptions().complianceLevel;
      boolean isCompliant14 = complianceLevel >= 3145728L;
      boolean isCompliant15 = complianceLevel >= 3211264L;
      boolean soureLevel18 = this.compilerOptions().sourceLevel >= 3407872L;
      ReferenceBinding classHierarchyStart = currentType;

      MethodVerifier verifier;
      for(verifier = this.environment().methodVerifier(); currentType != null; currentType = currentType.superclass()) {
         unitScope.recordTypeReference(currentType);
         currentType = (ReferenceBinding)currentType.capture(
            this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd()
         );
         MethodBinding[] currentMethods = currentType.getMethods(selector, argumentTypes.length);
         int currentLength = currentMethods.length;
         if (currentLength > 0) {
            if (isCompliant14 && (receiverTypeIsInterface || found.size > 0)) {
               int i = 0;

               for(int l = currentLength; i < l; ++i) {
                  MethodBinding currentMethod = currentMethods[i];
                  if (currentMethod != null) {
                     if (receiverTypeIsInterface && !currentMethod.isPublic()) {
                        --currentLength;
                        currentMethods[i] = null;
                     } else {
                        int j = 0;

                        for(int max = found.size; j < max; ++j) {
                           MethodBinding matchingMethod = (MethodBinding)found.elementAt(j);
                           MethodBinding matchingOriginal = matchingMethod.original();
                           MethodBinding currentOriginal = matchingOriginal.findOriginalInheritedMethod(currentMethod);
                           if (currentOriginal != null && verifier.isParameterSubsignature(matchingOriginal, currentOriginal)) {
                              if (!isCompliant15 || !matchingMethod.isBridge() || currentMethod.isBridge()) {
                                 --currentLength;
                                 currentMethods[i] = null;
                              }
                              break;
                           }
                        }
                     }
                  }
               }
            }

            if (currentLength > 0) {
               if (currentMethods.length == currentLength) {
                  found.addAll(currentMethods);
               } else {
                  int i = 0;

                  for(int max = currentMethods.length; i < max; ++i) {
                     MethodBinding currentMethod = currentMethods[i];
                     if (currentMethod != null) {
                        found.add(currentMethod);
                     }
                  }
               }
            }
         }
      }

      int foundSize = found.size;
      MethodBinding[] candidates = null;
      int candidatesCount = 0;
      MethodBinding problemMethod = null;
      boolean searchForDefaultAbstractMethod = soureLevel18
         || isCompliant14 && !receiverTypeIsInterface && (receiverType.isAbstract() || receiverType.isTypeVariable());
      if (foundSize > 0) {
         for(int i = 0; i < foundSize; ++i) {
            MethodBinding methodBinding = (MethodBinding)found.elementAt(i);
            MethodBinding compatibleMethod = this.computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
            if (compatibleMethod != null) {
               if (!compatibleMethod.isValidBinding() && compatibleMethod.problemId() != 23) {
                  if (problemMethod == null) {
                     problemMethod = compatibleMethod;
                  }
               } else {
                  if (foundSize == 1 && compatibleMethod.canBeSeenBy(receiverType, invocationSite, this)) {
                     if (searchForDefaultAbstractMethod) {
                        return this.findDefaultAbstractMethod(
                           receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, new MethodBinding[]{compatibleMethod}
                        );
                     }

                     unitScope.recordTypeReferences(compatibleMethod.thrownExceptions);
                     return compatibleMethod;
                  }

                  if (candidatesCount == 0) {
                     candidates = new MethodBinding[foundSize];
                  }

                  candidates[candidatesCount++] = compatibleMethod;
               }
            }
         }
      }

      if (candidatesCount == 0) {
         if (problemMethod != null) {
            switch(problemMethod.problemId()) {
               case 11:
               case 13:
                  return problemMethod;
               case 12:
            }
         }

         MethodBinding interfaceMethod = this.findDefaultAbstractMethod(
            receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, null
         );
         if (interfaceMethod != null) {
            if (soureLevel18 && foundSize > 0 && interfaceMethod.isVarargs() && interfaceMethod instanceof ParameterizedGenericMethodBinding) {
               MethodBinding original = interfaceMethod.original();

               for(int i = 0; i < foundSize; ++i) {
                  MethodBinding classMethod = (MethodBinding)found.elementAt(i);
                  if (!classMethod.isAbstract()) {
                     MethodBinding substitute = verifier.computeSubstituteMethod(original, classMethod);
                     if (substitute != null && verifier.isSubstituteParameterSubsignature(classMethod, substitute)) {
                        return new ProblemMethodBinding(interfaceMethod, selector, argumentTypes, 24);
                     }
                  }
               }
            }

            return interfaceMethod;
         } else if (found.size == 0) {
            return null;
         } else if (problemMethod != null) {
            return problemMethod;
         } else {
            int bestArgMatches = -1;
            MethodBinding bestGuess = (MethodBinding)found.elementAt(0);
            int argLength = argumentTypes.length;
            foundSize = found.size;

            for(int i = 0; i < foundSize; ++i) {
               MethodBinding methodBinding = (MethodBinding)found.elementAt(i);
               TypeBinding[] params = methodBinding.parameters;
               int paramLength = params.length;
               int argMatches = 0;

               for(int a = 0; a < argLength; ++a) {
                  TypeBinding arg = argumentTypes[a];

                  for(int p = a == 0 ? 0 : a - 1; p < paramLength && p < a + 1; ++p) {
                     if (TypeBinding.equalsEquals(params[p], arg)) {
                        ++argMatches;
                        break;
                     }
                  }
               }

               if (argMatches >= bestArgMatches) {
                  if (argMatches == bestArgMatches) {
                     int diff1 = paramLength < argLength ? 2 * (argLength - paramLength) : paramLength - argLength;
                     int bestLength = bestGuess.parameters.length;
                     int diff2 = bestLength < argLength ? 2 * (argLength - bestLength) : bestLength - argLength;
                     if (diff1 >= diff2) {
                        continue;
                     }
                  }

                  if (bestGuess == methodBinding || !MethodVerifier.doesMethodOverride(bestGuess, methodBinding, this.environment())) {
                     bestArgMatches = argMatches;
                     bestGuess = methodBinding;
                  }
               }
            }

            return new ProblemMethodBinding(bestGuess, bestGuess.selector, argumentTypes, 1);
         }
      } else {
         int visiblesCount = 0;

         for(int i = 0; i < candidatesCount; ++i) {
            MethodBinding methodBinding = candidates[i];
            if (methodBinding.canBeSeenBy(receiverType, invocationSite, this)) {
               if (visiblesCount != i) {
                  candidates[i] = null;
                  candidates[visiblesCount] = methodBinding;
               }

               ++visiblesCount;
            }
         }

         switch(visiblesCount) {
            case 0:
               MethodBinding interfaceMethod = this.findDefaultAbstractMethod(
                  receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, null
               );
               if (interfaceMethod != null) {
                  return interfaceMethod;
               }

               MethodBinding candidate = candidates[0];
               int reason = 2;
               if (candidate.isStatic() && candidate.declaringClass.isInterface()) {
                  if (soureLevel18) {
                     reason = 20;
                  } else {
                     reason = 29;
                  }
               }

               return new ProblemMethodBinding(candidate, candidate.selector, candidate.parameters, reason);
            case 1:
               if (searchForDefaultAbstractMethod) {
                  return this.findDefaultAbstractMethod(
                     receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, new MethodBinding[]{candidates[0]}
                  );
               }

               MethodBinding candidate = candidates[0];
               if (candidate != null) {
                  unitScope.recordTypeReferences(candidate.thrownExceptions);
               }

               return candidate;
            default:
               if (complianceLevel <= 3080192L) {
                  ReferenceBinding declaringClass = candidates[0].declaringClass;
                  return !declaringClass.isInterface()
                     ? this.mostSpecificClassMethodBinding(candidates, visiblesCount, invocationSite)
                     : this.mostSpecificInterfaceMethodBinding(candidates, visiblesCount, invocationSite);
               } else {
                  if (this.compilerOptions().sourceLevel >= 3211264L) {
                     for(int i = 0; i < visiblesCount; ++i) {
                        MethodBinding candidate = candidates[i];
                        if (candidate.isParameterizedGeneric()) {
                           candidate = candidate.shallowOriginal();
                        }

                        if (candidate.hasSubstitutedParameters()) {
                           for(int j = i + 1; j < visiblesCount; ++j) {
                              MethodBinding otherCandidate = candidates[j];
                              if (otherCandidate.hasSubstitutedParameters()
                                 && (
                                    otherCandidate == candidate
                                       || TypeBinding.equalsEquals(candidate.declaringClass, otherCandidate.declaringClass)
                                          && candidate.areParametersEqual(otherCandidate)
                                 )) {
                                 return new ProblemMethodBinding(candidates[i], candidates[i].selector, candidates[i].parameters, 3);
                              }
                           }
                        }
                     }
                  }

                  if (inStaticContext) {
                     MethodBinding[] staticCandidates = new MethodBinding[visiblesCount];
                     int staticCount = 0;

                     for(int i = 0; i < visiblesCount; ++i) {
                        if (candidates[i].isStatic()) {
                           staticCandidates[staticCount++] = candidates[i];
                        }
                     }

                     if (staticCount == 1) {
                        return staticCandidates[0];
                     }

                     if (staticCount > 1) {
                        return this.mostSpecificMethodBinding(staticCandidates, staticCount, argumentTypes, invocationSite, receiverType);
                     }
                  }

                  if (visiblesCount != candidates.length) {
                     System.arraycopy(candidates, 0, candidates = new MethodBinding[visiblesCount], 0, visiblesCount);
                  }

                  return searchForDefaultAbstractMethod
                     ? this.findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, candidates)
                     : this.mostSpecificMethodBinding(candidates, visiblesCount, argumentTypes, invocationSite, receiverType);
               }
         }
      }
   }

   public MethodBinding findMethodForArray(ArrayBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      TypeBinding leafType = receiverType.leafComponentType();
      if (leafType instanceof ReferenceBinding && !((ReferenceBinding)leafType).canBeSeenBy(this)) {
         return new ProblemMethodBinding(selector, Binding.NO_PARAMETERS, (ReferenceBinding)leafType, 8);
      } else {
         ReferenceBinding object = this.getJavaLangObject();
         MethodBinding methodBinding = object.getExactMethod(selector, argumentTypes, null);
         if (methodBinding != null) {
            if (argumentTypes == Binding.NO_PARAMETERS) {
               switch(selector[0]) {
                  case 'c':
                     if (CharOperation.equals(selector, TypeConstants.CLONE)) {
                        return this.environment().computeArrayClone(methodBinding);
                     }
                  case 'd':
                  case 'e':
                  case 'f':
                  default:
                     break;
                  case 'g':
                     if (CharOperation.equals(selector, TypeConstants.GETCLASS) && methodBinding.returnType.isParameterizedType()) {
                        return this.environment().createGetClassMethod(receiverType, methodBinding, this);
                     }
               }
            }

            if (methodBinding.canBeSeenBy(receiverType, invocationSite, this)) {
               return methodBinding;
            }
         }

         methodBinding = this.findMethod(object, selector, argumentTypes, invocationSite, false);
         return (MethodBinding)(methodBinding == null ? new ProblemMethodBinding(selector, argumentTypes, 26) : methodBinding);
      }
   }

   protected void findMethodInSuperInterfaces(
      ReferenceBinding receiverType, char[] selector, ObjectVector found, List<TypeBinding> visitedTypes, InvocationSite invocationSite
   ) {
      ReferenceBinding[] itsInterfaces = receiverType.superInterfaces();
      if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
         label104:
         for(ReferenceBinding currentType : itsInterfaces) {
            if (visitedTypes != null) {
               TypeBinding uncaptured = currentType.uncapture(this);

               for(TypeBinding visited : visitedTypes) {
                  if (uncaptured.isEquivalentTo(visited)) {
                     continue label104;
                  }
               }

               visitedTypes.add(uncaptured);
            }

            this.compilationUnitScope().recordTypeReference(currentType);
            currentType = (ReferenceBinding)currentType.capture(
               this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd()
            );
            MethodBinding[] currentMethods = currentType.getMethods(selector);
            if (currentMethods.length > 0) {
               int foundSize = found.size;
               int c = 0;

               label89:
               for(int l = currentMethods.length; c < l; ++c) {
                  MethodBinding current = currentMethods[c];
                  if (current.canBeSeenBy(receiverType, invocationSite, this)) {
                     if (foundSize > 0) {
                        for(int f = 0; f < foundSize; ++f) {
                           if (current == found.elementAt(f)) {
                              continue label89;
                           }
                        }
                     }

                     found.add(current);
                  }
               }
            }

            if ((itsInterfaces = currentType.superInterfaces()) != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               int itsLength = itsInterfaces.length;
               ReferenceBinding[] interfacesToVisit;
               int nextPosition;
               if (nextPosition + itsLength >= interfacesToVisit.length) {
                  System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
               }

               label71:
               for(int a = 0; a < itsLength; ++a) {
                  ReferenceBinding next = itsInterfaces[a];

                  for(int b = 0; b < nextPosition; ++b) {
                     if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                        continue label71;
                     }
                  }

                  interfacesToVisit[nextPosition++] = next;
               }
            }
         }
      }
   }

   public ReferenceBinding findType(char[] typeName, PackageBinding declarationPackage, PackageBinding invocationPackage) {
      this.compilationUnitScope().recordReference(declarationPackage.compoundName, typeName);
      ReferenceBinding typeBinding = declarationPackage.getType(typeName);
      if (typeBinding == null) {
         return null;
      } else {
         return (ReferenceBinding)(typeBinding.isValidBinding() && declarationPackage != invocationPackage && !typeBinding.canBeSeenBy(invocationPackage)
            ? new ProblemReferenceBinding(new char[][]{typeName}, typeBinding, 2)
            : typeBinding);
      }
   }

   public LocalVariableBinding findVariable(char[] variable) {
      return null;
   }

   public Binding getBinding(char[] name, int mask, InvocationSite invocationSite, boolean needResolve) {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      LookupEnvironment env = unitScope.environment;

      try {
         env.missingClassFileLocation = invocationSite;
         Binding binding = null;
         FieldBinding problemField = null;
         if ((mask & 3) != 0) {
            boolean insideStaticContext = false;
            boolean insideConstructorCall = false;
            boolean insideTypeAnnotation = false;
            FieldBinding foundField = null;
            ProblemFieldBinding foundInsideProblem = null;
            Scope scope = this;
            MethodScope methodScope = null;
            int depth = 0;
            int foundDepth = 0;
            boolean shouldTrackOuterLocals = false;
            ReferenceBinding foundActualReceiverType = null;

            label511:
            while(true) {
               switch(scope.kind) {
                  case 2:
                     methodScope = (MethodScope)scope;
                     insideStaticContext |= methodScope.isStatic;
                     insideConstructorCall |= methodScope.isConstructorCall;
                     insideTypeAnnotation = methodScope.insideTypeAnnotation;
                  case 1:
                     LocalVariableBinding variableBinding = scope.findVariable(name);
                     if (variableBinding != null) {
                        if (foundField != null && foundField.isValidBinding()) {
                           return new ProblemFieldBinding(foundField, foundField.declaringClass, name, 5);
                        }

                        if (depth > 0) {
                           invocationSite.setDepth(depth);
                        }

                        if (shouldTrackOuterLocals) {
                           if (invocationSite instanceof NameReference) {
                              NameReference nameReference = (NameReference)invocationSite;
                              nameReference.bits |= 524288;
                           } else if (invocationSite instanceof AbstractVariableDeclaration) {
                              AbstractVariableDeclaration variableDeclaration = (AbstractVariableDeclaration)invocationSite;
                              variableDeclaration.bits |= 2097152;
                           }
                        }

                        return variableBinding;
                     }
                     break;
                  case 3:
                     ClassScope classScope = (ClassScope)scope;
                     ReferenceBinding receiverType = classScope.enclosingReceiverType();
                     if (!insideTypeAnnotation) {
                        FieldBinding fieldBinding = classScope.findField(receiverType, name, invocationSite, needResolve);
                        if (fieldBinding != null) {
                           if (fieldBinding.problemId() == 3) {
                              if (foundField != null && foundField.problemId() != 2) {
                                 return new ProblemFieldBinding(foundField, foundField.declaringClass, name, 5);
                              }

                              return fieldBinding;
                           }

                           ProblemFieldBinding insideProblem = null;
                           if (fieldBinding.isValidBinding()) {
                              if (!fieldBinding.isStatic()) {
                                 if (insideConstructorCall) {
                                    insideProblem = new ProblemFieldBinding(fieldBinding, fieldBinding.declaringClass, name, 6);
                                 } else if (insideStaticContext) {
                                    insideProblem = new ProblemFieldBinding(fieldBinding, fieldBinding.declaringClass, name, 7);
                                 }
                              }

                              if (TypeBinding.equalsEquals(receiverType, fieldBinding.declaringClass) || this.compilerOptions().complianceLevel >= 3145728L) {
                                 if (foundField == null) {
                                    if (depth > 0) {
                                       invocationSite.setDepth(depth);
                                       invocationSite.setActualReceiverType(receiverType);
                                    }

                                    return (Binding)(insideProblem == null ? fieldBinding : insideProblem);
                                 }

                                 if (foundField.isValidBinding()
                                    && TypeBinding.notEquals(foundField.declaringClass, fieldBinding.declaringClass)
                                    && TypeBinding.notEquals(foundField.declaringClass, foundActualReceiverType)) {
                                    return new ProblemFieldBinding(foundField, foundField.declaringClass, name, 5);
                                 }
                              }
                           }

                           if (foundField == null || foundField.problemId() == 2 && fieldBinding.problemId() != 2) {
                              foundDepth = depth;
                              foundActualReceiverType = receiverType;
                              foundInsideProblem = insideProblem;
                              foundField = fieldBinding;
                           }
                        }
                     }

                     insideTypeAnnotation = false;
                     ++depth;
                     shouldTrackOuterLocals = true;
                     insideStaticContext |= receiverType.isStatic();
                     MethodScope enclosingMethodScope = scope.methodScope();
                     insideConstructorCall = enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;
                     break;
                  case 4:
                     if (foundInsideProblem != null) {
                        return foundInsideProblem;
                     }

                     if (foundField != null) {
                        if (foundField.isValidBinding()) {
                           if (foundDepth > 0) {
                              invocationSite.setDepth(foundDepth);
                              invocationSite.setActualReceiverType(foundActualReceiverType);
                           }

                           return foundField;
                        }

                        problemField = foundField;
                        foundField = null;
                     }

                     if (this.compilerOptions().sourceLevel >= 3211264L) {
                        unitScope.faultInImports();
                        ImportBinding[] imports = unitScope.imports;
                        if (imports != null) {
                           int i = 0;

                           for(int length = imports.length; i < length; ++i) {
                              ImportBinding importBinding = imports[i];
                              if (importBinding.isStatic()
                                 && !importBinding.onDemand
                                 && CharOperation.equals(importBinding.compoundName[importBinding.compoundName.length - 1], name)
                                 && unitScope.resolveSingleImport(importBinding, 13) != null
                                 && importBinding.resolvedImport instanceof FieldBinding) {
                                 foundField = (FieldBinding)importBinding.resolvedImport;
                                 ImportReference importReference = importBinding.reference;
                                 if (importReference != null && needResolve) {
                                    importReference.bits |= 2;
                                 }

                                 invocationSite.setActualReceiverType(foundField.declaringClass);
                                 if (foundField.isValidBinding()) {
                                    return foundField;
                                 }

                                 if (problemField == null) {
                                    problemField = foundField;
                                 }
                              }
                           }

                           boolean foundInImport = false;
                           int ix = 0;

                           for(int length = imports.length; ix < length; ++ix) {
                              ImportBinding importBinding = imports[ix];
                              if (importBinding.isStatic() && importBinding.onDemand) {
                                 Binding resolvedImport = importBinding.resolvedImport;
                                 if (resolvedImport instanceof ReferenceBinding) {
                                    FieldBinding temp = this.findField((ReferenceBinding)resolvedImport, name, invocationSite, needResolve);
                                    if (temp != null) {
                                       if (!temp.isValidBinding()) {
                                          if (problemField == null) {
                                             problemField = temp;
                                          }
                                       } else if (temp.isStatic() && foundField != temp) {
                                          ImportReference importReference = importBinding.reference;
                                          if (importReference != null && needResolve) {
                                             importReference.bits |= 2;
                                          }

                                          if (foundInImport) {
                                             return new ProblemFieldBinding(foundField, foundField.declaringClass, name, 3);
                                          }

                                          foundField = temp;
                                          foundInImport = true;
                                       }
                                    }
                                 }
                              }
                           }

                           if (foundField != null) {
                              invocationSite.setActualReceiverType(foundField.declaringClass);
                              return foundField;
                           }
                        }
                     }
                     break label511;
               }

               if (scope.isLambdaScope()) {
                  shouldTrackOuterLocals = true;
               }

               scope = scope.parent;
            }
         }

         if ((mask & 4) != 0) {
            if ((binding = getBaseType(name)) != null) {
               return binding;
            }

            binding = this.getTypeOrPackage(name, (mask & 16) == 0 ? 4 : 20, needResolve);
            if (binding.isValidBinding() || mask == 4) {
               return binding;
            }
         } else if ((mask & 16) != 0) {
            unitScope.recordSimpleReference(name);
            if ((binding = env.getTopLevelPackage(name)) != null) {
               return binding;
            }
         }

         if (problemField != null) {
            return problemField;
         } else {
            return (Binding)(binding != null && binding.problemId() != 1 ? binding : new ProblemBinding(name, this.enclosingSourceType(), 1));
         }
      } catch (AbortCompilation var32) {
         var32.updateContext(invocationSite, this.referenceCompilationUnit().compilationResult);
         throw var32;
      } finally {
         env.missingClassFileLocation = null;
      }
   }

   private MethodBinding getExactMethod(TypeBinding receiverType, TypeBinding type, char[] selector, InvocationSite invocationSite, MethodBinding candidate) {
      if (type == null) {
         return null;
      } else {
         TypeBinding[] superInterfaces = type.superInterfaces();
         TypeBinding[] typePlusSupertypes = new TypeBinding[2 + superInterfaces.length];
         typePlusSupertypes[0] = type;
         typePlusSupertypes[1] = type.superclass();
         if (superInterfaces.length != 0) {
            System.arraycopy(superInterfaces, 0, typePlusSupertypes, 2, superInterfaces.length);
         }

         CompilationUnitScope unitScope = this.compilationUnitScope();
         unitScope.recordTypeReference(type);
         type = type.capture(this, invocationSite.sourceStart(), invocationSite.sourceEnd());
         int i = 0;

         for(int typesLength = typePlusSupertypes.length; i < typesLength; ++i) {
            MethodBinding[] methods = i == 0
               ? type.getMethods(selector)
               : new MethodBinding[]{this.getExactMethod(receiverType, typePlusSupertypes[i], selector, invocationSite, candidate)};
            int j = 0;

            for(int length = methods.length; j < length; ++j) {
               MethodBinding currentMethod = methods[j];
               if (currentMethod != null
                  && candidate != currentMethod
                  && (i != 0 || currentMethod.canBeSeenBy(receiverType, invocationSite, this) && !currentMethod.isSynthetic() && !currentMethod.isBridge())) {
                  if (candidate != null) {
                     if (!candidate.areParameterErasuresEqual(currentMethod)) {
                        throw new Scope.MethodClashException();
                     }
                  } else {
                     candidate = currentMethod;
                  }
               }
            }
         }

         return candidate;
      }
   }

   public MethodBinding getExactMethod(TypeBinding receiverType, char[] selector, InvocationSite invocationSite) {
      if (receiverType != null && receiverType.isValidBinding() && !receiverType.isBaseType()) {
         TypeBinding currentType = receiverType;
         if (receiverType.isArrayType()) {
            if (!receiverType.leafComponentType().canBeSeenBy(this)) {
               return null;
            }

            currentType = this.getJavaLangObject();
         }

         MethodBinding exactMethod = null;

         try {
            exactMethod = this.getExactMethod(receiverType, currentType, selector, invocationSite, null);
         } catch (Scope.MethodClashException var8) {
            return null;
         }

         if (exactMethod != null && exactMethod.canBeSeenBy(invocationSite, this)) {
            TypeBinding[] typeArguments = invocationSite.genericTypeArguments();
            TypeVariableBinding[] typeVariables = exactMethod.typeVariables();
            if (!exactMethod.isVarargs()
               && (typeVariables == Binding.NO_TYPE_VARIABLES || typeArguments != null && typeArguments.length == typeVariables.length)) {
               if (receiverType.isArrayType()) {
                  if (CharOperation.equals(selector, TypeConstants.CLONE)) {
                     return this.environment().computeArrayClone(exactMethod);
                  }

                  if (CharOperation.equals(selector, TypeConstants.GETCLASS)) {
                     return this.environment().createGetClassMethod(receiverType, exactMethod, this);
                  }
               }

               if (exactMethod.declaringClass.id == 1
                  && CharOperation.equals(selector, TypeConstants.GETCLASS)
                  && exactMethod.returnType.isParameterizedType()) {
                  return this.environment().createGetClassMethod(receiverType, exactMethod, this);
               } else {
                  return (MethodBinding)(typeVariables != Binding.NO_TYPE_VARIABLES
                     ? this.environment().createParameterizedGenericMethod(exactMethod, typeArguments)
                     : exactMethod);
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public MethodBinding getExactConstructor(TypeBinding receiverType, InvocationSite invocationSite) {
      if (receiverType != null && receiverType.isValidBinding() && receiverType.canBeInstantiated() && !receiverType.isBaseType()) {
         if (receiverType.isArrayType()) {
            TypeBinding leafType = receiverType.leafComponentType();
            return leafType.canBeSeenBy(this) && leafType.isReifiable()
               ? new MethodBinding(4097, TypeConstants.INIT, receiverType, new TypeBinding[]{TypeBinding.INT}, Binding.NO_EXCEPTIONS, this.getJavaLangObject())
               : null;
         } else {
            CompilationUnitScope unitScope = this.compilationUnitScope();
            MethodBinding exactConstructor = null;
            unitScope.recordTypeReference(receiverType);
            MethodBinding[] methods = receiverType.getMethods(TypeConstants.INIT);
            TypeBinding[] genericTypeArguments = invocationSite.genericTypeArguments();
            int i = 0;

            for(int length = methods.length; i < length; ++i) {
               MethodBinding constructor = methods[i];
               if (constructor.canBeSeenBy(invocationSite, this)) {
                  if (constructor.isVarargs()) {
                     return null;
                  }

                  if (constructor.typeVariables() != Binding.NO_TYPE_VARIABLES && genericTypeArguments == null) {
                     return null;
                  }

                  if (exactConstructor != null) {
                     return null;
                  }

                  exactConstructor = constructor;
               }
            }

            if (exactConstructor != null) {
               TypeVariableBinding[] typeVariables = exactConstructor.typeVariables();
               if (typeVariables != Binding.NO_TYPE_VARIABLES) {
                  if (typeVariables.length != genericTypeArguments.length) {
                     return null;
                  }

                  exactConstructor = this.environment().createParameterizedGenericMethod(exactConstructor, genericTypeArguments);
               }
            }

            return exactConstructor;
         }
      } else {
         return null;
      }
   }

   public MethodBinding getConstructor(ReferenceBinding receiverType, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      MethodBinding method = this.getConstructor0(receiverType, argumentTypes, invocationSite);
      if (method != null && method.isValidBinding() && method.isVarargs()) {
         TypeBinding elementType = method.parameters[method.parameters.length - 1].leafComponentType();
         if (elementType instanceof ReferenceBinding && !((ReferenceBinding)elementType).canBeSeenBy(this)) {
            return new ProblemMethodBinding(method, method.selector, invocationSite.genericTypeArguments(), 16);
         }
      }

      return method;
   }

   public MethodBinding getConstructor0(ReferenceBinding receiverType, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      LookupEnvironment env = unitScope.environment;

      ProblemMethodBinding var16;
      try {
         env.missingClassFileLocation = invocationSite;
         unitScope.recordTypeReference(receiverType);
         unitScope.recordTypeReferences(argumentTypes);
         MethodBinding methodBinding = receiverType.getExactConstructor(argumentTypes);
         if (methodBinding != null && methodBinding.canBeSeenBy(invocationSite, this)) {
            if (invocationSite.genericTypeArguments() != null) {
               methodBinding = this.computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
            }

            return methodBinding;
         }

         MethodBinding[] methods = receiverType.getMethods(TypeConstants.INIT, argumentTypes.length);
         if (methods == Binding.NO_METHODS) {
            return new ProblemMethodBinding(TypeConstants.INIT, argumentTypes, 1);
         }

         MethodBinding[] compatible = new MethodBinding[methods.length];
         int compatibleIndex = 0;
         MethodBinding problemMethod = null;
         int i = 0;

         for(int length = methods.length; i < length; ++i) {
            MethodBinding compatibleMethod = this.computeCompatibleMethod(methods[i], argumentTypes, invocationSite);
            if (compatibleMethod != null) {
               if (compatibleMethod.isValidBinding()) {
                  compatible[compatibleIndex++] = compatibleMethod;
               } else if (problemMethod == null) {
                  problemMethod = compatibleMethod;
               }
            }
         }

         if (compatibleIndex != 0) {
            MethodBinding[] visible = new MethodBinding[compatibleIndex];
            int visibleIndex = 0;

            for(int ix = 0; ix < compatibleIndex; ++ix) {
               MethodBinding method = compatible[ix];
               if (method.canBeSeenBy(invocationSite, this)) {
                  visible[visibleIndex++] = method;
               }
            }

            if (visibleIndex == 1) {
               return visible[0];
            }

            if (visibleIndex == 0) {
               return new ProblemMethodBinding(compatible[0], TypeConstants.INIT, compatible[0].parameters, 2);
            }

            return this.mostSpecificMethodBinding(visible, visibleIndex, argumentTypes, invocationSite, receiverType);
         }

         if (problemMethod != null) {
            return problemMethod;
         }

         var16 = new ProblemMethodBinding(methods[0], TypeConstants.INIT, argumentTypes, 1);
      } catch (AbortCompilation var19) {
         var19.updateContext(invocationSite, this.referenceCompilationUnit().compilationResult);
         throw var19;
      } finally {
         env.missingClassFileLocation = null;
      }

      return var16;
   }

   public final PackageBinding getCurrentPackage() {
      Scope unitScope = this;

      while(true) {
         Scope scope = unitScope.parent;
         if (unitScope.parent == null) {
            return ((CompilationUnitScope)unitScope).fPackage;
         }

         unitScope = scope;
      }
   }

   public int getDeclarationModifiers() {
      switch(this.kind) {
         case 1:
         case 2:
            MethodScope methodScope = this.methodScope();
            if (!methodScope.isInsideInitializer()) {
               MethodBinding context = ((AbstractMethodDeclaration)methodScope.referenceContext).binding;
               if (context != null) {
                  return context.modifiers;
               }
            } else {
               SourceTypeBinding type = ((BlockScope)this).referenceType().binding;
               if (methodScope.initializedField != null) {
                  return methodScope.initializedField.modifiers;
               }

               if (type != null) {
                  return type.modifiers;
               }
            }
            break;
         case 3:
            ReferenceBinding context = ((ClassScope)this).referenceType().binding;
            if (context != null) {
               return context.modifiers;
            }
      }

      return -1;
   }

   public FieldBinding getField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite) {
      LookupEnvironment env = this.environment();

      FieldBinding var7;
      try {
         env.missingClassFileLocation = invocationSite;
         FieldBinding field = this.findField(receiverType, fieldName, invocationSite, true);
         if (field == null) {
            return new ProblemFieldBinding(receiverType instanceof ReferenceBinding ? (ReferenceBinding)receiverType : null, fieldName, 1);
         }

         var7 = field;
      } catch (AbortCompilation var10) {
         var10.updateContext(invocationSite, this.referenceCompilationUnit().compilationResult);
         throw var10;
      } finally {
         env.missingClassFileLocation = null;
      }

      return var7;
   }

   public MethodBinding getImplicitMethod(char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      boolean insideStaticContext = false;
      boolean insideConstructorCall = false;
      boolean insideTypeAnnotation = false;
      MethodBinding foundMethod = null;
      MethodBinding foundProblem = null;
      boolean foundProblemVisible = false;
      Scope scope = this;
      MethodScope methodScope = null;
      int depth = 0;
      CompilerOptions options;
      boolean inheritedHasPrecedence = (options = this.compilerOptions()).complianceLevel >= 3145728L;

      while(true) {
         switch(scope.kind) {
            case 2:
               methodScope = (MethodScope)scope;
               insideStaticContext |= methodScope.isStatic;
               insideConstructorCall |= methodScope.isConstructorCall;
               insideTypeAnnotation = methodScope.insideTypeAnnotation;
               break;
            case 3:
               ClassScope classScope = (ClassScope)scope;
               ReferenceBinding receiverType = classScope.enclosingReceiverType();
               if (!insideTypeAnnotation) {
                  MethodBinding methodBinding = classScope.findExactMethod(receiverType, selector, argumentTypes, invocationSite);
                  if (methodBinding == null) {
                     methodBinding = classScope.findMethod(receiverType, selector, argumentTypes, invocationSite, false);
                  }

                  if (methodBinding != null) {
                     if (foundMethod == null) {
                        if (methodBinding.isValidBinding()) {
                           if (!methodBinding.isStatic() && (insideConstructorCall || insideStaticContext)) {
                              if (foundProblem != null && foundProblem.problemId() != 2) {
                                 return foundProblem;
                              }

                              return new ProblemMethodBinding(methodBinding, methodBinding.selector, methodBinding.parameters, insideConstructorCall ? 6 : 7);
                           }

                           if (!methodBinding.isStatic() && methodScope != null) {
                              this.tagAsAccessingEnclosingInstanceStateOf(receiverType, false);
                           }

                           if (inheritedHasPrecedence
                              || TypeBinding.equalsEquals(receiverType, methodBinding.declaringClass)
                              || receiverType.getMethods(selector) != Binding.NO_METHODS) {
                              if (foundProblemVisible) {
                                 return foundProblem;
                              }

                              if (depth > 0) {
                                 invocationSite.setDepth(depth);
                                 invocationSite.setActualReceiverType(receiverType);
                              }

                              if (argumentTypes == Binding.NO_PARAMETERS
                                 && CharOperation.equals(selector, TypeConstants.GETCLASS)
                                 && methodBinding.returnType.isParameterizedType()) {
                                 return this.environment().createGetClassMethod(receiverType, methodBinding, this);
                              }

                              return methodBinding;
                           }

                           if (foundProblem == null || foundProblem.problemId() == 2) {
                              if (foundProblem != null) {
                                 foundProblem = null;
                              }

                              if (depth > 0) {
                                 invocationSite.setDepth(depth);
                                 invocationSite.setActualReceiverType(receiverType);
                              }

                              foundMethod = methodBinding;
                           }
                        } else {
                           if (methodBinding.problemId() != 2 && methodBinding.problemId() != 1) {
                              return methodBinding;
                           }

                           if (foundProblem == null) {
                              foundProblem = methodBinding;
                           }

                           if (!foundProblemVisible && methodBinding.problemId() == 1) {
                              MethodBinding closestMatch = ((ProblemMethodBinding)methodBinding).closestMatch;
                              if (closestMatch != null && closestMatch.canBeSeenBy(receiverType, invocationSite, this)) {
                                 foundProblem = methodBinding;
                                 foundProblemVisible = true;
                              }
                           }
                        }
                     } else if (methodBinding.problemId() == 3
                        || TypeBinding.notEquals(foundMethod.declaringClass, methodBinding.declaringClass)
                           && (TypeBinding.equalsEquals(receiverType, methodBinding.declaringClass) || receiverType.getMethods(selector) != Binding.NO_METHODS)
                        )
                      {
                        return new ProblemMethodBinding(methodBinding, selector, argumentTypes, 5);
                     }
                  }
               }

               insideTypeAnnotation = false;
               ++depth;
               insideStaticContext |= receiverType.isStatic();
               MethodScope enclosingMethodScope = scope.methodScope();
               insideConstructorCall = enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;
               break;
            case 4:
               if (insideStaticContext && options.sourceLevel >= 3211264L) {
                  if (foundProblem != null) {
                     if (foundProblem.declaringClass != null && foundProblem.declaringClass.id == 1) {
                        return foundProblem;
                     }

                     if (foundProblem.problemId() == 1 && foundProblemVisible) {
                        return foundProblem;
                     }
                  }

                  CompilationUnitScope unitScope = (CompilationUnitScope)scope;
                  unitScope.faultInImports();
                  ImportBinding[] imports = unitScope.imports;
                  if (imports != null) {
                     ObjectVector visible = null;
                     boolean skipOnDemand = false;
                     int i = 0;

                     for(int length = imports.length; i < length; ++i) {
                        ImportBinding importBinding = imports[i];
                        if (importBinding.isStatic()) {
                           Binding resolvedImport = importBinding.resolvedImport;
                           MethodBinding possible = null;
                           if (importBinding.onDemand) {
                              if (!skipOnDemand && resolvedImport instanceof ReferenceBinding) {
                                 possible = this.findMethod((ReferenceBinding)resolvedImport, selector, argumentTypes, invocationSite, true);
                              }
                           } else if (resolvedImport instanceof MethodBinding) {
                              MethodBinding staticMethod = (MethodBinding)resolvedImport;
                              if (CharOperation.equals(staticMethod.selector, selector)) {
                                 possible = this.findMethod(staticMethod.declaringClass, selector, argumentTypes, invocationSite, true);
                              }
                           } else if (resolvedImport instanceof FieldBinding) {
                              FieldBinding staticField = (FieldBinding)resolvedImport;
                              if (CharOperation.equals(staticField.name, selector)) {
                                 char[][] importName = importBinding.reference.tokens;
                                 TypeBinding referencedType = this.getType(importName, importName.length - 1);
                                 if (referencedType != null) {
                                    possible = this.findMethod((ReferenceBinding)referencedType, selector, argumentTypes, invocationSite, true);
                                 }
                              }
                           }

                           if (possible != null && possible != foundProblem) {
                              if (!possible.isValidBinding()) {
                                 if (foundProblem == null) {
                                    foundProblem = possible;
                                 }
                              } else if (possible.isStatic()) {
                                 MethodBinding compatibleMethod = this.computeCompatibleMethod(possible, argumentTypes, invocationSite);
                                 if (compatibleMethod != null) {
                                    if (compatibleMethod.isValidBinding()) {
                                       if (compatibleMethod.canBeSeenBy(unitScope.fPackage)) {
                                          if (!skipOnDemand && !importBinding.onDemand) {
                                             visible = null;
                                             skipOnDemand = true;
                                          }

                                          if (visible == null || !visible.contains(compatibleMethod)) {
                                             ImportReference importReference = importBinding.reference;
                                             if (importReference != null) {
                                                importReference.bits |= 2;
                                             }

                                             if (visible == null) {
                                                visible = new ObjectVector(3);
                                             }

                                             visible.add(compatibleMethod);
                                          }
                                       } else if (foundProblem == null) {
                                          foundProblem = new ProblemMethodBinding(compatibleMethod, selector, compatibleMethod.parameters, 2);
                                       }
                                    } else if (foundProblem == null) {
                                       foundProblem = compatibleMethod;
                                    }
                                 } else if (foundProblem == null) {
                                    foundProblem = new ProblemMethodBinding(possible, selector, argumentTypes, 1);
                                 }
                              }
                           }
                        }
                     }

                     if (visible != null) {
                        if (visible.size == 1) {
                           foundMethod = (MethodBinding)visible.elementAt(0);
                        } else {
                           MethodBinding[] temp = new MethodBinding[visible.size];
                           visible.copyInto(temp);
                           foundMethod = this.mostSpecificMethodBinding(temp, temp.length, argumentTypes, invocationSite, null);
                        }
                     }
                  }
               }

               if (foundMethod != null) {
                  invocationSite.setActualReceiverType(foundMethod.declaringClass);
                  return foundMethod;
               }

               if (foundProblem != null) {
                  return foundProblem;
               }

               return new ProblemMethodBinding(selector, argumentTypes, 1);
         }

         scope = scope.parent;
      }
   }

   public final ReferenceBinding getJavaIoSerializable() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_IO_SERIALIZABLE);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_IO_SERIALIZABLE, this);
   }

   public final ReferenceBinding getJavaLangAnnotationAnnotation() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION, this);
   }

   public final ReferenceBinding getJavaLangAssertionError() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ASSERTIONERROR);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_ASSERTIONERROR, this);
   }

   public final ReferenceBinding getJavaLangClass() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CLASS);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_CLASS, this);
   }

   public final ReferenceBinding getJavaLangCloneable() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CLONEABLE);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_CLONEABLE, this);
   }

   public final ReferenceBinding getJavaLangEnum() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ENUM);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_ENUM, this);
   }

   public final ReferenceBinding getJavaLangInvokeLambdaMetafactory() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY, this);
   }

   public final ReferenceBinding getJavaLangInvokeSerializedLambda() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_SERIALIZEDLAMBDA);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_INVOKE_SERIALIZEDLAMBDA, this);
   }

   public final ReferenceBinding getJavaLangInvokeMethodHandlesLookup() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLES);
      ReferenceBinding outerType = unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLES, this);
      return this.findDirectMemberType("Lookup".toCharArray(), outerType);
   }

   public final ReferenceBinding getJavaLangIterable() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ITERABLE);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_ITERABLE, this);
   }

   public final ReferenceBinding getJavaLangObject() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_OBJECT);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, this);
   }

   public final ReferenceBinding getJavaLangString() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_STRING);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_STRING, this);
   }

   public final ReferenceBinding getJavaLangThrowable() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_THROWABLE);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_THROWABLE, this);
   }

   public final ReferenceBinding getJavaLangIllegalArgumentException() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION, this);
   }

   public final ReferenceBinding getJavaUtilIterator() {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(TypeConstants.JAVA_UTIL_ITERATOR);
      return unitScope.environment.getResolvedType(TypeConstants.JAVA_UTIL_ITERATOR, this);
   }

   public final ReferenceBinding getMemberType(char[] typeName, ReferenceBinding enclosingType) {
      ReferenceBinding memberType = this.findMemberType(typeName, enclosingType);
      if (memberType != null) {
         return memberType;
      } else {
         char[][] compoundName = new char[][]{typeName};
         return new ProblemReferenceBinding(compoundName, null, 1);
      }
   }

   public MethodBinding getMethod(TypeBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
      CompilationUnitScope unitScope = this.compilationUnitScope();
      LookupEnvironment env = unitScope.environment;

      ParameterizedMethodBinding var17;
      try {
         env.missingClassFileLocation = invocationSite;
         switch(receiverType.kind()) {
            case 68:
               unitScope.recordTypeReference(receiverType);
               return this.findMethodForArray((ArrayBinding)receiverType, selector, argumentTypes, invocationSite);
            case 132:
               return new ProblemMethodBinding(selector, argumentTypes, 1);
         }

         unitScope.recordTypeReference(receiverType);
         ReferenceBinding currentType = (ReferenceBinding)receiverType;
         if (!currentType.canBeSeenBy(this)) {
            return new ProblemMethodBinding(selector, argumentTypes, 8);
         }

         MethodBinding methodBinding = this.findExactMethod(currentType, selector, argumentTypes, invocationSite);
         if (methodBinding != null && methodBinding.isValidBinding()) {
            return methodBinding;
         }

         methodBinding = this.findMethod(currentType, selector, argumentTypes, invocationSite, false);
         if (methodBinding == null) {
            return new ProblemMethodBinding(selector, argumentTypes, 1);
         }

         if (!methodBinding.isValidBinding()) {
            return methodBinding;
         }

         if (argumentTypes != Binding.NO_PARAMETERS
            || !CharOperation.equals(selector, TypeConstants.GETCLASS)
            || !methodBinding.returnType.isParameterizedType()) {
            return methodBinding;
         }

         var17 = this.environment().createGetClassMethod(receiverType, methodBinding, this);
      } catch (AbortCompilation var13) {
         var13.updateContext(invocationSite, this.referenceCompilationUnit().compilationResult);
         throw var13;
      } finally {
         env.missingClassFileLocation = null;
      }

      return var17;
   }

   public final Binding getPackage(char[][] compoundName) {
      this.compilationUnitScope().recordQualifiedReference(compoundName);
      Binding binding = this.getTypeOrPackage(compoundName[0], 20, true);
      if (binding == null) {
         char[][] qName = new char[][]{compoundName[0]};
         return new ProblemReferenceBinding(qName, this.environment().createMissingType(null, compoundName), 1);
      } else if (!binding.isValidBinding()) {
         if (binding instanceof PackageBinding) {
            char[][] qName = new char[][]{compoundName[0]};
            return new ProblemReferenceBinding(qName, null, 1);
         } else {
            return binding;
         }
      } else if (!(binding instanceof PackageBinding)) {
         return null;
      } else {
         int currentIndex = 1;
         int length = compoundName.length;

         for(PackageBinding packageBinding = (PackageBinding)binding; currentIndex < length; packageBinding = (PackageBinding)binding) {
            binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
            if (binding == null) {
               return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
            }

            if (!binding.isValidBinding()) {
               return new ProblemReferenceBinding(
                  CharOperation.subarray(compoundName, 0, currentIndex),
                  binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
                  binding.problemId()
               );
            }

            if (!(binding instanceof PackageBinding)) {
               return packageBinding;
            }
         }

         return new ProblemReferenceBinding(compoundName, null, 1);
      }
   }

   public final Binding getOnlyPackage(char[][] compoundName) {
      this.compilationUnitScope().recordQualifiedReference(compoundName);
      Binding binding = this.getTypeOrPackage(compoundName[0], 16, true);
      if (binding != null && binding.isValidBinding()) {
         if (!(binding instanceof PackageBinding)) {
            return null;
         } else {
            int currentIndex = 1;
            int length = compoundName.length;

            PackageBinding packageBinding;
            for(packageBinding = (PackageBinding)binding; currentIndex < length; packageBinding = var6) {
               var6 = packageBinding.getPackage(compoundName[currentIndex++]);
               if (var6 == null) {
                  return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
               }

               if (!var6.isValidBinding()) {
                  return new ProblemReferenceBinding(
                     CharOperation.subarray(compoundName, 0, currentIndex),
                     var6 instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)var6).closestMatch() : null,
                     var6.problemId()
                  );
               }
            }

            return packageBinding;
         }
      } else {
         char[][] qName = new char[][]{compoundName[0]};
         return new ProblemReferenceBinding(qName, null, 1);
      }
   }

   public final TypeBinding getType(char[] name) {
      TypeBinding binding = getBaseType(name);
      return (TypeBinding)(binding != null ? binding : (ReferenceBinding)this.getTypeOrPackage(name, 4, true));
   }

   public final TypeBinding getType(char[] name, PackageBinding packageBinding) {
      if (packageBinding == null) {
         return this.getType(name);
      } else {
         Binding binding = packageBinding.getTypeOrPackage(name);
         if (binding == null) {
            return new ProblemReferenceBinding(CharOperation.arrayConcat(packageBinding.compoundName, name), null, 1);
         } else if (!binding.isValidBinding()) {
            return new ProblemReferenceBinding(
               binding instanceof ReferenceBinding ? ((ReferenceBinding)binding).compoundName : CharOperation.arrayConcat(packageBinding.compoundName, name),
               binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
               binding.problemId()
            );
         } else {
            ReferenceBinding typeBinding = (ReferenceBinding)binding;
            return (TypeBinding)(!typeBinding.canBeSeenBy(this) ? new ProblemReferenceBinding(typeBinding.compoundName, typeBinding, 2) : typeBinding);
         }
      }
   }

   public final TypeBinding getType(char[][] compoundName, int typeNameLength) {
      if (typeNameLength == 1) {
         TypeBinding binding = getBaseType(compoundName[0]);
         if (binding != null) {
            return binding;
         }
      }

      CompilationUnitScope unitScope = this.compilationUnitScope();
      unitScope.recordQualifiedReference(compoundName);
      Binding binding = this.getTypeOrPackage(compoundName[0], typeNameLength == 1 ? 4 : 20, true);
      if (binding == null) {
         char[][] qName = new char[][]{compoundName[0]};
         return new ProblemReferenceBinding(qName, this.environment().createMissingType(this.compilationUnitScope().getCurrentPackage(), qName), 1);
      } else if (!binding.isValidBinding()) {
         if (binding instanceof PackageBinding) {
            char[][] qName = new char[][]{compoundName[0]};
            return new ProblemReferenceBinding(qName, this.environment().createMissingType(null, qName), 1);
         } else {
            return (ReferenceBinding)binding;
         }
      } else {
         int currentIndex = 1;
         boolean checkVisibility = false;
         if (binding instanceof PackageBinding) {
            PackageBinding packageBinding = (PackageBinding)binding;

            while(true) {
               if (currentIndex < typeNameLength) {
                  binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
                  if (binding == null) {
                     char[][] qName = CharOperation.subarray(compoundName, 0, currentIndex);
                     return new ProblemReferenceBinding(qName, this.environment().createMissingType(packageBinding, qName), 1);
                  }

                  if (!binding.isValidBinding()) {
                     return new ProblemReferenceBinding(
                        CharOperation.subarray(compoundName, 0, currentIndex),
                        binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
                        binding.problemId()
                     );
                  }

                  if (binding instanceof PackageBinding) {
                     packageBinding = (PackageBinding)binding;
                     continue;
                  }
               }

               if (binding instanceof PackageBinding) {
                  char[][] qName = CharOperation.subarray(compoundName, 0, currentIndex);
                  return new ProblemReferenceBinding(qName, this.environment().createMissingType(null, qName), 1);
               }

               checkVisibility = true;
               break;
            }
         }

         ReferenceBinding typeBinding = (ReferenceBinding)binding;
         unitScope.recordTypeReference(typeBinding);
         if (checkVisibility && !typeBinding.canBeSeenBy(this)) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), typeBinding, 2);
         } else {
            while(currentIndex < typeNameLength) {
               typeBinding = this.getMemberType(compoundName[currentIndex++], typeBinding);
               if (!typeBinding.isValidBinding()) {
                  if (typeBinding instanceof ProblemReferenceBinding) {
                     ProblemReferenceBinding problemBinding = (ProblemReferenceBinding)typeBinding;
                     return new ProblemReferenceBinding(
                        CharOperation.subarray(compoundName, 0, currentIndex), problemBinding.closestReferenceMatch(), typeBinding.problemId()
                     );
                  }

                  return new ProblemReferenceBinding(
                     CharOperation.subarray(compoundName, 0, currentIndex),
                     (ReferenceBinding)((ReferenceBinding)binding).closestMatch(),
                     typeBinding.problemId()
                  );
               }
            }

            return typeBinding;
         }
      }
   }

   final Binding getTypeOrPackage(char[] name, int mask, boolean needResolve) {
      Scope scope = this;
      MethodScope methodScope = null;
      ReferenceBinding foundType = null;
      boolean insideStaticContext = false;
      boolean insideTypeAnnotation = false;
      if ((mask & 4) == 0) {
         while(true) {
            Scope var23 = scope.parent;
            if (scope.parent == null) {
               break;
            }

            scope = var23;
         }
      } else {
         boolean inheritedHasPrecedence = this.compilerOptions().complianceLevel >= 3145728L;

         label348:
         while(true) {
            switch(scope.kind) {
               case 2:
                  methodScope = (MethodScope)scope;
                  AbstractMethodDeclaration methodDecl = methodScope.referenceMethod();
                  if (methodDecl != null) {
                     if (methodDecl.binding != null) {
                        TypeVariableBinding typeVariable = methodDecl.binding.getTypeVariable(name);
                        if (typeVariable != null) {
                           return typeVariable;
                        }
                     } else {
                        TypeParameter[] params = methodDecl.typeParameters();
                        int i = params == null ? 0 : params.length;

                        while(--i >= 0) {
                           if (CharOperation.equals(params[i].name, name) && params[i].binding != null && params[i].binding.isValidBinding()) {
                              return params[i].binding;
                           }
                        }
                     }
                  }

                  insideStaticContext |= methodScope.isStatic;
                  insideTypeAnnotation = methodScope.insideTypeAnnotation;
               case 1:
                  ReferenceBinding localType = ((BlockScope)scope).findLocalType(name);
                  if (localType != null) {
                     if (foundType != null && TypeBinding.notEquals(foundType, localType)) {
                        return new ProblemReferenceBinding(new char[][]{name}, foundType, 5);
                     }

                     return localType;
                  }
                  break;
               case 3:
                  SourceTypeBinding sourceType = ((ClassScope)scope).referenceContext.binding;
                  if (scope == this && (sourceType.tagBits & 262144L) == 0L) {
                     TypeVariableBinding typeVariable = sourceType.getTypeVariable(name);
                     if (typeVariable != null) {
                        return typeVariable;
                     }

                     if (CharOperation.equals(name, sourceType.sourceName)) {
                        return sourceType;
                     }

                     insideStaticContext |= sourceType.isStatic();
                  } else {
                     if (!insideTypeAnnotation) {
                        ReferenceBinding memberType = this.findMemberType(name, sourceType);
                        if (memberType != null) {
                           if (memberType.problemId() == 3) {
                              if (foundType != null && foundType.problemId() != 2) {
                                 return new ProblemReferenceBinding(new char[][]{name}, foundType, 5);
                              }

                              return memberType;
                           }

                           if (memberType.isValidBinding() && (TypeBinding.equalsEquals(sourceType, memberType.enclosingType()) || inheritedHasPrecedence)) {
                              if (insideStaticContext && !memberType.isStatic() && sourceType.isGenericType()) {
                                 return new ProblemReferenceBinding(new char[][]{name}, memberType, 7);
                              }

                              if (foundType == null || inheritedHasPrecedence && foundType.problemId() == 2) {
                                 return memberType;
                              }

                              if (foundType.isValidBinding() && TypeBinding.notEquals(foundType, memberType)) {
                                 return new ProblemReferenceBinding(new char[][]{name}, foundType, 5);
                              }
                           }

                           if (foundType == null || foundType.problemId() == 2 && memberType.problemId() != 2) {
                              foundType = memberType;
                           }
                        }
                     }

                     TypeVariableBinding typeVariable = sourceType.getTypeVariable(name);
                     if (typeVariable != null) {
                        if (insideStaticContext) {
                           return new ProblemReferenceBinding(new char[][]{name}, typeVariable, 7);
                        }

                        return typeVariable;
                     }

                     insideStaticContext |= sourceType.isStatic();
                     insideTypeAnnotation = false;
                     if (CharOperation.equals(sourceType.sourceName, name)) {
                        if (foundType != null && TypeBinding.notEquals(foundType, sourceType) && foundType.problemId() != 2) {
                           return new ProblemReferenceBinding(new char[][]{name}, foundType, 5);
                        }

                        return sourceType;
                     }
                  }
                  break;
               case 4:
                  if (foundType != null && foundType.problemId() != 2) {
                     return foundType;
                  }
                  break label348;
            }

            scope = scope.parent;
         }
      }

      CompilationUnitScope unitScope = (CompilationUnitScope)scope;
      HashtableOfObject typeOrPackageCache = unitScope.typeOrPackageCache;
      if (typeOrPackageCache != null) {
         Binding cachedBinding = (Binding)typeOrPackageCache.get(name);
         if (cachedBinding != null) {
            if (cachedBinding instanceof ImportBinding) {
               ImportReference importReference = ((ImportBinding)cachedBinding).reference;
               if (importReference != null) {
                  importReference.bits |= 2;
               }

               if (cachedBinding instanceof ImportConflictBinding) {
                  typeOrPackageCache.put(name, cachedBinding = ((ImportConflictBinding)cachedBinding).conflictingTypeBinding);
               } else {
                  typeOrPackageCache.put(name, cachedBinding = ((ImportBinding)cachedBinding).resolvedImport);
               }
            }

            if ((mask & 4) != 0) {
               if (foundType != null && foundType.problemId() != 2 && cachedBinding.problemId() != 3) {
                  return foundType;
               }

               if (cachedBinding instanceof ReferenceBinding) {
                  return cachedBinding;
               }
            }

            if ((mask & 16) != 0 && cachedBinding instanceof PackageBinding) {
               return cachedBinding;
            }
         }
      }

      if ((mask & 4) != 0) {
         ImportBinding[] imports = unitScope.imports;
         if (imports != null && typeOrPackageCache == null) {
            int i = 0;

            for(int length = imports.length; i < length; ++i) {
               ImportBinding importBinding = imports[i];
               if (!importBinding.onDemand && CharOperation.equals(importBinding.compoundName[importBinding.compoundName.length - 1], name)) {
                  Binding resolvedImport = unitScope.resolveSingleImport(importBinding, 4);
                  if (resolvedImport != null && resolvedImport instanceof TypeBinding) {
                     ImportReference importReference = importBinding.reference;
                     if (importReference != null) {
                        importReference.bits |= 2;
                     }

                     return resolvedImport;
                  }
               }
            }
         }

         PackageBinding currentPackage = unitScope.fPackage;
         unitScope.recordReference(currentPackage.compoundName, name);
         Binding binding = currentPackage.getTypeOrPackage(name);
         if (binding instanceof ReferenceBinding) {
            ReferenceBinding referenceType = (ReferenceBinding)binding;
            if ((referenceType.tagBits & 128L) == 0L) {
               if (typeOrPackageCache != null) {
                  typeOrPackageCache.put(name, referenceType);
               }

               return referenceType;
            }
         }

         if (imports != null) {
            boolean foundInImport = false;
            ReferenceBinding type = null;
            int i = 0;

            for(int length = imports.length; i < length; ++i) {
               ImportBinding someImport = imports[i];
               if (someImport.onDemand) {
                  Binding resolvedImport = someImport.resolvedImport;
                  ReferenceBinding temp = null;
                  if (resolvedImport instanceof PackageBinding) {
                     temp = this.findType(name, (PackageBinding)resolvedImport, currentPackage);
                  } else if (someImport.isStatic()) {
                     temp = this.findMemberType(name, (ReferenceBinding)resolvedImport);
                     if (temp != null && !temp.isStatic()) {
                        temp = null;
                     }
                  } else {
                     temp = this.findDirectMemberType(name, (ReferenceBinding)resolvedImport);
                  }

                  if (TypeBinding.notEquals(temp, type) && temp != null) {
                     if (temp.isValidBinding()) {
                        ImportReference importReference = someImport.reference;
                        if (importReference != null) {
                           importReference.bits |= 2;
                        }

                        if (foundInImport) {
                           ReferenceBinding var48 = new ProblemReferenceBinding(new char[][]{name}, type, 3);
                           if (typeOrPackageCache != null) {
                              typeOrPackageCache.put(name, var48);
                           }

                           return var48;
                        }

                        type = temp;
                        foundInImport = true;
                     } else if (foundType == null) {
                        foundType = temp;
                     }
                  }
               }
            }

            if (type != null) {
               if (typeOrPackageCache != null) {
                  typeOrPackageCache.put(name, type);
               }

               return type;
            }
         }
      }

      unitScope.recordSimpleReference(name);
      if ((mask & 16) != 0) {
         PackageBinding packageBinding = unitScope.environment.getTopLevelPackage(name);
         if (packageBinding != null && (packageBinding.tagBits & 128L) == 0L) {
            if (typeOrPackageCache != null) {
               typeOrPackageCache.put(name, packageBinding);
            }

            return packageBinding;
         }
      }

      if (foundType == null) {
         char[][] qName = new char[][]{name};
         ReferenceBinding closestMatch = null;
         if ((mask & 16) != 0) {
            if (needResolve) {
               closestMatch = this.environment().createMissingType(unitScope.fPackage, qName);
            }
         } else {
            PackageBinding packageBinding = unitScope.environment.getTopLevelPackage(name);
            if ((packageBinding == null || !packageBinding.isValidBinding()) && needResolve) {
               closestMatch = this.environment().createMissingType(unitScope.fPackage, qName);
            }
         }

         foundType = new ProblemReferenceBinding(qName, closestMatch, 1);
         if (typeOrPackageCache != null && (mask & 16) != 0) {
            typeOrPackageCache.put(name, foundType);
         }
      } else if ((foundType.tagBits & 128L) != 0L) {
         char[][] qName = new char[][]{name};
         foundType = new ProblemReferenceBinding(qName, foundType, 1);
         if (typeOrPackageCache != null && (mask & 16) != 0) {
            typeOrPackageCache.put(name, foundType);
         }
      }

      return foundType;
   }

   public final Binding getTypeOrPackage(char[][] compoundName) {
      int nameLength = compoundName.length;
      if (nameLength == 1) {
         TypeBinding binding = getBaseType(compoundName[0]);
         if (binding != null) {
            return binding;
         }
      }

      Binding binding = this.getTypeOrPackage(compoundName[0], 20, true);
      if (!binding.isValidBinding()) {
         return binding;
      } else {
         int currentIndex = 1;
         boolean checkVisibility = false;
         if (binding instanceof PackageBinding) {
            PackageBinding packageBinding = (PackageBinding)binding;

            while(true) {
               if (currentIndex < nameLength) {
                  binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
                  if (binding == null) {
                     return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, 1);
                  }

                  if (!binding.isValidBinding()) {
                     return new ProblemReferenceBinding(
                        CharOperation.subarray(compoundName, 0, currentIndex),
                        binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
                        binding.problemId()
                     );
                  }

                  if (binding instanceof PackageBinding) {
                     packageBinding = (PackageBinding)binding;
                     continue;
                  }
               }

               if (binding instanceof PackageBinding) {
                  return binding;
               }

               checkVisibility = true;
               break;
            }
         }

         ReferenceBinding typeBinding = (ReferenceBinding)binding;
         ReferenceBinding qualifiedType = (ReferenceBinding)this.environment().convertToRawType(typeBinding, false);
         if (checkVisibility && !typeBinding.canBeSeenBy(this)) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), typeBinding, 2);
         } else {
            while(currentIndex < nameLength) {
               typeBinding = this.getMemberType(compoundName[currentIndex++], typeBinding);
               if (!typeBinding.isValidBinding()) {
                  return new ProblemReferenceBinding(
                     CharOperation.subarray(compoundName, 0, currentIndex), (ReferenceBinding)typeBinding.closestMatch(), typeBinding.problemId()
                  );
               }

               if (typeBinding.isGenericType()) {
                  qualifiedType = this.environment().createRawType(typeBinding, qualifiedType);
               } else {
                  qualifiedType = (ReferenceBinding)(qualifiedType == null || !qualifiedType.isRawType() && !qualifiedType.isParameterizedType()
                     ? typeBinding
                     : this.environment().createParameterizedType(typeBinding, null, qualifiedType));
               }
            }

            return qualifiedType;
         }
      }
   }

   public boolean hasErasedCandidatesCollisions(TypeBinding one, TypeBinding two, Map invocations, ReferenceBinding type, ASTNode typeRef) {
      invocations.clear();
      TypeBinding[] mecs = this.minimalErasedCandidates(new TypeBinding[]{one, two}, invocations);
      if (mecs != null) {
         int k = 0;

         for(int max = mecs.length; k < max; ++k) {
            TypeBinding mec = mecs[k];
            if (mec != null) {
               Object value = invocations.get(mec);
               if (value instanceof TypeBinding[]) {
                  TypeBinding[] invalidInvocations = (TypeBinding[])value;
                  this.problemReporter().superinterfacesCollide(invalidInvocations[0].erasure(), typeRef, invalidInvocations[0], invalidInvocations[1]);
                  type.tagBits |= 131072L;
                  return true;
               }
            }
         }
      }

      return false;
   }

   public CaseStatement innermostSwitchCase() {
      Scope scope = this;

      while(!(scope instanceof BlockScope)) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return ((BlockScope)scope).enclosingCase;
   }

   protected boolean isAcceptableMethod(MethodBinding one, MethodBinding two) {
      TypeBinding[] oneParams = one.parameters;
      TypeBinding[] twoParams = two.parameters;
      int oneParamsLength = oneParams.length;
      int twoParamsLength = twoParams.length;
      if (oneParamsLength == twoParamsLength) {
         boolean applyErasure = this.environment().globalOptions.sourceLevel < 3211264L;

         for(int i = 0; i < oneParamsLength; ++i) {
            TypeBinding oneParam = applyErasure ? oneParams[i].erasure() : oneParams[i];
            TypeBinding twoParam = applyErasure ? twoParams[i].erasure() : twoParams[i];
            if (!TypeBinding.equalsEquals(oneParam, twoParam) && !oneParam.isCompatibleWith(twoParam)) {
               if (i == oneParamsLength - 1 && one.isVarargs() && two.isVarargs()) {
                  TypeBinding oType = ((ArrayBinding)oneParam).elementsType();
                  TypeBinding eType = ((ArrayBinding)twoParam).elementsType();
                  if (CompilerOptions.tolerateIllegalAmbiguousVarargsInvocation && this.compilerOptions().complianceLevel < 3342336L) {
                     if (TypeBinding.equalsEquals(oneParam, eType) || oneParam.isCompatibleWith(eType)) {
                        return true;
                     }
                  } else if (TypeBinding.equalsEquals(oType, eType) || oType.isCompatibleWith(eType)) {
                     return true;
                  }
               }

               return false;
            }

            if (!two.declaringClass.isRawType()) {
               TypeBinding leafComponentType = two.original().parameters[i].leafComponentType();
               TypeBinding originalTwoParam = applyErasure ? leafComponentType.erasure() : leafComponentType;
               switch(originalTwoParam.kind()) {
                  case 4100:
                     if (((TypeVariableBinding)originalTwoParam).hasOnlyRawBounds()) {
                        break;
                     }
                  case 260:
                  case 516:
                  case 8196:
                     TypeBinding originalOneParam = one.original().parameters[i].leafComponentType();
                     switch(originalOneParam.kind()) {
                        case 4:
                        case 2052:
                           TypeBinding inheritedTwoParam = oneParam.findSuperTypeOriginatingFrom(twoParam);
                           if (inheritedTwoParam != null && inheritedTwoParam.leafComponentType().isRawType()) {
                              return false;
                           }
                           break;
                        case 1028:
                           return false;
                        case 4100:
                           if (((TypeVariableBinding)originalOneParam).upperBound().isRawType()) {
                              return false;
                           }
                     }
               }
            }
         }

         return true;
      } else {
         if (one.isVarargs() && two.isVarargs()) {
            if (CompilerOptions.tolerateIllegalAmbiguousVarargsInvocation
               && this.compilerOptions().complianceLevel < 3342336L
               && oneParamsLength > twoParamsLength
               && ((ArrayBinding)twoParams[twoParamsLength - 1]).elementsType().id != 1) {
               return false;
            }

            for(int i = (oneParamsLength > twoParamsLength ? twoParamsLength : oneParamsLength) - 2; i >= 0; --i) {
               if (TypeBinding.notEquals(oneParams[i], twoParams[i]) && !oneParams[i].isCompatibleWith(twoParams[i])) {
                  return false;
               }
            }

            if (this.parameterCompatibilityLevel(one, twoParams, true) == -1 && this.parameterCompatibilityLevel(two, oneParams, true) == 2) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isBoxingCompatibleWith(TypeBinding expressionType, TypeBinding targetType) {
      LookupEnvironment environment = this.environment();
      if (environment.globalOptions.sourceLevel >= 3211264L && expressionType.isBaseType() != targetType.isBaseType()) {
         TypeBinding convertedType = environment.computeBoxingType(expressionType);
         return TypeBinding.equalsEquals(convertedType, targetType) || convertedType.isCompatibleWith(targetType, this);
      } else {
         return false;
      }
   }

   public final boolean isDefinedInField(FieldBinding field) {
      Scope scope = this;

      do {
         if (scope instanceof MethodScope) {
            MethodScope methodScope = (MethodScope)scope;
            if (methodScope.initializedField == field) {
               return true;
            }
         }

         scope = scope.parent;
      } while(scope != null);

      return false;
   }

   public final boolean isDefinedInMethod(MethodBinding method) {
      method = method.original();
      Scope scope = this;

      do {
         if (scope instanceof MethodScope) {
            ReferenceContext refContext = ((MethodScope)scope).referenceContext;
            if (refContext instanceof AbstractMethodDeclaration && ((AbstractMethodDeclaration)refContext).binding == method) {
               return true;
            }
         }

         scope = scope.parent;
      } while(scope != null);

      return false;
   }

   public final boolean isDefinedInSameUnit(ReferenceBinding type) {
      ReferenceBinding enclosingType = type;

      while((type = enclosingType.enclosingType()) != null) {
         enclosingType = type;
      }

      Scope unitScope = this;

      while(true) {
         Scope scope = unitScope.parent;
         if (unitScope.parent == null) {
            SourceTypeBinding[] topLevelTypes = ((CompilationUnitScope)unitScope).topLevelTypes;
            int i = topLevelTypes.length;

            while(--i >= 0) {
               if (TypeBinding.equalsEquals(topLevelTypes[i], enclosingType.original())) {
                  return true;
               }
            }

            return false;
         }

         unitScope = scope;
      }
   }

   public final boolean isDefinedInType(ReferenceBinding type) {
      Scope scope = this;

      while(!(scope instanceof ClassScope) || !TypeBinding.equalsEquals(((ClassScope)scope).referenceContext.binding, type)) {
         scope = scope.parent;
         if (scope == null) {
            return false;
         }
      }

      return true;
   }

   public boolean isInsideCase(CaseStatement caseStatement) {
      Scope scope = this;

      do {
         switch(scope.kind) {
            case 1:
               if (((BlockScope)scope).enclosingCase == caseStatement) {
                  return true;
               }
         }

         scope = scope.parent;
      } while(scope != null);

      return false;
   }

   public boolean isInsideDeprecatedCode() {
      switch(this.kind) {
         case 1:
         case 2:
            MethodScope methodScope = this.methodScope();
            if (!methodScope.isInsideInitializer()) {
               ReferenceContext referenceContext = methodScope.referenceContext;
               MethodBinding context = referenceContext instanceof AbstractMethodDeclaration
                  ? ((AbstractMethodDeclaration)referenceContext).binding
                  : ((LambdaExpression)referenceContext).binding;
               if (context != null && context.isViewedAsDeprecated()) {
                  return true;
               }
            } else if (methodScope.initializedField != null && methodScope.initializedField.isViewedAsDeprecated()) {
               return true;
            }

            SourceTypeBinding declaringType = ((BlockScope)this).referenceType().binding;
            if (declaringType != null) {
               declaringType.initializeDeprecatedAnnotationTagBits();
               if (declaringType.isViewedAsDeprecated()) {
                  return true;
               }
            }
            break;
         case 3:
            ReferenceBinding context = ((ClassScope)this).referenceType().binding;
            if (context != null) {
               context.initializeDeprecatedAnnotationTagBits();
               if (context.isViewedAsDeprecated()) {
                  return true;
               }
            }
            break;
         case 4:
            CompilationUnitDeclaration unit = this.referenceCompilationUnit();
            if (unit.types != null && unit.types.length > 0) {
               SourceTypeBinding type = unit.types[0].binding;
               if (type != null) {
                  type.initializeDeprecatedAnnotationTagBits();
                  if (type.isViewedAsDeprecated()) {
                     return true;
                  }
               }
            }
      }

      return false;
   }

   private boolean isOverriddenMethodGeneric(MethodBinding method) {
      MethodVerifier verifier = this.environment().methodVerifier();

      for(ReferenceBinding currentType = method.declaringClass.superclass(); currentType != null; currentType = currentType.superclass()) {
         MethodBinding[] currentMethods = currentType.getMethods(method.selector);
         int i = 0;

         for(int l = currentMethods.length; i < l; ++i) {
            MethodBinding currentMethod = currentMethods[i];
            if (currentMethod != null
               && currentMethod.original().typeVariables != Binding.NO_TYPE_VARIABLES
               && verifier.doesMethodOverride(method, currentMethod)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isSubtypeOfRawType(TypeBinding paramType) {
      TypeBinding t = paramType.leafComponentType();
      if (t.isBaseType()) {
         return false;
      } else {
         ReferenceBinding currentType = (ReferenceBinding)t;
         ReferenceBinding[] interfacesToVisit = null;
         int nextPosition = 0;

         while(!currentType.isRawType()) {
            ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
            if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               if (interfacesToVisit == null) {
                  interfacesToVisit = itsInterfaces;
                  nextPosition = itsInterfaces.length;
               } else {
                  int itsLength = itsInterfaces.length;
                  if (nextPosition + itsLength >= interfacesToVisit.length) {
                     System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                  }

                  label94:
                  for(int a = 0; a < itsLength; ++a) {
                     ReferenceBinding next = itsInterfaces[a];

                     for(int b = 0; b < nextPosition; ++b) {
                        if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                           continue label94;
                        }
                     }

                     interfacesToVisit[nextPosition++] = next;
                  }
               }
            }

            if ((currentType = currentType.superclass()) == null) {
               for(int i = 0; i < nextPosition; ++i) {
                  currentType = interfacesToVisit[i];
                  if (currentType.isRawType()) {
                     return true;
                  }

                  ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
                  if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                     int itsLength = itsInterfaces.length;
                     if (nextPosition + itsLength >= interfacesToVisit.length) {
                        System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                     }

                     label66:
                     for(int a = 0; a < itsLength; ++a) {
                        ReferenceBinding next = itsInterfaces[a];

                        for(int b = 0; b < nextPosition; ++b) {
                           if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                              continue label66;
                           }
                        }

                        interfacesToVisit[nextPosition++] = next;
                     }
                  }
               }

               return false;
            }
         }

         return true;
      }
   }

   private TypeBinding leastContainingInvocation(TypeBinding mec, Object invocationData, ArrayList lubStack) {
      if (invocationData == null) {
         return mec;
      } else if (invocationData instanceof TypeBinding) {
         return (TypeBinding)invocationData;
      } else {
         TypeBinding[] invocations = (TypeBinding[])invocationData;
         int dim = mec.dimensions();
         mec = mec.leafComponentType();
         int argLength = mec.typeVariables().length;
         if (argLength == 0) {
            return mec;
         } else {
            TypeBinding[] bestArguments = new TypeBinding[argLength];
            int i = 0;

            for(int length = invocations.length; i < length; ++i) {
               TypeBinding invocation = invocations[i].leafComponentType();
               switch(invocation.kind()) {
                  case 260:
                     ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)invocation;

                     for(int j = 0; j < argLength; ++j) {
                        TypeBinding bestArgument = this.leastContainingTypeArgument(
                           bestArguments[j], parameterizedType.arguments[j], (ReferenceBinding)mec, j, (ArrayList)lubStack.clone()
                        );
                        if (bestArgument == null) {
                           return null;
                        }

                        bestArguments[j] = bestArgument;
                     }
                     break;
                  case 1028:
                     return (TypeBinding)(dim == 0 ? invocation : this.environment().createArrayType(invocation, dim));
                  case 2052:
                     TypeVariableBinding[] invocationVariables = invocation.typeVariables();

                     for(int j = 0; j < argLength; ++j) {
                        TypeBinding bestArgument = this.leastContainingTypeArgument(
                           bestArguments[j], invocationVariables[j], (ReferenceBinding)mec, j, (ArrayList)lubStack.clone()
                        );
                        if (bestArgument == null) {
                           return null;
                        }

                        bestArguments[j] = bestArgument;
                     }
               }
            }

            TypeBinding least = this.environment().createParameterizedType((ReferenceBinding)mec.erasure(), bestArguments, mec.enclosingType());
            return (TypeBinding)(dim == 0 ? least : this.environment().createArrayType(least, dim));
         }
      }
   }

   private TypeBinding leastContainingTypeArgument(TypeBinding u, TypeBinding v, ReferenceBinding genericType, int rank, ArrayList lubStack) {
      if (u == null) {
         return v;
      } else if (TypeBinding.equalsEquals(u, v)) {
         return u;
      } else {
         if (v.isWildcard()) {
            WildcardBinding wildV = (WildcardBinding)v;
            if (u.isWildcard()) {
               WildcardBinding wildU = (WildcardBinding)u;
               label83:
               switch(wildU.boundKind) {
                  case 1:
                     switch(wildV.boundKind) {
                        case 1:
                           TypeBinding lub = this.lowerUpperBound(new TypeBinding[]{wildU.bound, wildV.bound}, lubStack);
                           if (lub == null) {
                              return null;
                           }

                           if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) {
                              return this.environment().createWildcard(genericType, rank, null, null, 0);
                           }

                           return this.environment().createWildcard(genericType, rank, lub, null, 1);
                        case 2:
                           if (TypeBinding.equalsEquals(wildU.bound, wildV.bound)) {
                              return wildU.bound;
                           }

                           return this.environment().createWildcard(genericType, rank, null, null, 0);
                        default:
                           break label83;
                     }
                  case 2:
                     if (wildU.boundKind == 2) {
                        TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{wildU.bound, wildV.bound}, this, this.environment());
                        if (glb == null) {
                           return null;
                        }

                        return this.environment().createWildcard(genericType, rank, glb[0], null, 2);
                     }
               }
            } else {
               switch(wildV.boundKind) {
                  case 0:
                  default:
                     break;
                  case 1:
                     TypeBinding lub = this.lowerUpperBound(new TypeBinding[]{u, wildV.bound}, lubStack);
                     if (lub == null) {
                        return null;
                     }

                     if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) {
                        return this.environment().createWildcard(genericType, rank, null, null, 0);
                     }

                     return this.environment().createWildcard(genericType, rank, lub, null, 1);
                  case 2:
                     TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{u, wildV.bound}, this, this.environment());
                     if (glb == null) {
                        return null;
                     }

                     return this.environment().createWildcard(genericType, rank, glb[0], null, 2);
               }
            }
         } else if (u.isWildcard()) {
            WildcardBinding wildU = (WildcardBinding)u;
            switch(wildU.boundKind) {
               case 0:
               default:
                  break;
               case 1:
                  TypeBinding lub = this.lowerUpperBound(new TypeBinding[]{wildU.bound, v}, lubStack);
                  if (lub == null) {
                     return null;
                  }

                  if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) {
                     return this.environment().createWildcard(genericType, rank, null, null, 0);
                  }

                  return this.environment().createWildcard(genericType, rank, lub, null, 1);
               case 2:
                  TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{wildU.bound, v}, this, this.environment());
                  if (glb == null) {
                     return null;
                  }

                  return this.environment().createWildcard(genericType, rank, glb[0], null, 2);
            }
         }

         TypeBinding lub = this.lowerUpperBound(new TypeBinding[]{u, v}, lubStack);
         if (lub == null) {
            return null;
         } else {
            return TypeBinding.equalsEquals(lub, TypeBinding.INT)
               ? this.environment().createWildcard(genericType, rank, null, null, 0)
               : this.environment().createWildcard(genericType, rank, lub, null, 1);
         }
      }
   }

   public TypeBinding lowerUpperBound(TypeBinding[] types) {
      int typeLength = types.length;
      if (typeLength == 1) {
         TypeBinding type = types[0];
         return (TypeBinding)(type == null ? TypeBinding.VOID : type);
      } else {
         return this.lowerUpperBound(types, new ArrayList(1));
      }
   }

   private TypeBinding lowerUpperBound(TypeBinding[] types, ArrayList lubStack) {
      int typeLength = types.length;
      if (typeLength == 1) {
         TypeBinding type = types[0];
         return (TypeBinding)(type == null ? TypeBinding.VOID : type);
      } else {
         int stackLength = lubStack.size();

         label136:
         for(int i = 0; i < stackLength; ++i) {
            TypeBinding[] lubTypes = (TypeBinding[])lubStack.get(i);
            int lubTypeLength = lubTypes.length;
            if (lubTypeLength >= typeLength) {
               for(int j = 0; j < typeLength; ++j) {
                  TypeBinding type = types[j];
                  if (type != null) {
                     int k = 0;

                     while(true) {
                        if (k >= lubTypeLength) {
                           continue label136;
                        }

                        TypeBinding lubType = lubTypes[k];
                        if (lubType != null && (TypeBinding.equalsEquals(lubType, type) || lubType.isEquivalentTo(type))) {
                           break;
                        }

                        ++k;
                     }
                  }
               }

               return TypeBinding.INT;
            }
         }

         lubStack.add(types);
         Map invocations = new HashMap(1);
         TypeBinding[] mecs = this.minimalErasedCandidates(types, invocations);
         if (mecs == null) {
            return null;
         } else {
            int length = mecs.length;
            if (length == 0) {
               return TypeBinding.VOID;
            } else {
               int count = 0;
               TypeBinding firstBound = null;
               int commonDim = -1;

               for(int i = 0; i < length; ++i) {
                  TypeBinding mec = mecs[i];
                  if (mec != null) {
                     mec = this.leastContainingInvocation(mec, invocations.get(mec), lubStack);
                     if (mec == null) {
                        return null;
                     }

                     int dim = mec.dimensions();
                     if (commonDim == -1) {
                        commonDim = dim;
                     } else if (dim != commonDim) {
                        return null;
                     }

                     if (firstBound == null && !mec.leafComponentType().isInterface()) {
                        firstBound = mec.leafComponentType();
                     }

                     mecs[count++] = mec;
                  }
               }

               switch(count) {
                  case 0:
                     return TypeBinding.VOID;
                  case 1:
                     return mecs[0];
                  case 2:
                     if ((commonDim == 0 ? mecs[1].id : mecs[1].leafComponentType().id) == 1) {
                        return mecs[0];
                     } else if ((commonDim == 0 ? mecs[0].id : mecs[0].leafComponentType().id) == 1) {
                        return mecs[1];
                     }
                  default:
                     TypeBinding[] otherBounds = new TypeBinding[count - 1];
                     int rank = 0;

                     for(int i = 0; i < count; ++i) {
                        TypeBinding mec = commonDim == 0 ? mecs[i] : mecs[i].leafComponentType();
                        if (mec.isInterface()) {
                           otherBounds[rank++] = mec;
                        }
                     }

                     TypeBinding intersectionType = this.environment().createWildcard(null, 0, firstBound, otherBounds, 1);
                     return (TypeBinding)(commonDim == 0 ? intersectionType : this.environment().createArrayType(intersectionType, commonDim));
               }
            }
         }
      }
   }

   public final MethodScope methodScope() {
      Scope scope = this;

      while(!(scope instanceof MethodScope)) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return (MethodScope)scope;
   }

   public final MethodScope namedMethodScope() {
      Scope scope = this;

      while(!(scope instanceof MethodScope) || scope.isLambdaScope()) {
         scope = scope.parent;
         if (scope == null) {
            return null;
         }
      }

      return (MethodScope)scope;
   }

   protected TypeBinding[] minimalErasedCandidates(TypeBinding[] types, Map allInvocations) {
      int length = types.length;
      int indexOfFirst = -1;
      int actualLength = 0;

      for(int i = 0; i < length; ++i) {
         TypeBinding type = types[i];
         if (type == TypeBinding.NULL) {
            type = null;
            types[i] = null;
         }

         if (type != null) {
            if (type.isBaseType()) {
               return null;
            }

            if (indexOfFirst < 0) {
               indexOfFirst = i;
            }

            ++actualLength;
         }
      }

      switch(actualLength) {
         case 0:
            return Binding.NO_TYPES;
         case 1:
            return types;
         default:
            TypeBinding firstType = types[indexOfFirst];
            if (firstType.isBaseType()) {
               return null;
            } else {
               ArrayList typesToVisit = new ArrayList(5);
               int dim = firstType.dimensions();
               TypeBinding leafType = firstType.leafComponentType();
               TypeBinding firstErasure;
               switch(leafType.kind()) {
                  case 68:
                  case 260:
                  case 1028:
                     firstErasure = firstType.erasure();
                     break;
                  default:
                     firstErasure = firstType;
               }

               if (TypeBinding.notEquals(firstErasure, firstType)) {
                  allInvocations.put(firstErasure, firstType);
               }

               typesToVisit.add(firstType);
               int max = 1;
               int i = 0;

               for(; i < max; ++i) {
                  TypeBinding typeToVisit = (TypeBinding)typesToVisit.get(i);
                  dim = typeToVisit.dimensions();
                  if (dim > 0) {
                     leafType = typeToVisit.leafComponentType();
                     switch(leafType.id) {
                        case 1:
                           if (dim > 1) {
                              TypeBinding elementType = ((ArrayBinding)typeToVisit).elementsType();
                              if (!typesToVisit.contains(elementType)) {
                                 typesToVisit.add(elementType);
                                 ++max;
                              }
                              continue;
                           }
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                           TypeBinding superType = this.getJavaIoSerializable();
                           if (!typesToVisit.contains(superType)) {
                              typesToVisit.add(superType);
                              ++max;
                           }

                           TypeBinding var33 = this.getJavaLangCloneable();
                           if (!typesToVisit.contains(var33)) {
                              typesToVisit.add(var33);
                              ++max;
                           }

                           var33 = this.getJavaLangObject();
                           if (!typesToVisit.contains(var33)) {
                              typesToVisit.add(var33);
                              ++max;
                           }
                           continue;
                        case 6:
                        default:
                           typeToVisit = leafType;
                     }
                  }

                  ReferenceBinding currentType = (ReferenceBinding)typeToVisit;
                  if (currentType.isCapture()) {
                     TypeBinding firstBound = ((CaptureBinding)currentType).firstBound;
                     if (firstBound != null && firstBound.isArrayType()) {
                        TypeBinding superType = (TypeBinding)(dim == 0 ? firstBound : this.environment().createArrayType(firstBound, dim));
                        if (typesToVisit.contains(superType)) {
                           continue;
                        }

                        typesToVisit.add(superType);
                        ++max;
                        TypeBinding superTypeErasure = !firstBound.isTypeVariable() && !firstBound.isWildcard() ? superType.erasure() : superType;
                        if (TypeBinding.notEquals(superTypeErasure, superType)) {
                           allInvocations.put(superTypeErasure, superType);
                        }
                        continue;
                     }
                  }

                  ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
                  if (itsInterfaces != null) {
                     int j = 0;

                     for(int count = itsInterfaces.length; j < count; ++j) {
                        TypeBinding itsInterface = itsInterfaces[j];
                        TypeBinding superType = (TypeBinding)(dim == 0 ? itsInterface : this.environment().createArrayType(itsInterface, dim));
                        if (!typesToVisit.contains(superType)) {
                           typesToVisit.add(superType);
                           ++max;
                           TypeBinding superTypeErasure = !itsInterface.isTypeVariable() && !itsInterface.isWildcard() ? superType.erasure() : superType;
                           if (TypeBinding.notEquals(superTypeErasure, superType)) {
                              allInvocations.put(superTypeErasure, superType);
                           }
                        }
                     }
                  }

                  TypeBinding itsSuperclass = currentType.superclass();
                  if (itsSuperclass != null) {
                     TypeBinding superType = (TypeBinding)(dim == 0 ? itsSuperclass : this.environment().createArrayType(itsSuperclass, dim));
                     if (!typesToVisit.contains(superType)) {
                        typesToVisit.add(superType);
                        ++max;
                        TypeBinding superTypeErasure = !itsSuperclass.isTypeVariable() && !itsSuperclass.isWildcard() ? superType.erasure() : superType;
                        if (TypeBinding.notEquals(superTypeErasure, superType)) {
                           allInvocations.put(superTypeErasure, superType);
                        }
                     }
                  }
               }

               i = typesToVisit.size();
               TypeBinding[] erasedSuperTypes = new TypeBinding[i];
               int rank = 0;

               for(TypeBinding type : typesToVisit) {
                  leafType = type.leafComponentType();
                  erasedSuperTypes[rank++] = !leafType.isTypeVariable() && !leafType.isWildcard() ? type.erasure() : type;
               }

               int remaining = i;

               for(int ix = indexOfFirst + 1; ix < length; ++ix) {
                  TypeBinding otherType = types[ix];
                  if (otherType != null) {
                     if (otherType.isArrayType()) {
                        label257:
                        for(int j = 0; j < i; ++j) {
                           TypeBinding erasedSuperType = erasedSuperTypes[j];
                           if (erasedSuperType != null && !TypeBinding.equalsEquals(erasedSuperType, otherType)) {
                              TypeBinding match;
                              if ((match = otherType.findSuperTypeOriginatingFrom(erasedSuperType)) == null) {
                                 erasedSuperTypes[j] = null;
                                 if (--remaining == 0) {
                                    return null;
                                 }
                              } else {
                                 Object invocationData = allInvocations.get(erasedSuperType);
                                 if (invocationData == null) {
                                    allInvocations.put(erasedSuperType, match);
                                 } else if (invocationData instanceof TypeBinding) {
                                    if (TypeBinding.notEquals(match, (TypeBinding)invocationData)) {
                                       TypeBinding[] someInvocations = new TypeBinding[]{(TypeBinding)invocationData, match};
                                       allInvocations.put(erasedSuperType, someInvocations);
                                    }
                                 } else {
                                    TypeBinding[] someInvocations = (TypeBinding[])invocationData;
                                    int invocLength = someInvocations.length;

                                    for(int k = 0; k < invocLength; ++k) {
                                       if (TypeBinding.equalsEquals(someInvocations[k], match)) {
                                          continue label257;
                                       }
                                    }

                                    TypeBinding[] var62;
                                    System.arraycopy(someInvocations, 0, var62 = new TypeBinding[invocLength + 1], 0, invocLength);
                                    allInvocations.put(erasedSuperType, var62);
                                    var62[invocLength] = match;
                                 }
                              }
                           }
                        }
                     } else {
                        label288:
                        for(int j = 0; j < i; ++j) {
                           TypeBinding erasedSuperType = erasedSuperTypes[j];
                           if (erasedSuperType != null) {
                              TypeBinding match;
                              if (!TypeBinding.equalsEquals(erasedSuperType, otherType) && (erasedSuperType.id != 1 || !otherType.isInterface())) {
                                 if (erasedSuperType.isArrayType()) {
                                    match = null;
                                 } else {
                                    match = otherType.findSuperTypeOriginatingFrom(erasedSuperType);
                                 }

                                 if (match == null) {
                                    erasedSuperTypes[j] = null;
                                    if (--remaining == 0) {
                                       return null;
                                    }
                                    continue;
                                 }
                              } else {
                                 match = erasedSuperType;
                              }

                              Object invocationData = allInvocations.get(erasedSuperType);
                              if (invocationData == null) {
                                 allInvocations.put(erasedSuperType, match);
                              } else if (invocationData instanceof TypeBinding) {
                                 if (TypeBinding.notEquals(match, (TypeBinding)invocationData)) {
                                    TypeBinding[] someInvocations = new TypeBinding[]{(TypeBinding)invocationData, match};
                                    allInvocations.put(erasedSuperType, someInvocations);
                                 }
                              } else {
                                 TypeBinding[] someInvocations = (TypeBinding[])invocationData;
                                 int invocLength = someInvocations.length;

                                 for(int k = 0; k < invocLength; ++k) {
                                    if (TypeBinding.equalsEquals(someInvocations[k], match)) {
                                       continue label288;
                                    }
                                 }

                                 TypeBinding[] var59;
                                 System.arraycopy(someInvocations, 0, var59 = new TypeBinding[invocLength + 1], 0, invocLength);
                                 allInvocations.put(erasedSuperType, var59);
                                 var59[invocLength] = match;
                              }
                           }
                        }
                     }
                  }
               }

               if (remaining > 1) {
                  for(int ix = 0; ix < i; ++ix) {
                     TypeBinding erasedSuperType = erasedSuperTypes[ix];
                     if (erasedSuperType != null) {
                        for(int j = 0; j < i; ++j) {
                           if (ix != j) {
                              TypeBinding otherType = erasedSuperTypes[j];
                              if (otherType != null) {
                                 if (erasedSuperType instanceof ReferenceBinding) {
                                    if ((otherType.id != 1 || !erasedSuperType.isInterface())
                                       && erasedSuperType.findSuperTypeOriginatingFrom(otherType) != null) {
                                       erasedSuperTypes[j] = null;
                                       --remaining;
                                    }
                                 } else if (erasedSuperType.isArrayType()
                                    && (
                                       !otherType.isArrayType()
                                          || otherType.leafComponentType().id != 1
                                          || otherType.dimensions() != erasedSuperType.dimensions()
                                          || !erasedSuperType.leafComponentType().isInterface()
                                    )
                                    && erasedSuperType.findSuperTypeOriginatingFrom(otherType) != null) {
                                    erasedSuperTypes[j] = null;
                                    --remaining;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return erasedSuperTypes;
            }
      }
   }

   protected final MethodBinding mostSpecificClassMethodBinding(MethodBinding[] visible, int visibleSize, InvocationSite invocationSite) {
      MethodBinding previous = null;
      int i = 0;

      label36:
      while(i < visibleSize) {
         MethodBinding method = visible[i];
         if (previous != null && TypeBinding.notEquals(method.declaringClass, previous.declaringClass)) {
            break;
         }

         if (!method.isStatic()) {
            previous = method;
         }

         for(int j = 0; j < visibleSize; ++j) {
            if (i != j && !visible[j].areParametersCompatibleWith(method.parameters)) {
               ++i;
               continue label36;
            }
         }

         this.compilationUnitScope().recordTypeReferences(method.thrownExceptions);
         return method;
      }

      return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
   }

   protected final MethodBinding mostSpecificInterfaceMethodBinding(MethodBinding[] visible, int visibleSize, InvocationSite invocationSite) {
      label27:
      for(int i = 0; i < visibleSize; ++i) {
         MethodBinding method = visible[i];

         for(int j = 0; j < visibleSize; ++j) {
            if (i != j && !visible[j].areParametersCompatibleWith(method.parameters)) {
               continue label27;
            }
         }

         this.compilationUnitScope().recordTypeReferences(method.thrownExceptions);
         return method;
      }

      return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
   }

   protected final MethodBinding mostSpecificMethodBinding(
      MethodBinding[] visible, int visibleSize, TypeBinding[] argumentTypes, final InvocationSite invocationSite, ReferenceBinding receiverType
   ) {
      boolean isJdk18 = this.compilerOptions().sourceLevel >= 3407872L;
      if (isJdk18 && invocationSite.checkingPotentialCompatibility()) {
         if (visibleSize != visible.length) {
            System.arraycopy(visible, 0, visible = new MethodBinding[visibleSize], 0, visibleSize);
         }

         invocationSite.acceptPotentiallyCompatibleMethods(visible);
      }

      int[] compatibilityLevels = new int[visibleSize];
      int compatibleCount = 0;

      for(int i = 0; i < visibleSize; ++i) {
         if ((compatibilityLevels[i] = this.parameterCompatibilityLevel(visible[i], argumentTypes, invocationSite)) != -1) {
            if (i != compatibleCount) {
               visible[compatibleCount] = visible[i];
               compatibilityLevels[compatibleCount] = compatibilityLevels[i];
            }

            ++compatibleCount;
         }
      }

      if (compatibleCount == 0) {
         return new ProblemMethodBinding(visible[0].selector, argumentTypes, 1);
      } else if (compatibleCount == 1) {
         MethodBinding candidate = visible[0];
         if (candidate != null) {
            this.compilationUnitScope().recordTypeReferences(candidate.thrownExceptions);
         }

         return candidate;
      } else {
         if (compatibleCount != visibleSize) {
            visibleSize = compatibleCount;
            System.arraycopy(visible, 0, visible = new MethodBinding[compatibleCount], 0, compatibleCount);
            System.arraycopy(compatibilityLevels, 0, compatibilityLevels = new int[compatibleCount], 0, compatibleCount);
         }

         MethodBinding[] moreSpecific = new MethodBinding[visibleSize];
         if (isJdk18) {
            int count = 0;

            label389:
            for(int j = 0; j < visibleSize; ++j) {
               MethodBinding mbj = visible[j].genericMethod();
               TypeBinding[] mbjParameters = mbj.parameters;
               int levelj = compatibilityLevels[j];

               for(int k = 0; k < visibleSize; ++k) {
                  if (j != k) {
                     int levelk = compatibilityLevels[k];
                     if (levelj > -1 && levelk > -1 && levelj != levelk) {
                        if (levelj >= levelk) {
                           continue label389;
                        }
                     } else {
                        MethodBinding mbk = visible[k].genericMethod();
                        TypeBinding[] mbkParameters = mbk.parameters;
                        if ((invocationSite instanceof Invocation || invocationSite instanceof ReferenceExpression)
                           && mbk.typeVariables() != Binding.NO_TYPE_VARIABLES) {
                           Expression[] expressions = null;
                           if (invocationSite instanceof Invocation) {
                              expressions = ((Invocation)invocationSite).arguments();
                           } else {
                              expressions = ((ReferenceExpression)invocationSite).createPseudoExpressions(argumentTypes);
                           }

                           InferenceContext18 ic18 = new InferenceContext18(this, expressions, null, null);
                           if (!ic18.isMoreSpecificThan(mbj, mbk, levelj == 2, levelk == 2)) {
                              continue label389;
                           }
                        } else {
                           int i = 0;

                           for(int length = argumentTypes.length; i < length; ++i) {
                              TypeBinding argumentType = argumentTypes[i];
                              TypeBinding s = InferenceContext18.getParameter(mbjParameters, i, levelj == 2);
                              TypeBinding t = InferenceContext18.getParameter(mbkParameters, i, levelk == 2);
                              if (!TypeBinding.equalsEquals(s, t) && !argumentType.sIsMoreSpecific(s, t, this)) {
                                 continue label389;
                              }
                           }

                           if (levelj == 2 && levelk == 2) {
                              TypeBinding s = InferenceContext18.getParameter(mbjParameters, argumentTypes.length, true);
                              TypeBinding t = InferenceContext18.getParameter(mbkParameters, argumentTypes.length, true);
                              if (TypeBinding.notEquals(s, t) && t.isSubtypeOf(s)) {
                                 continue label389;
                              }
                           }
                        }
                     }
                  }
               }

               moreSpecific[count++] = visible[j];
            }

            if (count == 0) {
               return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
            }

            if (count == 1) {
               MethodBinding candidate = moreSpecific[0];
               if (candidate != null) {
                  this.compilationUnitScope().recordTypeReferences(candidate.thrownExceptions);
               }

               return candidate;
            }

            visibleSize = count;
         } else {
            InvocationSite tieBreakInvocationSite = new InvocationSite() {
               @Override
               public TypeBinding[] genericTypeArguments() {
                  return null;
               }

               @Override
               public boolean isSuperAccess() {
                  return invocationSite.isSuperAccess();
               }

               @Override
               public boolean isTypeAccess() {
                  return invocationSite.isTypeAccess();
               }

               @Override
               public void setActualReceiverType(ReferenceBinding actualReceiverType) {
               }

               @Override
               public void setDepth(int depth) {
               }

               @Override
               public void setFieldIndex(int depth) {
               }

               @Override
               public int sourceStart() {
                  return invocationSite.sourceStart();
               }

               @Override
               public int sourceEnd() {
                  return invocationSite.sourceStart();
               }

               @Override
               public TypeBinding invocationTargetType() {
                  return invocationSite.invocationTargetType();
               }

               @Override
               public boolean receiverIsImplicitThis() {
                  return invocationSite.receiverIsImplicitThis();
               }

               @Override
               public InferenceContext18 freshInferenceContext(Scope scope) {
                  return null;
               }

               @Override
               public ExpressionContext getExpressionContext() {
                  return ExpressionContext.VANILLA_CONTEXT;
               }

               @Override
               public boolean isQualifiedSuper() {
                  return invocationSite.isQualifiedSuper();
               }

               @Override
               public boolean checkingPotentialCompatibility() {
                  return false;
               }

               @Override
               public void acceptPotentiallyCompatibleMethods(MethodBinding[] methods) {
               }
            };
            int count = 0;
            int level = 0;

            for(int max = 2; level <= max; ++level) {
               label434:
               for(int i = 0; i < visibleSize; ++i) {
                  if (compatibilityLevels[i] == level) {
                     max = level;
                     MethodBinding current = visible[i];
                     MethodBinding original = current.original();
                     MethodBinding tiebreakMethod = current.tiebreakMethod();

                     for(int j = 0; j < visibleSize; ++j) {
                        if (i != j && compatibilityLevels[j] == level) {
                           MethodBinding next = visible[j];
                           if (original == next.original()) {
                              compatibilityLevels[j] = -1;
                           } else {
                              MethodBinding methodToTest = next;
                              if (next instanceof ParameterizedGenericMethodBinding) {
                                 ParameterizedGenericMethodBinding pNext = (ParameterizedGenericMethodBinding)next;
                                 if (!pNext.isRaw || pNext.isStatic()) {
                                    methodToTest = pNext.originalMethod;
                                 }
                              }

                              MethodBinding acceptable = this.computeCompatibleMethod(
                                 methodToTest, tiebreakMethod.parameters, tieBreakInvocationSite, level == 2
                              );
                              if (acceptable == null
                                 || !acceptable.isValidBinding()
                                 || !this.isAcceptableMethod(tiebreakMethod, acceptable)
                                 || current.isBridge() && !next.isBridge() && tiebreakMethod.areParametersEqual(acceptable)) {
                                 continue label434;
                              }
                           }
                        }
                     }

                     moreSpecific[i] = current;
                     ++count;
                  }
               }
            }

            if (count == 1) {
               for(int i = 0; i < visibleSize; ++i) {
                  if (moreSpecific[i] != null) {
                     MethodBinding candidate = visible[i];
                     if (candidate != null) {
                        this.compilationUnitScope().recordTypeReferences(candidate.thrownExceptions);
                     }

                     return candidate;
                  }
               }
            } else if (count == 0) {
               return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
            }
         }

         if (receiverType != null) {
            receiverType = receiverType instanceof CaptureBinding ? receiverType : (ReferenceBinding)receiverType.erasure();
         }

         label346:
         for(int i = 0; i < visibleSize; ++i) {
            MethodBinding current = moreSpecific[i];
            if (current != null) {
               ReferenceBinding[] mostSpecificExceptions = null;
               MethodBinding original = current.original();
               boolean shouldIntersectExceptions = original.declaringClass.isAbstract() && original.thrownExceptions != Binding.NO_EXCEPTIONS;

               for(int j = 0; j < visibleSize; ++j) {
                  MethodBinding next = moreSpecific[j];
                  if (next != null && i != j) {
                     MethodBinding original2 = next.original();
                     if (TypeBinding.equalsEquals(original.declaringClass, original2.declaringClass)) {
                        return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
                     }

                     if (!original.isAbstract()) {
                        if (!original2.isAbstract() && !original2.isDefaultMethod()) {
                           original2 = original.findOriginalInheritedMethod(original2);
                           if (original2 == null
                              || (current.hasSubstitutedParameters() || original.typeVariables != Binding.NO_TYPE_VARIABLES)
                                 && !this.environment().methodVerifier().isParameterSubsignature(original, original2)) {
                              continue label346;
                           }
                        }
                     } else if (receiverType != null) {
                        TypeBinding superType = receiverType.findSuperTypeOriginatingFrom(original.declaringClass.erasure());
                        if (!TypeBinding.equalsEquals(original.declaringClass, superType) && superType instanceof ReferenceBinding) {
                           MethodBinding[] superMethods = ((ReferenceBinding)superType).getMethods(original.selector, argumentTypes.length);
                           int m = 0;

                           for(int l = superMethods.length; m < l; ++m) {
                              if (superMethods[m].original() == original) {
                                 original = superMethods[m];
                                 break;
                              }
                           }
                        }

                        superType = receiverType.findSuperTypeOriginatingFrom(original2.declaringClass.erasure());
                        if (!TypeBinding.equalsEquals(original2.declaringClass, superType) && superType instanceof ReferenceBinding) {
                           MethodBinding[] superMethods = ((ReferenceBinding)superType).getMethods(original2.selector, argumentTypes.length);
                           int m = 0;

                           for(int l = superMethods.length; m < l; ++m) {
                              if (superMethods[m].original() == original2) {
                                 original2 = superMethods[m];
                                 break;
                              }
                           }
                        }

                        if (original.typeVariables != Binding.NO_TYPE_VARIABLES) {
                           original2 = original.computeSubstitutedMethod(original2, this.environment());
                        }

                        if (original2 == null
                           || !original.areParameterErasuresEqual(original2)
                           || TypeBinding.notEquals(original.returnType, original2.returnType)
                              && (
                                 next.original().typeVariables != Binding.NO_TYPE_VARIABLES
                                    ? original.returnType.erasure().findSuperTypeOriginatingFrom(original2.returnType.erasure()) == null
                                    : !current.returnType.isCompatibleWith(next.returnType)
                              )) {
                           continue label346;
                        }

                        if (shouldIntersectExceptions && original2.declaringClass.isInterface() && current.thrownExceptions != next.thrownExceptions) {
                           if (next.thrownExceptions == Binding.NO_EXCEPTIONS) {
                              mostSpecificExceptions = Binding.NO_EXCEPTIONS;
                           } else {
                              if (mostSpecificExceptions == null) {
                                 mostSpecificExceptions = current.thrownExceptions;
                              }

                              int mostSpecificLength = mostSpecificExceptions.length;
                              ReferenceBinding[] nextExceptions = this.getFilteredExceptions(next);
                              int nextLength = nextExceptions.length;
                              SimpleSet temp = new SimpleSet(mostSpecificLength);
                              boolean changed = false;

                              for(int t = 0; t < mostSpecificLength; ++t) {
                                 ReferenceBinding exception = mostSpecificExceptions[t];

                                 for(int s = 0; s < nextLength; ++s) {
                                    ReferenceBinding nextException = nextExceptions[s];
                                    if (exception.isCompatibleWith(nextException)) {
                                       temp.add(exception);
                                       break;
                                    }

                                    if (nextException.isCompatibleWith(exception)) {
                                       temp.add(nextException);
                                       changed = true;
                                       break;
                                    }

                                    changed = true;
                                 }
                              }

                              if (changed) {
                                 mostSpecificExceptions = temp.elementSize == 0 ? Binding.NO_EXCEPTIONS : new ReferenceBinding[temp.elementSize];
                                 temp.asArray(mostSpecificExceptions);
                              }
                           }
                        }
                     }
                  }
               }

               if (mostSpecificExceptions != null && mostSpecificExceptions != current.thrownExceptions) {
                  return new MostSpecificExceptionMethodBinding(current, mostSpecificExceptions);
               }

               return current;
            }
         }

         return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, 3);
      }
   }

   private ReferenceBinding[] getFilteredExceptions(MethodBinding method) {
      ReferenceBinding[] allExceptions = method.thrownExceptions;
      int length = allExceptions.length;
      if (length < 2) {
         return allExceptions;
      } else {
         ReferenceBinding[] filteredExceptions = new ReferenceBinding[length];
         int count = 0;

         label43:
         for(int i = 0; i < length; ++i) {
            ReferenceBinding currentException = allExceptions[i];

            for(int j = 0; j < length; ++j) {
               if (i != j) {
                  if (TypeBinding.equalsEquals(currentException, allExceptions[j])) {
                     if (i >= j) {
                        continue label43;
                     }
                     break;
                  }

                  if (currentException.isCompatibleWith(allExceptions[j])) {
                     continue label43;
                  }
               }
            }

            filteredExceptions[count++] = currentException;
         }

         if (count != length) {
            ReferenceBinding[] tmp = new ReferenceBinding[count];
            System.arraycopy(filteredExceptions, 0, tmp, 0, count);
            return tmp;
         } else {
            return allExceptions;
         }
      }
   }

   public final ClassScope outerMostClassScope() {
      ClassScope lastClassScope = null;
      Scope scope = this;

      do {
         if (scope instanceof ClassScope) {
            lastClassScope = (ClassScope)scope;
         }

         scope = scope.parent;
      } while(scope != null);

      return lastClassScope;
   }

   public final MethodScope outerMostMethodScope() {
      MethodScope lastMethodScope = null;
      Scope scope = this;

      do {
         if (scope instanceof MethodScope) {
            lastMethodScope = (MethodScope)scope;
         }

         scope = scope.parent;
      } while(scope != null);

      return lastMethodScope;
   }

   public int parameterCompatibilityLevel(MethodBinding method, TypeBinding[] arguments, InvocationSite site) {
      if (method.problemId() == 23) {
         method = ((ProblemMethodBinding)method).closestMatch;
         if (method == null) {
            return -1;
         }
      }

      if (this.compilerOptions().sourceLevel >= 3407872L && method instanceof ParameterizedGenericMethodBinding) {
         int inferenceKind = 0;
         InferenceContext18 context = null;
         if (site instanceof Invocation) {
            Invocation invocation = (Invocation)site;
            context = invocation.getInferenceContext((ParameterizedGenericMethodBinding)method);
            if (context != null) {
               inferenceKind = context.inferenceKind;
            }
         } else if (site instanceof ReferenceExpression) {
            ReferenceExpression referenceExpression = (ReferenceExpression)site;
            context = referenceExpression.getInferenceContext((ParameterizedGenericMethodBinding)method);
            if (context != null) {
               inferenceKind = context.inferenceKind;
            }
         }

         if (site instanceof Invocation && context != null && context.stepCompleted >= 2) {
            int i = 0;

            for(int length = arguments.length; i < length; ++i) {
               TypeBinding argument = arguments[i];
               if (argument.isFunctionalType()) {
                  TypeBinding parameter = InferenceContext18.getParameter(method.parameters, i, context.isVarArgs());
                  if (!argument.isCompatibleWith(parameter, this)) {
                     if (!argument.isPolyType()) {
                        return -1;
                     }

                     parameter = InferenceContext18.getParameter(method.original().parameters, i, context.isVarArgs());
                     if (((PolyTypeBinding)argument).expression.isPertinentToApplicability(parameter, method)) {
                        return -1;
                     }
                  }
               }
            }
         }

         switch(inferenceKind) {
            case 1:
               return 0;
            case 2:
               return 1;
            case 3:
               return 2;
         }
      }

      return this.parameterCompatibilityLevel(method, arguments, false);
   }

   public int parameterCompatibilityLevel(MethodBinding method, TypeBinding[] arguments) {
      return this.parameterCompatibilityLevel(method, arguments, false);
   }

   public int parameterCompatibilityLevel(MethodBinding method, TypeBinding[] arguments, boolean tiebreakingVarargsMethods) {
      TypeBinding[] parameters = method.parameters;
      int paramLength = parameters.length;
      int argLength = arguments.length;
      CompilerOptions compilerOptions = this.compilerOptions();
      if (compilerOptions.sourceLevel < 3211264L) {
         if (paramLength != argLength) {
            return -1;
         } else {
            for(int i = 0; i < argLength; ++i) {
               TypeBinding param = parameters[i];
               TypeBinding arg = arguments[i];
               if (TypeBinding.notEquals(arg, param) && !arg.isCompatibleWith(param.erasure(), this)) {
                  return -1;
               }
            }

            return 0;
         }
      } else {
         if (tiebreakingVarargsMethods && CompilerOptions.tolerateIllegalAmbiguousVarargsInvocation && compilerOptions.complianceLevel < 3342336L) {
            tiebreakingVarargsMethods = false;
         }

         int level = 0;
         int lastIndex = argLength;
         LookupEnvironment env = this.environment();
         if (method.isVarargs()) {
            lastIndex = paramLength - 1;
            if (paramLength == argLength) {
               TypeBinding param = parameters[lastIndex];
               TypeBinding arg = arguments[lastIndex];
               if (TypeBinding.notEquals(param, arg)) {
                  level = this.parameterCompatibilityLevel(arg, param, env, tiebreakingVarargsMethods);
                  if (level == -1) {
                     param = ((ArrayBinding)param).elementsType();
                     if (tiebreakingVarargsMethods) {
                        arg = ((ArrayBinding)arg).elementsType();
                     }

                     if (this.parameterCompatibilityLevel(arg, param, env, tiebreakingVarargsMethods) == -1) {
                        return -1;
                     }

                     level = 2;
                  }
               }
            } else {
               if (paramLength < argLength) {
                  TypeBinding param = ((ArrayBinding)parameters[lastIndex]).elementsType();

                  for(int i = lastIndex; i < argLength; ++i) {
                     TypeBinding arg = tiebreakingVarargsMethods && i == argLength - 1 ? ((ArrayBinding)arguments[i]).elementsType() : arguments[i];
                     if (TypeBinding.notEquals(param, arg) && this.parameterCompatibilityLevel(arg, param, env, tiebreakingVarargsMethods) == -1) {
                        return -1;
                     }
                  }
               } else if (lastIndex != argLength) {
                  return -1;
               }

               level = 2;
            }
         } else if (paramLength != argLength) {
            return -1;
         }

         for(int i = 0; i < lastIndex; ++i) {
            TypeBinding param = parameters[i];
            TypeBinding arg = tiebreakingVarargsMethods && i == argLength - 1 ? ((ArrayBinding)arguments[i]).elementsType() : arguments[i];
            if (TypeBinding.notEquals(arg, param)) {
               int newLevel = this.parameterCompatibilityLevel(arg, param, env, tiebreakingVarargsMethods);
               if (newLevel == -1) {
                  return -1;
               }

               if (newLevel > level) {
                  level = newLevel;
               }
            }
         }

         return level;
      }
   }

   public int parameterCompatibilityLevel(TypeBinding arg, TypeBinding param) {
      if (TypeBinding.equalsEquals(arg, param)) {
         return 0;
      } else if (arg == null || param == null) {
         return -1;
      } else if (arg.isCompatibleWith(param, this)) {
         return 0;
      } else {
         if (arg.kind() == 65540 || arg.isBaseType() != param.isBaseType()) {
            TypeBinding convertedType = this.environment().computeBoxingType(arg);
            if (TypeBinding.equalsEquals(convertedType, param) || convertedType.isCompatibleWith(param, this)) {
               return 1;
            }
         }

         return -1;
      }
   }

   private int parameterCompatibilityLevel(TypeBinding arg, TypeBinding param, LookupEnvironment env, boolean tieBreakingVarargsMethods) {
      if (arg == null || param == null) {
         return -1;
      } else if (arg.isCompatibleWith(param, this)) {
         return 0;
      } else if (!tieBreakingVarargsMethods || this.compilerOptions().complianceLevel < 3342336L && CompilerOptions.tolerateIllegalAmbiguousVarargsInvocation) {
         if (arg.kind() == 65540 || arg.isBaseType() != param.isBaseType()) {
            TypeBinding convertedType = env.computeBoxingType(arg);
            if (TypeBinding.equalsEquals(convertedType, param) || convertedType.isCompatibleWith(param, this)) {
               return 1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public abstract ProblemReporter problemReporter();

   public final CompilationUnitDeclaration referenceCompilationUnit() {
      Scope unitScope = this;

      while(true) {
         Scope scope = unitScope.parent;
         if (unitScope.parent == null) {
            return ((CompilationUnitScope)unitScope).referenceContext;
         }

         unitScope = scope;
      }
   }

   public ReferenceContext referenceContext() {
      Scope current = this;

      while(true) {
         switch(current.kind) {
            case 2:
               return ((MethodScope)current).referenceContext;
            case 3:
               return ((ClassScope)current).referenceContext;
            case 4:
               return ((CompilationUnitScope)current).referenceContext;
            default:
               if ((current = current.parent) == null) {
                  return null;
               }
         }
      }
   }

   public ReferenceContext originalReferenceContext() {
      Scope current = this;

      while(true) {
         switch(current.kind) {
            case 2:
               ReferenceContext context = ((MethodScope)current).referenceContext;
               if (!(context instanceof LambdaExpression)) {
                  return context;
               }

               LambdaExpression expression = (LambdaExpression)context;

               while(expression != expression.original) {
                  expression = expression.original;
               }

               return expression;
            case 3:
               return ((ClassScope)current).referenceContext;
            case 4:
               return ((CompilationUnitScope)current).referenceContext;
            default:
               if ((current = current.parent) == null) {
                  return null;
               }
         }
      }
   }

   public boolean deferCheck(Runnable check) {
      return this.parent != null ? this.parent.deferCheck(check) : false;
   }

   public void deferBoundCheck(TypeReference typeRef) {
      if (this.kind == 3) {
         ClassScope classScope = (ClassScope)this;
         if (classScope.deferredBoundChecks == null) {
            classScope.deferredBoundChecks = new ArrayList<>(3);
            classScope.deferredBoundChecks.add(typeRef);
         } else if (!classScope.deferredBoundChecks.contains(typeRef)) {
            classScope.deferredBoundChecks.add(typeRef);
         }
      }
   }

   int startIndex() {
      return 0;
   }

   public MethodBinding getStaticFactory(
      ParameterizedTypeBinding allocationType, ReferenceBinding originalEnclosingType, TypeBinding[] argumentTypes, InvocationSite allocationSite
   ) {
      int classTypeVariablesArity = 0;
      TypeVariableBinding[] classTypeVariables = Binding.NO_TYPE_VARIABLES;
      ReferenceBinding genericType = allocationType.genericType();

      for(ReferenceBinding currentType = genericType; currentType != null; currentType = currentType.enclosingType()) {
         TypeVariableBinding[] typeVariables = currentType.typeVariables();
         int length = typeVariables == null ? 0 : typeVariables.length;
         if (length > 0) {
            System.arraycopy(classTypeVariables, 0, classTypeVariables = new TypeVariableBinding[classTypeVariablesArity + length], 0, classTypeVariablesArity);
            System.arraycopy(typeVariables, 0, classTypeVariables, classTypeVariablesArity, length);
            classTypeVariablesArity += length;
         }

         if (currentType.isStatic()) {
            break;
         }
      }

      MethodBinding[] methods = allocationType.getMethods(TypeConstants.INIT, argumentTypes.length);
      MethodBinding[] staticFactories = new MethodBinding[methods.length];
      int sfi = 0;
      int i = 0;

      for(int length = methods.length; i < length; ++i) {
         MethodBinding method = methods[i];
         if (method.canBeSeenBy(allocationSite, this)) {
            int paramLength = method.parameters.length;
            boolean isVarArgs = method.isVarargs();
            if (argumentTypes.length == paramLength || isVarArgs && argumentTypes.length >= paramLength - 1) {
               TypeVariableBinding[] methodTypeVariables = method.typeVariables();
               int methodTypeVariablesArity = methodTypeVariables.length;
               int factoryArity = classTypeVariablesArity + methodTypeVariablesArity;
               LookupEnvironment environment = this.environment();
               MethodBinding staticFactory = new SyntheticFactoryMethodBinding(method.original(), environment, originalEnclosingType);
               staticFactory.typeVariables = new TypeVariableBinding[factoryArity];
               final SimpleLookupTable map = new SimpleLookupTable(factoryArity);
               String prime = "";
               Binding declaringElement = null;

               for(int j = 0; j < classTypeVariablesArity; ++j) {
                  TypeVariableBinding original = classTypeVariables[j];
                  if (original.declaringElement != declaringElement) {
                     declaringElement = original.declaringElement;
                     prime = prime + "'";
                  }

                  map.put(
                     original.unannotated(),
                     staticFactory.typeVariables[j] = new TypeVariableBinding(
                        CharOperation.concat(original.sourceName, prime.toCharArray()), staticFactory, j, environment
                     )
                  );
               }

               prime = prime + "'";
               int j = classTypeVariablesArity;

               for(int k = 0; j < factoryArity; ++k) {
                  map.put(
                     methodTypeVariables[k].unannotated(),
                     staticFactory.typeVariables[j] = new TypeVariableBinding(
                        CharOperation.concat(methodTypeVariables[k].sourceName, prime.toCharArray()), staticFactory, j, environment
                     )
                  );
                  ++j;
               }

               final Scope scope = this;
               Substitution substitution = new Substitution() {
                  @Override
                  public LookupEnvironment environment() {
                     return scope.environment();
                  }

                  @Override
                  public boolean isRawSubstitution() {
                     return false;
                  }

                  @Override
                  public TypeBinding substitute(TypeVariableBinding typeVariable) {
                     TypeBinding retVal = (TypeBinding)map.get(typeVariable.unannotated());
                     return (TypeBinding)(retVal == null
                        ? typeVariable
                        : (typeVariable.hasTypeAnnotations() ? this.environment().createAnnotatedType(retVal, typeVariable.getTypeAnnotations()) : retVal));
                  }
               };

               for(int jx = 0; jx < factoryArity; ++jx) {
                  TypeVariableBinding originalVariable = jx < classTypeVariablesArity
                     ? classTypeVariables[jx]
                     : methodTypeVariables[jx - classTypeVariablesArity];
                  TypeVariableBinding substitutedVariable = (TypeVariableBinding)map.get(originalVariable.unannotated());
                  TypeBinding substitutedSuperclass = substitute(substitution, originalVariable.superclass);
                  ReferenceBinding[] substitutedInterfaces = substitute(substitution, originalVariable.superInterfaces);
                  if (originalVariable.firstBound != null) {
                     TypeBinding firstBound = (TypeBinding)(TypeBinding.equalsEquals(originalVariable.firstBound, originalVariable.superclass)
                        ? substitutedSuperclass
                        : substitutedInterfaces[0]);
                     substitutedVariable.setFirstBound(firstBound);
                  }

                  switch(substitutedSuperclass.kind()) {
                     case 68:
                        substitutedVariable.setSuperClass(environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                        substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                        break;
                     default:
                        if (substitutedSuperclass.isInterface()) {
                           substitutedVariable.setSuperClass(environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null));
                           int interfaceCount = substitutedInterfaces.length;
                           ReferenceBinding[] var44;
                           System.arraycopy(substitutedInterfaces, 0, var44 = new ReferenceBinding[interfaceCount + 1], 1, interfaceCount);
                           var44[0] = (ReferenceBinding)substitutedSuperclass;
                           substitutedVariable.setSuperInterfaces(var44);
                        } else {
                           substitutedVariable.setSuperClass((ReferenceBinding)substitutedSuperclass);
                           substitutedVariable.setSuperInterfaces(substitutedInterfaces);
                        }
                  }
               }

               staticFactory.returnType = environment.createParameterizedType(
                  genericType, substitute(substitution, (ReferenceBinding[])genericType.typeVariables()), originalEnclosingType
               );
               staticFactory.parameters = substitute(substitution, method.parameters);
               staticFactory.thrownExceptions = substitute(substitution, method.thrownExceptions);
               if (staticFactory.thrownExceptions == null) {
                  staticFactory.thrownExceptions = Binding.NO_EXCEPTIONS;
               }

               staticFactories[sfi++] = new ParameterizedMethodBinding(
                  (ParameterizedTypeBinding)environment.convertToParameterizedType(staticFactory.declaringClass), staticFactory
               );
            }
         }
      }

      if (sfi == 0) {
         return null;
      } else {
         if (sfi != methods.length) {
            System.arraycopy(staticFactories, 0, staticFactories = new MethodBinding[sfi], 0, sfi);
         }

         MethodBinding[] compatible = new MethodBinding[sfi];
         int compatibleIndex = 0;

         for(int ix = 0; ix < sfi; ++ix) {
            MethodBinding compatibleMethod = this.computeCompatibleMethod(staticFactories[ix], argumentTypes, allocationSite);
            if (compatibleMethod != null && compatibleMethod.isValidBinding()) {
               compatible[compatibleIndex++] = compatibleMethod;
            }
         }

         if (compatibleIndex == 0) {
            return null;
         } else {
            return compatibleIndex == 1
               ? compatible[0]
               : this.mostSpecificMethodBinding(compatible, compatibleIndex, argumentTypes, allocationSite, allocationType);
         }
      }
   }

   public boolean validateNullAnnotation(long tagBits, TypeReference typeRef, Annotation[] annotations) {
      if (typeRef == null) {
         return true;
      } else {
         TypeBinding type = typeRef.resolvedType;
         boolean usesNullTypeAnnotations = this.environment().usesNullTypeAnnotations();
         long nullAnnotationTagBit;
         if (usesNullTypeAnnotations) {
            type = type.leafComponentType();
            nullAnnotationTagBit = type.tagBits & 108086391056891904L;
         } else {
            nullAnnotationTagBit = tagBits & 108086391056891904L;
         }

         if (nullAnnotationTagBit != 0L && type != null && type.isBaseType()) {
            if (typeRef.resolvedType.id != 6 || !usesNullTypeAnnotations) {
               this.problemReporter().illegalAnnotationForBaseType(typeRef, annotations, nullAnnotationTagBit);
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public abstract boolean hasDefaultNullnessFor(int var1);

   public static BlockScope typeAnnotationsResolutionScope(Scope scope) {
      BlockScope resolutionScope = null;
      switch(scope.kind) {
         case 1:
         case 2:
            resolutionScope = (BlockScope)scope;
            break;
         case 3:
            resolutionScope = ((ClassScope)scope).referenceContext.staticInitializerScope;
      }

      return resolutionScope;
   }

   public void tagAsAccessingEnclosingInstanceStateOf(ReferenceBinding enclosingType, boolean typeVariableAccess) {
      MethodScope methodScope = this.methodScope();
      if (methodScope != null
         && methodScope.referenceContext instanceof TypeDeclaration
         && !methodScope.enclosingReceiverType().isCompatibleWith(enclosingType)) {
         methodScope = methodScope.enclosingMethodScope();
      }

      MethodBinding enclosingMethod = enclosingType != null ? enclosingType.enclosingMethod() : null;

      while(methodScope != null) {
         for(; methodScope != null && methodScope.referenceContext instanceof LambdaExpression; methodScope = methodScope.enclosingMethodScope()) {
            LambdaExpression lambda = (LambdaExpression)methodScope.referenceContext;
            if (!typeVariableAccess) {
               lambda.shouldCaptureInstance = true;
            }
         }

         if (methodScope != null) {
            if (methodScope.referenceContext instanceof MethodDeclaration) {
               MethodDeclaration methodDeclaration = (MethodDeclaration)methodScope.referenceContext;
               if (methodDeclaration.binding == enclosingMethod) {
                  break;
               }

               methodDeclaration.bits &= -257;
            }

            ClassScope enclosingClassScope = methodScope.enclosingClassScope();
            if (enclosingClassScope == null) {
               break;
            }

            TypeDeclaration type = enclosingClassScope.referenceContext;
            if (type == null || type.binding == null || enclosingType == null || type.binding.isCompatibleWith(enclosingType.original())) {
               break;
            }

            methodScope = enclosingClassScope.enclosingMethodScope();
         }
      }
   }

   class MethodClashException extends RuntimeException {
      private static final long serialVersionUID = -7996779527641476028L;
   }

   public static class Substitutor {
      public ReferenceBinding[] substitute(Substitution substitution, ReferenceBinding[] originalTypes) {
         if (originalTypes == null) {
            return null;
         } else {
            ReferenceBinding[] substitutedTypes = originalTypes;
            int i = 0;

            for(int length = originalTypes.length; i < length; ++i) {
               ReferenceBinding originalType = originalTypes[i];
               TypeBinding substitutedType = this.substitute(substitution, originalType);
               if (!(substitutedType instanceof ReferenceBinding)) {
                  return null;
               }

               if (substitutedType != originalType) {
                  if (substitutedTypes == originalTypes) {
                     System.arraycopy(originalTypes, 0, substitutedTypes = new ReferenceBinding[length], 0, i);
                  }

                  substitutedTypes[i] = (ReferenceBinding)substitutedType;
               } else if (substitutedTypes != originalTypes) {
                  substitutedTypes[i] = originalType;
               }
            }

            return substitutedTypes;
         }
      }

      public TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
         if (originalType == null) {
            return null;
         } else {
            switch(originalType.kind()) {
               case 4:
                  if (originalType.isMemberType()) {
                     ReferenceBinding originalReferenceType = (ReferenceBinding)originalType.unannotated();
                     ReferenceBinding originalEnclosing = originalType.enclosingType();
                     ReferenceBinding substitutedEnclosing = originalEnclosing;
                     if (originalEnclosing != null) {
                        substitutedEnclosing = (ReferenceBinding)this.substitute(substitution, originalEnclosing);
                        if (isMemberTypeOfRaw(originalType, substitutedEnclosing)) {
                           return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
                        }
                     }

                     if (substitutedEnclosing != originalEnclosing) {
                        return (TypeBinding)(substitution.isRawSubstitution()
                           ? substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations())
                           : substitution.environment()
                              .createParameterizedType(originalReferenceType, null, substitutedEnclosing, originalType.getTypeAnnotations()));
                     }
                  }
                  break;
               case 68:
                  ArrayBinding originalArrayType = (ArrayBinding)originalType;
                  TypeBinding originalLeafComponentType = originalArrayType.leafComponentType;
                  TypeBinding substitute = this.substitute(substitution, originalLeafComponentType);
                  if (substitute != originalLeafComponentType) {
                     return originalArrayType.environment
                        .createArrayType(
                           substitute.leafComponentType(), substitute.dimensions() + originalType.dimensions(), originalType.getTypeAnnotations()
                        );
                  }
                  break;
               case 260:
                  ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding)originalType;
                  ReferenceBinding originalEnclosing = originalType.enclosingType();
                  ReferenceBinding substitutedEnclosing = originalEnclosing;
                  if (originalEnclosing != null) {
                     substitutedEnclosing = (ReferenceBinding)this.substitute(substitution, originalEnclosing);
                     if (isMemberTypeOfRaw(originalType, substitutedEnclosing)) {
                        return originalParameterizedType.environment
                           .createRawType(originalParameterizedType.genericType(), substitutedEnclosing, originalType.getTypeAnnotations());
                     }
                  }

                  TypeBinding[] originalArguments = originalParameterizedType.arguments;
                  TypeBinding[] substitutedArguments = originalArguments;
                  if (originalArguments != null) {
                     if (substitution.isRawSubstitution()) {
                        return originalParameterizedType.environment
                           .createRawType(originalParameterizedType.genericType(), substitutedEnclosing, originalType.getTypeAnnotations());
                     }

                     substitutedArguments = this.substitute(substitution, originalArguments);
                  }

                  if (substitutedArguments != originalArguments || substitutedEnclosing != originalEnclosing) {
                     return originalParameterizedType.environment
                        .createParameterizedType(
                           originalParameterizedType.genericType(), substitutedArguments, substitutedEnclosing, originalType.getTypeAnnotations()
                        );
                  }
                  break;
               case 516:
               case 8196:
                  WildcardBinding wildcard = (WildcardBinding)originalType;
                  if (wildcard.boundKind == 0) {
                     break;
                  }

                  TypeBinding originalBound = wildcard.bound;
                  TypeBinding substitutedBound = this.substitute(substitution, originalBound);
                  TypeBinding[] originalOtherBounds = wildcard.otherBounds;
                  TypeBinding[] substitutedOtherBounds = this.substitute(substitution, originalOtherBounds);
                  if (substitutedBound == originalBound && originalOtherBounds == substitutedOtherBounds) {
                     break;
                  }

                  if (originalOtherBounds != null) {
                     TypeBinding[] bounds = new TypeBinding[1 + substitutedOtherBounds.length];
                     bounds[0] = substitutedBound;
                     System.arraycopy(substitutedOtherBounds, 0, bounds, 1, substitutedOtherBounds.length);
                     TypeBinding[] glb = Scope.greaterLowerBound(bounds, null, substitution.environment());
                     if (glb != null && glb != bounds) {
                        substitutedBound = glb[0];
                        if (glb.length == 1) {
                           substitutedOtherBounds = null;
                        } else {
                           System.arraycopy(glb, 1, substitutedOtherBounds = new TypeBinding[glb.length - 1], 0, glb.length - 1);
                        }
                     }
                  }

                  return wildcard.environment
                     .createWildcard(
                        wildcard.genericType, wildcard.rank, substitutedBound, substitutedOtherBounds, wildcard.boundKind, wildcard.getTypeAnnotations()
                     );
               case 2052:
                  ReferenceBinding originalReferenceType = (ReferenceBinding)originalType.unannotated();
                  ReferenceBinding originalEnclosing = originalType.enclosingType();
                  ReferenceBinding substitutedEnclosing = originalEnclosing;
                  if (originalEnclosing != null) {
                     substitutedEnclosing = (ReferenceBinding)(
                        originalType.isStatic()
                           ? substitution.environment().convertToRawType(originalEnclosing, true)
                           : (ReferenceBinding)this.substitute(substitution, originalEnclosing)
                     );
                     if (isMemberTypeOfRaw(originalType, substitutedEnclosing)) {
                        return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
                     }
                  }

                  if (substitution.isRawSubstitution()) {
                     return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
                  }

                  TypeBinding[] originalArguments = originalReferenceType.typeVariables();
                  TypeBinding[] substitutedArguments = this.substitute(substitution, originalArguments);
                  return substitution.environment()
                     .createParameterizedType(originalReferenceType, substitutedArguments, substitutedEnclosing, originalType.getTypeAnnotations());
               case 4100:
                  return substitution.substitute((TypeVariableBinding)originalType);
               case 32772:
                  IntersectionTypeBinding18 intersection = (IntersectionTypeBinding18)originalType;
                  ReferenceBinding[] types = intersection.getIntersectingTypes();
                  TypeBinding[] substitutes = this.substitute(substitution, types);
                  ReferenceBinding[] refSubsts = new ReferenceBinding[substitutes.length];
                  System.arraycopy(substitutes, 0, refSubsts, 0, substitutes.length);
                  return substitution.environment().createIntersectionType18(refSubsts);
            }

            return originalType;
         }
      }

      private static boolean isMemberTypeOfRaw(TypeBinding originalType, ReferenceBinding substitutedEnclosing) {
         return substitutedEnclosing != null
            && substitutedEnclosing.isRawType()
            && originalType instanceof ReferenceBinding
            && !((ReferenceBinding)originalType).isStatic();
      }

      public TypeBinding[] substitute(Substitution substitution, TypeBinding[] originalTypes) {
         if (originalTypes == null) {
            return null;
         } else {
            TypeBinding[] substitutedTypes = originalTypes;
            int i = 0;

            for(int length = originalTypes.length; i < length; ++i) {
               TypeBinding originalType = originalTypes[i];
               TypeBinding substitutedParameter = this.substitute(substitution, originalType);
               if (substitutedParameter != originalType) {
                  if (substitutedTypes == originalTypes) {
                     System.arraycopy(originalTypes, 0, substitutedTypes = new TypeBinding[length], 0, i);
                  }

                  substitutedTypes[i] = substitutedParameter;
               } else if (substitutedTypes != originalTypes) {
                  substitutedTypes[i] = originalType;
               }
            }

            return substitutedTypes;
         }
      }
   }
}
