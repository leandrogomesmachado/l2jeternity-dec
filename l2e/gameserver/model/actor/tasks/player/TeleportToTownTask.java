package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;

public class TeleportToTownTask implements Runnable {
   private final Player _activeChar;

   public TeleportToTownTask(Player player) {
      this._activeChar = player;
   }

   @Override
   public void run() {
      if (this._activeChar != null && this._activeChar.isOnline()) {
         this._activeChar.setReflectionId(0);
         this._activeChar.teleToLocation(TeleportWhereType.TOWN, true);
      }
   }
}
