package com.mchange.util.impl;

import com.mchange.util.ObjectCache;
import java.util.Hashtable;

public abstract class NoGCObjectCache implements ObjectCache {
   Hashtable store = new Hashtable();

   @Override
   public Object find(Object var1) throws Exception {
      Object var2 = this.store.get(var1);
      if (var2 == null || this.isDirty(var1, var2)) {
         var2 = this.createFromKey(var1);
         this.store.put(var1, var2);
      }

      return var2;
   }

   protected boolean isDirty(Object var1, Object var2) {
      return false;
   }

   protected abstract Object createFromKey(Object var1) throws Exception;
}
