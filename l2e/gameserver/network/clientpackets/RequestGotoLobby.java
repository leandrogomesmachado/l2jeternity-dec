package l2e.gameserver.network.clientpackets;

import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;

public class RequestGotoLobby extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      client.sendPacket(new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1));
   }
}
