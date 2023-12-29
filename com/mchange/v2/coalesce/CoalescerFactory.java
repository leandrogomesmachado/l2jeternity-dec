package com.mchange.v2.coalesce;

public final class CoalescerFactory {
   public static Coalescer createCoalescer() {
      return createCoalescer(true, true);
   }

   public static Coalescer createCoalescer(boolean var0, boolean var1) {
      return createCoalescer(null, var0, var1);
   }

   public static Coalescer createCoalescer(CoalesceChecker var0, boolean var1, boolean var2) {
      Object var3;
      if (var0 == null) {
         var3 = var1 ? new WeakEqualsCoalescer() : new StrongEqualsCoalescer();
      } else {
         var3 = var1 ? new WeakCcCoalescer(var0) : new StrongCcCoalescer(var0);
      }

      return (Coalescer)(var2 ? new SyncedCoalescer((Coalescer)var3) : var3);
   }
}
