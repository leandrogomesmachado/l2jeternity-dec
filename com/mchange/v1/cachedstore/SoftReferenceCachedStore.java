package com.mchange.v1.cachedstore;

import java.lang.ref.SoftReference;

class SoftReferenceCachedStore extends ValueTransformingCachedStore {
   public SoftReferenceCachedStore(CachedStore.Manager var1) {
      super(var1);
   }

   @Override
   protected Object toUserValue(Object var1) {
      return var1 == null ? null : ((SoftReference)var1).get();
   }

   @Override
   protected Object toCacheValue(Object var1) {
      return var1 == null ? null : new SoftReference<>(var1);
   }
}
