package com.mchange.v2.async;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.util.ResourceClosedException;

public class RoundRobinAsynchronousRunner implements AsynchronousRunner, Queuable {
   private static final MLogger logger = MLog.getLogger(RoundRobinAsynchronousRunner.class);
   final RunnableQueue[] rqs;
   int task_turn = 0;
   int view_turn = 0;

   public RoundRobinAsynchronousRunner(int var1, boolean var2) {
      this.rqs = new RunnableQueue[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         this.rqs[var3] = new CarefulRunnableQueue(var2, false);
      }
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      try {
         int var2 = this.task_turn;
         this.task_turn = (this.task_turn + 1) % this.rqs.length;
         this.rqs[var2].postRunnable(var1);
      } catch (NullPointerException var3) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "NullPointerException while posting Runnable -- Probably we're closed.", (Throwable)var3);
         }

         this.close(true);
         throw new ResourceClosedException("Attempted to use a RoundRobinAsynchronousRunner in a closed or broken state.");
      }
   }

   @Override
   public synchronized RunnableQueue asRunnableQueue() {
      try {
         int var1 = this.view_turn;
         this.view_turn = (this.view_turn + 1) % this.rqs.length;
         return new RoundRobinAsynchronousRunner.RunnableQueueView(var1);
      } catch (NullPointerException var2) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "NullPointerException in asRunnableQueue() -- Probably we're closed.", (Throwable)var2);
         }

         this.close(true);
         throw new ResourceClosedException("Attempted to use a RoundRobinAsynchronousRunner in a closed or broken state.");
      }
   }

   @Override
   public synchronized void close(boolean var1) {
      int var2 = 0;

      for(int var3 = this.rqs.length; var2 < var3; ++var2) {
         attemptClose(this.rqs[var2], var1);
         this.rqs[var2] = null;
      }
   }

   @Override
   public void close() {
      this.close(true);
   }

   static void attemptClose(RunnableQueue var0, boolean var1) {
      try {
         var0.close(var1);
      } catch (Exception var3) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "RunnableQueue close FAILED.", (Throwable)var3);
         }
      }
   }

   class RunnableQueueView implements RunnableQueue {
      final int rq_num;

      RunnableQueueView(int var2) {
         this.rq_num = var2;
      }

      @Override
      public void postRunnable(Runnable var1) {
         RoundRobinAsynchronousRunner.this.rqs[this.rq_num].postRunnable(var1);
      }

      @Override
      public void close(boolean var1) {
      }

      @Override
      public void close() {
      }
   }
}
