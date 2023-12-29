package l2e.gameserver.network.serverpackets;

public final class ChooseInventoryItem extends GameServerPacket {
   private final int _itemId;

   public ChooseInventoryItem(int itemId) {
      this._itemId = itemId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._itemId);
   }
}
