package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.ItemInfo;
import l2e.gameserver.model.items.instance.ItemInstance;

public class InventoryUpdate extends GameServerPacket {
   private final List<ItemInfo> _items;

   public InventoryUpdate() {
      this._items = new ArrayList<>();
   }

   public InventoryUpdate(List<ItemInfo> items) {
      this._items = items;
   }

   public void addItem(ItemInstance item) {
      if (item != null) {
         this._items.add(new ItemInfo(item));
      }
   }

   public void addNewItem(ItemInstance item) {
      if (item != null) {
         this._items.add(new ItemInfo(item, 1));
      }
   }

   public void addModifiedItem(ItemInstance item) {
      if (item != null) {
         this._items.add(new ItemInfo(item, 2));
      }
   }

   public void addRemovedItem(ItemInstance item) {
      if (item != null) {
         this._items.add(new ItemInfo(item, 3));
      }
   }

   public void addItems(List<ItemInstance> items) {
      if (items != null) {
         for(ItemInstance item : items) {
            if (item != null) {
               this._items.add(new ItemInfo(item));
            }
         }
      }
   }

   @Override
   protected final void writeImpl() {
      int count = this._items.size();
      this.writeH(count);

      for(ItemInfo item : this._items) {
         this.writeH(item.getChange());
         this.writeD(item.getObjectId());
         this.writeD(item.getItem().getDisplayId());
         this.writeD(item.getLocation());
         this.writeQ(item.getCount());
         this.writeH(item.getItem().getType2());
         this.writeH(item.getCustomType1());
         this.writeH(item.getEquipped());
         this.writeD(item.getItem().getBodyPart());
         this.writeH(item.getEnchant());
         this.writeH(item.getCustomType2());
         this.writeD(item.getAugmentationBonus());
         this.writeD(item.getMana());
         this.writeD(item.getTime());
         this.writeH(item.getAttackElementType());
         this.writeH(item.getAttackElementPower());

         for(byte i = 0; i < 6; ++i) {
            this.writeH(item.getElementDefAttr(i));
         }

         for(int op : item.getEnchantOptions()) {
            this.writeH(op);
         }
      }

      this.checkAgathionItems();
   }

   private void checkAgathionItems() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         int agathionItems = 0;
         ItemInstance[] allItem = player.getInventory().getItems();

         for(ItemInstance item : allItem) {
            if (item != null && item.getAgathionEnergy() >= 0) {
               ++agathionItems;
            }
         }

         if (agathionItems > 0) {
            player.sendPacket(new ExBrAgathionEnergyInfo(agathionItems, allItem));
         }
      }
   }
}
