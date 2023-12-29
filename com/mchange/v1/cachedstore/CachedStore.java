package com.mchange.v1.cachedstore;

public interface CachedStore {
   Object find(Object var1) throws CachedStoreException;

   void reset() throws CachedStoreException;

   public interface Manager {
      boolean isDirty(Object var1, Object var2) throws Exception;

      Object recreateFromKey(Object var1) throws Exception;
   }
}
