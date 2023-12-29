package com.mchange.v1.cachedstore;

import java.util.Collections;
import java.util.Set;

class NoCacheWritableCachedStore implements WritableCachedStore, Autoflushing {
   WritableCachedStore.Manager mgr;

   NoCacheWritableCachedStore(WritableCachedStore.Manager var1) {
      this.mgr = var1;
   }

   @Override
   public Object find(Object var1) throws CachedStoreException {
      try {
         return this.mgr.recreateFromKey(var1);
      } catch (Exception var3) {
         var3.printStackTrace();
         throw CachedStoreUtils.toCachedStoreException(var3);
      }
   }

   @Override
   public void reset() {
   }

   @Override
   public void write(Object var1, Object var2) throws CachedStoreException {
      try {
         this.mgr.writeToStorage(var1, var2);
      } catch (Exception var4) {
         var4.printStackTrace();
         throw CachedStoreUtils.toCachedStoreException(var4);
      }
   }

   @Override
   public void remove(Object var1) throws CachedStoreException {
      try {
         this.mgr.removeFromStorage(var1);
      } catch (Exception var3) {
         var3.printStackTrace();
         throw CachedStoreUtils.toCachedStoreException(var3);
      }
   }

   @Override
   public void flushWrites() throws CacheFlushException {
   }

   @Override
   public Set getFailedWrites() throws CachedStoreException {
      return Collections.EMPTY_SET;
   }

   @Override
   public void clearPendingWrites() throws CachedStoreException {
   }

   @Override
   public void sync() throws CachedStoreException {
   }
}
