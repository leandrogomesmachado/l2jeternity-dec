package l2e.commons.threading;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RunnableStatsManager {
   private static final RunnableStatsManager _instance = new RunnableStatsManager();
   private final Map<Class<?>, RunnableStatsManager.ClassStat> classStats = new HashMap<>();
   private final Lock lock = new ReentrantLock();

   public static final RunnableStatsManager getInstance() {
      return _instance;
   }

   public void handleStats(Class<?> cl, long runTime) {
      try {
         this.lock.lock();
         RunnableStatsManager.ClassStat stat = this.classStats.get(cl);
         if (stat == null) {
            stat = new RunnableStatsManager.ClassStat(cl);
         }

         stat.runCount++;
         stat.runTime = stat.runTime + runTime;
         if (stat.minTime > runTime) {
            stat.minTime = runTime;
         }

         if (stat.maxTime < runTime) {
            stat.maxTime = runTime;
         }
      } finally {
         this.lock.unlock();
      }
   }

   private List<RunnableStatsManager.ClassStat> getSortedClassStats() {
      List<RunnableStatsManager.ClassStat> result = Collections.emptyList();

      try {
         this.lock.lock();
         result = Arrays.asList(this.classStats.values().toArray(new RunnableStatsManager.ClassStat[this.classStats.size()]));
      } finally {
         this.lock.unlock();
      }

      Collections.sort(result, (c1, c2) -> {
         if (c1.maxTime < c2.maxTime) {
            return 1;
         } else {
            return c1.maxTime == c2.maxTime ? 0 : -1;
         }
      });
      return result;
   }

   public CharSequence getStats() {
      StringBuilder list = new StringBuilder();

      for(RunnableStatsManager.ClassStat stat : this.getSortedClassStats()) {
         list.append(stat.clazz.getName()).append(":\n");
         list.append("\tRun: ............ ").append(stat.runCount).append("\n");
         list.append("\tTime: ........... ").append(stat.runTime).append("\n");
         list.append("\tMin: ............ ").append(stat.minTime).append("\n");
         list.append("\tMax: ............ ").append(stat.maxTime).append("\n");
         list.append("\tAverage: ........ ").append(stat.runTime / stat.runCount).append("\n");
      }

      return list;
   }

   private class ClassStat {
      private final Class<?> clazz;
      private long runCount = 0L;
      private long runTime = 0L;
      private long minTime = Long.MAX_VALUE;
      private long maxTime = Long.MIN_VALUE;

      private ClassStat(Class<?> cl) {
         this.clazz = cl;
         RunnableStatsManager.this.classStats.put(cl, this);
      }
   }
}
