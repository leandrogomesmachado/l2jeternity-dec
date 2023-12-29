package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public final class ItemList extends GameServerPacket {
   private final PcInventory _inventory;
   private final ItemInstance[] _items;
   private final boolean _showWindow;
   private final int _size;

   public ItemList(PcInventory inventory, int size, ItemInstance[] items, boolean showWindow) {
      this._inventory = inventory;
      this._size = size;
      this._items = items;
      this._showWindow = showWindow;
   }

   @Override
   protected final void writeImpl() {
      this.writeH(this._showWindow ? 1 : 0);
      this.writeH(this._size);

      for(ItemInstance temp : this._items) {
         if (temp != null && temp.getItem() != null && !temp.isQuestItem()) {
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
