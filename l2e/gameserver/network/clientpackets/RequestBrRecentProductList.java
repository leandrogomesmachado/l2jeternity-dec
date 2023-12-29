package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.model.actor.Player;

public class RequestBrRecentProductList extends GameClientPacket {
   @Override
   public void readImpl() {
   }

   @Override
   public void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         ProductItemParser.getInstance().recentProductList(player);
      }
   }
}
