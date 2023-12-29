package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public final class GMSnoopEnd extends GameClientPacket {
   private int _snoopID;

   @Override
   protected void readImpl() {
      this._snoopID = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = World.getInstance().getPlayer(this._snoopID);
      if (player != null) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            player.removeSnooper(activeChar);
            activeChar.removeSnooped(player);
         }
      }
   }
}
