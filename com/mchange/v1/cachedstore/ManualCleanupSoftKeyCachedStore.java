package com.mchange.v1.cachedstore;

import java.lang.ref.ReferenceQueue;

class ManualCleanupSoftKeyCachedStore extends KeyTransformingCachedStore implements Vacuumable {
   ReferenceQueue queue = new ReferenceQueue();

   public ManualCleanupSoftKeyCachedStore(CachedStore.Manager var1) {
      super(var1);
   }

   @Override
   protected Object toUserKey(Object var1) {
      return ((SoftKey)var1).get();
   }

   @Override
   protected Object toCacheFetchKey(Object var1) {
      return new SoftKey(var1, null);
   }

   @Override
   protected Object toCachePutKey(Object var1) {
      return new SoftKey(var1, this.queue);
   }

   @Override
   public void vacuum() throws CachedStoreException {
      SoftKey var1;
      while((var1 = (SoftKey)this.queue.poll()) != null) {
         this.removeByTransformedKey(var1);
      }
   }
}
