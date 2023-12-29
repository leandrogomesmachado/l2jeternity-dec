package com.mchange.v1.lang;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ClassUtils {
   static final String[] EMPTY_SA = new String[0];
   static Map primitivesToClasses;

   public static Set publicSupertypesForMethods(Class var0, Method[] var1) {
      Set var2 = allAssignableFrom(var0);
      HashSet var3 = new HashSet();

      for(Class var5 : var2) {
         if (isPublic(var5) && hasAllMethodsAsSupertype(var5, var1)) {
            var3.add(var5);
         }
      }

      return Collections.unmodifiableSet(var3);
   }

   public static boolean isPublic(Class var0) {
      return (var0.getModifiers() & 1) != 0;
   }

   public static boolean hasAllMethodsAsSupertype(Class var0, Method[] var1) {
      return hasAllMethods(var0, var1, true);
   }

   public static boolean hasAllMethodsAsSubtype(Class var0, Method[] var1) {
      return hasAllMethods(var0, var1, false);
   }

   private static boolean hasAllMethods(Class var0, Method[] var1, boolean var2) {
      int var3 = 0;

      for(int var4 = var1.length; var3 < var4; ++var3) {
         if (!containsMethod(var0, var1[var3], var2)) {
            return false;
         }
      }

      return true;
   }

   public static boolean containsMethodAsSupertype(Class var0, Method var1) {
      return containsMethod(var0, var1, true);
   }

   public static boolean containsMethodAsSubtype(Class var0, Method var1) {
      return containsMethod(var0, var1, false);
   }

   private static boolean containsMethod(Class var0, Method var1, boolean var2) {
      try {
         Method var3 = var0.getMethod(var1.getName(), var1.getParameterTypes());
         Class var4 = var1.getReturnType();
         Class var5 = var3.getReturnType();
         return var4.equals(var5) || var2 && var5.isAssignableFrom(var4) || !var2 && var4.isAssignableFrom(var5);
      } catch (NoSuchMethodException var6) {
         return false;
      }
   }

   public static Set allAssignableFrom(Class var0) {
      HashSet var1 = new HashSet();

      for(Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         var1.add(var2);
      }

      addSuperInterfacesToSet(var0, var1);
      return var1;
   }

   public static String simpleClassName(Class var0) {
      int var2;
      for(var2 = 0; var0.isArray(); var0 = var0.getComponentType()) {
         ++var2;
      }

      String var1 = simpleClassName(var0.getName());
      if (var2 <= 0) {
         return var1;
      } else {
         StringBuffer var3 = new StringBuffer(16);
         var3.append(var1);

         for(int var4 = 0; var4 < var2; ++var4) {
            var3.append("[]");
         }

         return var3.toString();
      }
   }

   private static String simpleClassName(String var0) {
      int var1 = var0.lastIndexOf(46);
      if (var1 < 0) {
         return var0;
      } else {
         String var2 = var0.substring(var1 + 1);
         if (var2.indexOf(36) >= 0) {
            StringBuffer var3 = new StringBuffer(var2);
            int var4 = 0;

            for(int var5 = var3.length(); var4 < var5; ++var4) {
               if (var3.charAt(var4) == '$') {
                  var3.setCharAt(var4, '.');
               }
            }

            return var3.toString();
         } else {
            return var2;
         }
      }
   }

   public static boolean isPrimitive(String var0) {
      return primitivesToClasses.get(var0) != null;
   }

   public static Class classForPrimitive(String var0) {
      return (Class)primitivesToClasses.get(var0);
   }

   public static Class forName(String var0) throws ClassNotFoundException {
      Class var1 = classForPrimitive(var0);
      if (var1 == null) {
         var1 = Class.forName(var0);
      }

      return var1;
   }

   public static Class forName(String var0, String[] var1, String[] var2) throws AmbiguousClassNameException, ClassNotFoundException {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var4) {
         return classForSimpleName(var0, var1, var2);
      }
   }

   public static Class classForSimpleName(String var0, String[] var1, String[] var2) throws AmbiguousClassNameException, ClassNotFoundException {
      HashSet var3 = new HashSet();
      Class var4 = classForPrimitive(var0);
      if (var4 == null) {
         if (var1 == null) {
            var1 = EMPTY_SA;
         }

         if (var2 == null) {
            var2 = EMPTY_SA;
         }

         int var5 = 0;

         for(int var6 = var2.length; var5 < var6; ++var5) {
            String var7 = fqcnLastElement(var2[var5]);
            if (!var3.add(var7)) {
               throw new IllegalArgumentException("Duplicate imported classes: " + var7);
            }

            if (var0.equals(var7)) {
               var4 = Class.forName(var2[var5]);
            }
         }

         if (var4 == null) {
            try {
               var4 = Class.forName("java.lang." + var0);
            } catch (ClassNotFoundException var10) {
            }

            var5 = 0;

            for(int var12 = var1.length; var5 < var12; ++var5) {
               try {
                  String var13 = var1[var5] + '.' + var0;
                  Class var8 = Class.forName(var13);
                  if (var4 != null) {
                     throw new AmbiguousClassNameException(var0, var4, var8);
                  }

                  var4 = var8;
               } catch (ClassNotFoundException var9) {
               }
            }
         }
      }

      if (var4 == null) {
         throw new ClassNotFoundException(
            "Could not find a class whose unqualified name is \""
               + var0
               + "\" with the imports supplied. Import packages are "
               + Arrays.asList(var1)
               + "; class imports are "
               + Arrays.asList(var2)
         );
      } else {
         return var4;
      }
   }

   public static String resolvableTypeName(Class var0, String[] var1, String[] var2) throws ClassNotFoundException {
      String var3 = simpleClassName(var0);

      try {
         classForSimpleName(var3, var1, var2);
         return var3;
      } catch (AmbiguousClassNameException var5) {
         return var0.getName();
      }
   }

   public static String fqcnLastElement(String var0) {
      int var1 = var0.lastIndexOf(46);
      return var1 < 0 ? var0 : var0.substring(var1 + 1);
   }

   private static void addSuperInterfacesToSet(Class var0, Set var1) {
      Class[] var2 = var0.getInterfaces();
      int var3 = 0;

      for(int var4 = var2.length; var3 < var4; ++var3) {
         var1.add(var2[var3]);
         addSuperInterfacesToSet(var2[var3], var1);
      }
   }

   private ClassUtils() {
   }

   static {
      HashMap var0 = new HashMap();
      var0.put("boolean", Boolean.TYPE);
      var0.put("int", Integer.TYPE);
      var0.put("char", Character.TYPE);
      var0.put("short", Short.TYPE);
      var0.put("int", Integer.TYPE);
      var0.put("long", Long.TYPE);
      var0.put("float", Float.TYPE);
      var0.put("double", Double.TYPE);
      var0.put("void", Void.TYPE);
      primitivesToClasses = Collections.unmodifiableMap(var0);
   }
}
