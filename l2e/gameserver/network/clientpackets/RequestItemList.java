package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestItemList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (!activeChar.isInventoryDisabled() && !activeChar.isBlocked()) {
            activeChar.sendItemList(true);
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
