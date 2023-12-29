package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.Config;
import l2e.gameserver.model.items.buylist.Product;
import l2e.gameserver.model.items.buylist.ProductList;

public class ShopPreviewList extends GameServerPacket {
   private final int _listId;
   private final Collection<Product> _list;
   private final long _money;
   private int _expertise;

   public ShopPreviewList(ProductList list, long currentMoney, int expertiseIndex) {
      this._listId = list.getListId();
      this._list = list.getProducts();
      this._money = currentMoney;
      this._expertise = expertiseIndex;
   }

   public ShopPreviewList(Collection<Product> lst, int listId, long currentMoney) {
      this._listId = listId;
      this._list = lst;
      this._money = currentMoney;
   }

   @Override
   protected final void writeImpl() {
      this.writeC(192);
      this.writeC(19);
      this.writeC(0);
      this.writeC(0);
      this.writeQ(this._money);
      this.writeD(this._listId);
      int newlength = 0;

      for(Product product : this._list) {
         if (product.getItem().getCrystalType() <= this._expertise && product.getItem().isEquipable()) {
            ++newlength;
         }
      }

      this.writeH(newlength);

      for(Product product : this._list) {
         if (product.getItem().getCrystalType() <= this._expertise && product.getItem().isEquipable()) {
            this.writeD(product.getId());
            this.writeH(product.getItem().getType2());
            if (product.getItem().getType1() != 4) {
               this.writeH(product.getItem().getBodyPart());
            } else {
               this.writeH(0);
            }

            this.writeQ((long)Config.WEAR_PRICE);
         }
      }
   }
}
