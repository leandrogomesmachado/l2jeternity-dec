package l2e.commons.lang;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

public final class StatsUtils {
   private static final MemoryMXBean memMXbean = ManagementFactory.getMemoryMXBean();
   private static final ThreadMXBean threadMXbean = ManagementFactory.getThreadMXBean();

   public static long getMemUsed() {
      return memMXbean.getHeapMemoryUsage().getUsed();
   }

   public static String getMemUsedMb() {
      return getMemUsed() / 1048576L + " Mb";
   }

   public static long getMemMax() {
      return memMXbean.getHeapMemoryUsage().getMax();
   }

   public static String getMemMaxMb() {
      return getMemMax() / 1048576L + " Mb";
   }

   public static long getMemFree() {
      MemoryUsage heapMemoryUsage = memMXbean.getHeapMemoryUsage();
      return heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
   }

   public static String getMemFreeMb() {
      return getMemFree() / 1048576L + " Mb";
   }

   public static void getMemUsage(Logger log) {
      double maxMem = (double)memMXbean.getHeapMemoryUsage().getMax() / 1024.0;
      double allocatedMem = (double)memMXbean.getHeapMemoryUsage().getCommitted() / 1024.0;
      double usedMem = (double)memMXbean.getHeapMemoryUsage().getUsed() / 1024.0;
      double nonAllocatedMem = maxMem - allocatedMem;
      double cachedMem = allocatedMem - usedMem;
      double useableMem = maxMem - usedMem;
      new StringBuilder();
      log.info("    AllowedMemory: ........... " + (int)maxMem + " KB");
      log.info("        Allocated: ........... " + (int)allocatedMem + " KB (" + (double)Math.round(allocatedMem / maxMem * 1000000.0) / 10000.0 + "%)");
      log.info("    Non-Allocated: ........... " + (int)nonAllocatedMem + " KB (" + (double)Math.round(nonAllocatedMem / maxMem * 1000000.0) / 10000.0 + "%)");
      log.info("  AllocatedMemory: ........... " + (int)allocatedMem + " KB");
      log.info("             Used: ........... " + (int)usedMem + " KB (" + (double)Math.round(usedMem / maxMem * 1000000.0) / 10000.0 + "%)");
      log.info("  Unused (cached): ........... " + (int)cachedMem + " KB (" + (double)Math.round(cachedMem / maxMem * 1000000.0) / 10000.0 + "%)");
      log.info("    UseableMemory: ........... " + (int)useableMem + " KB (" + (double)Math.round(useableMem / maxMem * 1000000.0) / 10000.0 + "%)");
   }

   public static CharSequence getThreadStats() {
      StringBuilder list = new StringBuilder();
      int threadCount = threadMXbean.getThreadCount();
      int daemonCount = threadMXbean.getThreadCount();
      int nonDaemonCount = threadCount - daemonCount;
      int peakCount = threadMXbean.getPeakThreadCount();
      long totalCount = threadMXbean.getTotalStartedThreadCount();
      list.append("Live: .................... ").append(threadCount).append(" threads").append("\n\r");
      list.append("     Non-Daemon: ......... ").append(nonDaemonCount).append(" threads").append("\n\r");
      list.append("     Daemon: ............. ").append(daemonCount).append(" threads").append("\n\r");
      list.append("Peak: .................... ").append(peakCount).append(" threads").append("\n\r");
      list.append("Total started: ........... ").append(totalCount).append(" threads").append("\n\r");
      list.append("=================================================").append("\n\r");
      return list;
   }

   public static CharSequence getThreadStats(boolean lockedMonitors, boolean lockedSynchronizers, boolean stackTrace) {
      StringBuilder list = new StringBuilder();

      for(ThreadInfo info : threadMXbean.dumpAllThreads(lockedMonitors, lockedSynchronizers)) {
         list.append("Thread #").append(info.getThreadId()).append(" (").append(info.getThreadName()).append(")").append("\n\r");
         list.append("=================================================\n\r");
         list.append("\tgetThreadState: ...... ").append(info.getThreadState()).append("\n\r");

         for(MonitorInfo monitorInfo : info.getLockedMonitors()) {
            list.append("\tLocked monitor: ....... ").append(monitorInfo).append("\n\r");
            list.append("\t\t[").append(monitorInfo.getLockedStackDepth()).append(".]: at ").append(monitorInfo.getLockedStackFrame()).append("\n\r");
         }

         for(LockInfo lockInfo : info.getLockedSynchronizers()) {
            list.append("\tLocked synchronizer: ...").append(lockInfo).append("\n\r");
         }

         if (stackTrace) {
            list.append("\tgetStackTace: ..........\n\r");

            for(StackTraceElement trace : info.getStackTrace()) {
               list.append("\t\tat ").append(trace).append("\n\r");
            }
         }

         list.append("=================================================\n\r");
      }

      return list;
   }

   public static CharSequence getGCStats() {
      StringBuilder list = new StringBuilder();

      for(GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
         list.append("GarbageCollector (").append(gcBean.getName()).append(")\n\r");
         list.append("=================================================\n\r");
         list.append("getCollectionCount: ..... ").append(gcBean.getCollectionCount()).append("\n\r");
         list.append("getCollectionTime: ...... ").append(gcBean.getCollectionTime()).append(" ms").append("\n\r");
         list.append("=================================================\n\r");
      }

      return list;
   }
}
