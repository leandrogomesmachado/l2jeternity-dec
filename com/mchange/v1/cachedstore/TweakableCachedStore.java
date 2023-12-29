package com.mchange.v1.cachedstore;

import java.util.Iterator;

public interface TweakableCachedStore extends CachedStore {
   Object getCachedValue(Object var1) throws CachedStoreException;

   void removeFromCache(Object var1) throws CachedStoreException;

   void setCachedValue(Object var1, Object var2) throws CachedStoreException;

   Iterator cachedKeys() throws CachedStoreException;
}
