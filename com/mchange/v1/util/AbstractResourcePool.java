package com.mchange.v1.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** @deprecated */
public abstract class AbstractResourcePool {
   private static final boolean TRACE = true;
   private static final boolean DEBUG = true;
   private static RunnableQueue sharedQueue = new SimpleRunnableQueue();
   Set managed = new HashSet();
   List unused = new LinkedList();
   int start;
   int max;
   int inc;
   int num_acq_attempts = Integer.MAX_VALUE;
   int acq_attempt_delay = 50;
   RunnableQueue rq;
   boolean initted = false;
   boolean broken = false;

   protected AbstractResourcePool(int var1, int var2, int var3) {
      this(var1, var2, var3, sharedQueue);
   }

   protected AbstractResourcePool(int var1, int var2, int var3, RunnableQueue var4) {
      this.start = var1;
      this.max = var2;
      this.inc = var3;
      this.rq = var4;
   }

   protected abstract Object acquireResource() throws Exception;

   protected abstract void refurbishResource(Object var1) throws BrokenObjectException;

   protected abstract void destroyResource(Object var1) throws Exception;

   protected synchronized void init() throws Exception {
      for(int var1 = 0; var1 < this.start; ++var1) {
         this.assimilateResource();
      }

      this.initted = true;
   }

   protected Object checkoutResource() throws BrokenObjectException, InterruptedException, Exception {
      return this.checkoutResource(0L);
   }

   protected synchronized Object checkoutResource(long var1) throws BrokenObjectException, InterruptedException, AbstractResourcePool.TimeoutException, Exception {
      if (!this.initted) {
         this.init();
      }

      this.ensureNotBroken();
      int var3 = this.unused.size();
      if (var3 == 0) {
         int var4 = this.managed.size();
         if (var4 < this.max) {
            this.postAcquireMore();
         }

         this.awaitAvailable(var1);
      }

      Object var7 = this.unused.get(0);
      this.unused.remove(0);

      try {
         this.refurbishResource(var7);
      } catch (Exception var6) {
         var6.printStackTrace();
         this.removeResource(var7);
         return this.checkoutResource(var1);
      }

      this.trace();
      return var7;
   }

   protected synchronized void checkinResource(Object var1) throws BrokenObjectException {
      if (!this.managed.contains(var1)) {
         throw new IllegalArgumentException("ResourcePool: Tried to check-in a foreign resource!");
      } else {
         this.unused.add(var1);
         this.notifyAll();
         this.trace();
      }
   }

   protected synchronized void markBad(Object var1) throws Exception {
      this.removeResource(var1);
   }

   protected synchronized void close() throws Exception {
      this.broken = true;
      Iterator var1 = this.managed.iterator();

      while(var1.hasNext()) {
         try {
            this.removeResource(var1.next());
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }

   private void postAcquireMore() throws InterruptedException {
      this.rq.postRunnable(new AbstractResourcePool.AcquireTask());
   }

   private void awaitAvailable(long var1) throws InterruptedException, AbstractResourcePool.TimeoutException {
      int var3;
      while((var3 = this.unused.size()) == false) {
         this.wait(var1);
      }

      if (var3 == 0) {
         throw new AbstractResourcePool.TimeoutException();
      }
   }

   private void acquireMore() throws Exception {
      int var1 = this.managed.size();

      for(int var2 = 0; var2 < Math.min(this.inc, this.max - var1); ++var2) {
         this.assimilateResource();
      }
   }

   private void assimilateResource() throws Exception {
      Object var1 = this.acquireResource();
      this.managed.add(var1);
      this.unused.add(var1);
      this.notifyAll();
      this.trace();
   }

   private void removeResource(Object var1) throws Exception {
      this.managed.remove(var1);
      this.unused.remove(var1);
      this.destroyResource(var1);
      this.trace();
   }

   private void ensureNotBroken() throws BrokenObjectException {
      if (this.broken) {
         throw new BrokenObjectException(this);
      }
   }

   private synchronized void unexpectedBreak() {
      this.broken = true;
      Iterator var1 = this.unused.iterator();

      while(var1.hasNext()) {
         try {
            this.removeResource(var1.next());
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }

   private void trace() {
      System.err.println(this + "  [managed: " + this.managed.size() + ", " + "unused: " + this.unused.size() + ']');
   }

   class AcquireTask implements Runnable {
      boolean success = false;

      @Override
      public void run() {
         for(int var1 = 0; !this.success && var1 < AbstractResourcePool.this.num_acq_attempts; ++var1) {
            try {
               if (var1 > 0) {
                  Thread.sleep((long)AbstractResourcePool.this.acq_attempt_delay);
               }

               synchronized(AbstractResourcePool.this) {
                  AbstractResourcePool.this.acquireMore();
               }

               this.success = true;
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         if (!this.success) {
            AbstractResourcePool.this.unexpectedBreak();
         }
      }
   }

   protected class TimeoutException extends Exception {
   }
}
