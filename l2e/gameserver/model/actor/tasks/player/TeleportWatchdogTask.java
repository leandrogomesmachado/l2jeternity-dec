package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;

public class TeleportWatchdogTask implements Runnable {
   private final Player _player;

   public TeleportWatchdogTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null && this._player.isTeleporting()) {
         this._player.onTeleported();
      }
   }
}
