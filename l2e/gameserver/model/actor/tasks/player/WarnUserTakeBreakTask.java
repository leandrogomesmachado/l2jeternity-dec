package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class WarnUserTakeBreakTask implements Runnable {
   private final Player _player;

   public WarnUserTakeBreakTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (this._player.isOnline()) {
            this._player.sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME);
            this._player.getHoursInGame();
         } else {
            this._player.stopWarnUserTakeBreak();
         }
      }
   }
}
