package com.mchange.v2.async;

import com.mchange.v2.io.IndentedWriter;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.util.ResourceClosedException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

public final class ThreadPoolAsynchronousRunner implements AsynchronousRunner {
   static final MLogger logger = MLog.getLogger(ThreadPoolAsynchronousRunner.class);
   static final int POLL_FOR_STOP_INTERVAL = 5000;
   static final int DFLT_DEADLOCK_DETECTOR_INTERVAL = 10000;
   static final int DFLT_INTERRUPT_DELAY_AFTER_APPARENT_DEADLOCK = 60000;
   static final int DFLT_MAX_INDIVIDUAL_TASK_TIME = 0;
   static final int DFLT_MAX_EMERGENCY_THREADS = 10;
   static final long PURGE_EVERY = 500L;
   int deadlock_detector_interval;
   int interrupt_delay_after_apparent_deadlock;
   int max_individual_task_time;
   int num_threads;
   boolean daemon;
   HashSet managed;
   HashSet available;
   LinkedList pendingTasks;
   Random rnd = new Random();
   Timer myTimer;
   boolean should_cancel_timer;
   TimerTask deadlockDetector = new ThreadPoolAsynchronousRunner.DeadlockDetector();
   TimerTask replacedThreadInterruptor = null;
   Map stoppedThreadsToStopDates = new HashMap();
   String threadLabel;

   private ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5, Timer var6, boolean var7, String var8) {
      this.num_threads = var1;
      this.daemon = var2;
      this.max_individual_task_time = var3;
      this.deadlock_detector_interval = var4;
      this.interrupt_delay_after_apparent_deadlock = var5;
      this.myTimer = var6;
      this.should_cancel_timer = var7;
      this.threadLabel = var8;
      this.recreateThreadsAndTasks();
      var6.schedule(this.deadlockDetector, (long)var4, (long)var4);
   }

   private ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5, Timer var6, boolean var7) {
      this(var1, var2, var3, var4, var5, var6, var7, null);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5, Timer var6, String var7) {
      this(var1, var2, var3, var4, var5, var6, false, var7);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5, Timer var6) {
      this(var1, var2, var3, var4, var5, var6, false);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5, String var6) {
      this(var1, var2, var3, var4, var5, new Timer(true), true, var6);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, int var3, int var4, int var5) {
      this(var1, var2, var3, var4, var5, new Timer(true), true);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, Timer var3, String var4) {
      this(var1, var2, 0, 10000, 60000, var3, false, var4);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2, Timer var3) {
      this(var1, var2, 0, 10000, 60000, var3, false);
   }

   public ThreadPoolAsynchronousRunner(int var1, boolean var2) {
      this(var1, var2, 0, 10000, 60000, new Timer(true), true);
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      try {
         this.pendingTasks.add(var1);
         this.notifyAll();
         if (logger.isLoggable(MLevel.FINEST)) {
            logger.log(MLevel.FINEST, this + ": Adding task to queue -- " + var1);
         }
      } catch (NullPointerException var3) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "NullPointerException while posting Runnable -- Probably we're closed.", (Throwable)var3);
         }

         throw new ResourceClosedException("Attempted to use a ThreadPoolAsynchronousRunner in a closed or broken state.");
      }
   }

   public synchronized int getThreadCount() {
      return this.managed.size();
   }

   @Override
   public void close(boolean var1) {
      synchronized(this) {
         if (this.managed != null) {
            this.deadlockDetector.cancel();
            if (this.should_cancel_timer) {
               this.myTimer.cancel();
            }

            this.myTimer = null;

            for(ThreadPoolAsynchronousRunner.PoolThread var4 : this.managed) {
               var4.gentleStop();
               if (var1) {
                  var4.interrupt();
               }
            }

            this.managed = null;
            if (!var1) {
               Iterator var7 = this.pendingTasks.iterator();

               while(var7.hasNext()) {
                  Runnable var8 = (Runnable)var7.next();
                  new Thread(var8).start();
                  var7.remove();
               }
            }

            this.available = null;
            this.pendingTasks = null;
         }
      }
   }

   @Override
   public void close() {
      this.close(true);
   }

   public synchronized int getActiveCount() {
      return this.managed.size() - this.available.size();
   }

   public synchronized int getIdleCount() {
      return this.available.size();
   }

   public synchronized int getPendingTaskCount() {
      return this.pendingTasks.size();
   }

   public synchronized String getStatus() {
      return this.getMultiLineStatusString();
   }

   public synchronized String getStackTraces() {
      return this.getStackTraces(0);
   }

   private String getStackTraces(int var1) {
      assert Thread.holdsLock(this);

      if (this.managed == null) {
         return null;
      } else {
         try {
            Method var2 = Thread.class.getMethod("getStackTrace", (Class<?>[])null);
            StringWriter var3 = new StringWriter(2048);
            IndentedWriter var4 = new IndentedWriter(var3);

            for(int var5 = 0; var5 < var1; ++var5) {
               var4.upIndent();
            }

            for(Object var6 : this.managed) {
               Object[] var7 = (Object[])var2.invoke(var6, (Object[])null);
               this.printStackTraces(var4, var6, var7);
            }

            for(int var11 = 0; var11 < var1; ++var11) {
               var4.downIndent();
            }

            var4.flush();
            String var12 = var3.toString();
            var4.close();
            return var12;
         } catch (NoSuchMethodException var8) {
            if (logger.isLoggable(MLevel.FINE)) {
               logger.fine(this + ": stack traces unavailable because this is a pre-Java 1.5 VM.");
            }

            return null;
         } catch (Exception var9) {
            if (logger.isLoggable(MLevel.FINE)) {
               logger.log(MLevel.FINE, this + ": An Exception occurred while trying to extract PoolThread stack traces.", (Throwable)var9);
            }

            return null;
         }
      }
   }

   private String getJvmStackTraces(int var1) {
      try {
         Method var2 = Thread.class.getMethod("getAllStackTraces", (Class<?>[])null);
         Map var3 = (Map)var2.invoke(null, (Object[])null);
         StringWriter var4 = new StringWriter(2048);
         IndentedWriter var5 = new IndentedWriter(var4);

         for(int var6 = 0; var6 < var1; ++var6) {
            var5.upIndent();
         }

         for(Entry var7 : var3.entrySet()) {
            Object var8 = var7.getKey();
            Object[] var9 = (Object[])var7.getValue();
            this.printStackTraces(var5, var8, var9);
         }

         for(int var13 = 0; var13 < var1; ++var13) {
            var5.downIndent();
         }

         var5.flush();
         String var14 = var4.toString();
         var5.close();
         return var14;
      } catch (NoSuchMethodException var10) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.fine(this + ": JVM stack traces unavailable because this is a pre-Java 1.5 VM.");
         }

         return null;
      } catch (Exception var11) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, this + ": An Exception occurred while trying to extract PoolThread stack traces.", (Throwable)var11);
         }

         return null;
      }
   }

   private void printStackTraces(IndentedWriter var1, Object var2, Object[] var3) throws IOException {
      var1.println(var2);
      var1.upIndent();
      int var4 = 0;

      for(int var5 = var3.length; var4 < var5; ++var4) {
         var1.println(var3[var4]);
      }

      var1.downIndent();
   }

   public synchronized String getMultiLineStatusString() {
      return this.getMultiLineStatusString(0);
   }

   private String getMultiLineStatusString(int var1) {
      try {
         StringWriter var2 = new StringWriter(2048);
         IndentedWriter var3 = new IndentedWriter(var2);

         for(int var4 = 0; var4 < var1; ++var4) {
            var3.upIndent();
         }

         if (this.managed == null) {
            var3.print("[");
            var3.print(this);
            var3.println(" closed.]");
         } else {
            HashSet var8 = (HashSet)this.managed.clone();
            var8.removeAll(this.available);
            var3.print("Managed Threads: ");
            var3.println(this.managed.size());
            var3.print("Active Threads: ");
            var3.println(var8.size());
            var3.println("Active Tasks: ");
            var3.upIndent();

            for(ThreadPoolAsynchronousRunner.PoolThread var6 : var8) {
               var3.println(var6.getCurrentTask());
               var3.upIndent();
               var3.print("on thread: ");
               var3.println(var6.getName());
               var3.downIndent();
            }

            var3.downIndent();
            var3.println("Pending Tasks: ");
            var3.upIndent();
            int var11 = 0;

            for(int var12 = this.pendingTasks.size(); var11 < var12; ++var11) {
               var3.println(this.pendingTasks.get(var11));
            }

            var3.downIndent();
         }

         for(int var9 = 0; var9 < var1; ++var9) {
            var3.downIndent();
         }

         var3.flush();
         String var10 = var2.toString();
         var3.close();
         return var10;
      } catch (IOException var7) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "Huh? An IOException when working with a StringWriter?!?", (Throwable)var7);
         }

         throw new RuntimeException("Huh? An IOException when working with a StringWriter?!? " + var7);
      }
   }

   private void appendStatusString(StringBuffer var1) {
      if (this.managed == null) {
         var1.append("[closed]");
      } else {
         HashSet var2 = (HashSet)this.managed.clone();
         var2.removeAll(this.available);
         var1.append("[num_managed_threads: ");
         var1.append(this.managed.size());
         var1.append(", num_active: ");
         var1.append(var2.size());
         var1.append("; activeTasks: ");
         boolean var3 = true;
         Iterator var4 = var2.iterator();

         while(var4.hasNext()) {
            if (var3) {
               var3 = false;
            } else {
               var1.append(", ");
            }

            ThreadPoolAsynchronousRunner.PoolThread var5 = (ThreadPoolAsynchronousRunner.PoolThread)var4.next();
            var1.append(var5.getCurrentTask());
            var1.append(" (");
            var1.append(var5.getName());
            var1.append(')');
         }

         var1.append("; pendingTasks: ");
         int var6 = 0;

         for(int var7 = this.pendingTasks.size(); var6 < var7; ++var6) {
            if (var6 != 0) {
               var1.append(", ");
            }

            var1.append(this.pendingTasks.get(var6));
         }

         var1.append(']');
      }
   }

   private void recreateThreadsAndTasks() {
      if (this.managed != null) {
         Date var1 = new Date();

         for(ThreadPoolAsynchronousRunner.PoolThread var3 : this.managed) {
            var3.gentleStop();
            this.stoppedThreadsToStopDates.put(var3, var1);
            this.ensureReplacedThreadsProcessing();
         }
      }

      this.managed = new HashSet();
      this.available = new HashSet();
      this.pendingTasks = new LinkedList();

      for(int var4 = 0; var4 < this.num_threads; ++var4) {
         ThreadPoolAsynchronousRunner.PoolThread var5 = new ThreadPoolAsynchronousRunner.PoolThread(var4, this.daemon);
         this.managed.add(var5);
         this.available.add(var5);
         var5.start();
      }
   }

   private void processReplacedThreads() {
      long var1 = System.currentTimeMillis();
      Iterator var3 = this.stoppedThreadsToStopDates.keySet().iterator();

      while(var3.hasNext()) {
         ThreadPoolAsynchronousRunner.PoolThread var4 = (ThreadPoolAsynchronousRunner.PoolThread)var3.next();
         if (!var4.isAlive()) {
            var3.remove();
         } else {
            Date var5 = (Date)this.stoppedThreadsToStopDates.get(var4);
            if (var1 - var5.getTime() > (long)this.interrupt_delay_after_apparent_deadlock) {
               if (logger.isLoggable(MLevel.WARNING)) {
                  logger.log(
                     MLevel.WARNING,
                     "Task "
                        + var4.getCurrentTask()
                        + " (in deadlocked PoolThread) failed to complete in maximum time "
                        + this.interrupt_delay_after_apparent_deadlock
                        + "ms. Trying interrupt()."
                  );
               }

               var4.interrupt();
               var3.remove();
            }
         }

         if (this.stoppedThreadsToStopDates.isEmpty()) {
            this.stopReplacedThreadsProcessing();
         }
      }
   }

   private void ensureReplacedThreadsProcessing() {
      if (this.replacedThreadInterruptor == null) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.fine("Apparently some threads have been replaced. Replacement thread processing enabled.");
         }

         this.replacedThreadInterruptor = new ThreadPoolAsynchronousRunner.ReplacedThreadInterruptor();
         int var1 = this.interrupt_delay_after_apparent_deadlock / 4;
         this.myTimer.schedule(this.replacedThreadInterruptor, (long)var1, (long)var1);
      }
   }

   private void stopReplacedThreadsProcessing() {
      if (this.replacedThreadInterruptor != null) {
         this.replacedThreadInterruptor.cancel();
         this.replacedThreadInterruptor = null;
         if (logger.isLoggable(MLevel.FINE)) {
            logger.fine("Apparently all replaced threads have either completed their tasks or been interrupted(). Replacement thread processing cancelled.");
         }
      }
   }

   private void shuttingDown(ThreadPoolAsynchronousRunner.PoolThread var1) {
      if (this.managed != null && this.managed.contains(var1)) {
         this.managed.remove(var1);
         this.available.remove(var1);
         ThreadPoolAsynchronousRunner.PoolThread var2 = new ThreadPoolAsynchronousRunner.PoolThread(var1.getIndex(), this.daemon);
         this.managed.add(var2);
         this.available.add(var2);
         var2.start();
      }
   }

   private void runInEmergencyThread(Runnable var1) {
      Thread var2 = new Thread(var1);
      var2.start();
      if (this.max_individual_task_time > 0) {
         ThreadPoolAsynchronousRunner.MaxIndividualTaskTimeEnforcer var3 = new ThreadPoolAsynchronousRunner.MaxIndividualTaskTimeEnforcer(
            var2, var2 + " [One-off emergency thread!!!]", var1.toString()
         );
         this.myTimer.schedule(var3, (long)this.max_individual_task_time);
      }
   }

   class DeadlockDetector extends TimerTask {
      LinkedList last = null;
      LinkedList current = null;

      @Override
      public void run() {
         boolean var1 = false;
         synchronized(ThreadPoolAsynchronousRunner.this) {
            if (ThreadPoolAsynchronousRunner.this.pendingTasks.size() == 0) {
               this.last = null;
               if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.FINEST)) {
                  ThreadPoolAsynchronousRunner.logger.log(MLevel.FINEST, this + " -- Running DeadlockDetector[Exiting. No pending tasks.]");
               }

               return;
            }

            this.current = (LinkedList)ThreadPoolAsynchronousRunner.this.pendingTasks.clone();
            if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.FINEST)) {
               ThreadPoolAsynchronousRunner.logger
                  .log(MLevel.FINEST, this + " -- Running DeadlockDetector[last->" + this.last + ",current->" + this.current + ']');
            }

            if (this.current.equals(this.last)) {
               if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
                  ThreadPoolAsynchronousRunner.logger.warning(this + " -- APPARENT DEADLOCK!!! Creating emergency threads for unassigned pending tasks!");
                  StringWriter var3 = new StringWriter(4096);
                  PrintWriter var4 = new PrintWriter(var3);
                  var4.print(this);
                  var4.println(" -- APPARENT DEADLOCK!!! Complete Status: ");
                  var4.print(ThreadPoolAsynchronousRunner.this.getMultiLineStatusString(1));
                  var4.println("Pool thread stack traces:");
                  String var5 = ThreadPoolAsynchronousRunner.this.getStackTraces(1);
                  if (var5 == null) {
                     var4.println("\t[Stack traces of deadlocked task threads not available.]");
                  } else {
                     var4.print(var5);
                  }

                  var4.flush();
                  ThreadPoolAsynchronousRunner.logger.warning(var3.toString());
                  var4.close();
               }

               if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.FINEST)) {
                  StringWriter var9 = new StringWriter(4096);
                  PrintWriter var11 = new PrintWriter(var9);
                  var11.print(this);
                  var11.println(" -- APPARENT DEADLOCK extra info, full JVM thread dump: ");
                  String var12 = ThreadPoolAsynchronousRunner.this.getJvmStackTraces(1);
                  if (var12 == null) {
                     var11.println("\t[Full JVM thread dump not available.]");
                  } else {
                     var11.print(var12);
                  }

                  var11.flush();
                  ThreadPoolAsynchronousRunner.logger.finest(var9.toString());
                  var11.close();
               }

               ThreadPoolAsynchronousRunner.this.recreateThreadsAndTasks();
               var1 = true;
            }
         }

         if (var1) {
            ThreadPerTaskAsynchronousRunner var8 = new ThreadPerTaskAsynchronousRunner(10, (long)ThreadPoolAsynchronousRunner.this.max_individual_task_time);
            Iterator var10 = this.current.iterator();

            while(var10.hasNext()) {
               var8.postRunnable((Runnable)var10.next());
            }

            var8.close(false);
            this.last = null;
         } else {
            this.last = this.current;
         }

         this.current = null;
      }
   }

   class MaxIndividualTaskTimeEnforcer extends TimerTask {
      ThreadPoolAsynchronousRunner.PoolThread pt;
      Thread interruptMe;
      String threadStr;
      String fixedTaskStr;

      MaxIndividualTaskTimeEnforcer(ThreadPoolAsynchronousRunner.PoolThread var2) {
         this.pt = var2;
         this.interruptMe = var2;
         this.threadStr = var2.toString();
         this.fixedTaskStr = null;
      }

      MaxIndividualTaskTimeEnforcer(Thread var2, String var3, String var4) {
         this.pt = null;
         this.interruptMe = var2;
         this.threadStr = var3;
         this.fixedTaskStr = var4;
      }

      @Override
      public void run() {
         String var1;
         if (this.fixedTaskStr != null) {
            var1 = this.fixedTaskStr;
         } else if (this.pt != null) {
            synchronized(ThreadPoolAsynchronousRunner.this) {
               var1 = String.valueOf(this.pt.getCurrentTask());
            }
         } else {
            var1 = "Unknown task?!";
         }

         if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
            ThreadPoolAsynchronousRunner.logger
               .warning("A task has exceeded the maximum allowable task time. Will interrupt() thread [" + this.threadStr + "], with current task: " + var1);
         }

         this.interruptMe.interrupt();
         if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
            ThreadPoolAsynchronousRunner.logger.warning("Thread [" + this.threadStr + "] interrupted.");
         }
      }
   }

   class PoolThread extends Thread {
      Runnable currentTask;
      boolean should_stop;
      int index;
      TimerTask maxIndividualTaskTimeEnforcer = null;

      PoolThread(int var2, boolean var3) {
         this.setName(
            (ThreadPoolAsynchronousRunner.this.threadLabel == null ? this.getClass().getName() : ThreadPoolAsynchronousRunner.this.threadLabel) + "-#" + var2
         );
         this.setDaemon(var3);
         this.index = var2;
      }

      public int getIndex() {
         return this.index;
      }

      void gentleStop() {
         this.should_stop = true;
      }

      Runnable getCurrentTask() {
         return this.currentTask;
      }

      private void setMaxIndividualTaskTimeEnforcer() {
         this.maxIndividualTaskTimeEnforcer = ThreadPoolAsynchronousRunner.this.new MaxIndividualTaskTimeEnforcer(this);
         ThreadPoolAsynchronousRunner.this.myTimer
            .schedule(this.maxIndividualTaskTimeEnforcer, (long)ThreadPoolAsynchronousRunner.this.max_individual_task_time);
      }

      private void cancelMaxIndividualTaskTimeEnforcer() {
         this.maxIndividualTaskTimeEnforcer.cancel();
         this.maxIndividualTaskTimeEnforcer = null;
      }

      private void purgeTimer() {
         ThreadPoolAsynchronousRunner.this.myTimer.purge();
         if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.FINER)) {
            ThreadPoolAsynchronousRunner.logger.log(MLevel.FINER, this.getClass().getName() + " -- PURGING TIMER");
         }
      }

      @Override
      public void run() {
         long var1 = ThreadPoolAsynchronousRunner.this.rnd.nextLong();

         try {
            while(true) {
               Runnable var3;
               synchronized(ThreadPoolAsynchronousRunner.this) {
                  while(!this.should_stop && ThreadPoolAsynchronousRunner.this.pendingTasks.size() == 0) {
                     ThreadPoolAsynchronousRunner.this.wait(5000L);
                  }

                  if (this.should_stop) {
                     break;
                  }

                  if (!ThreadPoolAsynchronousRunner.this.available.remove(this)) {
                     throw new InternalError("An unavailable PoolThread tried to check itself out!!!");
                  }

                  var3 = (Runnable)ThreadPoolAsynchronousRunner.this.pendingTasks.remove(0);
                  this.currentTask = var3;
               }

               try {
                  if (ThreadPoolAsynchronousRunner.this.max_individual_task_time > 0) {
                     this.setMaxIndividualTaskTimeEnforcer();
                  }

                  var3.run();
               } catch (RuntimeException var42) {
                  if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
                     ThreadPoolAsynchronousRunner.logger
                        .log(MLevel.WARNING, this + " -- caught unexpected Exception while executing posted task.", (Throwable)var42);
                  }
               } finally {
                  if (this.maxIndividualTaskTimeEnforcer != null) {
                     this.cancelMaxIndividualTaskTimeEnforcer();
                     var1 ^= var1 << 21;
                     var1 ^= var1 >>> 35;
                     var1 ^= var1 << 4;
                     if (var1 % 500L == 0L) {
                        this.purgeTimer();
                     }
                  }

                  synchronized(ThreadPoolAsynchronousRunner.this) {
                     if (!this.should_stop) {
                        if (ThreadPoolAsynchronousRunner.this.available != null && !ThreadPoolAsynchronousRunner.this.available.add(this)) {
                           throw new InternalError("An apparently available PoolThread tried to check itself in!!!");
                        }

                        this.currentTask = null;
                     }
                     break;
                  }
               }
            }
         } catch (InterruptedException var45) {
         } catch (RuntimeException var46) {
            if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
               ThreadPoolAsynchronousRunner.logger
                  .log(MLevel.WARNING, "An unexpected RuntimException is implicated in the closing of " + this, (Throwable)var46);
            }

            throw var46;
         } catch (Error var47) {
            if (ThreadPoolAsynchronousRunner.logger.isLoggable(MLevel.WARNING)) {
               ThreadPoolAsynchronousRunner.logger
                  .log(
                     MLevel.WARNING,
                     "An Error forced the closing of " + this + ". Will attempt to reconstruct, but this might mean that something bad is happening.",
                     (Throwable)var47
                  );
            }

            throw var47;
         } finally {
            synchronized(ThreadPoolAsynchronousRunner.this) {
               ThreadPoolAsynchronousRunner.this.shuttingDown(this);
            }
         }
      }
   }

   class ReplacedThreadInterruptor extends TimerTask {
      @Override
      public void run() {
         synchronized(ThreadPoolAsynchronousRunner.this) {
            ThreadPoolAsynchronousRunner.this.processReplacedThreads();
         }
      }
   }
}
