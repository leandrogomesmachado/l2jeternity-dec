package com.mchange.v2.coalesce;

import java.util.Iterator;
import java.util.Map;

class AbstractStrongCoalescer implements Coalescer {
   Map coalesced;

   AbstractStrongCoalescer(Map var1) {
      this.coalesced = var1;
   }

   @Override
   public Object coalesce(Object var1) {
      Object var2 = this.coalesced.get(var1);
      if (var2 == null) {
         this.coalesced.put(var1, var1);
         var2 = var1;
      }

      return var2;
   }

   @Override
   public int countCoalesced() {
      return this.coalesced.size();
   }

   @Override
   public Iterator iterator() {
      return new CoalescerIterator(this.coalesced.keySet().iterator());
   }
}
