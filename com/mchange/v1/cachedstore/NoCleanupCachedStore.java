package com.mchange.v1.cachedstore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class NoCleanupCachedStore implements TweakableCachedStore {
   static final boolean DEBUG = true;
   protected Map cache = new HashMap();
   CachedStore.Manager manager;

   public NoCleanupCachedStore(CachedStore.Manager var1) {
      this.manager = var1;
   }

   @Override
   public Object find(Object var1) throws CachedStoreException {
      try {
         Object var2 = this.getCachedValue(var1);
         if (var2 == null || this.manager.isDirty(var1, var2)) {
            var2 = this.manager.recreateFromKey(var1);
            if (var2 != null) {
               this.setCachedValue(var1, var2);
            }
         }

         return var2;
      } catch (CachedStoreException var3) {
         throw var3;
      } catch (Exception var4) {
         var4.printStackTrace();
         throw new CachedStoreException(var4);
      }
   }

   @Override
   public Object getCachedValue(Object var1) {
      return this.cache.get(var1);
   }

   @Override
   public void removeFromCache(Object var1) throws CachedStoreException {
      this.cache.remove(var1);
   }

   @Override
   public void setCachedValue(Object var1, Object var2) throws CachedStoreException {
      this.cache.put(var1, var2);
   }

   @Override
   public Iterator cachedKeys() throws CachedStoreException {
      return this.cache.keySet().iterator();
   }

   @Override
   public void reset() {
      this.cache.clear();
   }
}
