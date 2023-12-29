package com.mchange.util.impl;

import com.mchange.util.ObjectCache;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public abstract class SoftReferenceObjectCache implements ObjectCache {
   Map store = new HashMap();

   @Override
   public synchronized Object find(Object var1) throws Exception {
      Reference var2 = (Reference)this.store.get(var1);
      Object var3;
      if (var2 == null || (var3 = var2.get()) == null || this.isDirty(var1, var3)) {
         var3 = this.createFromKey(var1);
         this.store.put(var1, new SoftReference<>(var3));
      }

      return var3;
   }

   protected boolean isDirty(Object var1, Object var2) {
      return false;
   }

   protected abstract Object createFromKey(Object var1) throws Exception;
}
