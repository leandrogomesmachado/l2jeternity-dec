package com.mchange.v2.lang;

public final class ThreadGroupUtils {
   public static ThreadGroup rootThreadGroup() {
      ThreadGroup var0 = Thread.currentThread().getThreadGroup();

      for(ThreadGroup var1 = var0.getParent(); var1 != null; var1 = var1.getParent()) {
         var0 = var1;
      }

      return var0;
   }

   private ThreadGroupUtils() {
   }
}
