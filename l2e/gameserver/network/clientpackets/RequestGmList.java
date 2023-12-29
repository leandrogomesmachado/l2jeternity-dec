package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.AdminParser;

public final class RequestGmList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      if (this.getClient().getActiveChar() != null) {
         AdminParser.getInstance().sendListToPlayer(this.getClient().getActiveChar());
      }
   }
}
