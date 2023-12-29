package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.model.ProductItem;
import l2e.gameserver.model.actor.templates.ProductItemTemplate;

public class ExBrProductInfo extends GameServerPacket {
   private final ProductItem _productId;

   public ExBrProductInfo(int id) {
      this._productId = ProductItemParser.getInstance().getProduct(id);
   }

   @Override
   protected void writeImpl() {
      if (this._productId != null) {
         this.writeD(this._productId.getProductId());
         this.writeD(this._productId.getPoints());
         this.writeD(this._productId.getComponents().size());

         for(ProductItemTemplate com : this._productId.getComponents()) {
            this.writeD(com.getId());
            this.writeD(com.getCount());
            this.writeD(com.getWeight());
            this.writeD(com.isDropable() ? 1 : 0);
         }
      }
   }
}
