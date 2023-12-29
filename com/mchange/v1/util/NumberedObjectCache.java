package com.mchange.v1.util;

import java.util.ArrayList;

public abstract class NumberedObjectCache {
   ArrayList al = new ArrayList();

   public Object getObject(int var1) throws Exception {
      Object var2 = null;
      int var3 = var1 + 1;
      if (var3 > this.al.size()) {
         this.al.ensureCapacity(var3 * 2);
         int var4 = this.al.size();

         for(int var5 = var3 * 2; var4 < var5; ++var4) {
            this.al.add(null);
         }

         var2 = this.addToCache(var1);
      } else {
         var2 = this.al.get(var1);
         if (var2 == null) {
            var2 = this.addToCache(var1);
         }
      }

      return var2;
   }

   private Object addToCache(int var1) throws Exception {
      Object var2 = this.findObject(var1);
      this.al.set(var1, var2);
      return var2;
   }

   protected abstract Object findObject(int var1) throws Exception;
}
