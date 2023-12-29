package org.apache.commons.lang.builder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang.ArrayUtils;

public class ReflectionToStringBuilder extends ToStringBuilder {
   private boolean appendStatics = false;
   private boolean appendTransients = false;
   private String[] excludeFieldNames;
   private Class upToClass = null;

   public static String toString(Object object) {
      return toString(object, null, false, false, null);
   }

   public static String toString(Object object, ToStringStyle style) {
      return toString(object, style, false, false, null);
   }

   public static String toString(Object object, ToStringStyle style, boolean outputTransients) {
      return toString(object, style, outputTransients, false, null);
   }

   public static String toString(Object object, ToStringStyle style, boolean outputTransients, boolean outputStatics) {
      return toString(object, style, outputTransients, outputStatics, null);
   }

   public static String toString(Object object, ToStringStyle style, boolean outputTransients, boolean outputStatics, Class reflectUpToClass) {
      return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics).toString();
   }

   /** @deprecated */
   public static String toString(Object object, ToStringStyle style, boolean outputTransients, Class reflectUpToClass) {
      return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients).toString();
   }

   public static String toStringExclude(Object object, String excludeFieldName) {
      return toStringExclude(object, new String[]{excludeFieldName});
   }

   public static String toStringExclude(Object object, Collection excludeFieldNames) {
      return toStringExclude(object, toNoNullStringArray(excludeFieldNames));
   }

   static String[] toNoNullStringArray(Collection collection) {
      return collection == null ? ArrayUtils.EMPTY_STRING_ARRAY : toNoNullStringArray(collection.toArray());
   }

   static String[] toNoNullStringArray(Object[] array) {
      ArrayList list = new ArrayList(array.length);

      for(int i = 0; i < array.length; ++i) {
         Object e = array[i];
         if (e != null) {
            list.add(e.toString());
         }
      }

      return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
   }

   public static String toStringExclude(Object object, String[] excludeFieldNames) {
      return new ReflectionToStringBuilder(object).setExcludeFieldNames(excludeFieldNames).toString();
   }

   public ReflectionToStringBuilder(Object object) {
      super(object);
   }

   public ReflectionToStringBuilder(Object object, ToStringStyle style) {
      super(object, style);
   }

   public ReflectionToStringBuilder(Object object, ToStringStyle style, StringBuffer buffer) {
      super(object, style, buffer);
   }

   /** @deprecated */
   public ReflectionToStringBuilder(Object object, ToStringStyle style, StringBuffer buffer, Class reflectUpToClass, boolean outputTransients) {
      super(object, style, buffer);
      this.setUpToClass(reflectUpToClass);
      this.setAppendTransients(outputTransients);
   }

   public ReflectionToStringBuilder(
      Object object, ToStringStyle style, StringBuffer buffer, Class reflectUpToClass, boolean outputTransients, boolean outputStatics
   ) {
      super(object, style, buffer);
      this.setUpToClass(reflectUpToClass);
      this.setAppendTransients(outputTransients);
      this.setAppendStatics(outputStatics);
   }

   protected boolean accept(Field field) {
      if (field.getName().indexOf(36) != -1) {
         return false;
      } else if (Modifier.isTransient(field.getModifiers()) && !this.isAppendTransients()) {
         return false;
      } else if (Modifier.isStatic(field.getModifiers()) && !this.isAppendStatics()) {
         return false;
      } else {
         return this.getExcludeFieldNames() == null || Arrays.binarySearch(this.getExcludeFieldNames(), field.getName()) < 0;
      }
   }

   protected void appendFieldsIn(Class clazz) {
      if (clazz.isArray()) {
         this.reflectionAppendArray(this.getObject());
      } else {
         Field[] fields = clazz.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);

         for(int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            String fieldName = field.getName();
            if (this.accept(field)) {
               try {
                  Object fieldValue = this.getValue(field);
                  this.append(fieldName, fieldValue);
               } catch (IllegalAccessException var7) {
                  throw new InternalError("Unexpected IllegalAccessException: " + var7.getMessage());
               }
            }
         }
      }
   }

   public String[] getExcludeFieldNames() {
      return this.excludeFieldNames;
   }

   public Class getUpToClass() {
      return this.upToClass;
   }

   protected Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
      return field.get(this.getObject());
   }

   public boolean isAppendStatics() {
      return this.appendStatics;
   }

   public boolean isAppendTransients() {
      return this.appendTransients;
   }

   public ToStringBuilder reflectionAppendArray(Object array) {
      this.getStyle().reflectionAppendArrayDetail(this.getStringBuffer(), null, array);
      return this;
   }

   public void setAppendStatics(boolean appendStatics) {
      this.appendStatics = appendStatics;
   }

   public void setAppendTransients(boolean appendTransients) {
      this.appendTransients = appendTransients;
   }

   public ReflectionToStringBuilder setExcludeFieldNames(String[] excludeFieldNamesParam) {
      if (excludeFieldNamesParam == null) {
         this.excludeFieldNames = null;
      } else {
         this.excludeFieldNames = toNoNullStringArray(excludeFieldNamesParam);
         Arrays.sort((Object[])this.excludeFieldNames);
      }

      return this;
   }

   public void setUpToClass(Class clazz) {
      this.upToClass = clazz;
   }

   public String toString() {
      if (this.getObject() == null) {
         return this.getStyle().getNullText();
      } else {
         Class clazz = this.getObject().getClass();
         this.appendFieldsIn(clazz);

         while(clazz.getSuperclass() != null && clazz != this.getUpToClass()) {
            clazz = clazz.getSuperclass();
            this.appendFieldsIn(clazz);
         }

         return super.toString();
      }
   }
}
