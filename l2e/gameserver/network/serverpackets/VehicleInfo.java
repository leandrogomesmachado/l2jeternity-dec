package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.instance.BoatInstance;

public class VehicleInfo extends GameServerPacket {
   private final int _objId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;

   public VehicleInfo(BoatInstance boat) {
      this._objId = boat.getObjectId();
      this._x = boat.getX();
      this._y = boat.getY();
      this._z = boat.getZ();
      this._heading = boat.getHeading();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
   }
}
