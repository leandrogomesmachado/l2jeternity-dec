package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersChangeAttackTimeTask implements Runnable {
   @Override
   public void run() {
      FourSepulchersManager manager = FourSepulchersManager.getInstance();
      manager.setIsEntryTime(false);
      manager.setIsWarmUpTime(false);
      manager.setIsAttackTime(true);
      manager.setIsCoolDownTime(false);
      manager.locationShadowSpawns();
      manager.spawnMysteriousBox(31921);
      manager.spawnMysteriousBox(31922);
      manager.spawnMysteriousBox(31923);
      manager.spawnMysteriousBox(31924);
      if (!manager.isFirstTimeRun()) {
         manager.setWarmUpTimeEnd(Calendar.getInstance().getTimeInMillis());
      }

      long interval = 0L;
      if (manager.isFirstTimeRun()) {
         for(double min = (double)Calendar.getInstance().get(12); min < (double)manager.getCycleMin(); ++min) {
            if (min % 5.0 == 0.0) {
               Calendar inter = Calendar.getInstance();
               inter.set(12, (int)min);
               ThreadPoolManager.getInstance()
                  .schedule(new FourSepulchersManagerSayTask(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
               break;
            }
         }
      } else {
         ThreadPoolManager.getInstance().schedule(new FourSepulchersManagerSayTask(), 302000L);
      }

      if (manager.isFirstTimeRun()) {
         interval = manager.getAttackTimeEnd() - Calendar.getInstance().getTimeInMillis();
      } else {
         interval = (long)Config.FS_TIME_ATTACK * 60000L;
      }

      manager.setChangeCoolDownTimeTask(ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeCoolDownTimeTask(), interval));
      ScheduledFuture<?> changeAttackTimeTask = manager.getChangeAttackTimeTask();
      if (changeAttackTimeTask != null) {
         changeAttackTimeTask.cancel(true);
         manager.setChangeAttackTimeTask(null);
      }
   }
}
