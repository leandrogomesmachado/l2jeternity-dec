package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.model.actor.Player;

public class RequestBrBuyProduct extends GameClientPacket {
   private int _productId;
   private int _count;

   @Override
   protected void readImpl() {
      this._productId = this.readD();
      this._count = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         ProductItemParser.getInstance().requestBuyItem(player, this._productId, this._count);
      }
   }
}
