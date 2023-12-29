package com.mchange.v2.codegen;

import com.mchange.v1.lang.ClassUtils;
import java.io.File;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class CodegenUtils {
   public static String getModifierString(int var0) {
      StringBuffer var1 = new StringBuffer(32);
      if (Modifier.isPublic(var0)) {
         var1.append("public ");
      }

      if (Modifier.isProtected(var0)) {
         var1.append("protected ");
      }

      if (Modifier.isPrivate(var0)) {
         var1.append("private ");
      }

      if (Modifier.isAbstract(var0)) {
         var1.append("abstract ");
      }

      if (Modifier.isStatic(var0)) {
         var1.append("static ");
      }

      if (Modifier.isFinal(var0)) {
         var1.append("final ");
      }

      if (Modifier.isSynchronized(var0)) {
         var1.append("synchronized ");
      }

      if (Modifier.isTransient(var0)) {
         var1.append("transient ");
      }

      if (Modifier.isVolatile(var0)) {
         var1.append("volatile ");
      }

      if (Modifier.isStrict(var0)) {
         var1.append("strictfp ");
      }

      if (Modifier.isNative(var0)) {
         var1.append("native ");
      }

      if (Modifier.isInterface(var0)) {
         var1.append("interface ");
      }

      return var1.toString().trim();
   }

   public static Class unarrayClass(Class var0) {
      Class var1 = var0;

      while(var1.isArray()) {
         var1 = var1.getComponentType();
      }

      return var1;
   }

   public static boolean inSamePackage(String var0, String var1) {
      int var2 = var0.lastIndexOf(46);
      int var3 = var1.lastIndexOf(46);
      if (var2 >= 0 && var3 >= 0) {
         if (!var0.substring(0, var2).equals(var0.substring(0, var2))) {
            return false;
         } else {
            return var1.indexOf(46) < 0;
         }
      } else {
         return true;
      }
   }

   public static String fqcnLastElement(String var0) {
      return ClassUtils.fqcnLastElement(var0);
   }

   public static String methodSignature(Method var0) {
      return methodSignature(var0, null);
   }

   public static String methodSignature(Method var0, String[] var1) {
      return methodSignature(1, var0, var1);
   }

   public static String methodSignature(int var0, Method var1, String[] var2) {
      StringBuffer var3 = new StringBuffer(256);
      var3.append(getModifierString(var0));
      var3.append(' ');
      var3.append(ClassUtils.simpleClassName(var1.getReturnType()));
      var3.append(' ');
      var3.append(var1.getName());
      var3.append('(');
      Class[] var4 = var1.getParameterTypes();
      int var5 = 0;

      for(int var6 = var4.length; var5 < var6; ++var5) {
         if (var5 != 0) {
            var3.append(", ");
         }

         var3.append(ClassUtils.simpleClassName(var4[var5]));
         var3.append(' ');
         var3.append(var2 == null ? String.valueOf((char)(97 + var5)) : var2[var5]);
      }

      var3.append(')');
      Class[] var8 = var1.getExceptionTypes();
      if (var8.length > 0) {
         var3.append(" throws ");
         int var9 = 0;

         for(int var7 = var8.length; var9 < var7; ++var9) {
            if (var9 != 0) {
               var3.append(", ");
            }

            var3.append(ClassUtils.simpleClassName(var8[var9]));
         }
      }

      return var3.toString();
   }

   public static String methodCall(Method var0) {
      return methodCall(var0, null);
   }

   public static String methodCall(Method var0, String[] var1) {
      StringBuffer var2 = new StringBuffer(256);
      var2.append(var0.getName());
      var2.append('(');
      Class[] var3 = var0.getParameterTypes();
      int var4 = 0;

      for(int var5 = var3.length; var4 < var5; ++var4) {
         if (var4 != 0) {
            var2.append(", ");
         }

         var2.append(var1 == null ? generatedArgumentName(var4) : var1[var4]);
      }

      var2.append(')');
      return var2.toString();
   }

   public static String reflectiveMethodObjectArray(Method var0) {
      return reflectiveMethodObjectArray(var0, null);
   }

   public static String reflectiveMethodObjectArray(Method var0, String[] var1) {
      StringBuffer var2 = new StringBuffer(256);
      var2.append("new Object[] ");
      var2.append('{');
      Class[] var3 = var0.getParameterTypes();
      int var4 = 0;

      for(int var5 = var3.length; var4 < var5; ++var4) {
         if (var4 != 0) {
            var2.append(", ");
         }

         var2.append(var1 == null ? generatedArgumentName(var4) : var1[var4]);
      }

      var2.append('}');
      return var2.toString();
   }

   public static String reflectiveMethodParameterTypeArray(Method var0) {
      StringBuffer var1 = new StringBuffer(256);
      var1.append("new Class[] ");
      var1.append('{');
      Class[] var2 = var0.getParameterTypes();
      int var3 = 0;

      for(int var4 = var2.length; var3 < var4; ++var3) {
         if (var3 != 0) {
            var1.append(", ");
         }

         var1.append(ClassUtils.simpleClassName(var2[var3]));
         var1.append(".class");
      }

      var1.append('}');
      return var1.toString();
   }

   public static String generatedArgumentName(int var0) {
      return String.valueOf((char)(97 + var0));
   }

   public static String simpleClassName(Class var0) {
      return ClassUtils.simpleClassName(var0);
   }

   public static IndentedWriter toIndentedWriter(Writer var0) {
      return var0 instanceof IndentedWriter ? (IndentedWriter)var0 : new IndentedWriter(var0);
   }

   public static String packageNameToFileSystemDirPath(String var0) {
      StringBuffer var1 = new StringBuffer(var0);
      int var2 = 0;

      for(int var3 = var1.length(); var2 < var3; ++var2) {
         if (var1.charAt(var2) == '.') {
            var1.setCharAt(var2, File.separatorChar);
         }
      }

      var1.append(File.separatorChar);
      return var1.toString();
   }

   private CodegenUtils() {
   }
}
