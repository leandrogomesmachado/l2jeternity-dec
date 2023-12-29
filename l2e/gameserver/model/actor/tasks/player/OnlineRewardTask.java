package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.instancemanager.OnlineRewardManager;
import l2e.gameserver.model.actor.Player;

public class OnlineRewardTask implements Runnable {
   private final Player _player;

   public OnlineRewardTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         OnlineRewardManager.getInstance().getOnlineReward(this._player);
      }
   }
}
