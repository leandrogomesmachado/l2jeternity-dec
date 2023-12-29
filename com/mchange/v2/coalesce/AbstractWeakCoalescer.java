package com.mchange.v2.coalesce;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

class AbstractWeakCoalescer implements Coalescer {
   Map wcoalesced;

   AbstractWeakCoalescer(Map var1) {
      this.wcoalesced = var1;
   }

   @Override
   public Object coalesce(Object var1) {
      Object var2 = null;
      WeakReference var3 = (WeakReference)this.wcoalesced.get(var1);
      if (var3 != null) {
         var2 = var3.get();
      }

      if (var2 == null) {
         this.wcoalesced.put(var1, new WeakReference<>(var1));
         var2 = var1;
      }

      return var2;
   }

   @Override
   public int countCoalesced() {
      return this.wcoalesced.size();
   }

   @Override
   public Iterator iterator() {
      return new CoalescerIterator(this.wcoalesced.keySet().iterator());
   }
}
