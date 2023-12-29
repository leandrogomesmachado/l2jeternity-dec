package l2e.gameserver.network.serverpackets;

public class ExPutCommissionResultForVariationMake extends GameServerPacket {
   private final int _gemstoneObjId;
   private final int _itemId;
   private final long _gemstoneCount;
   private final int _unk1;
   private final int _unk2;
   private final int _unk3;

   public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count, int itemId) {
      this._gemstoneObjId = gemstoneObjId;
      this._itemId = itemId;
      this._gemstoneCount = count;
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 1;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._gemstoneObjId);
      this.writeD(this._itemId);
      this.writeQ(this._gemstoneCount);
      this.writeD(this._unk1);
      this.writeD(this._unk2);
      this.writeD(this._unk3);
   }
}
