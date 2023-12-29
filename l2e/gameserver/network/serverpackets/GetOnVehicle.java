package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;

public class GetOnVehicle extends GameServerPacket {
   private final int _charObjId;
   private final int _boatObjId;
   private final Location _pos;

   public GetOnVehicle(int charObjId, int boatObjId, Location pos) {
      this._charObjId = charObjId;
      this._boatObjId = boatObjId;
      this._pos = pos;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._boatObjId);
      this.writeD(this._pos.getX());
      this.writeD(this._pos.getY());
      this.writeD(this._pos.getZ());
   }
}
