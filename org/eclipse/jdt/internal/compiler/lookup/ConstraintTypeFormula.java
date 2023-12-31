package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.internal.compiler.ast.Invocation;

class ConstraintTypeFormula extends ConstraintFormula {
   TypeBinding left;
   boolean isSoft;

   public static ConstraintTypeFormula create(TypeBinding exprType, TypeBinding right, int relation) {
      return exprType != null && right != null ? new ConstraintTypeFormula(exprType, right, relation, false) : FALSE;
   }

   public static ConstraintTypeFormula create(TypeBinding exprType, TypeBinding right, int relation, boolean isSoft) {
      return exprType != null && right != null ? new ConstraintTypeFormula(exprType, right, relation, isSoft) : FALSE;
   }

   private ConstraintTypeFormula(TypeBinding exprType, TypeBinding right, int relation, boolean isSoft) {
      this.left = exprType;
      this.right = right;
      this.relation = relation;
      this.isSoft = isSoft;
   }

   ConstraintTypeFormula() {
   }

   @Override
   public Object reduce(InferenceContext18 inferenceContext) {
      switch(this.relation) {
         case 1:
            if (this.left.isProperType(true) && this.right.isProperType(true)) {
               return !this.left.isCompatibleWith(this.right, inferenceContext.scope) && !this.left.isBoxingCompatibleWith(this.right, inferenceContext.scope)
                  ? FALSE
                  : TRUE;
            } else if (this.left.isPrimitiveType()) {
               TypeBinding sPrime = inferenceContext.environment.computeBoxingType(this.left);
               return create(sPrime, this.right, 1, this.isSoft);
            } else if (this.right.isPrimitiveType()) {
               TypeBinding tPrime = inferenceContext.environment.computeBoxingType(this.right);
               return create(this.left, tPrime, 4, this.isSoft);
            } else {
               switch(this.right.kind()) {
                  case 68:
                     if (this.right.leafComponentType().kind() != 260) {
                        break;
                     }
                  case 260:
                     TypeBinding gs = this.left.findSuperTypeOriginatingFrom(this.right);
                     if (gs != null && gs.leafComponentType().isRawType()) {
                        inferenceContext.recordUncheckedConversion(this);
                        return TRUE;
                     }
               }

               return create(this.left, this.right, 2, this.isSoft);
            }
         case 2:
            return this.reduceSubType(inferenceContext.scope, this.left, this.right);
         case 3:
            return this.reduceSubType(inferenceContext.scope, this.right, this.left);
         case 4:
            if (inferenceContext.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled && !this.checkIVFreeTVmatch(this.left, this.right)) {
               this.checkIVFreeTVmatch(this.right, this.left);
            }

            return this.reduceTypeEquality(inferenceContext.object);
         case 5:
            if (this.right.kind() != 516) {
               if (this.left.kind() != 516) {
                  return create(this.left, this.right, 4, this.isSoft);
               }

               return FALSE;
            } else {
               WildcardBinding t = (WildcardBinding)this.right;
               if (t.boundKind == 0) {
                  return TRUE;
               } else if (t.boundKind == 1) {
                  if (this.left.kind() != 516) {
                     return create(this.left, t.bound, 2, this.isSoft);
                  } else {
                     WildcardBinding s = (WildcardBinding)this.left;
                     switch(s.boundKind) {
                        case 0:
                           return create(inferenceContext.object, t.bound, 2, this.isSoft);
                        case 1:
                           return create(s.bound, t.bound, 2, this.isSoft);
                        case 2:
                           return create(inferenceContext.object, t.bound, 4, this.isSoft);
                        default:
                           throw new IllegalArgumentException("Unexpected boundKind " + s.boundKind);
                     }
                  }
               } else if (this.left.kind() != 516) {
                  return create(t.bound, this.left, 2, this.isSoft);
               } else {
                  WildcardBinding s = (WildcardBinding)this.left;
                  if (s.boundKind == 2) {
                     return create(t.bound, s.bound, 2, this.isSoft);
                  }

                  return FALSE;
               }
            }
         default:
            throw new IllegalStateException("Unexpected relation kind " + this.relation);
      }
   }

   boolean checkIVFreeTVmatch(TypeBinding one, TypeBinding two) {
      if (one instanceof InferenceVariable && two.isTypeVariable() && (two.tagBits & 108086391056891904L) == 0L) {
         ((InferenceVariable)one).nullHints = 108086391056891904L;
         return true;
      } else {
         return false;
      }
   }

   private Object reduceTypeEquality(TypeBinding object) {
      if (this.left.kind() == 516) {
         if (this.right.kind() == 516) {
            WildcardBinding leftWC = (WildcardBinding)this.left;
            WildcardBinding rightWC = (WildcardBinding)this.right;
            if (leftWC.boundKind == 0 && rightWC.boundKind == 0) {
               return TRUE;
            }

            if (leftWC.boundKind == 0 && rightWC.boundKind == 1) {
               return create(object, rightWC.bound, 4, this.isSoft);
            }

            if (leftWC.boundKind == 1 && rightWC.boundKind == 0) {
               return create(leftWC.bound, object, 4, this.isSoft);
            }

            if (leftWC.boundKind == 1 && rightWC.boundKind == 1 || leftWC.boundKind == 2 && rightWC.boundKind == 2) {
               return create(leftWC.bound, rightWC.bound, 4, this.isSoft);
            }
         }
      } else if (this.right.kind() != 516) {
         if (this.left.isProperType(true) && this.right.isProperType(true)) {
            if (TypeBinding.equalsEquals(this.left, this.right)) {
               return TRUE;
            }

            return FALSE;
         }

         if (this.left.id == 12 || this.right.id == 12) {
            return FALSE;
         }

         if (this.left instanceof InferenceVariable && !this.right.isPrimitiveType()) {
            return new TypeBound((InferenceVariable)this.left, this.right, 4, this.isSoft);
         }

         if (this.right instanceof InferenceVariable && !this.left.isPrimitiveType()) {
            return new TypeBound((InferenceVariable)this.right, this.left, 4, this.isSoft);
         }

         if ((this.left.isClass() || this.left.isInterface())
            && (this.right.isClass() || this.right.isInterface())
            && TypeBinding.equalsEquals(this.left.erasure(), this.right.erasure())) {
            TypeBinding[] leftParams = this.left.typeArguments();
            TypeBinding[] rightParams = this.right.typeArguments();
            if (leftParams != null && rightParams != null) {
               if (leftParams.length != rightParams.length) {
                  return FALSE;
               }

               int len = leftParams.length;
               ConstraintFormula[] constraints = new ConstraintFormula[len];

               for(int i = 0; i < len; ++i) {
                  constraints[i] = create(leftParams[i], rightParams[i], 4, this.isSoft);
               }

               return constraints;
            }

            return leftParams == rightParams ? TRUE : FALSE;
         }

         if (this.left.isArrayType() && this.right.isArrayType() && this.left.dimensions() == this.right.dimensions()) {
            return create(this.left.leafComponentType(), this.right.leafComponentType(), 4, this.isSoft);
         }
      }

      return FALSE;
   }

   private Object reduceSubType(Scope scope, TypeBinding subCandidate, TypeBinding superCandidate) {
      if (subCandidate.isProperType(true) && superCandidate.isProperType(true)) {
         return subCandidate.isCompatibleWith(superCandidate, scope) ? TRUE : FALSE;
      } else if (subCandidate.id == 12) {
         return TRUE;
      } else if (superCandidate.id == 12) {
         return FALSE;
      } else if (subCandidate instanceof InferenceVariable) {
         return new TypeBound((InferenceVariable)subCandidate, superCandidate, 2, this.isSoft);
      } else if (superCandidate instanceof InferenceVariable) {
         return new TypeBound((InferenceVariable)superCandidate, subCandidate, 3, this.isSoft);
      } else {
         switch(superCandidate.kind()) {
            case 4:
            case 1028:
            case 2052:
               if (subCandidate.isSubtypeOf(superCandidate)) {
                  return TRUE;
               }

               return FALSE;
            case 68:
               TypeBinding tPrime = ((ArrayBinding)superCandidate).elementsType();
               ArrayBinding sPrimeArray = null;
               switch(subCandidate.kind()) {
                  case 68:
                     sPrimeArray = (ArrayBinding)subCandidate;
                     break;
                  case 4100:
                     TypeVariableBinding subTVB = (TypeVariableBinding)subCandidate;
                     sPrimeArray = this.findMostSpecificSuperArray(subTVB.firstBound, subTVB.otherUpperBounds(), subTVB);
                     break;
                  case 8196:
                     WildcardBinding intersection = (WildcardBinding)subCandidate;
                     sPrimeArray = this.findMostSpecificSuperArray(intersection.bound, intersection.otherBounds, intersection);
                     break;
                  default:
                     return FALSE;
               }

               if (sPrimeArray == null) {
                  return FALSE;
               } else {
                  TypeBinding sPrime = sPrimeArray.elementsType();
                  if (!tPrime.isPrimitiveType() && !sPrime.isPrimitiveType()) {
                     return create(sPrime, tPrime, 2, this.isSoft);
                  }

                  return TypeBinding.equalsEquals(tPrime, sPrime) ? TRUE : FALSE;
               }
            case 260:
               List<ConstraintFormula> constraints;
               for(constraints = new ArrayList<>();
                  superCandidate != null && superCandidate.kind() == 260 && subCandidate != null;
                  subCandidate = subCandidate.enclosingType()
               ) {
                  if (!this.addConstraintsFromTypeParameters(subCandidate, (ParameterizedTypeBinding)superCandidate, constraints)) {
                     return FALSE;
                  }

                  superCandidate = superCandidate.enclosingType();
               }

               switch(constraints.size()) {
                  case 0:
                     return TRUE;
                  case 1:
                     return constraints.get(0);
                  default:
                     return constraints.toArray(new ConstraintFormula[constraints.size()]);
               }
            case 516:
               if (subCandidate.kind() == 8196) {
                  ReferenceBinding[] intersectingTypes = subCandidate.getIntersectingTypes();
                  if (intersectingTypes != null) {
                     for(int i = 0; i < intersectingTypes.length; ++i) {
                        if (TypeBinding.equalsEquals(intersectingTypes[i], superCandidate)) {
                           return true;
                        }
                     }
                  }
               }

               WildcardBinding variable = (WildcardBinding)superCandidate;
               if (variable.boundKind == 2) {
                  return create(subCandidate, variable.bound, 2, this.isSoft);
               }

               return FALSE;
            case 4100:
               if (subCandidate.kind() == 8196) {
                  ReferenceBinding[] intersectingTypes = subCandidate.getIntersectingTypes();
                  if (intersectingTypes != null) {
                     for(int i = 0; i < intersectingTypes.length; ++i) {
                        if (TypeBinding.equalsEquals(intersectingTypes[i], superCandidate)) {
                           return true;
                        }
                     }
                  }
               }

               if (superCandidate instanceof CaptureBinding) {
                  CaptureBinding capture = (CaptureBinding)superCandidate;
                  if (capture.lowerBound != null && (capture.firstBound == null || capture.firstBound.id == 1)) {
                     return create(subCandidate, capture.lowerBound, 2, this.isSoft);
                  }
               }

               return FALSE;
            case 8196:
               superCandidate = ((WildcardBinding)superCandidate).allBounds();
            case 32772:
               TypeBinding[] intersectingTypes = ((IntersectionTypeBinding18)superCandidate).intersectingTypes;
               ConstraintFormula[] result = new ConstraintFormula[intersectingTypes.length];

               for(int i = 0; i < intersectingTypes.length; ++i) {
                  result[i] = create(subCandidate, intersectingTypes[i], 2, this.isSoft);
               }

               return result;
            case 65540:
               PolyTypeBinding poly = (PolyTypeBinding)superCandidate;
               Invocation invocation = (Invocation)poly.expression;
               MethodBinding binding = invocation.binding();
               if (binding != null && binding.isValidBinding()) {
                  TypeBinding returnType = (TypeBinding)(binding.isConstructor() ? binding.declaringClass : binding.returnType);
                  return this.reduceSubType(scope, subCandidate, returnType.capture(scope, invocation.sourceStart(), invocation.sourceEnd()));
               }

               return FALSE;
            default:
               throw new IllegalStateException("Unexpected RHS " + superCandidate);
         }
      }
   }

   private ArrayBinding findMostSpecificSuperArray(TypeBinding firstBound, TypeBinding[] otherUpperBounds, TypeBinding theType) {
      int numArrayBounds = 0;
      ArrayBinding result = null;
      if (firstBound != null && firstBound.isArrayType()) {
         result = (ArrayBinding)firstBound;
         ++numArrayBounds;
      }

      for(int i = 0; i < otherUpperBounds.length; ++i) {
         if (otherUpperBounds[i].isArrayType()) {
            result = (ArrayBinding)otherUpperBounds[i];
            ++numArrayBounds;
         }
      }

      if (numArrayBounds == 0) {
         return null;
      } else if (numArrayBounds == 1) {
         return result;
      } else {
         InferenceContext18.missingImplementation("Extracting array from intersection is not defined");
         return null;
      }
   }

   boolean addConstraintsFromTypeParameters(TypeBinding subCandidate, ParameterizedTypeBinding ca, List<ConstraintFormula> constraints) {
      TypeBinding[] ai = ca.arguments;
      if (ai == null) {
         return true;
      } else {
         TypeBinding cb = subCandidate.findSuperTypeOriginatingFrom(ca);
         if (cb == null) {
            return false;
         } else if (TypeBinding.equalsEquals(ca, cb)) {
            return true;
         } else if (!(cb instanceof ParameterizedTypeBinding)) {
            return ca.isParameterizedWithOwnVariables();
         } else {
            TypeBinding[] bi = ((ParameterizedTypeBinding)cb).arguments;
            if (!cb.isRawType() && bi != null && bi.length != 0) {
               for(int i = 0; i < ai.length; ++i) {
                  constraints.add(create(bi[i], ai[i], 5, this.isSoft));
               }

               return true;
            } else {
               return this.isSoft;
            }
         }
      }
   }

   public boolean equalsEquals(ConstraintTypeFormula that) {
      return that != null
         && this.relation == that.relation
         && this.isSoft == that.isSoft
         && TypeBinding.equalsEquals(this.left, that.left)
         && TypeBinding.equalsEquals(this.right, that.right);
   }

   @Override
   public boolean applySubstitution(BoundSet solutionSet, InferenceVariable[] variables) {
      super.applySubstitution(solutionSet, variables);

      for(int i = 0; i < variables.length; ++i) {
         InferenceVariable variable = variables[i];
         TypeBinding instantiation = solutionSet.getInstantiation(variables[i], null);
         if (instantiation == null) {
            return false;
         }

         this.left = this.left.substituteInferenceVariable(variable, instantiation);
      }

      return true;
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer("Type Constraint:\n");
      buf.append('\t').append('⟨');
      this.appendTypeName(buf, this.left);
      buf.append(relationToString(this.relation));
      this.appendTypeName(buf, this.right);
      buf.append('⟩');
      return buf.toString();
   }
}
