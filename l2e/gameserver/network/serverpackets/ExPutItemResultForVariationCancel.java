package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;

public class ExPutItemResultForVariationCancel extends GameServerPacket {
   private final int _itemObjId;
   private final int _itemId;
   private final int _itemAug1;
   private final int _itemAug2;
   private final int _price;

   public ExPutItemResultForVariationCancel(ItemInstance item, int price) {
      this._itemObjId = item.getObjectId();
      this._itemId = item.getDisplayId();
      this._price = price;
      this._itemAug1 = (short)item.getAugmentation().getAugmentationId();
      this._itemAug2 = item.getAugmentation().getAugmentationId() >> 16;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._itemObjId);
      this.writeD(this._itemId);
      this.writeD(this._itemAug1);
      this.writeD(this._itemAug2);
      this.writeQ((long)this._price);
      this.writeD(1);
   }
}
