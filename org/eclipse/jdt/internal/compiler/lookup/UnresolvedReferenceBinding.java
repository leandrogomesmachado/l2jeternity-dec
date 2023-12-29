package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class UnresolvedReferenceBinding extends ReferenceBinding {
   ReferenceBinding resolvedType;
   TypeBinding[] wrappers;
   UnresolvedReferenceBinding prototype;

   UnresolvedReferenceBinding(char[][] compoundName, PackageBinding packageBinding) {
      this.compoundName = compoundName;
      this.sourceName = compoundName[compoundName.length - 1];
      this.fPackage = packageBinding;
      this.wrappers = null;
      this.prototype = this;
      this.computeId();
   }

   public UnresolvedReferenceBinding(UnresolvedReferenceBinding prototype) {
      super(prototype);
      this.resolvedType = prototype.resolvedType;
      this.wrappers = null;
      this.prototype = prototype.prototype;
   }

   @Override
   public TypeBinding clone(TypeBinding outerType) {
      if (this.resolvedType != null) {
         return this.resolvedType.clone(outerType);
      } else {
         UnresolvedReferenceBinding copy = new UnresolvedReferenceBinding(this);
         this.addWrapper(copy, null);
         return copy;
      }
   }

   void addWrapper(TypeBinding wrapper, LookupEnvironment environment) {
      if (this.resolvedType != null) {
         wrapper.swapUnresolved(this, this.resolvedType, environment);
      } else {
         if (this.wrappers == null) {
            this.wrappers = new TypeBinding[]{wrapper};
         } else {
            int length = this.wrappers.length;
            System.arraycopy(this.wrappers, 0, this.wrappers = new TypeBinding[length + 1], 0, length);
            this.wrappers[length] = wrapper;
         }
      }
   }

   @Override
   public boolean isUnresolvedType() {
      return true;
   }

   @Override
   public String debugName() {
      return this.toString();
   }

   @Override
   public int depth() {
      int last = this.compoundName.length - 1;
      return CharOperation.occurencesOf((char)36, this.compoundName[last]);
   }

   @Override
   public boolean hasTypeBit(int bit) {
      return false;
   }

   @Override
   public TypeBinding prototype() {
      return this.prototype;
   }

   ReferenceBinding resolve(LookupEnvironment environment, boolean convertGenericToRawType) {
      if (this != this.prototype) {
         ReferenceBinding targetType = this.prototype.resolve(environment, convertGenericToRawType);
         if (convertGenericToRawType && targetType != null && targetType.isRawType()) {
            targetType = (ReferenceBinding)environment.createAnnotatedType(targetType, this.typeAnnotations);
         } else {
            targetType = this.resolvedType;
         }

         return targetType;
      } else {
         ReferenceBinding targetType = this.resolvedType;
         if (targetType == null) {
            char[] typeName = this.compoundName[this.compoundName.length - 1];
            targetType = this.fPackage.getType0(typeName);
            if (targetType == this) {
               targetType = environment.askForType(this.compoundName);
            }

            if ((targetType == null || targetType == this) && CharOperation.contains('.', typeName)) {
               targetType = environment.askForType(this.fPackage, CharOperation.replaceOnCopy(typeName, '.', '$'));
            }

            if (targetType == null || targetType == this) {
               if ((this.tagBits & 128L) == 0L && !environment.mayTolerateMissingType) {
                  environment.problemReporter.isClassPathCorrect(this.compoundName, environment.unitBeingCompleted, environment.missingClassFileLocation);
               }

               targetType = environment.createMissingType(null, this.compoundName);
            }

            if (targetType.id != Integer.MAX_VALUE) {
               this.id = targetType.id;
            }

            this.setResolvedType(targetType, environment);
         }

         if (convertGenericToRawType) {
            targetType = (ReferenceBinding)environment.convertUnresolvedBinaryToRawType(targetType);
         }

         return targetType;
      }
   }

   void setResolvedType(ReferenceBinding targetType, LookupEnvironment environment) {
      if (this.resolvedType != targetType) {
         this.resolvedType = targetType;
         environment.updateCaches(this, targetType);
         if (this.wrappers != null) {
            int i = 0;

            for(int l = this.wrappers.length; i < l; ++i) {
               this.wrappers[i].swapUnresolved(this, targetType, environment);
            }
         }
      }
   }

   @Override
   public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding unannotatedType, LookupEnvironment environment) {
      if (this.resolvedType == null) {
         ReferenceBinding annotatedType = (ReferenceBinding)unannotatedType.clone(null);
         this.resolvedType = annotatedType;
         annotatedType.setTypeAnnotations(this.getTypeAnnotations(), environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
         annotatedType.id = unannotatedType.id = this.id;
         environment.updateCaches(this, annotatedType);
         if (this.wrappers != null) {
            int i = 0;

            for(int l = this.wrappers.length; i < l; ++i) {
               this.wrappers[i].swapUnresolved(this, annotatedType, environment);
            }
         }
      }
   }

   @Override
   public String toString() {
      return this.hasTypeAnnotations()
         ? super.annotatedDebugName() + "(unresolved)"
         : "Unresolved type " + (this.compoundName != null ? CharOperation.toString(this.compoundName) : "UNNAMED");
   }
}
