package com.mchange.v1.lang;

public abstract class GentleThread extends Thread {
   boolean should_stop = false;
   boolean should_suspend = false;

   public GentleThread() {
   }

   public GentleThread(String var1) {
      super(var1);
   }

   @Override
   public abstract void run();

   public synchronized void gentleStop() {
      this.should_stop = true;
   }

   public synchronized void gentleSuspend() {
      this.should_suspend = true;
   }

   public synchronized void gentleResume() {
      this.should_suspend = false;
      this.notifyAll();
   }

   protected synchronized boolean shouldStop() {
      return this.should_stop;
   }

   protected synchronized boolean shouldSuspend() {
      return this.should_suspend;
   }

   protected synchronized void allowSuspend() throws InterruptedException {
      while(this.should_suspend) {
         this.wait();
      }
   }
}
