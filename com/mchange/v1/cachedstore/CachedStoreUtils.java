package com.mchange.v1.cachedstore;

import com.mchange.lang.PotentiallySecondary;
import com.mchange.v1.lang.Synchronizer;

public final class CachedStoreUtils {
   static final boolean DEBUG = true;

   public static CachedStore synchronizedCachedStore(CachedStore var0) {
      return (CachedStore)Synchronizer.createSynchronizedWrapper(var0);
   }

   public static TweakableCachedStore synchronizedTweakableCachedStore(TweakableCachedStore var0) {
      return (TweakableCachedStore)Synchronizer.createSynchronizedWrapper(var0);
   }

   public static WritableCachedStore synchronizedWritableCachedStore(WritableCachedStore var0) {
      return (WritableCachedStore)Synchronizer.createSynchronizedWrapper(var0);
   }

   public static CachedStore untweakableCachedStore(final TweakableCachedStore var0) {
      return new CachedStore() {
         @Override
         public Object find(Object var1) throws CachedStoreException {
            return var0.find(var1);
         }

         @Override
         public void reset() throws CachedStoreException {
            var0.reset();
         }
      };
   }

   static CachedStoreException toCachedStoreException(Throwable var0) {
      var0.printStackTrace();
      if (var0 instanceof CachedStoreException) {
         return (CachedStoreException)var0;
      } else {
         if (var0 instanceof PotentiallySecondary) {
            Throwable var1 = ((PotentiallySecondary)var0).getNestedThrowable();
            if (var1 instanceof CachedStoreException) {
               return (CachedStoreException)var1;
            }
         }

         return new CachedStoreException(var0);
      }
   }

   static CacheFlushException toCacheFlushException(Throwable var0) {
      var0.printStackTrace();
      return var0 instanceof CacheFlushException ? (CacheFlushException)var0 : new CacheFlushException(var0);
   }

   private CachedStoreUtils() {
   }
}
