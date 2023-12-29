package com.mchange.v1.cachedstore;

import java.util.Set;

public interface WritableCachedStore extends CachedStore {
   void write(Object var1, Object var2) throws CachedStoreException;

   void remove(Object var1) throws CachedStoreException;

   void flushWrites() throws CacheFlushException;

   Set getFailedWrites() throws CachedStoreException;

   void clearPendingWrites() throws CachedStoreException;

   @Override
   void reset() throws CachedStoreException;

   void sync() throws CachedStoreException;

   public interface Manager extends CachedStore.Manager {
      void writeToStorage(Object var1, Object var2) throws Exception;

      void removeFromStorage(Object var1) throws Exception;
   }
}
