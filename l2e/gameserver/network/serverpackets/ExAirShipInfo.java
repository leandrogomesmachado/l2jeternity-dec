package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.instance.AirShipInstance;

public class ExAirShipInfo extends GameServerPacket {
   private final AirShipInstance _ship;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;
   private final int _moveSpeed;
   private final int _rotationSpeed;
   private final int _captain;
   private final int _helm;

   public ExAirShipInfo(AirShipInstance ship) {
      this._ship = ship;
      this._x = ship.getX();
      this._y = ship.getY();
      this._z = ship.getZ();
      this._heading = ship.getHeading();
      this._moveSpeed = (int)ship.getStat().getMoveSpeed();
      this._rotationSpeed = ship.getStat().getRotationSpeed();
      this._captain = ship.getCaptainId();
      this._helm = ship.getHelmObjectId();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._ship.getObjectId());
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
      this.writeD(this._captain);
      this.writeD(this._moveSpeed);
      this.writeD(this._rotationSpeed);
      this.writeD(this._helm);
      if (this._helm != 0) {
         this.writeD(366);
         this.writeD(0);
         this.writeD(107);
         this.writeD(348);
         this.writeD(0);
         this.writeD(105);
      } else {
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
      }

      this.writeD(this._ship.getFuel());
      this.writeD(this._ship.getMaxFuel());
   }
}
