package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ExChooseInventoryAttributeItem extends GameServerPacket {
   private final int _itemId;
   private final byte _atribute;
   private final int _level;

   public ExChooseInventoryAttributeItem(ItemInstance item) {
      this._itemId = item.getDisplayId();
      this._atribute = Elementals.getItemElement(this._itemId);
      if (this._atribute == -1) {
         throw new IllegalArgumentException("Undefined Atribute item: " + item);
      } else {
         this._level = Elementals.getMaxElementLevel(this._itemId);
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._itemId);
      this.writeD(this._atribute == 0 ? 1 : 0);
      this.writeD(this._atribute == 1 ? 1 : 0);
      this.writeD(this._atribute == 2 ? 1 : 0);
      this.writeD(this._atribute == 3 ? 1 : 0);
      this.writeD(this._atribute == 4 ? 1 : 0);
      this.writeD(this._atribute == 5 ? 1 : 0);
      this.writeD(this._level);
   }
}
