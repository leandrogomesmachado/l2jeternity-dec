package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.instance.BoatInstance;

public class VehicleDeparture extends GameServerPacket {
   private final int _objId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _moveSpeed;
   private final int _rotationSpeed;

   public VehicleDeparture(BoatInstance boat) {
      this._objId = boat.getObjectId();
      this._x = boat.getXdestination();
      this._y = boat.getYdestination();
      this._z = boat.getZdestination();
      this._moveSpeed = (int)boat.getStat().getMoveSpeed();
      this._rotationSpeed = boat.getStat().getRotationSpeed();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._moveSpeed);
      this.writeD(this._rotationSpeed);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
