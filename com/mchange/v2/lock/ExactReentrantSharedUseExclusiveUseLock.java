package com.mchange.v2.lock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ExactReentrantSharedUseExclusiveUseLock implements SharedUseExclusiveUseLock {
   Set waitingShared = new HashSet();
   List activeShared = new LinkedList();
   Set waitingExclusive = new HashSet();
   Thread activeExclusive = null;
   int exclusive_shared_reentries = 0;
   int exclusive_exclusive_reentries = 0;
   String name;

   public ExactReentrantSharedUseExclusiveUseLock(String var1) {
      this.name = var1;
   }

   public ExactReentrantSharedUseExclusiveUseLock() {
      this(null);
   }

   void status(String var1) {
      System.err.println(this + " -- after " + var1);
      System.err.println("waitingShared: " + this.waitingShared);
      System.err.println("activeShared: " + this.activeShared);
      System.err.println("waitingExclusive: " + this.waitingExclusive);
      System.err.println("activeExclusive: " + this.activeExclusive);
      System.err.println("exclusive_shared_reentries: " + this.exclusive_shared_reentries);
      System.err.println("exclusive_exclusive_reentries: " + this.exclusive_exclusive_reentries);
      System.err.println(" ---- ");
      System.err.println();
   }

   @Override
   public synchronized void acquireShared() throws InterruptedException {
      Thread var1 = Thread.currentThread();
      if (var1 == this.activeExclusive) {
         ++this.exclusive_shared_reentries;
      } else {
         try {
            this.waitingShared.add(var1);

            while(!this.okayForShared()) {
               this.wait();
            }

            this.activeShared.add(var1);
         } finally {
            this.waitingShared.remove(var1);
         }
      }
   }

   @Override
   public synchronized void relinquishShared() {
      Thread var1 = Thread.currentThread();
      if (var1 == this.activeExclusive) {
         --this.exclusive_shared_reentries;
         if (this.exclusive_shared_reentries < 0) {
            throw new IllegalStateException(var1 + " relinquished a shared lock (reentrant on exclusive) it did not hold!");
         }
      } else {
         boolean var2 = this.activeShared.remove(var1);
         if (!var2) {
            throw new IllegalStateException(var1 + " relinquished a shared lock it did not hold!");
         }

         this.notifyAll();
      }
   }

   @Override
   public synchronized void acquireExclusive() throws InterruptedException {
      Thread var1 = Thread.currentThread();
      if (var1 == this.activeExclusive) {
         ++this.exclusive_exclusive_reentries;
      } else {
         try {
            this.waitingExclusive.add(var1);

            while(!this.okayForExclusive(var1)) {
               this.wait();
            }

            this.activeExclusive = var1;
         } finally {
            this.waitingExclusive.remove(var1);
         }
      }
   }

   @Override
   public synchronized void relinquishExclusive() {
      Thread var1 = Thread.currentThread();
      if (var1 != this.activeExclusive) {
         throw new IllegalStateException(var1 + " relinquished an exclusive lock it did not hold!");
      } else {
         if (this.exclusive_exclusive_reentries > 0) {
            --this.exclusive_exclusive_reentries;
         } else {
            if (this.exclusive_shared_reentries != 0) {
               throw new IllegalStateException(
                  var1 + " relinquished an exclusive lock while it had reentered but not yet relinquished shared lock acquisitions!"
               );
            }

            this.activeExclusive = null;
            this.notifyAll();
         }
      }
   }

   private boolean okayForShared() {
      return this.activeExclusive == null && this.waitingExclusive.size() == 0;
   }

   private boolean okayForExclusive(Thread var1) {
      int var2 = this.activeShared.size();
      if (var2 == 0) {
         return this.activeExclusive == null;
      } else if (var2 == 1) {
         return this.activeShared.get(0) == var1;
      } else {
         HashSet var3 = new HashSet(this.activeShared);
         return var3.size() == 1 && var3.contains(var1);
      }
   }

   @Override
   public String toString() {
      return super.toString() + " [name=" + this.name + ']';
   }
}
