package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class RemoveWearItemsTask implements Runnable {
   private final Player _player;

   public RemoveWearItemsTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         this._player.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
         this._player.sendUserInfo(true);
      }
   }
}
