package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public class ExQuestItemList extends GameServerPacket {
   private final int _size;
   private final ItemInstance[] _items;
   private final PcInventory _inventory;

   public ExQuestItemList(PcInventory inv, int size, ItemInstance[] items) {
      this._size = size;
      this._items = items;
      this._inventory = inv;
   }

   @Override
   protected void writeImpl() {
      this.writeH(this._size);

      for(ItemInstance item : this._items) {
         if (item != null && item.getItem() != null && item.isQuestItem()) {
            this.writeD(item.getObjectId());
            this.writeD(item.getDisplayId());
            this.writeD(item.getLocationSlot());
            this.writeQ(item.getCount());
            this.writeD(item.getItem().getType2());
            this.writeH(item.getCustomType1());
            this.writeD(item.getItem().getBodyPart());
            this.writeH(item.getEnchantLevel());
            this.writeH(item.getCustomType2());
            if (item.isAugmented()) {
               this.writeD(item.getAugmentation().getAugmentationId());
            } else {
               this.writeD(0);
            }

            this.writeD(item.getMana());
            this.writeD(item.isTimeLimitedItem() ? (int)(item.getRemainingTime() / 1000L) : -9999);
            this.writeH(item.getAttackElementType());
            this.writeH(item.getAttackElementPower());

            for(byte i = 0; i < 6; ++i) {
               this.writeH(item.getElementDefAttr(i));
            }

            for(int op : item.getEnchantOptions()) {
               this.writeH(op);
            }
         }
      }

      if (this._inventory.hasInventoryBlock()) {
         this.writeH(this._inventory.getBlockItems().length);
         this.writeC(this._inventory.getBlockMode());

         for(int i : this._inventory.getBlockItems()) {
            this.writeD(i);
         }
      } else {
         this.writeH(0);
      }
   }
}
