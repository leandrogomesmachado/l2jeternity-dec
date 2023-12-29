package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.ExShowAgitInfo;

public class RequestAllAgitInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (client != null) {
         client.sendPacket(new ExShowAgitInfo());
      }
   }
}
