package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExUISetting;

public class RequestKeyMapping extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (Config.STORE_UI_SETTINGS) {
            activeChar.sendPacket(new ExUISetting(activeChar));
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
