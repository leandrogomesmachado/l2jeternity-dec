package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class RequestBuySellUIClose extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && !activeChar.isInventoryDisabled()) {
         if (Config.ALLOW_UI_OPEN) {
            activeChar.sendItemList(true);
         }
      }
   }
}
