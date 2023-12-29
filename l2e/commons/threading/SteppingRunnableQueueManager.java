package l2e.commons.threading;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.collections.LazyArrayList;
import org.apache.commons.lang3.mutable.MutableLong;

public abstract class SteppingRunnableQueueManager implements Runnable {
   private static final Logger _log = Logger.getLogger(SteppingRunnableQueueManager.class.getName());
   protected final long tickPerStepInMillis;
   private final List<SteppingRunnableQueueManager.SteppingScheduledFuture<?>> queue = new CopyOnWriteArrayList<>();
   private final AtomicBoolean isRunning = new AtomicBoolean();

   public SteppingRunnableQueueManager(long tickPerStepInMillis) {
      this.tickPerStepInMillis = tickPerStepInMillis;
   }

   public SteppingRunnableQueueManager.SteppingScheduledFuture<?> schedule(Runnable r, long delay) {
      return this.schedule(r, delay, delay, false);
   }

   public SteppingRunnableQueueManager.SteppingScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay) {
      return this.schedule(r, initial, delay, true);
   }

   private SteppingRunnableQueueManager.SteppingScheduledFuture<?> schedule(Runnable r, long initial, long delay, boolean isPeriodic) {
      long initialStepping = this.getStepping(initial);
      long stepping = this.getStepping(delay);
      SteppingRunnableQueueManager.SteppingScheduledFuture<?> sr;
      this.queue.add(sr = new SteppingRunnableQueueManager.SteppingScheduledFuture(r, initialStepping, stepping, isPeriodic));
      return sr;
   }

   private long getStepping(long delay) {
      delay = Math.max(0L, delay);
      return delay % this.tickPerStepInMillis > this.tickPerStepInMillis / 2L
         ? delay / this.tickPerStepInMillis + 1L
         : (delay < this.tickPerStepInMillis ? 1L : delay / this.tickPerStepInMillis);
   }

   @Override
   public void run() {
      if (!this.isRunning.compareAndSet(false, true)) {
         _log.warning("Slow running queue, managed by " + this + ", queue size : " + this.queue.size() + "!");
      } else {
         try {
            if (this.queue.isEmpty()) {
               return;
            }

            for(SteppingRunnableQueueManager.SteppingScheduledFuture<?> sr : this.queue) {
               if (!sr.isDone()) {
                  sr.run();
               }
            }
         } finally {
            this.isRunning.set(false);
         }
      }
   }

   public void purge() {
      LazyArrayList<SteppingRunnableQueueManager.SteppingScheduledFuture<?>> purge = LazyArrayList.newInstance();

      for(SteppingRunnableQueueManager.SteppingScheduledFuture<?> sr : this.queue) {
         if (sr.isDone()) {
            purge.add(sr);
         }
      }

      this.queue.removeAll(purge);
      LazyArrayList.recycle(purge);
   }

   public CharSequence getStats() {
      StringBuilder list = new StringBuilder();
      Map<String, MutableLong> stats = new TreeMap<>();
      int total = 0;
      int done = 0;

      for(SteppingRunnableQueueManager.SteppingScheduledFuture<?> sr : this.queue) {
         if (sr.isDone()) {
            ++done;
         } else {
            ++total;
            MutableLong count = stats.get(sr._r.getClass().getName());
            if (count == null) {
               stats.put(sr._r.getClass().getName(), new MutableLong(1L));
            } else {
               count.increment();
            }
         }
      }

      for(Entry<String, MutableLong> e : stats.entrySet()) {
         list.append("\t").append(e.getKey()).append(" : ").append(e.getValue().longValue()).append("\n");
      }

      list.append("Scheduled: ....... ").append(total).append("\n");
      list.append("Done/Cancelled: .. ").append(done).append("\n");
      return list;
   }

   public class SteppingScheduledFuture<V> implements RunnableScheduledFuture<V> {
      private final Runnable _r;
      private final long _stepping;
      private final boolean _isPeriodic;
      private long _step;
      private boolean _isCancelled;

      public SteppingScheduledFuture(Runnable r, long initial, long stepping, boolean isPeriodic) {
         this._r = r;
         this._step = initial;
         this._stepping = stepping;
         this._isPeriodic = isPeriodic;
      }

      @Override
      public void run() {
         if (--this._step == 0L) {
            try {
               this._r.run();
            } catch (Exception var5) {
               SteppingRunnableQueueManager._log.log(Level.WARNING, "SteppingScheduledFuture.run():" + var5, (Throwable)var5);
            } finally {
               if (this._isPeriodic) {
                  this._step = this._stepping;
               }
            }
         }
      }

      @Override
      public boolean isDone() {
         return this._isCancelled || !this._isPeriodic && this._step == 0L;
      }

      @Override
      public boolean isCancelled() {
         return this._isCancelled;
      }

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
         return this._isCancelled = true;
      }

      @Override
      public V get() throws InterruptedException, ExecutionException {
         return null;
      }

      @Override
      public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         return null;
      }

      @Override
      public long getDelay(TimeUnit unit) {
         return unit.convert(this._step * SteppingRunnableQueueManager.this.tickPerStepInMillis, TimeUnit.MILLISECONDS);
      }

      public int compareTo(Delayed o) {
         return 0;
      }

      @Override
      public boolean isPeriodic() {
         return this._isPeriodic;
      }
   }
}
