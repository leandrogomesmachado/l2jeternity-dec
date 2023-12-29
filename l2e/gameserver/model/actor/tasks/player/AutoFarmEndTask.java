package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.autofarm.FarmSettings;

public class AutoFarmEndTask implements Runnable {
   private final Player _player;

   public AutoFarmEndTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.getFarmSystem().setAutoFarmEndTask(0L);
         this._player.getFarmSystem().stopFarmTask(false);
         if (FarmSettings.FARM_ONLINE_TYPE) {
            this._player.getFarmSystem().checkFarmTask();
         }
      }
   }
}
