package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.items.instance.ItemInstance;

public class DropItem extends GameServerPacket {
   private final ItemInstance _item;
   private final int _charObjId;

   public DropItem(ItemInstance item, int playerObjId) {
      this._item = item;
      this._charObjId = playerObjId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._item.getObjectId());
      this.writeD(this._item.getDisplayId());
      this.writeD(this._item.getX());
      this.writeD(this._item.getY());
      this.writeD(this._item.getZ() - Config.CLIENT_SHIFTZ);
      this.writeD(this._item.isStackable() ? 1 : 0);
      this.writeQ(this._item.getCount());
      this.writeD(1);
   }
}
