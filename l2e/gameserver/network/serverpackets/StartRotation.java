package l2e.gameserver.network.serverpackets;

public final class StartRotation extends GameServerPacket {
   private final int _charObjId;
   private final int _degree;
   private final int _side;
   private final int _speed;

   public StartRotation(int objectId, int degree, int side, int speed) {
      this._charObjId = objectId;
      this._degree = degree;
      this._side = side;
      this._speed = speed;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._degree);
      this.writeD(this._side);
      this.writeD(this._speed);
   }
}
