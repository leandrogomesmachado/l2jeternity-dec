package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public class RequestResetNickname extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.getAppearance().setTitleColor(16777079);
         activeChar.unsetVar("titlecolor");
         activeChar.setTitle("");
         activeChar.broadcastTitleInfo();
      }
   }
}
