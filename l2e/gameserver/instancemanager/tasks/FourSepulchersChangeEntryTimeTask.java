package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersChangeEntryTimeTask implements Runnable {
   @Override
   public void run() {
      FourSepulchersManager manager = FourSepulchersManager.getInstance();
      manager.setIsEntryTime(true);
      manager.setIsWarmUpTime(false);
      manager.setIsAttackTime(false);
      manager.setIsCoolDownTime(false);
      long interval = 0L;
      if (manager.isFirstTimeRun()) {
         interval = manager.getEntrytTimeEnd() - Calendar.getInstance().getTimeInMillis();
      } else {
         interval = (long)Config.FS_TIME_ENTRY * 60000L;
      }

      ThreadPoolManager.getInstance().schedule(new FourSepulchersManagerSayTask(), 0L);
      manager.setChangeWarmUpTimeTask(ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeWarmUpTimeTask(), interval));
      ScheduledFuture<?> changeEntryTimeTask = manager.getChangeEntryTimeTask();
      if (changeEntryTimeTask != null) {
         changeEntryTimeTask.cancel(true);
         manager.setChangeEntryTimeTask(null);
      }
   }
}
