package l2e.gameserver.network.serverpackets;

public class ExRequestChangeNicknameColor extends GameServerPacket {
   private final int _itemObjectId;

   public ExRequestChangeNicknameColor(int itemObjectId) {
      this._itemObjectId = itemObjectId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._itemObjectId);
   }
}
