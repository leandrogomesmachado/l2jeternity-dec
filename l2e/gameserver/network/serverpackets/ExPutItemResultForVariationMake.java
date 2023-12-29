package l2e.gameserver.network.serverpackets;

public class ExPutItemResultForVariationMake extends GameServerPacket {
   private final int _itemObjId;
   private final int _itemId;

   public ExPutItemResultForVariationMake(int itemObjId, int itemId) {
      this._itemObjId = itemObjId;
      this._itemId = itemId;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._itemObjId);
      this.writeD(this._itemId);
      this.writeD(1);
   }
}
