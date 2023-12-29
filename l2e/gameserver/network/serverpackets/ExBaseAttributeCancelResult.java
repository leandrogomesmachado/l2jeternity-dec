package l2e.gameserver.network.serverpackets;

public class ExBaseAttributeCancelResult extends GameServerPacket {
   private final int _objId;
   private final byte _attribute;

   public ExBaseAttributeCancelResult(int objId, byte attribute) {
      this._objId = objId;
      this._attribute = attribute;
   }

   @Override
   protected void writeImpl() {
      this.writeD(1);
      this.writeD(this._objId);
      this.writeD(this._attribute);
   }
}
