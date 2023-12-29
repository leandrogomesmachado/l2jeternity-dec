package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class TeleportTask implements Runnable {
   private final Player _activeChar;
   private final Location _loc;

   public TeleportTask(Player player, Location loc) {
      this._activeChar = player;
      this._loc = loc;
   }

   @Override
   public void run() {
      if (this._activeChar != null && this._activeChar.isOnline()) {
         this._activeChar.teleToLocation(this._loc, true);
      }
   }
}
