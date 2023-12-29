package com.mchange.v1.cachedstore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class CachedStoreFactory {
   public static TweakableCachedStore createNoCleanupCachedStore(CachedStore.Manager var0) {
      return new NoCleanupCachedStore(var0);
   }

   public static TweakableCachedStore createSoftValueCachedStore(CachedStore.Manager var0) {
      return new SoftReferenceCachedStore(var0);
   }

   public static TweakableCachedStore createSynchronousCleanupSoftKeyCachedStore(CachedStore.Manager var0) {
      final ManualCleanupSoftKeyCachedStore var1 = new ManualCleanupSoftKeyCachedStore(var0);
      InvocationHandler var2 = new InvocationHandler() {
         @Override
         public Object invoke(Object var1x, Method var2, Object[] var3) throws Throwable {
            var1.vacuum();
            return var2.invoke(var1, var3);
         }
      };
      return (TweakableCachedStore)Proxy.newProxyInstance(CachedStoreFactory.class.getClassLoader(), new Class[]{TweakableCachedStore.class}, var2);
   }

   public static TweakableCachedStore createNoCacheCachedStore(CachedStore.Manager var0) {
      return new NoCacheCachedStore(var0);
   }

   public static WritableCachedStore createDefaultWritableCachedStore(WritableCachedStore.Manager var0) {
      TweakableCachedStore var1 = createSynchronousCleanupSoftKeyCachedStore(var0);
      return new SimpleWritableCachedStore(var1, var0);
   }

   public static WritableCachedStore cacheWritesOnlyWritableCachedStore(WritableCachedStore.Manager var0) {
      TweakableCachedStore var1 = createNoCacheCachedStore(var0);
      return new SimpleWritableCachedStore(var1, var0);
   }

   public static WritableCachedStore createNoCacheWritableCachedStore(WritableCachedStore.Manager var0) {
      return new NoCacheWritableCachedStore(var0);
   }
}
