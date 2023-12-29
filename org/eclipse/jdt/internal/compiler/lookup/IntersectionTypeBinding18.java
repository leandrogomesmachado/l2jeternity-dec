package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Set;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class IntersectionTypeBinding18 extends ReferenceBinding {
   public ReferenceBinding[] intersectingTypes;
   private ReferenceBinding javaLangObject;
   int length;

   public IntersectionTypeBinding18(ReferenceBinding[] intersectingTypes, LookupEnvironment environment) {
      this.intersectingTypes = intersectingTypes;
      this.length = intersectingTypes.length;
      if (!intersectingTypes[0].isClass()) {
         this.javaLangObject = environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
         this.modifiers |= 512;
      }
   }

   private IntersectionTypeBinding18(IntersectionTypeBinding18 prototype) {
      this.intersectingTypes = prototype.intersectingTypes;
      this.length = prototype.length;
      if (!this.intersectingTypes[0].isClass()) {
         this.javaLangObject = prototype.javaLangObject;
         this.modifiers |= 512;
      }
   }

   @Override
   public TypeBinding clone(TypeBinding enclosingType) {
      return new IntersectionTypeBinding18(this);
   }

   @Override
   protected MethodBinding[] getInterfaceAbstractContracts(Scope scope, boolean replaceWildcards) throws InvalidInputException {
      int typesLength = this.intersectingTypes.length;
      MethodBinding[][] methods = new MethodBinding[typesLength][];
      int contractsLength = 0;

      for(int i = 0; i < typesLength; ++i) {
         methods[i] = this.intersectingTypes[i].getInterfaceAbstractContracts(scope, replaceWildcards);
         contractsLength += methods[i].length;
      }

      MethodBinding[] contracts = new MethodBinding[contractsLength];
      int idx = 0;

      for(int i = 0; i < typesLength; ++i) {
         int len = methods[i].length;
         System.arraycopy(methods[i], 0, contracts, idx, len);
         idx += len;
      }

      return contracts;
   }

   @Override
   public boolean hasTypeBit(int bit) {
      for(int i = 0; i < this.length; ++i) {
         if (this.intersectingTypes[i].hasTypeBit(bit)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean canBeInstantiated() {
      return false;
   }

   @Override
   public boolean canBeSeenBy(PackageBinding invocationPackage) {
      for(int i = 0; i < this.length; ++i) {
         if (!this.intersectingTypes[i].canBeSeenBy(invocationPackage)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean canBeSeenBy(Scope scope) {
      for(int i = 0; i < this.length; ++i) {
         if (!this.intersectingTypes[i].canBeSeenBy(scope)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean canBeSeenBy(ReferenceBinding receiverType, ReferenceBinding invocationType) {
      for(int i = 0; i < this.length; ++i) {
         if (!this.intersectingTypes[i].canBeSeenBy(receiverType, invocationType)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public char[] constantPoolName() {
      return this.intersectingTypes[0].constantPoolName();
   }

   @Override
   public PackageBinding getPackage() {
      throw new UnsupportedOperationException();
   }

   @Override
   public ReferenceBinding[] getIntersectingTypes() {
      return this.intersectingTypes;
   }

   @Override
   public ReferenceBinding superclass() {
      return this.intersectingTypes[0].isClass() ? this.intersectingTypes[0] : this.javaLangObject;
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      if (this.intersectingTypes[0].isClass()) {
         ReferenceBinding[] superInterfaces = new ReferenceBinding[this.length - 1];
         System.arraycopy(this.intersectingTypes, 1, superInterfaces, 0, this.length - 1);
         return superInterfaces;
      } else {
         return this.intersectingTypes;
      }
   }

   @Override
   public boolean isBoxedPrimitiveType() {
      return this.intersectingTypes[0].isBoxedPrimitiveType();
   }

   @Override
   public boolean isCompatibleWith(TypeBinding right, Scope scope) {
      if (TypeBinding.equalsEquals(this, right)) {
         return true;
      } else {
         int rightKind = right.kind();
         TypeBinding[] rightIntersectingTypes = null;
         if (rightKind == 8196 && right.boundKind() == 1) {
            TypeBinding allRightBounds = ((WildcardBinding)right).allBounds();
            if (allRightBounds instanceof IntersectionTypeBinding18) {
               rightIntersectingTypes = ((IntersectionTypeBinding18)allRightBounds).intersectingTypes;
            }
         } else if (rightKind == 32772) {
            rightIntersectingTypes = ((IntersectionTypeBinding18)right).intersectingTypes;
         }

         if (rightIntersectingTypes != null) {
            int numRequired = rightIntersectingTypes.length;
            TypeBinding[] required = new TypeBinding[numRequired];
            System.arraycopy(rightIntersectingTypes, 0, required, 0, numRequired);

            for(int i = 0; i < this.length; ++i) {
               TypeBinding provided = this.intersectingTypes[i];

               for(int j = 0; j < required.length; ++j) {
                  if (required[j] != null && provided.isCompatibleWith(required[j], scope)) {
                     required[j] = null;
                     if (--numRequired == 0) {
                        return true;
                     }
                     break;
                  }
               }
            }

            return false;
         } else {
            for(int i = 0; i < this.length; ++i) {
               if (this.intersectingTypes[i].isCompatibleWith(right, scope)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Override
   public boolean isSubtypeOf(TypeBinding other) {
      if (TypeBinding.equalsEquals(this, other)) {
         return true;
      } else {
         for(int i = 0; i < this.intersectingTypes.length; ++i) {
            if (this.intersectingTypes[i].isSubtypeOf(other)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public char[] qualifiedSourceName() {
      StringBuffer qualifiedSourceName = new StringBuffer(16);

      for(int i = 0; i < this.length; ++i) {
         qualifiedSourceName.append(this.intersectingTypes[i].qualifiedSourceName());
         if (i != this.length - 1) {
            qualifiedSourceName.append(" & ");
         }
      }

      return qualifiedSourceName.toString().toCharArray();
   }

   @Override
   public char[] sourceName() {
      StringBuffer srcName = new StringBuffer(16);

      for(int i = 0; i < this.length; ++i) {
         srcName.append(this.intersectingTypes[i].sourceName());
         if (i != this.length - 1) {
            srcName.append(" & ");
         }
      }

      return srcName.toString().toCharArray();
   }

   @Override
   public char[] readableName() {
      StringBuffer readableName = new StringBuffer(16);

      for(int i = 0; i < this.length; ++i) {
         readableName.append(this.intersectingTypes[i].readableName());
         if (i != this.length - 1) {
            readableName.append(" & ");
         }
      }

      return readableName.toString().toCharArray();
   }

   @Override
   public char[] shortReadableName() {
      StringBuffer shortReadableName = new StringBuffer(16);

      for(int i = 0; i < this.length; ++i) {
         shortReadableName.append(this.intersectingTypes[i].shortReadableName());
         if (i != this.length - 1) {
            shortReadableName.append(" & ");
         }
      }

      return shortReadableName.toString().toCharArray();
   }

   @Override
   public boolean isIntersectionType18() {
      return true;
   }

   @Override
   public int kind() {
      return 32772;
   }

   @Override
   public String debugName() {
      StringBuffer debugName = new StringBuffer(16);

      for(int i = 0; i < this.length; ++i) {
         debugName.append(this.intersectingTypes[i].debugName());
         if (i != this.length - 1) {
            debugName.append(" & ");
         }
      }

      return debugName.toString();
   }

   @Override
   public String toString() {
      return this.debugName();
   }

   public TypeBinding getSAMType(Scope scope) {
      int i = 0;

      for(int max = this.intersectingTypes.length; i < max; ++i) {
         TypeBinding typeBinding = this.intersectingTypes[i];
         MethodBinding methodBinding = typeBinding.getSingleAbstractMethod(scope, true);
         if (methodBinding != null && methodBinding.problemId() != 17) {
            return typeBinding;
         }
      }

      return null;
   }

   @Override
   void collectInferenceVariables(Set<InferenceVariable> variables) {
      for(int i = 0; i < this.intersectingTypes.length; ++i) {
         this.intersectingTypes[i].collectInferenceVariables(variables);
      }
   }

   @Override
   public boolean mentionsAny(TypeBinding[] parameters, int idx) {
      if (super.mentionsAny(parameters, idx)) {
         return true;
      } else {
         for(int i = 0; i < this.intersectingTypes.length; ++i) {
            if (this.intersectingTypes[i].mentionsAny(parameters, -1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public long updateTagBits() {
      for(TypeBinding intersectingType : this.intersectingTypes) {
         this.tagBits |= intersectingType.updateTagBits();
      }

      return super.updateTagBits();
   }
}
