package com.mchange.v1.cachedstore;

import com.mchange.v1.util.IteratorUtils;
import java.util.Iterator;

class NoCacheCachedStore implements TweakableCachedStore {
   CachedStore.Manager mgr;

   NoCacheCachedStore(CachedStore.Manager var1) {
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
   public Object getCachedValue(Object var1) {
      return null;
   }

   @Override
   public void removeFromCache(Object var1) {
   }

   @Override
   public void setCachedValue(Object var1, Object var2) {
   }

   @Override
   public Iterator cachedKeys() {
      return IteratorUtils.EMPTY_ITERATOR;
   }
}
