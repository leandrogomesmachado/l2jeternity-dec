package org.apache.commons.lang.enums;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.lang.ClassUtils;

public abstract class ValuedEnum extends Enum {
   private static final long serialVersionUID = -7129650521543789085L;
   private final int iValue;

   protected ValuedEnum(String name, int value) {
      super(name);
      this.iValue = value;
   }

   protected static Enum getEnum(Class enumClass, int value) {
      if (enumClass == null) {
         throw new IllegalArgumentException("The Enum Class must not be null");
      } else {
         for(ValuedEnum enumeration : Enum.getEnumList(enumClass)) {
            if (enumeration.getValue() == value) {
               return enumeration;
            }
         }

         return null;
      }
   }

   public final int getValue() {
      return this.iValue;
   }

   public int compareTo(Object other) {
      if (other == this) {
         return 0;
      } else if (other.getClass() != this.getClass()) {
         if (other.getClass().getName().equals(this.getClass().getName())) {
            return this.iValue - this.getValueInOtherClassLoader(other);
         } else {
            throw new ClassCastException("Different enum class '" + ClassUtils.getShortClassName(other.getClass()) + "'");
         }
      } else {
         return this.iValue - ((ValuedEnum)other).iValue;
      }
   }

   private int getValueInOtherClassLoader(Object other) {
      try {
         Method mth = other.getClass().getMethod("getValue", null);
         Integer value = (Integer)mth.invoke(other, null);
         return value;
      } catch (NoSuchMethodException var4) {
      } catch (IllegalAccessException var5) {
      } catch (InvocationTargetException var6) {
      }

      throw new IllegalStateException("This should not happen");
   }

   public String toString() {
      if (this.iToString == null) {
         String shortName = ClassUtils.getShortClassName(this.getEnumClass());
         this.iToString = shortName + "[" + this.getName() + "=" + this.getValue() + "]";
      }

      return this.iToString;
   }
}
