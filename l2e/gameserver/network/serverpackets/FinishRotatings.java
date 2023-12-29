package l2e.gameserver.network.serverpackets;

public class FinishRotatings extends GameServerPacket {
   private final int _charObjId;
   private final int _degree;
   private final int _speed;

   public FinishRotatings(int objectId, int degree, int speed) {
      this._charObjId = objectId;
      this._degree = degree;
      this._speed = speed;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._degree);
      this.writeD(this._speed);
      this.writeC(0);
   }
}
