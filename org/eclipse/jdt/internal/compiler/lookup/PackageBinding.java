package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.HashtableOfType;

public class PackageBinding extends Binding implements TypeConstants {
   public long tagBits = 0L;
   public char[][] compoundName;
   PackageBinding parent;
   public LookupEnvironment environment;
   HashtableOfType knownTypes;
   HashtableOfPackage knownPackages;
   protected int defaultNullness = 0;

   protected PackageBinding() {
   }

   public PackageBinding(char[] topLevelPackageName, LookupEnvironment environment) {
      this(new char[][]{topLevelPackageName}, null, environment);
   }

   public PackageBinding(char[][] compoundName, PackageBinding parent, LookupEnvironment environment) {
      this.compoundName = compoundName;
      this.parent = parent;
      this.environment = environment;
      this.knownTypes = null;
      this.knownPackages = new HashtableOfPackage(3);
      if (compoundName != CharOperation.NO_CHAR_CHAR) {
         this.checkIfNullAnnotationPackage();
      }
   }

   public PackageBinding(LookupEnvironment environment) {
      this(CharOperation.NO_CHAR_CHAR, null, environment);
   }

   private void addNotFoundPackage(char[] simpleName) {
      this.knownPackages.put(simpleName, LookupEnvironment.TheNotFoundPackage);
   }

   private void addNotFoundType(char[] simpleName) {
      if (this.knownTypes == null) {
         this.knownTypes = new HashtableOfType(25);
      }

      this.knownTypes.put(simpleName, LookupEnvironment.TheNotFoundType);
   }

   void addPackage(PackageBinding element) {
      if ((element.tagBits & 128L) == 0L) {
         this.clearMissingTagBit();
      }

      this.knownPackages.put(element.compoundName[element.compoundName.length - 1], element);
   }

   void addType(ReferenceBinding element) {
      if ((element.tagBits & 128L) == 0L) {
         this.clearMissingTagBit();
      }

      if (this.knownTypes == null) {
         this.knownTypes = new HashtableOfType(25);
      }

      char[] name = element.compoundName[element.compoundName.length - 1];
      ReferenceBinding priorType = this.knownTypes.getput(name, element);
      if (priorType != null && priorType.isUnresolvedType() && !element.isUnresolvedType()) {
         ((UnresolvedReferenceBinding)priorType).setResolvedType(element, this.environment);
      }

      if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled && (element.isAnnotationType() || element instanceof UnresolvedReferenceBinding)
         )
       {
         this.checkIfNullAnnotationType(element);
      }
   }

   void clearMissingTagBit() {
      PackageBinding current = this;

      do {
         current.tagBits &= -129L;
      } while((current = current.parent) != null);
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      return CharOperation.concatWith(this.compoundName, '/');
   }

   private PackageBinding findPackage(char[] name) {
      if (!this.environment.isPackage(this.compoundName, name)) {
         return null;
      } else {
         char[][] subPkgCompoundName = CharOperation.arrayConcat(this.compoundName, name);
         PackageBinding subPackageBinding = new PackageBinding(subPkgCompoundName, this, this.environment);
         this.addPackage(subPackageBinding);
         return subPackageBinding;
      }
   }

   PackageBinding getPackage(char[] name) {
      PackageBinding binding = this.getPackage0(name);
      if (binding != null) {
         return binding == LookupEnvironment.TheNotFoundPackage ? null : binding;
      } else if ((binding = this.findPackage(name)) != null) {
         return binding;
      } else {
         this.addNotFoundPackage(name);
         return null;
      }
   }

   PackageBinding getPackage0(char[] name) {
      return this.knownPackages.get(name);
   }

   ReferenceBinding getType(char[] name) {
      ReferenceBinding referenceBinding = this.getType0(name);
      if (referenceBinding == null && (referenceBinding = this.environment.askForType(this, name)) == null) {
         this.addNotFoundType(name);
         return null;
      } else if (referenceBinding == LookupEnvironment.TheNotFoundType) {
         return null;
      } else {
         referenceBinding = (ReferenceBinding)BinaryTypeBinding.resolveType(referenceBinding, this.environment, false);
         return (ReferenceBinding)(referenceBinding.isNestedType() ? new ProblemReferenceBinding(new char[][]{name}, referenceBinding, 4) : referenceBinding);
      }
   }

   ReferenceBinding getType0(char[] name) {
      return this.knownTypes == null ? null : this.knownTypes.get(name);
   }

   public Binding getTypeOrPackage(char[] name) {
      ReferenceBinding referenceBinding = this.getType0(name);
      if (referenceBinding != null && referenceBinding != LookupEnvironment.TheNotFoundType) {
         referenceBinding = (ReferenceBinding)BinaryTypeBinding.resolveType(referenceBinding, this.environment, false);
         if (referenceBinding.isNestedType()) {
            return new ProblemReferenceBinding(new char[][]{name}, referenceBinding, 4);
         }

         if ((referenceBinding.tagBits & 128L) == 0L) {
            return referenceBinding;
         }
      }

      PackageBinding packageBinding = this.getPackage0(name);
      if (packageBinding != null && packageBinding != LookupEnvironment.TheNotFoundPackage) {
         return packageBinding;
      } else {
         if (packageBinding == null) {
            if ((packageBinding = this.findPackage(name)) != null) {
               return packageBinding;
            }

            if (referenceBinding != null && referenceBinding != LookupEnvironment.TheNotFoundType) {
               return referenceBinding;
            }

            this.addNotFoundPackage(name);
         }

         if (referenceBinding == null) {
            if ((referenceBinding = this.environment.askForType(this, name)) != null) {
               if (referenceBinding.isNestedType()) {
                  return new ProblemReferenceBinding(new char[][]{name}, referenceBinding, 4);
               }

               return referenceBinding;
            }

            this.addNotFoundType(name);
         }

         return null;
      }
   }

   public final boolean isViewedAsDeprecated() {
      if ((this.tagBits & 17179869184L) == 0L) {
         this.tagBits |= 17179869184L;
         if (this.compoundName != CharOperation.NO_CHAR_CHAR) {
            ReferenceBinding packageInfo = this.getType(TypeConstants.PACKAGE_INFO_NAME);
            if (packageInfo != null) {
               packageInfo.initializeDeprecatedAnnotationTagBits();
               this.tagBits |= packageInfo.tagBits & 1729382222550532096L;
            }
         }
      }

      return (this.tagBits & 70368744177664L) != 0L;
   }

   @Override
   public final int kind() {
      return 16;
   }

   @Override
   public int problemId() {
      return (this.tagBits & 128L) != 0L ? 1 : 0;
   }

   void checkIfNullAnnotationPackage() {
      LookupEnvironment env = this.environment;
      if (env.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
         if (this.isPackageOfQualifiedTypeName(this.compoundName, env.getNullableAnnotationName())) {
            env.nullableAnnotationPackage = this;
         }

         if (this.isPackageOfQualifiedTypeName(this.compoundName, env.getNonNullAnnotationName())) {
            env.nonnullAnnotationPackage = this;
         }

         if (this.isPackageOfQualifiedTypeName(this.compoundName, env.getNonNullByDefaultAnnotationName())) {
            env.nonnullByDefaultAnnotationPackage = this;
         }
      }
   }

   private boolean isPackageOfQualifiedTypeName(char[][] packageName, char[][] typeName) {
      int length;
      if (typeName != null && (length = packageName.length) == typeName.length - 1) {
         for(int i = 0; i < length; ++i) {
            if (!CharOperation.equals(packageName[i], typeName[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   void checkIfNullAnnotationType(ReferenceBinding type) {
      if (this.environment.nullableAnnotationPackage == this && CharOperation.equals(type.compoundName, this.environment.getNullableAnnotationName())) {
         type.typeBits |= 64;
         if (!(type instanceof UnresolvedReferenceBinding)) {
            this.environment.nullableAnnotationPackage = null;
         }
      } else if (this.environment.nonnullAnnotationPackage == this && CharOperation.equals(type.compoundName, this.environment.getNonNullAnnotationName())) {
         type.typeBits |= 32;
         if (!(type instanceof UnresolvedReferenceBinding)) {
            this.environment.nonnullAnnotationPackage = null;
         }
      } else if (this.environment.nonnullByDefaultAnnotationPackage == this
         && CharOperation.equals(type.compoundName, this.environment.getNonNullByDefaultAnnotationName())) {
         type.typeBits |= 128;
         if (!(type instanceof UnresolvedReferenceBinding)) {
            this.environment.nonnullByDefaultAnnotationPackage = null;
         }
      } else {
         type.typeBits |= this.environment.getNullAnnotationBit(type.compoundName);
      }
   }

   @Override
   public char[] readableName() {
      return CharOperation.concatWith(this.compoundName, '.');
   }

   @Override
   public String toString() {
      String str;
      if (this.compoundName == CharOperation.NO_CHAR_CHAR) {
         str = "The Default Package";
      } else {
         str = "package " + (this.compoundName != null ? CharOperation.toString(this.compoundName) : "UNNAMED");
      }

      if ((this.tagBits & 128L) != 0L) {
         str = str + "[MISSING]";
      }

      return str;
   }
}
