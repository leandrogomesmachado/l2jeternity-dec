package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class RequestReload extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendUserInfo();
         activeChar.refreshInfos();
      }
   }
}
