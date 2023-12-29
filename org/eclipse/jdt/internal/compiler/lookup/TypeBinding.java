package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class TypeBinding extends Binding {
   public int id = Integer.MAX_VALUE;
   public long tagBits = 0L;
   protected AnnotationBinding[] typeAnnotations = Binding.NO_ANNOTATIONS;
   public static final ReferenceBinding TYPE_USE_BINDING = new ReferenceBinding() {
      {
         this.id = 0;
      }

      @Override
      public int kind() {
         return 16388;
      }

      @Override
      public boolean hasTypeBit(int bit) {
         return false;
      }
   };
   public static final BaseTypeBinding INT = new BaseTypeBinding(10, TypeConstants.INT, new char[]{'I'});
   public static final BaseTypeBinding BYTE = new BaseTypeBinding(3, TypeConstants.BYTE, new char[]{'B'});
   public static final BaseTypeBinding SHORT = new BaseTypeBinding(4, TypeConstants.SHORT, new char[]{'S'});
   public static final BaseTypeBinding CHAR = new BaseTypeBinding(2, TypeConstants.CHAR, new char[]{'C'});
   public static final BaseTypeBinding LONG = new BaseTypeBinding(7, TypeConstants.LONG, new char[]{'J'});
   public static final BaseTypeBinding FLOAT = new BaseTypeBinding(9, TypeConstants.FLOAT, new char[]{'F'});
   public static final BaseTypeBinding DOUBLE = new BaseTypeBinding(8, TypeConstants.DOUBLE, new char[]{'D'});
   public static final BaseTypeBinding BOOLEAN = new BaseTypeBinding(5, TypeConstants.BOOLEAN, new char[]{'Z'});
   public static final NullTypeBinding NULL = new NullTypeBinding();
   public static final VoidTypeBinding VOID = new VoidTypeBinding();

   public TypeBinding() {
   }

   public TypeBinding(TypeBinding prototype) {
      this.id = prototype.id;
      this.tagBits = prototype.tagBits & -108086391056891905L;
   }

   public static final TypeBinding wellKnownType(Scope scope, int id) {
      switch(id) {
         case 1:
            return scope.getJavaLangObject();
         case 2:
            return CHAR;
         case 3:
            return BYTE;
         case 4:
            return SHORT;
         case 5:
            return BOOLEAN;
         case 6:
         default:
            return null;
         case 7:
            return LONG;
         case 8:
            return DOUBLE;
         case 9:
            return FLOAT;
         case 10:
            return INT;
         case 11:
            return scope.getJavaLangString();
      }
   }

   public ReferenceBinding actualType() {
      return null;
   }

   TypeBinding[] additionalBounds() {
      return null;
   }

   public String annotatedDebugName() {
      TypeBinding enclosingType = this.enclosingType();
      StringBuffer buffer = new StringBuffer(16);
      if (enclosingType != null) {
         buffer.append(enclosingType.annotatedDebugName());
         buffer.append('.');
      }

      AnnotationBinding[] annotations = this.getTypeAnnotations();
      int i = 0;

      for(int length = annotations == null ? 0 : annotations.length; i < length; ++i) {
         buffer.append(annotations[i]);
         buffer.append(' ');
      }

      buffer.append(this.sourceName());
      return buffer.toString();
   }

   TypeBinding bound() {
      return null;
   }

   int boundKind() {
      return -1;
   }

   int rank() {
      return -1;
   }

   public ReferenceBinding containerAnnotationType() {
      return null;
   }

   public boolean canBeInstantiated() {
      return !this.isBaseType();
   }

   public TypeBinding capture(Scope scope, int start, int end) {
      return this;
   }

   public TypeBinding uncapture(Scope scope) {
      return this;
   }

   public TypeBinding closestMatch() {
      return this;
   }

   public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
      return missingTypes;
   }

   public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
   }

   public TypeBinding clone(TypeBinding enclosingType) {
      throw new IllegalStateException("TypeBinding#clone() should have been overridden");
   }

   public abstract char[] constantPoolName();

   public String debugName() {
      return this.hasTypeAnnotations() ? this.annotatedDebugName() : new String(this.readableName());
   }

   public int dimensions() {
      return 0;
   }

   public int depth() {
      return 0;
   }

   public MethodBinding enclosingMethod() {
      return null;
   }

   public ReferenceBinding enclosingType() {
      return null;
   }

   public TypeBinding erasure() {
      return this;
   }

   public ReferenceBinding findSuperTypeOriginatingFrom(int wellKnownOriginalID, boolean originalIsClass) {
      if (!(this instanceof ReferenceBinding)) {
         return null;
      } else {
         ReferenceBinding reference = (ReferenceBinding)this;
         if (reference.id != wellKnownOriginalID && this.original().id != wellKnownOriginalID) {
            ReferenceBinding currentType = reference;
            if (originalIsClass) {
               while((currentType = currentType.superclass()) != null) {
                  if (currentType.id == wellKnownOriginalID) {
                     return currentType;
                  }

                  if (currentType.original().id == wellKnownOriginalID) {
                     return currentType;
                  }
               }

               return null;
            } else {
               ReferenceBinding[] interfacesToVisit = null;
               int nextPosition = 0;

               do {
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

                        label114:
                        for(int a = 0; a < itsLength; ++a) {
                           ReferenceBinding next = itsInterfaces[a];

                           for(int b = 0; b < nextPosition; ++b) {
                              if (equalsEquals(next, interfacesToVisit[b])) {
                                 continue label114;
                              }
                           }

                           interfacesToVisit[nextPosition++] = next;
                        }
                     }
                  }
               } while((currentType = currentType.superclass()) != null);

               for(int i = 0; i < nextPosition; ++i) {
                  currentType = interfacesToVisit[i];
                  if (currentType.id == wellKnownOriginalID) {
                     return currentType;
                  }

                  if (currentType.original().id == wellKnownOriginalID) {
                     return currentType;
                  }

                  ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
                  if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                     int itsLength = itsInterfaces.length;
                     if (nextPosition + itsLength >= interfacesToVisit.length) {
                        System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                     }

                     label87:
                     for(int a = 0; a < itsLength; ++a) {
                        ReferenceBinding next = itsInterfaces[a];

                        for(int b = 0; b < nextPosition; ++b) {
                           if (equalsEquals(next, interfacesToVisit[b])) {
                              continue label87;
                           }
                        }

                        interfacesToVisit[nextPosition++] = next;
                     }
                  }
               }

               return null;
            }
         } else {
            return reference;
         }
      }
   }

   public TypeBinding findSuperTypeOriginatingFrom(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return this;
      } else if (otherType == null) {
         return null;
      } else {
         switch(this.kind()) {
            case 68:
               ArrayBinding arrayType = (ArrayBinding)this;
               int otherDim = otherType.dimensions();
               if (arrayType.dimensions != otherDim) {
                  switch(otherType.id) {
                     case 1:
                     case 36:
                     case 37:
                        return otherType;
                     default:
                        if (otherDim < arrayType.dimensions && otherType.leafComponentType().id == 1) {
                           return otherType;
                        }

                        return null;
                  }
               }

               if (!(arrayType.leafComponentType instanceof ReferenceBinding)) {
                  return null;
               }

               TypeBinding leafSuperType = arrayType.leafComponentType.findSuperTypeOriginatingFrom(otherType.leafComponentType());
               if (leafSuperType == null) {
                  return null;
               }

               return arrayType.environment().createArrayType(leafSuperType, arrayType.dimensions);
            case 4100:
               if (this.isCapture()) {
                  CaptureBinding capture = (CaptureBinding)this;
                  TypeBinding captureBound = capture.firstBound;
                  if (captureBound instanceof ArrayBinding) {
                     TypeBinding match = captureBound.findSuperTypeOriginatingFrom(otherType);
                     if (match != null) {
                        return match;
                     }
                  }
               }
            case 4:
            case 260:
            case 516:
            case 1028:
            case 2052:
            case 8196:
               otherType = otherType.original();
               if (equalsEquals(this, otherType)) {
                  return this;
               }

               if (equalsEquals(this.original(), otherType)) {
                  return this;
               }

               ReferenceBinding currentType = (ReferenceBinding)this;
               if (!otherType.isInterface()) {
                  while((currentType = currentType.superclass()) != null) {
                     if (equalsEquals(currentType, otherType)) {
                        return currentType;
                     }

                     if (equalsEquals(currentType.original(), otherType)) {
                        return currentType;
                     }
                  }

                  return null;
               }

               ReferenceBinding[] interfacesToVisit = null;
               int nextPosition = 0;

               do {
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

                        label168:
                        for(int a = 0; a < itsLength; ++a) {
                           ReferenceBinding next = itsInterfaces[a];

                           for(int b = 0; b < nextPosition; ++b) {
                              if (equalsEquals(next, interfacesToVisit[b])) {
                                 continue label168;
                              }
                           }

                           interfacesToVisit[nextPosition++] = next;
                        }
                     }
                  }
               } while((currentType = currentType.superclass()) != null);

               for(int i = 0; i < nextPosition; ++i) {
                  currentType = interfacesToVisit[i];
                  if (equalsEquals(currentType, otherType)) {
                     return currentType;
                  }

                  if (equalsEquals(currentType.original(), otherType)) {
                     return currentType;
                  }

                  ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
                  if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
                     int itsLength = itsInterfaces.length;
                     if (nextPosition + itsLength >= interfacesToVisit.length) {
                        System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                     }

                     label142:
                     for(int a = 0; a < itsLength; ++a) {
                        ReferenceBinding next = itsInterfaces[a];

                        for(int b = 0; b < nextPosition; ++b) {
                           if (equalsEquals(next, interfacesToVisit[b])) {
                              continue label142;
                           }
                        }

                        interfacesToVisit[nextPosition++] = next;
                     }
                  }
               }
               break;
            case 32772:
               IntersectionTypeBinding18 itb18 = (IntersectionTypeBinding18)this;
               ReferenceBinding[] intersectingTypes = itb18.getIntersectingTypes();
               int i = 0;

               for(int length = intersectingTypes.length; i < length; ++i) {
                  TypeBinding superType = intersectingTypes[i].findSuperTypeOriginatingFrom(otherType);
                  if (superType != null) {
                     return superType;
                  }
               }
         }

         return null;
      }
   }

   public TypeBinding genericCast(TypeBinding targetType) {
      if (equalsEquals(this, targetType)) {
         return null;
      } else {
         TypeBinding targetErasure = targetType.erasure();
         return this.erasure().findSuperTypeOriginatingFrom(targetErasure) != null ? null : targetErasure;
      }
   }

   public char[] genericTypeSignature() {
      return this.signature();
   }

   public TypeBinding getErasureCompatibleType(TypeBinding declaringClass) {
      switch(this.kind()) {
         case 4100:
            TypeVariableBinding variable = (TypeVariableBinding)this;
            if (variable.erasure().findSuperTypeOriginatingFrom(declaringClass) != null) {
               return this;
            } else if (variable.superclass != null && variable.superclass.findSuperTypeOriginatingFrom(declaringClass) != null) {
               return variable.superclass.getErasureCompatibleType(declaringClass);
            } else {
               int i = 0;

               for(int otherLength = variable.superInterfaces.length; i < otherLength; ++i) {
                  ReferenceBinding superInterface = variable.superInterfaces[i];
                  if (superInterface.findSuperTypeOriginatingFrom(declaringClass) != null) {
                     return superInterface.getErasureCompatibleType(declaringClass);
                  }
               }

               return this;
            }
         case 8196:
            WildcardBinding intersection = (WildcardBinding)this;
            if (intersection.erasure().findSuperTypeOriginatingFrom(declaringClass) != null) {
               return this;
            } else if (intersection.superclass != null && intersection.superclass.findSuperTypeOriginatingFrom(declaringClass) != null) {
               return intersection.superclass.getErasureCompatibleType(declaringClass);
            } else {
               int i = 0;

               for(int otherLength = intersection.superInterfaces.length; i < otherLength; ++i) {
                  ReferenceBinding superInterface = intersection.superInterfaces[i];
                  if (superInterface.findSuperTypeOriginatingFrom(declaringClass) != null) {
                     return superInterface.getErasureCompatibleType(declaringClass);
                  }
               }

               return this;
            }
         default:
            return this;
      }
   }

   public abstract PackageBinding getPackage();

   void initializeForStaticImports() {
   }

   public final boolean isAnonymousType() {
      return (this.tagBits & 32L) != 0L;
   }

   public final boolean isArrayType() {
      return (this.tagBits & 1L) != 0L;
   }

   public final boolean isBaseType() {
      return (this.tagBits & 2L) != 0L;
   }

   public final boolean isPrimitiveType() {
      return (this.tagBits & 2L) != 0L && this.id != 6 && this.id != 12;
   }

   public final boolean isPrimitiveOrBoxedPrimitiveType() {
      if (this.isPrimitiveType()) {
         return true;
      } else {
         switch(this.id) {
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
               return true;
            default:
               return false;
         }
      }
   }

   public boolean isBoxedPrimitiveType() {
      switch(this.id) {
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
            return true;
         default:
            return false;
      }
   }

   public boolean isBoundParameterizedType() {
      return false;
   }

   public boolean isCapture() {
      return false;
   }

   public boolean isClass() {
      return false;
   }

   public boolean isCompatibleWith(TypeBinding right) {
      return this.isCompatibleWith(right, null);
   }

   public abstract boolean isCompatibleWith(TypeBinding var1, Scope var2);

   public boolean isPotentiallyCompatibleWith(TypeBinding right, Scope scope) {
      return this.isCompatibleWith(right, scope);
   }

   public boolean isBoxingCompatibleWith(TypeBinding right, Scope scope) {
      if (right == null) {
         return false;
      } else if (equalsEquals(this, right)) {
         return true;
      } else if (this.isCompatibleWith(right, scope)) {
         return true;
      } else {
         if (this.isBaseType() != right.isBaseType()) {
            TypeBinding convertedType = scope.environment().computeBoxingType(this);
            if (equalsEquals(convertedType, right) || convertedType.isCompatibleWith(right, scope)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isEnum() {
      return false;
   }

   public boolean isEquivalentTo(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else if (otherType == null) {
         return false;
      } else {
         switch(otherType.kind()) {
            case 516:
            case 8196:
               return ((WildcardBinding)otherType).boundCheck(this);
            default:
               return false;
         }
      }
   }

   public boolean isGenericType() {
      return false;
   }

   public final boolean isHierarchyInconsistent() {
      return (this.tagBits & 131072L) != 0L;
   }

   public boolean isInterface() {
      return false;
   }

   public boolean isFunctionalInterface(Scope scope) {
      return false;
   }

   public boolean isIntersectionType() {
      return false;
   }

   public final boolean isLocalType() {
      return (this.tagBits & 16L) != 0L;
   }

   public final boolean isMemberType() {
      return (this.tagBits & 8L) != 0L;
   }

   public final boolean isNestedType() {
      return (this.tagBits & 4L) != 0L;
   }

   public final boolean isNumericType() {
      switch(this.id) {
         case 2:
         case 3:
         case 4:
         case 7:
         case 8:
         case 9:
         case 10:
            return true;
         case 5:
         case 6:
         default:
            return false;
      }
   }

   public boolean isParameterizedType() {
      return false;
   }

   public boolean hasNullTypeAnnotations() {
      return (this.tagBits & 1048576L) != 0L;
   }

   public boolean acceptsNonNullDefault() {
      return false;
   }

   public boolean isIntersectionType18() {
      return false;
   }

   public final boolean isParameterizedTypeWithActualArguments() {
      return this.kind() == 260 && ((ParameterizedTypeBinding)this).arguments != null;
   }

   public boolean isParameterizedWithOwnVariables() {
      if (this.kind() != 260) {
         return false;
      } else {
         ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)this;
         if (paramType.arguments == null) {
            return false;
         } else {
            TypeVariableBinding[] variables = this.erasure().typeVariables();
            int i = 0;

            for(int length = variables.length; i < length; ++i) {
               if (notEquals(variables[i], paramType.arguments[i])) {
                  return false;
               }
            }

            ReferenceBinding enclosing = paramType.enclosingType();
            return enclosing == null || !enclosing.erasure().isGenericType() || enclosing.isParameterizedWithOwnVariables();
         }
      }
   }

   public boolean isProperType(boolean admitCapture18) {
      return true;
   }

   public boolean isPolyType() {
      return false;
   }

   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      return this;
   }

   private boolean isProvableDistinctSubType(TypeBinding otherType) {
      if (otherType.isInterface()) {
         if (this.isInterface()) {
            return false;
         } else if (this.isArrayType()
            || this instanceof ReferenceBinding && ((ReferenceBinding)this).isFinal()
            || this.isTypeVariable() && ((TypeVariableBinding)this).superclass().isFinal()) {
            return !this.isCompatibleWith(otherType);
         } else {
            return false;
         }
      } else {
         if (!this.isInterface()) {
            if (!this.isTypeVariable() && !otherType.isTypeVariable()) {
               return !this.isCompatibleWith(otherType);
            }
         } else if (otherType.isArrayType()
            || otherType instanceof ReferenceBinding && ((ReferenceBinding)otherType).isFinal()
            || otherType.isTypeVariable() && ((TypeVariableBinding)otherType).superclass().isFinal()) {
            return !this.isCompatibleWith(otherType);
         }

         return false;
      }
   }

   public boolean isProvablyDistinct(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return false;
      } else if (otherType == null) {
         return true;
      } else {
         switch(this.kind()) {
            case 4:
               switch(otherType.kind()) {
                  case 260:
                  case 1028:
                     return notEquals(this, otherType.erasure());
               }
            default:
               return true;
            case 260:
               ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)this;
               switch(otherType.kind()) {
                  case 4:
                     return notEquals(this.erasure(), otherType);
                  case 260:
                     ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding)otherType;
                     if (notEquals(paramType.genericType(), otherParamType.genericType())) {
                        return true;
                     } else {
                        if (!paramType.isStatic()) {
                           ReferenceBinding enclosing = this.enclosingType();
                           if (enclosing != null) {
                              ReferenceBinding otherEnclosing = otherParamType.enclosingType();
                              if (otherEnclosing == null) {
                                 return true;
                              }

                              if ((otherEnclosing.tagBits & 1073741824L) == 0L) {
                                 if (enclosing.isProvablyDistinct(otherEnclosing)) {
                                    return true;
                                 }
                              } else if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) {
                                 return true;
                              }
                           }
                        }

                        int length = paramType.arguments == null ? 0 : paramType.arguments.length;
                        TypeBinding[] otherArguments = otherParamType.arguments;
                        int otherLength = otherArguments == null ? 0 : otherArguments.length;
                        if (otherLength != length) {
                           return true;
                        } else {
                           for(int i = 0; i < length; ++i) {
                              if (paramType.arguments[i].isProvablyDistinctTypeArgument(otherArguments[i], paramType, i)) {
                                 return true;
                              }
                           }

                           return false;
                        }
                     }
                  case 1028:
                     return notEquals(this.erasure(), otherType.erasure());
                  case 2052:
                     if (notEquals(paramType.genericType(), otherType)) {
                        return true;
                     } else {
                        if (!paramType.isStatic()) {
                           ReferenceBinding enclosing = this.enclosingType();
                           if (enclosing != null) {
                              ReferenceBinding otherEnclosing = otherType.enclosingType();
                              if (otherEnclosing == null) {
                                 return true;
                              }

                              if ((otherEnclosing.tagBits & 1073741824L) == 0L) {
                                 if (notEquals(enclosing, otherEnclosing)) {
                                    return true;
                                 }
                              } else if (!enclosing.isEquivalentTo(otherType.enclosingType())) {
                                 return true;
                              }
                           }
                        }

                        int length = paramType.arguments == null ? 0 : paramType.arguments.length;
                        TypeBinding[] otherArguments = otherType.typeVariables();
                        int otherLength = otherArguments == null ? 0 : otherArguments.length;
                        if (otherLength != length) {
                           return true;
                        } else {
                           for(int i = 0; i < length; ++i) {
                              if (paramType.arguments[i].isProvablyDistinctTypeArgument(otherArguments[i], paramType, i)) {
                                 return true;
                              }
                           }

                           return false;
                        }
                     }
                  default:
                     return true;
               }
            case 1028:
               switch(otherType.kind()) {
                  case 4:
                  case 260:
                  case 1028:
                  case 2052:
                     return notEquals(this.erasure(), otherType.erasure());
                  default:
                     return true;
               }
         }
      }
   }

   private boolean isProvablyDistinctTypeArgument(TypeBinding otherArgument, ParameterizedTypeBinding paramType, int rank) {
      if (equalsEquals(this, otherArgument)) {
         return false;
      } else {
         TypeBinding upperBound1;
         TypeBinding lowerBound1;
         ReferenceBinding genericType;
         upperBound1 = null;
         lowerBound1 = null;
         genericType = paramType.genericType();
         label154:
         switch(this.kind()) {
            case 516:
               WildcardBinding wildcard = (WildcardBinding)this;
               switch(wildcard.boundKind) {
                  case 0:
                     return false;
                  case 1:
                     upperBound1 = wildcard.bound;
                     break label154;
                  case 2:
                     lowerBound1 = wildcard.bound;
                  default:
                     break label154;
               }
            case 4100:
               TypeVariableBinding variable = (TypeVariableBinding)this;
               if (variable.isCapture()) {
                  if (variable instanceof CaptureBinding18) {
                     CaptureBinding18 cb18 = (CaptureBinding18)variable;
                     upperBound1 = cb18.firstBound;
                     lowerBound1 = cb18.lowerBound;
                  } else {
                     CaptureBinding capture = (CaptureBinding)variable;
                     switch(capture.wildcard.boundKind) {
                        case 0:
                           return false;
                        case 1:
                           upperBound1 = capture.wildcard.bound;
                           break;
                        case 2:
                           lowerBound1 = capture.wildcard.bound;
                     }
                  }
               } else {
                  if (variable.firstBound == null) {
                     return false;
                  }

                  TypeBinding eliminatedType = Scope.convertEliminatingTypeVariables(variable, genericType, rank, null);
                  switch(eliminatedType.kind()) {
                     case 516:
                     case 8196:
                        WildcardBinding wildcard = (WildcardBinding)eliminatedType;
                        switch(wildcard.boundKind) {
                           case 0:
                              return false;
                           case 1:
                              upperBound1 = wildcard.bound;
                              break;
                           case 2:
                              lowerBound1 = wildcard.bound;
                        }
                  }
               }
            case 8196:
         }

         TypeBinding upperBound2;
         TypeBinding lowerBound2;
         upperBound2 = null;
         lowerBound2 = null;
         label139:
         switch(otherArgument.kind()) {
            case 516:
               WildcardBinding otherWildcard = (WildcardBinding)otherArgument;
               switch(otherWildcard.boundKind) {
                  case 0:
                     return false;
                  case 1:
                     upperBound2 = otherWildcard.bound;
                     break label139;
                  case 2:
                     lowerBound2 = otherWildcard.bound;
                  default:
                     break label139;
               }
            case 4100:
               TypeVariableBinding otherVariable = (TypeVariableBinding)otherArgument;
               if (otherVariable.isCapture()) {
                  if (otherVariable instanceof CaptureBinding18) {
                     CaptureBinding18 cb18 = (CaptureBinding18)otherVariable;
                     upperBound2 = cb18.firstBound;
                     lowerBound2 = cb18.lowerBound;
                  } else {
                     CaptureBinding otherCapture = (CaptureBinding)otherVariable;
                     switch(otherCapture.wildcard.boundKind) {
                        case 0:
                           return false;
                        case 1:
                           upperBound2 = otherCapture.wildcard.bound;
                           break;
                        case 2:
                           lowerBound2 = otherCapture.wildcard.bound;
                     }
                  }
               } else {
                  if (otherVariable.firstBound == null) {
                     return false;
                  }

                  TypeBinding otherEliminatedType = Scope.convertEliminatingTypeVariables(otherVariable, genericType, rank, null);
                  switch(otherEliminatedType.kind()) {
                     case 516:
                     case 8196:
                        WildcardBinding otherWildcard = (WildcardBinding)otherEliminatedType;
                        switch(otherWildcard.boundKind) {
                           case 0:
                              return false;
                           case 1:
                              upperBound2 = otherWildcard.bound;
                              break;
                           case 2:
                              lowerBound2 = otherWildcard.bound;
                        }
                  }
               }
            case 8196:
         }

         if (lowerBound1 != null) {
            if (lowerBound2 != null) {
               return false;
            } else if (upperBound2 != null) {
               if (!lowerBound1.isTypeVariable() && !upperBound2.isTypeVariable()) {
                  return !lowerBound1.isCompatibleWith(upperBound2);
               } else {
                  return false;
               }
            } else if (!lowerBound1.isTypeVariable() && !otherArgument.isTypeVariable()) {
               return !lowerBound1.isCompatibleWith(otherArgument);
            } else {
               return false;
            }
         } else if (upperBound1 != null) {
            if (lowerBound2 != null) {
               return !lowerBound2.isCompatibleWith(upperBound1);
            } else if (upperBound2 != null) {
               return upperBound1.isProvableDistinctSubType(upperBound2) && upperBound2.isProvableDistinctSubType(upperBound1);
            } else {
               return otherArgument.isProvableDistinctSubType(upperBound1);
            }
         } else if (lowerBound2 != null) {
            if (!lowerBound2.isTypeVariable() && !this.isTypeVariable()) {
               return !lowerBound2.isCompatibleWith(this);
            } else {
               return false;
            }
         } else {
            return upperBound2 != null ? this.isProvableDistinctSubType(upperBound2) : true;
         }
      }
   }

   public boolean isRepeatableAnnotationType() {
      return false;
   }

   public final boolean isRawType() {
      return this.kind() == 1028;
   }

   public boolean isReifiable() {
      TypeBinding leafType = this.leafComponentType();
      if (!(leafType instanceof ReferenceBinding)) {
         return true;
      } else {
         ReferenceBinding current = (ReferenceBinding)leafType;

         while(true) {
            switch(current.kind()) {
               case 260:
                  if (current.isBoundParameterizedType()) {
                     return false;
                  }
               default:
                  if (current.isStatic()) {
                     return true;
                  }

                  if (current.isLocalType()) {
                     LocalTypeBinding localTypeBinding = (LocalTypeBinding)current.erasure();
                     MethodBinding enclosingMethod = localTypeBinding.enclosingMethod;
                     if (enclosingMethod != null && enclosingMethod.isStatic()) {
                        return true;
                     }
                  }

                  if ((current = current.enclosingType()) == null) {
                     return true;
                  }
                  break;
               case 516:
               case 2052:
               case 4100:
               case 8196:
                  return false;
               case 1028:
                  return true;
            }
         }
      }
   }

   public boolean isStatic() {
      return false;
   }

   public boolean isThrowable() {
      return false;
   }

   public boolean isTypeArgumentContainedBy(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else {
         switch(otherType.kind()) {
            case 260:
               if (!this.isParameterizedType()) {
                  return false;
               } else {
                  ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)this;
                  ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding)otherType;
                  if (notEquals(paramType.actualType(), otherParamType.actualType())) {
                     return false;
                  } else {
                     if (!paramType.isStatic()) {
                        ReferenceBinding enclosing = this.enclosingType();
                        if (enclosing != null) {
                           ReferenceBinding otherEnclosing = otherParamType.enclosingType();
                           if (otherEnclosing == null) {
                              return false;
                           }

                           if ((otherEnclosing.tagBits & 1073741824L) == 0L) {
                              if (notEquals(enclosing, otherEnclosing)) {
                                 return false;
                              }
                           } else if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) {
                              return false;
                           }
                        }
                     }

                     int length = paramType.arguments == null ? 0 : paramType.arguments.length;
                     TypeBinding[] otherArguments = otherParamType.arguments;
                     int otherLength = otherArguments == null ? 0 : otherArguments.length;
                     if (otherLength != length) {
                        return false;
                     } else {
                        for(int i = 0; i < length; ++i) {
                           TypeBinding argument = paramType.arguments[i];
                           TypeBinding otherArgument = otherArguments[i];
                           if (!equalsEquals(argument, otherArgument)) {
                              int kind = argument.kind();
                              if (otherArgument.kind() != kind) {
                                 return false;
                              }

                              switch(kind) {
                                 case 260:
                                    if (!argument.isTypeArgumentContainedBy(otherArgument)) {
                                       return false;
                                    }
                                    break;
                                 case 516:
                                 case 8196:
                                    WildcardBinding wildcard = (WildcardBinding)argument;
                                    WildcardBinding otherWildcard = (WildcardBinding)otherArgument;
                                    switch(wildcard.boundKind) {
                                       case 0:
                                          if (otherWildcard.boundKind != 1 || !equalsEquals(otherWildcard.bound, otherWildcard.typeVariable().upperBound())) {
                                             return false;
                                          }
                                          continue;
                                       case 1:
                                          if (otherWildcard.boundKind != 0 || !equalsEquals(wildcard.bound, wildcard.typeVariable().upperBound())) {
                                             return false;
                                          }
                                          continue;
                                       case 2:
                                       default:
                                          return false;
                                    }
                                 default:
                                    return false;
                              }
                           }
                        }

                        return true;
                     }
                  }
               }
            case 516:
            case 8196:
               TypeBinding lowerBound;
               TypeBinding upperBound;
               lowerBound = this;
               upperBound = this;
               label166:
               switch(this.kind()) {
                  case 516:
                  case 8196:
                     WildcardBinding wildcard = (WildcardBinding)this;
                     switch(wildcard.boundKind) {
                        case 0:
                           upperBound = wildcard;
                           lowerBound = null;
                           break label166;
                        case 1:
                           if (wildcard.otherBounds == null) {
                              upperBound = wildcard.bound;
                              lowerBound = null;
                           }
                           break label166;
                        case 2:
                           upperBound = wildcard;
                           lowerBound = wildcard.bound;
                        default:
                           break label166;
                     }
                  case 4100:
                     if (this.isCapture()) {
                        CaptureBinding capture = (CaptureBinding)this;
                        if (capture.lowerBound != null) {
                           lowerBound = capture.lowerBound;
                        }
                     }
               }

               WildcardBinding otherWildcard = (WildcardBinding)otherType;
               if (otherWildcard.otherBounds != null) {
                  return false;
               } else {
                  TypeBinding otherBound = otherWildcard.bound;
                  switch(otherWildcard.boundKind) {
                     case 0:
                     default:
                        return true;
                     case 1:
                        if (otherBound instanceof IntersectionTypeBinding18) {
                           TypeBinding[] intersectingTypes = ((IntersectionTypeBinding18)otherBound).intersectingTypes;
                           int i = 0;

                           for(int length = intersectingTypes.length; i < length; ++i) {
                              if (equalsEquals(intersectingTypes[i], this)) {
                                 return true;
                              }
                           }
                        }

                        if (equalsEquals(otherBound, this)) {
                           return true;
                        } else if (upperBound == null) {
                           return false;
                        } else {
                           TypeBinding match = upperBound.findSuperTypeOriginatingFrom(otherBound);
                           if (match != null && (match = match.leafComponentType()).isRawType()) {
                              return equalsEquals(match, otherBound.leafComponentType());
                           }

                           return upperBound.isCompatibleWith(otherBound);
                        }
                     case 2:
                        if (otherBound instanceof IntersectionTypeBinding18) {
                           TypeBinding[] intersectingTypes = ((IntersectionTypeBinding18)otherBound).intersectingTypes;
                           int i = 0;

                           for(int length = intersectingTypes.length; i < length; ++i) {
                              if (equalsEquals(intersectingTypes[i], this)) {
                                 return true;
                              }
                           }
                        }

                        if (equalsEquals(otherBound, this)) {
                           return true;
                        } else if (lowerBound == null) {
                           return false;
                        } else {
                           TypeBinding match = otherBound.findSuperTypeOriginatingFrom(lowerBound);
                           if (match != null && (match = match.leafComponentType()).isRawType()) {
                              return equalsEquals(match, lowerBound.leafComponentType());
                           }

                           return otherBound.isCompatibleWith(lowerBound);
                        }
                  }
               }
            case 4100:
               if (this.isParameterizedType() && otherType.isCapture()) {
                  CaptureBinding capture = (CaptureBinding)otherType;
                  WildcardBinding var16;
                  if (capture instanceof CaptureBinding18) {
                     CaptureBinding18 cb18 = (CaptureBinding18)capture;
                     if (cb18.firstBound != null) {
                        if (cb18.lowerBound != null) {
                           return false;
                        }

                        TypeBinding[] otherBounds = null;
                        int len = cb18.upperBounds.length;
                        if (len > 1) {
                           System.arraycopy(cb18.upperBounds, 1, otherBounds = new TypeBinding[len - 1], 0, len - 1);
                        }

                        var16 = capture.environment.createWildcard(null, 0, cb18.firstBound, otherBounds, 1);
                     } else {
                        if (cb18.lowerBound == null) {
                           return false;
                        }

                        var16 = capture.environment.createWildcard(null, 0, cb18.lowerBound, null, 2);
                     }
                  } else {
                     TypeBinding upperBound = null;
                     TypeBinding[] otherBounds = null;
                     WildcardBinding wildcard = capture.wildcard;
                     switch(wildcard.boundKind) {
                        case 0:
                           TypeVariableBinding variable = wildcard.genericType.typeVariables()[wildcard.rank];
                           upperBound = variable.upperBound();
                           otherBounds = variable.boundsCount() > 1 ? variable.otherUpperBounds() : null;
                           break;
                        case 1:
                           upperBound = wildcard.bound;
                           otherBounds = wildcard.otherBounds;
                           break;
                        case 2:
                           return false;
                     }

                     if (upperBound.id == 1 && otherBounds == null) {
                        return false;
                     }

                     var16 = capture.environment.createWildcard(null, 0, upperBound, otherBounds, 1);
                  }

                  return this.isTypeArgumentContainedBy(var16);
               }

               return false;
            default:
               if (otherType.id == 1) {
                  switch(this.kind()) {
                     case 516:
                        WildcardBinding wildcard = (WildcardBinding)this;
                        if (wildcard.boundKind == 2 && wildcard.bound.id == 1) {
                           return true;
                        }
                  }
               }

               return false;
         }
      }
   }

   public boolean isTypeVariable() {
      return false;
   }

   public boolean isUnboundWildcard() {
      return false;
   }

   public boolean isUncheckedException(boolean includeSupertype) {
      return false;
   }

   public boolean isWildcard() {
      return false;
   }

   @Override
   public int kind() {
      return 4;
   }

   public TypeBinding leafComponentType() {
      return this;
   }

   public boolean needsUncheckedConversion(TypeBinding targetType) {
      if (equalsEquals(this, targetType)) {
         return false;
      } else {
         TypeBinding var5 = targetType.leafComponentType();
         if (!(var5 instanceof ReferenceBinding)) {
            return false;
         } else {
            TypeBinding currentType = this.leafComponentType();
            TypeBinding match = currentType.findSuperTypeOriginatingFrom((TypeBinding)var5);
            if (!(match instanceof ReferenceBinding)) {
               return false;
            } else {
               ReferenceBinding compatible = (ReferenceBinding)match;

               while(compatible.isRawType()) {
                  if (((TypeBinding)var5).isBoundParameterizedType()) {
                     return true;
                  }

                  if (compatible.isStatic() || (compatible = compatible.enclosingType()) == null || (var5 = ((TypeBinding)var5).enclosingType()) == null) {
                     break;
                  }
               }

               return false;
            }
         }
      }
   }

   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      return shortNames ? this.shortReadableName() : this.readableName();
   }

   public TypeBinding original() {
      switch(this.kind()) {
         case 68:
         case 260:
         case 1028:
            return this.erasure().unannotated();
         default:
            return this.unannotated();
      }
   }

   public TypeBinding unannotated() {
      return this;
   }

   public TypeBinding withoutToplevelNullAnnotation() {
      return this;
   }

   public final boolean hasTypeAnnotations() {
      return (this.tagBits & 2097152L) != 0L;
   }

   public char[] qualifiedPackageName() {
      PackageBinding packageBinding = this.getPackage();
      return packageBinding != null && packageBinding.compoundName != CharOperation.NO_CHAR_CHAR ? packageBinding.readableName() : CharOperation.NO_CHAR;
   }

   public abstract char[] qualifiedSourceName();

   public final AnnotationBinding[] getTypeAnnotations() {
      return this.typeAnnotations;
   }

   public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
      this.tagBits |= 2097152L;
      if (annotations != null && annotations.length != 0) {
         this.typeAnnotations = annotations;
         if (evalNullAnnotations) {
            int i = 0;

            for(int length = annotations.length; i < length; ++i) {
               AnnotationBinding annotation = annotations[i];
               if (annotation != null) {
                  if (annotation.type.hasNullBit(64)) {
                     this.tagBits |= 36028797020012544L;
                  } else if (annotation.type.hasNullBit(32)) {
                     this.tagBits |= 72057594038976512L;
                  }
               }
            }
         }
      }
   }

   public char[] signableName() {
      return this.readableName();
   }

   public char[] signature() {
      return this.constantPoolName();
   }

   public abstract char[] sourceName();

   public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment environment) {
   }

   TypeBinding[] typeArguments() {
      return null;
   }

   public TypeVariableBinding[] typeVariables() {
      return Binding.NO_TYPE_VARIABLES;
   }

   public MethodBinding getSingleAbstractMethod(Scope scope, boolean replaceWildcards) {
      return null;
   }

   public ReferenceBinding[] getIntersectingTypes() {
      return null;
   }

   public static boolean equalsEquals(TypeBinding that, TypeBinding other) {
      if (that == other) {
         return true;
      } else if (that == null || other == null) {
         return false;
      } else {
         return that.id != Integer.MAX_VALUE && that.id == other.id;
      }
   }

   public static boolean notEquals(TypeBinding that, TypeBinding other) {
      if (that == other) {
         return false;
      } else if (that == null || other == null) {
         return true;
      } else {
         return that.id == Integer.MAX_VALUE || that.id != other.id;
      }
   }

   public TypeBinding prototype() {
      return null;
   }

   public boolean isUnresolvedType() {
      return false;
   }

   public boolean mentionsAny(TypeBinding[] parameters, int idx) {
      for(int i = 0; i < parameters.length; ++i) {
         if (i != idx && equalsEquals(parameters[i], this)) {
            return true;
         }
      }

      return false;
   }

   void collectInferenceVariables(Set<InferenceVariable> variables) {
   }

   public boolean hasTypeBit(int bit) {
      return false;
   }

   public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope scope) {
      return s.isCompatibleWith(t, scope) && !s.needsUncheckedConversion(t);
   }

   public boolean isSubtypeOf(TypeBinding right) {
      return this.isCompatibleWith(right);
   }

   public MethodBinding[] getMethods(char[] selector) {
      return Binding.NO_METHODS;
   }

   public boolean canBeSeenBy(Scope scope) {
      return true;
   }

   public ReferenceBinding superclass() {
      return null;
   }

   public ReferenceBinding[] superInterfaces() {
      return Binding.NO_SUPERINTERFACES;
   }

   public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
      return null;
   }

   public boolean enterRecursiveFunction() {
      return true;
   }

   public void exitRecursiveFunction() {
   }

   public boolean isFunctionalType() {
      return false;
   }

   public long updateTagBits() {
      return this.tagBits & 1048576L;
   }

   public boolean isFreeTypeVariable() {
      return false;
   }
}
