package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.ExShowFortressInfo;

public class RequestAllFortressInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (client != null) {
         client.sendPacket(ExShowFortressInfo.STATIC_PACKET);
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
