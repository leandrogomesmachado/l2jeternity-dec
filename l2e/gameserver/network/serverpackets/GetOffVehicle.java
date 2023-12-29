package l2e.gameserver.network.serverpackets;

public class GetOffVehicle extends GameServerPacket {
   private final int _charObjId;
   private final int _boatObjId;
   private final int _x;
   private final int _y;
   private final int _z;

   public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z) {
      this._charObjId = charObjId;
      this._boatObjId = boatObjId;
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._boatObjId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
