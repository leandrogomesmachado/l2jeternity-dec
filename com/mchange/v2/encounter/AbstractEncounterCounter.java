package com.mchange.v2.encounter;

import java.util.Map;

class AbstractEncounterCounter implements EncounterCounter {
   static final Long ONE = new Long(1L);
   Map m;

   AbstractEncounterCounter(Map var1) {
      this.m = var1;
   }

   @Override
   public long encounter(Object var1) {
      Long var2 = (Long)this.m.get(var1);
      Long var3;
      long var4;
      if (var2 == null) {
         var4 = 0L;
         var3 = ONE;
      } else {
         var4 = var2;
         var3 = new Long(var4 + 1L);
      }

      this.m.put(var1, var3);
      return var4;
   }

   @Override
   public long reset(Object var1) {
      long var2 = this.encounter(var1);
      this.m.remove(var1);
      return var2;
   }

   @Override
   public void resetAll() {
      this.m.clear();
   }
}
