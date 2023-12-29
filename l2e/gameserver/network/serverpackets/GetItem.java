package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.instance.ItemInstance;

public final class GetItem extends GameServerPacket {
   private final ItemInstance _item;
   private final int _playerId;

   public GetItem(ItemInstance item, int playerId) {
      this._item = item;
      this._playerId = playerId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._playerId);
      this.writeD(this._item.getObjectId());
      this.writeD(this._item.getX());
      this.writeD(this._item.getY());
      this.writeD(this._item.getZ());
   }
}
