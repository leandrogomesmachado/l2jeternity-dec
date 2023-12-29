package com.mchange.v1.cachedstore;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class SimpleWritableCachedStore implements WritableCachedStore {
   private static final Object REMOVE_TOKEN = new Object();
   TweakableCachedStore readOnlyCache;
   WritableCachedStore.Manager manager;
   HashMap writeCache = new HashMap();
   Set failedWrites = null;

   SimpleWritableCachedStore(TweakableCachedStore var1, WritableCachedStore.Manager var2) {
      this.readOnlyCache = var1;
      this.manager = var2;
   }

   @Override
   public Object find(Object var1) throws CachedStoreException {
      Object var2 = this.writeCache.get(var1);
      if (var2 == null) {
         var2 = this.readOnlyCache.find(var1);
      }

      return var2 == REMOVE_TOKEN ? null : var2;
   }

   @Override
   public void write(Object var1, Object var2) {
      this.writeCache.put(var1, var2);
   }

   @Override
   public void remove(Object var1) {
      this.write(var1, REMOVE_TOKEN);
   }

   @Override
   public void flushWrites() throws CacheFlushException {
      HashMap var1 = (HashMap)this.writeCache.clone();

      for(Object var3 : var1.keySet()) {
         Object var4 = var1.get(var3);

         try {
            if (var4 == REMOVE_TOKEN) {
               this.manager.removeFromStorage(var3);
            } else {
               this.manager.writeToStorage(var3, var4);
            }

            try {
               this.readOnlyCache.setCachedValue(var3, var4);
               this.writeCache.remove(var3);
               if (this.failedWrites != null) {
                  this.failedWrites.remove(var3);
                  if (this.failedWrites.size() == 0) {
                     this.failedWrites = null;
                  }
               }
            } catch (CachedStoreException var6) {
               throw new CachedStoreError("SimpleWritableCachedStore: Internal cache is broken!");
            }
         } catch (Exception var7) {
            if (this.failedWrites == null) {
               this.failedWrites = new HashSet();
            }

            this.failedWrites.add(var3);
         }
      }

      if (this.failedWrites != null) {
         throw new CacheFlushException("Some keys failed to write!");
      }
   }

   @Override
   public Set getFailedWrites() {
      return this.failedWrites == null ? null : Collections.unmodifiableSet(this.failedWrites);
   }

   @Override
   public void clearPendingWrites() {
      this.writeCache.clear();
      this.failedWrites = null;
   }

   @Override
   public void reset() throws CachedStoreException {
      this.writeCache.clear();
      this.readOnlyCache.reset();
      this.failedWrites = null;
   }

   @Override
   public void sync() throws CachedStoreException {
      this.flushWrites();
      this.reset();
   }
}
