package l2e.gameserver.network.serverpackets;

public class ExAutoSoulShot extends GameServerPacket {
   private final int _itemId;
   private final int _type;

   public ExAutoSoulShot(int itemId, int type) {
      this._itemId = itemId;
      this._type = type;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._itemId);
      this.writeD(this._type);
   }
}
