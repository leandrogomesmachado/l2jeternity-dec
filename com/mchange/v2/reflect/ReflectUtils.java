package com.mchange.v2.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ReflectUtils {
   public static final Class[] PROXY_CTOR_ARGS = new Class[]{InvocationHandler.class};

   public static Constructor findProxyConstructor(ClassLoader var0, Class var1) throws NoSuchMethodException {
      return findProxyConstructor(var0, new Class[]{var1});
   }

   public static Constructor findProxyConstructor(ClassLoader var0, Class[] var1) throws NoSuchMethodException {
      Class var2 = Proxy.getProxyClass(var0, var1);
      return var2.getConstructor(PROXY_CTOR_ARGS);
   }

   public static boolean isPublic(Member var0) {
      return (var0.getModifiers() & 1) != 0;
   }

   public static boolean isPublic(Class var0) {
      return (var0.getModifiers() & 1) != 0;
   }

   public static Class findPublicParent(Class var0) {
      do {
         var0 = var0.getSuperclass();
      } while(var0 != null && !isPublic(var0));

      return var0;
   }

   public static Iterator traverseInterfaces(Class var0) {
      HashSet var1 = new HashSet();
      if (var0.isInterface()) {
         var1.add(var0);
      }

      addParentInterfaces(var1, var0);
      return var1.iterator();
   }

   private static void addParentInterfaces(Set var0, Class var1) {
      Class[] var2 = var1.getInterfaces();
      int var3 = 0;

      for(int var4 = var2.length; var3 < var4; ++var3) {
         var0.add(var2[var3]);
         addParentInterfaces(var0, var2[var3]);
      }
   }

   public static Method findInPublicScope(Method var0) {
      if (!isPublic(var0)) {
         return null;
      } else {
         Class var1 = var0.getDeclaringClass();
         if (isPublic(var1)) {
            return var0;
         } else {
            Class var2 = var1;

            while((var2 = findPublicParent(var2)) != null) {
               try {
                  return var2.getMethod(var0.getName(), var0.getParameterTypes());
               } catch (NoSuchMethodException var6) {
               }
            }

            Iterator var3 = traverseInterfaces(var1);

            while(var3.hasNext()) {
               var2 = (Class)var3.next();
               if (isPublic(var2)) {
                  try {
                     return var2.getMethod(var0.getName(), var0.getParameterTypes());
                  } catch (NoSuchMethodException var5) {
                  }
               }
            }

            return null;
         }
      }
   }

   private ReflectUtils() {
   }
}
