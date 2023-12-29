package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.serverpackets.ExReplyDominionInfo;
import l2e.gameserver.network.serverpackets.ExShowOwnthingPos;

public class RequestDominionInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      this.sendPacket(ExReplyDominionInfo.STATIC_PACKET);
      this.sendPacket(ExShowOwnthingPos.STATIC_PACKET);
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
