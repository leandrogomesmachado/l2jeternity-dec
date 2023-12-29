package com.mchange.v1.cachedstore;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

final class SoftKey extends SoftReference {
   int hash_code;

   SoftKey(Object var1, ReferenceQueue var2) {
      super(var1, var2);
      this.hash_code = var1.hashCode();
   }

   @Override
   public int hashCode() {
      return this.hash_code;
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         Object var2 = this.get();
         if (var2 == null) {
            return false;
         } else if (this.getClass() == var1.getClass()) {
            SoftKey var3 = (SoftKey)var1;
            return var2.equals(var3.get());
         } else {
            return false;
         }
      }
   }
}
