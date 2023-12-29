package com.mchange.v1.util;

import java.util.LinkedList;
import java.util.List;

/** @deprecated */
public class SimpleRunnableQueue implements RunnableQueue {
   private List taskList = new LinkedList();
   private Thread t = new SimpleRunnableQueue.TaskThread();

   public SimpleRunnableQueue(boolean var1) {
      this.t.setDaemon(var1);
      this.t.start();
   }

   public SimpleRunnableQueue() {
      this(true);
   }

   @Override
   public synchronized void postRunnable(Runnable var1) {
      this.taskList.add(var1);
      this.notifyAll();
   }

   public synchronized void close() {
      this.t.interrupt();
      this.taskList = null;
      this.t = null;
   }

   private synchronized Runnable dequeueRunnable() {
      Runnable var1 = (Runnable)this.taskList.get(0);
      this.taskList.remove(0);
      return var1;
   }

   private synchronized void awaitTask() throws InterruptedException {
      while(this.taskList.size() == 0) {
         this.wait();
      }
   }

   class TaskThread extends Thread {
      TaskThread() {
         super("SimpleRunnableQueue.TaskThread");
      }

      @Override
      public void run() {
         try {
            while(true) {
               SimpleRunnableQueue.this.awaitTask();
               Runnable var1 = SimpleRunnableQueue.this.dequeueRunnable();

               try {
                  var1.run();
               } catch (Exception var3) {
                  System.err.println(this.getClass().getName() + " -- Unexpected exception in task!");
                  var3.printStackTrace();
               }
            }
         } catch (InterruptedException var4) {
            System.err.println(this.toString() + " interrupted. Shutting down.");
         }
      }
   }
}
