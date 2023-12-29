package com.mchange.v1.identicator;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

final class WeakIdHashKey extends IdHashKey {
   WeakIdHashKey.Ref keyRef;
   int hash;

   public WeakIdHashKey(Object var1, Identicator var2, ReferenceQueue var3) {
      super(var2);
      if (var1 == null) {
         throw new UnsupportedOperationException("Collection does not accept nulls!");
      } else {
         this.keyRef = new WeakIdHashKey.Ref(var1, var3);
         this.hash = var2.hash(var1);
      }
   }

   public WeakIdHashKey.Ref getInternalRef() {
      return this.keyRef;
   }

   @Override
   public Object getKeyObj() {
      return this.keyRef.get();
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 instanceof WeakIdHashKey) {
         WeakIdHashKey var2 = (WeakIdHashKey)var1;
         if (this.keyRef == var2.keyRef) {
            return true;
         } else {
            Object var3 = this.keyRef.get();
            Object var4 = var2.keyRef.get();
            return var3 != null && var4 != null ? this.id.identical(var3, var4) : false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   class Ref extends WeakReference {
      public Ref(Object var2, ReferenceQueue var3) {
         super(var2, var3);
      }

      WeakIdHashKey getKey() {
         return WeakIdHashKey.this;
      }
   }
}
