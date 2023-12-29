package com.mchange.v1.cachedstore;

import com.mchange.v1.util.WrapperIterator;
import java.util.Iterator;

abstract class KeyTransformingCachedStore extends NoCleanupCachedStore {
   protected KeyTransformingCachedStore(CachedStore.Manager var1) {
      super(var1);
   }

   @Override
   public Object getCachedValue(Object var1) {
      return this.cache.get(this.toCacheFetchKey(var1));
   }

   @Override
   public void removeFromCache(Object var1) throws CachedStoreException {
      this.cache.remove(this.toCacheFetchKey(var1));
   }

   @Override
   public void setCachedValue(Object var1, Object var2) throws CachedStoreException {
      Object var3 = this.toCachePutKey(var1);
      this.cache.put(var3, var2);
   }

   @Override
   public Iterator cachedKeys() throws CachedStoreException {
      return new WrapperIterator(this.cache.keySet().iterator(), false) {
         @Override
         public Object transformObject(Object var1) {
            Object var2 = KeyTransformingCachedStore.this.toUserKey(var1);
            return var2 == null ? SKIP_TOKEN : var2;
         }
      };
   }

   protected Object toUserKey(Object var1) {
      return var1;
   }

   protected Object toCacheFetchKey(Object var1) {
      return this.toCachePutKey(var1);
   }

   protected Object toCachePutKey(Object var1) {
      return var1;
   }

   protected Object removeByTransformedKey(Object var1) {
      return this.cache.remove(var1);
   }
}
