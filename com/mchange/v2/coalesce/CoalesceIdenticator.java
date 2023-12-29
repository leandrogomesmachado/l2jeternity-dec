package com.mchange.v2.coalesce;

import com.mchange.v1.identicator.Identicator;

class CoalesceIdenticator implements Identicator {
   CoalesceChecker cc;

   CoalesceIdenticator(CoalesceChecker var1) {
      this.cc = var1;
   }

   @Override
   public boolean identical(Object var1, Object var2) {
      return this.cc.checkCoalesce(var1, var2);
   }

   @Override
   public int hash(Object var1) {
      return this.cc.coalesceHash(var1);
   }
}
