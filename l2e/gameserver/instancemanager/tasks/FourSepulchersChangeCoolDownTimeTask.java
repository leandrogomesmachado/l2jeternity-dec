package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersChangeCoolDownTimeTask implements Runnable {
   @Override
   public void run() {
      FourSepulchersManager manager = FourSepulchersManager.getInstance();
      manager.setIsEntryTime(false);
      manager.setIsWarmUpTime(false);
      manager.setIsAttackTime(false);
      manager.setIsCoolDownTime(true);
      manager.clean();
      Calendar time = Calendar.getInstance();
      if (!manager.isFirstTimeRun() && Calendar.getInstance().get(12) > manager.getCycleMin()) {
         time.set(10, Calendar.getInstance().get(10) + 1);
      }

      time.set(12, manager.getCycleMin());
      if (manager.isFirstTimeRun()) {
         manager.setIsFirstTimeRun(false);
      }

      long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
      manager.setChangeEntryTimeTask(ThreadPoolManager.getInstance().schedule(new FourSepulchersChangeEntryTimeTask(), interval));
      ScheduledFuture<?> changeCoolDownTimeTask = manager.getChangeCoolDownTimeTask();
      if (changeCoolDownTimeTask != null) {
         changeCoolDownTimeTask.cancel(true);
         manager.setChangeCoolDownTimeTask(null);
      }
   }
}
