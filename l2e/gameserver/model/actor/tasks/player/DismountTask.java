package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;

public class DismountTask implements Runnable {
   private final Player _player;

   public DismountTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.dismount();
      }
   }
}
