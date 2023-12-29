package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ClassFilePool;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironmentExtension;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class LookupEnvironment implements ProblemReasons, TypeConstants {
   private Map accessRestrictions;
   ImportBinding[] defaultImports;
   public PackageBinding defaultPackage;
   HashtableOfPackage knownPackages;
   private int lastCompletedUnitIndex = -1;
   private int lastUnitIndex = -1;
   TypeSystem typeSystem;
   public INameEnvironment nameEnvironment;
   public CompilerOptions globalOptions;
   public ProblemReporter problemReporter;
   public ClassFilePool classFilePool;
   private int stepCompleted;
   public ITypeRequestor typeRequestor;
   private SimpleLookupTable uniqueParameterizedGenericMethodBindings;
   private SimpleLookupTable uniquePolymorphicMethodBindings;
   private SimpleLookupTable uniqueGetClassMethodBinding;
   public CompilationUnitDeclaration unitBeingCompleted = null;
   public Object missingClassFileLocation = null;
   private CompilationUnitDeclaration[] units = new CompilationUnitDeclaration[4];
   private MethodVerifier verifier;
   public MethodBinding arrayClone;
   private ArrayList missingTypes;
   Set<SourceTypeBinding> typesBeingConnected;
   public boolean isProcessingAnnotations = false;
   public boolean mayTolerateMissingType = false;
   PackageBinding nullableAnnotationPackage;
   PackageBinding nonnullAnnotationPackage;
   PackageBinding nonnullByDefaultAnnotationPackage;
   AnnotationBinding nonNullAnnotation;
   AnnotationBinding nullableAnnotation;
   Map<String, Integer> allNullAnnotations = null;
   final List<MethodBinding> deferredEnumMethods = new ArrayList<>();
   InferenceContext18 currentInferenceContext;
   static final int BUILD_FIELDS_AND_METHODS = 4;
   static final int BUILD_TYPE_HIERARCHY = 1;
   static final int CHECK_AND_SET_IMPORTS = 2;
   static final int CONNECT_TYPE_HIERARCHY = 3;
   static final ProblemPackageBinding TheNotFoundPackage = new ProblemPackageBinding(CharOperation.NO_CHAR, 1);
   static final ProblemReferenceBinding TheNotFoundType = new ProblemReferenceBinding(CharOperation.NO_CHAR_CHAR, null, 1);
   public IQualifiedTypeResolutionListener[] resolutionListeners = new IQualifiedTypeResolutionListener[0];

   public LookupEnvironment(ITypeRequestor typeRequestor, CompilerOptions globalOptions, ProblemReporter problemReporter, INameEnvironment nameEnvironment) {
      this.typeRequestor = typeRequestor;
      this.globalOptions = globalOptions;
      this.problemReporter = problemReporter;
      this.defaultPackage = new PackageBinding(this);
      this.defaultImports = null;
      this.nameEnvironment = nameEnvironment;
      this.knownPackages = new HashtableOfPackage();
      this.uniqueParameterizedGenericMethodBindings = new SimpleLookupTable(3);
      this.uniquePolymorphicMethodBindings = new SimpleLookupTable(3);
      this.missingTypes = null;
      this.accessRestrictions = new HashMap(3);
      this.classFilePool = ClassFilePool.newInstance();
      this.typesBeingConnected = new HashSet<>();
      this.typeSystem = (TypeSystem)(this.globalOptions.sourceLevel >= 3407872L && this.globalOptions.storeAnnotations
         ? new AnnotatableTypeSystem(this)
         : new TypeSystem(this));
   }

   public ReferenceBinding askForType(char[][] compoundName) {
      NameEnvironmentAnswer answer = this.nameEnvironment.findType(compoundName);
      if (answer == null) {
         return null;
      } else {
         if (answer.isBinaryType()) {
            this.typeRequestor.accept(answer.getBinaryType(), this.computePackageFrom(compoundName, false), answer.getAccessRestriction());
         } else if (answer.isCompilationUnit()) {
            this.typeRequestor.accept(answer.getCompilationUnit(), answer.getAccessRestriction());
         } else if (answer.isSourceType()) {
            this.typeRequestor.accept(answer.getSourceTypes(), this.computePackageFrom(compoundName, false), answer.getAccessRestriction());
         }

         return this.getCachedType(compoundName);
      }
   }

   ReferenceBinding askForType(PackageBinding packageBinding, char[] name) {
      if (packageBinding == null) {
         packageBinding = this.defaultPackage;
      }

      NameEnvironmentAnswer answer = this.nameEnvironment.findType(name, packageBinding.compoundName);
      if (answer == null) {
         return null;
      } else {
         if (answer.isBinaryType()) {
            this.typeRequestor.accept(answer.getBinaryType(), packageBinding, answer.getAccessRestriction());
         } else if (answer.isCompilationUnit()) {
            try {
               this.typeRequestor.accept(answer.getCompilationUnit(), answer.getAccessRestriction());
            } catch (AbortCompilation var6) {
               if (CharOperation.equals(name, TypeConstants.PACKAGE_INFO_NAME)) {
                  return null;
               }

               throw var6;
            }
         } else if (answer.isSourceType()) {
            this.typeRequestor.accept(answer.getSourceTypes(), packageBinding, answer.getAccessRestriction());
            ReferenceBinding binding = packageBinding.getType0(name);
            String externalAnnotationPath = answer.getExternalAnnotationPath();
            if (externalAnnotationPath != null && this.globalOptions.isAnnotationBasedNullAnalysisEnabled && binding instanceof SourceTypeBinding) {
               ExternalAnnotationSuperimposer.apply((SourceTypeBinding)binding, externalAnnotationPath);
            }

            return binding;
         }

         return packageBinding.getType0(name);
      }
   }

   public void buildTypeBindings(CompilationUnitDeclaration unit, AccessRestriction accessRestriction) {
      CompilationUnitScope scope = new CompilationUnitScope(unit, this);
      scope.buildTypeBindings(accessRestriction);
      int unitsLength = this.units.length;
      if (++this.lastUnitIndex >= unitsLength) {
         System.arraycopy(this.units, 0, this.units = new CompilationUnitDeclaration[2 * unitsLength], 0, unitsLength);
      }

      this.units[this.lastUnitIndex] = unit;
   }

   public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType, AccessRestriction accessRestriction) {
      return this.cacheBinaryType(binaryType, true, accessRestriction);
   }

   public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType, boolean needFieldsAndMethods, AccessRestriction accessRestriction) {
      char[][] compoundName = CharOperation.splitOn('/', binaryType.getName());
      ReferenceBinding existingType = this.getCachedType(compoundName);
      return existingType != null && !(existingType instanceof UnresolvedReferenceBinding)
         ? null
         : this.createBinaryTypeFrom(binaryType, this.computePackageFrom(compoundName, false), needFieldsAndMethods, accessRestriction);
   }

   public void completeTypeBindings() {
      this.stepCompleted = 1;

      for(int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; ++i) {
         (this.unitBeingCompleted = this.units[i]).scope.checkAndSetImports();
      }

      this.stepCompleted = 2;

      for(int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; ++i) {
         (this.unitBeingCompleted = this.units[i]).scope.connectTypeHierarchy();
      }

      this.stepCompleted = 3;

      for(int i = this.lastCompletedUnitIndex + 1; i <= this.lastUnitIndex; ++i) {
         CompilationUnitScope unitScope = (this.unitBeingCompleted = this.units[i]).scope;
         unitScope.checkParameterizedTypes();
         unitScope.buildFieldsAndMethods();
         this.units[i] = null;
      }

      this.stepCompleted = 4;
      this.lastCompletedUnitIndex = this.lastUnitIndex;
      this.unitBeingCompleted = null;
   }

   public void completeTypeBindings(CompilationUnitDeclaration parsedUnit) {
      if (this.stepCompleted == 4) {
         this.completeTypeBindings();
      } else {
         if (parsedUnit.scope == null) {
            return;
         }

         if (this.stepCompleted >= 2) {
            (this.unitBeingCompleted = parsedUnit).scope.checkAndSetImports();
         }

         if (this.stepCompleted >= 3) {
            (this.unitBeingCompleted = parsedUnit).scope.connectTypeHierarchy();
         }

         this.unitBeingCompleted = null;
      }
   }

   public void completeTypeBindings(CompilationUnitDeclaration parsedUnit, boolean buildFieldsAndMethods) {
      if (parsedUnit.scope != null) {
         (this.unitBeingCompleted = parsedUnit).scope.checkAndSetImports();
         parsedUnit.scope.connectTypeHierarchy();
         parsedUnit.scope.checkParameterizedTypes();
         if (buildFieldsAndMethods) {
            parsedUnit.scope.buildFieldsAndMethods();
         }

         this.unitBeingCompleted = null;
      }
   }

   public void completeTypeBindings(CompilationUnitDeclaration[] parsedUnits, boolean[] buildFieldsAndMethods, int unitCount) {
      for(int i = 0; i < unitCount; ++i) {
         CompilationUnitDeclaration parsedUnit = parsedUnits[i];
         if (parsedUnit.scope != null) {
            (this.unitBeingCompleted = parsedUnit).scope.checkAndSetImports();
         }
      }

      for(int i = 0; i < unitCount; ++i) {
         CompilationUnitDeclaration parsedUnit = parsedUnits[i];
         if (parsedUnit.scope != null) {
            (this.unitBeingCompleted = parsedUnit).scope.connectTypeHierarchy();
         }
      }

      for(int i = 0; i < unitCount; ++i) {
         CompilationUnitDeclaration parsedUnit = parsedUnits[i];
         if (parsedUnit.scope != null) {
            (this.unitBeingCompleted = parsedUnit).scope.checkParameterizedTypes();
            if (buildFieldsAndMethods[i]) {
               parsedUnit.scope.buildFieldsAndMethods();
            }
         }
      }

      this.unitBeingCompleted = null;
   }

   public MethodBinding computeArrayClone(MethodBinding objectClone) {
      if (this.arrayClone == null) {
         this.arrayClone = new MethodBinding(
            objectClone.modifiers & -5 | 1,
            TypeConstants.CLONE,
            objectClone.returnType,
            Binding.NO_PARAMETERS,
            Binding.NO_EXCEPTIONS,
            (ReferenceBinding)objectClone.returnType
         );
      }

      return this.arrayClone;
   }

   public TypeBinding computeBoxingType(TypeBinding type) {
      switch(type.id) {
         case 2:
            TypeBinding boxedType = this.getType(JAVA_LANG_CHARACTER);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_CHARACTER, null, 1);
         case 3:
            TypeBinding boxedType = this.getType(JAVA_LANG_BYTE);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_BYTE, null, 1);
         case 4:
            TypeBinding boxedType = this.getType(JAVA_LANG_SHORT);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_SHORT, null, 1);
         case 5:
            TypeBinding boxedType = this.getType(JAVA_LANG_BOOLEAN);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_BOOLEAN, null, 1);
         case 6:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         default:
            switch(type.kind()) {
               case 516:
               case 4100:
               case 8196:
                  switch(type.erasure().id) {
                     case 26:
                        return TypeBinding.BYTE;
                     case 27:
                        return TypeBinding.SHORT;
                     case 28:
                        return TypeBinding.CHAR;
                     case 29:
                        return TypeBinding.INT;
                     case 30:
                        return TypeBinding.LONG;
                     case 31:
                        return TypeBinding.FLOAT;
                     case 32:
                        return TypeBinding.DOUBLE;
                     case 33:
                        return TypeBinding.BOOLEAN;
                  }
               default:
                  return type;
               case 32772:
                  return this.computeBoxingType(type.getIntersectingTypes()[0]);
               case 65540:
                  return ((PolyTypeBinding)type).computeBoxingType();
            }
         case 7:
            TypeBinding boxedType = this.getType(JAVA_LANG_LONG);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_LONG, null, 1);
         case 8:
            TypeBinding boxedType = this.getType(JAVA_LANG_DOUBLE);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_DOUBLE, null, 1);
         case 9:
            TypeBinding boxedType = this.getType(JAVA_LANG_FLOAT);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_FLOAT, null, 1);
         case 10:
            TypeBinding boxedType = this.getType(JAVA_LANG_INTEGER);
            if (boxedType != null) {
               return boxedType;
            }

            return new ProblemReferenceBinding(JAVA_LANG_INTEGER, null, 1);
         case 26:
            return TypeBinding.BYTE;
         case 27:
            return TypeBinding.SHORT;
         case 28:
            return TypeBinding.CHAR;
         case 29:
            return TypeBinding.INT;
         case 30:
            return TypeBinding.LONG;
         case 31:
            return TypeBinding.FLOAT;
         case 32:
            return TypeBinding.DOUBLE;
         case 33:
            return TypeBinding.BOOLEAN;
      }
   }

   private PackageBinding computePackageFrom(char[][] constantPoolName, boolean isMissing) {
      if (constantPoolName.length == 1) {
         return this.defaultPackage;
      } else {
         PackageBinding packageBinding = this.getPackage0(constantPoolName[0]);
         if (packageBinding == null || packageBinding == TheNotFoundPackage) {
            packageBinding = new PackageBinding(constantPoolName[0], this);
            if (isMissing) {
               packageBinding.tagBits |= 128L;
            }

            this.knownPackages.put(constantPoolName[0], packageBinding);
         }

         int i = 1;

         for(int length = constantPoolName.length - 1; i < length; ++i) {
            PackageBinding parent = packageBinding;
            if ((packageBinding = packageBinding.getPackage0(constantPoolName[i])) == null || packageBinding == TheNotFoundPackage) {
               packageBinding = new PackageBinding(CharOperation.subarray(constantPoolName, 0, i + 1), parent, this);
               if (isMissing) {
                  packageBinding.tagBits |= 128L;
               }

               parent.addPackage(packageBinding);
            }
         }

         return packageBinding;
      }
   }

   public ReferenceBinding convertToParameterizedType(ReferenceBinding originalType) {
      if (originalType != null) {
         boolean isGeneric = originalType.isGenericType();
         ReferenceBinding originalEnclosingType = originalType.enclosingType();
         ReferenceBinding convertedEnclosingType = originalEnclosingType;
         boolean needToConvert = isGeneric;
         if (originalEnclosingType != null) {
            convertedEnclosingType = originalType.isStatic()
               ? (ReferenceBinding)this.convertToRawType(originalEnclosingType, false)
               : this.convertToParameterizedType(originalEnclosingType);
            needToConvert = isGeneric | TypeBinding.notEquals(originalEnclosingType, convertedEnclosingType);
         }

         if (needToConvert) {
            return this.createParameterizedType(originalType, isGeneric ? originalType.typeVariables() : null, convertedEnclosingType);
         }
      }

      return originalType;
   }

   public TypeBinding convertToRawType(TypeBinding type, boolean forceRawEnclosingType) {
      int dimension;
      TypeBinding originalType;
      switch(type.kind()) {
         case 68:
            dimension = type.dimensions();
            originalType = type.leafComponentType();
            break;
         case 132:
         case 516:
         case 1028:
         case 4100:
         case 8196:
            return type;
         default:
            if (type.id == 1) {
               return type;
            }

            dimension = 0;
            originalType = type;
      }

      boolean needToConvert;
      switch(originalType.kind()) {
         case 132:
            return type;
         case 260:
            ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)originalType;
            needToConvert = paramType.genericType().isGenericType();
            break;
         case 2052:
            needToConvert = true;
            break;
         default:
            needToConvert = false;
      }

      ReferenceBinding originalEnclosing = originalType.enclosingType();
      TypeBinding convertedType;
      if (originalEnclosing == null) {
         convertedType = (TypeBinding)(needToConvert ? this.createRawType((ReferenceBinding)originalType.erasure(), null) : originalType);
      } else {
         ReferenceBinding convertedEnclosing;
         if (originalEnclosing.kind() == 1028) {
            needToConvert |= !((ReferenceBinding)originalType).isStatic();
            convertedEnclosing = originalEnclosing;
         } else if (forceRawEnclosingType && !needToConvert) {
            convertedEnclosing = (ReferenceBinding)this.convertToRawType(originalEnclosing, forceRawEnclosingType);
            needToConvert = TypeBinding.notEquals(originalEnclosing, convertedEnclosing);
         } else if (!needToConvert && !((ReferenceBinding)originalType).isStatic()) {
            convertedEnclosing = this.convertToParameterizedType(originalEnclosing);
         } else {
            convertedEnclosing = (ReferenceBinding)this.convertToRawType(originalEnclosing, false);
         }

         if (needToConvert) {
            convertedType = this.createRawType((ReferenceBinding)originalType.erasure(), convertedEnclosing);
         } else if (TypeBinding.notEquals(originalEnclosing, convertedEnclosing)) {
            convertedType = this.createParameterizedType((ReferenceBinding)originalType.erasure(), null, convertedEnclosing);
         } else {
            convertedType = originalType;
         }
      }

      if (TypeBinding.notEquals(originalType, convertedType)) {
         return (TypeBinding)(dimension > 0 ? this.createArrayType(convertedType, dimension) : convertedType);
      } else {
         return type;
      }
   }

   public ReferenceBinding[] convertToRawTypes(ReferenceBinding[] originalTypes, boolean forceErasure, boolean forceRawEnclosingType) {
      if (originalTypes == null) {
         return null;
      } else {
         ReferenceBinding[] convertedTypes = originalTypes;
         int i = 0;

         for(int length = originalTypes.length; i < length; ++i) {
            ReferenceBinding originalType = originalTypes[i];
            ReferenceBinding convertedType = (ReferenceBinding)this.convertToRawType(
               (TypeBinding)(forceErasure ? originalType.erasure() : originalType), forceRawEnclosingType
            );
            if (TypeBinding.notEquals(convertedType, originalType)) {
               if (convertedTypes == originalTypes) {
                  System.arraycopy(originalTypes, 0, convertedTypes = new ReferenceBinding[length], 0, i);
               }

               convertedTypes[i] = convertedType;
            } else if (convertedTypes != originalTypes) {
               convertedTypes[i] = originalType;
            }
         }

         return convertedTypes;
      }
   }

   public TypeBinding convertUnresolvedBinaryToRawType(TypeBinding type) {
      int dimension;
      TypeBinding originalType;
      switch(type.kind()) {
         case 68:
            dimension = type.dimensions();
            originalType = type.leafComponentType();
            break;
         case 132:
         case 516:
         case 1028:
         case 4100:
         case 8196:
            return type;
         default:
            if (type.id == 1) {
               return type;
            }

            dimension = 0;
            originalType = type;
      }

      boolean needToConvert;
      switch(originalType.kind()) {
         case 132:
            return type;
         case 260:
            ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)originalType;
            needToConvert = paramType.genericType().isGenericType();
            break;
         case 2052:
            needToConvert = true;
            break;
         default:
            needToConvert = false;
      }

      ReferenceBinding originalEnclosing = originalType.enclosingType();
      TypeBinding convertedType;
      if (originalEnclosing == null) {
         convertedType = (TypeBinding)(needToConvert ? this.createRawType((ReferenceBinding)originalType.erasure(), null) : originalType);
      } else {
         ReferenceBinding convertedEnclosing = (ReferenceBinding)this.convertUnresolvedBinaryToRawType(originalEnclosing);
         if (TypeBinding.notEquals(convertedEnclosing, originalEnclosing)) {
            needToConvert |= !((ReferenceBinding)originalType).isStatic();
         }

         if (needToConvert) {
            convertedType = this.createRawType((ReferenceBinding)originalType.erasure(), convertedEnclosing);
         } else if (TypeBinding.notEquals(originalEnclosing, convertedEnclosing)) {
            convertedType = this.createParameterizedType((ReferenceBinding)originalType.erasure(), null, convertedEnclosing);
         } else {
            convertedType = originalType;
         }
      }

      if (TypeBinding.notEquals(originalType, convertedType)) {
         return (TypeBinding)(dimension > 0 ? this.createArrayType(convertedType, dimension) : convertedType);
      } else {
         return type;
      }
   }

   public AnnotationBinding createAnnotation(ReferenceBinding annotationType, ElementValuePair[] pairs) {
      if (pairs.length != 0) {
         AnnotationBinding.setMethodBindings(annotationType, pairs);
         return new AnnotationBinding(annotationType, pairs);
      } else {
         return this.typeSystem.getAnnotationType(annotationType, true);
      }
   }

   public AnnotationBinding createUnresolvedAnnotation(ReferenceBinding annotationType, ElementValuePair[] pairs) {
      return (AnnotationBinding)(pairs.length != 0
         ? new UnresolvedAnnotationBinding(annotationType, pairs, this)
         : this.typeSystem.getAnnotationType(annotationType, false));
   }

   public ArrayBinding createArrayType(TypeBinding leafComponentType, int dimensionCount) {
      return this.typeSystem.getArrayType(leafComponentType, dimensionCount);
   }

   public ArrayBinding createArrayType(TypeBinding leafComponentType, int dimensionCount, AnnotationBinding[] annotations) {
      return this.typeSystem.getArrayType(leafComponentType, dimensionCount, annotations);
   }

   public TypeBinding createIntersectionType18(ReferenceBinding[] intersectingTypes) {
      return this.typeSystem.getIntersectionType18(intersectingTypes);
   }

   public BinaryTypeBinding createBinaryTypeFrom(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
      return this.createBinaryTypeFrom(binaryType, packageBinding, true, accessRestriction);
   }

   public BinaryTypeBinding createBinaryTypeFrom(
      IBinaryType binaryType, PackageBinding packageBinding, boolean needFieldsAndMethods, AccessRestriction accessRestriction
   ) {
      BinaryTypeBinding binaryBinding = new BinaryTypeBinding(packageBinding, binaryType, this);
      ReferenceBinding cachedType = packageBinding.getType0(binaryBinding.compoundName[binaryBinding.compoundName.length - 1]);
      if (cachedType == null || cachedType.isUnresolvedType()) {
         packageBinding.addType(binaryBinding);
         this.setAccessRestriction(binaryBinding, accessRestriction);
         binaryBinding.cachePartsFrom(binaryType, needFieldsAndMethods);
         return binaryBinding;
      } else {
         return cachedType.isBinaryBinding() ? (BinaryTypeBinding)cachedType : null;
      }
   }

   public MissingTypeBinding createMissingType(PackageBinding packageBinding, char[][] compoundName) {
      if (packageBinding == null) {
         packageBinding = this.computePackageFrom(compoundName, true);
         if (packageBinding == TheNotFoundPackage) {
            packageBinding = this.defaultPackage;
         }
      }

      MissingTypeBinding missingType = new MissingTypeBinding(packageBinding, compoundName, this);
      if (missingType.id != 1) {
         ReferenceBinding objectType = this.getType(TypeConstants.JAVA_LANG_OBJECT);
         if (objectType == null) {
            objectType = this.createMissingType(null, TypeConstants.JAVA_LANG_OBJECT);
         }

         missingType.setMissingSuperclass(objectType);
      }

      packageBinding.addType(missingType);
      if (this.missingTypes == null) {
         this.missingTypes = new ArrayList(3);
      }

      this.missingTypes.add(missingType);
      return missingType;
   }

   public PackageBinding createPackage(char[][] compoundName) {
      PackageBinding packageBinding = this.getPackage0(compoundName[0]);
      if (packageBinding == null || packageBinding == TheNotFoundPackage) {
         packageBinding = new PackageBinding(compoundName[0], this);
         this.knownPackages.put(compoundName[0], packageBinding);
      }

      int i = 1;

      for(int length = compoundName.length; i < length; ++i) {
         ReferenceBinding type = packageBinding.getType0(compoundName[i]);
         if (type != null && type != TheNotFoundType && !(type instanceof UnresolvedReferenceBinding)) {
            return null;
         }

         PackageBinding parent = packageBinding;
         if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null || packageBinding == TheNotFoundPackage) {
            if (this.nameEnvironment instanceof INameEnvironmentExtension) {
               if (((INameEnvironmentExtension)this.nameEnvironment).findType(compoundName[i], parent.compoundName, false) != null) {
                  return null;
               }
            } else if (this.nameEnvironment.findType(compoundName[i], parent.compoundName) != null) {
               return null;
            }

            packageBinding = new PackageBinding(CharOperation.subarray(compoundName, 0, i + 1), parent, this);
            parent.addPackage(packageBinding);
         }
      }

      return packageBinding;
   }

   public ParameterizedGenericMethodBinding createParameterizedGenericMethod(MethodBinding genericMethod, RawTypeBinding rawType) {
      ParameterizedGenericMethodBinding[] cachedInfo = (ParameterizedGenericMethodBinding[])this.uniqueParameterizedGenericMethodBindings.get(genericMethod);
      boolean needToGrow = false;
      int index = 0;
      if (cachedInfo != null) {
         for(int max = cachedInfo.length; index < max; ++index) {
            ParameterizedGenericMethodBinding cachedMethod = cachedInfo[index];
            if (cachedMethod == null) {
               break;
            }

            if (cachedMethod.isRaw && cachedMethod.declaringClass == (rawType == null ? genericMethod.declaringClass : rawType)) {
               return cachedMethod;
            }
         }

         needToGrow = true;
      } else {
         cachedInfo = new ParameterizedGenericMethodBinding[5];
         this.uniqueParameterizedGenericMethodBindings.put(genericMethod, cachedInfo);
      }

      int length = cachedInfo.length;
      if (needToGrow && index == length) {
         System.arraycopy(cachedInfo, 0, cachedInfo = new ParameterizedGenericMethodBinding[length * 2], 0, length);
         this.uniqueParameterizedGenericMethodBindings.put(genericMethod, cachedInfo);
      }

      ParameterizedGenericMethodBinding parameterizedGenericMethod = new ParameterizedGenericMethodBinding(genericMethod, rawType, this);
      cachedInfo[index] = parameterizedGenericMethod;
      return parameterizedGenericMethod;
   }

   public ParameterizedGenericMethodBinding createParameterizedGenericMethod(MethodBinding genericMethod, TypeBinding[] typeArguments) {
      return this.createParameterizedGenericMethod(genericMethod, typeArguments, false, false);
   }

   public ParameterizedGenericMethodBinding createParameterizedGenericMethod(
      MethodBinding genericMethod, TypeBinding[] typeArguments, boolean inferredWithUncheckedConversion, boolean hasReturnProblem
   ) {
      ParameterizedGenericMethodBinding[] cachedInfo = (ParameterizedGenericMethodBinding[])this.uniqueParameterizedGenericMethodBindings.get(genericMethod);
      int argLength = typeArguments == null ? 0 : typeArguments.length;
      boolean needToGrow = false;
      int index = 0;
      if (cachedInfo == null) {
         cachedInfo = new ParameterizedGenericMethodBinding[5];
         this.uniqueParameterizedGenericMethodBindings.put(genericMethod, cachedInfo);
      } else {
         int max = cachedInfo.length;

         while(true) {
            if (index < max) {
               ParameterizedGenericMethodBinding cachedMethod = cachedInfo[index];
               if (cachedMethod != null) {
                  if (!cachedMethod.isRaw && cachedMethod.inferredWithUncheckedConversion == inferredWithUncheckedConversion) {
                     TypeBinding[] cachedArguments = cachedMethod.typeArguments;
                     int cachedArgLength = cachedArguments == null ? 0 : cachedArguments.length;
                     if (argLength == cachedArgLength) {
                        int j = 0;

                        label73:
                        while(true) {
                           if (j >= cachedArgLength) {
                              if (!inferredWithUncheckedConversion) {
                                 return cachedMethod;
                              }

                              if (!cachedMethod.returnType.isParameterizedType() && !cachedMethod.returnType.isTypeVariable()) {
                                 for(TypeBinding exc : cachedMethod.thrownExceptions) {
                                    if (exc.isParameterizedType() || exc.isTypeVariable()) {
                                       break label73;
                                    }
                                 }

                                 return cachedMethod;
                              }
                              break;
                           }

                           if (typeArguments[j] != cachedArguments[j]) {
                              break;
                           }

                           ++j;
                        }
                     }
                  }

                  ++index;
                  continue;
               }
            }

            needToGrow = true;
            break;
         }
      }

      int length = cachedInfo.length;
      if (needToGrow && index == length) {
         System.arraycopy(cachedInfo, 0, cachedInfo = new ParameterizedGenericMethodBinding[length * 2], 0, length);
         this.uniqueParameterizedGenericMethodBindings.put(genericMethod, cachedInfo);
      }

      ParameterizedGenericMethodBinding parameterizedGenericMethod = new ParameterizedGenericMethodBinding(
         genericMethod, typeArguments, this, inferredWithUncheckedConversion, hasReturnProblem
      );
      cachedInfo[index] = parameterizedGenericMethod;
      return parameterizedGenericMethod;
   }

   public PolymorphicMethodBinding createPolymorphicMethod(MethodBinding originalPolymorphicMethod, TypeBinding[] parameters) {
      String key = new String(originalPolymorphicMethod.selector);
      PolymorphicMethodBinding[] cachedInfo = (PolymorphicMethodBinding[])this.uniquePolymorphicMethodBindings.get(key);
      int parametersLength = parameters == null ? 0 : parameters.length;
      TypeBinding[] parametersTypeBinding = new TypeBinding[parametersLength];

      for(int i = 0; i < parametersLength; ++i) {
         TypeBinding parameterTypeBinding = parameters[i];
         if (parameterTypeBinding.id == 12) {
            parametersTypeBinding[i] = this.getType(JAVA_LANG_VOID);
         } else {
            parametersTypeBinding[i] = parameterTypeBinding.erasure();
         }
      }

      boolean needToGrow = false;
      int index = 0;
      if (cachedInfo != null) {
         for(int max = cachedInfo.length; index < max; ++index) {
            PolymorphicMethodBinding cachedMethod = cachedInfo[index];
            if (cachedMethod == null) {
               break;
            }

            if (cachedMethod.matches(parametersTypeBinding, originalPolymorphicMethod.returnType)) {
               return cachedMethod;
            }
         }

         needToGrow = true;
      } else {
         cachedInfo = new PolymorphicMethodBinding[5];
         this.uniquePolymorphicMethodBindings.put(key, cachedInfo);
      }

      int length = cachedInfo.length;
      if (needToGrow && index == length) {
         System.arraycopy(cachedInfo, 0, cachedInfo = new PolymorphicMethodBinding[length * 2], 0, length);
         this.uniquePolymorphicMethodBindings.put(key, cachedInfo);
      }

      PolymorphicMethodBinding polymorphicMethod = new PolymorphicMethodBinding(originalPolymorphicMethod, parametersTypeBinding);
      cachedInfo[index] = polymorphicMethod;
      return polymorphicMethod;
   }

   public boolean usesAnnotatedTypeSystem() {
      return this.typeSystem.isAnnotatedTypeSystem();
   }

   public MethodBinding updatePolymorphicMethodReturnType(PolymorphicMethodBinding binding, TypeBinding typeBinding) {
      String key = new String(binding.selector);
      PolymorphicMethodBinding[] cachedInfo = (PolymorphicMethodBinding[])this.uniquePolymorphicMethodBindings.get(key);
      boolean needToGrow = false;
      int index = 0;
      TypeBinding[] parameters = binding.parameters;
      if (cachedInfo != null) {
         for(int max = cachedInfo.length; index < max; ++index) {
            PolymorphicMethodBinding cachedMethod = cachedInfo[index];
            if (cachedMethod == null) {
               break;
            }

            if (cachedMethod.matches(parameters, typeBinding)) {
               return cachedMethod;
            }
         }

         needToGrow = true;
      } else {
         cachedInfo = new PolymorphicMethodBinding[5];
         this.uniquePolymorphicMethodBindings.put(key, cachedInfo);
      }

      int length = cachedInfo.length;
      if (needToGrow && index == length) {
         System.arraycopy(cachedInfo, 0, cachedInfo = new PolymorphicMethodBinding[length * 2], 0, length);
         this.uniquePolymorphicMethodBindings.put(key, cachedInfo);
      }

      PolymorphicMethodBinding polymorphicMethod = new PolymorphicMethodBinding(binding.original(), typeBinding, parameters);
      cachedInfo[index] = polymorphicMethod;
      return polymorphicMethod;
   }

   public ParameterizedMethodBinding createGetClassMethod(TypeBinding receiverType, MethodBinding originalMethod, Scope scope) {
      ParameterizedMethodBinding retVal = null;
      if (this.uniqueGetClassMethodBinding == null) {
         this.uniqueGetClassMethodBinding = new SimpleLookupTable(3);
      } else {
         retVal = (ParameterizedMethodBinding)this.uniqueGetClassMethodBinding.get(receiverType);
      }

      if (retVal == null) {
         retVal = ParameterizedMethodBinding.instantiateGetClass(receiverType, originalMethod, scope);
         this.uniqueGetClassMethodBinding.put(receiverType, retVal);
      }

      return retVal;
   }

   public ReferenceBinding createMemberType(ReferenceBinding memberType, ReferenceBinding enclosingType) {
      return this.typeSystem.getMemberType(memberType, enclosingType);
   }

   public ParameterizedTypeBinding createParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType) {
      AnnotationBinding[] annotations = genericType.typeAnnotations;
      return annotations != Binding.NO_ANNOTATIONS
         ? this.typeSystem.getParameterizedType((ReferenceBinding)genericType.unannotated(), typeArguments, enclosingType, annotations)
         : this.typeSystem.getParameterizedType(genericType, typeArguments, enclosingType);
   }

   public ParameterizedTypeBinding createParameterizedType(
      ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding[] annotations
   ) {
      return this.typeSystem.getParameterizedType(genericType, typeArguments, enclosingType, annotations);
   }

   public TypeBinding createAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations) {
      return this.typeSystem.getAnnotatedType(type, annotations);
   }

   public TypeBinding createAnnotatedType(TypeBinding type, AnnotationBinding[] newbies) {
      int newLength = newbies == null ? 0 : newbies.length;
      if (type != null && newLength != 0) {
         AnnotationBinding[] oldies = type.getTypeAnnotations();
         int oldLength = oldies == null ? 0 : oldies.length;
         if (oldLength > 0) {
            System.arraycopy(newbies, 0, newbies = new AnnotationBinding[newLength + oldLength], 0, newLength);
            System.arraycopy(oldies, 0, newbies, newLength, oldLength);
         }

         if (this.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
            long tagBitsSeen = 0L;
            AnnotationBinding[] filtered = new AnnotationBinding[newbies.length];
            int count = 0;

            for(int i = 0; i < newbies.length; ++i) {
               if (newbies[i] == null) {
                  filtered[count++] = null;
               } else {
                  long tagBits = 0L;
                  if (newbies[i].type.hasNullBit(32)) {
                     tagBits = 72057594037927936L;
                  } else if (newbies[i].type.hasNullBit(64)) {
                     tagBits = 36028797018963968L;
                  }

                  if ((tagBitsSeen & tagBits) == 0L) {
                     tagBitsSeen |= tagBits;
                     filtered[count++] = newbies[i];
                  }
               }
            }

            if (count < newbies.length) {
               System.arraycopy(filtered, 0, newbies = new AnnotationBinding[count], 0, count);
            }
         }

         return this.typeSystem.getAnnotatedType(type, new AnnotationBinding[][]{newbies});
      } else {
         return type;
      }
   }

   public RawTypeBinding createRawType(ReferenceBinding genericType, ReferenceBinding enclosingType) {
      AnnotationBinding[] annotations = genericType.typeAnnotations;
      return annotations != Binding.NO_ANNOTATIONS
         ? this.typeSystem.getRawType((ReferenceBinding)genericType.unannotated(), enclosingType, annotations)
         : this.typeSystem.getRawType(genericType, enclosingType);
   }

   public RawTypeBinding createRawType(ReferenceBinding genericType, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
      return this.typeSystem.getRawType(genericType, enclosingType, annotations);
   }

   public WildcardBinding createWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind) {
      if (genericType != null) {
         AnnotationBinding[] annotations = genericType.typeAnnotations;
         if (annotations != Binding.NO_ANNOTATIONS) {
            return this.typeSystem.getWildcard((ReferenceBinding)genericType.unannotated(), rank, bound, otherBounds, boundKind, annotations);
         }
      }

      return this.typeSystem.getWildcard(genericType, rank, bound, otherBounds, boundKind);
   }

   public CaptureBinding createCapturedWildcard(WildcardBinding wildcard, ReferenceBinding contextType, int start, int end, ASTNode cud, int id) {
      return this.typeSystem.getCapturedWildcard(wildcard, contextType, start, end, cud, id);
   }

   public WildcardBinding createWildcard(
      ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, AnnotationBinding[] annotations
   ) {
      return this.typeSystem.getWildcard(genericType, rank, bound, otherBounds, boundKind, annotations);
   }

   public AccessRestriction getAccessRestriction(TypeBinding type) {
      return (AccessRestriction)this.accessRestrictions.get(type);
   }

   public ReferenceBinding getCachedType(char[][] compoundName) {
      if (compoundName.length == 1) {
         return this.defaultPackage.getType0(compoundName[0]);
      } else {
         PackageBinding packageBinding = this.getPackage0(compoundName[0]);
         if (packageBinding != null && packageBinding != TheNotFoundPackage) {
            int i = 1;

            for(int packageLength = compoundName.length - 1; i < packageLength; ++i) {
               if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null || packageBinding == TheNotFoundPackage) {
                  return null;
               }
            }

            return packageBinding.getType0(compoundName[compoundName.length - 1]);
         } else {
            return null;
         }
      }
   }

   public AnnotationBinding getNullableAnnotation() {
      if (this.nullableAnnotation != null) {
         return this.nullableAnnotation;
      } else {
         ReferenceBinding nullable = this.getResolvedType(this.globalOptions.nullableAnnotationName, null);
         return this.nullableAnnotation = this.typeSystem.getAnnotationType(nullable, true);
      }
   }

   public char[][] getNullableAnnotationName() {
      return this.globalOptions.nullableAnnotationName;
   }

   public AnnotationBinding getNonNullAnnotation() {
      if (this.nonNullAnnotation != null) {
         return this.nonNullAnnotation;
      } else {
         ReferenceBinding nonNull = this.getResolvedType(this.globalOptions.nonNullAnnotationName, null);
         return this.nonNullAnnotation = this.typeSystem.getAnnotationType(nonNull, true);
      }
   }

   public AnnotationBinding[] nullAnnotationsFromTagBits(long nullTagBits) {
      if (nullTagBits == 72057594037927936L) {
         return new AnnotationBinding[]{this.getNonNullAnnotation()};
      } else {
         return nullTagBits == 36028797018963968L ? new AnnotationBinding[]{this.getNullableAnnotation()} : null;
      }
   }

   public char[][] getNonNullAnnotationName() {
      return this.globalOptions.nonNullAnnotationName;
   }

   public char[][] getNonNullByDefaultAnnotationName() {
      return this.globalOptions.nonNullByDefaultAnnotationName;
   }

   int getNullAnnotationBit(char[][] qualifiedTypeName) {
      if (this.allNullAnnotations == null) {
         this.allNullAnnotations = new HashMap<>();
         this.allNullAnnotations.put(CharOperation.toString(this.globalOptions.nonNullAnnotationName), 32);
         this.allNullAnnotations.put(CharOperation.toString(this.globalOptions.nullableAnnotationName), 64);
         this.allNullAnnotations.put(CharOperation.toString(this.globalOptions.nonNullByDefaultAnnotationName), 128);

         for(String name : this.globalOptions.nullableAnnotationSecondaryNames) {
            this.allNullAnnotations.put(name, 64);
         }

         for(String name : this.globalOptions.nonNullAnnotationSecondaryNames) {
            this.allNullAnnotations.put(name, 32);
         }

         for(String name : this.globalOptions.nonNullByDefaultAnnotationSecondaryNames) {
            this.allNullAnnotations.put(name, 128);
         }
      }

      String qualifiedTypeString = CharOperation.toString(qualifiedTypeName);
      Integer typeBit = this.allNullAnnotations.get(qualifiedTypeString);
      return typeBit == null ? 0 : typeBit;
   }

   public boolean isNullnessAnnotationPackage(PackageBinding pkg) {
      return this.nonnullAnnotationPackage == pkg || this.nullableAnnotationPackage == pkg || this.nonnullByDefaultAnnotationPackage == pkg;
   }

   public boolean usesNullTypeAnnotations() {
      if (this.globalOptions.useNullTypeAnnotations != null) {
         return this.globalOptions.useNullTypeAnnotations;
      } else {
         this.initializeUsesNullTypeAnnotation();

         for(MethodBinding enumMethod : this.deferredEnumMethods) {
            int purpose = 0;
            if (CharOperation.equals(enumMethod.selector, TypeConstants.VALUEOF)) {
               purpose = 10;
            } else if (CharOperation.equals(enumMethod.selector, TypeConstants.VALUES)) {
               purpose = 9;
            }

            if (purpose != 0) {
               SyntheticMethodBinding.markNonNull(enumMethod, purpose, this);
            }
         }

         this.deferredEnumMethods.clear();
         return this.globalOptions.useNullTypeAnnotations;
      }
   }

   private void initializeUsesNullTypeAnnotation() {
      this.globalOptions.useNullTypeAnnotations = Boolean.FALSE;
      if (this.globalOptions.isAnnotationBasedNullAnalysisEnabled && this.globalOptions.originalSourceLevel >= 3407872L) {
         ReferenceBinding nullable = this.nullableAnnotation != null
            ? this.nullableAnnotation.getAnnotationType()
            : this.getType(this.getNullableAnnotationName());
         ReferenceBinding nonNull = this.nonNullAnnotation != null
            ? this.nonNullAnnotation.getAnnotationType()
            : this.getType(this.getNonNullAnnotationName());
         if (nullable != null || nonNull != null) {
            if (nullable != null && nonNull != null) {
               long nullableMetaBits = nullable.getAnnotationTagBits() & 9007199254740992L;
               long nonNullMetaBits = nonNull.getAnnotationTagBits() & 9007199254740992L;
               if (nullableMetaBits == nonNullMetaBits) {
                  if (nullableMetaBits != 0L) {
                     this.globalOptions.useNullTypeAnnotations = Boolean.TRUE;
                  }
               }
            }
         }
      }
   }

   PackageBinding getPackage0(char[] name) {
      return this.knownPackages.get(name);
   }

   public ReferenceBinding getResolvedType(char[][] compoundName, Scope scope) {
      ReferenceBinding type = this.getType(compoundName);
      if (type != null) {
         return type;
      } else {
         this.problemReporter
            .isClassPathCorrect(compoundName, scope == null ? this.unitBeingCompleted : scope.referenceCompilationUnit(), this.missingClassFileLocation);
         return this.createMissingType(null, compoundName);
      }
   }

   PackageBinding getTopLevelPackage(char[] name) {
      PackageBinding packageBinding = this.getPackage0(name);
      if (packageBinding != null) {
         return packageBinding == TheNotFoundPackage ? null : packageBinding;
      } else if (this.nameEnvironment.isPackage(null, name)) {
         this.knownPackages.put(name, packageBinding = new PackageBinding(name, this));
         return packageBinding;
      } else {
         this.knownPackages.put(name, TheNotFoundPackage);
         return null;
      }
   }

   public ReferenceBinding getType(char[][] compoundName) {
      ReferenceBinding referenceBinding;
      if (compoundName.length == 1) {
         if ((referenceBinding = this.defaultPackage.getType0(compoundName[0])) == null) {
            PackageBinding packageBinding = this.getPackage0(compoundName[0]);
            if (packageBinding != null && packageBinding != TheNotFoundPackage) {
               return null;
            }

            referenceBinding = this.askForType(this.defaultPackage, compoundName[0]);
         }
      } else {
         PackageBinding packageBinding = this.getPackage0(compoundName[0]);
         if (packageBinding == TheNotFoundPackage) {
            return null;
         }

         if (packageBinding != null) {
            int i = 1;

            for(int packageLength = compoundName.length - 1; i < packageLength && (packageBinding = packageBinding.getPackage0(compoundName[i])) != null; ++i) {
               if (packageBinding == TheNotFoundPackage) {
                  return null;
               }
            }
         }

         if (packageBinding == null) {
            referenceBinding = this.askForType(compoundName);
         } else if ((referenceBinding = packageBinding.getType0(compoundName[compoundName.length - 1])) == null) {
            referenceBinding = this.askForType(packageBinding, compoundName[compoundName.length - 1]);
         }
      }

      if (referenceBinding != null && referenceBinding != TheNotFoundType) {
         referenceBinding = (ReferenceBinding)BinaryTypeBinding.resolveType(referenceBinding, this, false);
         return (ReferenceBinding)(referenceBinding.isNestedType() ? new ProblemReferenceBinding(compoundName, referenceBinding, 4) : referenceBinding);
      } else {
         return null;
      }
   }

   private TypeBinding[] getTypeArgumentsFromSignature(
      SignatureWrapper wrapper,
      TypeVariableBinding[] staticVariables,
      ReferenceBinding enclosingType,
      ReferenceBinding genericType,
      char[][][] missingTypeNames,
      ITypeAnnotationWalker walker
   ) {
      ArrayList args = new ArrayList(2);
      int rank = 0;

      do {
         args.add(
            this.getTypeFromVariantTypeSignature(wrapper, staticVariables, enclosingType, genericType, rank, missingTypeNames, walker.toTypeArgument(rank++))
         );
      } while(wrapper.signature[wrapper.start] != '>');

      ++wrapper.start;
      TypeBinding[] typeArguments = new TypeBinding[args.size()];
      args.toArray(typeArguments);
      return typeArguments;
   }

   private ReferenceBinding getTypeFromCompoundName(char[][] compoundName, boolean isParameterized, boolean wasMissingType) {
      ReferenceBinding binding = this.getCachedType(compoundName);
      if (binding == null) {
         PackageBinding packageBinding = this.computePackageFrom(compoundName, false);
         binding = new UnresolvedReferenceBinding(compoundName, packageBinding);
         if (wasMissingType) {
            binding.tagBits |= 128L;
         }

         packageBinding.addType(binding);
      } else if (binding == TheNotFoundType) {
         if (!wasMissingType) {
            this.problemReporter.isClassPathCorrect(compoundName, this.unitBeingCompleted, this.missingClassFileLocation);
         }

         binding = this.createMissingType(null, compoundName);
      } else if (!isParameterized) {
         binding = (ReferenceBinding)this.convertUnresolvedBinaryToRawType(binding);
      }

      return binding;
   }

   ReferenceBinding getTypeFromConstantPoolName(
      char[] signature, int start, int end, boolean isParameterized, char[][][] missingTypeNames, ITypeAnnotationWalker walker
   ) {
      if (end == -1) {
         end = signature.length;
      }

      char[][] compoundName = CharOperation.splitOn('/', signature, start, end);
      boolean wasMissingType = false;
      if (missingTypeNames != null) {
         int i = 0;

         for(int max = missingTypeNames.length; i < max; ++i) {
            if (CharOperation.equals(compoundName, missingTypeNames[i])) {
               wasMissingType = true;
               break;
            }
         }
      }

      ReferenceBinding binding = this.getTypeFromCompoundName(compoundName, isParameterized, wasMissingType);
      if (walker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER) {
         binding = (ReferenceBinding)this.annotateType(binding, walker, missingTypeNames);
      }

      return binding;
   }

   ReferenceBinding getTypeFromConstantPoolName(char[] signature, int start, int end, boolean isParameterized, char[][][] missingTypeNames) {
      return this.getTypeFromConstantPoolName(signature, start, end, isParameterized, missingTypeNames, ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
   }

   TypeBinding getTypeFromSignature(
      char[] signature, int start, int end, boolean isParameterized, TypeBinding enclosingType, char[][][] missingTypeNames, ITypeAnnotationWalker walker
   ) {
      int dimension;
      for(dimension = 0; signature[start] == '['; ++dimension) {
         ++start;
      }

      AnnotationBinding[][] annotationsOnDimensions = null;
      if (dimension > 0 && walker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER) {
         for(int i = 0; i < dimension; ++i) {
            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(0), this, missingTypeNames);
            if (annotations != Binding.NO_ANNOTATIONS) {
               if (annotationsOnDimensions == null) {
                  annotationsOnDimensions = new AnnotationBinding[dimension][];
               }

               annotationsOnDimensions[i] = annotations;
            }

            walker = walker.toNextArrayDimension();
         }
      }

      if (end == -1) {
         end = signature.length - 1;
      }

      TypeBinding binding = null;
      if (start == end) {
         switch(signature[start]) {
            case 'B':
               binding = TypeBinding.BYTE;
               break;
            case 'C':
               binding = TypeBinding.CHAR;
               break;
            case 'D':
               binding = TypeBinding.DOUBLE;
               break;
            case 'F':
               binding = TypeBinding.FLOAT;
               break;
            case 'I':
               binding = TypeBinding.INT;
               break;
            case 'J':
               binding = TypeBinding.LONG;
               break;
            case 'S':
               binding = TypeBinding.SHORT;
               break;
            case 'V':
               binding = TypeBinding.VOID;
               break;
            case 'Z':
               binding = TypeBinding.BOOLEAN;
               break;
            default:
               this.problemReporter.corruptedSignature(enclosingType, signature, start);
         }
      } else {
         binding = this.getTypeFromConstantPoolName(signature, start + 1, end, isParameterized, missingTypeNames);
      }

      if (isParameterized) {
         if (dimension != 0) {
            throw new IllegalStateException();
         } else {
            return binding;
         }
      } else {
         if (walker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER) {
            binding = this.annotateType(binding, walker, missingTypeNames);
         }

         if (dimension != 0) {
            binding = this.typeSystem.getArrayType(binding, dimension, AnnotatableTypeSystem.flattenedAnnotations(annotationsOnDimensions));
         }

         return binding;
      }
   }

   private TypeBinding annotateType(TypeBinding binding, ITypeAnnotationWalker walker, char[][][] missingTypeNames) {
      int depth = binding.depth() + 1;
      if (depth > 1) {
         if (binding.isUnresolvedType()) {
            binding = ((UnresolvedReferenceBinding)binding).resolve(this, true);
         }

         TypeBinding currentBinding = binding;

         for(depth = 0; currentBinding != null; currentBinding = currentBinding.enclosingType()) {
            ++depth;
            if (currentBinding.isStatic()) {
               break;
            }
         }
      }

      AnnotationBinding[][] annotations = null;

      for(int i = 0; i < depth; ++i) {
         AnnotationBinding[] annots = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(binding.id), this, missingTypeNames);
         if (annots != null && annots.length > 0) {
            if (annotations == null) {
               annotations = new AnnotationBinding[depth][];
            }

            annotations[i] = annots;
         }

         walker = walker.toNextNestedType();
      }

      if (annotations != null) {
         binding = this.createAnnotatedType(binding, annotations);
      }

      return binding;
   }

   boolean qualifiedNameMatchesSignature(char[][] name, char[] signature) {
      int s = 1;

      for(int i = 0; i < name.length; ++i) {
         char[] n = name[i];

         for(int j = 0; j < n.length; ++j) {
            if (n[j] != signature[s++]) {
               return false;
            }
         }

         if (signature[s] == ';' && i == name.length - 1) {
            return true;
         }

         if (signature[s++] != '/') {
            return false;
         }
      }

      return false;
   }

   public TypeBinding getTypeFromTypeSignature(
      SignatureWrapper wrapper,
      TypeVariableBinding[] staticVariables,
      ReferenceBinding enclosingType,
      char[][][] missingTypeNames,
      ITypeAnnotationWalker walker
   ) {
      int dimension;
      for(dimension = 0; wrapper.signature[wrapper.start] == '['; ++dimension) {
         ++wrapper.start;
      }

      AnnotationBinding[][] annotationsOnDimensions = null;
      if (dimension > 0 && walker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER) {
         for(int i = 0; i < dimension; ++i) {
            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(0), this, missingTypeNames);
            if (annotations != Binding.NO_ANNOTATIONS) {
               if (annotationsOnDimensions == null) {
                  annotationsOnDimensions = new AnnotationBinding[dimension][];
               }

               annotationsOnDimensions[i] = annotations;
            }

            walker = walker.toNextArrayDimension();
         }
      }

      if (wrapper.signature[wrapper.start] != 'T') {
         boolean isParameterized;
         TypeBinding type = this.getTypeFromSignature(
            wrapper.signature, wrapper.start, wrapper.computeEnd(), isParameterized = wrapper.end == wrapper.bracket, enclosingType, missingTypeNames, walker
         );
         if (!isParameterized) {
            return (TypeBinding)(dimension == 0
               ? type
               : this.createArrayType(type, dimension, AnnotatableTypeSystem.flattenedAnnotations(annotationsOnDimensions)));
         } else {
            ReferenceBinding actualType = (ReferenceBinding)type;
            if (actualType instanceof UnresolvedReferenceBinding
               && CharOperation.indexOf('$', actualType.compoundName[actualType.compoundName.length - 1]) > 0) {
               actualType = (ReferenceBinding)BinaryTypeBinding.resolveType(actualType, this, false);
            }

            ReferenceBinding actualEnclosing = actualType.enclosingType();
            if (actualEnclosing != null) {
               actualEnclosing = (ReferenceBinding)this.convertToRawType(actualEnclosing, false);
            }

            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(actualType.id), this, missingTypeNames);
            TypeBinding[] typeArguments = this.getTypeArgumentsFromSignature(wrapper, staticVariables, enclosingType, actualType, missingTypeNames, walker);

            ParameterizedTypeBinding parameterizedType;
            ReferenceBinding memberType;
            for(parameterizedType = this.createParameterizedType(actualType, typeArguments, actualEnclosing, annotations);
               wrapper.signature[wrapper.start] == '.';
               parameterizedType = this.createParameterizedType(memberType, typeArguments, parameterizedType, annotations)
            ) {
               ++wrapper.start;
               int memberStart = wrapper.start;
               char[] memberName = wrapper.nextWord();
               BinaryTypeBinding.resolveType(parameterizedType, this, false);
               memberType = parameterizedType.genericType().getMemberType(memberName);
               if (memberType == null) {
                  this.problemReporter.corruptedSignature(parameterizedType, wrapper.signature, memberStart);
               }

               walker = walker.toNextNestedType();
               annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(memberType.id), this, missingTypeNames);
               if (wrapper.signature[wrapper.start] == '<') {
                  ++wrapper.start;
                  typeArguments = this.getTypeArgumentsFromSignature(wrapper, staticVariables, enclosingType, memberType, missingTypeNames, walker);
               } else {
                  typeArguments = null;
               }
            }

            ++wrapper.start;
            return (TypeBinding)(dimension == 0
               ? parameterizedType
               : this.createArrayType(parameterizedType, dimension, AnnotatableTypeSystem.flattenedAnnotations(annotationsOnDimensions)));
         }
      } else {
         int varStart = wrapper.start + 1;
         int varEnd = wrapper.computeEnd();
         int i = staticVariables.length;

         while(--i >= 0) {
            if (CharOperation.equals(staticVariables[i].sourceName, wrapper.signature, varStart, varEnd)) {
               return this.getTypeFromTypeVariable(staticVariables[i], dimension, annotationsOnDimensions, walker, missingTypeNames);
            }
         }

         ReferenceBinding initialType = enclosingType;

         do {
            TypeVariableBinding[] enclosingTypeVariables;
            if (enclosingType instanceof BinaryTypeBinding) {
               enclosingTypeVariables = ((BinaryTypeBinding)enclosingType).typeVariables;
            } else {
               enclosingTypeVariables = enclosingType.typeVariables();
            }

            int ix = enclosingTypeVariables.length;

            while(--ix >= 0) {
               if (CharOperation.equals(enclosingTypeVariables[ix].sourceName, wrapper.signature, varStart, varEnd)) {
                  return this.getTypeFromTypeVariable(enclosingTypeVariables[ix], dimension, annotationsOnDimensions, walker, missingTypeNames);
               }
            }
         } while((enclosingType = enclosingType.enclosingType()) != null);

         this.problemReporter.undefinedTypeVariableSignature(CharOperation.subarray(wrapper.signature, varStart, varEnd), initialType);
         return null;
      }
   }

   private TypeBinding getTypeFromTypeVariable(
      TypeVariableBinding typeVariableBinding,
      int dimension,
      AnnotationBinding[][] annotationsOnDimensions,
      ITypeAnnotationWalker walker,
      char[][][] missingTypeNames
   ) {
      AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(-1), this, missingTypeNames);
      if (annotations != null && annotations != Binding.NO_ANNOTATIONS) {
         typeVariableBinding = (TypeVariableBinding)this.createAnnotatedType(typeVariableBinding, new AnnotationBinding[][]{annotations});
      }

      return (TypeBinding)(dimension == 0
         ? typeVariableBinding
         : this.typeSystem.getArrayType(typeVariableBinding, dimension, AnnotatableTypeSystem.flattenedAnnotations(annotationsOnDimensions)));
   }

   TypeBinding getTypeFromVariantTypeSignature(
      SignatureWrapper wrapper,
      TypeVariableBinding[] staticVariables,
      ReferenceBinding enclosingType,
      ReferenceBinding genericType,
      int rank,
      char[][][] missingTypeNames,
      ITypeAnnotationWalker walker
   ) {
      switch(wrapper.signature[wrapper.start]) {
         case '*': {
            ++wrapper.start;
            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(-1), this, missingTypeNames);
            return this.typeSystem.getWildcard(genericType, rank, null, null, 0, annotations);
         }
         case '+': {
            ++wrapper.start;
            TypeBinding bound = this.getTypeFromTypeSignature(wrapper, staticVariables, enclosingType, missingTypeNames, walker.toWildcardBound());
            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(-1), this, missingTypeNames);
            return this.typeSystem.getWildcard(genericType, rank, bound, null, 1, annotations);
         }
         case ',':
         default:
            return this.getTypeFromTypeSignature(wrapper, staticVariables, enclosingType, missingTypeNames, walker);
         case '-': {
            ++wrapper.start;
            TypeBinding bound = this.getTypeFromTypeSignature(wrapper, staticVariables, enclosingType, missingTypeNames, walker.toWildcardBound());
            AnnotationBinding[] annotations = BinaryTypeBinding.createAnnotations(walker.getAnnotationsAtCursor(-1), this, missingTypeNames);
            return this.typeSystem.getWildcard(genericType, rank, bound, null, 2, annotations);
         }
      }
   }

   boolean isMissingType(char[] typeName) {
      int i = this.missingTypes == null ? 0 : this.missingTypes.size();

      while(--i >= 0) {
         MissingTypeBinding missingType = (MissingTypeBinding)this.missingTypes.get(i);
         if (CharOperation.equals(missingType.sourceName, typeName)) {
            return true;
         }
      }

      return false;
   }

   boolean isPackage(char[][] compoundName, char[] name) {
      return compoundName != null && compoundName.length != 0
         ? this.nameEnvironment.isPackage(compoundName, name)
         : this.nameEnvironment.isPackage(null, name);
   }

   public MethodVerifier methodVerifier() {
      if (this.verifier == null) {
         this.verifier = this.newMethodVerifier();
      }

      return this.verifier;
   }

   public MethodVerifier newMethodVerifier() {
      return new MethodVerifier15(this);
   }

   public void releaseClassFiles(ClassFile[] classFiles) {
      int i = 0;

      for(int fileCount = classFiles.length; i < fileCount; ++i) {
         this.classFilePool.release(classFiles[i]);
      }
   }

   public void reset() {
      this.defaultPackage = new PackageBinding(this);
      this.defaultImports = null;
      this.knownPackages = new HashtableOfPackage();
      this.accessRestrictions = new HashMap(3);
      this.verifier = null;
      this.uniqueParameterizedGenericMethodBindings = new SimpleLookupTable(3);
      this.uniquePolymorphicMethodBindings = new SimpleLookupTable(3);
      this.uniqueGetClassMethodBinding = null;
      this.missingTypes = null;
      this.typesBeingConnected = new HashSet<>();
      int i = this.units.length;

      while(--i >= 0) {
         this.units[i] = null;
      }

      this.lastUnitIndex = -1;
      this.lastCompletedUnitIndex = -1;
      this.unitBeingCompleted = null;
      this.classFilePool.reset();
      this.typeSystem.reset();
   }

   public void setAccessRestriction(ReferenceBinding type, AccessRestriction accessRestriction) {
      if (accessRestriction != null) {
         type.modifiers |= 262144;
         this.accessRestrictions.put(type, accessRestriction);
      }
   }

   void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
      this.typeSystem.updateCaches(unresolvedType, resolvedType);
   }

   public void addResolutionListener(IQualifiedTypeResolutionListener resolutionListener) {
      int length = this.resolutionListeners.length;

      for(int i = 0; i < length; ++i) {
         if (this.resolutionListeners[i].equals(resolutionListener)) {
            return;
         }
      }

      System.arraycopy(this.resolutionListeners, 0, this.resolutionListeners = new IQualifiedTypeResolutionListener[length + 1], 0, length);
      this.resolutionListeners[length] = resolutionListener;
   }

   public TypeBinding getUnannotatedType(TypeBinding typeBinding) {
      return this.typeSystem.getUnannotatedType(typeBinding);
   }

   public TypeBinding[] getAnnotatedTypes(TypeBinding type) {
      return this.typeSystem.getAnnotatedTypes(type);
   }

   public AnnotationBinding[] filterNullTypeAnnotations(AnnotationBinding[] typeAnnotations) {
      if (typeAnnotations.length == 0) {
         return typeAnnotations;
      } else {
         AnnotationBinding[] filtered = new AnnotationBinding[typeAnnotations.length];
         int count = 0;

         for(int i = 0; i < typeAnnotations.length; ++i) {
            AnnotationBinding typeAnnotation = typeAnnotations[i];
            if (typeAnnotation == null) {
               ++count;
            } else if (!typeAnnotation.type.hasNullBit(96)) {
               filtered[count++] = typeAnnotation;
            }
         }

         if (count == 0) {
            return Binding.NO_ANNOTATIONS;
         } else if (count == typeAnnotations.length) {
            return typeAnnotations;
         } else {
            AnnotationBinding[] var6;
            System.arraycopy(filtered, 0, var6 = new AnnotationBinding[count], 0, count);
            return var6;
         }
      }
   }

   public boolean containsNullTypeAnnotation(IBinaryAnnotation[] typeAnnotations) {
      if (typeAnnotations.length == 0) {
         return false;
      } else {
         for(int i = 0; i < typeAnnotations.length; ++i) {
            IBinaryAnnotation typeAnnotation = typeAnnotations[i];
            char[] typeName = typeAnnotation.getTypeName();
            if (typeName != null && typeName.length >= 3 && typeName[0] == 'L') {
               char[][] name = CharOperation.splitOn('/', typeName, 1, typeName.length - 1);
               if (this.getNullAnnotationBit(name) != 0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean containsNullTypeAnnotation(AnnotationBinding[] typeAnnotations) {
      if (typeAnnotations.length == 0) {
         return false;
      } else {
         for(int i = 0; i < typeAnnotations.length; ++i) {
            AnnotationBinding typeAnnotation = typeAnnotations[i];
            if (typeAnnotation.type.hasNullBit(96)) {
               return true;
            }
         }

         return false;
      }
   }
}
