package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;

public class ExBrAgathionEnergyInfo extends GameServerPacket {
   private final int _size;
   private ItemInstance[] _itemList = null;

   public ExBrAgathionEnergyInfo(int size, ItemInstance... item) {
      this._itemList = item;
      this._size = size;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._size);

      for(ItemInstance item : this._itemList) {
         if (item != null && item.getItem().getAgathionMaxEnergy() >= 0) {
            this.writeD(item.getObjectId());
            this.writeD(item.getDisplayId());
            this.writeD(2097152);
            this.writeD(item.getAgathionEnergy());
            this.writeD(item.getItem().getAgathionMaxEnergy());
         }
      }
   }
}
