package com.mchange.v1.cachedstore;

import com.mchange.v1.util.WrapperIterator;
import java.util.Iterator;

abstract class KeyValueTransformingCachedStore extends ValueTransformingCachedStore {
   protected KeyValueTransformingCachedStore(CachedStore.Manager var1) {
      super(var1);
   }

   @Override
   public Object getCachedValue(Object var1) {
      return this.toUserValue(this.cache.get(this.toCacheFetchKey(var1)));
   }

   public void clearCachedValue(Object var1) throws CachedStoreException {
      this.cache.remove(this.toCacheFetchKey(var1));
   }

   @Override
   public void setCachedValue(Object var1, Object var2) throws CachedStoreException {
      this.cache.put(this.toCachePutKey(var1), this.toCacheValue(var2));
   }

   @Override
   public Iterator cachedKeys() throws CachedStoreException {
      return new WrapperIterator(this.cache.keySet().iterator(), false) {
         @Override
         public Object transformObject(Object var1) {
            Object var2 = KeyValueTransformingCachedStore.this.toUserKey(var1);
            return var2 == null ? SKIP_TOKEN : var2;
         }
      };
   }

   protected Object toUserKey(Object var1) {
      return var1;
   }

   protected Object toCacheFetchKey(Object var1) {
      return var1;
   }

   protected Object toCachePutKey(Object var1) {
      return var1;
   }
}
