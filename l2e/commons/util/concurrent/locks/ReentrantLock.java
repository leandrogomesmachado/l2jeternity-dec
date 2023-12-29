package l2e.commons.util.concurrent.locks;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ReentrantLock {
   private static final AtomicIntegerFieldUpdater<ReentrantLock> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(ReentrantLock.class, "state");
   private Thread owner;
   private volatile int state;

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

   public void lock() {
      if (this.compareAndSetState(0, 1)) {
         this.setExclusiveOwnerThread(Thread.currentThread());
      } else {
         while(!this.tryLock()) {
         }
      }
   }

   public boolean tryLock() {
      Thread current = Thread.currentThread();
      int c = this.getState();
      if (c == 0) {
         if (this.compareAndSetState(0, 1)) {
            this.setExclusiveOwnerThread(current);
            return true;
         }
      } else if (current == this.getExclusiveOwnerThread()) {
         int nextc = c + 1;
         if (nextc < 0) {
            throw new Error("Maximum lock count exceeded");
         }

         this.setState(nextc);
         return true;
      }

      return false;
   }

   public boolean unlock() {
      int c = this.getState() - 1;
      if (Thread.currentThread() != this.getExclusiveOwnerThread()) {
         throw new IllegalMonitorStateException();
      } else {
         boolean free = false;
         if (c == 0) {
            free = true;
            this.setExclusiveOwnerThread(null);
         }

         this.setState(c);
         return free;
      }
   }
}
