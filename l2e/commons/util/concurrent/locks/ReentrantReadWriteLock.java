package l2e.commons.util.concurrent.locks;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ReentrantReadWriteLock {
   private static final AtomicIntegerFieldUpdater<ReentrantReadWriteLock> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(
      ReentrantReadWriteLock.class, "state"
   );
   static final int SHARED_SHIFT = 16;
   static final int SHARED_UNIT = 65536;
   static final int MAX_COUNT = 65535;
   static final int EXCLUSIVE_MASK = 65535;
   transient ReentrantReadWriteLock.ThreadLocalHoldCounter readHolds = new ReentrantReadWriteLock.ThreadLocalHoldCounter();
   transient ReentrantReadWriteLock.HoldCounter cachedHoldCounter;
   private Thread owner;
   private volatile int state;

   static int sharedCount(int c) {
      return c >>> 16;
   }

   static int exclusiveCount(int c) {
      return c & 65535;
   }

   public ReentrantReadWriteLock() {
      this.setState(0);
   }

   private final int getState() {
      return this.state;
   }

   private void setState(int newState) {
      this.state = newState;
   }

   private boolean compareAndSetState(int expect, int update) {
      return stateUpdater.compareAndSet(this, expect, update);
   }

   private Thread getExclusiveOwnerThread() {
      return this.owner;
   }

   private void setExclusiveOwnerThread(Thread thread) {
      this.owner = thread;
   }

   public void writeLock() {
      Thread current = Thread.currentThread();

      while(true) {
         int c;
         while(true) {
            c = this.getState();
            int w = exclusiveCount(c);
            if (c == 0) {
               break;
            }

            if (w != 0 && current == this.getExclusiveOwnerThread()) {
               if (w + exclusiveCount(1) > 65535) {
                  throw new Error("Maximum lock count exceeded");
               }
               break;
            }
         }

         if (this.compareAndSetState(c, c + 1)) {
            this.setExclusiveOwnerThread(current);
            return;
         }
      }
   }

   public boolean tryWriteLock() {
      Thread current = Thread.currentThread();
      int c = this.getState();
      if (c != 0) {
         int w = exclusiveCount(c);
         if (w == 0 || current != this.getExclusiveOwnerThread()) {
            return false;
         }

         if (w == 65535) {
            throw new Error("Maximum lock count exceeded");
         }
      }

      if (!this.compareAndSetState(c, c + 1)) {
         return false;
      } else {
         this.setExclusiveOwnerThread(current);
         return true;
      }
   }

   final boolean tryReadLock() {
      Thread current = Thread.currentThread();
      int c = this.getState();
      int w = exclusiveCount(c);
      if (w != 0 && this.getExclusiveOwnerThread() != current) {
         return false;
      } else if (sharedCount(c) == 65535) {
         throw new Error("Maximum lock count exceeded");
      } else if (!this.compareAndSetState(c, c + 65536)) {
         return false;
      } else {
         ReentrantReadWriteLock.HoldCounter rh = this.cachedHoldCounter;
         if (rh == null || rh.tid != current.getId()) {
            this.cachedHoldCounter = rh = this.readHolds.get();
         }

         ++rh.count;
         return true;
      }
   }

   public void readLock() {
      Thread current = Thread.currentThread();
      ReentrantReadWriteLock.HoldCounter rh = this.cachedHoldCounter;
      if (rh == null || rh.tid != current.getId()) {
         rh = this.readHolds.get();
      }

      while(true) {
         int c = this.getState();
         int w = exclusiveCount(c);
         if (w == 0 || this.getExclusiveOwnerThread() == current) {
            if (sharedCount(c) == 65535) {
               throw new Error("Maximum lock count exceeded");
            }

            if (this.compareAndSetState(c, c + 65536)) {
               this.cachedHoldCounter = rh;
               ++rh.count;
               return;
            }
         }
      }
   }

   public void writeUnlock() {
      int nextc = this.getState() - 1;
      if (Thread.currentThread() != this.getExclusiveOwnerThread()) {
         throw new IllegalMonitorStateException();
      } else if (exclusiveCount(nextc) == 0) {
         this.setExclusiveOwnerThread(null);
         this.setState(nextc);
      } else {
         this.setState(nextc);
      }
   }

   public void readUnlock() {
      ReentrantReadWriteLock.HoldCounter rh = this.cachedHoldCounter;
      Thread current = Thread.currentThread();
      if (rh == null || rh.tid != current.getId()) {
         rh = this.readHolds.get();
      }

      if (rh.tryDecrement() <= 0) {
         throw new IllegalMonitorStateException();
      } else {
         int c;
         int nextc;
         do {
            c = this.getState();
            nextc = c - 65536;
         } while(!this.compareAndSetState(c, nextc));
      }
   }

   static final class HoldCounter {
      int count;
      final long tid = Thread.currentThread().getId();

      int tryDecrement() {
         int c = this.count;
         if (c > 0) {
            this.count = c - 1;
         }

         return c;
      }
   }

   static final class ThreadLocalHoldCounter extends ThreadLocal<ReentrantReadWriteLock.HoldCounter> {
      public ReentrantReadWriteLock.HoldCounter initialValue() {
         return new ReentrantReadWriteLock.HoldCounter();
      }
   }
}
