package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class WildcardBinding extends ReferenceBinding {
   public ReferenceBinding genericType;
   public int rank;
   public TypeBinding bound;
   public TypeBinding[] otherBounds;
   char[] genericSignature;
   public int boundKind;
   ReferenceBinding superclass;
   ReferenceBinding[] superInterfaces;
   TypeVariableBinding typeVariable;
   LookupEnvironment environment;
   boolean inRecursiveFunction = false;

   public WildcardBinding(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, LookupEnvironment environment) {
      this.rank = rank;
      this.boundKind = boundKind;
      this.modifiers = 1073741825;
      this.environment = environment;
      this.initialize(genericType, bound, otherBounds);
      if (genericType instanceof UnresolvedReferenceBinding) {
         ((UnresolvedReferenceBinding)genericType).addWrapper(this, environment);
      }

      if (bound instanceof UnresolvedReferenceBinding) {
         ((UnresolvedReferenceBinding)bound).addWrapper(this, environment);
      }

      this.tagBits |= 16777216L;
      this.typeBits = 134217728;
   }

   @Override
   TypeBinding bound() {
      return this.bound;
   }

   @Override
   int boundKind() {
      return this.boundKind;
   }

   public TypeBinding allBounds() {
      if (this.otherBounds != null && this.otherBounds.length != 0) {
         ReferenceBinding[] allBounds = new ReferenceBinding[this.otherBounds.length + 1];

         try {
            allBounds[0] = (ReferenceBinding)this.bound;
            System.arraycopy(this.otherBounds, 0, allBounds, 1, this.otherBounds.length);
         } catch (ClassCastException var2) {
            return this.bound;
         } catch (ArrayStoreException var3) {
            return this.bound;
         }

         return this.environment.createIntersectionType18(allBounds);
      } else {
         return this.bound;
      }
   }

   @Override
   public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
      this.tagBits |= 2097152L;
      if (annotations != null && annotations.length != 0) {
         this.typeAnnotations = annotations;
      }

      if (evalNullAnnotations) {
         this.evaluateNullAnnotations(null, null);
      }
   }

   public void evaluateNullAnnotations(Scope scope, Wildcard wildcard) {
      long nullTagBits = this.determineNullBitsFromDeclaration(scope, wildcard);
      if (nullTagBits == 0L) {
         TypeVariableBinding typeVariable2 = this.typeVariable();
         if (typeVariable2 != null) {
            long typeVariableNullTagBits = typeVariable2.tagBits & 108086391056891904L;
            if (typeVariableNullTagBits != 0L) {
               nullTagBits = typeVariableNullTagBits;
            }
         }
      }

      if (nullTagBits != 0L) {
         this.tagBits = this.tagBits & -108086391056891905L | nullTagBits | 1048576L;
      }
   }

   public long determineNullBitsFromDeclaration(Scope scope, Wildcard wildcard) {
      long nullTagBits = 0L;
      AnnotationBinding[] annotations = this.typeAnnotations;
      if (annotations != null) {
         int i = 0;

         for(int length = annotations.length; i < length; ++i) {
            AnnotationBinding annotation = annotations[i];
            if (annotation != null) {
               if (annotation.type.hasNullBit(64)) {
                  if ((nullTagBits & 72057594037927936L) == 0L) {
                     nullTagBits |= 36028797018963968L;
                  } else if (wildcard != null) {
                     Annotation annotation1 = wildcard.findAnnotation(36028797018963968L);
                     if (annotation1 != null) {
                        scope.problemReporter().contradictoryNullAnnotations(annotation1);
                     }
                  }
               } else if (annotation.type.hasNullBit(32)) {
                  if ((nullTagBits & 36028797018963968L) == 0L) {
                     nullTagBits |= 72057594037927936L;
                  } else if (wildcard != null) {
                     Annotation annotation1 = wildcard.findAnnotation(72057594037927936L);
                     if (annotation1 != null) {
                        scope.problemReporter().contradictoryNullAnnotations(annotation1);
                     }
                  }
               }
            }
         }
      }

      if (this.bound != null && this.bound.isValidBinding()) {
         long boundNullTagBits = this.bound.tagBits & 108086391056891904L;
         if (boundNullTagBits != 0L) {
            if (this.boundKind == 2) {
               if ((boundNullTagBits & 36028797018963968L) != 0L) {
                  if (nullTagBits == 0L) {
                     nullTagBits = 36028797018963968L;
                  } else if (wildcard != null && (nullTagBits & 72057594037927936L) != 0L) {
                     Annotation annotation = wildcard.bound.findAnnotation(boundNullTagBits);
                     if (annotation == null) {
                        TypeBinding newBound = this.bound.withoutToplevelNullAnnotation();
                        this.bound = newBound;
                        wildcard.bound.resolvedType = newBound;
                     } else {
                        scope.problemReporter().contradictoryNullAnnotationsOnBounds(annotation, nullTagBits);
                     }
                  }
               }
            } else {
               if ((boundNullTagBits & 72057594037927936L) != 0L) {
                  if (nullTagBits == 0L) {
                     nullTagBits = 72057594037927936L;
                  } else if (wildcard != null && (nullTagBits & 36028797018963968L) != 0L) {
                     Annotation annotation = wildcard.bound.findAnnotation(boundNullTagBits);
                     if (annotation == null) {
                        TypeBinding newBound = this.bound.withoutToplevelNullAnnotation();
                        this.bound = newBound;
                        wildcard.bound.resolvedType = newBound;
                     } else {
                        scope.problemReporter().contradictoryNullAnnotationsOnBounds(annotation, nullTagBits);
                     }
                  }
               }

               if (nullTagBits == 0L && this.otherBounds != null) {
                  int i = 0;

                  for(int length = this.otherBounds.length; i < length; ++i) {
                     if ((this.otherBounds[i].tagBits & 72057594037927936L) != 0L) {
                        nullTagBits = 72057594037927936L;
                        break;
                     }
                  }
               }
            }
         }
      }

      return nullTagBits;
   }

   @Override
   public ReferenceBinding actualType() {
      return this.genericType;
   }

   @Override
   TypeBinding[] additionalBounds() {
      return this.otherBounds;
   }

   @Override
   public int kind() {
      return this.otherBounds == null ? 516 : 8196;
   }

   public boolean boundCheck(TypeBinding argumentType) {
      switch(this.boundKind) {
         case 0:
            return true;
         case 1:
            if (!argumentType.isCompatibleWith(this.bound)) {
               return false;
            } else {
               int i = 0;

               for(int length = this.otherBounds == null ? 0 : this.otherBounds.length; i < length; ++i) {
                  if (!argumentType.isCompatibleWith(this.otherBounds[i])) {
                     return false;
                  }
               }

               return true;
            }
         default:
            return argumentType.isCompatibleWith(this.bound);
      }
   }

   @Override
   public boolean canBeInstantiated() {
      return false;
   }

   @Override
   public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
      if ((this.tagBits & 128L) != 0L) {
         missingTypes = this.bound.collectMissingTypes(missingTypes);
      }

      return missingTypes;
   }

   @Override
   public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
      if ((this.tagBits & 536870912L) != 0L) {
         if (actualType != TypeBinding.NULL && actualType.kind() != 65540) {
            if (actualType.isCapture()) {
               CaptureBinding capture = (CaptureBinding)actualType;
               actualType = capture.wildcard;
            }

            label191:
            switch(constraint) {
               case 0:
                  switch(this.boundKind) {
                     case 0:
                     default:
                        return;
                     case 1:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 2:
                                 default:
                                    return;
                                 case 1:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 0);
                                    int i = 0;

                                    for(int length = actualWildcard.otherBounds == null ? 0 : actualWildcard.otherBounds.length; i < length; ++i) {
                                       this.bound.collectSubstitutes(scope, actualWildcard.otherBounds[i], inferenceContext, 0);
                                    }

                                    return;
                              }
                           case 8196:
                              WildcardBinding actuaIntersection = (WildcardBinding)actualType;
                              this.bound.collectSubstitutes(scope, actuaIntersection.bound, inferenceContext, 0);
                              int i = 0;

                              for(int length = actuaIntersection.otherBounds == null ? 0 : actuaIntersection.otherBounds.length; i < length; ++i) {
                                 this.bound.collectSubstitutes(scope, actuaIntersection.otherBounds[i], inferenceContext, 0);
                              }
                              break label191;
                           default:
                              return;
                        }
                     case 2:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 1:
                                 default:
                                    return;
                                 case 2:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 0);
                                    int i = 0;

                                    for(int length = actualWildcard.otherBounds == null ? 0 : actualWildcard.otherBounds.length; i < length; ++i) {
                                       this.bound.collectSubstitutes(scope, actualWildcard.otherBounds[i], inferenceContext, 0);
                                    }
                                    break label191;
                              }
                           case 8196:
                           default:
                              return;
                        }
                  }
               case 1:
                  switch(this.boundKind) {
                     case 0:
                     default:
                        return;
                     case 1:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 2:
                                 default:
                                    return;
                                 case 1:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 1);
                                    return;
                              }
                           case 8196:
                              WildcardBinding actualIntersection = (WildcardBinding)actualType;
                              this.bound.collectSubstitutes(scope, actualIntersection.bound, inferenceContext, 1);
                              int i = 0;

                              for(int length = actualIntersection.otherBounds.length; i < length; ++i) {
                                 this.bound.collectSubstitutes(scope, actualIntersection.otherBounds[i], inferenceContext, 1);
                              }

                              return;
                           default:
                              this.bound.collectSubstitutes(scope, actualType, inferenceContext, 1);
                              return;
                        }
                     case 2:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 1:
                                 default:
                                    return;
                                 case 2:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 2);
                                    int i = 0;

                                    for(int length = actualWildcard.otherBounds == null ? 0 : actualWildcard.otherBounds.length; i < length; ++i) {
                                       this.bound.collectSubstitutes(scope, actualWildcard.otherBounds[i], inferenceContext, 2);
                                    }
                                    break label191;
                              }
                           case 8196:
                              return;
                           default:
                              this.bound.collectSubstitutes(scope, actualType, inferenceContext, 2);
                              return;
                        }
                  }
               case 2:
                  label147:
                  switch(this.boundKind) {
                     case 0:
                     default:
                        break;
                     case 1:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 2:
                                 default:
                                    return;
                                 case 1:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 2);
                                    int i = 0;

                                    for(int length = actualWildcard.otherBounds == null ? 0 : actualWildcard.otherBounds.length; i < length; ++i) {
                                       this.bound.collectSubstitutes(scope, actualWildcard.otherBounds[i], inferenceContext, 2);
                                    }

                                    return;
                              }
                           case 8196:
                              WildcardBinding actualIntersection = (WildcardBinding)actualType;
                              this.bound.collectSubstitutes(scope, actualIntersection.bound, inferenceContext, 2);
                              int i = 0;

                              for(int length = actualIntersection.otherBounds == null ? 0 : actualIntersection.otherBounds.length; i < length; ++i) {
                                 this.bound.collectSubstitutes(scope, actualIntersection.otherBounds[i], inferenceContext, 2);
                              }
                              break label147;
                           default:
                              return;
                        }
                     case 2:
                        switch(actualType.kind()) {
                           case 516:
                              WildcardBinding actualWildcard = (WildcardBinding)actualType;
                              switch(actualWildcard.boundKind) {
                                 case 0:
                                 case 1:
                                 default:
                                    break;
                                 case 2:
                                    this.bound.collectSubstitutes(scope, actualWildcard.bound, inferenceContext, 2);
                                    int i = 0;

                                    for(int length = actualWildcard.otherBounds == null ? 0 : actualWildcard.otherBounds.length; i < length; ++i) {
                                       this.bound.collectSubstitutes(scope, actualWildcard.otherBounds[i], inferenceContext, 2);
                                    }
                              }
                           case 8196:
                        }
                  }
            }
         }
      }
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      char[] genericTypeKey = this.genericType.computeUniqueKey(false);
      char[] rankComponent = ('{' + String.valueOf(this.rank) + '}').toCharArray();
      char[] wildCardKey;
      switch(this.boundKind) {
         case 0:
            wildCardKey = TypeConstants.WILDCARD_STAR;
            break;
         case 1:
            wildCardKey = CharOperation.concat(TypeConstants.WILDCARD_PLUS, this.bound.computeUniqueKey(false));
            break;
         default:
            wildCardKey = CharOperation.concat(TypeConstants.WILDCARD_MINUS, this.bound.computeUniqueKey(false));
      }

      return CharOperation.concat(genericTypeKey, rankComponent, wildCardKey);
   }

   @Override
   public char[] constantPoolName() {
      return this.erasure().constantPoolName();
   }

   @Override
   public TypeBinding clone(TypeBinding immaterial) {
      return new WildcardBinding(this.genericType, this.rank, this.bound, this.otherBounds, this.boundKind, this.environment);
   }

   @Override
   public String annotatedDebugName() {
      StringBuffer buffer = new StringBuffer(16);
      AnnotationBinding[] annotations = this.getTypeAnnotations();
      int i = 0;

      for(int length = annotations == null ? 0 : annotations.length; i < length; ++i) {
         buffer.append(annotations[i]);
         buffer.append(' ');
      }

      switch(this.boundKind) {
         case 0:
            return buffer.append(TypeConstants.WILDCARD_NAME).toString();
         case 1:
            if (this.otherBounds == null) {
               return buffer.append(
                     CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_EXTENDS, this.bound.annotatedDebugName().toCharArray())
                  )
                  .toString();
            }

            buffer.append(this.bound.annotatedDebugName());
            i = 0;

            for(int length = this.otherBounds.length; i < length; ++i) {
               buffer.append(" & ").append(this.otherBounds[i].annotatedDebugName());
            }

            return buffer.toString();
         default:
            return buffer.append(
                  CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_SUPER, this.bound.annotatedDebugName().toCharArray())
               )
               .toString();
      }
   }

   @Override
   public String debugName() {
      return this.toString();
   }

   @Override
   public TypeBinding erasure() {
      if (this.otherBounds == null) {
         if (this.boundKind == 1) {
            return this.bound.erasure();
         } else {
            TypeVariableBinding var = this.typeVariable();
            return (TypeBinding)(var != null ? var.erasure() : this.genericType);
         }
      } else {
         return this.bound.id == 1 ? this.otherBounds[0].erasure() : this.bound.erasure();
      }
   }

   @Override
   public char[] genericTypeSignature() {
      if (this.genericSignature == null) {
         switch(this.boundKind) {
            case 0:
               this.genericSignature = TypeConstants.WILDCARD_STAR;
               break;
            case 1:
               this.genericSignature = CharOperation.concat(TypeConstants.WILDCARD_PLUS, this.bound.genericTypeSignature());
               break;
            default:
               this.genericSignature = CharOperation.concat(TypeConstants.WILDCARD_MINUS, this.bound.genericTypeSignature());
         }
      }

      return this.genericSignature;
   }

   @Override
   public int hashCode() {
      return this.genericType.hashCode();
   }

   @Override
   public boolean hasTypeBit(int bit) {
      if (this.typeBits == 134217728) {
         this.typeBits = 0;
         if (this.superclass != null && this.superclass.hasTypeBit(-134217729)) {
            this.typeBits |= this.superclass.typeBits & 19;
         }

         if (this.superInterfaces != null) {
            int i = 0;

            for(int l = this.superInterfaces.length; i < l; ++i) {
               if (this.superInterfaces[i].hasTypeBit(-134217729)) {
                  this.typeBits |= this.superInterfaces[i].typeBits & 19;
               }
            }
         }
      }

      return (this.typeBits & bit) != 0;
   }

   void initialize(ReferenceBinding someGenericType, TypeBinding someBound, TypeBinding[] someOtherBounds) {
      this.genericType = someGenericType;
      this.bound = someBound;
      this.otherBounds = someOtherBounds;
      if (someGenericType != null) {
         this.fPackage = someGenericType.getPackage();
      }

      if (someBound != null) {
         this.tagBits |= someBound.tagBits & 2305843009751615616L;
      }

      if (someOtherBounds != null) {
         int i = 0;

         for(int max = someOtherBounds.length; i < max; ++i) {
            TypeBinding someOtherBound = someOtherBounds[i];
            this.tagBits |= someOtherBound.tagBits & 2305843009214744576L;
         }
      }
   }

   @Override
   public boolean isSuperclassOf(ReferenceBinding otherType) {
      if (this.boundKind == 2) {
         if (this.bound instanceof ReferenceBinding) {
            return ((ReferenceBinding)this.bound).isSuperclassOf(otherType);
         } else {
            return otherType.id == 1;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isIntersectionType() {
      return this.otherBounds != null;
   }

   @Override
   public ReferenceBinding[] getIntersectingTypes() {
      if (this.isIntersectionType()) {
         ReferenceBinding[] allBounds = new ReferenceBinding[this.otherBounds.length + 1];

         try {
            allBounds[0] = (ReferenceBinding)this.bound;
            System.arraycopy(this.otherBounds, 0, allBounds, 1, this.otherBounds.length);
            return allBounds;
         } catch (ClassCastException var2) {
            return null;
         } catch (ArrayStoreException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean isHierarchyConnected() {
      return this.superclass != null && this.superInterfaces != null;
   }

   @Override
   public boolean enterRecursiveFunction() {
      if (this.inRecursiveFunction) {
         return false;
      } else {
         this.inRecursiveFunction = true;
         return true;
      }
   }

   @Override
   public void exitRecursiveFunction() {
      this.inRecursiveFunction = false;
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      if (this.inRecursiveFunction) {
         return true;
      } else {
         this.inRecursiveFunction = true;

         try {
            if (this.bound != null && !this.bound.isProperType(admitCapture18)) {
               return false;
            } else if (this.superclass != null && !this.superclass.isProperType(admitCapture18)) {
               return false;
            } else if (this.superInterfaces == null) {
               return true;
            } else {
               int i = 0;

               for(int l = this.superInterfaces.length; i < l; ++i) {
                  if (!this.superInterfaces[i].isProperType(admitCapture18)) {
                     return false;
                  }
               }

               return true;
            }
         } finally {
            this.inRecursiveFunction = false;
         }
      }
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      boolean haveSubstitution = false;
      TypeBinding currentBound = this.bound;
      if (currentBound != null) {
         currentBound = currentBound.substituteInferenceVariable(var, substituteType);
         haveSubstitution |= TypeBinding.notEquals(currentBound, this.bound);
      }

      TypeBinding[] currentOtherBounds = null;
      if (this.otherBounds != null) {
         int length = this.otherBounds.length;
         if (haveSubstitution) {
            System.arraycopy(this.otherBounds, 0, currentOtherBounds = new ReferenceBinding[length], 0, length);
         }

         for(int i = 0; i < length; ++i) {
            TypeBinding currentOtherBound = this.otherBounds[i];
            if (currentOtherBound != null) {
               currentOtherBound = currentOtherBound.substituteInferenceVariable(var, substituteType);
               if (TypeBinding.notEquals(currentOtherBound, this.otherBounds[i])) {
                  if (currentOtherBounds == null) {
                     System.arraycopy(this.otherBounds, 0, currentOtherBounds = new ReferenceBinding[length], 0, length);
                  }

                  currentOtherBounds[i] = currentOtherBound;
               }
            }
         }
      }

      haveSubstitution |= currentOtherBounds != null;
      return haveSubstitution ? this.environment.createWildcard(this.genericType, this.rank, currentBound, currentOtherBounds, this.boundKind) : this;
   }

   @Override
   public boolean isUnboundWildcard() {
      return this.boundKind == 0;
   }

   @Override
   public boolean isWildcard() {
      return true;
   }

   @Override
   int rank() {
      return this.rank;
   }

   @Override
   public char[] readableName() {
      switch(this.boundKind) {
         case 0:
            return TypeConstants.WILDCARD_NAME;
         case 1:
            if (this.otherBounds == null) {
               return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_EXTENDS, this.bound.readableName());
            }

            StringBuffer buffer = new StringBuffer(10);
            buffer.append(this.bound.readableName());
            int i = 0;

            for(int length = this.otherBounds.length; i < length; ++i) {
               buffer.append('&').append(this.otherBounds[i].readableName());
            }

            char[] result = new char[i = buffer.length()];
            buffer.getChars(0, i, result, 0);
            return result;
         default:
            return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_SUPER, this.bound.readableName());
      }
   }

   @Override
   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      StringBuffer buffer = new StringBuffer(10);
      this.appendNullAnnotation(buffer, options);
      switch(this.boundKind) {
         case 0:
            buffer.append(TypeConstants.WILDCARD_NAME);
            break;
         case 1:
            if (this.otherBounds == null) {
               buffer.append(TypeConstants.WILDCARD_NAME).append(TypeConstants.WILDCARD_EXTENDS);
               buffer.append(this.bound.nullAnnotatedReadableName(options, shortNames));
            } else {
               buffer.append(this.bound.nullAnnotatedReadableName(options, shortNames));
               int i = 0;

               for(int length = this.otherBounds.length; i < length; ++i) {
                  buffer.append('&').append(this.otherBounds[i].nullAnnotatedReadableName(options, shortNames));
               }
            }
            break;
         default:
            buffer.append(TypeConstants.WILDCARD_NAME).append(TypeConstants.WILDCARD_SUPER).append(this.bound.nullAnnotatedReadableName(options, shortNames));
      }

      int length;
      char[] result = new char[length = buffer.length()];
      buffer.getChars(0, length, result, 0);
      return result;
   }

   ReferenceBinding resolve() {
      if ((this.tagBits & 16777216L) == 0L) {
         return this;
      } else {
         this.tagBits &= -16777217L;
         BinaryTypeBinding.resolveType(this.genericType, this.environment, false);
         switch(this.boundKind) {
            case 0:
            default:
               break;
            case 1: {
               TypeBinding resolveType = BinaryTypeBinding.resolveType(this.bound, this.environment, true);
               this.bound = resolveType;
               this.tagBits |= resolveType.tagBits & 2048L | 2305843009213693952L;
               int i = 0;

               for(int length = this.otherBounds == null ? 0 : this.otherBounds.length; i < length; ++i) {
                  resolveType = BinaryTypeBinding.resolveType(this.otherBounds[i], this.environment, true);
                  this.otherBounds[i] = resolveType;
                  this.tagBits |= resolveType.tagBits & 2048L | 2305843009213693952L;
               }
               break;
            }
            case 2: {
               TypeBinding resolveType = BinaryTypeBinding.resolveType(this.bound, this.environment, true);
               this.bound = resolveType;
               this.tagBits |= resolveType.tagBits & 2048L | 2305843009213693952L;
            }
         }

         if (this.environment.usesNullTypeAnnotations()) {
            this.evaluateNullAnnotations(null, null);
         }

         return this;
      }
   }

   @Override
   public char[] shortReadableName() {
      switch(this.boundKind) {
         case 0:
            return TypeConstants.WILDCARD_NAME;
         case 1:
            if (this.otherBounds == null) {
               return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_EXTENDS, this.bound.shortReadableName());
            }

            StringBuffer buffer = new StringBuffer(10);
            buffer.append(this.bound.shortReadableName());
            int i = 0;

            for(int length = this.otherBounds.length; i < length; ++i) {
               buffer.append('&').append(this.otherBounds[i].shortReadableName());
            }

            char[] result = new char[i = buffer.length()];
            buffer.getChars(0, i, result, 0);
            return result;
         default:
            return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_SUPER, this.bound.shortReadableName());
      }
   }

   @Override
   public char[] signature() {
      if (this.signature == null) {
         switch(this.boundKind) {
            case 1:
               return this.bound.signature();
            default:
               return this.typeVariable().signature();
         }
      } else {
         return this.signature;
      }
   }

   @Override
   public char[] sourceName() {
      switch(this.boundKind) {
         case 0:
            return TypeConstants.WILDCARD_NAME;
         case 1:
            return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_EXTENDS, this.bound.sourceName());
         default:
            return CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_SUPER, this.bound.sourceName());
      }
   }

   @Override
   public ReferenceBinding superclass() {
      if (this.superclass == null) {
         TypeBinding superType = null;
         if (this.boundKind == 1 && !this.bound.isInterface()) {
            superType = this.bound;
         } else {
            TypeVariableBinding variable = this.typeVariable();
            if (variable != null) {
               superType = variable.firstBound;
            }
         }

         this.superclass = superType instanceof ReferenceBinding && !superType.isInterface()
            ? (ReferenceBinding)superType
            : this.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
      }

      return this.superclass;
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      if (this.superInterfaces == null) {
         if (this.typeVariable() != null) {
            this.superInterfaces = this.typeVariable.superInterfaces();
         } else {
            this.superInterfaces = Binding.NO_SUPERINTERFACES;
         }

         if (this.boundKind == 1) {
            if (this.bound.isInterface()) {
               int length = this.superInterfaces.length;
               System.arraycopy(this.superInterfaces, 0, this.superInterfaces = new ReferenceBinding[length + 1], 1, length);
               this.superInterfaces[0] = (ReferenceBinding)this.bound;
            }

            if (this.otherBounds != null) {
               int length = this.superInterfaces.length;
               int otherLength = this.otherBounds.length;
               System.arraycopy(this.superInterfaces, 0, this.superInterfaces = new ReferenceBinding[length + otherLength], 0, length);

               for(int i = 0; i < otherLength; ++i) {
                  this.superInterfaces[length + i] = (ReferenceBinding)this.otherBounds[i];
               }
            }
         }
      }

      return this.superInterfaces;
   }

   @Override
   public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
      boolean affected = false;
      if (this.genericType == unresolvedType) {
         this.genericType = resolvedType;
         affected = true;
      }

      if (this.bound == unresolvedType) {
         this.bound = env.convertUnresolvedBinaryToRawType(resolvedType);
         affected = true;
      }

      if (this.otherBounds != null) {
         int i = 0;

         for(int length = this.otherBounds.length; i < length; ++i) {
            if (this.otherBounds[i] == unresolvedType) {
               this.otherBounds[i] = env.convertUnresolvedBinaryToRawType(resolvedType);
               affected = true;
            }
         }
      }

      if (affected) {
         this.initialize(this.genericType, this.bound, this.otherBounds);
      }
   }

   @Override
   public String toString() {
      if (this.hasTypeAnnotations()) {
         return this.annotatedDebugName();
      } else {
         switch(this.boundKind) {
            case 0:
               return new String(TypeConstants.WILDCARD_NAME);
            case 1:
               if (this.otherBounds == null) {
                  return new String(CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_EXTENDS, this.bound.debugName().toCharArray()));
               }

               StringBuffer buffer = new StringBuffer(this.bound.debugName());
               int i = 0;

               for(int length = this.otherBounds.length; i < length; ++i) {
                  buffer.append('&').append(this.otherBounds[i].debugName());
               }

               return buffer.toString();
            default:
               return new String(CharOperation.concat(TypeConstants.WILDCARD_NAME, TypeConstants.WILDCARD_SUPER, this.bound.debugName().toCharArray()));
         }
      }
   }

   public TypeVariableBinding typeVariable() {
      if (this.typeVariable == null) {
         TypeVariableBinding[] typeVariables = this.genericType.typeVariables();
         if (this.rank < typeVariables.length) {
            this.typeVariable = typeVariables[this.rank];
         }
      }

      return this.typeVariable;
   }

   @Override
   public TypeBinding unannotated() {
      return (TypeBinding)(this.hasTypeAnnotations() ? this.environment.getUnannotatedType(this) : this);
   }

   @Override
   public TypeBinding withoutToplevelNullAnnotation() {
      if (!this.hasNullTypeAnnotations()) {
         return this;
      } else {
         AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.getTypeAnnotations());
         return this.environment.createWildcard(this.genericType, this.rank, this.bound, this.otherBounds, this.boundKind, newAnnotations);
      }
   }

   @Override
   public TypeBinding uncapture(Scope scope) {
      if ((this.tagBits & 2305843009213693952L) == 0L) {
         return this;
      } else {
         TypeBinding freeBound = this.bound != null ? this.bound.uncapture(scope) : null;
         int length = 0;
         TypeBinding[] freeOtherBounds = this.otherBounds == null ? null : new TypeBinding[length = this.otherBounds.length];

         for(int i = 0; i < length; ++i) {
            freeOtherBounds[i] = this.otherBounds[i] == null ? null : this.otherBounds[i].uncapture(scope);
         }

         return scope.environment().createWildcard(this.genericType, this.rank, freeBound, freeOtherBounds, this.boundKind, this.getTypeAnnotations());
      }
   }

   @Override
   void collectInferenceVariables(Set<InferenceVariable> variables) {
      if (this.bound != null) {
         this.bound.collectInferenceVariables(variables);
      }

      if (this.otherBounds != null) {
         int i = 0;

         for(int length = this.otherBounds.length; i < length; ++i) {
            this.otherBounds[i].collectInferenceVariables(variables);
         }
      }
   }

   @Override
   public boolean mentionsAny(TypeBinding[] parameters, int idx) {
      if (this.inRecursiveFunction) {
         return false;
      } else {
         this.inRecursiveFunction = true;

         try {
            if (super.mentionsAny(parameters, idx)) {
               return true;
            } else if (this.bound != null && this.bound.mentionsAny(parameters, -1)) {
               return true;
            } else if (this.otherBounds == null) {
               return false;
            } else {
               int i = 0;

               for(int length = this.otherBounds.length; i < length; ++i) {
                  if (this.otherBounds[i].mentionsAny(parameters, -1)) {
                     return true;
                  }
               }

               return false;
            }
         } finally {
            this.inRecursiveFunction = false;
         }
      }
   }

   @Override
   public boolean acceptsNonNullDefault() {
      return false;
   }

   @Override
   public long updateTagBits() {
      if (!this.inRecursiveFunction) {
         this.inRecursiveFunction = true;

         try {
            if (this.bound != null) {
               this.tagBits |= this.bound.updateTagBits();
            }

            if (this.otherBounds != null) {
               int i = 0;

               for(int length = this.otherBounds.length; i < length; ++i) {
                  this.tagBits |= this.otherBounds[i].updateTagBits();
               }
            }
         } finally {
            this.inRecursiveFunction = false;
         }
      }

      return super.updateTagBits();
   }
}
