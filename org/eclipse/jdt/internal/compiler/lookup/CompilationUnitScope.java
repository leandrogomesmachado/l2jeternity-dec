package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CompoundNameVector;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.HashtableOfType;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SimpleNameVector;

public class CompilationUnitScope extends Scope {
   public LookupEnvironment environment;
   public CompilationUnitDeclaration referenceContext;
   public char[][] currentPackageName;
   public PackageBinding fPackage;
   public ImportBinding[] imports;
   public int importPtr;
   public HashtableOfObject typeOrPackageCache;
   public SourceTypeBinding[] topLevelTypes;
   private CompoundNameVector qualifiedReferences;
   private SimpleNameVector simpleNameReferences;
   private SimpleNameVector rootReferences;
   private ObjectVector referencedTypes;
   private ObjectVector referencedSuperTypes;
   HashtableOfType constantPoolNameUsage;
   private int captureID = 1;
   private ImportBinding[] tempImports;
   public boolean suppressImportErrors;
   private boolean skipCachingImports;
   boolean connectingHierarchy;
   private ArrayList<Invocation> inferredInvocations;

   public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
      super(4, null);
      this.environment = environment;
      this.referenceContext = unit;
      unit.scope = this;
      this.currentPackageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;
      if (this.compilerOptions().produceReferenceInfo) {
         this.qualifiedReferences = new CompoundNameVector();
         this.simpleNameReferences = new SimpleNameVector();
         this.rootReferences = new SimpleNameVector();
         this.referencedTypes = new ObjectVector();
         this.referencedSuperTypes = new ObjectVector();
      } else {
         this.qualifiedReferences = null;
         this.simpleNameReferences = null;
         this.rootReferences = null;
         this.referencedTypes = null;
         this.referencedSuperTypes = null;
      }
   }

   void buildFieldsAndMethods() {
      int i = 0;

      for(int length = this.topLevelTypes.length; i < length; ++i) {
         this.topLevelTypes[i].scope.buildFieldsAndMethods();
      }
   }

   void buildTypeBindings(AccessRestriction accessRestriction) {
      this.topLevelTypes = new SourceTypeBinding[0];
      boolean firstIsSynthetic = false;
      if (this.referenceContext.compilationResult.compilationUnit != null) {
         char[][] expectedPackageName = this.referenceContext.compilationResult.compilationUnit.getPackageName();
         if (expectedPackageName != null && !CharOperation.equals(this.currentPackageName, expectedPackageName)) {
            if (this.referenceContext.currentPackage != null || this.referenceContext.types != null || this.referenceContext.imports != null) {
               this.problemReporter().packageIsNotExpectedPackage(this.referenceContext);
            }

            this.currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR : expectedPackageName;
         }
      }

      if (this.currentPackageName == CharOperation.NO_CHAR_CHAR) {
         this.fPackage = this.environment.defaultPackage;
      } else {
         if ((this.fPackage = this.environment.createPackage(this.currentPackageName)) == null) {
            if (this.referenceContext.currentPackage != null) {
               this.problemReporter().packageCollidesWithType(this.referenceContext);
            }

            this.fPackage = this.environment.defaultPackage;
            return;
         }

         if (this.referenceContext.isPackageInfo()) {
            if (this.referenceContext.types == null || this.referenceContext.types.length == 0) {
               this.referenceContext.types = new TypeDeclaration[1];
               this.referenceContext.createPackageInfoType();
               firstIsSynthetic = true;
            }

            if (this.referenceContext.currentPackage != null && this.referenceContext.currentPackage.annotations != null) {
               this.referenceContext.types[0].annotations = this.referenceContext.currentPackage.annotations;
            }
         }

         this.recordQualifiedReference(this.currentPackageName);
      }

      TypeDeclaration[] types = this.referenceContext.types;
      int typeLength = types == null ? 0 : types.length;
      this.topLevelTypes = new SourceTypeBinding[typeLength];
      int count = 0;

      for(int i = 0; i < typeLength; ++i) {
         TypeDeclaration typeDecl = types[i];
         if (this.environment.isProcessingAnnotations && this.environment.isMissingType(typeDecl.name)) {
            throw new SourceTypeCollisionException();
         }

         ReferenceBinding typeBinding = this.fPackage.getType0(typeDecl.name);
         this.recordSimpleReference(typeDecl.name);
         if (typeBinding != null && typeBinding.isValidBinding() && !(typeBinding instanceof UnresolvedReferenceBinding)) {
            if (this.environment.isProcessingAnnotations) {
               throw new SourceTypeCollisionException();
            }

            this.problemReporter().duplicateTypes(this.referenceContext, typeDecl);
         } else {
            if (this.fPackage != this.environment.defaultPackage && this.fPackage.getPackage(typeDecl.name) != null) {
               this.problemReporter().typeCollidesWithPackage(this.referenceContext, typeDecl);
            }

            char[] mainTypeName;
            if ((typeDecl.modifiers & 1) != 0
               && (mainTypeName = this.referenceContext.getMainTypeName()) != null
               && !CharOperation.equals(mainTypeName, typeDecl.name)) {
               this.problemReporter().publicClassMustMatchFileName(this.referenceContext, typeDecl);
            }

            ClassScope child = new ClassScope(this, typeDecl);
            SourceTypeBinding type = child.buildType(null, this.fPackage, accessRestriction);
            if (firstIsSynthetic && i == 0) {
               type.modifiers |= 4096;
            }

            if (type != null) {
               this.topLevelTypes[count++] = type;
            }
         }
      }

      if (count != this.topLevelTypes.length) {
         System.arraycopy(this.topLevelTypes, 0, this.topLevelTypes = new SourceTypeBinding[count], 0, count);
      }
   }

   void checkAndSetImports() {
      if (this.referenceContext.imports == null) {
         this.imports = this.getDefaultImports();
      } else {
         int numberOfStatements = this.referenceContext.imports.length;
         int numberOfImports = numberOfStatements + 1;

         for(int i = 0; i < numberOfStatements; ++i) {
            ImportReference importReference = this.referenceContext.imports[i];
            if ((importReference.bits & 131072) != 0 && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
               --numberOfImports;
               break;
            }
         }

         ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
         resolvedImports[0] = this.getDefaultImports()[0];
         int index = 1;

         label67:
         for(int i = 0; i < numberOfStatements; ++i) {
            ImportReference importReference = this.referenceContext.imports[i];
            char[][] compoundName = importReference.tokens;

            for(int j = 0; j < index; ++j) {
               ImportBinding resolved = resolvedImports[j];
               if (resolved.onDemand == ((importReference.bits & 131072) != 0)
                  && resolved.isStatic() == importReference.isStatic()
                  && CharOperation.equals(compoundName, resolvedImports[j].compoundName)) {
                  continue label67;
               }
            }

            if ((importReference.bits & 131072) != 0) {
               if (!CharOperation.equals(compoundName, this.currentPackageName)) {
                  Binding importBinding = this.findImport(compoundName, compoundName.length);
                  if (importBinding.isValidBinding() && (!importReference.isStatic() || !(importBinding instanceof PackageBinding))) {
                     resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
                  }
               }
            } else {
               resolvedImports[index++] = new ImportBinding(compoundName, false, null, importReference);
            }
         }

         if (resolvedImports.length > index) {
            System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
         }

         this.imports = resolvedImports;
      }
   }

   void checkParameterizedTypes() {
      if (this.compilerOptions().sourceLevel >= 3211264L) {
         int i = 0;

         for(int length = this.topLevelTypes.length; i < length; ++i) {
            ClassScope scope = this.topLevelTypes[i].scope;
            scope.checkParameterizedTypeBounds();
            scope.checkParameterizedSuperTypeCollisions();
         }
      }
   }

   public char[] computeConstantPoolName(LocalTypeBinding localType) {
      if (localType.constantPoolName != null) {
         return localType.constantPoolName;
      } else {
         if (this.constantPoolNameUsage == null) {
            this.constantPoolNameUsage = new HashtableOfType();
         }

         ReferenceBinding outerMostEnclosingType = localType.scope.outerMostClassScope().enclosingSourceType();
         int index = 0;
         boolean isCompliant15 = this.compilerOptions().complianceLevel >= 3211264L;

         while(true) {
            char[] candidateName;
            if (localType.isMemberType()) {
               if (index == 0) {
                  candidateName = CharOperation.concat(localType.enclosingType().constantPoolName(), localType.sourceName, '$');
               } else {
                  candidateName = CharOperation.concat(
                     localType.enclosingType().constantPoolName(), '$', String.valueOf(index).toCharArray(), '$', localType.sourceName
                  );
               }
            } else if (localType.isAnonymousType()) {
               if (isCompliant15) {
                  candidateName = CharOperation.concat(localType.enclosingType.constantPoolName(), String.valueOf(index + 1).toCharArray(), '$');
               } else {
                  candidateName = CharOperation.concat(outerMostEnclosingType.constantPoolName(), String.valueOf(index + 1).toCharArray(), '$');
               }
            } else if (isCompliant15) {
               candidateName = CharOperation.concat(
                  CharOperation.concat(localType.enclosingType().constantPoolName(), String.valueOf(index + 1).toCharArray(), '$'), localType.sourceName
               );
            } else {
               candidateName = CharOperation.concat(
                  outerMostEnclosingType.constantPoolName(), '$', String.valueOf(index + 1).toCharArray(), '$', localType.sourceName
               );
            }

            if (this.constantPoolNameUsage.get(candidateName) == null) {
               this.constantPoolNameUsage.put(candidateName, localType);
               return candidateName;
            }

            ++index;
         }
      }
   }

   void connectTypeHierarchy() {
      int i = 0;

      for(int length = this.topLevelTypes.length; i < length; ++i) {
         this.topLevelTypes[i].scope.connectTypeHierarchy();
      }
   }

   void faultInImports() {
      boolean unresolvedFound = false;
      boolean reportUnresolved = !this.suppressImportErrors;
      if (this.typeOrPackageCache == null || this.skipCachingImports) {
         if (this.referenceContext.imports == null) {
            this.typeOrPackageCache = new HashtableOfObject(1);
         } else {
            int numberOfStatements = this.referenceContext.imports.length;
            HashtableOfType typesBySimpleNames = null;

            for(int i = 0; i < numberOfStatements; ++i) {
               if ((this.referenceContext.imports[i].bits & 131072) == 0) {
                  typesBySimpleNames = new HashtableOfType(this.topLevelTypes.length + numberOfStatements);
                  int j = 0;

                  for(int length = this.topLevelTypes.length; j < length; ++j) {
                     typesBySimpleNames.put(this.topLevelTypes[j].sourceName, this.topLevelTypes[j]);
                  }
                  break;
               }
            }

            int numberOfImports = numberOfStatements + 1;

            for(int i = 0; i < numberOfStatements; ++i) {
               ImportReference importReference = this.referenceContext.imports[i];
               if ((importReference.bits & 131072) != 0
                  && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens)
                  && !importReference.isStatic()) {
                  --numberOfImports;
                  break;
               }
            }

            this.tempImports = new ImportBinding[numberOfImports];
            this.tempImports[0] = this.getDefaultImports()[0];
            this.importPtr = 1;

            label137:
            for(int i = 0; i < numberOfStatements; ++i) {
               ImportReference importReference = this.referenceContext.imports[i];
               char[][] compoundName = importReference.tokens;

               for(int j = 0; j < this.importPtr; ++j) {
                  ImportBinding resolved = this.tempImports[j];
                  if (resolved.onDemand == ((importReference.bits & 131072) != 0)
                     && resolved.isStatic() == importReference.isStatic()
                     && CharOperation.equals(compoundName, resolved.compoundName)) {
                     this.problemReporter().unusedImport(importReference);
                     continue label137;
                  }
               }

               if ((importReference.bits & 131072) != 0) {
                  if (CharOperation.equals(compoundName, this.currentPackageName)) {
                     this.problemReporter().unusedImport(importReference);
                  } else {
                     Binding importBinding = this.findImport(compoundName, compoundName.length);
                     if (!importBinding.isValidBinding()) {
                        this.problemReporter().importProblem(importReference, importBinding);
                     } else if (importReference.isStatic() && importBinding instanceof PackageBinding) {
                        this.problemReporter().cannotImportPackage(importReference);
                     } else {
                        this.recordImportBinding(new ImportBinding(compoundName, true, importBinding, importReference));
                     }
                  }
               } else {
                  Binding importBinding = this.findSingleImport(compoundName, 13, importReference.isStatic());
                  if (!importBinding.isValidBinding() && importBinding.problemId() != 3) {
                     unresolvedFound = true;
                     if (reportUnresolved) {
                        this.problemReporter().importProblem(importReference, importBinding);
                     }
                  } else if (importBinding instanceof PackageBinding) {
                     this.problemReporter().cannotImportPackage(importReference);
                  } else if (this.checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName) != -1
                     && importReference.isStatic()) {
                     if (importBinding.kind() == 1) {
                        this.checkMoreStaticBindings(compoundName, typesBySimpleNames, 12, importReference);
                     } else if (importBinding.kind() == 8) {
                        this.checkMoreStaticBindings(compoundName, typesBySimpleNames, 4, importReference);
                     }
                  }
               }
            }

            if (this.tempImports.length > this.importPtr) {
               System.arraycopy(this.tempImports, 0, this.tempImports = new ImportBinding[this.importPtr], 0, this.importPtr);
            }

            this.imports = this.tempImports;
            int length = this.imports.length;
            this.typeOrPackageCache = new HashtableOfObject(length);

            for(int i = 0; i < length; ++i) {
               ImportBinding binding = this.imports[i];
               if (!binding.onDemand && binding.resolvedImport instanceof ReferenceBinding || binding instanceof ImportConflictBinding) {
                  this.typeOrPackageCache.put(binding.compoundName[binding.compoundName.length - 1], binding);
               }
            }

            this.skipCachingImports = this.suppressImportErrors && unresolvedFound;
         }
      }
   }

   public void faultInTypes() {
      this.faultInImports();
      int i = 0;

      for(int length = this.topLevelTypes.length; i < length; ++i) {
         this.topLevelTypes[i].faultInTypesForFieldsAndMethods();
      }
   }

   public Binding findImport(char[][] compoundName, boolean findStaticImports, boolean onDemand) {
      return onDemand ? this.findImport(compoundName, compoundName.length) : this.findSingleImport(compoundName, 13, findStaticImports);
   }

   private Binding findImport(char[][] compoundName, int length) {
      this.recordQualifiedReference(compoundName);
      Binding binding = this.environment.getTopLevelPackage(compoundName[0]);
      int i = 1;
      if (binding != null) {
         PackageBinding packageBinding = (PackageBinding)binding;

         while(true) {
            if (i >= length) {
               return packageBinding;
            }

            binding = packageBinding.getTypeOrPackage(compoundName[i++]);
            if (binding == null || !binding.isValidBinding()) {
               binding = null;
               break;
            }

            if (!(binding instanceof PackageBinding)) {
               break;
            }

            packageBinding = (PackageBinding)binding;
         }
      }

      ReferenceBinding type;
      if (binding == null) {
         if (this.compilerOptions().complianceLevel >= 3145728L) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, 1);
         }

         type = this.findType(compoundName[0], this.environment.defaultPackage, this.environment.defaultPackage);
         if (type == null || !type.isValidBinding()) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, 1);
         }

         i = 1;
      } else {
         type = (ReferenceBinding)binding;
      }

      while(i < length) {
         type = (ReferenceBinding)this.environment.convertToRawType(type, false);
         if (!type.canBeSeenBy(this.fPackage)) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), type, 2);
         }

         char[] name = compoundName[i++];
         type = type.getMemberType(name);
         if (type == null) {
            return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, 1);
         }
      }

      return (Binding)(!type.canBeSeenBy(this.fPackage) ? new ProblemReferenceBinding(compoundName, type, 2) : type);
   }

   private Binding findSingleImport(char[][] compoundName, int mask, boolean findStaticImports) {
      if (compoundName.length == 1) {
         if (this.compilerOptions().complianceLevel >= 3145728L) {
            return new ProblemReferenceBinding(compoundName, null, 1);
         } else {
            ReferenceBinding typeBinding = this.findType(compoundName[0], this.environment.defaultPackage, this.fPackage);
            return (Binding)(typeBinding == null ? new ProblemReferenceBinding(compoundName, null, 1) : typeBinding);
         }
      } else {
         return findStaticImports ? this.findSingleStaticImport(compoundName, mask) : this.findImport(compoundName, compoundName.length);
      }
   }

   private Binding findSingleStaticImport(char[][] compoundName, int mask) {
      Binding binding = this.findImport(compoundName, compoundName.length - 1);
      if (!binding.isValidBinding()) {
         return binding;
      } else {
         char[] name = compoundName[compoundName.length - 1];
         if (binding instanceof PackageBinding) {
            Binding temp = ((PackageBinding)binding).getTypeOrPackage(name);
            return (Binding)(temp != null && temp instanceof ReferenceBinding
               ? new ProblemReferenceBinding(compoundName, (ReferenceBinding)temp, 14)
               : binding);
         } else {
            ReferenceBinding type = (ReferenceBinding)binding;
            FieldBinding field = (mask & 1) != 0 ? this.findField(type, name, null, true) : null;
            if (field != null) {
               if (field.problemId() == 3 && ((ProblemFieldBinding)field).closestMatch.isStatic()) {
                  return field;
               }

               if (field.isValidBinding() && field.isStatic() && field.canBeSeenBy(type, null, this)) {
                  return field;
               }
            }

            MethodBinding method = (mask & 8) != 0 ? this.findStaticMethod(type, name) : null;
            if (method != null) {
               return method;
            } else {
               type = this.findMemberType(name, type);
               if (type != null && type.isStatic()) {
                  if (type.isValidBinding() && !type.canBeSeenBy(this.fPackage)) {
                     return new ProblemReferenceBinding(compoundName, type, 2);
                  } else {
                     return (Binding)(type.problemId() == 2
                        ? new ProblemReferenceBinding(compoundName, ((ProblemReferenceBinding)type).closestMatch, 2)
                        : type);
                  }
               } else {
                  return (Binding)(field != null && !field.isValidBinding() && field.problemId() != 1
                     ? field
                     : new ProblemReferenceBinding(compoundName, type, 1));
               }
            }
         }
      }
   }

   private MethodBinding findStaticMethod(ReferenceBinding currentType, char[] selector) {
      if (!currentType.canBeSeenBy(this)) {
         return null;
      } else {
         do {
            currentType.initializeForStaticImports();
            MethodBinding[] methods = currentType.getMethods(selector);
            if (methods != Binding.NO_METHODS) {
               int i = methods.length;

               while(--i >= 0) {
                  MethodBinding method = methods[i];
                  if (method.isStatic() && method.canBeSeenBy(this.fPackage)) {
                     return method;
                  }
               }
            }
         } while((currentType = currentType.superclass()) != null);

         return null;
      }
   }

   ImportBinding[] getDefaultImports() {
      if (this.environment.defaultImports != null) {
         return this.environment.defaultImports;
      } else {
         Binding importBinding = this.environment.getTopLevelPackage(TypeConstants.JAVA);
         if (importBinding != null) {
            importBinding = ((PackageBinding)importBinding).getTypeOrPackage(TypeConstants.JAVA_LANG[1]);
         }

         if (importBinding == null || !importBinding.isValidBinding()) {
            this.problemReporter().isClassPathCorrect(TypeConstants.JAVA_LANG_OBJECT, this.referenceContext, this.environment.missingClassFileLocation);
            BinaryTypeBinding missingObject = this.environment.createMissingType(null, TypeConstants.JAVA_LANG_OBJECT);
            importBinding = missingObject.fPackage;
         }

         return this.environment.defaultImports = new ImportBinding[]{new ImportBinding(TypeConstants.JAVA_LANG, true, importBinding, null)};
      }
   }

   public final Binding getImport(char[][] compoundName, boolean onDemand, boolean isStaticImport) {
      return onDemand ? this.findImport(compoundName, compoundName.length) : this.findSingleImport(compoundName, 13, isStaticImport);
   }

   public int nextCaptureID() {
      return this.captureID++;
   }

   @Override
   public ProblemReporter problemReporter() {
      ProblemReporter problemReporter = this.referenceContext.problemReporter;
      problemReporter.referenceContext = this.referenceContext;
      return problemReporter;
   }

   void recordQualifiedReference(char[][] qualifiedName) {
      if (this.qualifiedReferences != null) {
         int length = qualifiedName.length;
         if (length > 1) {
            this.recordRootReference(qualifiedName[0]);

            while(!this.qualifiedReferences.contains(qualifiedName)) {
               this.qualifiedReferences.add(qualifiedName);
               if (length == 2) {
                  this.recordSimpleReference(qualifiedName[0]);
                  this.recordSimpleReference(qualifiedName[1]);
                  return;
               }

               this.recordSimpleReference(qualifiedName[--length]);
               System.arraycopy(qualifiedName, 0, qualifiedName = new char[length][], 0, length);
            }
         } else if (length == 1) {
            this.recordRootReference(qualifiedName[0]);
            this.recordSimpleReference(qualifiedName[0]);
         }
      }
   }

   void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
      this.recordQualifiedReference(qualifiedEnclosingName);
      if (qualifiedEnclosingName.length == 0) {
         this.recordRootReference(simpleName);
      }

      this.recordSimpleReference(simpleName);
   }

   void recordReference(ReferenceBinding type, char[] simpleName) {
      ReferenceBinding actualType = this.typeToRecord(type);
      if (actualType != null) {
         this.recordReference(actualType.compoundName, simpleName);
      }
   }

   void recordRootReference(char[] simpleName) {
      if (this.rootReferences != null) {
         if (!this.rootReferences.contains(simpleName)) {
            this.rootReferences.add(simpleName);
         }
      }
   }

   void recordSimpleReference(char[] simpleName) {
      if (this.simpleNameReferences != null) {
         if (!this.simpleNameReferences.contains(simpleName)) {
            this.simpleNameReferences.add(simpleName);
         }
      }
   }

   void recordSuperTypeReference(TypeBinding type) {
      if (this.referencedSuperTypes != null) {
         ReferenceBinding actualType = this.typeToRecord(type);
         if (actualType != null && !this.referencedSuperTypes.containsIdentical(actualType)) {
            this.referencedSuperTypes.add(actualType);
         }
      }
   }

   public void recordTypeConversion(TypeBinding superType, TypeBinding subType) {
      this.recordSuperTypeReference(subType);
   }

   void recordTypeReference(TypeBinding type) {
      if (this.referencedTypes != null) {
         ReferenceBinding actualType = this.typeToRecord(type);
         if (actualType != null && !this.referencedTypes.containsIdentical(actualType)) {
            this.referencedTypes.add(actualType);
         }
      }
   }

   void recordTypeReferences(TypeBinding[] types) {
      if (this.referencedTypes != null) {
         if (types != null && types.length != 0) {
            int i = 0;

            for(int max = types.length; i < max; ++i) {
               ReferenceBinding actualType = this.typeToRecord(types[i]);
               if (actualType != null && !this.referencedTypes.containsIdentical(actualType)) {
                  this.referencedTypes.add(actualType);
               }
            }
         }
      }
   }

   Binding resolveSingleImport(ImportBinding importBinding, int mask) {
      if (importBinding.resolvedImport == null) {
         importBinding.resolvedImport = this.findSingleImport(importBinding.compoundName, mask, importBinding.isStatic());
         if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
            if (importBinding.resolvedImport.problemId() == 3) {
               return importBinding.resolvedImport;
            }

            if (this.imports != null) {
               ImportBinding[] newImports = new ImportBinding[this.imports.length - 1];
               int i = 0;
               int n = 0;

               for(int max = this.imports.length; i < max; ++i) {
                  if (this.imports[i] != importBinding) {
                     newImports[n++] = this.imports[i];
                  }
               }

               this.imports = newImports;
            }

            return null;
         }
      }

      return importBinding.resolvedImport;
   }

   public void storeDependencyInfo() {
      for(int i = 0; i < this.referencedSuperTypes.size; ++i) {
         ReferenceBinding type = (ReferenceBinding)this.referencedSuperTypes.elementAt(i);
         if (!this.referencedTypes.containsIdentical(type)) {
            this.referencedTypes.add(type);
         }

         if (!type.isLocalType()) {
            ReferenceBinding enclosing = type.enclosingType();
            if (enclosing != null) {
               this.recordSuperTypeReference(enclosing);
            }
         }

         ReferenceBinding superclass = type.superclass();
         if (superclass != null) {
            this.recordSuperTypeReference(superclass);
         }

         ReferenceBinding[] interfaces = type.superInterfaces();
         if (interfaces != null) {
            int j = 0;

            for(int length = interfaces.length; j < length; ++j) {
               this.recordSuperTypeReference(interfaces[j]);
            }
         }
      }

      int i = 0;

      for(int l = this.referencedTypes.size; i < l; ++i) {
         ReferenceBinding type = (ReferenceBinding)this.referencedTypes.elementAt(i);
         if (!type.isLocalType()) {
            this.recordQualifiedReference(type.isMemberType() ? CharOperation.splitOn('.', type.readableName()) : type.compoundName);
         }
      }

      i = this.qualifiedReferences.size;
      char[][][] qualifiedRefs = new char[i][][];

      for(int ix = 0; ix < i; ++ix) {
         qualifiedRefs[ix] = this.qualifiedReferences.elementAt(ix);
      }

      this.referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;
      i = this.simpleNameReferences.size;
      char[][] simpleRefs = new char[i][];

      for(int ix = 0; ix < i; ++ix) {
         simpleRefs[ix] = this.simpleNameReferences.elementAt(ix);
      }

      this.referenceContext.compilationResult.simpleNameReferences = simpleRefs;
      i = this.rootReferences.size;
      char[][] rootRefs = new char[i][];

      for(int ix = 0; ix < i; ++ix) {
         rootRefs[ix] = this.rootReferences.elementAt(ix);
      }

      this.referenceContext.compilationResult.rootReferences = rootRefs;
   }

   @Override
   public String toString() {
      return "--- CompilationUnit Scope : " + new String(this.referenceContext.getFileName());
   }

   private ReferenceBinding typeToRecord(TypeBinding type) {
      if (type == null) {
         return null;
      } else {
         while(type.isArrayType()) {
            type = ((ArrayBinding)type).leafComponentType();
         }

         switch(type.kind()) {
            case 132:
            case 516:
            case 4100:
            case 8196:
            case 32772:
            case 65540:
               return null;
            case 260:
            case 1028:
               type = type.erasure();
            default:
               ReferenceBinding refType = (ReferenceBinding)type;
               return refType.isLocalType() ? null : refType;
         }
      }
   }

   public void verifyMethods(MethodVerifier verifier) {
      int i = 0;

      for(int length = this.topLevelTypes.length; i < length; ++i) {
         this.topLevelTypes[i].verifyMethods(verifier);
      }
   }

   private void recordImportBinding(ImportBinding bindingToAdd) {
      if (this.tempImports.length == this.importPtr) {
         System.arraycopy(this.tempImports, 0, this.tempImports = new ImportBinding[this.importPtr + 1], 0, this.importPtr);
      }

      this.tempImports[this.importPtr++] = bindingToAdd;
   }

   private void checkMoreStaticBindings(char[][] compoundName, HashtableOfType typesBySimpleNames, int mask, ImportReference importReference) {
      Binding importBinding = this.findSingleStaticImport(compoundName, mask);
      if (!importBinding.isValidBinding()) {
         if (importBinding.problemId() == 3) {
            this.checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
         }
      } else {
         this.checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
      }

      if ((mask & 8) != 0 && importBinding.kind() == 8) {
         mask &= -9;
         this.checkMoreStaticBindings(compoundName, typesBySimpleNames, mask, importReference);
      }
   }

   private int checkAndRecordImportBinding(Binding importBinding, HashtableOfType typesBySimpleNames, ImportReference importReference, char[][] compoundName) {
      ReferenceBinding conflictingType = null;
      if (importBinding instanceof MethodBinding) {
         conflictingType = (ReferenceBinding)this.getType(compoundName, compoundName.length);
         if (!conflictingType.isValidBinding() || importReference.isStatic() && !conflictingType.isStatic()) {
            conflictingType = null;
         }
      }

      char[] name = compoundName[compoundName.length - 1];
      if (!(importBinding instanceof ReferenceBinding) && conflictingType == null) {
         if (importBinding instanceof FieldBinding) {
            for(int j = 0; j < this.importPtr; ++j) {
               ImportBinding resolved = this.tempImports[j];
               if (resolved.isStatic()
                  && resolved.resolvedImport instanceof FieldBinding
                  && importBinding != resolved.resolvedImport
                  && CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
                  if (this.compilerOptions().sourceLevel >= 3407872L) {
                     FieldBinding field = (FieldBinding)resolved.resolvedImport;
                     resolved.resolvedImport = new ProblemFieldBinding(field, field.declaringClass, name, 3);
                     return -1;
                  }

                  this.problemReporter().duplicateImport(importReference);
                  return -1;
               }
            }
         }
      } else {
         ReferenceBinding referenceBinding = conflictingType == null ? (ReferenceBinding)importBinding : conflictingType;
         ReferenceBinding typeToCheck = referenceBinding.problemId() == 3 ? ((ProblemReferenceBinding)referenceBinding).closestMatch : referenceBinding;
         if (importReference.isTypeUseDeprecated(typeToCheck, this)) {
            this.problemReporter().deprecatedType(typeToCheck, importReference);
         }

         ReferenceBinding existingType = typesBySimpleNames.get(name);
         if (existingType != null) {
            if (TypeBinding.equalsEquals(existingType, referenceBinding)) {
               for(int j = 0; j < this.importPtr; ++j) {
                  ImportBinding resolved = this.tempImports[j];
                  if (resolved instanceof ImportConflictBinding) {
                     ImportConflictBinding importConflictBinding = (ImportConflictBinding)resolved;
                     if (TypeBinding.equalsEquals(importConflictBinding.conflictingTypeBinding, referenceBinding) && !importReference.isStatic()) {
                        this.problemReporter().duplicateImport(importReference);
                        this.recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
                     }
                  } else if (resolved.resolvedImport == referenceBinding && importReference.isStatic() != resolved.isStatic()) {
                     this.problemReporter().duplicateImport(importReference);
                     this.recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
                  }
               }

               return -1;
            }

            int j = 0;

            for(int length = this.topLevelTypes.length; j < length; ++j) {
               if (CharOperation.equals(this.topLevelTypes[j].sourceName, existingType.sourceName)) {
                  this.problemReporter().conflictingImport(importReference);
                  return -1;
               }
            }

            if (importReference.isStatic() && importBinding instanceof ReferenceBinding && this.compilerOptions().sourceLevel >= 3407872L) {
               for(int jx = 0; jx < this.importPtr; ++jx) {
                  ImportBinding resolved = this.tempImports[jx];
                  if (resolved.isStatic()
                     && resolved.resolvedImport instanceof ReferenceBinding
                     && importBinding != resolved.resolvedImport
                     && CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
                     ReferenceBinding type = (ReferenceBinding)resolved.resolvedImport;
                     resolved.resolvedImport = new ProblemReferenceBinding(new char[][]{name}, type, 3);
                     return -1;
                  }
               }
            }

            this.problemReporter().duplicateImport(importReference);
            return -1;
         }

         typesBySimpleNames.put(name, referenceBinding);
      }

      if (conflictingType == null) {
         this.recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
      } else {
         this.recordImportBinding(new ImportConflictBinding(compoundName, importBinding, conflictingType, importReference));
      }

      return this.importPtr;
   }

   @Override
   public boolean hasDefaultNullnessFor(int location) {
      if (this.fPackage != null) {
         return (this.fPackage.defaultNullness & location) != 0;
      } else {
         return false;
      }
   }

   public void registerInferredInvocation(Invocation invocation) {
      if (this.inferredInvocations == null) {
         this.inferredInvocations = new ArrayList<>();
      }

      this.inferredInvocations.add(invocation);
   }

   public void cleanUpInferenceContexts() {
      if (this.inferredInvocations != null) {
         for(Invocation invocation : this.inferredInvocations) {
            invocation.cleanUpInferenceContexts();
         }

         this.inferredInvocations = null;
      }
   }
}
