package l2e.commons.util.concurrent.atomic;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicState {
   private static final AtomicIntegerFieldUpdater<AtomicState> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(AtomicState.class, "value");
   private volatile int value;

   public AtomicState(boolean initialValue) {
      this.value = initialValue ? 1 : 0;
   }

   public AtomicState() {
   }

   public final boolean get() {
      return this.value != 0;
   }

   private boolean getBool(int value) {
      if (value < 0) {
         throw new IllegalStateException();
      } else {
         return value > 0;
      }
   }

   public final boolean setAndGet(boolean newValue) {
      return newValue ? this.getBool(stateUpdater.incrementAndGet(this)) : this.getBool(stateUpdater.decrementAndGet(this));
   }

   public final boolean getAndSet(boolean newValue) {
      return newValue ? this.getBool(stateUpdater.getAndIncrement(this)) : this.getBool(stateUpdater.getAndDecrement(this));
   }
}
