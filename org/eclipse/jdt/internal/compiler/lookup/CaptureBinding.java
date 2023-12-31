package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CaptureBinding extends TypeVariableBinding {
   public TypeBinding lowerBound;
   public WildcardBinding wildcard;
   public int captureID;
   public ReferenceBinding sourceType;
   public int start;
   public int end;
   public ASTNode cud;
   TypeBinding pendingSubstitute;

   public CaptureBinding(WildcardBinding wildcard, ReferenceBinding sourceType, int start, int end, ASTNode cud, int captureID) {
      super(TypeConstants.WILDCARD_CAPTURE_NAME_PREFIX, wildcard.environment);
      this.wildcard = wildcard;
      this.modifiers = 1073741825;
      this.fPackage = wildcard.fPackage;
      this.sourceType = sourceType;
      this.start = start;
      this.end = end;
      this.captureID = captureID;
      this.tagBits |= 2305843009213693952L;
      this.cud = cud;
      if (wildcard.hasTypeAnnotations()) {
         CaptureBinding unannotated = (CaptureBinding)this.clone(null);
         unannotated.wildcard = (WildcardBinding)this.wildcard.unannotated();
         this.environment.getUnannotatedType(unannotated);
         this.id = unannotated.id;
         this.environment.typeSystem.cacheDerivedType(this, unannotated, this);
         super.setTypeAnnotations(wildcard.getTypeAnnotations(), wildcard.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
         if (wildcard.hasNullTypeAnnotations()) {
            this.tagBits |= 1048576L;
         }
      } else {
         this.computeId(this.environment);
      }
   }

   protected CaptureBinding(ReferenceBinding sourceType, char[] sourceName, int start, int end, int captureID, LookupEnvironment environment) {
      super(sourceName, null, 0, environment);
      this.modifiers = 1073741825;
      this.sourceType = sourceType;
      this.start = start;
      this.end = end;
      this.captureID = captureID;
   }

   public CaptureBinding(CaptureBinding prototype) {
      super(prototype);
      this.wildcard = prototype.wildcard;
      this.sourceType = prototype.sourceType;
      this.start = prototype.start;
      this.end = prototype.end;
      this.captureID = prototype.captureID;
      this.lowerBound = prototype.lowerBound;
      this.tagBits |= prototype.tagBits & 2305843009213693952L;
      this.cud = prototype.cud;
   }

   @Override
   public TypeBinding clone(TypeBinding enclosingType) {
      return new CaptureBinding(this);
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      StringBuffer buffer = new StringBuffer();
      if (isLeaf) {
         buffer.append(this.sourceType.computeUniqueKey(false));
         buffer.append('&');
      }

      buffer.append(TypeConstants.WILDCARD_CAPTURE);
      buffer.append(this.wildcard.computeUniqueKey(false));
      buffer.append(this.end);
      buffer.append(';');
      int length = buffer.length();
      char[] uniqueKey = new char[length];
      buffer.getChars(0, length, uniqueKey, 0);
      return uniqueKey;
   }

   @Override
   public String debugName() {
      if (this.wildcard == null) {
         return super.debugName();
      } else {
         StringBuffer buffer = new StringBuffer(10);
         AnnotationBinding[] annotations = this.getTypeAnnotations();
         int i = 0;

         for(int length = annotations == null ? 0 : annotations.length; i < length; ++i) {
            buffer.append(annotations[i]);
            buffer.append(' ');
         }

         buffer.append(TypeConstants.WILDCARD_CAPTURE_NAME_PREFIX)
            .append(this.captureID)
            .append(TypeConstants.WILDCARD_CAPTURE_NAME_SUFFIX)
            .append(this.wildcard.debugName());
         return buffer.toString();
      }
   }

   @Override
   public char[] genericTypeSignature() {
      if (this.genericTypeSignature == null) {
         this.genericTypeSignature = CharOperation.concat(TypeConstants.WILDCARD_CAPTURE, this.wildcard.genericTypeSignature());
      }

      return this.genericTypeSignature;
   }

   public void initializeBounds(Scope scope, ParameterizedTypeBinding capturedParameterizedType) {
      TypeVariableBinding wildcardVariable = this.wildcard.typeVariable();
      if (wildcardVariable == null) {
         TypeBinding originalWildcardBound = this.wildcard.bound;
         switch(this.wildcard.boundKind) {
            case 0:
               this.setSuperClass(scope.getJavaLangObject());
               this.setSuperInterfaces(Binding.NO_SUPERINTERFACES);
               this.tagBits &= -536870913L;
               break;
            case 1:
               TypeBinding capturedWildcardBound = originalWildcardBound.capture(scope, this.start, this.end);
               if (originalWildcardBound.isInterface()) {
                  this.setSuperClass(scope.getJavaLangObject());
                  this.setSuperInterfaces(new ReferenceBinding[]{(ReferenceBinding)capturedWildcardBound});
               } else {
                  if (!capturedWildcardBound.isArrayType() && !TypeBinding.equalsEquals(capturedWildcardBound, this)) {
                     this.setSuperClass((ReferenceBinding)capturedWildcardBound);
                  } else {
                     this.setSuperClass(scope.getJavaLangObject());
                  }

                  this.setSuperInterfaces(Binding.NO_SUPERINTERFACES);
               }

               this.setFirstBound(capturedWildcardBound);
               if ((capturedWildcardBound.tagBits & 536870912L) == 0L) {
                  this.tagBits &= -536870913L;
               }
               break;
            case 2:
               this.setSuperClass(scope.getJavaLangObject());
               this.setSuperInterfaces(Binding.NO_SUPERINTERFACES);
               this.lowerBound = this.wildcard.bound;
               if ((originalWildcardBound.tagBits & 536870912L) == 0L) {
                  this.tagBits &= -536870913L;
               }
         }
      } else {
         ReferenceBinding originalVariableSuperclass = wildcardVariable.superclass;
         ReferenceBinding substitutedVariableSuperclass = (ReferenceBinding)Scope.substitute(capturedParameterizedType, originalVariableSuperclass);
         if (TypeBinding.equalsEquals(substitutedVariableSuperclass, this)) {
            substitutedVariableSuperclass = originalVariableSuperclass;
         }

         ReferenceBinding[] originalVariableInterfaces = wildcardVariable.superInterfaces();
         ReferenceBinding[] substitutedVariableInterfaces = Scope.substitute(capturedParameterizedType, originalVariableInterfaces);
         if (substitutedVariableInterfaces != originalVariableInterfaces) {
            int i = 0;

            for(int length = substitutedVariableInterfaces.length; i < length; ++i) {
               if (TypeBinding.equalsEquals(substitutedVariableInterfaces[i], this)) {
                  substitutedVariableInterfaces[i] = originalVariableInterfaces[i];
               }
            }
         }

         TypeBinding originalWildcardBound = this.wildcard.bound;
         switch(this.wildcard.boundKind) {
            case 0:
               this.setSuperClass(substitutedVariableSuperclass);
               this.setSuperInterfaces(substitutedVariableInterfaces);
               this.tagBits &= -536870913L;
               break;
            case 1:
               TypeBinding capturedWildcardBound = originalWildcardBound.capture(scope, this.start, this.end);
               if (originalWildcardBound.isInterface()) {
                  this.setSuperClass(substitutedVariableSuperclass);
                  if (substitutedVariableInterfaces == Binding.NO_SUPERINTERFACES) {
                     this.setSuperInterfaces(new ReferenceBinding[]{(ReferenceBinding)capturedWildcardBound});
                  } else {
                     int length = substitutedVariableInterfaces.length;
                     ReferenceBinding[] var13;
                     System.arraycopy(substitutedVariableInterfaces, 0, var13 = new ReferenceBinding[length + 1], 1, length);
                     var13[0] = (ReferenceBinding)capturedWildcardBound;
                     this.setSuperInterfaces(Scope.greaterLowerBound(var13));
                  }
               } else {
                  if (!capturedWildcardBound.isArrayType() && !TypeBinding.equalsEquals(capturedWildcardBound, this)) {
                     this.setSuperClass((ReferenceBinding)capturedWildcardBound);
                     if (this.superclass.isSuperclassOf(substitutedVariableSuperclass)) {
                        this.setSuperClass(substitutedVariableSuperclass);
                     }
                  } else {
                     this.setSuperClass(substitutedVariableSuperclass);
                  }

                  this.setSuperInterfaces(substitutedVariableInterfaces);
               }

               this.setFirstBound(capturedWildcardBound);
               if ((capturedWildcardBound.tagBits & 536870912L) == 0L) {
                  this.tagBits &= -536870913L;
               }
               break;
            case 2:
               this.setSuperClass(substitutedVariableSuperclass);
               if (TypeBinding.equalsEquals(wildcardVariable.firstBound, substitutedVariableSuperclass)
                  || TypeBinding.equalsEquals(originalWildcardBound, substitutedVariableSuperclass)) {
                  this.setFirstBound(substitutedVariableSuperclass);
               }

               this.setSuperInterfaces(substitutedVariableInterfaces);
               this.lowerBound = originalWildcardBound;
               if ((originalWildcardBound.tagBits & 536870912L) == 0L) {
                  this.tagBits &= -536870913L;
               }
         }

         if (scope.environment().usesNullTypeAnnotations()) {
            this.evaluateNullAnnotations(scope, null);
         }
      }
   }

   @Override
   public boolean isCapture() {
      return true;
   }

   @Override
   public boolean isEquivalentTo(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else if (otherType == null) {
         return false;
      } else if (this.firstBound != null && this.firstBound.isArrayType() && this.firstBound.isCompatibleWith(otherType)) {
         return true;
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

   @Override
   public boolean isProperType(boolean admitCapture18) {
      if (this.lowerBound != null && !this.lowerBound.isProperType(admitCapture18)) {
         return false;
      } else {
         return this.wildcard != null && !this.wildcard.isProperType(admitCapture18) ? false : super.isProperType(admitCapture18);
      }
   }

   @Override
   public char[] readableName() {
      if (this.wildcard != null) {
         StringBuffer buffer = new StringBuffer(10);
         buffer.append(TypeConstants.WILDCARD_CAPTURE_NAME_PREFIX)
            .append(this.captureID)
            .append(TypeConstants.WILDCARD_CAPTURE_NAME_SUFFIX)
            .append(this.wildcard.readableName());
         int length = buffer.length();
         char[] name = new char[length];
         buffer.getChars(0, length, name, 0);
         return name;
      } else {
         return super.readableName();
      }
   }

   @Override
   public char[] signableName() {
      if (this.wildcard != null) {
         StringBuffer buffer = new StringBuffer(10);
         buffer.append(TypeConstants.WILDCARD_CAPTURE_SIGNABLE_NAME_SUFFIX).append(this.wildcard.readableName());
         int length = buffer.length();
         char[] name = new char[length];
         buffer.getChars(0, length, name, 0);
         return name;
      } else {
         return super.readableName();
      }
   }

   @Override
   public char[] shortReadableName() {
      if (this.wildcard != null) {
         StringBuffer buffer = new StringBuffer(10);
         buffer.append(TypeConstants.WILDCARD_CAPTURE_NAME_PREFIX)
            .append(this.captureID)
            .append(TypeConstants.WILDCARD_CAPTURE_NAME_SUFFIX)
            .append(this.wildcard.shortReadableName());
         int length = buffer.length();
         char[] name = new char[length];
         buffer.getChars(0, length, name, 0);
         return name;
      } else {
         return super.shortReadableName();
      }
   }

   @Override
   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      StringBuffer nameBuffer = new StringBuffer(10);
      this.appendNullAnnotation(nameBuffer, options);
      nameBuffer.append(this.sourceName());
      if (!this.inRecursiveFunction) {
         this.inRecursiveFunction = true;

         try {
            if (this.wildcard != null) {
               nameBuffer.append("of ");
               nameBuffer.append(this.wildcard.withoutToplevelNullAnnotation().nullAnnotatedReadableName(options, shortNames));
            } else if (this.lowerBound != null) {
               nameBuffer.append(" super ");
               nameBuffer.append(this.lowerBound.nullAnnotatedReadableName(options, shortNames));
            } else if (this.firstBound != null) {
               nameBuffer.append(" extends ");
               nameBuffer.append(this.firstBound.nullAnnotatedReadableName(options, shortNames));
               TypeBinding[] otherUpperBounds = this.otherUpperBounds();
               if (otherUpperBounds != NO_TYPES) {
                  nameBuffer.append(" & ...");
               }
            }
         } finally {
            this.inRecursiveFunction = false;
         }
      }

      int nameLength = nameBuffer.length();
      char[] readableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, readableName, 0);
      return readableName;
   }

   @Override
   public TypeBinding withoutToplevelNullAnnotation() {
      if (!this.hasNullTypeAnnotations()) {
         return this;
      } else {
         if (this.wildcard != null && this.wildcard.hasNullTypeAnnotations()) {
            WildcardBinding newWildcard = (WildcardBinding)this.wildcard.withoutToplevelNullAnnotation();
            if (newWildcard != this.wildcard) {
               CaptureBinding newCapture = (CaptureBinding)this.environment.getUnannotatedType(this).clone(null);
               if (newWildcard.hasTypeAnnotations()) {
                  newCapture.tagBits |= 2097152L;
               }

               newCapture.wildcard = newWildcard;
               newCapture.superclass = this.superclass;
               newCapture.superInterfaces = this.superInterfaces;
               AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.typeAnnotations);
               return this.environment.createAnnotatedType(newCapture, newAnnotations);
            }
         }

         return super.withoutToplevelNullAnnotation();
      }
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      if (this.pendingSubstitute != null) {
         return this.pendingSubstitute;
      } else {
         CaptureBinding var8;
         try {
            TypeBinding substitutedWildcard = this.wildcard.substituteInferenceVariable(var, substituteType);
            if (substitutedWildcard == this.wildcard) {
               return this;
            }

            CaptureBinding substitute = (CaptureBinding)this.clone(this.enclosingType());
            substitute.wildcard = (WildcardBinding)substitutedWildcard;
            this.pendingSubstitute = substitute;
            if (this.lowerBound != null) {
               substitute.lowerBound = this.lowerBound.substituteInferenceVariable(var, substituteType);
            }

            if (this.firstBound != null) {
               substitute.firstBound = this.firstBound.substituteInferenceVariable(var, substituteType);
            }

            if (this.superclass != null) {
               substitute.superclass = (ReferenceBinding)this.superclass.substituteInferenceVariable(var, substituteType);
            }

            if (this.superInterfaces != null) {
               int length = this.superInterfaces.length;
               substitute.superInterfaces = new ReferenceBinding[length];

               for(int i = 0; i < length; ++i) {
                  substitute.superInterfaces[i] = (ReferenceBinding)this.superInterfaces[i].substituteInferenceVariable(var, substituteType);
               }
            }

            var8 = substitute;
         } finally {
            this.pendingSubstitute = null;
         }

         return var8;
      }
   }

   @Override
   public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
      super.setTypeAnnotations(annotations, evalNullAnnotations);
      if (annotations != Binding.NO_ANNOTATIONS && this.wildcard != null) {
         this.wildcard = (WildcardBinding)this.wildcard.environment.createAnnotatedType(this.wildcard, annotations);
      }
   }

   @Override
   public TypeBinding uncapture(Scope scope) {
      return this.wildcard;
   }

   @Override
   protected TypeBinding[] getDerivedTypesForDeferredInitialization() {
      TypeBinding[] derived = this.environment.typeSystem.getDerivedTypes(this);
      if (derived.length > 0) {
         int count = 0;

         for(int i = 0; i < derived.length; ++i) {
            if (derived[i] != null && derived[i].id == this.id) {
               derived[count++] = derived[i];
            }
         }

         if (count < derived.length) {
            System.arraycopy(derived, 0, derived = new TypeBinding[count], 0, count);
         }
      }

      return derived;
   }

   @Override
   public String toString() {
      if (this.wildcard == null) {
         return super.toString();
      } else {
         StringBuffer buffer = new StringBuffer(10);
         AnnotationBinding[] annotations = this.getTypeAnnotations();
         int i = 0;

         for(int length = annotations == null ? 0 : annotations.length; i < length; ++i) {
            buffer.append(annotations[i]);
            buffer.append(' ');
         }

         buffer.append(TypeConstants.WILDCARD_CAPTURE_NAME_PREFIX)
            .append(this.captureID)
            .append(TypeConstants.WILDCARD_CAPTURE_NAME_SUFFIX)
            .append(this.wildcard);
         return buffer.toString();
      }
   }
}
