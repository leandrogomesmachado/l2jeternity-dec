package com.mchange.v1.identicator;

public class StrongIdentityIdenticator implements Identicator {
   @Override
   public boolean identical(Object var1, Object var2) {
      return var1 == var2;
   }

   @Override
   public int hash(Object var1) {
      return System.identityHashCode(var1);
   }
}
