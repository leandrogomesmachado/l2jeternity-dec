package org.apache.commons.lang3;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AnnotationUtils {
   private static final ToStringStyle TO_STRING_STYLE = new ToStringStyle() {
      private static final long serialVersionUID = 1L;

      {
         this.setDefaultFullDetail(true);
         this.setArrayContentDetail(true);
         this.setUseClassName(true);
         this.setUseShortClassName(true);
         this.setUseIdentityHashCode(false);
         this.setContentStart("(");
         this.setContentEnd(")");
         this.setFieldSeparator(", ");
         this.setArrayStart("[");
         this.setArrayEnd("]");
      }

      @Override
      protected String getShortClassName(Class<?> cls) {
         Class<? extends Annotation> annotationType = null;

         for(Class<?> iface : ClassUtils.getAllInterfaces(cls)) {
            if (Annotation.class.isAssignableFrom(iface)) {
               annotationType = iface;
               break;
            }
         }

         return new StringBuilder(annotationType == null ? "" : annotationType.getName()).insert(0, '@').toString();
      }

      @Override
      protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
         if (value instanceof Annotation) {
            value = AnnotationUtils.toString((Annotation)value);
         }

         super.appendDetail(buffer, fieldName, value);
      }
   };

   public static boolean equals(Annotation a1, Annotation a2) {
      if (a1 == a2) {
         return true;
      } else if (a1 != null && a2 != null) {
         Class<? extends Annotation> type = a1.annotationType();
         Class<? extends Annotation> type2 = a2.annotationType();
         Validate.notNull(type, "Annotation %s with null annotationType()", a1);
         Validate.notNull(type2, "Annotation %s with null annotationType()", a2);
         if (!type.equals(type2)) {
            return false;
         } else {
            try {
               for(Method m : type.getDeclaredMethods()) {
                  if (m.getParameterTypes().length == 0 && isValidAnnotationMemberType(m.getReturnType())) {
                     Object v1 = m.invoke(a1);
                     Object v2 = m.invoke(a2);
                     if (!memberEquals(m.getReturnType(), v1, v2)) {
                        return false;
                     }
                  }
               }

               return true;
            } catch (InvocationTargetException | IllegalAccessException var10) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static int hashCode(Annotation a) {
      int result = 0;
      Class<? extends Annotation> type = a.annotationType();

      for(Method m : type.getDeclaredMethods()) {
         try {
            Object value = m.invoke(a);
            if (value == null) {
               throw new IllegalStateException(String.format("Annotation method %s returned null", m));
            }

            result += hashMember(m.getName(), value);
         } catch (RuntimeException var8) {
            throw var8;
         } catch (Exception var9) {
            throw new RuntimeException(var9);
         }
      }

      return result;
   }

   public static String toString(Annotation a) {
      ToStringBuilder builder = new ToStringBuilder(a, TO_STRING_STYLE);

      for(Method m : a.annotationType().getDeclaredMethods()) {
         if (m.getParameterTypes().length <= 0) {
            try {
               builder.append(m.getName(), m.invoke(a));
            } catch (RuntimeException var7) {
               throw var7;
            } catch (Exception var8) {
               throw new RuntimeException(var8);
            }
         }
      }

      return builder.build();
   }

   public static boolean isValidAnnotationMemberType(Class<?> type) {
      if (type == null) {
         return false;
      } else {
         if (type.isArray()) {
            type = type.getComponentType();
         }

         return type.isPrimitive() || type.isEnum() || type.isAnnotation() || String.class.equals(type) || Class.class.equals(type);
      }
   }

   private static int hashMember(String name, Object value) {
      int part1 = name.hashCode() * 127;
      if (value.getClass().isArray()) {
         return part1 ^ arrayMemberHash(value.getClass().getComponentType(), value);
      } else {
         return value instanceof Annotation ? part1 ^ hashCode((Annotation)value) : part1 ^ value.hashCode();
      }
   }

   private static boolean memberEquals(Class<?> type, Object o1, Object o2) {
      if (o1 == o2) {
         return true;
      } else if (o1 == null || o2 == null) {
         return false;
      } else if (type.isArray()) {
         return arrayMemberEquals(type.getComponentType(), o1, o2);
      } else {
         return type.isAnnotation() ? equals((Annotation)o1, (Annotation)o2) : o1.equals(o2);
      }
   }

   private static boolean arrayMemberEquals(Class<?> componentType, Object o1, Object o2) {
      if (componentType.isAnnotation()) {
         return annotationArrayMemberEquals((Annotation[])o1, (Annotation[])o2);
      } else if (componentType.equals(Byte.TYPE)) {
         return Arrays.equals((byte[])o1, (byte[])o2);
      } else if (componentType.equals(Short.TYPE)) {
         return Arrays.equals((short[])o1, (short[])o2);
      } else if (componentType.equals(Integer.TYPE)) {
         return Arrays.equals((int[])o1, (int[])o2);
      } else if (componentType.equals(Character.TYPE)) {
         return Arrays.equals((char[])o1, (char[])o2);
      } else if (componentType.equals(Long.TYPE)) {
         return Arrays.equals((long[])o1, (long[])o2);
      } else if (componentType.equals(Float.TYPE)) {
         return Arrays.equals((float[])o1, (float[])o2);
      } else if (componentType.equals(Double.TYPE)) {
         return Arrays.equals((double[])o1, (double[])o2);
      } else {
         return componentType.equals(Boolean.TYPE) ? Arrays.equals((boolean[])o1, (boolean[])o2) : Arrays.equals(o1, o2);
      }
   }

   private static boolean annotationArrayMemberEquals(Annotation[] a1, Annotation[] a2) {
      if (a1.length != a2.length) {
         return false;
      } else {
         for(int i = 0; i < a1.length; ++i) {
            if (!equals(a1[i], a2[i])) {
               return false;
            }
         }

         return true;
      }
   }

   private static int arrayMemberHash(Class<?> componentType, Object o) {
      if (componentType.equals(Byte.TYPE)) {
         return Arrays.hashCode((byte[])o);
      } else if (componentType.equals(Short.TYPE)) {
         return Arrays.hashCode((short[])o);
      } else if (componentType.equals(Integer.TYPE)) {
         return Arrays.hashCode((int[])o);
      } else if (componentType.equals(Character.TYPE)) {
         return Arrays.hashCode((char[])o);
      } else if (componentType.equals(Long.TYPE)) {
         return Arrays.hashCode((long[])o);
      } else if (componentType.equals(Float.TYPE)) {
         return Arrays.hashCode((float[])o);
      } else if (componentType.equals(Double.TYPE)) {
         return Arrays.hashCode((double[])o);
      } else {
         return componentType.equals(Boolean.TYPE) ? Arrays.hashCode((boolean[])o) : Arrays.hashCode(o);
      }
   }
}
