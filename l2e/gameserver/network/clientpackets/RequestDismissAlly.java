package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestDismissAlly extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (!activeChar.isClanLeader()) {
            activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
         } else {
            activeChar.getClan().dissolveAlly(activeChar);
         }
      }
   }
}
