package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExStopMoveAirShip extends GameServerPacket {
   private final int _objectId;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;

   public ExStopMoveAirShip(Creature ship) {
      this._objectId = ship.getObjectId();
      this._x = ship.getX();
      this._y = ship.getY();
      this._z = ship.getZ();
      this._heading = ship.getHeading();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
   }
}
