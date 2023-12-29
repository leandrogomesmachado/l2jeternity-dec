package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;

public class ResetSoulsTask implements Runnable {
   private final Player _player;

   public ResetSoulsTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.clearSouls();
      }
   }
}
