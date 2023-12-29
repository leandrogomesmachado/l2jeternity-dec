package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

public class Factory {
   public static final Byte DUMMY_BYTE = (byte)0;
   public static final Character DUMMY_CHAR = '0';
   public static final Double DUMMY_DOUBLE = 0.0;
   public static final Float DUMMY_FLOAT = 0.0F;
   public static final Integer DUMMY_INTEGER = 0;
   public static final Long DUMMY_LONG = 0L;
   public static final Short DUMMY_SHORT = (short)0;
   private final BaseProcessingEnvImpl _env;
   public static List<? extends AnnotationMirror> EMPTY_ANNOTATION_MIRRORS = Collections.emptyList();

   public Factory(BaseProcessingEnvImpl env) {
      this._env = env;
   }

   public List<? extends AnnotationMirror> getAnnotationMirrors(AnnotationBinding[] annotations) {
      if (annotations != null && annotations.length != 0) {
         List<AnnotationMirror> list = new ArrayList<>(annotations.length);

         for(AnnotationBinding annotation : annotations) {
            if (annotation != null) {
               list.add(this.newAnnotationMirror(annotation));
            }
         }

         return Collections.unmodifiableList(list);
      } else {
         return Collections.emptyList();
      }
   }

   public <A extends Annotation> A[] getAnnotationsByType(AnnotationBinding[] annoInstances, Class<A> annotationClass) {
      Annotation[] result = this.getAnnotations(annoInstances, annotationClass, false);
      return (A[])(result == null ? (Annotation[])Array.newInstance(annotationClass, 0) : result);
   }

   public <A extends Annotation> A getAnnotation(AnnotationBinding[] annoInstances, Class<A> annotationClass) {
      Annotation[] result = this.getAnnotations(annoInstances, annotationClass, true);
      return (A)(result == null ? null : result[0]);
   }

   private <A extends Annotation> A[] getAnnotations(AnnotationBinding[] annoInstances, Class<A> annotationClass, boolean justTheFirst) {
      if (annoInstances != null && annoInstances.length != 0 && annotationClass != null) {
         String annoTypeName = annotationClass.getName();
         if (annoTypeName == null) {
            return null;
         } else {
            List<A> list = new ArrayList<>(annoInstances.length);

            for(AnnotationBinding annoInstance : annoInstances) {
               if (annoInstance != null) {
                  AnnotationMirrorImpl annoMirror = this.createAnnotationMirror(annoTypeName, annoInstance);
                  if (annoMirror != null) {
                     list.add((A)Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[]{annotationClass}, annoMirror));
                     if (justTheFirst) {
                        break;
                     }
                  }
               }
            }

            Annotation[] result = (Annotation[])Array.newInstance(annotationClass, list.size());
            return (A[])(list.size() > 0 ? list.toArray(result) : null);
         }
      } else {
         return null;
      }
   }

   private AnnotationMirrorImpl createAnnotationMirror(String annoTypeName, AnnotationBinding annoInstance) {
      ReferenceBinding binding = annoInstance.getAnnotationType();
      if (binding != null && binding.isAnnotationType()) {
         char[] qName;
         if (binding.isMemberType()) {
            annoTypeName = annoTypeName.replace('$', '.');
            qName = CharOperation.concatWith(binding.enclosingType().compoundName, binding.sourceName, '.');
            CharOperation.replace(qName, '$', '.');
         } else {
            qName = CharOperation.concatWith(binding.compoundName, '.');
         }

         if (annoTypeName.equals(new String(qName))) {
            return (AnnotationMirrorImpl)this._env.getFactory().newAnnotationMirror(annoInstance);
         }
      }

      return null;
   }

   private static void appendModifier(Set<Modifier> result, int modifiers, int modifierConstant, Modifier modifier) {
      if ((modifiers & modifierConstant) != 0) {
         result.add(modifier);
      }
   }

   private static void decodeModifiers(Set<Modifier> result, int modifiers, int[] checkBits) {
      if (checkBits != null) {
         int i = 0;

         for(int max = checkBits.length; i < max; ++i) {
            switch(checkBits[i]) {
               case 1:
                  appendModifier(result, modifiers, checkBits[i], Modifier.PUBLIC);
                  break;
               case 2:
                  appendModifier(result, modifiers, checkBits[i], Modifier.PRIVATE);
                  break;
               case 4:
                  appendModifier(result, modifiers, checkBits[i], Modifier.PROTECTED);
                  break;
               case 8:
                  appendModifier(result, modifiers, checkBits[i], Modifier.STATIC);
                  break;
               case 16:
                  appendModifier(result, modifiers, checkBits[i], Modifier.FINAL);
                  break;
               case 32:
                  appendModifier(result, modifiers, checkBits[i], Modifier.SYNCHRONIZED);
                  break;
               case 64:
                  appendModifier(result, modifiers, checkBits[i], Modifier.VOLATILE);
                  break;
               case 128:
                  appendModifier(result, modifiers, checkBits[i], Modifier.TRANSIENT);
                  break;
               case 256:
                  appendModifier(result, modifiers, checkBits[i], Modifier.NATIVE);
                  break;
               case 1024:
                  appendModifier(result, modifiers, checkBits[i], Modifier.ABSTRACT);
                  break;
               case 2048:
                  appendModifier(result, modifiers, checkBits[i], Modifier.STRICTFP);
                  break;
               case 65536:
                  try {
                     appendModifier(result, modifiers, checkBits[i], Modifier.valueOf("DEFAULT"));
                  } catch (IllegalArgumentException var5) {
                  }
            }
         }
      }
   }

   public static Object getMatchingDummyValue(Class<?> expectedType) {
      if (expectedType.isPrimitive()) {
         if (expectedType == Boolean.TYPE) {
            return Boolean.FALSE;
         } else if (expectedType == Byte.TYPE) {
            return DUMMY_BYTE;
         } else if (expectedType == Character.TYPE) {
            return DUMMY_CHAR;
         } else if (expectedType == Double.TYPE) {
            return DUMMY_DOUBLE;
         } else if (expectedType == Float.TYPE) {
            return DUMMY_FLOAT;
         } else if (expectedType == Integer.TYPE) {
            return DUMMY_INTEGER;
         } else if (expectedType == Long.TYPE) {
            return DUMMY_LONG;
         } else {
            return expectedType == Short.TYPE ? DUMMY_SHORT : DUMMY_INTEGER;
         }
      } else {
         return null;
      }
   }

   public TypeMirror getReceiverType(MethodBinding binding) {
      if (binding != null) {
         if (binding.receiver != null) {
            return this._env.getFactory().newTypeMirror(binding.receiver);
         }

         if (binding.declaringClass != null && !binding.isStatic() && (!binding.isConstructor() || binding.declaringClass.isMemberType())) {
            return this._env.getFactory().newTypeMirror(binding.declaringClass);
         }
      }

      return NoTypeImpl.NO_TYPE_NONE;
   }

   public static Set<Modifier> getModifiers(int modifiers, ElementKind kind) {
      return getModifiers(modifiers, kind, false);
   }

   public static Set<Modifier> getModifiers(int modifiers, ElementKind kind, boolean isFromBinary) {
      EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);
      switch(kind) {
         case ENUM:
            if (isFromBinary) {
               decodeModifiers(result, modifiers, new int[]{1, 4, 16, 2, 1024, 8, 2048});
            } else {
               decodeModifiers(result, modifiers, new int[]{1, 4, 16, 2, 8, 2048});
            }
            break;
         case CLASS:
         case ANNOTATION_TYPE:
         case INTERFACE:
            decodeModifiers(result, modifiers, new int[]{1, 4, 1024, 16, 2, 8, 2048});
            break;
         case ENUM_CONSTANT:
         case FIELD:
            decodeModifiers(result, modifiers, new int[]{1, 4, 2, 8, 16, 128, 64});
         case PARAMETER:
         case LOCAL_VARIABLE:
         case EXCEPTION_PARAMETER:
         default:
            break;
         case METHOD:
         case CONSTRUCTOR:
            decodeModifiers(result, modifiers, new int[]{1, 4, 2, 1024, 8, 16, 32, 256, 2048, 65536});
      }

      return Collections.unmodifiableSet(result);
   }

   public AnnotationMirror newAnnotationMirror(AnnotationBinding binding) {
      return new AnnotationMirrorImpl(this._env, binding);
   }

   public Element newElement(Binding binding, ElementKind kindHint) {
      if (binding == null) {
         return null;
      } else {
         switch(binding.kind()) {
            case 1:
            case 2:
            case 3:
               return new VariableElementImpl(this._env, (VariableBinding)binding);
            case 4:
            case 2052:
               ReferenceBinding referenceBinding = (ReferenceBinding)binding;
               if ((referenceBinding.tagBits & 128L) != 0L) {
                  return new ErrorTypeElement(this._env, referenceBinding);
               } else {
                  if (CharOperation.equals(referenceBinding.sourceName, TypeConstants.PACKAGE_INFO_NAME)) {
                     return new PackageElementImpl(this._env, referenceBinding.fPackage);
                  }

                  return new TypeElementImpl(this._env, referenceBinding, kindHint);
               }
            case 8:
               return new ExecutableElementImpl(this._env, (MethodBinding)binding);
            case 16:
               return new PackageElementImpl(this._env, (PackageBinding)binding);
            case 32:
            case 68:
            case 132:
            case 516:
            case 8196:
               throw new UnsupportedOperationException("NYI: binding type " + binding.kind());
            case 260:
            case 1028:
               return new TypeElementImpl(this._env, ((ParameterizedTypeBinding)binding).genericType(), kindHint);
            case 4100:
               return new TypeParameterElementImpl(this._env, (TypeVariableBinding)binding);
            default:
               return null;
         }
      }
   }

   public Element newElement(Binding binding) {
      return this.newElement(binding, null);
   }

   public PackageElement newPackageElement(PackageBinding binding) {
      return new PackageElementImpl(this._env, binding);
   }

   public NullType getNullType() {
      return NoTypeImpl.NULL_TYPE;
   }

   public NoType getNoType(TypeKind kind) {
      switch(kind) {
         case VOID:
            return NoTypeImpl.NO_TYPE_VOID;
         case NONE:
            return NoTypeImpl.NO_TYPE_NONE;
         case PACKAGE:
            return NoTypeImpl.NO_TYPE_PACKAGE;
         default:
            throw new IllegalArgumentException();
      }
   }

   public PrimitiveTypeImpl getPrimitiveType(TypeKind kind) {
      switch(kind) {
         case BOOLEAN:
            return PrimitiveTypeImpl.BOOLEAN;
         case BYTE:
            return PrimitiveTypeImpl.BYTE;
         case SHORT:
            return PrimitiveTypeImpl.SHORT;
         case INT:
            return PrimitiveTypeImpl.INT;
         case LONG:
            return PrimitiveTypeImpl.LONG;
         case CHAR:
            return PrimitiveTypeImpl.CHAR;
         case FLOAT:
            return PrimitiveTypeImpl.FLOAT;
         case DOUBLE:
            return PrimitiveTypeImpl.DOUBLE;
         default:
            throw new IllegalArgumentException();
      }
   }

   public PrimitiveTypeImpl getPrimitiveType(BaseTypeBinding binding) {
      AnnotationBinding[] annotations = binding.getTypeAnnotations();
      return annotations != null && annotations.length != 0
         ? new PrimitiveTypeImpl(this._env, binding)
         : this.getPrimitiveType(PrimitiveTypeImpl.getKind(binding));
   }

   public TypeMirror newTypeMirror(Binding binding) {
      switch(binding.kind()) {
         case 1:
         case 2:
         case 3:
            return this.newTypeMirror(((VariableBinding)binding).type);
         case 4:
         case 260:
         case 1028:
         case 2052:
            ReferenceBinding referenceBinding = (ReferenceBinding)binding;
            if ((referenceBinding.tagBits & 128L) != 0L) {
               return this.getErrorType(referenceBinding);
            }

            return new DeclaredTypeImpl(this._env, (ReferenceBinding)binding);
         case 8:
            return new ExecutableTypeImpl(this._env, (MethodBinding)binding);
         case 16:
            return this.getNoType(TypeKind.PACKAGE);
         case 32:
            throw new UnsupportedOperationException("NYI: import type " + binding.kind());
         case 68:
            return new ArrayTypeImpl(this._env, (ArrayBinding)binding);
         case 132:
            BaseTypeBinding btb = (BaseTypeBinding)binding;
            switch(btb.id) {
               case 6:
                  return this.getNoType(TypeKind.VOID);
               case 12:
                  return this.getNullType();
               default:
                  return this.getPrimitiveType(btb);
            }
         case 516:
         case 8196:
            return new WildcardTypeImpl(this._env, (WildcardBinding)binding);
         case 4100:
            return new TypeVariableImpl(this._env, (TypeVariableBinding)binding);
         default:
            return null;
      }
   }

   public TypeParameterElement newTypeParameterElement(TypeVariableBinding variable, Element declaringElement) {
      return new TypeParameterElementImpl(this._env, variable, declaringElement);
   }

   public ErrorType getErrorType(ReferenceBinding binding) {
      return new ErrorTypeImpl(this._env, binding);
   }

   public static Object performNecessaryPrimitiveTypeConversion(Class<?> expectedType, Object value, boolean avoidReflectException) {
      assert expectedType.isPrimitive() : "expectedType is not a primitive type: " + expectedType.getName();

      if (value == null) {
         return avoidReflectException ? getMatchingDummyValue(expectedType) : null;
      } else {
         String typeName = expectedType.getName();
         char expectedTypeChar = typeName.charAt(0);
         int nameLen = typeName.length();
         if (value instanceof Byte) {
            byte b = (Byte)value;
            switch(expectedTypeChar) {
               case 'b':
                  if (nameLen == 4) {
                     return value;
                  }

                  return avoidReflectException ? Boolean.FALSE : value;
               case 'c':
                  return (char)b;
               case 'd':
                  return new Double((double)b);
               case 'f':
                  return new Float((float)b);
               case 'i':
                  return Integer.valueOf(b);
               case 'l':
                  return (long)b;
               case 's':
                  return Short.valueOf(b);
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Short) {
            short s = (Short)value;
            switch(expectedTypeChar) {
               case 'b':
                  if (nameLen == 4) {
                     return (byte)s;
                  }

                  return avoidReflectException ? Boolean.FALSE : value;
               case 'c':
                  return (char)s;
               case 'd':
                  return new Double((double)s);
               case 'f':
                  return new Float((float)s);
               case 'i':
                  return Integer.valueOf(s);
               case 'l':
                  return (long)s;
               case 's':
                  return value;
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Character) {
            char c = (Character)value;
            switch(expectedTypeChar) {
               case 'b':
                  if (nameLen == 4) {
                     return (byte)c;
                  }

                  return avoidReflectException ? Boolean.FALSE : value;
               case 'c':
                  return value;
               case 'd':
                  return new Double((double)c);
               case 'f':
                  return new Float((float)c);
               case 'i':
                  return Integer.valueOf(c);
               case 'l':
                  return (long)c;
               case 's':
                  return (short)c;
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Integer) {
            int i = (Integer)value;
            switch(expectedTypeChar) {
               case 'b':
                  if (nameLen == 4) {
                     return (byte)i;
                  }

                  return avoidReflectException ? Boolean.FALSE : value;
               case 'c':
                  return (char)i;
               case 'd':
                  return new Double((double)i);
               case 'f':
                  return new Float((float)i);
               case 'i':
                  return value;
               case 'l':
                  return (long)i;
               case 's':
                  return (short)i;
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Long) {
            long l = (Long)value;
            switch(expectedTypeChar) {
               case 'b':
               case 'c':
               case 'i':
               case 's':
                  return avoidReflectException ? getMatchingDummyValue(expectedType) : value;
               case 'd':
                  return new Double((double)l);
               case 'f':
                  return new Float((float)l);
               case 'l':
                  return value;
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Float) {
            float f = (Float)value;
            switch(expectedTypeChar) {
               case 'b':
               case 'c':
               case 'i':
               case 'l':
               case 's':
                  return avoidReflectException ? getMatchingDummyValue(expectedType) : value;
               case 'd':
                  return new Double((double)f);
               case 'f':
                  return value;
               default:
                  throw new IllegalStateException("unknown type " + expectedTypeChar);
            }
         } else if (value instanceof Double) {
            if (expectedTypeChar == 'd') {
               return value;
            } else {
               return avoidReflectException ? getMatchingDummyValue(expectedType) : value;
            }
         } else if (value instanceof Boolean) {
            if (expectedTypeChar == 'b' && nameLen == 7) {
               return value;
            } else {
               return avoidReflectException ? getMatchingDummyValue(expectedType) : value;
            }
         } else {
            return avoidReflectException ? getMatchingDummyValue(expectedType) : value;
         }
      }
   }

   public static void setArrayMatchingDummyValue(Object array, int i, Class<?> expectedLeafType) {
      if (Boolean.TYPE.equals(expectedLeafType)) {
         Array.setBoolean(array, i, false);
      } else if (Byte.TYPE.equals(expectedLeafType)) {
         Array.setByte(array, i, DUMMY_BYTE);
      } else if (Character.TYPE.equals(expectedLeafType)) {
         Array.setChar(array, i, DUMMY_CHAR);
      } else if (Double.TYPE.equals(expectedLeafType)) {
         Array.setDouble(array, i, DUMMY_DOUBLE);
      } else if (Float.TYPE.equals(expectedLeafType)) {
         Array.setFloat(array, i, DUMMY_FLOAT);
      } else if (Integer.TYPE.equals(expectedLeafType)) {
         Array.setInt(array, i, DUMMY_INTEGER);
      } else if (Long.TYPE.equals(expectedLeafType)) {
         Array.setLong(array, i, DUMMY_LONG);
      } else if (Short.TYPE.equals(expectedLeafType)) {
         Array.setShort(array, i, DUMMY_SHORT);
      } else {
         Array.set(array, i, null);
      }
   }

   public static AnnotationBinding[] getPackedAnnotationBindings(AnnotationBinding[] annotations) {
      int length = annotations == null ? 0 : annotations.length;
      if (length == 0) {
         return annotations;
      } else {
         AnnotationBinding[] repackagedBindings = annotations;

         for(int i = 0; i < length; ++i) {
            AnnotationBinding annotation = repackagedBindings[i];
            if (annotation != null) {
               ReferenceBinding annotationType = annotation.getAnnotationType();
               if (annotationType.isRepeatableAnnotationType()) {
                  ReferenceBinding containerType = annotationType.containerAnnotationType();
                  if (containerType != null) {
                     MethodBinding[] values = containerType.getMethods(TypeConstants.VALUE);
                     if (values != null && values.length == 1) {
                        MethodBinding value = values[0];
                        if (value.returnType != null
                           && value.returnType.dimensions() == 1
                           && !TypeBinding.notEquals(value.returnType.leafComponentType(), annotationType)) {
                           List<AnnotationBinding> containees = null;

                           for(int j = i + 1; j < length; ++j) {
                              AnnotationBinding otherAnnotation = repackagedBindings[j];
                              if (otherAnnotation != null && otherAnnotation.getAnnotationType() == annotationType) {
                                 if (repackagedBindings == annotations) {
                                    System.arraycopy(repackagedBindings, 0, repackagedBindings = new AnnotationBinding[length], 0, length);
                                 }

                                 repackagedBindings[j] = null;
                                 if (containees == null) {
                                    containees = new ArrayList<>();
                                    containees.add(annotation);
                                 }

                                 containees.add(otherAnnotation);
                              }
                           }

                           if (containees != null) {
                              ElementValuePair[] elementValuePairs = new ElementValuePair[]{
                                 new ElementValuePair(TypeConstants.VALUE, containees.toArray(), value)
                              };
                              repackagedBindings[i] = new AnnotationBinding(containerType, elementValuePairs);
                           }
                        }
                     }
                  }
               }
            }
         }

         int finalTally = 0;

         for(int i = 0; i < length; ++i) {
            if (repackagedBindings[i] != null) {
               ++finalTally;
            }
         }

         if (repackagedBindings == annotations && finalTally == length) {
            return annotations;
         } else {
            annotations = new AnnotationBinding[finalTally];
            int i = 0;

            for(int j = 0; i < length; ++i) {
               if (repackagedBindings[i] != null) {
                  annotations[j++] = repackagedBindings[i];
               }
            }

            return annotations;
         }
      }
   }

   public static AnnotationBinding[] getUnpackedAnnotationBindings(AnnotationBinding[] annotations) {
      int length = annotations == null ? 0 : annotations.length;
      if (length == 0) {
         return annotations;
      } else {
         List<AnnotationBinding> unpackedAnnotations = new ArrayList<>();

         for(int i = 0; i < length; ++i) {
            AnnotationBinding annotation = annotations[i];
            if (annotation != null) {
               unpackedAnnotations.add(annotation);
               ReferenceBinding annotationType = annotation.getAnnotationType();
               MethodBinding[] values = annotationType.getMethods(TypeConstants.VALUE);
               if (values != null && values.length == 1) {
                  MethodBinding value = values[0];
                  if (value.returnType.dimensions() == 1) {
                     TypeBinding containeeType = value.returnType.leafComponentType();
                     if (containeeType != null
                        && containeeType.isAnnotationType()
                        && containeeType.isRepeatableAnnotationType()
                        && containeeType.containerAnnotationType() == annotationType) {
                        ElementValuePair[] elementValuePairs = annotation.getElementValuePairs();

                        for(ElementValuePair elementValuePair : elementValuePairs) {
                           if (CharOperation.equals(elementValuePair.getName(), TypeConstants.VALUE)) {
                              Object[] containees = (Object[])elementValuePair.getValue();

                              for(Object object : containees) {
                                 unpackedAnnotations.add((AnnotationBinding)object);
                              }
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         return unpackedAnnotations.toArray(new AnnotationBinding[unpackedAnnotations.size()]);
      }
   }
}
