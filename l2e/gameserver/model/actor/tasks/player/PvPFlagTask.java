package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;

public class PvPFlagTask implements Runnable {
   private final Player _player;

   public PvPFlagTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (System.currentTimeMillis() > this._player.getPvpFlagLasts()) {
            this._player.stopPvPFlag();
         } else if (System.currentTimeMillis() > this._player.getPvpFlagLasts() - 20000L) {
            this._player.updatePvPFlag(2);
         } else {
            this._player.updatePvPFlag(1);
         }
      }
   }
}
