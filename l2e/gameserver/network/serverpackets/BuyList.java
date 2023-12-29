package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.Config;
import l2e.gameserver.model.items.buylist.Product;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.network.ServerPacketOpcodes;

public final class BuyList extends GameServerPacket {
   private final int _listId;
   private final Collection<Product> _list;
   private final long _money;
   private double _taxRate = 0.0;

   @Override
   protected ServerPacketOpcodes getOpcodes() {
      return ServerPacketOpcodes.ExBuySellList;
   }

   public BuyList(ProductList list, long currentMoney, double taxRate) {
      this._listId = list.getListId();
      this._list = list.getProducts();
      this._money = currentMoney;
      this._taxRate = taxRate;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(0);
      this.writeQ(this._money);
      this.writeD(this._listId);
      this.writeH(this._list.size());

      for(Product product : this._list) {
         if (product.getCount() > 0L || !product.hasLimitedStock()) {
            this.writeD(product.getId());
            this.writeD(product.getId());
            this.writeD(0);
            this.writeQ(product.getCount() < 0L ? 0L : product.getCount());
            this.writeH(product.getItem().getType2());
            this.writeH(product.getItem().getType1());
            this.writeH(0);
            this.writeD(product.getItem().getBodyPart());
            this.writeH(0);
            this.writeH(0);
            this.writeD(0);
            this.writeD(-1);
            this.writeD(-9999);
            this.writeH(0);
            this.writeH(0);

            for(byte i = 0; i < 6; ++i) {
               this.writeH(0);
            }

            this.writeH(0);
            this.writeH(0);
            this.writeH(0);
            if (product.getId() >= 3960 && product.getId() <= 4026) {
               this.writeQ((long)((double)((float)product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE) * (1.0 + this._taxRate)));
            } else {
               this.writeQ((long)((double)product.getPrice() * (1.0 + this._taxRate)));
            }
         }
      }
   }
}
