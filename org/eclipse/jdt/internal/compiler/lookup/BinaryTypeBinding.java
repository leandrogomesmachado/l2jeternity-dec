package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.eclipse.jdt.internal.compiler.classfmt.NonNullDefaultAwareTypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.classfmt.TypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class BinaryTypeBinding extends ReferenceBinding {
   private static final IBinaryMethod[] NO_BINARY_METHODS = new IBinaryMethod[0];
   protected ReferenceBinding superclass;
   protected ReferenceBinding enclosingType;
   protected ReferenceBinding[] superInterfaces;
   protected FieldBinding[] fields;
   protected MethodBinding[] methods;
   protected ReferenceBinding[] memberTypes;
   protected TypeVariableBinding[] typeVariables;
   private BinaryTypeBinding prototype;
   protected LookupEnvironment environment;
   protected SimpleLookupTable storedAnnotations = null;
   private ReferenceBinding containerAnnotationType;
   int defaultNullness = 0;
   public BinaryTypeBinding.ExternalAnnotationStatus externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.NOT_EEA_CONFIGURED;

   static Object convertMemberValue(Object binaryValue, LookupEnvironment env, char[][][] missingTypeNames, boolean resolveEnumConstants) {
      if (binaryValue == null) {
         return null;
      } else if (binaryValue instanceof Constant) {
         return binaryValue;
      } else if (binaryValue instanceof ClassSignature) {
         return env.getTypeFromSignature(
            ((ClassSignature)binaryValue).getTypeName(), 0, -1, false, null, missingTypeNames, ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
         );
      } else if (binaryValue instanceof IBinaryAnnotation) {
         return createAnnotation((IBinaryAnnotation)binaryValue, env, missingTypeNames);
      } else if (binaryValue instanceof EnumConstantSignature) {
         EnumConstantSignature ref = (EnumConstantSignature)binaryValue;
         ReferenceBinding enumType = (ReferenceBinding)env.getTypeFromSignature(
            ref.getTypeName(), 0, -1, false, null, missingTypeNames, ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
         );
         if (enumType.isUnresolvedType() && !resolveEnumConstants) {
            return new ElementValuePair.UnresolvedEnumConstant(enumType, env, ref.getEnumConstantName());
         } else {
            enumType = (ReferenceBinding)resolveType(enumType, env, false);
            return enumType.getField(ref.getEnumConstantName(), false);
         }
      } else if (!(binaryValue instanceof Object[])) {
         throw new IllegalStateException();
      } else {
         Object[] objects = (Object[])binaryValue;
         int length = objects.length;
         if (length == 0) {
            return objects;
         } else {
            Object[] values = new Object[length];

            for(int i = 0; i < length; ++i) {
               values[i] = convertMemberValue(objects[i], env, missingTypeNames, resolveEnumConstants);
            }

            return values;
         }
      }
   }

   @Override
   public TypeBinding clone(TypeBinding outerType) {
      BinaryTypeBinding copy = new BinaryTypeBinding(this);
      copy.enclosingType = (ReferenceBinding)outerType;
      if (copy.enclosingType != null) {
         copy.tagBits |= 134217728L;
      } else {
         copy.tagBits &= -134217729L;
      }

      copy.tagBits |= 268435456L;
      return copy;
   }

   static AnnotationBinding createAnnotation(IBinaryAnnotation annotationInfo, LookupEnvironment env, char[][][] missingTypeNames) {
      IBinaryElementValuePair[] binaryPairs = annotationInfo.getElementValuePairs();
      int length = binaryPairs == null ? 0 : binaryPairs.length;
      ElementValuePair[] pairs = length == 0 ? Binding.NO_ELEMENT_VALUE_PAIRS : new ElementValuePair[length];

      for(int i = 0; i < length; ++i) {
         pairs[i] = new ElementValuePair(binaryPairs[i].getName(), convertMemberValue(binaryPairs[i].getValue(), env, missingTypeNames, false), null);
      }

      char[] typeName = annotationInfo.getTypeName();
      ReferenceBinding annotationType = env.getTypeFromConstantPoolName(typeName, 1, typeName.length - 1, false, missingTypeNames);
      return env.createUnresolvedAnnotation(annotationType, pairs);
   }

   public static AnnotationBinding[] createAnnotations(IBinaryAnnotation[] annotationInfos, LookupEnvironment env, char[][][] missingTypeNames) {
      int length = annotationInfos == null ? 0 : annotationInfos.length;
      AnnotationBinding[] result = length == 0 ? Binding.NO_ANNOTATIONS : new AnnotationBinding[length];

      for(int i = 0; i < length; ++i) {
         result[i] = createAnnotation(annotationInfos[i], env, missingTypeNames);
      }

      return result;
   }

   public static TypeBinding resolveType(TypeBinding type, LookupEnvironment environment, boolean convertGenericToRawType) {
      switch(type.kind()) {
         case 68:
            ArrayBinding arrayBinding = (ArrayBinding)type;
            TypeBinding leafComponentType = arrayBinding.leafComponentType;
            resolveType(leafComponentType, environment, convertGenericToRawType);
            if (leafComponentType.hasNullTypeAnnotations() && environment.usesNullTypeAnnotations()) {
               if (arrayBinding.nullTagBitsPerDimension == null) {
                  arrayBinding.nullTagBitsPerDimension = new long[arrayBinding.dimensions + 1];
               }

               arrayBinding.nullTagBitsPerDimension[arrayBinding.dimensions] = leafComponentType.tagBits & 108086391056891904L;
            }
            break;
         case 260:
            ((ParameterizedTypeBinding)type).resolve();
            break;
         case 516:
         case 8196:
            return ((WildcardBinding)type).resolve();
         case 2052:
            if (convertGenericToRawType) {
               return environment.convertUnresolvedBinaryToRawType(type);
            }
            break;
         case 4100:
            ((TypeVariableBinding)type).resolve();
            break;
         default:
            if (type instanceof UnresolvedReferenceBinding) {
               return ((UnresolvedReferenceBinding)type).resolve(environment, convertGenericToRawType);
            }

            if (convertGenericToRawType) {
               return environment.convertUnresolvedBinaryToRawType(type);
            }
      }

      return type;
   }

   protected BinaryTypeBinding() {
      this.prototype = this;
   }

   public BinaryTypeBinding(BinaryTypeBinding prototype) {
      super(prototype);
      this.superclass = prototype.superclass;
      this.enclosingType = prototype.enclosingType;
      this.superInterfaces = prototype.superInterfaces;
      this.fields = prototype.fields;
      this.methods = prototype.methods;
      this.memberTypes = prototype.memberTypes;
      this.typeVariables = prototype.typeVariables;
      this.prototype = prototype.prototype;
      this.environment = prototype.environment;
      this.storedAnnotations = prototype.storedAnnotations;
   }

   public BinaryTypeBinding(PackageBinding packageBinding, IBinaryType binaryType, LookupEnvironment environment) {
      this(packageBinding, binaryType, environment, false);
   }

   public BinaryTypeBinding(PackageBinding packageBinding, IBinaryType binaryType, LookupEnvironment environment, boolean needFieldsAndMethods) {
      this.prototype = this;
      this.compoundName = CharOperation.splitOn('/', binaryType.getName());
      this.computeId();
      this.tagBits |= 64L;
      this.environment = environment;
      this.fPackage = packageBinding;
      this.fileName = binaryType.getFileName();
      char[] typeSignature = binaryType.getGenericSignature();
      this.typeVariables = typeSignature != null && typeSignature.length > 0 && typeSignature[0] == '<' ? null : Binding.NO_TYPE_VARIABLES;
      this.sourceName = binaryType.getSourceName();
      this.modifiers = binaryType.getModifiers();
      if ((binaryType.getTagBits() & 131072L) != 0L) {
         this.tagBits |= 131072L;
      }

      if (binaryType.isAnonymous()) {
         this.tagBits |= 2100L;
      } else if (binaryType.isLocal()) {
         this.tagBits |= 2068L;
      } else if (binaryType.isMember()) {
         this.tagBits |= 2060L;
      }

      char[] enclosingTypeName = binaryType.getEnclosingTypeName();
      if (enclosingTypeName != null) {
         this.enclosingType = environment.getTypeFromConstantPoolName(enclosingTypeName, 0, -1, true, null);
         this.tagBits |= 2060L;
         this.tagBits |= 134217728L;
         if (this.enclosingType().isStrictfp()) {
            this.modifiers |= 2048;
         }

         if (this.enclosingType().isDeprecated()) {
            this.modifiers |= 2097152;
         }
      }

      if (needFieldsAndMethods) {
         this.cachePartsFrom(binaryType, true);
      }
   }

   @Override
   public FieldBinding[] availableFields() {
      if (!this.isPrototype()) {
         return this.prototype.availableFields();
      } else if ((this.tagBits & 8192L) != 0L) {
         return this.fields;
      } else {
         if ((this.tagBits & 4096L) == 0L) {
            int length = this.fields.length;
            if (length > 1) {
               ReferenceBinding.sortFields(this.fields, 0, length);
            }

            this.tagBits |= 4096L;
         }

         FieldBinding[] availableFields = new FieldBinding[this.fields.length];
         int count = 0;

         for(int i = 0; i < this.fields.length; ++i) {
            try {
               availableFields[count] = this.resolveTypeFor(this.fields[i]);
               ++count;
            } catch (AbortCompilation var4) {
            }
         }

         if (count < availableFields.length) {
            System.arraycopy(availableFields, 0, availableFields = new FieldBinding[count], 0, count);
         }

         return availableFields;
      }
   }

   private TypeVariableBinding[] addMethodTypeVariables(TypeVariableBinding[] methodTypeVars) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else if (this.typeVariables != null && this.typeVariables != Binding.NO_TYPE_VARIABLES) {
         if (methodTypeVars != null && methodTypeVars != Binding.NO_TYPE_VARIABLES) {
            int total = this.typeVariables.length + methodTypeVars.length;
            TypeVariableBinding[] combinedTypeVars = new TypeVariableBinding[total];
            System.arraycopy(this.typeVariables, 0, combinedTypeVars, 0, this.typeVariables.length);
            int size = this.typeVariables.length;
            int i = 0;

            label37:
            for(int len = methodTypeVars.length; i < len; ++i) {
               for(int j = this.typeVariables.length - 1; j >= 0; --j) {
                  if (CharOperation.equals(methodTypeVars[i].sourceName, this.typeVariables[j].sourceName)) {
                     continue label37;
                  }
               }

               combinedTypeVars[size++] = methodTypeVars[i];
            }

            if (size != total) {
               System.arraycopy(combinedTypeVars, 0, combinedTypeVars = new TypeVariableBinding[size], 0, size);
            }

            return combinedTypeVars;
         } else {
            return this.typeVariables;
         }
      } else {
         return methodTypeVars;
      }
   }

   @Override
   public MethodBinding[] availableMethods() {
      if (!this.isPrototype()) {
         return this.prototype.availableMethods();
      } else if ((this.tagBits & 32768L) != 0L) {
         return this.methods;
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         MethodBinding[] availableMethods = new MethodBinding[this.methods.length];
         int count = 0;

         for(int i = 0; i < this.methods.length; ++i) {
            try {
               availableMethods[count] = this.resolveTypesFor(this.methods[i]);
               ++count;
            } catch (AbortCompilation var4) {
            }
         }

         if (count < availableMethods.length) {
            System.arraycopy(availableMethods, 0, availableMethods = new MethodBinding[count], 0, count);
         }

         return availableMethods;
      }
   }

   void cachePartsFrom(IBinaryType binaryType, boolean needFieldsAndMethods) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         try {
            this.typeVariables = Binding.NO_TYPE_VARIABLES;
            this.superInterfaces = Binding.NO_SUPERINTERFACES;
            this.memberTypes = Binding.NO_MEMBER_TYPES;
            IBinaryNestedType[] memberTypeStructures = binaryType.getMemberTypes();
            if (memberTypeStructures != null) {
               int size = memberTypeStructures.length;
               if (size > 0) {
                  this.memberTypes = new ReferenceBinding[size];

                  for(int i = 0; i < size; ++i) {
                     this.memberTypes[i] = this.environment.getTypeFromConstantPoolName(memberTypeStructures[i].getName(), 0, -1, false, null);
                  }

                  this.tagBits |= 268435456L;
               }
            }

            CompilerOptions globalOptions = this.environment.globalOptions;
            long sourceLevel = globalOptions.originalSourceLevel;
            if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
               this.scanTypeForNullDefaultAnnotation(binaryType, this.fPackage);
            }

            ITypeAnnotationWalker walker = this.getTypeAnnotationWalker(binaryType.getTypeAnnotations(), 0);
            ITypeAnnotationWalker toplevelWalker = binaryType.enrichWithExternalAnnotationsFor(walker, null, this.environment);
            this.externalAnnotationStatus = binaryType.getExternalAnnotationStatus();
            if (this.externalAnnotationStatus.isPotentiallyUnannotatedLib() && this.defaultNullness != 0) {
               this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
            }

            char[] typeSignature = binaryType.getGenericSignature();
            this.tagBits |= binaryType.getTagBits();
            char[][][] missingTypeNames = binaryType.getMissingTypeNames();
            SignatureWrapper wrapper = null;
            if (typeSignature != null) {
               wrapper = new SignatureWrapper(typeSignature);
               if (wrapper.signature[wrapper.start] == '<') {
                  ++wrapper.start;
                  this.typeVariables = this.createTypeVariables(wrapper, true, missingTypeNames, toplevelWalker, true);
                  ++wrapper.start;
                  this.tagBits |= 16777216L;
                  this.modifiers |= 1073741824;
               }
            }

            TypeVariableBinding[] typeVars = Binding.NO_TYPE_VARIABLES;
            char[] methodDescriptor = binaryType.getEnclosingMethod();
            if (methodDescriptor != null) {
               MethodBinding enclosingMethod = this.findMethod(methodDescriptor, missingTypeNames);
               if (enclosingMethod != null) {
                  typeVars = enclosingMethod.typeVariables;
                  this.typeVariables = this.addMethodTypeVariables(typeVars);
               }
            }

            if (typeSignature == null) {
               char[] superclassName = binaryType.getSuperclassName();
               if (superclassName != null) {
                  this.superclass = this.environment
                     .getTypeFromConstantPoolName(superclassName, 0, -1, false, missingTypeNames, toplevelWalker.toSupertype((short)-1, superclassName));
                  this.tagBits |= 33554432L;
               }

               this.superInterfaces = Binding.NO_SUPERINTERFACES;
               char[][] interfaceNames = binaryType.getInterfaceNames();
               if (interfaceNames != null) {
                  int size = interfaceNames.length;
                  if (size > 0) {
                     this.superInterfaces = new ReferenceBinding[size];

                     for(short i = 0; i < size; ++i) {
                        this.superInterfaces[i] = this.environment
                           .getTypeFromConstantPoolName(interfaceNames[i], 0, -1, false, missingTypeNames, toplevelWalker.toSupertype(i, superclassName));
                     }

                     this.tagBits |= 67108864L;
                  }
               }
            } else {
               this.superclass = (ReferenceBinding)this.environment
                  .getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, toplevelWalker.toSupertype((short)-1, wrapper.peekFullType()));
               this.tagBits |= 33554432L;
               this.superInterfaces = Binding.NO_SUPERINTERFACES;
               if (!wrapper.atEnd()) {
                  ArrayList types = new ArrayList(2);
                  short rank = 0;

                  do {
                     types.add(
                        this.environment
                           .getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, toplevelWalker.toSupertype(rank++, wrapper.peekFullType()))
                     );
                  } while(!wrapper.atEnd());

                  this.superInterfaces = new ReferenceBinding[types.size()];
                  types.toArray(this.superInterfaces);
                  this.tagBits |= 67108864L;
               }
            }

            boolean canUseNullTypeAnnotations = this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled
               && this.environment.globalOptions.sourceLevel >= 3407872L;
            if (canUseNullTypeAnnotations && this.externalAnnotationStatus.isPotentiallyUnannotatedLib()) {
               if (this.superclass != null && this.superclass.hasNullTypeAnnotations()) {
                  this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
               } else {
                  for(TypeBinding ifc : this.superInterfaces) {
                     if (ifc.hasNullTypeAnnotations()) {
                        this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
                        break;
                     }
                  }
               }
            }

            if (needFieldsAndMethods) {
               IBinaryField[] iFields = binaryType.getFields();
               this.createFields(iFields, binaryType, sourceLevel, missingTypeNames);
               IBinaryMethod[] iMethods = this.createMethods(binaryType.getMethods(), binaryType, sourceLevel, missingTypeNames);
               boolean isViewedAsDeprecated = this.isViewedAsDeprecated();
               if (isViewedAsDeprecated) {
                  int i = 0;

                  for(int max = this.fields.length; i < max; ++i) {
                     FieldBinding field = this.fields[i];
                     if (!field.isDeprecated()) {
                        field.modifiers |= 2097152;
                     }
                  }

                  i = 0;

                  for(int max = this.methods.length; i < max; ++i) {
                     MethodBinding method = this.methods[i];
                     if (!method.isDeprecated()) {
                        method.modifiers |= 2097152;
                     }
                  }
               }

               if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
                  if (iFields != null) {
                     for(int i = 0; i < iFields.length; ++i) {
                        ITypeAnnotationWalker fieldWalker = ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
                        if (sourceLevel < 3407872L) {
                           fieldWalker = binaryType.enrichWithExternalAnnotationsFor(walker, iFields[i], this.environment);
                        }

                        this.scanFieldForNullAnnotation(iFields[i], this.fields[i], this.isEnum(), fieldWalker);
                     }
                  }

                  if (iMethods != null) {
                     for(int i = 0; i < iMethods.length; ++i) {
                        ITypeAnnotationWalker methodWalker = ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
                        if (sourceLevel < 3407872L) {
                           methodWalker = binaryType.enrichWithExternalAnnotationsFor(methodWalker, iMethods[i], this.environment);
                        }

                        this.scanMethodForNullAnnotation(iMethods[i], this.methods[i], methodWalker, canUseNullTypeAnnotations);
                     }
                  }
               }
            }

            if (this.environment.globalOptions.storeAnnotations) {
               this.setAnnotations(createAnnotations(binaryType.getAnnotations(), this.environment, missingTypeNames));
            }

            if (this.isAnnotationType()) {
               this.scanTypeForContainerAnnotation(binaryType, missingTypeNames);
            }
         } finally {
            if (this.fields == null) {
               this.fields = Binding.NO_FIELDS;
            }

            if (this.methods == null) {
               this.methods = Binding.NO_METHODS;
            }
         }
      }
   }

   private ITypeAnnotationWalker getTypeAnnotationWalker(IBinaryTypeAnnotation[] annotations, int nullness) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else if (annotations != null && annotations.length != 0 && this.environment.usesAnnotatedTypeSystem()) {
         if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
            if (nullness == 0) {
               nullness = this.getNullDefault();
            }

            if (nullness > 2) {
               return new NonNullDefaultAwareTypeAnnotationWalker(annotations, nullness, this.environment);
            }
         }

         return new TypeAnnotationWalker(annotations);
      } else {
         if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
            if (nullness == 0) {
               nullness = this.getNullDefault();
            }

            if (nullness > 2) {
               return new NonNullDefaultAwareTypeAnnotationWalker(nullness, this.environment);
            }
         }

         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }
   }

   private int getNullDefaultFrom(IBinaryAnnotation[] declAnnotations) {
      if (declAnnotations != null) {
         for(IBinaryAnnotation annotation : declAnnotations) {
            char[][] typeName = this.signature2qualifiedTypeName(annotation.getTypeName());
            if (this.environment.getNullAnnotationBit(typeName) == 128) {
               return this.getNonNullByDefaultValue(annotation);
            }
         }
      }

      return 0;
   }

   private void createFields(IBinaryField[] iFields, IBinaryType binaryType, long sourceLevel, char[][][] missingTypeNames) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         this.fields = Binding.NO_FIELDS;
         if (iFields != null) {
            int size = iFields.length;
            if (size > 0) {
               FieldBinding[] fields1 = new FieldBinding[size];
               boolean use15specifics = sourceLevel >= 3211264L;
               boolean hasRestrictedAccess = this.hasRestrictedAccess();
               int firstAnnotatedFieldIndex = -1;

               for(int i = 0; i < size; ++i) {
                  IBinaryField binaryField = iFields[i];
                  char[] fieldSignature = use15specifics ? binaryField.getGenericSignature() : null;
                  ITypeAnnotationWalker walker = this.getTypeAnnotationWalker(binaryField.getTypeAnnotations(), 0);
                  if (sourceLevel >= 3407872L) {
                     walker = binaryType.enrichWithExternalAnnotationsFor(walker, iFields[i], this.environment);
                  }

                  walker = walker.toField();
                  TypeBinding type = fieldSignature == null
                     ? this.environment.getTypeFromSignature(binaryField.getTypeName(), 0, -1, false, this, missingTypeNames, walker)
                     : this.environment
                        .getTypeFromTypeSignature(new SignatureWrapper(fieldSignature), Binding.NO_TYPE_VARIABLES, this, missingTypeNames, walker);
                  FieldBinding field = new FieldBinding(binaryField.getName(), type, binaryField.getModifiers() | 33554432, this, binaryField.getConstant());
                  if (firstAnnotatedFieldIndex < 0 && this.environment.globalOptions.storeAnnotations && binaryField.getAnnotations() != null) {
                     firstAnnotatedFieldIndex = i;
                  }

                  field.id = i;
                  if (use15specifics) {
                     field.tagBits |= binaryField.getTagBits();
                  }

                  if (hasRestrictedAccess) {
                     field.modifiers |= 262144;
                  }

                  if (fieldSignature != null) {
                     field.modifiers |= 1073741824;
                  }

                  fields1[i] = field;
               }

               this.fields = fields1;
               if (firstAnnotatedFieldIndex >= 0) {
                  for(int i = firstAnnotatedFieldIndex; i < size; ++i) {
                     IBinaryField binaryField = iFields[i];
                     this.fields[i].setAnnotations(createAnnotations(binaryField.getAnnotations(), this.environment, missingTypeNames));
                  }
               }
            }
         }
      }
   }

   private MethodBinding createMethod(IBinaryMethod method, IBinaryType binaryType, long sourceLevel, char[][][] missingTypeNames) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         int methodModifiers = method.getModifiers() | 33554432;
         if (sourceLevel < 3211264L) {
            methodModifiers &= -129;
         }

         if (this.isInterface() && (methodModifiers & 1024) == 0 && (methodModifiers & 8) == 0) {
            methodModifiers |= 65536;
         }

         ReferenceBinding[] exceptions = Binding.NO_EXCEPTIONS;
         TypeBinding[] parameters = Binding.NO_PARAMETERS;
         TypeVariableBinding[] typeVars = Binding.NO_TYPE_VARIABLES;
         AnnotationBinding[][] paramAnnotations = null;
         TypeBinding returnType = null;
         char[][] argumentNames = method.getArgumentNames();
         boolean use15specifics = sourceLevel >= 3211264L;
         ITypeAnnotationWalker walker = this.getTypeAnnotationWalker(method.getTypeAnnotations(), this.getNullDefaultFrom(method.getAnnotations()));
         char[] methodSignature = method.getGenericSignature();
         if (methodSignature == null) {
            char[] methodDescriptor = method.getMethodDescriptor();
            if (sourceLevel >= 3407872L) {
               walker = binaryType.enrichWithExternalAnnotationsFor(walker, method, this.environment);
            }

            int numOfParams = 0;
            int index = 0;

            char nextChar;
            while((nextChar = methodDescriptor[++index]) != ')') {
               if (nextChar != '[') {
                  ++numOfParams;
                  if (nextChar == 'L') {
                     while((nextChar = methodDescriptor[++index]) != ';') {
                     }
                  }
               }
            }

            int startIndex = 0;
            if (method.isConstructor()) {
               if (this.isMemberType() && !this.isStatic()) {
                  ++startIndex;
               }

               if (this.isEnum()) {
                  startIndex += 2;
               }
            }

            int size = numOfParams - startIndex;
            if (size > 0) {
               parameters = new TypeBinding[size];
               if (this.environment.globalOptions.storeAnnotations) {
                  paramAnnotations = new AnnotationBinding[size][];
               }

               index = 1;
               short visibleIdx = 0;
               int end = 0;

               for(int i = 0; i < numOfParams; ++i) {
                  while((nextChar = methodDescriptor[++end]) == '[') {
                  }

                  if (nextChar == 'L') {
                     while((nextChar = methodDescriptor[++end]) != true) {
                     }
                  }

                  if (i >= startIndex) {
                     parameters[i - startIndex] = this.environment
                        .getTypeFromSignature(methodDescriptor, index, end, false, this, missingTypeNames, walker.toMethodParameter(visibleIdx++));
                     if (paramAnnotations != null) {
                        paramAnnotations[i - startIndex] = createAnnotations(
                           method.getParameterAnnotations(i - startIndex, this.fileName), this.environment, missingTypeNames
                        );
                     }
                  }

                  index = end + 1;
               }
            }

            char[][] exceptionTypes = method.getExceptionTypeNames();
            if (exceptionTypes != null) {
               size = exceptionTypes.length;
               if (size > 0) {
                  exceptions = new ReferenceBinding[size];

                  for(int i = 0; i < size; ++i) {
                     exceptions[i] = this.environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false, missingTypeNames, walker.toThrows(i));
                  }
               }
            }

            if (!method.isConstructor()) {
               returnType = this.environment.getTypeFromSignature(methodDescriptor, index + 1, -1, false, this, missingTypeNames, walker.toMethodReturn());
            }

            int argumentNamesLength = argumentNames == null ? 0 : argumentNames.length;
            if (startIndex > 0 && argumentNamesLength > 0) {
               if (startIndex >= argumentNamesLength) {
                  argumentNames = Binding.NO_PARAMETER_NAMES;
               } else {
                  char[][] slicedArgumentNames = new char[argumentNamesLength - startIndex][];
                  System.arraycopy(argumentNames, startIndex, slicedArgumentNames, 0, argumentNamesLength - startIndex);
                  argumentNames = slicedArgumentNames;
               }
            }
         } else {
            if (sourceLevel >= 3407872L) {
               walker = binaryType.enrichWithExternalAnnotationsFor(walker, method, this.environment);
            }

            methodModifiers |= 1073741824;
            SignatureWrapper wrapper = new SignatureWrapper(methodSignature, use15specifics);
            if (wrapper.signature[wrapper.start] == '<') {
               ++wrapper.start;
               typeVars = this.createTypeVariables(wrapper, false, missingTypeNames, walker, false);
               ++wrapper.start;
            }

            if (wrapper.signature[wrapper.start] == '(') {
               ++wrapper.start;
               if (wrapper.signature[wrapper.start] == ')') {
                  ++wrapper.start;
               } else {
                  ArrayList types = new ArrayList(2);
                  short rank = 0;

                  while(wrapper.signature[wrapper.start] != ')') {
                     types.add(this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toMethodParameter(rank++)));
                  }

                  ++wrapper.start;
                  int numParam = types.size();
                  parameters = new TypeBinding[numParam];
                  types.toArray(parameters);
                  if (this.environment.globalOptions.storeAnnotations) {
                     paramAnnotations = new AnnotationBinding[numParam][];

                     for(int i = 0; i < numParam; ++i) {
                        paramAnnotations[i] = createAnnotations(method.getParameterAnnotations(i, this.fileName), this.environment, missingTypeNames);
                     }
                  }
               }
            }

            returnType = this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toMethodReturn());
            if (!wrapper.atEnd() && wrapper.signature[wrapper.start] == '^') {
               ArrayList types = new ArrayList(2);
               int excRank = 0;

               do {
                  ++wrapper.start;
                  types.add(this.environment.getTypeFromTypeSignature(wrapper, typeVars, this, missingTypeNames, walker.toThrows(excRank++)));
               } while(!wrapper.atEnd() && wrapper.signature[wrapper.start] == '^');

               exceptions = new ReferenceBinding[types.size()];
               types.toArray(exceptions);
            } else {
               char[][] exceptionTypes = method.getExceptionTypeNames();
               if (exceptionTypes != null) {
                  int size = exceptionTypes.length;
                  if (size > 0) {
                     exceptions = new ReferenceBinding[size];

                     for(int i = 0; i < size; ++i) {
                        exceptions[i] = this.environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false, missingTypeNames, walker.toThrows(i));
                     }
                  }
               }
            }
         }

         MethodBinding result = method.isConstructor()
            ? new MethodBinding(methodModifiers, parameters, exceptions, this)
            : new MethodBinding(methodModifiers, method.getSelector(), returnType, parameters, exceptions, this);
         IBinaryAnnotation[] receiverAnnotations = walker.toReceiver().getAnnotationsAtCursor(this.id);
         if (receiverAnnotations != null && receiverAnnotations.length > 0) {
            result.receiver = this.environment.createAnnotatedType(this, createAnnotations(receiverAnnotations, this.environment, missingTypeNames));
         }

         if (this.environment.globalOptions.storeAnnotations) {
            IBinaryAnnotation[] annotations = method.getAnnotations();
            if ((annotations == null || annotations.length == 0) && method.isConstructor()) {
               annotations = walker.toMethodReturn().getAnnotationsAtCursor(this.id);
            }

            result.setAnnotations(
               createAnnotations(annotations, this.environment, missingTypeNames),
               paramAnnotations,
               this.isAnnotationType() ? convertMemberValue(method.getDefaultValue(), this.environment, missingTypeNames, true) : null,
               this.environment
            );
         }

         if (argumentNames != null) {
            result.parameterNames = argumentNames;
         }

         if (use15specifics) {
            result.tagBits |= method.getTagBits();
         }

         result.typeVariables = typeVars;
         int i = 0;

         for(int length = typeVars.length; i < length; ++i) {
            this.environment.typeSystem.fixTypeVariableDeclaringElement(typeVars[i], result);
         }

         return result;
      }
   }

   private IBinaryMethod[] createMethods(IBinaryMethod[] iMethods, IBinaryType binaryType, long sourceLevel, char[][][] missingTypeNames) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         int total = 0;
         int initialTotal = 0;
         int iClinit = -1;
         int[] toSkip = null;
         if (iMethods != null) {
            total = initialTotal = iMethods.length;
            boolean keepBridgeMethods = sourceLevel < 3211264L;
            int i = total;

            while(--i >= 0) {
               IBinaryMethod method = iMethods[i];
               if ((method.getModifiers() & 4096) != 0) {
                  if (!keepBridgeMethods || (method.getModifiers() & 64) == 0) {
                     if (toSkip == null) {
                        toSkip = new int[iMethods.length];
                     }

                     toSkip[i] = -1;
                     --total;
                  }
               } else if (iClinit == -1) {
                  char[] methodName = method.getSelector();
                  if (methodName.length == 8 && methodName[0] == '<') {
                     iClinit = i;
                     --total;
                  }
               }
            }
         }

         if (total == 0) {
            this.methods = Binding.NO_METHODS;
            return NO_BINARY_METHODS;
         } else {
            boolean hasRestrictedAccess = this.hasRestrictedAccess();
            MethodBinding[] methods1 = new MethodBinding[total];
            if (total == initialTotal) {
               for(int i = 0; i < initialTotal; ++i) {
                  MethodBinding method = this.createMethod(iMethods[i], binaryType, sourceLevel, missingTypeNames);
                  if (hasRestrictedAccess) {
                     method.modifiers |= 262144;
                  }

                  methods1[i] = method;
               }

               this.methods = methods1;
               return iMethods;
            } else {
               IBinaryMethod[] mappedBinaryMethods = new IBinaryMethod[total];
               int i = 0;

               for(int index = 0; i < initialTotal; ++i) {
                  if (iClinit != i && (toSkip == null || toSkip[i] != -1)) {
                     MethodBinding method = this.createMethod(iMethods[i], binaryType, sourceLevel, missingTypeNames);
                     if (hasRestrictedAccess) {
                        method.modifiers |= 262144;
                     }

                     mappedBinaryMethods[index] = iMethods[i];
                     methods1[index++] = method;
                  }
               }

               this.methods = methods1;
               return mappedBinaryMethods;
            }
         }
      }
   }

   private TypeVariableBinding[] createTypeVariables(
      SignatureWrapper wrapper, boolean assignVariables, char[][][] missingTypeNames, ITypeAnnotationWalker walker, boolean isClassTypeParameter
   ) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         char[] typeSignature = wrapper.signature;
         int depth = 0;
         int length = typeSignature.length;
         int rank = 0;
         ArrayList variables = new ArrayList(1);
         depth = 0;
         boolean pendingVariable = true;

         label55:
         for(int i = 1; i < length; ++i) {
            switch(typeSignature[i]) {
               case ';':
                  if (depth == 0 && i + 1 < length && typeSignature[i + 1] != ':') {
                     pendingVariable = true;
                  }
                  break;
               case '<':
                  ++depth;
                  break;
               case '=':
               default:
                  if (pendingVariable) {
                     pendingVariable = false;
                     int colon = CharOperation.indexOf(':', typeSignature, i);
                     char[] variableName = CharOperation.subarray(typeSignature, i, colon);
                     TypeVariableBinding typeVariable = new TypeVariableBinding(variableName, this, rank, this.environment);
                     AnnotationBinding[] annotations = createAnnotations(
                        walker.toTypeParameter(isClassTypeParameter, rank++).getAnnotationsAtCursor(0), this.environment, missingTypeNames
                     );
                     if (annotations != null && annotations != Binding.NO_ANNOTATIONS) {
                        typeVariable.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
                     }

                     variables.add(typeVariable);
                  }
                  break;
               case '>':
                  if (--depth < 0) {
                     break label55;
                  }
            }
         }

         TypeVariableBinding[] result;
         variables.toArray(result = new TypeVariableBinding[rank]);
         if (assignVariables) {
            this.typeVariables = result;
         }

         for(int i = 0; i < rank; ++i) {
            this.initializeTypeVariable(result[i], result, wrapper, missingTypeNames, walker.toTypeParameterBounds(isClassTypeParameter, i));
            if (this.externalAnnotationStatus.isPotentiallyUnannotatedLib() && result[i].hasNullTypeAnnotations()) {
               this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
            }
         }

         return result;
      }
   }

   @Override
   public ReferenceBinding enclosingType() {
      if ((this.tagBits & 134217728L) == 0L) {
         return this.enclosingType;
      } else {
         this.enclosingType = (ReferenceBinding)resolveType(this.enclosingType, this.environment, false);
         this.tagBits &= -134217729L;
         return this.enclosingType;
      }
   }

   @Override
   public FieldBinding[] fields() {
      if (!this.isPrototype()) {
         return this.fields = this.prototype.fields();
      } else if ((this.tagBits & 8192L) != 0L) {
         return this.fields;
      } else {
         if ((this.tagBits & 4096L) == 0L) {
            int length = this.fields.length;
            if (length > 1) {
               ReferenceBinding.sortFields(this.fields, 0, length);
            }

            this.tagBits |= 4096L;
         }

         int i = this.fields.length;

         while(--i >= 0) {
            this.resolveTypeFor(this.fields[i]);
         }

         this.tagBits |= 8192L;
         return this.fields;
      }
   }

   private MethodBinding findMethod(char[] methodDescriptor, char[][][] missingTypeNames) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         int index = -1;

         while(methodDescriptor[++index] != '(') {
         }

         char[] selector = new char[index];
         System.arraycopy(methodDescriptor, 0, selector, 0, index);
         TypeBinding[] parameters = Binding.NO_PARAMETERS;
         int numOfParams = 0;
         int paramStart = index;

         char nextChar;
         while((nextChar = methodDescriptor[++index]) != ')') {
            if (nextChar != '[') {
               ++numOfParams;
               if (nextChar == 'L') {
                  while((nextChar = methodDescriptor[++index]) != ';') {
                  }
               }
            }
         }

         if (numOfParams > 0) {
            parameters = new TypeBinding[numOfParams];
            index = paramStart + 1;
            int end = paramStart;

            for(int i = 0; i < numOfParams; ++i) {
               while((nextChar = methodDescriptor[++end]) == '[') {
               }

               if (nextChar == 'L') {
                  while((nextChar = methodDescriptor[++end]) != true) {
                  }
               }

               TypeBinding param = this.environment
                  .getTypeFromSignature(methodDescriptor, index, end, false, this, missingTypeNames, ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
               if (param instanceof UnresolvedReferenceBinding) {
                  param = resolveType(param, this.environment, true);
               }

               parameters[i] = param;
               index = end + 1;
            }
         }

         int parameterLength = parameters.length;
         MethodBinding[] methods2 = this.enclosingType.getMethods(selector, parameterLength);
         int i = 0;

         label51:
         for(int max = methods2.length; i < max; ++i) {
            MethodBinding currentMethod = methods2[i];
            TypeBinding[] parameters2 = currentMethod.parameters;
            int currentMethodParameterLength = parameters2.length;
            if (parameterLength == currentMethodParameterLength) {
               for(int j = 0; j < currentMethodParameterLength; ++j) {
                  if (TypeBinding.notEquals(parameters[j], parameters2[j]) && TypeBinding.notEquals(parameters[j].erasure(), parameters2[j].erasure())) {
                     continue label51;
                  }
               }

               return currentMethod;
            }
         }

         return null;
      }
   }

   @Override
   public char[] genericTypeSignature() {
      return !this.isPrototype() ? this.prototype.computeGenericTypeSignature(this.typeVariables) : this.computeGenericTypeSignature(this.typeVariables);
   }

   @Override
   public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
      if (!this.isPrototype()) {
         return this.prototype.getExactConstructor(argumentTypes);
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         int argCount = argumentTypes.length;
         long range;
         if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0L) {
            int imethod = (int)range;

            label38:
            for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
               MethodBinding method = this.methods[imethod];
               if (method.parameters.length == argCount) {
                  this.resolveTypesFor(method);
                  TypeBinding[] toMatch = method.parameters;

                  for(int iarg = 0; iarg < argCount; ++iarg) {
                     if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg])) {
                        continue label38;
                     }
                  }

                  return method;
               }
            }
         }

         return null;
      }
   }

   @Override
   public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
      if (!this.isPrototype()) {
         return this.prototype.getExactMethod(selector, argumentTypes, refScope);
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         int argCount = argumentTypes.length;
         boolean foundNothing = true;
         long range;
         if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0L) {
            int imethod = (int)range;

            label57:
            for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
               MethodBinding method = this.methods[imethod];
               foundNothing = false;
               if (method.parameters.length == argCount) {
                  this.resolveTypesFor(method);
                  TypeBinding[] toMatch = method.parameters;

                  for(int iarg = 0; iarg < argCount; ++iarg) {
                     if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg])) {
                        continue label57;
                     }
                  }

                  return method;
               }
            }
         }

         if (foundNothing) {
            if (this.isInterface()) {
               if (this.superInterfaces().length == 1) {
                  if (refScope != null) {
                     refScope.recordTypeReference(this.superInterfaces[0]);
                  }

                  return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
               }
            } else if (this.superclass() != null) {
               if (refScope != null) {
                  refScope.recordTypeReference(this.superclass);
               }

               return this.superclass.getExactMethod(selector, argumentTypes, refScope);
            }
         }

         return null;
      }
   }

   @Override
   public FieldBinding getField(char[] fieldName, boolean needResolve) {
      if (!this.isPrototype()) {
         return this.prototype.getField(fieldName, needResolve);
      } else {
         if ((this.tagBits & 4096L) == 0L) {
            int length = this.fields.length;
            if (length > 1) {
               ReferenceBinding.sortFields(this.fields, 0, length);
            }

            this.tagBits |= 4096L;
         }

         FieldBinding field = ReferenceBinding.binarySearch(fieldName, this.fields);
         return needResolve && field != null ? this.resolveTypeFor(field) : field;
      }
   }

   @Override
   public ReferenceBinding getMemberType(char[] typeName) {
      if (!this.isPrototype()) {
         ReferenceBinding memberType = this.prototype.getMemberType(typeName);
         return memberType == null ? null : this.environment.createMemberType(memberType, this);
      } else {
         int i = this.memberTypes.length;

         while(--i >= 0) {
            ReferenceBinding memberType = this.memberTypes[i];
            if (memberType instanceof UnresolvedReferenceBinding) {
               char[] name = memberType.sourceName;
               int prefixLength = this.compoundName[this.compoundName.length - 1].length + 1;
               if (name.length == prefixLength + typeName.length && CharOperation.fragmentEquals(typeName, name, prefixLength, true)) {
                  return this.memberTypes[i] = (ReferenceBinding)resolveType(memberType, this.environment, false);
               }
            } else if (CharOperation.equals(typeName, memberType.sourceName)) {
               return memberType;
            }
         }

         return null;
      }
   }

   @Override
   public MethodBinding[] getMethods(char[] selector) {
      if (!this.isPrototype()) {
         return this.prototype.getMethods(selector);
      } else if ((this.tagBits & 32768L) != 0L) {
         long range;
         if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0L) {
            int start = (int)range;
            int end = (int)(range >> 32);
            int length = end - start + 1;
            if ((this.tagBits & 32768L) != 0L) {
               MethodBinding[] result;
               System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
               return result;
            }
         }

         return Binding.NO_METHODS;
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         long range;
         if ((range = ReferenceBinding.binarySearch(selector, this.methods)) < 0L) {
            return Binding.NO_METHODS;
         } else {
            int start = (int)range;
            int end = (int)(range >> 32);
            int length = end - start + 1;
            MethodBinding[] result = new MethodBinding[length];
            int i = start;

            for(int index = 0; i <= end; ++index) {
               result[index] = this.resolveTypesFor(this.methods[i]);
               ++i;
            }

            return result;
         }
      }
   }

   @Override
   public MethodBinding[] getMethods(char[] selector, int suggestedParameterLength) {
      if (!this.isPrototype()) {
         return this.prototype.getMethods(selector, suggestedParameterLength);
      } else if ((this.tagBits & 32768L) != 0L) {
         return this.getMethods(selector);
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         long range;
         if ((range = ReferenceBinding.binarySearch(selector, this.methods)) < 0L) {
            return Binding.NO_METHODS;
         } else {
            int start = (int)range;
            int end = (int)(range >> 32);
            int length = end - start + 1;
            int count = 0;

            for(int i = start; i <= end; ++i) {
               int len = this.methods[i].parameters.length;
               if (len <= suggestedParameterLength || this.methods[i].isVarargs() && len == suggestedParameterLength + 1) {
                  ++count;
               }
            }

            if (count == 0) {
               MethodBinding[] result = new MethodBinding[length];
               int i = start;

               for(int index = 0; i <= end; ++i) {
                  result[index++] = this.resolveTypesFor(this.methods[i]);
               }

               return result;
            } else {
               MethodBinding[] result = new MethodBinding[count];
               int i = start;

               for(int index = 0; i <= end; ++i) {
                  int len = this.methods[i].parameters.length;
                  if (len <= suggestedParameterLength || this.methods[i].isVarargs() && len == suggestedParameterLength + 1) {
                     result[index++] = this.resolveTypesFor(this.methods[i]);
                  }
               }

               return result;
            }
         }
      }
   }

   @Override
   public boolean hasMemberTypes() {
      if (!this.isPrototype()) {
         return this.prototype.hasMemberTypes();
      } else {
         return this.memberTypes.length > 0;
      }
   }

   @Override
   public TypeVariableBinding getTypeVariable(char[] variableName) {
      if (!this.isPrototype()) {
         return this.prototype.getTypeVariable(variableName);
      } else {
         TypeVariableBinding variable = super.getTypeVariable(variableName);
         variable.resolve();
         return variable;
      }
   }

   @Override
   public boolean hasTypeBit(int bit) {
      if (!this.isPrototype()) {
         return this.prototype.hasTypeBit(bit);
      } else {
         boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
         this.environment.mayTolerateMissingType = true;

         try {
            this.superclass();
            this.superInterfaces();
         } finally {
            this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
         }

         return (this.typeBits & bit) != 0;
      }
   }

   private void initializeTypeVariable(
      TypeVariableBinding variable,
      TypeVariableBinding[] existingVariables,
      SignatureWrapper wrapper,
      char[][][] missingTypeNames,
      ITypeAnnotationWalker walker
   ) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         int colon = CharOperation.indexOf(':', wrapper.signature, wrapper.start);
         wrapper.start = colon + 1;
         ReferenceBinding firstBound = null;
         short rank = 0;
         ReferenceBinding type;
         if (wrapper.signature[wrapper.start] == ':') {
            type = this.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
            ++rank;
         } else {
            TypeBinding typeFromTypeSignature = this.environment
               .getTypeFromTypeSignature(wrapper, existingVariables, this, missingTypeNames, walker.toTypeBound(rank++));
            if (typeFromTypeSignature instanceof ReferenceBinding) {
               type = (ReferenceBinding)typeFromTypeSignature;
            } else {
               type = this.environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
            }

            firstBound = type;
         }

         variable.modifiers |= 33554432;
         variable.setSuperClass(type);
         ReferenceBinding[] bounds = null;
         if (wrapper.signature[wrapper.start] == ':') {
            ArrayList types = new ArrayList(2);

            do {
               ++wrapper.start;
               types.add(this.environment.getTypeFromTypeSignature(wrapper, existingVariables, this, missingTypeNames, walker.toTypeBound(rank++)));
            } while(wrapper.signature[wrapper.start] == ':');

            bounds = new ReferenceBinding[types.size()];
            types.toArray(bounds);
         }

         variable.setSuperInterfaces(bounds == null ? Binding.NO_SUPERINTERFACES : bounds);
         if (firstBound == null) {
            firstBound = variable.superInterfaces.length == 0 ? null : variable.superInterfaces[0];
         }

         variable.setFirstBound(firstBound);
      }
   }

   @Override
   public boolean isEquivalentTo(TypeBinding otherType) {
      if (TypeBinding.equalsEquals(this, otherType)) {
         return true;
      } else if (otherType == null) {
         return false;
      } else {
         switch(otherType.kind()) {
            case 260:
            case 1028:
               return TypeBinding.equalsEquals(otherType.erasure(), this);
            case 516:
            case 8196:
               return ((WildcardBinding)otherType).boundCheck(this);
            default:
               return false;
         }
      }
   }

   @Override
   public boolean isGenericType() {
      if (!this.isPrototype()) {
         return this.prototype.isGenericType();
      } else {
         return this.typeVariables != Binding.NO_TYPE_VARIABLES;
      }
   }

   @Override
   public boolean isHierarchyConnected() {
      if (!this.isPrototype()) {
         return this.prototype.isHierarchyConnected();
      } else {
         return (this.tagBits & 100663296L) == 0L;
      }
   }

   @Override
   public boolean isRepeatableAnnotationType() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         return this.containerAnnotationType != null;
      }
   }

   @Override
   public int kind() {
      if (!this.isPrototype()) {
         return this.prototype.kind();
      } else {
         return this.typeVariables != Binding.NO_TYPE_VARIABLES ? 2052 : 4;
      }
   }

   @Override
   public ReferenceBinding[] memberTypes() {
      if (!this.isPrototype()) {
         if ((this.tagBits & 268435456L) == 0L) {
            return this.memberTypes;
         } else {
            ReferenceBinding[] members = this.prototype.memberTypes();
            int memberTypesLength = members == null ? 0 : members.length;
            if (memberTypesLength > 0) {
               this.memberTypes = new ReferenceBinding[memberTypesLength];

               for(int i = 0; i < memberTypesLength; ++i) {
                  this.memberTypes[i] = this.environment.createMemberType(members[i], this);
               }
            }

            this.tagBits &= -268435457L;
            return this.memberTypes;
         }
      } else if ((this.tagBits & 268435456L) == 0L) {
         return this.memberTypes;
      } else {
         int i = this.memberTypes.length;

         while(--i >= 0) {
            this.memberTypes[i] = (ReferenceBinding)resolveType(this.memberTypes[i], this.environment, false);
         }

         this.tagBits &= -268435457L;
         return this.memberTypes;
      }
   }

   @Override
   public MethodBinding[] methods() {
      if (!this.isPrototype()) {
         return this.methods = this.prototype.methods();
      } else if ((this.tagBits & 32768L) != 0L) {
         return this.methods;
      } else {
         if ((this.tagBits & 16384L) == 0L) {
            int length = this.methods.length;
            if (length > 1) {
               ReferenceBinding.sortMethods(this.methods, 0, length);
            }

            this.tagBits |= 16384L;
         }

         int i = this.methods.length;

         while(--i >= 0) {
            this.resolveTypesFor(this.methods[i]);
         }

         this.tagBits |= 32768L;
         return this.methods;
      }
   }

   @Override
   public TypeBinding prototype() {
      return this.prototype;
   }

   private boolean isPrototype() {
      return this == this.prototype;
   }

   @Override
   public ReferenceBinding containerAnnotationType() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         if (this.containerAnnotationType instanceof UnresolvedReferenceBinding) {
            this.containerAnnotationType = (ReferenceBinding)resolveType(this.containerAnnotationType, this.environment, false);
         }

         return this.containerAnnotationType;
      }
   }

   private FieldBinding resolveTypeFor(FieldBinding field) {
      if (!this.isPrototype()) {
         return this.prototype.resolveTypeFor(field);
      } else if ((field.modifiers & 33554432) == 0) {
         return field;
      } else {
         TypeBinding resolvedType = resolveType(field.type, this.environment, true);
         field.type = resolvedType;
         if ((resolvedType.tagBits & 128L) != 0L) {
            field.tagBits |= 128L;
         }

         field.modifiers &= -33554433;
         return field;
      }
   }

   MethodBinding resolveTypesFor(MethodBinding method) {
      if (!this.isPrototype()) {
         return this.prototype.resolveTypesFor(method);
      } else if ((method.modifiers & 33554432) == 0) {
         return method;
      } else {
         if (!method.isConstructor()) {
            TypeBinding resolvedType = resolveType(method.returnType, this.environment, true);
            method.returnType = resolvedType;
            if ((resolvedType.tagBits & 128L) != 0L) {
               method.tagBits |= 128L;
            }
         }

         int i = method.parameters.length;

         while(--i >= 0) {
            TypeBinding resolvedType = resolveType(method.parameters[i], this.environment, true);
            method.parameters[i] = resolvedType;
            if ((resolvedType.tagBits & 128L) != 0L) {
               method.tagBits |= 128L;
            }
         }

         i = method.thrownExceptions.length;

         while(--i >= 0) {
            ReferenceBinding resolvedType = (ReferenceBinding)resolveType(method.thrownExceptions[i], this.environment, true);
            method.thrownExceptions[i] = resolvedType;
            if ((resolvedType.tagBits & 128L) != 0L) {
               method.tagBits |= 128L;
            }
         }

         i = method.typeVariables.length;

         while(--i >= 0) {
            method.typeVariables[i].resolve();
         }

         method.modifiers &= -33554433;
         return method;
      }
   }

   @Override
   AnnotationBinding[] retrieveAnnotations(Binding binding) {
      return !this.isPrototype()
         ? this.prototype.retrieveAnnotations(binding)
         : AnnotationBinding.addStandardAnnotations(super.retrieveAnnotations(binding), binding.getAnnotationTagBits(), this.environment);
   }

   @Override
   public void setContainerAnnotationType(ReferenceBinding value) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         this.containerAnnotationType = value;
      }
   }

   @Override
   public void tagAsHavingDefectiveContainerType() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         if (this.containerAnnotationType != null && this.containerAnnotationType.isValidBinding()) {
            this.containerAnnotationType = new ProblemReferenceBinding(this.containerAnnotationType.compoundName, this.containerAnnotationType, 22);
         }
      }
   }

   @Override
   SimpleLookupTable storedAnnotations(boolean forceInitialize) {
      if (!this.isPrototype()) {
         return this.prototype.storedAnnotations(forceInitialize);
      } else {
         if (forceInitialize && this.storedAnnotations == null) {
            if (!this.environment.globalOptions.storeAnnotations) {
               return null;
            }

            this.storedAnnotations = new SimpleLookupTable(3);
         }

         return this.storedAnnotations;
      }
   }

   private void scanFieldForNullAnnotation(IBinaryField field, FieldBinding fieldBinding, boolean isEnum, ITypeAnnotationWalker externalAnnotationWalker) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else if (isEnum && (field.getModifiers() & 16384) != 0) {
         fieldBinding.tagBits |= 72057594037927936L;
      } else if (!CharOperation.equals(this.fPackage.compoundName, TypeConstants.JAVA_LANG_ANNOTATION) && this.environment.usesNullTypeAnnotations()) {
         TypeBinding fieldType = fieldBinding.type;
         if (fieldType != null
            && !fieldType.isBaseType()
            && (fieldType.tagBits & 108086391056891904L) == 0L
            && fieldType.acceptsNonNullDefault()
            && this.hasNonNullDefaultFor(32, true)) {
            fieldBinding.type = this.environment.createAnnotatedType(fieldType, new AnnotationBinding[]{this.environment.getNonNullAnnotation()});
         }
      } else if (fieldBinding.type != null && !fieldBinding.type.isBaseType()) {
         boolean explicitNullness = false;
         IBinaryAnnotation[] annotations = externalAnnotationWalker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
            ? externalAnnotationWalker.getAnnotationsAtCursor(fieldBinding.type.id)
            : field.getAnnotations();
         if (annotations != null) {
            for(int i = 0; i < annotations.length; ++i) {
               char[] annotationTypeName = annotations[i].getTypeName();
               if (annotationTypeName[0] == 'L') {
                  int typeBit = this.environment.getNullAnnotationBit(this.signature2qualifiedTypeName(annotationTypeName));
                  if (typeBit == 32) {
                     fieldBinding.tagBits |= 72057594037927936L;
                     explicitNullness = true;
                     break;
                  }

                  if (typeBit == 64) {
                     fieldBinding.tagBits |= 36028797018963968L;
                     explicitNullness = true;
                     break;
                  }
               }
            }
         }

         if (explicitNullness && this.externalAnnotationStatus.isPotentiallyUnannotatedLib()) {
            this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
         }

         if (!explicitNullness && (this.tagBits & 144115188075855872L) != 0L) {
            fieldBinding.tagBits |= 72057594037927936L;
         }
      }
   }

   private void scanMethodForNullAnnotation(
      IBinaryMethod method, MethodBinding methodBinding, ITypeAnnotationWalker externalAnnotationWalker, boolean useNullTypeAnnotations
   ) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         if (this.isEnum()) {
            int purpose = 0;
            if (CharOperation.equals(TypeConstants.VALUEOF, method.getSelector())
               && methodBinding.parameters.length == 1
               && methodBinding.parameters[0].id == 11) {
               purpose = 10;
            } else if (CharOperation.equals(TypeConstants.VALUES, method.getSelector()) && methodBinding.parameters == Binding.NO_PARAMETERS) {
               purpose = 9;
            }

            if (purpose != 0) {
               boolean needToDefer = this.environment.globalOptions.useNullTypeAnnotations == null;
               if (needToDefer) {
                  this.environment.deferredEnumMethods.add(methodBinding);
               } else {
                  SyntheticMethodBinding.markNonNull(methodBinding, purpose, this.environment);
               }

               return;
            }
         }

         ITypeAnnotationWalker returnWalker = externalAnnotationWalker.toMethodReturn();
         IBinaryAnnotation[] annotations = returnWalker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
            ? returnWalker.getAnnotationsAtCursor(methodBinding.returnType.id)
            : method.getAnnotations();
         if (annotations != null) {
            for(int i = 0; i < annotations.length; ++i) {
               char[] annotationTypeName = annotations[i].getTypeName();
               if (annotationTypeName[0] == 'L') {
                  int typeBit = this.environment.getNullAnnotationBit(this.signature2qualifiedTypeName(annotationTypeName));
                  if (typeBit == 128) {
                     methodBinding.defaultNullness = this.getNonNullByDefaultValue(annotations[i]);
                     if (methodBinding.defaultNullness == 2) {
                        methodBinding.tagBits |= 288230376151711744L;
                     } else if (methodBinding.defaultNullness != 0) {
                        methodBinding.tagBits |= 144115188075855872L;
                        if (methodBinding.defaultNullness == 1 && this.environment.usesNullTypeAnnotations()) {
                           methodBinding.defaultNullness |= 24;
                        }
                     }
                  } else if (typeBit == 32) {
                     methodBinding.tagBits |= 72057594037927936L;
                  } else if (typeBit == 64) {
                     methodBinding.tagBits |= 36028797018963968L;
                  }
               }
            }
         }

         TypeBinding[] parameters = methodBinding.parameters;
         int numVisibleParams = parameters.length;
         int numParamAnnotations = externalAnnotationWalker instanceof ExternalAnnotationProvider.IMethodAnnotationWalker
            ? ((ExternalAnnotationProvider.IMethodAnnotationWalker)externalAnnotationWalker).getParameterCount()
            : method.getAnnotatedParametersCount();
         if (numParamAnnotations > 0) {
            for(int j = 0; j < numVisibleParams; ++j) {
               if (numParamAnnotations > 0) {
                  int startIndex = numParamAnnotations - numVisibleParams;
                  ITypeAnnotationWalker parameterWalker = externalAnnotationWalker.toMethodParameter((short)(j + startIndex));
                  IBinaryAnnotation[] paramAnnotations = parameterWalker != ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
                     ? parameterWalker.getAnnotationsAtCursor(parameters[j].id)
                     : method.getParameterAnnotations(j + startIndex, this.fileName);
                  if (paramAnnotations != null) {
                     for(int i = 0; i < paramAnnotations.length; ++i) {
                        char[] annotationTypeName = paramAnnotations[i].getTypeName();
                        if (annotationTypeName[0] == 'L') {
                           int typeBit = this.environment.getNullAnnotationBit(this.signature2qualifiedTypeName(annotationTypeName));
                           if (typeBit == 32) {
                              if (methodBinding.parameterNonNullness == null) {
                                 methodBinding.parameterNonNullness = new Boolean[numVisibleParams];
                              }

                              methodBinding.parameterNonNullness[j] = Boolean.TRUE;
                              break;
                           }

                           if (typeBit == 64) {
                              if (methodBinding.parameterNonNullness == null) {
                                 methodBinding.parameterNonNullness = new Boolean[numVisibleParams];
                              }

                              methodBinding.parameterNonNullness[j] = Boolean.FALSE;
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (useNullTypeAnnotations && this.externalAnnotationStatus.isPotentiallyUnannotatedLib()) {
            if (methodBinding.returnType.hasNullTypeAnnotations()) {
               this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
            } else {
               for(TypeBinding parameter : parameters) {
                  if (parameter.hasNullTypeAnnotations()) {
                     this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
                     break;
                  }
               }
            }
         }
      }
   }

   private void scanTypeForNullDefaultAnnotation(IBinaryType binaryType, PackageBinding packageBinding) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         char[][] nonNullByDefaultAnnotationName = this.environment.getNonNullByDefaultAnnotationName();
         if (nonNullByDefaultAnnotationName != null) {
            if (!CharOperation.equals(CharOperation.splitOn('/', binaryType.getName()), nonNullByDefaultAnnotationName)) {
               IBinaryAnnotation[] annotations = binaryType.getAnnotations();
               boolean isPackageInfo = CharOperation.equals(this.sourceName(), TypeConstants.PACKAGE_INFO_NAME);
               if (annotations != null) {
                  long annotationBit = 0L;
                  int nullness = 0;
                  int length = annotations.length;

                  for(int i = 0; i < length; ++i) {
                     char[] annotationTypeName = annotations[i].getTypeName();
                     if (annotationTypeName[0] == 'L') {
                        int typeBit = this.environment.getNullAnnotationBit(this.signature2qualifiedTypeName(annotationTypeName));
                        if (typeBit == 128) {
                           nullness = this.getNonNullByDefaultValue(annotations[i]);
                           if (nullness == 2) {
                              annotationBit = 288230376151711744L;
                           } else if (nullness != 0) {
                              annotationBit = 144115188075855872L;
                              if (nullness == 1 && this.environment.usesNullTypeAnnotations()) {
                                 nullness |= 56;
                              }
                           }

                           this.defaultNullness = nullness;
                           break;
                        }
                     }
                  }

                  if (annotationBit != 0L) {
                     this.tagBits |= annotationBit;
                     if (isPackageInfo) {
                        packageBinding.defaultNullness = nullness;
                     }

                     return;
                  }
               }

               if (isPackageInfo) {
                  packageBinding.defaultNullness = 0;
               } else {
                  ReferenceBinding enclosingTypeBinding = this.enclosingType;
                  if (enclosingTypeBinding == null || !this.setNullDefault(enclosingTypeBinding.tagBits, enclosingTypeBinding.getNullDefault())) {
                     if (packageBinding.defaultNullness == 0 && !isPackageInfo) {
                        ReferenceBinding packageInfo = packageBinding.getType(TypeConstants.PACKAGE_INFO_NAME);
                        if (packageInfo == null) {
                           packageBinding.defaultNullness = 0;
                        }
                     }

                     this.setNullDefault(0L, packageBinding.defaultNullness);
                  }
               }
            }
         }
      }
   }

   boolean setNullDefault(long oldNullTagBits, int newNullDefault) {
      this.defaultNullness = newNullDefault;
      if (newNullDefault != 0) {
         if (newNullDefault == 2) {
            this.tagBits |= 288230376151711744L;
         } else {
            this.tagBits |= 144115188075855872L;
         }

         return true;
      } else if ((oldNullTagBits & 144115188075855872L) != 0L) {
         this.tagBits |= 144115188075855872L;
         return true;
      } else if ((oldNullTagBits & 288230376151711744L) != 0L) {
         this.tagBits |= 288230376151711744L;
         return true;
      } else {
         return false;
      }
   }

   int getNonNullByDefaultValue(IBinaryAnnotation annotation) {
      char[] annotationTypeName = annotation.getTypeName();
      char[][] typeName = this.signature2qualifiedTypeName(annotationTypeName);
      IBinaryElementValuePair[] elementValuePairs = annotation.getElementValuePairs();
      if (elementValuePairs == null || elementValuePairs.length == 0) {
         ReferenceBinding annotationType = this.environment.getType(typeName);
         if (annotationType == null) {
            return 0;
         } else {
            if (annotationType.isUnresolvedType()) {
               annotationType = ((UnresolvedReferenceBinding)annotationType).resolve(this.environment, false);
            }

            MethodBinding[] annotationMethods = annotationType.methods();
            if (annotationMethods != null && annotationMethods.length == 1) {
               Object value = annotationMethods[0].getDefaultValue();
               return Annotation.nullLocationBitsFromAnnotationValue(value);
            } else {
               return 1;
            }
         }
      } else if (elementValuePairs.length <= 0) {
         return 2;
      } else {
         int nullness = 0;

         for(int i = 0; i < elementValuePairs.length; ++i) {
            nullness |= Annotation.nullLocationBitsFromAnnotationValue(elementValuePairs[i].getValue());
         }

         return nullness;
      }
   }

   private char[][] signature2qualifiedTypeName(char[] typeSignature) {
      return CharOperation.splitOn('/', typeSignature, 1, typeSignature.length - 1);
   }

   @Override
   int getNullDefault() {
      return this.defaultNullness;
   }

   private void scanTypeForContainerAnnotation(IBinaryType binaryType, char[][][] missingTypeNames) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         IBinaryAnnotation[] annotations = binaryType.getAnnotations();
         if (annotations != null) {
            int length = annotations.length;

            for(int i = 0; i < length; ++i) {
               char[] annotationTypeName = annotations[i].getTypeName();
               if (CharOperation.equals(annotationTypeName, ConstantPool.JAVA_LANG_ANNOTATION_REPEATABLE)) {
                  IBinaryElementValuePair[] elementValuePairs = annotations[i].getElementValuePairs();
                  if (elementValuePairs != null && elementValuePairs.length == 1) {
                     Object value = elementValuePairs[0].getValue();
                     if (value instanceof ClassSignature) {
                        this.containerAnnotationType = (ReferenceBinding)this.environment
                           .getTypeFromSignature(
                              ((ClassSignature)value).getTypeName(), 0, -1, false, null, missingTypeNames, ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
                           );
                     }
                  }
                  break;
               }
            }
         }
      }
   }

   @Override
   public ReferenceBinding superclass() {
      if (!this.isPrototype()) {
         return this.superclass = this.prototype.superclass();
      } else if ((this.tagBits & 33554432L) == 0L) {
         return this.superclass;
      } else {
         this.superclass = (ReferenceBinding)resolveType(this.superclass, this.environment, true);
         this.tagBits &= -33554433L;
         if (this.superclass.problemId() == 1) {
            this.tagBits |= 131072L;
         } else {
            boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
            this.environment.mayTolerateMissingType = true;

            try {
               this.superclass.superclass();
               this.superclass.superInterfaces();
            } finally {
               this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
            }
         }

         this.typeBits |= this.superclass.typeBits & 19;
         if ((this.typeBits & 3) != 0) {
            this.typeBits |= this.applyCloseableClassWhitelists();
         }

         return this.superclass;
      }
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      if (!this.isPrototype()) {
         return this.superInterfaces = this.prototype.superInterfaces();
      } else if ((this.tagBits & 67108864L) == 0L) {
         return this.superInterfaces;
      } else {
         int i = this.superInterfaces.length;

         while(--i >= 0) {
            this.superInterfaces[i] = (ReferenceBinding)resolveType(this.superInterfaces[i], this.environment, true);
            if (this.superInterfaces[i].problemId() == 1) {
               this.tagBits |= 131072L;
            } else {
               boolean wasToleratingMissingTypeProcessingAnnotations = this.environment.mayTolerateMissingType;
               this.environment.mayTolerateMissingType = true;

               try {
                  this.superInterfaces[i].superclass();
                  if (this.superInterfaces[i].isParameterizedType()) {
                     ReferenceBinding superType = this.superInterfaces[i].actualType();
                     if (TypeBinding.equalsEquals(superType, this)) {
                        this.tagBits |= 131072L;
                        continue;
                     }
                  }

                  this.superInterfaces[i].superInterfaces();
               } finally {
                  this.environment.mayTolerateMissingType = wasToleratingMissingTypeProcessingAnnotations;
               }
            }

            this.typeBits |= this.superInterfaces[i].typeBits & 19;
            if ((this.typeBits & 3) != 0) {
               this.typeBits |= this.applyCloseableInterfaceWhitelists();
            }
         }

         this.tagBits &= -67108865L;
         return this.superInterfaces;
      }
   }

   @Override
   public TypeVariableBinding[] typeVariables() {
      if (!this.isPrototype()) {
         return this.typeVariables = this.prototype.typeVariables();
      } else if ((this.tagBits & 16777216L) == 0L) {
         return this.typeVariables;
      } else {
         int i = this.typeVariables.length;

         while(--i >= 0) {
            this.typeVariables[i].resolve();
         }

         this.tagBits &= -16777217L;
         return this.typeVariables;
      }
   }

   @Override
   public String toString() {
      if (this.hasTypeAnnotations()) {
         return this.annotatedDebugName();
      } else {
         StringBuffer buffer = new StringBuffer();
         if (this.isDeprecated()) {
            buffer.append("deprecated ");
         }

         if (this.isPublic()) {
            buffer.append("public ");
         }

         if (this.isProtected()) {
            buffer.append("protected ");
         }

         if (this.isPrivate()) {
            buffer.append("private ");
         }

         if (this.isAbstract() && this.isClass()) {
            buffer.append("abstract ");
         }

         if (this.isStatic() && this.isNestedType()) {
            buffer.append("static ");
         }

         if (this.isFinal()) {
            buffer.append("final ");
         }

         if (this.isEnum()) {
            buffer.append("enum ");
         } else if (this.isAnnotationType()) {
            buffer.append("@interface ");
         } else if (this.isClass()) {
            buffer.append("class ");
         } else {
            buffer.append("interface ");
         }

         buffer.append(this.compoundName != null ? CharOperation.toString(this.compoundName) : "UNNAMED TYPE");
         if (this.typeVariables == null) {
            buffer.append("<NULL TYPE VARIABLES>");
         } else if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
            buffer.append("<");
            int i = 0;

            for(int length = this.typeVariables.length; i < length; ++i) {
               if (i > 0) {
                  buffer.append(", ");
               }

               if (this.typeVariables[i] == null) {
                  buffer.append("NULL TYPE VARIABLE");
               } else {
                  char[] varChars = this.typeVariables[i].toString().toCharArray();
                  buffer.append(varChars, 1, varChars.length - 2);
               }
            }

            buffer.append(">");
         }

         buffer.append("\n\textends ");
         buffer.append(this.superclass != null ? this.superclass.debugName() : "NULL TYPE");
         if (this.superInterfaces != null) {
            if (this.superInterfaces != Binding.NO_SUPERINTERFACES) {
               buffer.append("\n\timplements : ");
               int i = 0;

               for(int length = this.superInterfaces.length; i < length; ++i) {
                  if (i > 0) {
                     buffer.append(", ");
                  }

                  buffer.append(this.superInterfaces[i] != null ? this.superInterfaces[i].debugName() : "NULL TYPE");
               }
            }
         } else {
            buffer.append("NULL SUPERINTERFACES");
         }

         if (this.enclosingType != null) {
            buffer.append("\n\tenclosing type : ");
            buffer.append(this.enclosingType.debugName());
         }

         if (this.fields != null) {
            if (this.fields != Binding.NO_FIELDS) {
               buffer.append("\n/*   fields   */");
               int i = 0;

               for(int length = this.fields.length; i < length; ++i) {
                  buffer.append(this.fields[i] != null ? "\n" + this.fields[i].toString() : "\nNULL FIELD");
               }
            }
         } else {
            buffer.append("NULL FIELDS");
         }

         if (this.methods != null) {
            if (this.methods != Binding.NO_METHODS) {
               buffer.append("\n/*   methods   */");
               int i = 0;

               for(int length = this.methods.length; i < length; ++i) {
                  buffer.append(this.methods[i] != null ? "\n" + this.methods[i].toString() : "\nNULL METHOD");
               }
            }
         } else {
            buffer.append("NULL METHODS");
         }

         if (this.memberTypes != null) {
            if (this.memberTypes != Binding.NO_MEMBER_TYPES) {
               buffer.append("\n/*   members   */");
               int i = 0;

               for(int length = this.memberTypes.length; i < length; ++i) {
                  buffer.append(this.memberTypes[i] != null ? "\n" + this.memberTypes[i].toString() : "\nNULL TYPE");
               }
            }
         } else {
            buffer.append("NULL MEMBER TYPES");
         }

         buffer.append("\n\n\n");
         return buffer.toString();
      }
   }

   @Override
   public TypeBinding unannotated() {
      return this.prototype;
   }

   @Override
   public TypeBinding withoutToplevelNullAnnotation() {
      if (!this.hasNullTypeAnnotations()) {
         return this;
      } else {
         AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.typeAnnotations);
         return (TypeBinding)(newAnnotations.length > 0 ? this.environment.createAnnotatedType(this.prototype, newAnnotations) : this.prototype);
      }
   }

   @Override
   MethodBinding[] unResolvedMethods() {
      return !this.isPrototype() ? this.prototype.unResolvedMethods() : this.methods;
   }

   @Override
   public FieldBinding[] unResolvedFields() {
      return !this.isPrototype() ? this.prototype.unResolvedFields() : this.fields;
   }

   public static enum ExternalAnnotationStatus {
      FROM_SOURCE,
      NOT_EEA_CONFIGURED,
      NO_EEA_FILE,
      TYPE_IS_ANNOTATED;

      public boolean isPotentiallyUnannotatedLib() {
         switch(this) {
            case FROM_SOURCE:
            case TYPE_IS_ANNOTATED:
               return false;
            case NOT_EEA_CONFIGURED:
            case NO_EEA_FILE:
            default:
               return true;
         }
      }
   }
}
