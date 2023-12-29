package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;

public class PetItemList extends GameServerPacket {
   private final ItemInstance[] _items;

   public PetItemList(ItemInstance[] items) {
      this._items = items;
   }

   @Override
   protected final void writeImpl() {
      int count = this._items.length;
      this.writeH(count);

      for(ItemInstance temp : this._items) {
         this.writeD(temp.getObjectId());
         this.writeD(temp.getDisplayId());
         this.writeD(temp.getLocationSlot());
         this.writeQ(temp.getCount());
         this.writeH(temp.getItem().getType2());
         this.writeH(temp.getCustomType1());
         this.writeH(temp.isEquipped() ? 1 : 0);
         this.writeD(temp.getItem().getBodyPart());
         this.writeH(temp.getEnchantLevel());
         this.writeH(temp.getCustomType2());
         if (temp.isAugmented()) {
            this.writeD(temp.getAugmentation().getAugmentationId());
         } else {
            this.writeD(0);
         }

         this.writeD(temp.getMana());
         this.writeD(temp.isTimeLimitedItem() ? (int)(temp.getRemainingTime() / 1000L) : -9999);
         this.writeH(temp.getAttackElementType());
         this.writeH(temp.getAttackElementPower());

         for(byte i = 0; i < 6; ++i) {
            this.writeH(temp.getElementDefAttr(i));
         }

         for(int op : temp.getEnchantOptions()) {
            this.writeH(op);
         }
      }
   }
}
