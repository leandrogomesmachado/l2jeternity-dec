package com.mchange.v2.async;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.util.ResourceClosedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class ThreadPerTaskAsynchronousRunner implements AsynchronousRunner {
   static final int PRESUME_DEADLOCKED_MULTIPLE = 3;
   static final MLogger logger = MLog.getLogger(ThreadPerTaskAsynchronousRunner.class);
   final int max_task_threads;
   final long interrupt_task_delay;
   LinkedList queue = new LinkedList();
   ArrayList running = new ArrayList();
   ArrayList deadlockSnapshot = null;
   boolean still_open = true;
   Thread dispatchThread = new ThreadPerTaskAsynchronousRunner.DispatchThread();
   Timer interruptAndDeadlockTimer;

   public ThreadPerTaskAsynchronousRunner(int var1) {
      this(var1, 0L);
   }

   public ThreadPerTaskAsynchronousRunner(int var1, long var2) {
      this.max_task_threads = var1;
      this.interrupt_task_delay = var2;
      if (this.hasIdTimer()) {
         this.interruptAndDeadlockTimer = new Timer(true);
         TimerTask var4 = new TimerTask() {
            @Override
            public void run() {
               ThreadPerTaskAsynchronousRunner.this.checkForDeadlock();
            }
         };
         long var5 = var2 * 3L;
         this.interruptAndDeadlockTimer.schedule(var4, var5, var5);
      }

      this.dispatchThread.start();
   }

   private boolean hasIdTimer() {
      return this.interrupt_task_delay > 0L;
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      if (this.still_open) {
         this.queue.add(var1);
         this.notifyAll();
      } else {
         throw new ResourceClosedException("Attempted to use a ThreadPerTaskAsynchronousRunner in a closed or broken state.");
      }
   }

   @Override
   public void close() {
      this.close(true);
   }

   @Override
   public synchronized void close(boolean var1) {
      if (this.still_open) {
         this.still_open = false;
         if (var1) {
            this.queue.clear();
            Iterator var2 = this.running.iterator();

            while(var2.hasNext()) {
               ((Thread)var2.next()).interrupt();
            }

            this.closeThreadResources();
         }
      }
   }

   public synchronized int getRunningCount() {
      return this.running.size();
   }

   public synchronized Collection getRunningTasks() {
      return (Collection)this.running.clone();
   }

   public synchronized int getWaitingCount() {
      return this.queue.size();
   }

   public synchronized Collection getWaitingTasks() {
      return (Collection)this.queue.clone();
   }

   public synchronized boolean isClosed() {
      return !this.still_open;
   }

   public synchronized boolean isDoneAndGone() {
      return !this.dispatchThread.isAlive() && this.running.isEmpty() && this.interruptAndDeadlockTimer == null;
   }

   private synchronized void acknowledgeComplete(ThreadPerTaskAsynchronousRunner.TaskThread var1) {
      if (!var1.isCompleted()) {
         this.running.remove(var1);
         var1.markCompleted();
         this.notifyAll();
         if (!this.still_open && this.queue.isEmpty() && this.running.isEmpty()) {
            this.closeThreadResources();
         }
      }
   }

   private synchronized void checkForDeadlock() {
      if (this.deadlockSnapshot == null) {
         if (this.running.size() == this.max_task_threads) {
            this.deadlockSnapshot = (ArrayList)this.running.clone();
         }
      } else if (this.running.size() < this.max_task_threads) {
         this.deadlockSnapshot = null;
      } else if (this.deadlockSnapshot.equals(this.running)) {
         if (logger.isLoggable(MLevel.WARNING)) {
            StringBuffer var1 = new StringBuffer(1024);
            var1.append("APPARENT DEADLOCK! (");
            var1.append(this);
            var1.append(") Deadlocked threads (unresponsive to interrupt()) are being set aside as hopeless and up to ");
            var1.append(this.max_task_threads);
            var1.append(" may now be spawned for new tasks. If tasks continue to deadlock, you may run out of memory. Deadlocked task list: ");
            int var2 = 0;

            for(int var3 = this.deadlockSnapshot.size(); var2 < var3; ++var2) {
               if (var2 != 0) {
                  var1.append(", ");
               }

               var1.append(((ThreadPerTaskAsynchronousRunner.TaskThread)this.deadlockSnapshot.get(var2)).getTask());
            }

            logger.log(MLevel.WARNING, var1.toString());
         }

         int var4 = 0;

         for(int var5 = this.deadlockSnapshot.size(); var4 < var5; ++var4) {
            this.acknowledgeComplete((ThreadPerTaskAsynchronousRunner.TaskThread)this.deadlockSnapshot.get(var4));
         }

         this.deadlockSnapshot = null;
      } else {
         this.deadlockSnapshot = (ArrayList)this.running.clone();
      }
   }

   private void closeThreadResources() {
      if (this.interruptAndDeadlockTimer != null) {
         this.interruptAndDeadlockTimer.cancel();
         this.interruptAndDeadlockTimer = null;
      }

      this.dispatchThread.interrupt();
   }

   class DispatchThread extends Thread {
      DispatchThread() {
         super("Dispatch-Thread-for-" + ThreadPerTaskAsynchronousRunner.this);
      }

      @Override
      public void run() {
         synchronized(ThreadPerTaskAsynchronousRunner.this) {
            try {
               while(true) {
                  while(
                     ThreadPerTaskAsynchronousRunner.this.queue.isEmpty()
                        || ThreadPerTaskAsynchronousRunner.this.running.size() == ThreadPerTaskAsynchronousRunner.this.max_task_threads
                  ) {
                     ThreadPerTaskAsynchronousRunner.this.wait();
                  }

                  Runnable var2 = (Runnable)ThreadPerTaskAsynchronousRunner.this.queue.remove(0);
                  ThreadPerTaskAsynchronousRunner.TaskThread var3 = ThreadPerTaskAsynchronousRunner.this.new TaskThread(var2);
                  var3.start();
                  ThreadPerTaskAsynchronousRunner.this.running.add(var3);
               }
            } catch (InterruptedException var5) {
               if (ThreadPerTaskAsynchronousRunner.this.still_open) {
                  if (ThreadPerTaskAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
                     ThreadPerTaskAsynchronousRunner.logger.log(MLevel.WARNING, this.getName() + " unexpectedly interrupted! Shutting down!");
                  }

                  ThreadPerTaskAsynchronousRunner.this.close(false);
               }
            }
         }
      }
   }

   class TaskThread extends Thread {
      Runnable r;
      boolean completed = false;

      TaskThread(Runnable var2) {
         super("Task-Thread-for-" + ThreadPerTaskAsynchronousRunner.this);
         this.r = var2;
      }

      Runnable getTask() {
         return this.r;
      }

      synchronized void markCompleted() {
         this.completed = true;
      }

      synchronized boolean isCompleted() {
         return this.completed;
      }

      @Override
      public void run() {
         try {
            if (ThreadPerTaskAsynchronousRunner.this.hasIdTimer()) {
               TimerTask var1 = new TimerTask() {
                  @Override
                  public void run() {
                     TaskThread.this.interrupt();
                  }
               };
               ThreadPerTaskAsynchronousRunner.this.interruptAndDeadlockTimer.schedule(var1, ThreadPerTaskAsynchronousRunner.this.interrupt_task_delay);
            }

            this.r.run();
         } finally {
            ThreadPerTaskAsynchronousRunner.this.acknowledgeComplete(this);
         }
      }
   }
}
