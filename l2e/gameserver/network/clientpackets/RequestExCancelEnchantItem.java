package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.EnchantResult;

public class RequestExCancelEnchantItem extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(new EnchantResult(2, 0, 0));
         activeChar.setActiveEnchantItemId(-1);
      }
   }
}
