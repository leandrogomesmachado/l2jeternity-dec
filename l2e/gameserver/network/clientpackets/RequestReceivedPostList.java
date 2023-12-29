package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExShowReceivedPostList;

public final class RequestReceivedPostList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && Config.ALLOW_MAIL) {
         activeChar.sendPacket(new ExShowReceivedPostList(activeChar.getObjectId()));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
