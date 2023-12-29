package com.mchange.v2.async;

import java.util.LinkedList;
import java.util.List;

/** @deprecated */
public class SimpleRunnableQueue implements RunnableQueue, Queuable {
   private List taskList = new LinkedList();
   private Thread t = new SimpleRunnableQueue.TaskThread();
   boolean gentle_close_requested = false;

   public SimpleRunnableQueue(boolean var1) {
      this.t.setDaemon(var1);
      this.t.start();
   }

   public SimpleRunnableQueue() {
      this(true);
   }

   @Override
   public RunnableQueue asRunnableQueue() {
      return this;
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      if (this.gentle_close_requested) {
         throw new IllegalStateException("Attempted to post a task to a closed AsynchronousRunner.");
      } else {
         this.taskList.add(var1);
         this.notifyAll();
      }
   }

   @Override
   public synchronized void close(boolean var1) {
      if (var1) {
         this.t.interrupt();
      } else {
         this.gentle_close_requested = true;
      }
   }

   @Override
   public synchronized void close() {
      this.close(true);
   }

   private synchronized Runnable dequeueRunnable() {
      Runnable var1 = (Runnable)this.taskList.get(0);
      this.taskList.remove(0);
      return var1;
   }

   private synchronized void awaitTask() throws InterruptedException {
      for(; this.taskList.size() == 0; this.wait()) {
         if (this.gentle_close_requested) {
            this.t.interrupt();
         }
      }
   }

   class TaskThread extends Thread {
      TaskThread() {
         super("SimpleRunnableQueue.TaskThread");
      }

      @Override
      public void run() {
         try {
            while(!this.isInterrupted()) {
               SimpleRunnableQueue.this.awaitTask();
               Runnable var1 = SimpleRunnableQueue.this.dequeueRunnable();

               try {
                  var1.run();
               } catch (Exception var7) {
                  System.err.println(this.getClass().getName() + " -- Unexpected exception in task!");
                  var7.printStackTrace();
               }
            }
         } catch (InterruptedException var8) {
         } finally {
            SimpleRunnableQueue.this.taskList = null;
            SimpleRunnableQueue.this.t = null;
         }
      }
   }
}
