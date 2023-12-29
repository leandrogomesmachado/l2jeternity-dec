package com.mchange.v2.coalesce;

public class CoalesceTest {
   static final int NUM_ITERS = 10000;
   static final Coalescer c = CoalescerFactory.createCoalescer(null, true, true);

   public static void main(String[] var0) {
      doTest();
      System.gc();
      System.err.println("num coalesced after gc: " + c.countCoalesced());
   }

   private static void doTest() {
      String[] var0 = new String[10000];

      for(int var1 = 0; var1 < 10000; ++var1) {
         var0[var1] = new String("Hello");
      }

      long var6 = System.currentTimeMillis();

      for(int var3 = 0; var3 < 10000; ++var3) {
         String var4 = var0[var3];
         Object var5 = c.coalesce(var4);
      }

      long var7 = System.currentTimeMillis() - var6;
      System.out.println("avg time: " + (float)var7 / 10000.0F + "ms (" + 10000 + " iterations)");
      System.err.println("num coalesced: " + c.countCoalesced());
   }
}
