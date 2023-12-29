package l2e.gameserver.network.serverpackets;

public class ExRotation extends GameServerPacket {
   private final int _charObjId;
   private final int _degree;

   public ExRotation(int charId, int degree) {
      this._charObjId = charId;
      this._degree = degree;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._degree);
   }
}
