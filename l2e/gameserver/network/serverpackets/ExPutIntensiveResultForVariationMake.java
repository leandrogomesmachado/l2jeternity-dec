package l2e.gameserver.network.serverpackets;

public class ExPutIntensiveResultForVariationMake extends GameServerPacket {
   private final int _refinerItemObjId;
   private final int _lifestoneItemId;
   private final int _gemstoneItemId;
   private final int _gemstoneCount;
   private final int _unk2;

   public ExPutIntensiveResultForVariationMake(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, int gemstoneCount) {
      this._refinerItemObjId = refinerItemObjId;
      this._lifestoneItemId = lifeStoneId;
      this._gemstoneItemId = gemstoneItemId;
      this._gemstoneCount = gemstoneCount;
      this._unk2 = 1;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._refinerItemObjId);
      this.writeD(this._lifestoneItemId);
      this.writeD(this._gemstoneItemId);
      this.writeQ((long)this._gemstoneCount);
      this.writeD(this._unk2);
   }
}
