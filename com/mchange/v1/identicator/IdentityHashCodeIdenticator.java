package com.mchange.v1.identicator;

public class IdentityHashCodeIdenticator implements Identicator {
   public static IdentityHashCodeIdenticator INSTANCE = new IdentityHashCodeIdenticator();

   @Override
   public boolean identical(Object var1, Object var2) {
      return System.identityHashCode(var1) == System.identityHashCode(var2);
   }

   @Override
   public int hash(Object var1) {
      return System.identityHashCode(var1);
   }
}
