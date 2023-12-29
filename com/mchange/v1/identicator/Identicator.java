package com.mchange.v1.identicator;

public interface Identicator {
   boolean identical(Object var1, Object var2);

   int hash(Object var1);
}
