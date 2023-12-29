package l2e.gameserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import l2e.commons.threading.PriorityThreadFactory;
import l2e.commons.threading.RejectedExecutionHandlerImpl;
import l2e.commons.threading.RunnableWrapper;

public class ThreadPoolManager {
   private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2L;
   private static final ThreadPoolManager _instance = new ThreadPoolManager();
   private final ScheduledThreadPoolExecutor _scheduledExecutor = new ScheduledThreadPoolExecutor(
      Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("ScheduledThreadPool", 5), new CallerRunsPolicy()
   );
   private final ThreadPoolExecutor _executor;
   private boolean _shutdown;

   public static ThreadPoolManager getInstance() {
      return _instance;
   }

   private ThreadPoolManager() {
      this._scheduledExecutor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
      this._scheduledExecutor.prestartAllCoreThreads();
      this._executor = new ThreadPoolExecutor(
         Config.EXECUTOR_THREAD_POOL_SIZE,
         Integer.MAX_VALUE,
         5L,
         TimeUnit.SECONDS,
         new LinkedBlockingQueue<>(),
         new PriorityThreadFactory("ThreadPoolExecutor", 5),
         new CallerRunsPolicy()
      );
      this._executor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
      this._executor.prestartAllCoreThreads();
      this.scheduleAtFixedRate(() -> {
         this._scheduledExecutor.purge();
         this._executor.purge();
      }, 5L, 5L, TimeUnit.MINUTES);
   }

   private long validate(long delay, TimeUnit timeUnit) {
      long delayInMilliseconds = timeUnit.toMillis(delay);
      long validatedDelay = Math.max(0L, Math.min(MAX_DELAY, delayInMilliseconds));
      return delayInMilliseconds > validatedDelay ? -1L : timeUnit.convert(delayInMilliseconds, TimeUnit.MILLISECONDS);
   }

   public boolean isShutdown() {
      return this._shutdown;
   }

   public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit timeUnit) {
      delay = this.validate(delay, timeUnit);
      return delay == -1L ? null : this._scheduledExecutor.schedule(new RunnableWrapper(r), delay, timeUnit);
   }

   public ScheduledFuture<?> schedule(Runnable r, long delay) {
      return this.schedule(r, delay, TimeUnit.MILLISECONDS);
   }

   public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay, TimeUnit timeUnit) {
      initial = this.validate(initial, timeUnit);
      if (initial == -1L) {
         return null;
      } else {
         delay = this.validate(delay, timeUnit);
         return delay == -1L
            ? this._scheduledExecutor.schedule(new RunnableWrapper(r), initial, timeUnit)
            : this._scheduledExecutor.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, timeUnit);
      }
   }

   public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay) {
      return this.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
   }

   public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay, TimeUnit timeUnit) {
      initial = this.validate(initial, timeUnit);
      if (initial == -1L) {
         return null;
      } else {
         delay = this.validate(delay, timeUnit);
         return delay == -1L
            ? this._scheduledExecutor.schedule(new RunnableWrapper(r), initial, timeUnit)
            : this._scheduledExecutor.scheduleWithFixedDelay(new RunnableWrapper(r), initial, delay, timeUnit);
      }
   }

   public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay) {
      return this.scheduleAtFixedDelay(r, initial, delay, TimeUnit.MILLISECONDS);
   }

   public void execute(Runnable r) {
      this._executor.execute(new RunnableWrapper(r));
   }

   public void shutdown() throws InterruptedException {
      this._shutdown = true;

      try {
         this._scheduledExecutor.shutdown();
         this._scheduledExecutor.awaitTermination(10L, TimeUnit.SECONDS);
      } finally {
         this._executor.shutdown();
         this._executor.awaitTermination(1L, TimeUnit.MINUTES);
      }
   }

   public CharSequence getStats() {
      StringBuilder list = new StringBuilder();
      list.append("ScheduledThreadPool\n");
      list.append("=================================================\n");
      list.append("\tgetActiveCount: ...... ").append(this._scheduledExecutor.getActiveCount()).append("\n");
      list.append("\tgetCorePoolSize: ..... ").append(this._scheduledExecutor.getCorePoolSize()).append("\n");
      list.append("\tgetPoolSize: ......... ").append(this._scheduledExecutor.getPoolSize()).append("\n");
      list.append("\tgetLargestPoolSize: .. ").append(this._scheduledExecutor.getLargestPoolSize()).append("\n");
      list.append("\tgetMaximumPoolSize: .. ").append(this._scheduledExecutor.getMaximumPoolSize()).append("\n");
      list.append("\tgetCompletedTaskCount: ").append(this._scheduledExecutor.getCompletedTaskCount()).append("\n");
      list.append("\tgetQueuedTaskCount: .. ").append(this._scheduledExecutor.getQueue().size()).append("\n");
      list.append("\tgetTaskCount: ........ ").append(this._scheduledExecutor.getTaskCount()).append("\n");
      list.append("ThreadPoolExecutor\n");
      list.append("=================================================\n");
      list.append("\tgetActiveCount: ...... ").append(this._executor.getActiveCount()).append("\n");
      list.append("\tgetCorePoolSize: ..... ").append(this._executor.getCorePoolSize()).append("\n");
      list.append("\tgetPoolSize: ......... ").append(this._executor.getPoolSize()).append("\n");
      list.append("\tgetLargestPoolSize: .. ").append(this._executor.getLargestPoolSize()).append("\n");
      list.append("\tgetMaximumPoolSize: .. ").append(this._executor.getMaximumPoolSize()).append("\n");
      list.append("\tgetCompletedTaskCount: ").append(this._executor.getCompletedTaskCount()).append("\n");
      list.append("\tgetQueuedTaskCount: .. ").append(this._executor.getQueue().size()).append("\n");
      list.append("\tgetTaskCount: ........ ").append(this._executor.getTaskCount()).append("\n");
      return list;
   }
}
