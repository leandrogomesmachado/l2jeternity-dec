package com.mchange.v1.cachedstore;

import com.mchange.lang.PotentiallySecondaryError;

public class CachedStoreError extends PotentiallySecondaryError {
   public CachedStoreError(String var1, Throwable var2) {
      super(var1, var2);
   }

   public CachedStoreError(Throwable var1) {
      super(var1);
   }

   public CachedStoreError(String var1) {
      super(var1);
   }

   public CachedStoreError() {
   }
}
