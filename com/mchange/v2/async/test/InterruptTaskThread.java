package com.mchange.v2.async.test;

import com.mchange.v2.async.RoundRobinAsynchronousRunner;
import com.mchange.v2.lang.ThreadUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InterruptTaskThread {
   static Set interruptedThreads = Collections.synchronizedSet(new HashSet());

   public static void main(String[] var0) {
      try {
         RoundRobinAsynchronousRunner var1 = new RoundRobinAsynchronousRunner(5, false);
         new InterruptTaskThread.Interrupter().start();

         for(int var2 = 0; var2 < 1000; ++var2) {
            try {
               var1.postRunnable(new InterruptTaskThread.DumbTask());
            } catch (Exception var4) {
               var4.printStackTrace();
            }

            Thread.sleep(50L);
         }

         System.out.println("Interrupted Threads: " + interruptedThreads.size());
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   static class DumbTask implements Runnable {
      static int count = 0;

      static synchronized int number() {
         return count++;
      }

      @Override
      public void run() {
         try {
            Thread.sleep(200L);
            System.out.println("DumbTask complete! " + number());
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }

   static class Interrupter extends Thread {
      @Override
      public void run() {
         try {
            while(true) {
               Thread[] var1 = new Thread[1000];
               ThreadUtils.enumerateAll(var1);
               int var2 = 0;

               while(true) {
                  if (var1[var2] != null) {
                     if (var1[var2].getName().indexOf("RunnableQueue.TaskThread") < 0) {
                        ++var2;
                        continue;
                     }

                     var1[var2].interrupt();
                     System.out.println("INTERRUPTED!");
                     InterruptTaskThread.interruptedThreads.add(var1[var2]);
                  }

                  Thread.sleep(1000L);
                  break;
               }
            }
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }
}
