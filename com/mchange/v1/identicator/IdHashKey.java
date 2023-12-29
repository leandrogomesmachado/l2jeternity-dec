package com.mchange.v1.identicator;

abstract class IdHashKey {
   Identicator id;

   public IdHashKey(Identicator var1) {
      this.id = var1;
   }

   public abstract Object getKeyObj();

   public Identicator getIdenticator() {
      return this.id;
   }

   @Override
   public abstract boolean equals(Object var1);

   @Override
   public abstract int hashCode();
}
