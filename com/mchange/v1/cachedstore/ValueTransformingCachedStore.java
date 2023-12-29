package com.mchange.v1.cachedstore;

abstract class ValueTransformingCachedStore extends NoCleanupCachedStore {
   protected ValueTransformingCachedStore(CachedStore.Manager var1) {
      super(var1);
   }

   @Override
   public Object getCachedValue(Object var1) {
      return this.toUserValue(this.cache.get(var1));
   }

   @Override
   public void removeFromCache(Object var1) throws CachedStoreException {
      this.cache.remove(var1);
   }

   @Override
   public void setCachedValue(Object var1, Object var2) throws CachedStoreException {
      this.cache.put(var1, this.toCacheValue(var2));
   }

   protected Object toUserValue(Object var1) {
      return var1;
   }

   protected Object toCacheValue(Object var1) {
      return var1;
   }
}
