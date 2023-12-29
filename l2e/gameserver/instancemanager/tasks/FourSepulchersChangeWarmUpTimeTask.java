package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersChangeWarmUpTimeTask implements Runnable {
   @Override
   public void run() {
      FourSepulchersManager manager = FourSepulchersManager.getInstance();
      manager.setIsEntryTime(true);
      manager.setIsWarmUpTime(false);
      manager.setIsAttackTime(false);
      manager.setIsCoolDownTime(false);
      long interval = 0L;
      if (manager.isFirstTimeRun()) {
         interval = manager.getWarmUpTimeEnd() - Calendar.getInstance().getTimeInMillis();
      } else {
         interval = (long)Config.FS_TIME_WARMUP * 60000L;
      }

      manager.setChangeAttackTimeTask(ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeAttackTimeTask(), interval));
      ScheduledFuture<?> changeWarmUpTimeTask = manager.getChangeWarmUpTimeTask();
      if (changeWarmUpTimeTask != null) {
         changeWarmUpTimeTask.cancel(true);
         manager.setChangeWarmUpTimeTask(null);
      }
   }
}
