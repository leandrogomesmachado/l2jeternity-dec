package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class CaptureBinding18 extends CaptureBinding {
   TypeBinding[] upperBounds;
   private char[] originalName;
   private CaptureBinding18 prototype;
   int recursionLevel = 0;

   public CaptureBinding18(
      ReferenceBinding contextType, char[] sourceName, char[] originalName, int start, int end, int captureID, LookupEnvironment environment
   ) {
      super(contextType, sourceName, start, end, captureID, environment);
      this.originalName = originalName;
      this.prototype = this;
   }

   private CaptureBinding18(CaptureBinding18 prototype) {
      super(prototype);
      this.sourceName = CharOperation.append(prototype.sourceName, '\'');
      this.originalName = prototype.originalName;
      this.upperBounds = prototype.upperBounds;
      this.prototype = prototype.prototype;
   }

   public boolean setUpperBounds(TypeBinding[] upperBounds, ReferenceBinding javaLangObject) {
      this.upperBounds = upperBounds;
      if (upperBounds.length > 0) {
         this.firstBound = upperBounds[0];
      }

      int numReferenceInterfaces = 0;
      if (!isConsistentIntersection(upperBounds)) {
         return false;
      } else {
         for(int i = 0; i < upperBounds.length; ++i) {
            TypeBinding aBound = upperBounds[i];
            if (aBound instanceof ReferenceBinding) {
               if (this.superclass == null && aBound.isClass()) {
                  this.superclass = (ReferenceBinding)aBound;
               } else if (aBound.isInterface()) {
                  ++numReferenceInterfaces;
               }
            } else if (TypeBinding.equalsEquals(aBound.leafComponentType(), this)) {
               return false;
            }
         }

         this.superInterfaces = new ReferenceBinding[numReferenceInterfaces];
         int idx = 0;

         for(int i = 0; i < upperBounds.length; ++i) {
            TypeBinding aBound = upperBounds[i];
            if (aBound.isInterface()) {
               this.superInterfaces[idx++] = (ReferenceBinding)aBound;
            }
         }

         if (this.superclass == null) {
            this.superclass = javaLangObject;
         }

         return true;
      }
   }

   @Override
   public void initializeBounds(Scope scope, ParameterizedTypeBinding capturedParameterizedType) {
   }

   @Override
   public TypeBinding clone(TypeBinding enclosingType) {
      return new CaptureBinding18(this);
   }

   @Override
   public MethodBinding[] getMethods(char[] selector) {
      return this.upperBounds.length == 1 && this.upperBounds[0] instanceof ReferenceBinding
         ? ((ReferenceBinding)this.upperBounds[0]).getMethods(selector)
         : super.getMethods(selector);
   }

   @Override
   public TypeBinding erasure() {
      if (this.upperBounds != null && this.upperBounds.length > 1) {
         ReferenceBinding[] erasures = new ReferenceBinding[this.upperBounds.length];
         boolean multipleErasures = false;

         for(int i = 0; i < this.upperBounds.length; ++i) {
            erasures[i] = (ReferenceBinding)this.upperBounds[i].erasure();
            if (i > 0 && TypeBinding.notEquals(erasures[0], erasures[i])) {
               multipleErasures = true;
            }
         }

         return (TypeBinding)(!multipleErasures ? erasures[0] : this.environment.createIntersectionType18(erasures));
      } else {
         return super.erasure();
      }
   }

   @Override
   public boolean isEquivalentTo(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else if (otherType == null) {
         return false;
      } else if (this.upperBounds == null) {
         return false;
      } else {
         for(int i = 0; i < this.upperBounds.length; ++i) {
            TypeBinding aBound = this.upperBounds[i];
            if (aBound != null && aBound.isArrayType()) {
               if (!aBound.isCompatibleWith(otherType)) {
                  return false;
               }
            } else {
               switch(otherType.kind()) {
                  case 516:
                  case 8196:
                     if (!((WildcardBinding)otherType).boundCheck(aBound)) {
                        return false;
                     }
               }
            }
         }

         return true;
      }
   }

   @Override
   public boolean isCompatibleWith(TypeBinding otherType, Scope captureScope) {
      if (TypeBinding.equalsEquals(this, otherType)) {
         return true;
      } else if (this.inRecursiveFunction) {
         return true;
      } else {
         this.inRecursiveFunction = true;

         try {
            if (this.upperBounds == null) {
               return false;
            } else {
               int length = this.upperBounds.length;
               int rightKind = otherType.kind();
               TypeBinding[] rightIntersectingTypes = null;
               if (rightKind == 8196 && otherType.boundKind() == 1) {
                  TypeBinding allRightBounds = ((WildcardBinding)otherType).allBounds();
                  if (allRightBounds instanceof IntersectionTypeBinding18) {
                     rightIntersectingTypes = ((IntersectionTypeBinding18)allRightBounds).intersectingTypes;
                  }
               } else if (rightKind == 32772) {
                  rightIntersectingTypes = ((IntersectionTypeBinding18)otherType).intersectingTypes;
               }

               if (rightIntersectingTypes == null) {
                  for(int i = 0; i < length; ++i) {
                     if (this.upperBounds[i].isCompatibleWith(otherType, captureScope)) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  int numRequired = rightIntersectingTypes.length;
                  TypeBinding[] required = new TypeBinding[numRequired];
                  System.arraycopy(rightIntersectingTypes, 0, required, 0, numRequired);

                  for(int i = 0; i < length; ++i) {
                     TypeBinding provided = this.upperBounds[i];

                     for(int j = 0; j < required.length; ++j) {
                        if (required[j] != null && provided.isCompatibleWith(required[j], captureScope)) {
                           required[j] = null;
                           if (--numRequired == 0) {
                              return true;
                           }
                           break;
                        }
                     }
                  }

                  return false;
               }
            }
         } finally {
            this.inRecursiveFunction = false;
         }
      }
   }

   @Override
   public TypeBinding findSuperTypeOriginatingFrom(TypeBinding otherType) {
      if (this.upperBounds != null && this.upperBounds.length > 1) {
         for(int i = 0; i < this.upperBounds.length; ++i) {
            TypeBinding candidate = this.upperBounds[i].findSuperTypeOriginatingFrom(otherType);
            if (candidate != null) {
               return candidate;
            }
         }
      }

      return super.findSuperTypeOriginatingFrom(otherType);
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      if (this.inRecursiveFunction) {
         return this;
      } else {
         this.inRecursiveFunction = true;

         CaptureBinding18 var11;
         try {
            boolean haveSubstitution = false;
            ReferenceBinding currentSuperclass = this.superclass;
            if (currentSuperclass != null) {
               currentSuperclass = (ReferenceBinding)currentSuperclass.substituteInferenceVariable(var, substituteType);
               haveSubstitution |= TypeBinding.notEquals(currentSuperclass, this.superclass);
            }

            ReferenceBinding[] currentSuperInterfaces = null;
            if (this.superInterfaces != null) {
               int length = this.superInterfaces.length;
               if (haveSubstitution) {
                  System.arraycopy(this.superInterfaces, 0, currentSuperInterfaces = new ReferenceBinding[length], 0, length);
               }

               for(int i = 0; i < length; ++i) {
                  ReferenceBinding currentSuperInterface = this.superInterfaces[i];
                  if (currentSuperInterface != null) {
                     currentSuperInterface = (ReferenceBinding)currentSuperInterface.substituteInferenceVariable(var, substituteType);
                     if (TypeBinding.notEquals(currentSuperInterface, this.superInterfaces[i])) {
                        if (currentSuperInterfaces == null) {
                           System.arraycopy(this.superInterfaces, 0, currentSuperInterfaces = new ReferenceBinding[length], 0, length);
                        }

                        currentSuperInterfaces[i] = currentSuperInterface;
                        haveSubstitution = true;
                     }
                  }
               }
            }

            TypeBinding[] currentUpperBounds = null;
            if (this.upperBounds != null) {
               int length = this.upperBounds.length;
               if (haveSubstitution) {
                  System.arraycopy(this.upperBounds, 0, currentUpperBounds = new TypeBinding[length], 0, length);
               }

               for(int i = 0; i < length; ++i) {
                  TypeBinding currentBound = this.upperBounds[i];
                  if (currentBound != null) {
                     currentBound = currentBound.substituteInferenceVariable(var, substituteType);
                     if (TypeBinding.notEquals(currentBound, this.upperBounds[i])) {
                        if (currentUpperBounds == null) {
                           System.arraycopy(this.upperBounds, 0, currentUpperBounds = new TypeBinding[length], 0, length);
                        }

                        currentUpperBounds[i] = currentBound;
                        haveSubstitution = true;
                     }
                  }
               }
            }

            TypeBinding currentFirstBound = null;
            if (this.firstBound != null) {
               currentFirstBound = this.firstBound.substituteInferenceVariable(var, substituteType);
               haveSubstitution |= TypeBinding.notEquals(this.firstBound, currentFirstBound);
            }

            if (!haveSubstitution) {
               return this;
            }

            final CaptureBinding18 newCapture = (CaptureBinding18)this.clone(this.enclosingType());
            newCapture.tagBits = this.tagBits;
            Substitution substitution = new Substitution() {
               @Override
               public TypeBinding substitute(TypeVariableBinding typeVariable) {
                  return (TypeBinding)(typeVariable == CaptureBinding18.this ? newCapture : typeVariable);
               }

               @Override
               public boolean isRawSubstitution() {
                  return false;
               }

               @Override
               public LookupEnvironment environment() {
                  return CaptureBinding18.this.environment;
               }
            };
            if (currentFirstBound != null) {
               newCapture.firstBound = Scope.substitute(substitution, currentFirstBound);
            }

            newCapture.superclass = (ReferenceBinding)Scope.substitute(substitution, currentSuperclass);
            newCapture.superInterfaces = Scope.substitute(substitution, currentSuperInterfaces);
            newCapture.upperBounds = Scope.substitute(substitution, currentUpperBounds);
            var11 = newCapture;
         } finally {
            this.inRecursiveFunction = false;
         }

         return var11;
      }
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      if (!admitCapture18) {
         return false;
      } else if (this.inRecursiveFunction) {
         return true;
      } else {
         this.inRecursiveFunction = true;

         try {
            if (this.lowerBound != null && !this.lowerBound.isProperType(admitCapture18)) {
               return false;
            } else if (this.upperBounds == null) {
               return true;
            } else {
               for(int i = 0; i < this.upperBounds.length; ++i) {
                  if (!this.upperBounds[i].isProperType(admitCapture18)) {
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
   public char[] genericTypeSignature() {
      if (this.genericTypeSignature == null) {
         try {
            char[] boundSignature;
            if (this.prototype.recursionLevel++ > 0 || this.firstBound == null) {
               boundSignature = TypeConstants.WILDCARD_STAR;
            } else if (this.upperBounds != null) {
               boundSignature = CharOperation.concat(TypeConstants.WILDCARD_PLUS, this.firstBound.genericTypeSignature());
            } else if (this.lowerBound != null) {
               boundSignature = CharOperation.concat(TypeConstants.WILDCARD_MINUS, this.lowerBound.genericTypeSignature());
            } else {
               boundSignature = TypeConstants.WILDCARD_STAR;
            }

            this.genericTypeSignature = CharOperation.concat(TypeConstants.WILDCARD_CAPTURE, boundSignature);
         } finally {
            --this.prototype.recursionLevel;
         }
      }

      return this.genericTypeSignature;
   }

   @Override
   public char[] readableName() {
      if (this.lowerBound != null || this.firstBound == null) {
         return super.readableName();
      } else if (this.prototype.recursionLevel >= 2) {
         return this.originalName;
      } else {
         char[] var5;
         try {
            ++this.prototype.recursionLevel;
            if (this.upperBounds == null || this.upperBounds.length <= 1) {
               return this.firstBound.readableName();
            }

            StringBuffer sb = new StringBuffer();
            sb.append(this.upperBounds[0].readableName());

            for(int i = 1; i < this.upperBounds.length; ++i) {
               sb.append('&').append(this.upperBounds[i].readableName());
            }

            int len = sb.length();
            char[] name = new char[len];
            sb.getChars(0, len, name, 0);
            var5 = name;
         } finally {
            --this.prototype.recursionLevel;
         }

         return var5;
      }
   }

   @Override
   public char[] shortReadableName() {
      if (this.lowerBound != null || this.firstBound == null) {
         return super.shortReadableName();
      } else if (this.prototype.recursionLevel >= 2) {
         return this.originalName;
      } else {
         char[] var5;
         try {
            ++this.prototype.recursionLevel;
            if (this.upperBounds == null || this.upperBounds.length <= 1) {
               return this.firstBound.shortReadableName();
            }

            StringBuffer sb = new StringBuffer();
            sb.append(this.upperBounds[0].shortReadableName());

            for(int i = 1; i < this.upperBounds.length; ++i) {
               sb.append('&').append(this.upperBounds[i].shortReadableName());
            }

            int len = sb.length();
            char[] name = new char[len];
            sb.getChars(0, len, name, 0);
            var5 = name;
         } finally {
            --this.prototype.recursionLevel;
         }

         return var5;
      }
   }

   @Override
   public TypeBinding uncapture(Scope scope) {
      return this;
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(TypeConstants.CAPTURE18);
      buffer.append('{').append(this.end).append('#').append(this.captureID).append('}');
      buffer.append(';');
      int length = buffer.length();
      char[] uniqueKey = new char[length];
      buffer.getChars(0, length, uniqueKey, 0);
      return uniqueKey;
   }
}
