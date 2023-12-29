package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExReceiveShowPostFriend;

public final class RequestExShowPostFriendListForPostBox extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   public void runImpl() {
      if (Config.ALLOW_MAIL) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
         }
      }
   }
}
