package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;

public class GMViewItemList extends GameServerPacket {
   private final int _size;
   private final ItemInstance[] _items;
   private final int _limit;
   private final String _name;

   public GMViewItemList(Player cha, ItemInstance[] items, int size) {
      this._size = size;
      this._items = items;
      this._name = cha.getName();
      this._limit = cha.getInventoryLimit();
   }

   public GMViewItemList(PetInstance cha, ItemInstance[] items, int size) {
      this._size = size;
      this._items = items;
      this._name = cha.getName();
      this._limit = cha.getInventoryLimit();
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._limit);
      this.writeH(1);
      this.writeH(this._size);

      for(ItemInstance temp : this._items) {
         if (!temp.isQuestItem()) {
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
            this.writeD(temp.isAugmented() ? temp.getAugmentation().getAugmentationId() : 0);
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
}
