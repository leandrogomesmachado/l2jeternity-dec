package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestOpenMinimap extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected final void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(new ShowMiniMap(1665));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
