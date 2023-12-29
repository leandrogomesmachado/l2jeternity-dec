package com.mchange.v2.async;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.util.ResourceClosedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CarefulRunnableQueue implements RunnableQueue, Queuable, StrandedTaskReporting {
   private static final MLogger logger = MLog.getLogger(CarefulRunnableQueue.class);
   private List taskList = new LinkedList();
   private CarefulRunnableQueue.TaskThread t = new CarefulRunnableQueue.TaskThread();
   private boolean shutdown_on_interrupt;
   private boolean gentle_close_requested = false;
   private List strandedTasks = null;

   public CarefulRunnableQueue(boolean var1, boolean var2) {
      this.shutdown_on_interrupt = var2;
      this.t.setDaemon(var1);
      this.t.start();
   }

   @Override
   public RunnableQueue asRunnableQueue() {
      return this;
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      try {
         if (this.gentle_close_requested) {
            throw new ResourceClosedException("Attempted to post a task to a closing CarefulRunnableQueue.");
         } else {
            this.taskList.add(var1);
            this.notifyAll();
         }
      } catch (NullPointerException var3) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "NullPointerException while posting Runnable.", (Throwable)var3);
         }

         if (this.taskList == null) {
            throw new ResourceClosedException(
               "Attempted to post a task to a CarefulRunnableQueue which has been closed, or whose TaskThread has been interrupted."
            );
         } else {
            throw var3;
         }
      }
   }

   @Override
   public synchronized void close(boolean var1) {
      if (var1) {
         this.t.safeStop();
         this.t.interrupt();
      } else {
         this.gentle_close_requested = true;
      }
   }

   @Override
   public synchronized void close() {
      this.close(true);
   }

   @Override
   public synchronized List getStrandedTasks() {
      try {
         while(this.gentle_close_requested && this.taskList != null) {
            this.wait();
         }

         return this.strandedTasks;
      } catch (InterruptedException var2) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, Thread.currentThread() + " interrupted while waiting for stranded tasks from CarefulRunnableQueue.", (Throwable)var2);
         }

         throw new RuntimeException(Thread.currentThread() + " interrupted while waiting for stranded tasks from CarefulRunnableQueue.");
      }
   }

   private synchronized Runnable dequeueRunnable() {
      Runnable var1 = (Runnable)this.taskList.get(0);
      this.taskList.remove(0);
      return var1;
   }

   private synchronized void awaitTask() throws InterruptedException {
      for(; this.taskList.size() == 0; this.wait()) {
         if (this.gentle_close_requested) {
            this.t.safeStop();
            this.t.interrupt();
         }
      }
   }

   class TaskThread extends Thread {
      boolean should_stop = false;

      TaskThread() {
         super("CarefulRunnableQueue.TaskThread");
      }

      public synchronized void safeStop() {
         this.should_stop = true;
      }

      private synchronized boolean shouldStop() {
         return this.should_stop;
      }

      @Override
      public void run() {
         try {
            while(!this.shouldStop()) {
               try {
                  CarefulRunnableQueue.this.awaitTask();
                  Runnable var1 = CarefulRunnableQueue.this.dequeueRunnable();

                  try {
                     var1.run();
                  } catch (Exception var13) {
                     if (CarefulRunnableQueue.logger.isLoggable(MLevel.WARNING)) {
                        CarefulRunnableQueue.logger.log(MLevel.WARNING, this.getClass().getName() + " -- Unexpected exception in task!", (Throwable)var13);
                     }
                  }
               } catch (InterruptedException var14) {
                  if (CarefulRunnableQueue.this.shutdown_on_interrupt) {
                     CarefulRunnableQueue.this.close(false);
                     if (CarefulRunnableQueue.logger.isLoggable(MLevel.INFO)) {
                        CarefulRunnableQueue.logger.info(this.toString() + " interrupted. Shutting down after current tasks" + " have completed.");
                     }
                  } else {
                     CarefulRunnableQueue.logger.info(this.toString() + " received interrupt. IGNORING.");
                  }
               }
            }
         } finally {
            synchronized(CarefulRunnableQueue.this) {
               CarefulRunnableQueue.this.strandedTasks = Collections.unmodifiableList(CarefulRunnableQueue.this.taskList);
               CarefulRunnableQueue.this.taskList = null;
               CarefulRunnableQueue.this.t = null;
               CarefulRunnableQueue.this.notifyAll();
            }
         }
      }
   }
}
