package org.apache.commons.lang.enums;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

public abstract class Enum implements Comparable, Serializable {
   private static final long serialVersionUID = -487045951170455942L;
   private static final Map EMPTY_MAP = Collections.unmodifiableMap(new HashMap(0));
   private static Map cEnumClasses = new WeakHashMap();
   private final String iName;
   private final transient int iHashCode;
   protected transient String iToString = null;

   protected Enum(String name) {
      this.init(name);
      this.iName = name;
      this.iHashCode = 7 + this.getEnumClass().hashCode() + 3 * name.hashCode();
   }

   private void init(String name) {
      if (StringUtils.isEmpty(name)) {
         throw new IllegalArgumentException("The Enum name must not be empty or null");
      } else {
         Class enumClass = this.getEnumClass();
         if (enumClass == null) {
            throw new IllegalArgumentException("getEnumClass() must not be null");
         } else {
            Class cls = this.getClass();

            boolean ok;
            for(ok = false;
               cls != null
                  && cls
                     != (
                        class$org$apache$commons$lang$enums$Enum == null
                           ? (class$org$apache$commons$lang$enums$Enum = class$("org.apache.commons.lang.enums.Enum"))
                           : class$org$apache$commons$lang$enums$Enum
                     )
                  && cls
                     != (
                        class$org$apache$commons$lang$enums$ValuedEnum == null
                           ? (class$org$apache$commons$lang$enums$ValuedEnum = class$("org.apache.commons.lang.enums.ValuedEnum"))
                           : class$org$apache$commons$lang$enums$ValuedEnum
                     );
               cls = cls.getSuperclass()
            ) {
               if (cls == enumClass) {
                  ok = true;
                  break;
               }
            }

            if (!ok) {
               throw new IllegalArgumentException("getEnumClass() must return a superclass of this class");
            } else {
               Enum.Entry entry;
               synchronized(class$org$apache$commons$lang$enums$Enum == null
                  ? (class$org$apache$commons$lang$enums$Enum = class$("org.apache.commons.lang.enums.Enum"))
                  : class$org$apache$commons$lang$enums$Enum) {
                  entry = (Enum.Entry)cEnumClasses.get(enumClass);
                  if (entry == null) {
                     entry = createEntry(enumClass);
                     Map myMap = new WeakHashMap();
                     myMap.putAll(cEnumClasses);
                     myMap.put(enumClass, entry);
                     cEnumClasses = myMap;
                  }
               }

               if (entry.map.containsKey(name)) {
                  throw new IllegalArgumentException("The Enum name must be unique, '" + name + "' has already been added");
               } else {
                  entry.map.put(name, this);
                  entry.list.add(this);
               }
            }
         }
      }
   }

   protected Object readResolve() {
      Enum.Entry entry = (Enum.Entry)cEnumClasses.get(this.getEnumClass());
      return entry == null ? null : entry.map.get(this.getName());
   }

   protected static Enum getEnum(Class enumClass, String name) {
      Enum.Entry entry = getEntry(enumClass);
      return entry == null ? null : (Enum)entry.map.get(name);
   }

   protected static Map getEnumMap(Class enumClass) {
      Enum.Entry entry = getEntry(enumClass);
      return entry == null ? EMPTY_MAP : entry.unmodifiableMap;
   }

   protected static List getEnumList(Class enumClass) {
      Enum.Entry entry = getEntry(enumClass);
      return entry == null ? Collections.EMPTY_LIST : entry.unmodifiableList;
   }

   protected static Iterator iterator(Class enumClass) {
      return getEnumList(enumClass).iterator();
   }

   private static Enum.Entry getEntry(Class enumClass) {
      if (enumClass == null) {
         throw new IllegalArgumentException("The Enum Class must not be null");
      } else if (!(class$org$apache$commons$lang$enums$Enum == null
            ? (class$org$apache$commons$lang$enums$Enum = class$("org.apache.commons.lang.enums.Enum"))
            : class$org$apache$commons$lang$enums$Enum)
         .isAssignableFrom(enumClass)) {
         throw new IllegalArgumentException("The Class must be a subclass of Enum");
      } else {
         return (Enum.Entry)cEnumClasses.get(enumClass);
      }
   }

   private static Enum.Entry createEntry(Class enumClass) {
      Enum.Entry entry = new Enum.Entry();

      for(Class cls = enumClass.getSuperclass();
         cls != null
            && cls
               != (
                  class$org$apache$commons$lang$enums$Enum == null
                     ? (class$org$apache$commons$lang$enums$Enum = class$("org.apache.commons.lang.enums.Enum"))
                     : class$org$apache$commons$lang$enums$Enum
               )
            && cls
               != (
                  class$org$apache$commons$lang$enums$ValuedEnum == null
                     ? (class$org$apache$commons$lang$enums$ValuedEnum = class$("org.apache.commons.lang.enums.ValuedEnum"))
                     : class$org$apache$commons$lang$enums$ValuedEnum
               );
         cls = cls.getSuperclass()
      ) {
         Enum.Entry loopEntry = (Enum.Entry)cEnumClasses.get(cls);
         if (loopEntry != null) {
            entry.list.addAll(loopEntry.list);
            entry.map.putAll(loopEntry.map);
            break;
         }
      }

      return entry;
   }

   public final String getName() {
      return this.iName;
   }

   public Class getEnumClass() {
      return this.getClass();
   }

   public final boolean equals(Object other) {
      if (other == this) {
         return true;
      } else if (other == null) {
         return false;
      } else if (other.getClass() == this.getClass()) {
         return this.iName.equals(((Enum)other).iName);
      } else {
         return !other.getClass().getName().equals(this.getClass().getName()) ? false : this.iName.equals(this.getNameInOtherClassLoader(other));
      }
   }

   public final int hashCode() {
      return this.iHashCode;
   }

   public int compareTo(Object other) {
      if (other == this) {
         return 0;
      } else if (other.getClass() != this.getClass()) {
         if (other.getClass().getName().equals(this.getClass().getName())) {
            return this.iName.compareTo(this.getNameInOtherClassLoader(other));
         } else {
            throw new ClassCastException("Different enum class '" + ClassUtils.getShortClassName(other.getClass()) + "'");
         }
      } else {
         return this.iName.compareTo(((Enum)other).iName);
      }
   }

   private String getNameInOtherClassLoader(Object other) {
      try {
         Method mth = other.getClass().getMethod("getName", null);
         return (String)mth.invoke(other, null);
      } catch (NoSuchMethodException var4) {
      } catch (IllegalAccessException var5) {
      } catch (InvocationTargetException var6) {
      }

      throw new IllegalStateException("This should not happen");
   }

   public String toString() {
      if (this.iToString == null) {
         String shortName = ClassUtils.getShortClassName(this.getEnumClass());
         this.iToString = shortName + "[" + this.getName() + "]";
      }

      return this.iToString;
   }

   private static class Entry {
      final Map map = new HashMap();
      final Map unmodifiableMap = Collections.unmodifiableMap(this.map);
      final List list = new ArrayList(25);
      final List unmodifiableList = Collections.unmodifiableList(this.list);

      protected Entry() {
      }
   }
}
