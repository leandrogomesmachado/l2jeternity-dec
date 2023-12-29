package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersManagerSayTask implements Runnable {
   @Override
   public void run() {
      if (FourSepulchersManager.getInstance().isAttackTime()) {
         Calendar tmp = Calendar.getInstance();
         tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - FourSepulchersManager.getInstance().getWarmUpTimeEnd());
         if (tmp.get(12) + 5 < Config.FS_TIME_ATTACK) {
            FourSepulchersManager.getInstance().managerSay((byte)tmp.get(12));
            ThreadPoolManager.getInstance().schedule(new FourSepulchersManagerSayTask(), 300000L);
         } else if (tmp.get(12) + 5 >= Config.FS_TIME_ATTACK) {
            FourSepulchersManager.getInstance().managerSay((byte)90);
         }
      } else if (FourSepulchersManager.getInstance().isEntryTime()) {
         FourSepulchersManager.getInstance().managerSay((byte)0);
      }
   }
}
