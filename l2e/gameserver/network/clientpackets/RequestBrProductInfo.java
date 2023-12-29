package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExBrProductInfo;

public class RequestBrProductInfo extends GameClientPacket {
   private int _productId;

   @Override
   protected void readImpl() {
      this._productId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.sendPacket(new ExBrProductInfo(this._productId));
      }
   }
}
