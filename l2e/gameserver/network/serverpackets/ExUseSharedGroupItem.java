package l2e.gameserver.network.serverpackets;

public class ExUseSharedGroupItem extends GameServerPacket {
   private final int _itemId;
   private final int _grpId;
   private final int _remainingTime;
   private final int _totalTime;

   public ExUseSharedGroupItem(int itemId, int grpId, long remainingTime, int totalTime) {
      this._itemId = itemId;
      this._grpId = grpId;
      this._remainingTime = (int)(remainingTime / 1000L);
      this._totalTime = totalTime / 1000;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._itemId);
      this.writeD(this._grpId);
      this.writeD(this._remainingTime);
      this.writeD(this._totalTime);
   }
}
