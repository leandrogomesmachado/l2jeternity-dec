package org.eclipse.jdt.internal.compiler.lookup;

import java.util.HashMap;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;

public class TypeSystem {
   private int typeid = 128;
   private TypeBinding[][] types;
   protected TypeSystem.HashedParameterizedTypes parameterizedTypes;
   private SimpleLookupTable annotationTypes;
   LookupEnvironment environment;

   public TypeSystem(LookupEnvironment environment) {
      this.environment = environment;
      this.annotationTypes = new SimpleLookupTable(16);
      this.typeid = 128;
      this.types = new TypeBinding[256][];
      this.parameterizedTypes = new TypeSystem.HashedParameterizedTypes();
   }

   public final TypeBinding getUnannotatedType(TypeBinding type) {
      UnresolvedReferenceBinding urb = null;
      if (type.isUnresolvedType()) {
         urb = (UnresolvedReferenceBinding)type;
         ReferenceBinding resolvedType = urb.resolvedType;
         if (resolvedType != null) {
            if (CharOperation.indexOf('$', type.sourceName()) > 0) {
               type = this.environment.convertToRawType(resolvedType, false);
            } else {
               type = resolvedType;
            }
         } else if (CharOperation.indexOf('$', type.sourceName()) > 0) {
            boolean mayTolerateMissingType = this.environment.mayTolerateMissingType;
            this.environment.mayTolerateMissingType = true;

            try {
               type = BinaryTypeBinding.resolveType(type, this.environment, true);
            } finally {
               this.environment.mayTolerateMissingType = mayTolerateMissingType;
            }
         }
      }

      TypeBinding var5;
      try {
         if (type.id == Integer.MAX_VALUE) {
            if (type.hasTypeAnnotations()) {
               throw new IllegalStateException();
            }

            int typesLength = this.types.length;
            if (this.typeid == typesLength) {
               System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
            }

            this.types[type.id = this.typeid++] = new TypeBinding[4];
            return this.types[type.id][0] = type;
         }

         TypeBinding nakedType = this.types[type.id] == null ? null : this.types[type.id][0];
         if (type.hasTypeAnnotations() && nakedType == null) {
            throw new IllegalStateException();
         }

         if (nakedType == null) {
            this.types[type.id] = new TypeBinding[4];
            return this.types[type.id][0] = type;
         }

         var5 = nakedType;
      } finally {
         if (urb != null && urb.id == Integer.MAX_VALUE) {
            urb.id = type.id;
         }
      }

      return var5;
   }

   public void forceRegisterAsDerived(TypeBinding derived) {
      int id = derived.id;
      if (id != Integer.MAX_VALUE && this.types[id] != null) {
         TypeBinding unannotated = this.types[id][0];
         if (unannotated == derived) {
            this.types[id][0] = unannotated = derived.clone(null);
         }

         this.cacheDerivedType(unannotated, derived);
      } else {
         throw new IllegalStateException("Type was not yet registered as expected: " + derived);
      }
   }

   public TypeBinding[] getAnnotatedTypes(TypeBinding type) {
      return Binding.NO_TYPES;
   }

   public ArrayBinding getArrayType(TypeBinding leafType, int dimensions) {
      if (leafType instanceof ArrayBinding) {
         dimensions += leafType.dimensions();
         leafType = leafType.leafComponentType();
      }

      TypeBinding unannotatedLeafType = this.getUnannotatedType(leafType);

      for(TypeBinding derivedType : this.types[unannotatedLeafType.id]) {
         if (derivedType == null) {
            break;
         }

         if (derivedType.isArrayType()
            && !derivedType.hasTypeAnnotations()
            && derivedType.leafComponentType() == unannotatedLeafType
            && derivedType.dimensions() == dimensions) {
            return (ArrayBinding)derivedType;
         }
      }

      byte i;
      byte length;
      TypeBinding[] derivedTypes;
      if (i == length) {
         Object derivedTypesx;
         System.arraycopy(derivedTypesx, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
         this.types[unannotatedLeafType.id] = derivedTypes;
      }

      TypeBinding arrayType = derivedTypes[i] = new ArrayBinding(unannotatedLeafType, dimensions, this.environment);
      int typesLength = this.types.length;
      if (this.typeid == typesLength) {
         System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
      }

      this.types[this.typeid] = new TypeBinding[1];
      return (ArrayBinding)(this.types[arrayType.id = this.typeid++][0] = arrayType);
   }

   public ArrayBinding getArrayType(TypeBinding leafComponentType, int dimensions, AnnotationBinding[] annotations) {
      return this.getArrayType(leafComponentType, dimensions);
   }

   public ReferenceBinding getMemberType(ReferenceBinding memberType, ReferenceBinding enclosingType) {
      return memberType;
   }

   public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType) {
      ReferenceBinding unannotatedGenericType = (ReferenceBinding)this.getUnannotatedType(genericType);
      int typeArgumentsLength = typeArguments == null ? 0 : typeArguments.length;
      TypeBinding[] unannotatedTypeArguments = typeArguments == null ? null : new TypeBinding[typeArgumentsLength];

      for(int i = 0; i < typeArgumentsLength; ++i) {
         unannotatedTypeArguments[i] = this.getUnannotatedType(typeArguments[i]);
      }

      ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding)this.getUnannotatedType(enclosingType);
      ParameterizedTypeBinding parameterizedType = this.parameterizedTypes
         .get(unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, Binding.NO_ANNOTATIONS);
      if (parameterizedType != null) {
         return parameterizedType;
      } else {
         parameterizedType = new ParameterizedTypeBinding(unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, this.environment);
         this.cacheDerivedType(unannotatedGenericType, parameterizedType);
         this.parameterizedTypes.put(genericType, typeArguments, enclosingType, parameterizedType);
         int typesLength = this.types.length;
         if (this.typeid == typesLength) {
            System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
         }

         this.types[this.typeid] = new TypeBinding[1];
         return (ParameterizedTypeBinding)(this.types[parameterizedType.id = this.typeid++][0] = parameterizedType);
      }
   }

   public ParameterizedTypeBinding getParameterizedType(
      ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding[] annotations
   ) {
      return this.getParameterizedType(genericType, typeArguments, enclosingType);
   }

   public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType) {
      ReferenceBinding unannotatedGenericType = (ReferenceBinding)this.getUnannotatedType(genericType);
      ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding)this.getUnannotatedType(enclosingType);

      for(TypeBinding derivedType : this.types[unannotatedGenericType.id]) {
         if (derivedType == null) {
            break;
         }

         if (derivedType.isRawType()
            && derivedType.actualType() == unannotatedGenericType
            && !derivedType.hasTypeAnnotations()
            && derivedType.enclosingType() == unannotatedEnclosingType) {
            return (RawTypeBinding)derivedType;
         }
      }

      byte i;
      byte length;
      TypeBinding[] derivedTypes;
      if (i == length) {
         Object derivedTypesx;
         System.arraycopy(derivedTypesx, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
         this.types[unannotatedGenericType.id] = derivedTypes;
      }

      TypeBinding rawTytpe = derivedTypes[i] = new RawTypeBinding(unannotatedGenericType, unannotatedEnclosingType, this.environment);
      int typesLength = this.types.length;
      if (this.typeid == typesLength) {
         System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
      }

      this.types[this.typeid] = new TypeBinding[1];
      return (RawTypeBinding)(this.types[rawTytpe.id = this.typeid++][0] = rawTytpe);
   }

   public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
      return this.getRawType(genericType, enclosingType);
   }

   public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind) {
      if (genericType == null) {
         genericType = ReferenceBinding.LUB_GENERIC;
      }

      ReferenceBinding unannotatedGenericType = (ReferenceBinding)this.getUnannotatedType(genericType);
      int otherBoundsLength = otherBounds == null ? 0 : otherBounds.length;
      TypeBinding[] unannotatedOtherBounds = otherBounds == null ? null : new TypeBinding[otherBoundsLength];

      for(int i = 0; i < otherBoundsLength; ++i) {
         unannotatedOtherBounds[i] = this.getUnannotatedType(otherBounds[i]);
      }

      TypeBinding unannotatedBound = bound == null ? null : this.getUnannotatedType(bound);

      for(TypeBinding derivedType : this.types[unannotatedGenericType.id]) {
         if (derivedType == null) {
            break;
         }

         if (derivedType.isWildcard()
            && derivedType.actualType() == unannotatedGenericType
            && !derivedType.hasTypeAnnotations()
            && derivedType.rank() == rank
            && derivedType.boundKind() == boundKind
            && derivedType.bound() == unannotatedBound
            && Util.effectivelyEqual(derivedType.additionalBounds(), unannotatedOtherBounds)) {
            return (WildcardBinding)derivedType;
         }
      }

      byte i;
      byte length;
      TypeBinding[] derivedTypes;
      if (i == length) {
         Object derivedTypesx;
         System.arraycopy(derivedTypesx, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
         this.types[unannotatedGenericType.id] = derivedTypes;
      }

      TypeBinding wildcard = derivedTypes[i] = new WildcardBinding(
         unannotatedGenericType, rank, unannotatedBound, unannotatedOtherBounds, boundKind, this.environment
      );
      int typesLength = this.types.length;
      if (this.typeid == typesLength) {
         System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
      }

      this.types[this.typeid] = new TypeBinding[1];
      return (WildcardBinding)(this.types[wildcard.id = this.typeid++][0] = wildcard);
   }

   public final CaptureBinding getCapturedWildcard(WildcardBinding wildcard, ReferenceBinding contextType, int start, int end, ASTNode cud, int id) {
      WildcardBinding unannotatedWildcard = (WildcardBinding)this.getUnannotatedType(wildcard);
      TypeBinding[] derivedTypes = this.types[unannotatedWildcard.id];
      int length = derivedTypes.length;
      int nullSlot = length;

      int i;
      for(i = length - 1; i >= -1; --i) {
         if (i == -1) {
            i = nullSlot;
            break;
         }

         TypeBinding derivedType = derivedTypes[i];
         if (derivedType == null) {
            nullSlot = i;
         } else if (derivedType.isCapture()) {
            CaptureBinding prior = (CaptureBinding)derivedType;
            if (prior.cud != cud) {
               i = nullSlot;
               break;
            }

            if (prior.sourceType == contextType && prior.start == start && prior.end == end) {
               return prior;
            }
         }
      }

      if (i == length) {
         System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
         this.types[unannotatedWildcard.id] = derivedTypes;
      }

      return (CaptureBinding)(derivedTypes[i] = new CaptureBinding(wildcard, contextType, start, end, cud, id));
   }

   public WildcardBinding getWildcard(
      ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, AnnotationBinding[] annotations
   ) {
      return this.getWildcard(genericType, rank, bound, otherBounds, boundKind);
   }

   public TypeBinding getAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations) {
      return type;
   }

   protected final TypeBinding[] getDerivedTypes(TypeBinding keyType) {
      keyType = this.getUnannotatedType(keyType);
      return this.types[keyType.id];
   }

   private TypeBinding cacheDerivedType(TypeBinding keyType, TypeBinding derivedType) {
      if (keyType != null && derivedType != null && keyType.id != Integer.MAX_VALUE) {
         TypeBinding[] derivedTypes = this.types[keyType.id];
         int length = derivedTypes.length;
         int first = 0;
         int last = length;
         int i = (first + length) / 2;

         do {
            if (derivedTypes[i] == null) {
               if (i == first || i > 0 && derivedTypes[i - 1] != null) {
                  break;
               }

               last = i - 1;
            } else {
               first = i + 1;
            }

            i = (first + last) / 2;
         } while(i < length && first <= last);

         if (i == length) {
            System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
            this.types[keyType.id] = derivedTypes;
         }

         return derivedTypes[i] = derivedType;
      } else {
         throw new IllegalStateException();
      }
   }

   protected final TypeBinding cacheDerivedType(TypeBinding keyType, TypeBinding nakedType, TypeBinding derivedType) {
      this.cacheDerivedType(keyType, derivedType);
      if (nakedType.id != keyType.id) {
         this.cacheDerivedType(nakedType, derivedType);
      }

      return derivedType;
   }

   public final AnnotationBinding getAnnotationType(ReferenceBinding annotationType, boolean requiredResolved) {
      AnnotationBinding annotation = (AnnotationBinding)this.annotationTypes.get(annotationType);
      if (annotation == null) {
         if (requiredResolved) {
            annotation = new AnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS);
         } else {
            annotation = new UnresolvedAnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS, this.environment);
         }

         this.annotationTypes.put(annotationType, annotation);
      }

      if (requiredResolved) {
         annotation.resolve();
      }

      return annotation;
   }

   public boolean isAnnotatedTypeSystem() {
      return false;
   }

   public void reset() {
      this.annotationTypes = new SimpleLookupTable(16);
      this.typeid = 128;
      this.types = new TypeBinding[256][];
      this.parameterizedTypes = new TypeSystem.HashedParameterizedTypes();
   }

   public void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
      int unresolvedTypeId = unresolvedType.id;
      if (unresolvedTypeId != Integer.MAX_VALUE) {
         TypeBinding[] derivedTypes = this.types[unresolvedTypeId];
         int i = 0;

         for(int length = derivedTypes == null ? 0 : derivedTypes.length; i < length && derivedTypes[i] != null; ++i) {
            if (derivedTypes[i] == unresolvedType) {
               resolvedType.id = unresolvedTypeId;
               derivedTypes[i] = resolvedType;
            }
         }
      }

      if (this.annotationTypes.get(unresolvedType) != null) {
         Object[] keys = this.annotationTypes.keyTable;
         int i = 0;

         for(int l = keys.length; i < l; ++i) {
            if (keys[i] == unresolvedType) {
               keys[i] = resolvedType;
               break;
            }
         }
      }
   }

   public final TypeBinding getIntersectionType18(ReferenceBinding[] intersectingTypes) {
      int intersectingTypesLength = intersectingTypes == null ? 0 : intersectingTypes.length;
      if (intersectingTypesLength == 0) {
         return null;
      } else {
         TypeBinding keyType = intersectingTypes[0];
         if (keyType != null && intersectingTypesLength != 1) {
            label46:
            for(TypeBinding derivedType : this.getDerivedTypes(keyType)) {
               if (derivedType == null) {
                  break;
               }

               if (derivedType.isIntersectionType18()) {
                  ReferenceBinding[] priorIntersectingTypes = derivedType.getIntersectingTypes();
                  if (priorIntersectingTypes.length == intersectingTypesLength) {
                     for(int j = 0; j < intersectingTypesLength; ++j) {
                        if (intersectingTypes[j] != priorIntersectingTypes[j]) {
                           continue label46;
                        }
                     }

                     return derivedType;
                  }
               }
            }

            return this.cacheDerivedType(keyType, new IntersectionTypeBinding18(intersectingTypes, this.environment));
         } else {
            return keyType;
         }
      }
   }

   public void fixTypeVariableDeclaringElement(TypeVariableBinding var, Binding declaringElement) {
      int id = var.id;
      if (id < this.typeid && this.types[id] != null) {
         TypeBinding[] var7;
         for(TypeBinding t : var7 = this.types[id]) {
            if (t instanceof TypeVariableBinding) {
               ((TypeVariableBinding)t).declaringElement = declaringElement;
            }
         }
      } else {
         var.declaringElement = declaringElement;
      }
   }

   public final class HashedParameterizedTypes {
      HashMap<ParameterizedTypeBinding, ParameterizedTypeBinding[]> hashedParameterizedTypes = new HashMap<>(256);

      ParameterizedTypeBinding get(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
         ReferenceBinding unannotatedGenericType = (ReferenceBinding)TypeSystem.this.getUnannotatedType(genericType);
         int typeArgumentsLength = typeArguments == null ? 0 : typeArguments.length;
         TypeBinding[] unannotatedTypeArguments = typeArguments == null ? null : new TypeBinding[typeArgumentsLength];

         for(int i = 0; i < typeArgumentsLength; ++i) {
            unannotatedTypeArguments[i] = TypeSystem.this.getUnannotatedType(typeArguments[i]);
         }

         ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding)TypeSystem.this.getUnannotatedType(enclosingType);
         ParameterizedTypeBinding typeParameterization = new TypeSystem.HashedParameterizedTypes.InternalParameterizedTypeBinding(
            unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, TypeSystem.this.environment
         );
         ReferenceBinding genericTypeToMatch = unannotatedGenericType;
         ReferenceBinding enclosingTypeToMatch = unannotatedEnclosingType;
         TypeBinding[] typeArgumentsToMatch = unannotatedTypeArguments;
         if (TypeSystem.this instanceof AnnotatableTypeSystem) {
            genericTypeToMatch = genericType;
            enclosingTypeToMatch = enclosingType;
            typeArgumentsToMatch = typeArguments;
         }

         ParameterizedTypeBinding[] parameterizedTypeBindings = (ParameterizedTypeBinding[])this.hashedParameterizedTypes.get(typeParameterization);
         int i = 0;

         for(int length = parameterizedTypeBindings == null ? 0 : parameterizedTypeBindings.length; i < length; ++i) {
            ParameterizedTypeBinding parameterizedType = parameterizedTypeBindings[i];
            if (parameterizedType.actualType() == genericTypeToMatch
               && parameterizedType.enclosingType() == enclosingTypeToMatch
               && Util.effectivelyEqual(parameterizedType.typeArguments(), typeArgumentsToMatch)
               && Util.effectivelyEqual(annotations, parameterizedType.getTypeAnnotations())) {
               return parameterizedType;
            }
         }

         return null;
      }

      void put(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, ParameterizedTypeBinding parameterizedType) {
         ReferenceBinding unannotatedGenericType = (ReferenceBinding)TypeSystem.this.getUnannotatedType(genericType);
         int typeArgumentsLength = typeArguments == null ? 0 : typeArguments.length;
         TypeBinding[] unannotatedTypeArguments = typeArguments == null ? null : new TypeBinding[typeArgumentsLength];

         for(int i = 0; i < typeArgumentsLength; ++i) {
            unannotatedTypeArguments[i] = TypeSystem.this.getUnannotatedType(typeArguments[i]);
         }

         ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding)TypeSystem.this.getUnannotatedType(enclosingType);
         ParameterizedTypeBinding typeParameterization = new TypeSystem.HashedParameterizedTypes.InternalParameterizedTypeBinding(
            unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, TypeSystem.this.environment
         );
         ParameterizedTypeBinding[] parameterizedTypeBindings = (ParameterizedTypeBinding[])this.hashedParameterizedTypes.get(typeParameterization);
         int slot;
         if (parameterizedTypeBindings == null) {
            slot = 0;
            parameterizedTypeBindings = new ParameterizedTypeBinding[1];
         } else {
            slot = parameterizedTypeBindings.length;
            System.arraycopy(parameterizedTypeBindings, 0, parameterizedTypeBindings = new ParameterizedTypeBinding[slot + 1], 0, slot);
         }

         parameterizedTypeBindings[slot] = parameterizedType;
         this.hashedParameterizedTypes.put(typeParameterization, parameterizedTypeBindings);
      }

      private final class InternalParameterizedTypeBinding extends ParameterizedTypeBinding {
         public InternalParameterizedTypeBinding(
            ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, LookupEnvironment environment
         ) {
            super(genericType, typeArguments, enclosingType, environment);
         }

         @Override
         public boolean equals(Object other) {
            ParameterizedTypeBinding that = (ParameterizedTypeBinding)other;
            return this.type == that.type && this.enclosingType == that.enclosingType && Util.effectivelyEqual(this.arguments, that.arguments);
         }

         @Override
         public int hashCode() {
            int hashCode = this.type.hashCode() + 13 * (this.enclosingType != null ? this.enclosingType.hashCode() : 0);
            int i = 0;

            for(int length = this.arguments == null ? 0 : this.arguments.length; i < length; ++i) {
               hashCode += (i + 1) * this.arguments[i].id * this.arguments[i].hashCode();
            }

            return hashCode;
         }
      }
   }
}
