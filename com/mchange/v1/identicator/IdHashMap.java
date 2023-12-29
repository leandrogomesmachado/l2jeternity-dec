package com.mchange.v1.identicator;

import java.util.HashMap;
import java.util.Map;

public final class IdHashMap extends IdMap implements Map {
   public IdHashMap(Identicator var1) {
      super(new HashMap(), var1);
   }

   @Override
   protected IdHashKey createIdKey(Object var1) {
      return new StrongIdHashKey(var1, this.id);
   }
}
