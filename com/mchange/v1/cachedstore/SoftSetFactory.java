package com.mchange.v1.cachedstore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

public final class SoftSetFactory {
   public static Set createSynchronousCleanupSoftSet() {
      final ManualCleanupSoftSet var0 = new ManualCleanupSoftSet();
      InvocationHandler var1 = new InvocationHandler() {
         @Override
         public Object invoke(Object var1, Method var2, Object[] var3) throws Throwable {
            var0.vacuum();
            return var2.invoke(var0, var3);
         }
      };
      return (Set)Proxy.newProxyInstance(SoftSetFactory.class.getClassLoader(), new Class[]{Set.class}, var1);
   }

   private SoftSetFactory() {
   }
}
