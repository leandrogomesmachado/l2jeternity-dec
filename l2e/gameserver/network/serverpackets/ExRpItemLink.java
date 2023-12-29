package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;

public final class ExRpItemLink extends GameServerPacket {
   private final ItemInstance _item;

   public ExRpItemLink(ItemInstance item) {
      this._item = item;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._item.getObjectId());
      this.writeD(this._item.getDisplayId());
      this.writeD(this._item.getLocationSlot());
      this.writeQ(this._item.getCount());
      this.writeH(this._item.getItem().getType2());
      this.writeH(this._item.getCustomType1());
      this.writeH(this._item.isEquipped() ? 1 : 0);
      this.writeD(this._item.getItem().getBodyPart());
      this.writeH(this._item.getEnchantLevel());
      this.writeH(this._item.getCustomType2());
      if (this._item.isAugmented()) {
         this.writeD(this._item.getAugmentation().getAugmentationId());
      } else {
         this.writeD(0);
      }

      this.writeD(this._item.getMana());
      this.writeD(this._item.isTimeLimitedItem() ? (int)(this._item.getRemainingTime() / 1000L) : -9999);
      this.writeH(this._item.getAttackElementType());
      this.writeH(this._item.getAttackElementPower());

      for(byte i = 0; i < 6; ++i) {
         this.writeH(this._item.getElementDefAttr(i));
      }

      for(int op : this._item.getEnchantOptions()) {
         this.writeH(op);
      }
   }
}
