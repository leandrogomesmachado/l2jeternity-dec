package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public class ExMoveToLocationInAirShip extends GameServerPacket {
   private final int _charObjId;
   private final int _airShipId;
   private final Location _destination;
   private final int _heading;

   public ExMoveToLocationInAirShip(Player player) {
      this._charObjId = player.getObjectId();
      this._airShipId = player.getAirShip().getObjectId();
      this._destination = player.getInVehiclePosition();
      this._heading = player.getHeading();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._airShipId);
      this.writeD(this._destination.getX());
      this.writeD(this._destination.getY());
      this.writeD(this._destination.getZ());
      this.writeD(this._heading);
   }
}
