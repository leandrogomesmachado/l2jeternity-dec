package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class AnnotationMirrorImpl implements AnnotationMirror, InvocationHandler {
   public final BaseProcessingEnvImpl _env;
   public final AnnotationBinding _binding;

   AnnotationMirrorImpl(BaseProcessingEnvImpl env, AnnotationBinding binding) {
      this._env = env;
      this._binding = binding;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof AnnotationMirrorImpl) {
         if (this._binding == null) {
            return ((AnnotationMirrorImpl)obj)._binding == null;
         } else {
            return equals(this._binding, ((AnnotationMirrorImpl)obj)._binding);
         }
      } else {
         return obj == null ? false : obj.equals(this);
      }
   }

   private static boolean equals(AnnotationBinding annotationBinding, AnnotationBinding annotationBinding2) {
      if (annotationBinding.getAnnotationType() != annotationBinding2.getAnnotationType()) {
         return false;
      } else {
         ElementValuePair[] elementValuePairs = annotationBinding.getElementValuePairs();
         ElementValuePair[] elementValuePairs2 = annotationBinding2.getElementValuePairs();
         int length = elementValuePairs.length;
         if (length != elementValuePairs2.length) {
            return false;
         } else {
            label55:
            for(int i = 0; i < length; ++i) {
               ElementValuePair pair = elementValuePairs[i];

               for(int j = 0; j < length; ++j) {
                  ElementValuePair pair2 = elementValuePairs2[j];
                  if (pair.binding == pair2.binding) {
                     if (pair.value == null) {
                        if (pair2.value != null) {
                           return false;
                        }
                     } else {
                        if (pair2.value == null) {
                           return false;
                        }

                        if (pair2.value instanceof Object[] && pair.value instanceof Object[]) {
                           if (!Arrays.equals(pair.value, pair2.value)) {
                              return false;
                           }
                        } else if (!pair2.value.equals(pair.value)) {
                           return false;
                        }
                     }
                     continue label55;
                  }
               }

               return false;
            }

            return true;
         }
      }
   }

   @Override
   public DeclaredType getAnnotationType() {
      return (DeclaredType)this._env.getFactory().newTypeMirror(this._binding.getAnnotationType());
   }

   @Override
   public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
      if (this._binding == null) {
         return Collections.emptyMap();
      } else {
         ElementValuePair[] pairs = this._binding.getElementValuePairs();
         Map<ExecutableElement, AnnotationValue> valueMap = new LinkedHashMap<>(pairs.length);

         for(ElementValuePair pair : pairs) {
            MethodBinding method = pair.getMethodBinding();
            if (method != null) {
               ExecutableElement e = new ExecutableElementImpl(this._env, method);
               AnnotationValue v = new AnnotationMemberValue(this._env, pair.getValue(), method);
               valueMap.put(e, v);
            }
         }

         return Collections.unmodifiableMap(valueMap);
      }
   }

   public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults() {
      if (this._binding == null) {
         return Collections.emptyMap();
      } else {
         ElementValuePair[] pairs = this._binding.getElementValuePairs();
         ReferenceBinding annoType = this._binding.getAnnotationType();
         Map<ExecutableElement, AnnotationValue> valueMap = new LinkedHashMap<>();

         MethodBinding[] var7;
         for(MethodBinding method : var7 = annoType.methods()) {
            boolean foundExplicitValue = false;

            for(int i = 0; i < pairs.length; ++i) {
               MethodBinding explicitBinding = pairs[i].getMethodBinding();
               if (method == explicitBinding) {
                  ExecutableElement e = new ExecutableElementImpl(this._env, explicitBinding);
                  AnnotationValue v = new AnnotationMemberValue(this._env, pairs[i].getValue(), explicitBinding);
                  valueMap.put(e, v);
                  foundExplicitValue = true;
                  break;
               }
            }

            if (!foundExplicitValue) {
               Object defaultVal = method.getDefaultValue();
               if (defaultVal != null) {
                  ExecutableElement e = new ExecutableElementImpl(this._env, method);
                  AnnotationValue v = new AnnotationMemberValue(this._env, defaultVal, method);
                  valueMap.put(e, v);
               }
            }
         }

         return Collections.unmodifiableMap(valueMap);
      }
   }

   @Override
   public int hashCode() {
      return this._binding == null ? this._env.hashCode() : this._binding.hashCode();
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (this._binding == null) {
         return null;
      } else {
         String methodName = method.getName();
         if (args != null && args.length != 0) {
            if (args.length == 1 && methodName.equals("equals")) {
               return this.equals(args[0]);
            }
         } else {
            if (methodName.equals("hashCode")) {
               return this.hashCode();
            }

            if (methodName.equals("toString")) {
               return this.toString();
            }

            if (methodName.equals("annotationType")) {
               return proxy.getClass().getInterfaces()[0];
            }
         }

         if (args != null && args.length != 0) {
            throw new NoSuchMethodException("method " + method.getName() + this.formatArgs(args) + " does not exist on annotation " + this.toString());
         } else {
            MethodBinding methodBinding = this.getMethodBinding(methodName);
            if (methodBinding == null) {
               throw new NoSuchMethodException("method " + method.getName() + "() does not exist on annotation" + this.toString());
            } else {
               Object actualValue = null;
               boolean foundMethod = false;
               ElementValuePair[] pairs = this._binding.getElementValuePairs();

               for(ElementValuePair pair : pairs) {
                  if (methodName.equals(new String(pair.getName()))) {
                     actualValue = pair.getValue();
                     foundMethod = true;
                     break;
                  }
               }

               if (!foundMethod) {
                  actualValue = methodBinding.getDefaultValue();
               }

               Class<?> expectedType = method.getReturnType();
               TypeBinding actualType = methodBinding.returnType;
               return this.getReflectionValue(actualValue, actualType, expectedType);
            }
         }
      }
   }

   @Override
   public String toString() {
      TypeMirror decl = this.getAnnotationType();
      StringBuilder sb = new StringBuilder();
      sb.append('@');
      sb.append(decl.toString());
      Map<? extends ExecutableElement, ? extends AnnotationValue> values = this.getElementValues();
      if (!values.isEmpty()) {
         sb.append('(');
         boolean first = true;

         for(Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
            if (!first) {
               sb.append(", ");
            }

            first = false;
            sb.append((CharSequence)e.getKey().getSimpleName());
            sb.append(" = ");
            sb.append(e.getValue().toString());
         }

         sb.append(')');
      }

      return sb.toString();
   }

   private String formatArgs(Object[] args) {
      StringBuilder builder = new StringBuilder(args.length * 8 + 2);
      builder.append('(');

      for(int i = 0; i < args.length; ++i) {
         if (i > 0) {
            builder.append(", ");
         }

         builder.append(args[i].getClass().getName());
      }

      builder.append(')');
      return builder.toString();
   }

   private MethodBinding getMethodBinding(String name) {
      ReferenceBinding annoType = this._binding.getAnnotationType();
      MethodBinding[] methods = annoType.getMethods(name.toCharArray());

      for(MethodBinding method : methods) {
         if (method.parameters.length == 0) {
            return method;
         }
      }

      return null;
   }

   private Object getReflectionValue(Object actualValue, TypeBinding actualType, Class<?> expectedType) {
      if (expectedType == null) {
         return null;
      } else if (actualValue == null) {
         return Factory.getMatchingDummyValue(expectedType);
      } else if (expectedType.isArray()) {
         if (Class.class.equals(expectedType.getComponentType())) {
            if (actualType.isArrayType() && actualValue instanceof Object[] && ((ArrayBinding)actualType).leafComponentType.erasure().id == 16) {
               Object[] bindings = (Object[])actualValue;
               List<TypeMirror> mirrors = new ArrayList<>(bindings.length);

               for(int i = 0; i < bindings.length; ++i) {
                  if (bindings[i] instanceof TypeBinding) {
                     mirrors.add(this._env.getFactory().newTypeMirror((TypeBinding)bindings[i]));
                  }
               }

               throw new MirroredTypesException(mirrors);
            } else {
               return null;
            }
         } else {
            return this.convertJDTArrayToReflectionArray(actualValue, actualType, expectedType);
         }
      } else if (Class.class.equals(expectedType)) {
         if (actualValue instanceof TypeBinding) {
            TypeMirror mirror = this._env.getFactory().newTypeMirror((TypeBinding)actualValue);
            throw new MirroredTypeException(mirror);
         } else {
            return null;
         }
      } else {
         return this.convertJDTValueToReflectionType(actualValue, actualType, expectedType);
      }
   }

   private Object convertJDTArrayToReflectionArray(Object jdtValue, TypeBinding jdtType, Class<?> expectedType) {
      assert expectedType != null && expectedType.isArray();

      if (!jdtType.isArrayType()) {
         return null;
      } else {
         Object[] jdtArray;
         if (jdtValue != null && !(jdtValue instanceof Object[])) {
            jdtArray = (Object[])Array.newInstance(jdtValue.getClass(), 1);
            jdtArray[0] = jdtValue;
         } else {
            jdtArray = (Object[])jdtValue;
         }

         TypeBinding jdtLeafType = jdtType.leafComponentType();
         Class<?> expectedLeafType = expectedType.getComponentType();
         int length = jdtArray.length;
         Object returnArray = Array.newInstance(expectedLeafType, length);

         for(int i = 0; i < length; ++i) {
            Object jdtElementValue = jdtArray[i];
            if (!expectedLeafType.isPrimitive() && !String.class.equals(expectedLeafType)) {
               if (expectedLeafType.isEnum()) {
                  Object returnVal = null;
                  if (jdtLeafType != null && jdtLeafType.isEnum() && jdtElementValue instanceof FieldBinding) {
                     FieldBinding binding = (FieldBinding)jdtElementValue;

                     try {
                        Field returnedField = null;
                        returnedField = expectedLeafType.getField(new String(binding.name));
                        if (returnedField != null) {
                           returnVal = returnedField.get(null);
                        }
                     } catch (NoSuchFieldException var14) {
                     } catch (IllegalAccessException var15) {
                     }
                  }

                  Array.set(returnArray, i, returnVal);
               } else if (expectedLeafType.isAnnotation()) {
                  Object returnVal = null;
                  if (jdtLeafType.isAnnotationType() && jdtElementValue instanceof AnnotationBinding) {
                     AnnotationMirrorImpl annoMirror = (AnnotationMirrorImpl)this._env.getFactory().newAnnotationMirror((AnnotationBinding)jdtElementValue);
                     returnVal = Proxy.newProxyInstance(expectedLeafType.getClassLoader(), new Class[]{expectedLeafType}, annoMirror);
                  }

                  Array.set(returnArray, i, returnVal);
               } else {
                  Array.set(returnArray, i, null);
               }
            } else if (jdtElementValue instanceof Constant) {
               if (Boolean.TYPE.equals(expectedLeafType)) {
                  Array.setBoolean(returnArray, i, ((Constant)jdtElementValue).booleanValue());
               } else if (Byte.TYPE.equals(expectedLeafType)) {
                  Array.setByte(returnArray, i, ((Constant)jdtElementValue).byteValue());
               } else if (Character.TYPE.equals(expectedLeafType)) {
                  Array.setChar(returnArray, i, ((Constant)jdtElementValue).charValue());
               } else if (Double.TYPE.equals(expectedLeafType)) {
                  Array.setDouble(returnArray, i, ((Constant)jdtElementValue).doubleValue());
               } else if (Float.TYPE.equals(expectedLeafType)) {
                  Array.setFloat(returnArray, i, ((Constant)jdtElementValue).floatValue());
               } else if (Integer.TYPE.equals(expectedLeafType)) {
                  Array.setInt(returnArray, i, ((Constant)jdtElementValue).intValue());
               } else if (Long.TYPE.equals(expectedLeafType)) {
                  Array.setLong(returnArray, i, ((Constant)jdtElementValue).longValue());
               } else if (Short.TYPE.equals(expectedLeafType)) {
                  Array.setShort(returnArray, i, ((Constant)jdtElementValue).shortValue());
               } else if (String.class.equals(expectedLeafType)) {
                  Array.set(returnArray, i, ((Constant)jdtElementValue).stringValue());
               }
            } else {
               Factory.setArrayMatchingDummyValue(returnArray, i, expectedLeafType);
            }
         }

         return returnArray;
      }
   }

   private Object convertJDTValueToReflectionType(Object jdtValue, TypeBinding actualType, Class<?> expectedType) {
      if (!expectedType.isPrimitive() && !String.class.equals(expectedType)) {
         if (expectedType.isEnum()) {
            Object returnVal = null;
            if (actualType != null && actualType.isEnum() && jdtValue instanceof FieldBinding) {
               FieldBinding binding = (FieldBinding)jdtValue;

               try {
                  Field returnedField = null;
                  returnedField = expectedType.getField(new String(binding.name));
                  if (returnedField != null) {
                     returnVal = returnedField.get(null);
                  }
               } catch (NoSuchFieldException var7) {
               } catch (IllegalAccessException var8) {
               }
            }

            return returnVal == null ? Factory.getMatchingDummyValue(expectedType) : returnVal;
         } else if (!expectedType.isAnnotation()) {
            return Factory.getMatchingDummyValue(expectedType);
         } else if (actualType.isAnnotationType() && jdtValue instanceof AnnotationBinding) {
            AnnotationMirrorImpl annoMirror = (AnnotationMirrorImpl)this._env.getFactory().newAnnotationMirror((AnnotationBinding)jdtValue);
            return Proxy.newProxyInstance(expectedType.getClassLoader(), new Class[]{expectedType}, annoMirror);
         } else {
            return null;
         }
      } else {
         if (jdtValue instanceof Constant) {
            if (Boolean.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).booleanValue();
            }

            if (Byte.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).byteValue();
            }

            if (Character.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).charValue();
            }

            if (Double.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).doubleValue();
            }

            if (Float.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).floatValue();
            }

            if (Integer.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).intValue();
            }

            if (Long.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).longValue();
            }

            if (Short.TYPE.equals(expectedType)) {
               return ((Constant)jdtValue).shortValue();
            }

            if (String.class.equals(expectedType)) {
               return ((Constant)jdtValue).stringValue();
            }
         }

         return Factory.getMatchingDummyValue(expectedType);
      }
   }
}
