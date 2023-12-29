package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public final class ArrayBinding extends TypeBinding {
   public static final FieldBinding ArrayLength = new FieldBinding(TypeConstants.LENGTH, TypeBinding.INT, 17, null, Constant.NotAConstant);
   public TypeBinding leafComponentType;
   public int dimensions;
   LookupEnvironment environment;
   char[] constantPoolName;
   char[] genericTypeSignature;
   public long[] nullTagBitsPerDimension;

   public ArrayBinding(TypeBinding type, int dimensions, LookupEnvironment environment) {
      this.tagBits |= 1L;
      this.leafComponentType = type;
      this.dimensions = dimensions;
      this.environment = environment;
      if (type instanceof UnresolvedReferenceBinding) {
         ((UnresolvedReferenceBinding)type).addWrapper(this, environment);
      } else {
         this.tagBits |= type.tagBits & 2305843010824308864L;
      }

      long mask = type.tagBits & 108086391056891904L;
      if (mask != 0L) {
         this.nullTagBitsPerDimension = new long[this.dimensions + 1];
         this.nullTagBitsPerDimension[this.dimensions] = mask;
         this.tagBits |= 1048576L;
      }
   }

   @Override
   public TypeBinding closestMatch() {
      if (this.isValidBinding()) {
         return this;
      } else {
         TypeBinding leafClosestMatch = this.leafComponentType.closestMatch();
         return leafClosestMatch == null ? null : this.environment.createArrayType(this.leafComponentType.closestMatch(), this.dimensions);
      }
   }

   @Override
   public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
      if ((this.tagBits & 128L) != 0L) {
         missingTypes = this.leafComponentType.collectMissingTypes(missingTypes);
      }

      return missingTypes;
   }

   @Override
   public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
      if ((this.tagBits & 536870912L) != 0L) {
         if (actualType != TypeBinding.NULL && actualType.kind() != 65540) {
            switch(actualType.kind()) {
               case 68:
                  int actualDim = actualType.dimensions();
                  if (actualDim == this.dimensions) {
                     this.leafComponentType.collectSubstitutes(scope, actualType.leafComponentType(), inferenceContext, constraint);
                  } else if (actualDim > this.dimensions) {
                     ArrayBinding actualReducedType = this.environment.createArrayType(actualType.leafComponentType(), actualDim - this.dimensions);
                     this.leafComponentType.collectSubstitutes(scope, actualReducedType, inferenceContext, constraint);
                  }
               case 4100:
            }
         }
      }
   }

   @Override
   public boolean mentionsAny(TypeBinding[] parameters, int idx) {
      return this.leafComponentType.mentionsAny(parameters, idx);
   }

   @Override
   void collectInferenceVariables(Set<InferenceVariable> variables) {
      this.leafComponentType.collectInferenceVariables(variables);
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      TypeBinding substitutedLeaf = this.leafComponentType.substituteInferenceVariable(var, substituteType);
      return TypeBinding.notEquals(substitutedLeaf, this.leafComponentType)
         ? this.environment.createArrayType(substitutedLeaf, this.dimensions, this.typeAnnotations)
         : this;
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      char[] brackets = new char[this.dimensions];

      for(int i = this.dimensions - 1; i >= 0; --i) {
         brackets[i] = '[';
      }

      return CharOperation.concat(brackets, this.leafComponentType.computeUniqueKey(isLeaf));
   }

   @Override
   public char[] constantPoolName() {
      if (this.constantPoolName != null) {
         return this.constantPoolName;
      } else {
         char[] brackets = new char[this.dimensions];

         for(int i = this.dimensions - 1; i >= 0; --i) {
            brackets[i] = '[';
         }

         return this.constantPoolName = CharOperation.concat(brackets, this.leafComponentType.signature());
      }
   }

   @Override
   public String debugName() {
      if (this.hasTypeAnnotations()) {
         return this.annotatedDebugName();
      } else {
         StringBuffer brackets = new StringBuffer(this.dimensions * 2);
         int i = this.dimensions;

         while(--i >= 0) {
            brackets.append("[]");
         }

         return this.leafComponentType.debugName() + brackets.toString();
      }
   }

   @Override
   public String annotatedDebugName() {
      StringBuffer brackets = new StringBuffer(this.dimensions * 2);
      brackets.append(this.leafComponentType.annotatedDebugName());
      brackets.append(' ');
      AnnotationBinding[] annotations = this.getTypeAnnotations();
      int i = 0;

      for(int j = -1; i < this.dimensions; ++i) {
         if (annotations != null) {
            if (i != 0) {
               brackets.append(' ');
            }

            while(true) {
               ++j;
               if (j >= annotations.length || annotations[j] == null) {
                  break;
               }

               brackets.append(annotations[j]);
               brackets.append(' ');
            }
         }

         brackets.append("[]");
      }

      return brackets.toString();
   }

   @Override
   public int dimensions() {
      return this.dimensions;
   }

   public TypeBinding elementsType() {
      if (this.dimensions == 1) {
         return this.leafComponentType;
      } else {
         AnnotationBinding[] oldies = this.getTypeAnnotations();
         AnnotationBinding[] newbies = Binding.NO_ANNOTATIONS;
         int i = 0;

         for(int length = oldies == null ? 0 : oldies.length; i < length; ++i) {
            if (oldies[i] == null) {
               System.arraycopy(oldies, i + 1, newbies = new AnnotationBinding[length - i - 1], 0, length - i - 1);
               break;
            }
         }

         return this.environment.createArrayType(this.leafComponentType, this.dimensions - 1, newbies);
      }
   }

   @Override
   public TypeBinding erasure() {
      TypeBinding erasedType = this.leafComponentType.erasure();
      return TypeBinding.notEquals(this.leafComponentType, erasedType) ? this.environment.createArrayType(erasedType, this.dimensions) : this;
   }

   public LookupEnvironment environment() {
      return this.environment;
   }

   @Override
   public char[] genericTypeSignature() {
      if (this.genericTypeSignature == null) {
         char[] brackets = new char[this.dimensions];

         for(int i = this.dimensions - 1; i >= 0; --i) {
            brackets[i] = '[';
         }

         this.genericTypeSignature = CharOperation.concat(brackets, this.leafComponentType.genericTypeSignature());
      }

      return this.genericTypeSignature;
   }

   @Override
   public PackageBinding getPackage() {
      return this.leafComponentType.getPackage();
   }

   @Override
   public int hashCode() {
      return this.leafComponentType == null ? super.hashCode() : this.leafComponentType.hashCode();
   }

   @Override
   public boolean isCompatibleWith(TypeBinding otherType, Scope captureScope) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else {
         switch(otherType.kind()) {
            case 68:
               ArrayBinding otherArray = (ArrayBinding)otherType;
               if (otherArray.leafComponentType.isBaseType()) {
                  return false;
               } else if (this.dimensions == otherArray.dimensions) {
                  return this.leafComponentType.isCompatibleWith(otherArray.leafComponentType);
               } else if (this.dimensions < otherArray.dimensions) {
                  return false;
               }
            default:
               switch(otherType.leafComponentType().id) {
                  case 1:
                  case 36:
                  case 37:
                     return true;
                  default:
                     return false;
               }
            case 132:
               return false;
            case 516:
            case 8196:
               return ((WildcardBinding)otherType).boundCheck(this);
            case 4100:
               if (otherType.isCapture()) {
                  CaptureBinding otherCapture = (CaptureBinding)otherType;
                  TypeBinding otherLowerBound = otherCapture.lowerBound;
                  if (otherCapture.lowerBound != null) {
                     if (!otherLowerBound.isArrayType()) {
                        return false;
                     }

                     return this.isCompatibleWith(otherLowerBound, captureScope);
                  }
               }

               return false;
         }
      }
   }

   @Override
   public boolean isSubtypeOf(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else {
         switch(otherType.kind()) {
            case 68:
               ArrayBinding otherArray = (ArrayBinding)otherType;
               if (otherArray.leafComponentType.isBaseType()) {
                  return false;
               } else if (this.dimensions == otherArray.dimensions) {
                  return this.leafComponentType.isSubtypeOf(otherArray.leafComponentType);
               } else if (this.dimensions < otherArray.dimensions) {
                  return false;
               }
            default:
               switch(otherType.leafComponentType().id) {
                  case 1:
                  case 36:
                  case 37:
                     return true;
                  default:
                     return false;
               }
            case 132:
               return false;
         }
      }
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      return this.leafComponentType.isProperType(admitCapture18);
   }

   @Override
   public int kind() {
      return 68;
   }

   @Override
   public TypeBinding leafComponentType() {
      return this.leafComponentType;
   }

   @Override
   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      if (this.nullTagBitsPerDimension == null) {
         return shortNames ? this.shortReadableName() : this.readableName();
      } else {
         char[][] brackets = new char[this.dimensions][];

         for(int i = 0; i < this.dimensions; ++i) {
            if ((this.nullTagBitsPerDimension[i] & 108086391056891904L) != 0L) {
               char[][] fqAnnotationName;
               if ((this.nullTagBitsPerDimension[i] & 72057594037927936L) != 0L) {
                  fqAnnotationName = options.nonNullAnnotationName;
               } else {
                  fqAnnotationName = options.nullableAnnotationName;
               }

               char[] annotationName = shortNames ? fqAnnotationName[fqAnnotationName.length - 1] : CharOperation.concatWith(fqAnnotationName, '.');
               brackets[i] = new char[annotationName.length + 3];
               brackets[i][0] = '@';
               System.arraycopy(annotationName, 0, brackets[i], 1, annotationName.length);
               brackets[i][annotationName.length + 1] = '[';
               brackets[i][annotationName.length + 2] = ']';
            } else {
               brackets[i] = new char[]{'[', ']'};
            }
         }

         return CharOperation.concat(this.leafComponentType.nullAnnotatedReadableName(options, shortNames), CharOperation.concatWith(brackets, ' '), ' ');
      }
   }

   @Override
   public int problemId() {
      return this.leafComponentType.problemId();
   }

   @Override
   public char[] qualifiedSourceName() {
      char[] brackets = new char[this.dimensions * 2];

      for(int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
         brackets[i] = ']';
         brackets[i - 1] = '[';
      }

      return CharOperation.concat(this.leafComponentType.qualifiedSourceName(), brackets);
   }

   @Override
   public char[] readableName() {
      char[] brackets = new char[this.dimensions * 2];

      for(int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
         brackets[i] = ']';
         brackets[i - 1] = '[';
      }

      return CharOperation.concat(this.leafComponentType.readableName(), brackets);
   }

   @Override
   public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
      this.tagBits |= 2097152L;
      if (annotations != null && annotations.length != 0) {
         this.typeAnnotations = annotations;
         if (evalNullAnnotations) {
            long nullTagBits = 0L;
            if (this.nullTagBitsPerDimension == null) {
               this.nullTagBitsPerDimension = new long[this.dimensions + 1];
            }

            int dimension = 0;
            int i = 0;

            for(int length = annotations.length; i < length; ++i) {
               AnnotationBinding annotation = annotations[i];
               if (annotation != null) {
                  if (annotation.type.hasNullBit(64)) {
                     nullTagBits |= 36028797018963968L;
                     this.tagBits |= 1048576L;
                  } else if (annotation.type.hasNullBit(32)) {
                     nullTagBits |= 72057594037927936L;
                     this.tagBits |= 1048576L;
                  }
               } else {
                  if (nullTagBits != 0L) {
                     this.nullTagBitsPerDimension[dimension] = nullTagBits;
                     nullTagBits = 0L;
                  }

                  ++dimension;
               }
            }

            this.tagBits |= this.nullTagBitsPerDimension[0];
         }
      }
   }

   @Override
   public char[] shortReadableName() {
      char[] brackets = new char[this.dimensions * 2];

      for(int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
         brackets[i] = ']';
         brackets[i - 1] = '[';
      }

      return CharOperation.concat(this.leafComponentType.shortReadableName(), brackets);
   }

   @Override
   public char[] sourceName() {
      char[] brackets = new char[this.dimensions * 2];

      for(int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
         brackets[i] = ']';
         brackets[i - 1] = '[';
      }

      return CharOperation.concat(this.leafComponentType.sourceName(), brackets);
   }

   @Override
   public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
      if (this.leafComponentType == unresolvedType) {
         this.leafComponentType = env.convertUnresolvedBinaryToRawType(resolvedType);
         if (this.leafComponentType != resolvedType) {
            this.id = env.createArrayType(this.leafComponentType, this.dimensions, this.typeAnnotations).id;
         }

         this.tagBits |= this.leafComponentType.tagBits & 2305843010824306816L;
      }
   }

   @Override
   public String toString() {
      return this.leafComponentType != null ? this.debugName() : "NULL TYPE ARRAY";
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
         AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.typeAnnotations);
         return this.environment.createArrayType(this.leafComponentType, this.dimensions, newAnnotations);
      }
   }

   @Override
   public TypeBinding uncapture(Scope scope) {
      if ((this.tagBits & 2305843009213693952L) == 0L) {
         return this;
      } else {
         TypeBinding leafType = this.leafComponentType.uncapture(scope);
         return scope.environment().createArrayType(leafType, this.dimensions, this.typeAnnotations);
      }
   }

   @Override
   public boolean acceptsNonNullDefault() {
      return true;
   }

   @Override
   public long updateTagBits() {
      if (this.leafComponentType != null) {
         this.tagBits |= this.leafComponentType.updateTagBits();
      }

      return super.updateTagBits();
   }
}
