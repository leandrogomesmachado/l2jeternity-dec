package com.mchange.v1.lang;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;

public final class Synchronizer {
   public static Object createSynchronizedWrapper(final Object var0) {
      InvocationHandler var1 = new InvocationHandler() {
         @Override
         public Object invoke(Object var1, Method var2, Object[] var3) throws Throwable {
            synchronized(var1) {
               return var2.invoke(var0, var3);
            }
         }
      };
      Class var2 = var0.getClass();
      return Proxy.newProxyInstance(var2.getClassLoader(), recurseFindInterfaces(var2), var1);
   }

   private static Class[] recurseFindInterfaces(Class var0) {
      HashSet var1;
      for(var1 = new HashSet(); var0 != null; var0 = var0.getSuperclass()) {
         Class[] var2 = var0.getInterfaces();
         int var3 = 0;

         for(int var4 = var2.length; var3 < var4; ++var3) {
            var1.add(var2[var3]);
         }
      }

      Class[] var5 = new Class[var1.size()];
      var1.toArray(var5);
      return var5;
   }

   private Synchronizer() {
   }
}
