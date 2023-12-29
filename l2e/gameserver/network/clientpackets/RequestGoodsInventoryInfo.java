package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class RequestGoodsInventoryInfo extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (Config.PACKET_HANDLER_DEBUG) {
            _log.warning("RequestGoodsInventoryInfo: Not support for this packet!!!");
         }
      }
   }
}
