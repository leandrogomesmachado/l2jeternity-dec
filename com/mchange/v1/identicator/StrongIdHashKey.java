package com.mchange.v1.identicator;

final class StrongIdHashKey extends IdHashKey {
   Object keyObj;

   public StrongIdHashKey(Object var1, Identicator var2) {
      super(var2);
      this.keyObj = var1;
   }

   @Override
   public Object getKeyObj() {
      return this.keyObj;
   }

   @Override
   public boolean equals(Object var1) {
      return var1 instanceof StrongIdHashKey ? this.id.identical(this.keyObj, ((StrongIdHashKey)var1).keyObj) : false;
   }

   @Override
   public int hashCode() {
      return this.id.hash(this.keyObj);
   }
}
